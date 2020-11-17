package br.ufba.jnose.core.testsmelldetector.testsmell;

public class MethodUsage {
    private String testMethodName, productionMethodName, range;

    public MethodUsage(String testMethod, String productionMethod, String range) {
        this.testMethodName = testMethod;
        this.productionMethodName = productionMethod;
        this.range = range;
    }

    public MethodUsage(String testMethod){
        this.testMethodName = testMethod;
    }

    public MethodUsage (String testMethod, String productionMethod) {
        this.testMethodName = testMethod;
        this.productionMethodName = productionMethod;
    }

    public String getRange () {
        return range;
    }

    public String getProductionMethodName() {
        return productionMethodName;
    }

    public String getTestMethodName() {
        return testMethodName;
    }

    public String getBlock() {
        return range;
    }

}

