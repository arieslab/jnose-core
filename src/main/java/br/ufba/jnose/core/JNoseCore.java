package br.ufba.jnose.core;

import br.ufba.jnose.core.testsmelldetector.testsmell.smell.VerboseTest;
import br.ufba.jnose.dto.*;
import br.ufba.jnose.core.testsmelldetector.testsmell.AbstractSmell;
import br.ufba.jnose.core.testsmelldetector.testsmell.SmellyElement;
import br.ufba.jnose.core.testsmelldetector.testsmell.TestFile;
import br.ufba.jnose.core.testsmelldetector.testsmell.TestSmellDetector;
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

public class JNoseCore {

    private final static Logger LOGGER = Logger.getLogger(JNoseCore.class.getName());

    private Config config;

    public JNoseCore(Config config) {
        this.config = config;
        VerboseTest.MAX_STATEMENTS = config.maxStatements();
    }

    public List<TestClass> getFilesTest(String directoryPath) throws Exception {
        int numberThread = Runtime.getRuntime().availableProcessors() * 2;
        ExecutorService threadpool = Executors.newFixedThreadPool(numberThread);

        try {
            String projectName = directoryPath.substring(directoryPath.lastIndexOf(File.separatorChar) + 1);
            Path startDir = Paths.get(directoryPath);

            Map<String, String> fileMap = new ConcurrentHashMap<>();
            try (var paths = Files.walk(startDir).filter(Files::isRegularFile)) {
                paths.forEach(path -> {
                    if (path.toString().toLowerCase().endsWith(".java")) {
                        fileMap.put(path.getFileName().toString().toLowerCase(), path.toString());
                    }
                });
            }

            List<TestClass> files = new ArrayList<>();
            List<Future<List<TestClass>>> futures = new ArrayList<>();

            Files.walk(startDir)
                    .filter(Files::isRegularFile)
                    .forEach(filePath -> {
                        JNoseCallable callable = new JNoseCallable(filePath, projectName, startDir, this, fileMap);
                        Future<List<TestClass>> future = threadpool.submit(callable);
                        futures.add(future);
                    });

            threadpool.shutdown();
            threadpool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

            for (Future<List<TestClass>> future : futures) {
                files.addAll(future.get());
            }

            return files;
        } finally {
            if (!threadpool.isShutdown()) {
                threadpool.shutdownNow();
            }
        }
    }


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
