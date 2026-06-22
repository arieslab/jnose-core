package io.github.arieslab.core.testsmelldetector.testsmell.smell;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import io.github.arieslab.core.testsmelldetector.testsmell.*;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class DependentTest extends AbstractSmell {

    private List<MethodEntry> methodEntries;


    public DependentTest() {
        super("Dependent Test");
        methodEntries = new ArrayList<>();
    }

    /**
     * Analyze the test file for test methods that call other test methods
     */
    @Override
    public void runAnalysis(CompilationUnit testFileCompilationUnit, CompilationUnit productionFileCompilationUnit,
            String testFileName, String productionFileName) throws FileNotFoundException {
        classVisitor = new DependentTest.ClassVisitor();
        classVisitor.visit(testFileCompilationUnit, null);

        for (var methodEntry : methodEntries) {
            boolean callsOtherTestMethod = methodEntry.getCalledMethods().stream()
                .anyMatch(called -> methodEntries.stream()
                    .anyMatch(tm -> tm.getMethodDeclaration().getNameAsString().equals(called.getName())));
            if (callsOtherTestMethod) {
                var element = new io.github.arieslab.core.testsmelldetector.testsmell.TestMethod(
                    methodEntry.getMethodDeclaration().getNameAsString());
                element.setHasSmell(true);
                element.setRange(methodEntry.getMethodDeclaration().getRange().get().begin.line + "-" + methodEntry.getMethodDeclaration().getRange().get().end.line);
                smellyElementList.add(element);
            }
        }

    }
    
    public ArrayList<SmellyElement> list(){
    	return (ArrayList<SmellyElement>) smellyElementList;
    }

    private class ClassVisitor extends VoidVisitorAdapter<Void> {
        private MethodDeclaration currentMethod = null;
        List<CalledMethod> calledMethods;

        // examine all methods in the test class
        @Override
        public void visit(MethodDeclaration n, Void arg) {
            if (Util.isValidTestMethod(n)) {
                currentMethod = n;
                calledMethods = new ArrayList<>();

                super.visit(n, arg);

                MethodEntry methodEntry = new MethodEntry(n, calledMethods);

                methodEntries.add(methodEntry);
            }
        }

        // examine the methods being called within the test method
        @Override
        public void visit(MethodCallExpr n, Void arg) {
            super.visit(n, arg);
            if (currentMethod != null) {
                if (!calledMethods.contains(new CalledMethod(n.getArguments().size(), n.getNameAsString()))) {
                    calledMethods.add(new CalledMethod(n.getArguments().size(), n.getNameAsString()));
                }
            }
        }
    }

    private class MethodEntry {
        public List<CalledMethod> getCalledMethods() {
            return calledMethods;
        }

        public MethodDeclaration getMethodDeclaration() {
            return methodDeclaration;
        }

        public MethodEntry(MethodDeclaration methodDeclaration, List<CalledMethod> calledMethods) {
            this.methodDeclaration = methodDeclaration;
            this.calledMethods = calledMethods;
        }

        private List<CalledMethod> calledMethods;
        private MethodDeclaration methodDeclaration;
    }

    private class CalledMethod {
        public int getTotalArguments() {
            return totalArguments;
        }

        public String getName() {
            return name;
        }

        public CalledMethod(int totalArguments, String name) {
            this.totalArguments = totalArguments;
            this.name = name;
        }

        private int totalArguments;
        private String name;
    }
}
