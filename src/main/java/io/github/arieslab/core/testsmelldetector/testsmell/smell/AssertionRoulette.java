package io.github.arieslab.core.testsmelldetector.testsmell.smell;

import io.github.arieslab.core.testsmelldetector.testsmell.*;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Set;

/**
 * "Guess what's wrong?" This smell comes from having a number of assertions in a test method that have no explanation.
 * If one of the assertions fails, you do not know which one it is.
 * A. van Deursen, L. Moonen, A. Bergh, G. Kok, “Refactoring Test Code”, Technical Report, CWI, 2001.
 */
public class AssertionRoulette extends AbstractSmell {

    public AssertionRoulette() {
        super("Assertion Roulette");
    }
    
    public ArrayList<SmellyElement> list(){
    	return (ArrayList<SmellyElement>) smellyElementList;
    }

    /**
     * Analyze the test file for test methods for multiple assert statements without an explanation/message
     */
    @Override
    public void runAnalysis(CompilationUnit testFileCompilationUnit, CompilationUnit productionFileCompilationUnit, String testFileName, String productionFileName) throws FileNotFoundException {
        classVisitor = new AssertionRoulette.ClassVisitor();
        classVisitor.visit(testFileCompilationUnit, null);
    }

    private class ClassVisitor extends VoidVisitorAdapter<Void> {
        private static final Set<String> ASSERT_METHODS_3_4_PARAMS = Set.of(
            "assertArrayEquals", "assertEquals", "assertNotEquals",
            "assertNotSame", "assertSame", "assertThat",
            "failNotEquals", "failNotSame"
        );
        private static final Set<String> ASSERT_METHODS_2_PARAMS = Set.of(
            "assertFalse", "assertNotNull", "assertNull", "assertTrue"
        );
        private MethodDeclaration currentMethod = null;
        private int assertNoMessageCount = 0;

        private int assertCount = 0;
        String methodName = "";
        TestMethod testMethod;

        // examine all methods in the test class
        @Override
        public void visit(MethodDeclaration n, Void arg) {
            if (Util.isValidTestMethod(n)) {
                currentMethod = n;
                methodName = n.getNameAsString();
                testMethod = new TestMethod(methodName);

                testMethod.setHasSmell(false); //default value is false (i.e. no smell)
                super.visit(n, arg);

                //reset values for next method
                currentMethod = null;
                assertCount = 0;
                assertNoMessageCount = 0;
            }
        }
        public boolean explanationIsEmpty(String str) {
            return str.strip().equals("\"\"");
        }

        // examine the methods being called within the test method
        @Override
        public void visit(MethodCallExpr n, Void arg) {
            boolean hasMissingExplanation = false;
            super.visit(n, arg);
            if (currentMethod != null) {
                // if the name of a method being called is an assertion and has 3 or 4 parameters
                if (ASSERT_METHODS_3_4_PARAMS.contains(n.getNameAsString())) {
                    assertCount++;
                    // assert methods that do not contain a message
                    if (n.getArguments().size() < 3) {
                        assertNoMessageCount++;
                        hasMissingExplanation = true;
                    }
                }
                // if the name of a method being called is an assertion and has 2 parameters
                else if (ASSERT_METHODS_2_PARAMS.contains(n.getNameAsString())) {
                    assertCount++;
                    // assert methods that do not contain a message
                    if ((n.getArguments().size() < 2) || (explanationIsEmpty(n.getArgument(0).toString())) ) {
                        assertNoMessageCount++;
                        hasMissingExplanation = true;
                    }
                }

                // if the name of a method being called is 'fail'
                else if (n.getNameAsString().equals("fail") || n.getNameAsString().equals("failSame")) {
                    assertCount++;
                    // fail method does not contain a message
                    if (n.getArguments().size() < 1 || (explanationIsEmpty(n.getArgument(0).toString()))) {
                        assertNoMessageCount++;
                        hasMissingExplanation = true;
                    }
                }
                if (hasMissingExplanation && assertNoMessageCount > 1) {
                    testMethod = new TestMethod(methodName);

                    testMethod.setRange(n.getRange().get().begin.line + "-" + n.getRange().get().end.line);

//                    testMethod.setBegin(n.getRange().get().begin.line);
//                    testMethod.setEnd(n.getRange().get().end.line);
                    testMethod.setHasSmell(true);
//                    testMethod.getTestsmells().add(new TestSmell())
//                    testMethod.addDataItem("AssertCount", String.valueOf(assertNoMessageCount));
                    smellyElementList.add(testMethod);
                }

            }
        }
    }
}

