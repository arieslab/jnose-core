package br.ufba.jnose.core;

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
import java.util.logging.Level;
import java.util.logging.Logger;

public class JNoseCore implements PropertyChangeListener {

    private final static Logger LOGGER = Logger.getLogger(JNoseCore.class.getName());

    private Config config;

    public static void main(String[] args) throws IOException {
        String directoryPath = "/home/tassio/.jnose_projects/jnose-tests/";

        Config conf = new Config() {
            @Override
            public Boolean assertionRoulette() {
                return true;
            }

            @Override
            public Boolean conditionalTestLogic() {
                return true;
            }

            @Override
            public Boolean constructorInitialization() {
                return true;
            }

            @Override
            public Boolean defaultTest() {
                return true;
            }

            @Override
            public Boolean dependentTest() {
                return true;
            }

            @Override
            public Boolean duplicateAssert() {
                return true;
            }

            @Override
            public Boolean eagerTest() {
                return true;
            }

            @Override
            public Boolean emptyTest() {
                return true;
            }

            @Override
            public Boolean exceptionCatchingThrowing() {
                return true;
            }

            @Override
            public Boolean generalFixture() {
                return true;
            }

            @Override
            public Boolean mysteryGuest() {
                return true;
            }

            @Override
            public Boolean printStatement() {
                return true;
            }

            @Override
            public Boolean redundantAssertion() {
                return true;
            }

            @Override
            public Boolean sensitiveEquality() {
                return true;
            }

            @Override
            public Boolean verboseTest() {
                return true;
            }

            @Override
            public Boolean sleepyTest() {
                return true;
            }

            @Override
            public Boolean lazyTest() {
                return true;
            }

            @Override
            public Boolean unknownTest() {
                return true;
            }

            @Override
            public Boolean ignoredTest() {
                return true;
            }

            @Override
            public Boolean resourceOptimism() {
                return true;
            }

            @Override
            public Boolean magicNumberTest() {
                return true;
            }
        };

        JNoseCore jNoseCore = new JNoseCore(conf);

        List<TestClass> lista = jNoseCore.getFilesTest(directoryPath);

        for(TestClass testClass : lista){
            System.out.println(testClass.getPathFile() + " - " + testClass.getProductionFile() + " - " + testClass.getJunitVersion());

            for (TestSmell testSmell : testClass.getListTestSmell()){
                System.out.println(testSmell.getName() + " - " + testSmell.getMethod() + " - " + testSmell.getRange());
            }
        }

    }

    public JNoseCore(Config config) {
        this.config = config;
    }

    public List<TestClass> getFilesTest(String directoryPath) throws IOException {
        LOGGER.log(Level.INFO, "getFilesTest: start");

        String projectName = directoryPath.substring(directoryPath.lastIndexOf(File.separatorChar) + 1, directoryPath.length());

        List<TestClass> files = new ArrayList<>();

        Path startDir = Paths.get(directoryPath);

        Files.walk(startDir)
                .filter(Files::isRegularFile)
                .forEach(filePath -> {
                    if (filePath.getFileName().toString().lastIndexOf(".") != -1) {
                        String fileNameWithoutExtension = filePath.getFileName().toString().substring(0, filePath.getFileName().toString().lastIndexOf(".")).toLowerCase();

                        if (filePath.toString().toLowerCase().endsWith(".java") && fileNameWithoutExtension.matches("^.*test\\d*$")) {

                            TestClass testClass = new TestClass();
                            testClass.setProjectName(projectName);
                            testClass.setPathFile(filePath.toString());

                            if (isTestFile(testClass)) {

                                LOGGER.log(Level.INFO, "getFilesTest: " + testClass.getPathFile());

                                String productionFileName = "";
                                int index = testClass.getName().toLowerCase().lastIndexOf("test");
                                if (index > 0) {
                                    productionFileName = testClass.getName().substring(0, testClass.getName().toLowerCase().lastIndexOf("test")) + ".java";
                                }
                                testClass.setProductionFile(getFileProduction(startDir.toString(), productionFileName));

                                if (!testClass.getProductionFile().isEmpty()) {
                                    getTestSmells(testClass);
                                    files.add(testClass);
                                }
                            }
                        }
                    }
                });
        return files;
    }


    public Boolean isTestFile(TestClass testClass) {
        LOGGER.log(Level.INFO, "isTestFile: start");

        Boolean isTestFile = false;
        try {
            FileInputStream fileInputStream = null;
            fileInputStream = new FileInputStream(new File(testClass.getPathFile()));
            CompilationUnit compilationUnit = JavaParser.parse(fileInputStream);
            testClass.setNumberLine(compilationUnit.getRange().get().end.line);
            detectJUnitVersion(compilationUnit.getImports(), testClass);
            List<NodeList<?>> nodeList = compilationUnit.getNodeLists();
            for (NodeList<?> node : nodeList) {
                isTestFile = flowClass(node, testClass);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isTestFile;
    }

    public void detectJUnitVersion(NodeList<ImportDeclaration> nodeList, TestClass testClass) {
        LOGGER.log(Level.INFO, "detectJUnitVersion: start");
        for (ImportDeclaration node : nodeList) {
            if (node.getNameAsString().contains("org.junit.jupiter")) {
                testClass.setJunitVersion(TestClass.JunitVersion.JUnit5);
            } else if (node.getNameAsString().contains("org.junit")) {
                testClass.setJunitVersion(TestClass.JunitVersion.JUnit4);
            } else if (node.getNameAsString().contains("junit.framework")) {
                testClass.setJunitVersion(TestClass.JunitVersion.JUnit3);
            }
        }
    }

    public TestClass.JunitVersion getJUnitVersion(String directoryPath) {
        String projectName = directoryPath.substring(directoryPath.lastIndexOf(File.separatorChar) + 1, directoryPath.length());

        final br.ufba.jnose.dto.TestClass.JunitVersion[] jUnitVersion = new br.ufba.jnose.dto.TestClass.JunitVersion[1];

        List<br.ufba.jnose.dto.TestClass> files = new ArrayList<>();
        Path startDir = Paths.get(directoryPath);
        try {
            Files.walk(startDir)
                    .filter(Files::isRegularFile)
                    .forEach(filePath -> {
                        if (filePath.getFileName().toString().lastIndexOf(".") != -1) {
                            String fileNameWithoutExtension = filePath.getFileName().toString().substring(0, filePath.getFileName().toString().lastIndexOf(".")).toLowerCase();
                            if (filePath.toString().toLowerCase().endsWith(".java") && fileNameWithoutExtension.matches("^.*test\\d*$")) {
                                br.ufba.jnose.dto.TestClass testClass = new br.ufba.jnose.dto.TestClass();
                                testClass.setProjectName(projectName);
                                testClass.setPathFile(filePath.toString());
                                if (isTestFile(testClass)) {
                                    jUnitVersion[0] = testClass.getJunitVersion();

                                }
                            }
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jUnitVersion[0];
    }

    private Boolean flowClass(NodeList<?> nodeList, TestClass testClass) {
        LOGGER.log(Level.INFO, "flowClass: start -> " + nodeList.toString());
        boolean isTestClass = false;
        for (Object node : nodeList) {
            if (node instanceof ClassOrInterfaceDeclaration) {
                ClassOrInterfaceDeclaration classAtual = ((ClassOrInterfaceDeclaration) node);
                testClass.setName(classAtual.getNameAsString());
                NodeList<?> nodeList_members = classAtual.getMembers();
                testClass.setNumberMethods(classAtual.getMembers().size());
                isTestClass = flowClass(nodeList_members, testClass);
                if(isTestClass)return true;
            } else if (node instanceof MethodDeclaration) {
                isTestClass = flowClass(((MethodDeclaration) node).getAnnotations(), testClass);
                if(isTestClass)return true;
            } else if (node instanceof AnnotationExpr) {
                return ((AnnotationExpr) node).getNameAsString().toLowerCase().contains("test");
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
            e.printStackTrace();
        }
        return retorno[0];
    }

    public void getTestSmells(TestClass testClass) {
        LOGGER.log(Level.INFO, "getTestSmells: start");

        TestSmellDetector testSmellDetector = TestSmellDetector.createTestSmellDetector(config);

        TestFile testFile = new TestFile(testClass.getProjectName(), testClass.getPathFile().toString(), testClass.getProductionFile(), testClass.getNumberLine(), testClass.getNumberMethods());

        try {
            TestFile tempFile = testSmellDetector.detectSmells(testFile);
            for (AbstractSmell smell : tempFile.getTestSmells()) {
                smell.getSmellyElements();
                for (SmellyElement smellyElement : smell.getSmellyElements()) {
                    if (smellyElement.getHasSmell()) {
                        TestSmell testSmell = new TestSmell();
                        testSmell.setName(smell.getSmellName());
                        testSmell.setMethod(smellyElement.getElementName());
                        testSmell.setRange(smellyElement.getRange());
                        testClass.getListTestSmell().add(testSmell);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {

    }
}

