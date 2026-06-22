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
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main entry point for test smell analysis.
 * Orchestrates the detection of test smells across Java projects,
 * supporting parallel file processing and multiple JUnit versions.
 */
public class JNoseCore {

    private final static Logger LOGGER = Logger.getLogger(JNoseCore.class.getName());

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
     * @return list of {@link TestClass} results with detected test smells
     * @throws Exception if analysis fails
     */
    public List<TestClass> getFilesTest(String directoryPath) throws Exception {
        int numberThread = Runtime.getRuntime().availableProcessors() * 2;
        ExecutorService threadpool = Executors.newFixedThreadPool(numberThread);
        try {
            return getFilesTest(directoryPath, threadpool);
        } finally {
            if (!threadpool.isShutdown()) {
                threadpool.shutdown();
            }
            threadpool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        }
    }

    /**
     * Analyzes all test files in the given directory using the provided thread pool.
     * @param directoryPath path to the project root directory
     * @param threadpool    executor service for parallel processing
     * @return list of {@link TestClass} results with detected test smells
     * @throws Exception if analysis fails
     */
    public List<TestClass> getFilesTest(String directoryPath, ExecutorService threadpool) throws Exception {
        String projectName = directoryPath.substring(directoryPath.lastIndexOf(File.separatorChar) + 1);
        Path startDir = Paths.get(directoryPath);

        Map<String, String> fileMap = new ConcurrentHashMap<>();
        List<Path> javaFiles = new ArrayList<>();

        try (var paths = Files.walk(startDir).filter(Files::isRegularFile)) {
            paths.forEach(filePath -> {
                if (filePath.toString().toLowerCase().endsWith(".java")) {
                    fileMap.put(filePath.getFileName().toString().toLowerCase(), filePath.toString());
                    javaFiles.add(filePath);
                }
            });
        }

        List<Future<List<TestClass>>> futures = new ArrayList<>();
        for (Path filePath : javaFiles) {
            JNoseCallable callable = new JNoseCallable(filePath, projectName, startDir, this, fileMap);
            futures.add(threadpool.submit(callable));
        }

        List<TestClass> files = new ArrayList<>();
        for (Future<List<TestClass>> future : futures) {
            files.addAll(future.get());
        }
        return files;
    }


    /**
     * Determines if a file is a test file by parsing its AST and checking for JUnit annotations.
     * Updates the test class with line count and JUnit version information.
     * @param testClass the test class to check
     * @return true if the file is a test file (contains JUnit annotations or @Test methods)
     */
    public boolean isTestFile(TestClass testClass) {
        boolean isTestFile = false;
        try (FileInputStream fileInputStream = new FileInputStream(new File(testClass.getPathFile()))) {
            CompilationUnit compilationUnit = JavaParser.parse(fileInputStream);
            testClass.setNumberLine(compilationUnit.getRange().get().end.line);
            detectJUnitVersion(compilationUnit.getImports(), testClass);
            List<NodeList<?>> nodeList = compilationUnit.getNodeLists();
            for (NodeList<?> node : nodeList) {
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
        for (ImportDeclaration node : nodeList) {
            if (node.getNameAsString().contains("org.junit.jupiter")) {
                testClass.setJunitVersion(TestClass.JunitVersion.JUnit5);
                break;
            } else if (node.getNameAsString().contains("org.junit")) {
                testClass.setJunitVersion(TestClass.JunitVersion.JUnit4);
                break;
            } else if (node.getNameAsString().contains("junit.framework")) {
                testClass.setJunitVersion(TestClass.JunitVersion.JUnit3);
                break;
            } else {
                testClass.setJunitVersion(TestClass.JunitVersion.None);
            }
        }
    }


    /**
     * Scans a project directory to determine the JUnit version used.
     * Walks through all Java files and checks imports for JUnit references.
     * @param directoryPath path to the project root directory
     * @return the detected JUnit version
     */
    public TestClass.JunitVersion getJUnitVersion(String directoryPath) {
        String projectName = directoryPath.substring(directoryPath.lastIndexOf(File.separatorChar) + 1);

        final TestClass.JunitVersion[] jUnitVersion = {TestClass.JunitVersion.None};

        Path startDir = Paths.get(directoryPath);
        try {
            Files.walk(startDir)
                    .filter(Files::isRegularFile)
                    .forEach(filePath -> {
                        if (filePath.getFileName().toString().lastIndexOf(".") != -1) {
                            String fileNameWithoutExtension = filePath.getFileName().toString().substring(0, filePath.getFileName().toString().lastIndexOf(".")).toLowerCase();
                            if (filePath.toString().toLowerCase().endsWith(".java") && (
                                    fileNameWithoutExtension.matches("^.*test\\d*$") ||
                                            fileNameWithoutExtension.matches("^.*testcase\\d*") ||
                                            fileNameWithoutExtension.matches("^.*tests\\d*$") ||
                                            fileNameWithoutExtension.matches("^test.*") ||
                                            fileNameWithoutExtension.matches("^testcase.*") ||
                                            fileNameWithoutExtension.matches("^tests.*"))) {
                                TestClass testClass = new TestClass();
                                testClass.setProjectName(projectName);
                                testClass.setPathFile(filePath.toString());
                                if (isTestFile(testClass)) {
                                    if(testClass.getJunitVersion() == null){
                                        testClass.setJunitVersion(TestClass.JunitVersion.None);
                                        jUnitVersion[0] = TestClass.JunitVersion.None;
                                    }
                                    if(!testClass.getJunitVersion().equals(TestClass.JunitVersion.None)){
                                        jUnitVersion[0] = testClass.getJunitVersion();
                                    }
                                }
                            }
                        }
                    });
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "getJUnitVersion: error walking files", e);
        }
        return jUnitVersion[0];
    }

    /**
     * Recursively traverses the AST to find class and method declarations.
     * Checks if any method has a @Test annotation, indicating a test file.
     * @return true if a test annotation was found
     */
    private boolean flowClass(NodeList<?> nodeList, TestClass testClass) {
        boolean isTestClass = false;
        for (Object node : nodeList) {
            if (node instanceof ClassOrInterfaceDeclaration) {
                ClassOrInterfaceDeclaration classAtual = ((ClassOrInterfaceDeclaration) node);
                testClass.setName(classAtual.getNameAsString());
                testClass.setFullName(classAtual.getName().toString());
                NodeList<?> nodeList_members = classAtual.getMembers();
                testClass.setNumberMethods(classAtual.getMembers().size());
                isTestClass = flowClass(nodeList_members, testClass);
                if(isTestClass)return true;
            } else if (node instanceof MethodDeclaration) {
                isTestClass = flowClass(((MethodDeclaration) node).getAnnotations(), testClass);
                if(isTestClass)return true;
            } else if (node instanceof AnnotationExpr) {
                if(((AnnotationExpr) node).getNameAsString().toLowerCase().contains("test")){
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
        final String[] retorno = {""};
        try {
            Path startDir = Paths.get(directoryPath);
            Files.walk(startDir)
                    .filter(Files::isRegularFile)
                    .forEach(filePath -> {
                        if (filePath.getFileName().toString().toLowerCase().equals(productionFileName.toLowerCase())) {
                            retorno[0] = filePath.toString();
                        }
                    });
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
        TestSmellDetector testSmellDetector = TestSmellDetector.createTestSmellDetector(config);

        TestFile testFile = new TestFile(testClass.getProjectName(), testClass.getPathFile(), testClass.getProductionFile(), testClass.getNumberLine(), testClass.getNumberMethods());

        try {
            TestFile tempFile = testSmellDetector.detectSmells(testFile);
            for (AbstractSmell smell : tempFile.getTestSmells()) {
                if (smell == null) continue;
                for (SmellyElement smellyElement : smell.getSmellyElements()) {
                    if (smellyElement.getHasSmell()) {
                        TestSmell testSmell = new TestSmell();
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

        Map<String,Integer> mapaSoma = new HashMap<>();

        for (String smellName : TestSmellDetector.getAllTestSmellNames()) {
            mapaSoma.put(smellName, 0);
        }

        for(TestSmell testsmells : testClass.getListTestSmell()){
            if(mapaSoma.get(testsmells.getName()) == null){
                mapaSoma.put(testsmells.getName(),0);
            }

            Integer valorAtual = mapaSoma.get(testsmells.getName());
            mapaSoma.put(testsmells.getName(),valorAtual+1);
        }

        testClass.setLineSumTestSmells(mapaSoma);
    }

}
