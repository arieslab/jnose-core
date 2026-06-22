package io.github.arieslab.core.testsmelldetector.testsmell;

public record MethodUsage(String testMethodName, String productionMethodName, String range) {

    public String getTestMethodName() { return testMethodName(); }

    public String getProductionMethodName() { return productionMethodName(); }

    public String getRange() { return range(); }

    public String getBlock() { return range(); }
}
