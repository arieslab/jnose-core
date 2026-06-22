module io.github.arieslab.jnose {
    requires java.logging;
    requires javaparser.core;

    exports io.github.arieslab.core;
    exports io.github.arieslab.core.testsmelldetector.testsmell;
    exports io.github.arieslab.core.testsmelldetector.testsmell.smell;
    exports io.github.arieslab.dto;
}
