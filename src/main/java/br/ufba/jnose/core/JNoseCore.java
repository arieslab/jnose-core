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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JNoseCore implements PropertyChangeListener{

    private final static Logger LOGGER = Logger.getLogger(JNoseCore.class.getName());

    private Config config;

    public static void main(String[] args) throws Exception {
        String directoryPath = "C:\\Users\\Tássio\\Desenvolvimento\\repo.git\\KTestSmells\\tmp\\commons-io";

        Config conf = new Config() {
            @Override
            public boolean assertionRoulette() {
                return true;
            }
           

            @Override
            public boolean conditionalTestLogic() {
                return true;
            }

            @Override
            public boolean constructorInitialization() {
                return true;
            }

            @Override
            public boolean defaultTest() {
                return true;
            }

            @Override
            public boolean dependentTest() {
                return true;
            }

            @Override
            public boolean duplicateAssert() {
                return true;
            }

            @Override
            public boolean eagerTest() {
                return true;
            }

            @Override
            public boolean emptyTest() {
                return true;
            }

            @Override
            public boolean exceptionCatchingThrowing() {
                return true;
            }

            @Override
            public boolean generalFixture() {
                return true;
            }

            @Override
            public boolean mysteryGuest() {
                return true;
            }

            @Override
            public boolean printStatement() {
                return true;
            }

            @Override
            public boolean redundantAssertion() {
                return true;
            }

            @Override
            public boolean sensitiveEquality() {
                return true;
            }

            @Override
            public boolean verboseTest() {
                return true;
            }

            @Override
            public boolean sleepyTest() {
                return true;
            }

            @Override
            public boolean lazyTest() {
                return true;
            }

            @Override
            public boolean unknownTest() {
                return true;
            }

            @Override
            public boolean ignoredTest() {
                return true;
            }

            @Override
            public boolean resourceOptimism() {
                return true;
            }

            @Override
            public boolean magicNumberTest() {
                return true;
            }

            @Override
            public int maxStatements() {
                return 30;
            }
        };

        JNoseCore jNoseCore = new JNoseCore(conf, 3);

        List<TestClass> lista = jNoseCore.getFilesTest(directoryPath);

        for(TestClass testClass : lista){
            for (TestSmell testSmell : testClass.getListTestSmell()){
                System.out.println(
                            testClass.getPathFile() + ";" +
                            testClass.getProductionFile() + ";" +
                            testClass.getJunitVersion() + ";" +
                            testSmell.getName() + ";" +
                            testSmell.getMethod() + ";" +
                            testSmell.getRange()
                );
            }

//            System.out.println(testClass.getLineSumTestSmells());
        }

    }

    public JNoseCore(Config config, int numberThread) {
        this.config = config;
        VerboseTest.MAX_STATEMENTS = config.maxStatements();
    }

    public List<TestClass> getFilesTest(String directoryPath) throws Exception {
        LOGGER.log(Level.INFO, "getFilesTest: start");

        int numberThread = Runtime.getRuntime().availableProcessors() * 2;
        ExecutorService threadpool = Executors.newFixedThreadPool(numberThread);

        try {
            String projectName = directoryPath.substring(directoryPath.lastIndexOf(File.separatorChar) + 1, directoryPath.length());

            List<TestClass> files = new ArrayList<>();
            Path startDir = Paths.get(directoryPath);
            List<Future<List<TestClass>>> futures = new ArrayList<>();

            Files.walk(startDir)
                    .filter(Files::isRegularFile)
                    .forEach(filePath -> {
                        JNoseCallable jNoseCallable = new JNoseCallable(filePath, projectName, startDir, this);
                        Future<List<TestClass>> future = threadpool.submit(jNoseCallable);
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
        LOGGER.log(Level.INFO, "isTestFile: start");

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
        LOGGER.log(Level.INFO, "detectJUnitVersion: start");
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
        String projectName = directoryPath.substring(directoryPath.lastIndexOf(File.separatorChar) + 1, directoryPath.length());

        final br.ufba.jnose.dto.TestClass.JunitVersion[] jUnitVersion = new br.ufba.jnose.dto.TestClass.JunitVersion[1];

        jUnitVersion[0] = TestClass.JunitVersion.None;

        List<br.ufba.jnose.dto.TestClass> files = new ArrayList<>();
        Path startDir = Paths.get(directoryPath);
        try {
            Files.walk(startDir)
                    .filter(Files::isRegularFile)
                    .forEach(filePath -> {
                        if(filePath.getFileName().toString().toLowerCase().contains("loadtestcase")){
                            LOGGER.log(Level.FINE, "found: {0}", filePath.getFileName());
                        }
                        if (filePath.getFileName().toString().lastIndexOf(".") != -1) {
                            String fileNameWithoutExtension = filePath.getFileName().toString().substring(0, filePath.getFileName().toString().lastIndexOf(".")).toLowerCase();
//                            if (filePath.toString().toLowerCase().endsWith(".java") && fileNameWithoutExtension.matches("^.*test\\d*$")) {
                            if (filePath.toString().toLowerCase().endsWith(".java") && (
                                    fileNameWithoutExtension.matches("^.*test\\d*$") ||
                                            fileNameWithoutExtension.matches("^.*testcase\\d*") ||
                                            fileNameWithoutExtension.matches("^.*tests\\d*$") ||
                                            fileNameWithoutExtension.matches("^test.*") ||
                                            fileNameWithoutExtension.matches("^testcase.*") ||
                                            fileNameWithoutExtension.matches("^tests.*"))) {
                                br.ufba.jnose.dto.TestClass testClass = new br.ufba.jnose.dto.TestClass();
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
        LOGGER.log(Level.INFO, "flowClass: start -> " + nodeList.toString());
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
        LOGGER.log(Level.INFO, "getFileProduction: start");
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
        LOGGER.log(Level.INFO, "getTestSmells: start");

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

        List<TestSmell> listTestSmells = testClass.getListTestSmell();

        String[] lista = {"Unknown Test","IgnoredTest","Resource Optimism","Magic Number Test","Redundant Assertion","Sensitive Equality","Verbose Test","Sleepy Test","Lazy Test","Duplicate Assert","Eager Test","Assertion Roulette","Conditional Test Logic","Constructor Initialization","Default Test","EmptyTest","Exception Catching Throwing","General Fixture","Mystery Guest","Print Statement","Dependent Test"};
        for(String testsmellsName : lista){
            if(mapaSoma.get(testsmellsName) == null){
                mapaSoma.put(testsmellsName,0);
            }
        }

        for(TestSmell testsmells : listTestSmells){
            if(mapaSoma.get(testsmells.getName()) == null){
                mapaSoma.put(testsmells.getName(),0);
            }

            Integer valorAtual = mapaSoma.get(testsmells.getName());
            mapaSoma.put(testsmells.getName(),valorAtual+1);
        }

        testClass.setLineSumTestSmells(mapaSoma);
    }


    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {

    }
}

