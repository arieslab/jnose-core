package io.github.arieslab.core.testsmelldetector.testsmell;

public abstract sealed class SmellyElement permits TestMethod, TestClass {
    public abstract String getElementName();
    public abstract boolean getHasSmell();
    public abstract String getRange();
}
