package io.github.arieslab.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a test class with its metadata and detected test smells.
 * Used as the primary result container for test smell analysis.
 */
public class TestClass implements Serializable {
    private static final long serialVersionUID = 1L;

    private String projectName;
    private String pathFile;
    private String name;
    private String fullName;
    private Integer numberMethods;
    private Integer numberLine;
    private String productionFile;
    private List<TestSmell> listTestSmell = new ArrayList<>();
    private JunitVersion junitVersion;
    private Map<String,Integer> lineSumTestSmells;

    /** Supported JUnit versions for detection. */
    public enum JunitVersion{None, JUnit3, JUnit4, JUnit5}

    /** @return map of smell names to their total count in this class */
    public Map<String, Integer> getLineSumTestSmells() {
        return lineSumTestSmells;
    }

    /** @param lineSumTestSmells map of smell names to their total count */
    public void setLineSumTestSmells(Map<String, Integer> lineSumTestSmells) {
        this.lineSumTestSmells = lineSumTestSmells;
    }

    /** @return the project name this class belongs to */
    public String getProjectName() {
        return projectName;
    }

    /** @param projectName the project name */
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    /** @return the full file path of this test class */
    public String getPathFile() {
        return pathFile;
    }

    /** @param pathFile the full file path */
    public void setPathFile(String pathFile) {
        this.pathFile = pathFile;
    }

    /** @return the simple class name */
    public String getName() {
        return name;
    }

    /** @param name the simple class name */
    public void setName(String name) {
        this.name = name;
    }

    /** @return the number of methods in this class */
    public Integer getNumberMethods() {
        return numberMethods;
    }

    /** @param numberMethods the number of methods */
    public void setNumberMethods(Integer numberMethods) {
        this.numberMethods = numberMethods;
    }

    /** @return the total number of lines in the file */
    public Integer getNumberLine() {
        return numberLine;
    }

    /** @param numberLine the total number of lines */
    public void setNumberLine(Integer numberLine) {
        this.numberLine = numberLine;
    }

    /** @return the corresponding production file path, if found */
    public String getProductionFile() {
        return productionFile;
    }

    /** @param productionFile the production file path */
    public void setProductionFile(String productionFile) {
        this.productionFile = productionFile;
    }

    /** @return list of detected test smells */
    public List<TestSmell> getListTestSmell() {
        return listTestSmell;
    }

    /** @param listTestSmell list of detected test smells */
    public void setListTestSmell(List<TestSmell> listTestSmell) {
        this.listTestSmell = listTestSmell;
    }

    /** @return the detected JUnit version */
    public JunitVersion getJunitVersion() {
        if(junitVersion == null){
            junitVersion = JunitVersion.None;
        }
        return junitVersion;
    }

    /** @param junitVersion the detected JUnit version */
    public void setJunitVersion(JunitVersion junitVersion) {
        this.junitVersion = junitVersion;
    }

    /** @return the fully qualified class name */
    public String getFullName() {
        return fullName;
    }

    /** @param fullName the fully qualified class name */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @Override
    public String toString() {
        return "TestClass{" +
                "projectName=" + projectName +
                "pathFile=" + pathFile +
                ", name='" + name + '\'' +
                ", numberMethods=" + numberMethods +
                ", numberLine=" + numberLine +
                ", junitVersion='" + junitVersion + '\'' +
                ", productionFile='" + productionFile + '\'' +
                ", listTestSmell=" + listTestSmell +
                '}';
    }
}
