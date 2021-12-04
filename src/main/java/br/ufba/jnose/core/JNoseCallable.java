package br.ufba.jnose.core;

import br.ufba.jnose.dto.TestClass;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JNoseCallable implements Callable<List<TestClass>> {

    private final static Logger LOGGER = Logger.getLogger(JNoseCallable.class.getName());

    private Path filePath;
    private String projectName;
    private Path startDir;
    private JNoseCore jNoseCore;


    public JNoseCallable(Path filePath, String projectName, Path startDir, JNoseCore jNoseCore){
        this.filePath = filePath;
        this.projectName = projectName;
        this.startDir = startDir;
        this.jNoseCore = jNoseCore;
    }

    @Override
    public List<TestClass> call() throws Exception {
        List<TestClass> files = new ArrayList<>();

        if (filePath.getFileName().toString().lastIndexOf(".") != -1) {
            String fileNameWithoutExtension = filePath.getFileName().toString().substring(0, filePath.getFileName().toString().lastIndexOf(".")).toLowerCase();

            if (filePath.toString().toLowerCase().endsWith(".java") && (
                    fileNameWithoutExtension.matches("^.*test\\d*$") ||
                            fileNameWithoutExtension.matches("^.*tests\\d*$") ||
                            fileNameWithoutExtension.matches("^test.*") ||
                            fileNameWithoutExtension.matches("^tests.*"))) {

                Boolean testTrueFinal = fileNameWithoutExtension.matches("^.*test\\d*$");
                Boolean testsTrueFinal = fileNameWithoutExtension.matches("^.*tests\\d*$");

                Boolean testTrueInicio = fileNameWithoutExtension.matches("^test.*");
                Boolean testsTrueInicio = fileNameWithoutExtension.matches("^tests.*");

                TestClass testClass = new TestClass();
                testClass.setProjectName(projectName);
                testClass.setPathFile(filePath.toString());

                if (jNoseCore.isTestFile(testClass)) {
                    LOGGER.log(Level.INFO, "getFilesTest: " + testClass.getPathFile());
                    String productionFileName = "";
                    int index = 0;
                    if(testTrueInicio) index = 0;
                    if(testsTrueInicio) index = 0;
                    if(testTrueFinal) index = testClass.getName().toLowerCase().lastIndexOf("test");
                    if(testsTrueFinal) index = testClass.getName().toLowerCase().lastIndexOf("tests");

                    if (index > 0) {
                        if(testTrueFinal)
                            productionFileName = testClass.getName().substring(0, testClass.getName().toLowerCase().lastIndexOf("test")) + ".java";
                        if(testsTrueFinal)
                            productionFileName = testClass.getName().substring(0, testClass.getName().toLowerCase().lastIndexOf("tests")) + ".java";
                    }else{
                        if(testTrueInicio)
                            productionFileName = testClass.getName().substring(4, testClass.getName().length()) + ".java";
                        if(testsTrueInicio)
                            productionFileName = testClass.getName().substring(5, testClass.getName().length()) + ".java";
                    }
                    testClass.setProductionFile(jNoseCore.getFileProduction(startDir.toString(), productionFileName));

                    if (!testClass.getProductionFile().isEmpty()) {
                        jNoseCore.getTestSmells(testClass);
                        files.add(testClass);
                    }
                }
            }
        }
        return files;
    }
}
