package io.github.arieslab.core;

import io.github.arieslab.dto.TestClass;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class JNoseCallable implements Callable<List<TestClass>> {

    private static final Logger LOGGER = Logger.getLogger(JNoseCallable.class.getName());

    private static final Pattern TEST_SUFFIX = Pattern.compile("^.*test\\d*$");
    private static final Pattern TEST_CASE_SUFFIX = Pattern.compile("^.*testcase\\d*$");
    private static final Pattern TESTS_SUFFIX = Pattern.compile("^.*tests\\d*$");
    private static final Pattern TEST_PREFIX = Pattern.compile("^test.*");
    private static final Pattern TEST_CASE_PREFIX = Pattern.compile("^testcase.*");
    private static final Pattern TESTS_PREFIX = Pattern.compile("^tests.*");

    private final Path filePath;
    private final String projectName;
    private final Path startDir;
    private final JNoseCore jNoseCore;
    private final Map<String, String> fileMap;

    public JNoseCallable(Path filePath, String projectName, Path startDir, JNoseCore jNoseCore, Map<String, String> fileMap){
        this.filePath = filePath;
        this.projectName = projectName;
        this.startDir = startDir;
        this.jNoseCore = jNoseCore;
        this.fileMap = fileMap;
    }

    @Override
    public List<TestClass> call() throws Exception {
        List<TestClass> files = new ArrayList<>();

        if (filePath.getFileName().toString().lastIndexOf(".") != -1) {
            String fileNameWithoutExtension = filePath.getFileName().toString().substring(0, filePath.getFileName().toString().lastIndexOf(".")).toLowerCase();

            if (filePath.toString().toLowerCase().endsWith(".java") && (
                    fileNameWithoutExtension.matches("^.*test\\d*$") ||
                    fileNameWithoutExtension.matches("^.*testcase\\d*$") ||
                            fileNameWithoutExtension.matches("^.*tests\\d*$") ||
                            fileNameWithoutExtension.matches("^test.*") ||
                            fileNameWithoutExtension.matches("^testcase.*") ||
                            fileNameWithoutExtension.matches("^tests.*"))) {

                boolean testTrueFinal = TEST_SUFFIX.matcher(fileNameWithoutExtension).matches();
                boolean testCaseTrueFinal = TEST_CASE_SUFFIX.matcher(fileNameWithoutExtension).matches();
                boolean testsTrueFinal = TESTS_SUFFIX.matcher(fileNameWithoutExtension).matches();

                boolean testTrueInicio = TEST_PREFIX.matcher(fileNameWithoutExtension).matches();
                boolean testCaseTrueInicio = TEST_CASE_PREFIX.matcher(fileNameWithoutExtension).matches();
                boolean testsTrueInicio = TESTS_PREFIX.matcher(fileNameWithoutExtension).matches();


                TestClass testClass = new TestClass();
                testClass.setProjectName(projectName);
                testClass.setPathFile(filePath.toString());

                if (jNoseCore.isTestFile(testClass)) {
                    LOGGER.log(Level.INFO, "getFilesTest: {0}", testClass.getPathFile());
                    String productionFileName = "";
                    int index = 0;
                    if(testTrueInicio) index = 0;
                    if(testCaseTrueInicio) index = 0;
                    if(testsTrueInicio) index = 0;
                    if(testTrueFinal) index = testClass.getName().toLowerCase().lastIndexOf("test");
                    if(testCaseTrueFinal) index = testClass.getName().toLowerCase().lastIndexOf("testcase");
                    if(testsTrueFinal) index = testClass.getName().toLowerCase().lastIndexOf("tests");

                    if (index > 0) {
                        if(testTrueFinal)
                            productionFileName = testClass.getName().substring(0, testClass.getName().toLowerCase().lastIndexOf("test")) + ".java";
                        if(testCaseTrueFinal)
                            productionFileName = testClass.getName().substring(0, testClass.getName().toLowerCase().lastIndexOf("testcase")) + ".java";
                        if(testsTrueFinal)
                            productionFileName = testClass.getName().substring(0, testClass.getName().toLowerCase().lastIndexOf("tests")) + ".java";
                    }else{
                        if(testTrueInicio)
                            productionFileName = testClass.getName().substring(4, testClass.getName().length()) + ".java";
                        if(testCaseTrueInicio)
                            productionFileName = testClass.getName().substring(8, testClass.getName().length()) + ".java";
                        if(testsTrueInicio)
                            productionFileName = testClass.getName().substring(5, testClass.getName().length()) + ".java";
                    }

                    String productionFilePath = fileMap.get(productionFileName.toLowerCase());
                    testClass.setProductionFile(productionFilePath != null ? productionFilePath : "");

                    jNoseCore.getTestSmells(testClass);
                    files.add(testClass);
                }
            }
        }
        return files;
    }
}
