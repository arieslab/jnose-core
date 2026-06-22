package io.github.arieslab.core;

/**
 * Configuration interface for enabling/disabling specific test smell detections.
 * Implement this interface to customize which test smells are analyzed
 * and to set detection parameters.
 */
public interface Config {

    /** @return true to enable Assertion Roulette detection */
    boolean assertionRoulette();

    /** @return true to enable Conditional Test Logic detection */
    boolean conditionalTestLogic();

    /** @return true to enable Constructor Initialization detection */
    boolean constructorInitialization();

    /** @return true to enable Default Test detection */
    boolean defaultTest();

    /** @return true to enable Dependent Test detection */
    boolean dependentTest();

    /** @return true to enable Duplicate Assert detection */
    boolean duplicateAssert();

    /** @return true to enable Eager Test detection */
    boolean eagerTest();

    /** @return true to enable Empty Test detection */
    boolean emptyTest();

    /** @return true to enable Exception Catching Throwing detection */
    boolean exceptionCatchingThrowing();

    /** @return true to enable General Fixture detection */
    boolean generalFixture();

    /** @return true to enable Mystery Guest detection */
    boolean mysteryGuest();

    /** @return true to enable Print Statement detection */
    boolean printStatement();

    /** @return true to enable Redundant Assertion detection */
    boolean redundantAssertion();

    /** @return true to enable Sensitive Equality detection */
    boolean sensitiveEquality();

    /** @return true to enable Verbose Test detection */
    boolean verboseTest();

    /** @return true to enable Sleepy Test detection */
    boolean sleepyTest();

    /** @return true to enable Lazy Test detection */
    boolean lazyTest();

    /** @return true to enable Unknown Test detection */
    boolean unknownTest();

    /** @return true to enable Ignored Test detection */
    boolean ignoredTest();

    /** @return true to enable Resource Optimism detection */
    boolean resourceOptimism();

    /** @return true to enable Magic Number Test detection */
    boolean magicNumberTest();

    /**
     * Maximum number of statements allowed before a test is considered verbose.
     * @return max statement threshold
     */
    int maxStatements();
}
