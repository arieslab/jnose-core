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

/**
 * Callable task that analyzes a single Java file for test smells.
 * Implements {@link Callable} to support parallel processing of multiple files.
 */
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

    /**
     * Creates a new callable to analyze a single Java file.
     * @param filePath    path to the Java file
     * @param projectName name of the project being analyzed
     * @param startDir    root directory of the project
     * @param jNoseCore   core instance for smell detection
     * @param fileMap     mapping of file names to full paths
     */
    public JNoseCallable(Path filePath, String projectName, Path startDir, JNoseCore jNoseCore, Map<String, String> fileMap){
        this.filePath = filePath;
        this.projectName = projectName;
        this.startDir = startDir;
        this.jNoseCore = jNoseCore;
        this.fileMap = fileMap;
    }

    /**
     * Analyzes the file and detects test smells.
     * Determines if the file is a test file by checking naming conventions
     * (suffixes like "test", "tests", "testcase" or prefixes like "test").
     * If it is a test file, identifies the corresponding production file
     * and runs test smell detection.
     * @return list of {@link TestClass} results (empty if not a test file)
     * @throws Exception if analysis fails
     */
    @Override
    public List<TestClass> call() throws Exception {
        var files = new ArrayList<TestClass>();

        if (filePath.getFileName().toString().lastIndexOf(".") != -1) {
            var fileNameWithoutExtension = filePath.getFileName().toString().substring(0, filePath.getFileName().toString().lastIndexOf(".")).toLowerCase();

            if (filePath.toString().toLowerCase().endsWith(".java") && (
                    fileNameWithoutExtension.matches("^.*test\\d*$") ||
                    fileNameWithoutExtension.matches("^.*testcase\\d*$") ||
                            fileNameWithoutExtension.matches("^.*tests\\d*$") ||
                            fileNameWithoutExtension.matches("^test.*") ||
                            fileNameWithoutExtension.matches("^testcase.*") ||
                            fileNameWithoutExtension.matches("^tests.*"))) {

                var testClass = new TestClass();
                testClass.setProjectName(projectName);
                testClass.setPathFile(filePath.toString());

                if (jNoseCore.isTestFile(testClass)) {
                    LOGGER.log(Level.INFO, "getFilesTest: {0}", testClass.getPathFile());
                    var productionFileName = "";
                    var lowerName = testClass.getName().toLowerCase();

                    if (lowerName.endsWith("test")) {
                        productionFileName = testClass.getName().substring(0, lowerName.lastIndexOf("test")) + ".java";
                    } else if (lowerName.endsWith("tests")) {
                        productionFileName = testClass.getName().substring(0, lowerName.lastIndexOf("tests")) + ".java";
                    } else if (lowerName.endsWith("testcase")) {
                        productionFileName = testClass.getName().substring(0, lowerName.lastIndexOf("testcase")) + ".java";
                    } else if (lowerName.startsWith("test")) {
                        productionFileName = testClass.getName().substring(4) + ".java";
                    } else if (lowerName.startsWith("tests")) {
                        productionFileName = testClass.getName().substring(5) + ".java";
                    } else if (lowerName.startsWith("testcase")) {
                        productionFileName = testClass.getName().substring(8) + ".java";
                    }

                    var productionFilePath = fileMap.get(productionFileName.toLowerCase());
                    testClass.setProductionFile(productionFilePath != null ? productionFilePath : "");

                    jNoseCore.getTestSmells(testClass);
                    files.add(testClass);
                }
            }
        }
        return files;
    }
}
