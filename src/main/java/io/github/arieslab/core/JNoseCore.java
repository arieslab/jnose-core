package io.github.arieslab.core;

import io.github.arieslab.core.testsmelldetector.testsmell.smell.VerboseTest;
import io.github.arieslab.dto.*;
import io.github.arieslab.core.testsmelldetector.testsmell.AbstractSmell;
import io.github.arieslab.core.testsmelldetector.testsmell.SmellyElement;
import io.github.arieslab.core.testsmelldetector.testsmell.TestFile;
import io.github.arieslab.core.testsmelldetector.testsmell.TestSmellDetector;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Main entry point for test smell analysis.
 * Orchestrates the detection of test smells across Java projects,
 * supporting parallel file processing and multiple JUnit versions.
 */
public class JNoseCore {

    private final static Logger LOGGER = Logger.getLogger(JNoseCore.class.getName());

    private static final Pattern TEST_SUFFIX = Pattern.compile("^.*test\\d*$");
    private static final Pattern TEST_CASE_SUFFIX = Pattern.compile("^.*testcase\\d*$");
    private static final Pattern TESTS_SUFFIX = Pattern.compile("^.*tests\\d*$");
    private static final Pattern TEST_PREFIX = Pattern.compile("^test.*");
    private static final Pattern TEST_CASE_PREFIX = Pattern.compile("^testcase.*");
    private static final Pattern TESTS_PREFIX = Pattern.compile("^tests.*");

    private Config config;

    /**
     * Creates a new JNoseCore instance with the given configuration.
     * @param config detection configuration (which smells to enable, thresholds)
     */
    public JNoseCore(Config config) {
        this.config = config;
        VerboseTest.MAX_STATEMENTS = config.maxStatements();
    }

    /**
     * Analyzes all test files in the given directory, using all available processors.
     * @param directoryPath path to the project root directory
     * @return list of `TestClass` results with detected test smells
     * @throws Exception if analysis fails
     */
    public List<TestClass> getFilesTest(String directoryPath) throws Exception {
        try (var threadpool = Executors.newVirtualThreadPerTaskExecutor()) {
            return getFilesTest(directoryPath, threadpool);
        }
    }

    /**
     * Analyzes all test files in the given directory using the provided thread pool.
     * @param directoryPath path to the project root directory
     * @param threadpool    executor service for parallel processing
     * @return list of `TestClass` results with detected test smells
     * @throws Exception if analysis fails
     */
    public List<TestClass> getFilesTest(String directoryPath, ExecutorService threadpool) throws Exception {
        var projectName = directoryPath.substring(directoryPath.lastIndexOf(File.separatorChar) + 1);
        var startDir = Paths.get(directoryPath);

        var fileMap = new ConcurrentHashMap<String, String>();
        var javaFiles = new ArrayList<Path>();

        try (Stream<Path> paths = Files.walk(startDir).filter(Files::isRegularFile).filter(JNoseCore::isNotInBuildDir)) {
            paths.forEach(filePath -> {
                if (filePath.toString().toLowerCase().endsWith(".java")) {
                    fileMap.put(filePath.getFileName().toString().toLowerCase(), filePath.toString());
                    javaFiles.add(filePath);
                }
            });
        }

        var futures = new ArrayList<Future<List<TestClass>>>();
        for (var filePath : javaFiles) {
            var callable = new JNoseCallable(filePath, projectName, startDir, this, fileMap);
            futures.add(threadpool.submit(callable));
        }

        var files = new ArrayList<TestClass>();
        for (var future : futures) {
            files.addAll(future.get());
        }
        return files;
    }


    /**
     * Common build/output directory names to skip during file walking.
     */
    private static final Set<String> BUILD_DIRS = Set.of("target", "build", "classes", ".git", "node_modules", "out", "bin", "dist");

    /**
     * Returns true if the path is NOT inside a common build/output directory.
     */
    private static boolean isNotInBuildDir(Path path) {
        for (var i = 0; i < path.getNameCount(); i++) {
            if (BUILD_DIRS.contains(path.getName(i).toString())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Determines if a file is a test file by parsing its AST and checking for JUnit annotations.
     * Updates the test class with line count and JUnit version information.
     * @param testClass the test class to check
     * @return true if the file is a test file (contains JUnit annotations or @Test methods)
     */
    public boolean isTestFile(TestClass testClass) {
        var isTestFile = false;
        try (var fileInputStream = Files.newInputStream(Path.of(testClass.getPathFile()))) {
            var compilationUnit = JavaParser.parse(fileInputStream);
            testClass.setNumberLine(compilationUnit.getRange().get().end.line);
            detectJUnitVersion(compilationUnit.getImports(), testClass);
            var nodeList = compilationUnit.getNodeLists();
            for (var node : nodeList) {
                isTestFile = flowClass(node, testClass);
            }

            if(testClass.getJunitVersion() != null && testClass.getJunitVersion() != TestClass.JunitVersion.None){
                isTestFile = true;
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "isTestFile: error parsing file", e);
        }
        return isTestFile;
    }

    /**
     * Detects the JUnit version used by a test class based on its imports.
     * @param nodeList  the list of import declarations
     * @param testClass the test class to update with the detected version
     */
    public void detectJUnitVersion(NodeList<ImportDeclaration> nodeList, TestClass testClass) {
        for (var node : nodeList) {
            var name = node.getNameAsString();
            testClass.setJunitVersion(switch (name) {
                case String s when s.contains("org.junit.jupiter") -> TestClass.JunitVersion.JUnit5;
                case String s when s.contains("org.junit") -> TestClass.JunitVersion.JUnit4;
                case String s when s.contains("junit.framework") -> TestClass.JunitVersion.JUnit3;
                default -> TestClass.JunitVersion.None;
            });
            if (testClass.getJunitVersion() != TestClass.JunitVersion.None) break;
        }
    }


    /**
     * Scans a project directory to determine the JUnit version used.
     * Walks through all Java files and checks imports for JUnit references.
     * @param directoryPath path to the project root directory
     * @return the detected JUnit version
     */
    public TestClass.JunitVersion getJUnitVersion(String directoryPath) {
        var projectName = directoryPath.substring(directoryPath.lastIndexOf(File.separatorChar) + 1);

        var startDir = Paths.get(directoryPath);
        try (var files = Files.walk(startDir).filter(Files::isRegularFile)) {
            return files
                    .filter(f -> f.toString().toLowerCase().endsWith(".java"))
                    .filter(f -> isPotentialTestFileName(f.getFileName().toString()))
                    .map(f -> {
                        TestClass testClass = new TestClass();
                        testClass.setProjectName(projectName);
                        testClass.setPathFile(f.toString());
                        return testClass;
                    })
                    .filter(this::isTestFile)
                    .map(TestClass::getJunitVersion)
                    .filter(v -> v != TestClass.JunitVersion.None)
                    .findFirst()
                    .orElse(TestClass.JunitVersion.None);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "getJUnitVersion: error walking files", e);
            return TestClass.JunitVersion.None;
        }
    }

    private static boolean isPotentialTestFileName(String fileName) {
        var dotIndex = fileName.lastIndexOf(".");
        if (dotIndex == -1) return false;
        var name = fileName.substring(0, dotIndex).toLowerCase();
        return TEST_SUFFIX.matcher(name).matches() ||
               TEST_CASE_SUFFIX.matcher(name).matches() ||
               TESTS_SUFFIX.matcher(name).matches() ||
               TEST_PREFIX.matcher(name).matches() ||
               TEST_CASE_PREFIX.matcher(name).matches() ||
               TESTS_PREFIX.matcher(name).matches();
    }

    /**
     * Recursively traverses the AST to find class and method declarations.
     * Checks if any method has a @Test annotation, indicating a test file.
     * @return true if a test annotation was found
     */
    private boolean flowClass(NodeList<?> nodeList, TestClass testClass) {
        var isTestClass = false;
        for (var node : nodeList) {
            if (node instanceof ClassOrInterfaceDeclaration classAtual) {
                testClass.setName(classAtual.getNameAsString());
                testClass.setFullName(classAtual.getName().toString());
                testClass.setNumberMethods(classAtual.getMembers().size());
                isTestClass = flowClass(classAtual.getMembers(), testClass);
                if(isTestClass)return true;
            } else if (node instanceof MethodDeclaration methodDeclaration) {
                if (methodDeclaration.getAnnotationByName("Test").isPresent()) {
                    return true;
                }
                isTestClass = flowClass(methodDeclaration.getAnnotations(), testClass);
                if(isTestClass)return true;
            } else if (node instanceof AnnotationExpr annotationExpr) {
                if(annotationExpr.getNameAsString().toLowerCase().contains("test")){
                    return true;
                }
            }
        }
        return isTestClass;
    }

    /**
     * Finds the production file path corresponding to a test file name.
     * @param directoryPath     the project root directory
     * @param productionFileName the production file name to find
     * @return the full path to the production file, or empty string if not found
     */
    public String getFileProduction(String directoryPath, String productionFileName) {
        final var retorno = new String[]{""};
        try {
            var startDir = Paths.get(directoryPath);
            try (var files = Files.walk(startDir).filter(Files::isRegularFile).filter(JNoseCore::isNotInBuildDir)) {
                files.forEach(filePath -> {
                    if (filePath.getFileName().toString().toLowerCase().equals(productionFileName.toLowerCase())) {
                        retorno[0] = filePath.toString();
                    }
                });
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "getFileProduction: error finding file", e);
        }
        return retorno[0];
    }

    /**
     * Runs all enabled test smell detectors on the given test class.
     * Updates the test class with the list of detected smells.
     * @param testClass the test class to analyze
     */
    public void getTestSmells(TestClass testClass) {
        var testSmellDetector = TestSmellDetector.createTestSmellDetector(config);

        var testFile = new TestFile(testClass.getProjectName(), testClass.getPathFile(), testClass.getProductionFile(), testClass.getNumberLine(), testClass.getNumberMethods());

        try {
            var tempFile = testSmellDetector.detectSmells(testFile);
            for (var smell : tempFile.getTestSmells()) {
                if (smell == null) continue;
                for (var smellyElement : smell.getSmellyElements()) {
                    if (smellyElement.getHasSmell()) {
                        var testSmell = new TestSmell();
                        testSmell.setName(smell.getSmellName());
                        testSmell.setMethod(smellyElement.getElementName());
                        testSmell.setRange(smellyElement.getRange());
                        testSmell.setTestClass(testClass);
                        testClass.getListTestSmell().add(testSmell);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "getTestSmells: error detecting smells", e);
        }

        setLineSumTestSmells(testClass);
    }

    /**
     * Calculates the total count of each test smell type for the given test class.
     * @param testClass the test class to summarize
     */
    private void setLineSumTestSmells(TestClass testClass){
        var mapaSoma = TestSmellDetector.getAllTestSmellNames().stream()
                .collect(Collectors.toMap(Function.identity(), v -> 0));

        testClass.getListTestSmell().stream()
                .collect(Collectors.groupingBy(TestSmell::getName, Collectors.summingInt(v -> 1)))
                .forEach((k, v) -> mapaSoma.merge(k, v, Integer::sum));

        testClass.setLineSumTestSmells(mapaSoma);
    }

}
