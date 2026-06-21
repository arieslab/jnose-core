<p align="center"><img src="https://github.com/arieslab/jnose-core/blob/main/logo.png?raw=true" width="70"></p>

# JNose-Core

Java library for automatic **Test Smells** detection in test code.

## Supported Test Smells

| Test Smell | Description |
|---|---|
| Assertion Roulette | Multiple assertions without identification message |
| Conditional Test Logic | Tests with conditional logic (if/switch/loop) |
| Constructor Initialization | Initialization via constructor instead of @Before |
| Default Test | Test that only checks the default flow |
| Dependent Test | Tests that depend on other tests execution |
| Duplicate Assert | Duplicate assertions in the same test |
| Eager Test | Test that verifies multiple methods |
| Empty Test | Test with no body |
| Exception Catching Throwing | Catching and rethrowing exceptions in test |
| General Fixture | Overly generic fixture shared across tests |
| Ignored Test | Skipped/ignored test |
| Lazy Test | Test that does not verify the result |
| Magic Number Test | Usage of magic numbers in assertions |
| Mystery Guest | Test accessing external resources (files, network, database) |
| Print Statement | Usage of System.out/System.err in test |
| Redundant Assertion | Redundant assertions |
| Resource Optimism | Optimistic resource access without verification |
| Sensitive Equality | Equality comparison using toString() |
| Sleepy Test | Usage of Thread.sleep() in test |
| Unknown Test | Test that does not follow JUnit patterns |
| Verbose Test | Test with excessive lines / verbose logging |

## JUnit Support

- JUnit 3 (junit.framework)
- JUnit 4 (@Test)
- JUnit 5 (org.junit.jupiter)

## Usage

### CLI (Executable JAR)

```bash
java -jar jnose-core-<version>-jar-with-dependencies.jar <project-path>
```

### Maven Dependency

Add the GitHub Packages repository to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/arieslab/jnose-core</url>
    </repository>
</repositories>
```

```xml
<dependency>
    <groupId>io.github.arieslab</groupId>
    <artifactId>jnose-core</artifactId>
    <version>0.9.0</version>
</dependency>
```

### Programmatic Usage

```java
Config config = new Config() {
    public boolean assertionRoulette() { return true; }
    public boolean conditionalTestLogic() { return true; }
    // enable/disable each test smell
    public int maxStatements() { return 50; }
};

JNoseCore jnose = new JNoseCore(config);
List<TestClass> results = jnose.getFilesTest("/path/to/project");

for (TestClass tc : results) {
    System.out.println(tc.getName() + " - " + tc.getListTestSmell().size() + " smells");
}
```

## Build

```bash
mvn clean package
```

Generates:
- `target/jnose-core-<version>.jar`
- `target/jnose-core-<version>-jar-with-dependencies.jar` (with all dependencies)

## License

Apache License 2.0

## Contact

- tassiovirginio@gmail.com
- tassio.virginio@ifto.edu.br
