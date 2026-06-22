package io.github.arieslab.dto;

import java.io.Serializable;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Represents a single test smell detected in a test class.
 * Contains information about the smell type, affected method, and location.
 */
public class TestSmell implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String method;
    private String range;
    private TestClass testClass;
    private String code;

    /** @param code the source code snippet related to this smell */
    public void setCode(String code){
        this.code = code;
    }

    /** @return the source code snippet related to this smell */
    public String getCode(){
        return code;
    }

    /** @return the line range where the smell was detected (e.g. "10-15") */
    public String getRange() {
        return range;
    }

    /** @param range the line range */
    public void setRange(String range) {
        this.range = range;
    }

    /** @return the test smell name (e.g. "Assertion Roulette") */
    public String getName() {
        return name;
    }

    /** @param name the test smell name */
    public void setName(String name) {
        this.name = name;
    }

    /** @return the method name where the smell was detected */
    public String getMethod() {
        return method;
    }

    /** @param method the method name */
    public void setMethod(String method) {
        this.method = method;
    }

    /** @param testClass the parent test class */
    public void setTestClass(TestClass testClass){
        this.testClass = testClass;
    }

    /** @return the parent test class */
    public TestClass getTestClass(){
        return this.testClass;
    }

    /**
     * Generates an MD5 hash of the method name for identification.
     * @return 32-character MD5 hex string, or empty string on error
     */
    public String getMethodNameHash(){
        var hash = "";
        try {
            var md5 = MessageDigest.getInstance("MD5");
            md5.update(StandardCharsets.UTF_8.encode(this.method));
            hash = String.format("%032x", new BigInteger(1, md5.digest()));
        } catch (Exception _) {
            return "";
        }
        return hash;
    }

    /**
     * Generates an MD5 hash combining project name, class name, and method name.
     * @return 32-character MD5 hex string, or empty string on error
     */
    public String getMethodNameFullURIHash(){
        var nomeProjeto = this.testClass.getProjectName();
        var nomeClasse = this.testClass.getFullName();
        var nomeMetodo = this.method;
        var baseText = nomeProjeto + nomeClasse + nomeMetodo;
        var hash = "";
        try {
            var md5 = MessageDigest.getInstance("MD5");
            md5.update(StandardCharsets.UTF_8.encode(baseText));
            hash = String.format("%032x", new BigInteger(1, md5.digest()));
        } catch (Exception _) {
        }
        return hash;
    }

    @Override
    public String toString() {
        return "TestSmell{" +
                "name='" + name + '\'' +
                ", method='" + method + '\'' +
                ", range='[" + range + "]" +'\'' +
                '}';
    }
}
