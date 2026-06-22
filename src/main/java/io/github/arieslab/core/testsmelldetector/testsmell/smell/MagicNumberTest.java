package io.github.arieslab.core.testsmelldetector.testsmell.smell;

import io.github.arieslab.core.testsmelldetector.testsmell.*;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;


import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class MagicNumberTest extends AbstractSmell{

    private ArrayList<MethodUsage> instances;

    public MagicNumberTest(){
        super("Magic Number Test");
        instances = new ArrayList<>();
    }

    /**
     * Analyze the test file for test methods that have magic numbers in as parameters in the assert methods
     */
    @Override
    public void runAnalysis(CompilationUnit testFileCompilationUnit, CompilationUnit productionFileCompilationUnit, String testFileName, String productionFileName) throws FileNotFoundException{
        classVisitor = new MagicNumberTest.ClassVisitor();
        classVisitor.visit(testFileCompilationUnit, null);

        for (var method : instances) {
            TestMethod testClass = new TestMethod(method.getTestMethodName());
            testClass.setRange(method.getRange());
//            testClass.addDataItem("begin", method.getLine());
//            testClass.addDataItem("end", method.getLine()); // [Remover]
            testClass.setHasSmell(true);
            smellyElementList.add(testClass);
        }
    }
    
    public ArrayList<SmellyElement> list(){
    	return (ArrayList<SmellyElement>) smellyElementList;
    }

    private class ClassVisitor extends VoidVisitorAdapter<Void>{
        private MethodDeclaration currentMethod = null;
        private int magicCount = 0;

        // examine all methods in the test class
        @Override
        public void visit(MethodDeclaration n, Void arg){
            if (Util.isValidTestMethod(n)) {
                currentMethod = n;
                super.visit(n, arg);

                //reset values for next method
                currentMethod = null;
                magicCount = 0;
            }
        }

        // examine the methods being called within the test method
        @Override
        public void visit(MethodCallExpr n, Void arg){
            super.visit(n, arg);
            if (currentMethod != null) {
                // if the name of a method being called start with 'assert'
                if (n.getNameAsString().startsWith(("assert"))) {
                    // checks all arguments of the assert method

                    for (var argument : n.getArguments()) {
                        // if the argument is a number
                        if (Util.isNumber(argument.toString())) {
                            MethodUsage verification = new MethodUsage(currentMethod.getNameAsString(),
                                    "",n.getRange().get().begin.line+"");
                            if (!instances.contains(verification)){
                                instances.add(verification);
                            }
                        }
                        // if the argument contains an ObjectCreationExpr (e.g. assertEquals(new Integer(2),...)
                        else if (argument instanceof ObjectCreationExpr) {
                            checkObject( (ObjectCreationExpr) argument);
                        }
                        // if the argument contains an MethodCallExpr (e.g. assertEquals(someMethod(2),...)
                        else if (argument instanceof MethodCallExpr) {
                            checkMethodCall( (MethodCallExpr) argument);
                        }
                        //if the assertTrue has a number or methodcall with numbers
                        else if (argument instanceof BinaryExpr) {
                            checkBinary( (BinaryExpr) argument);
                        }
                    }
                }
            }
        }


        private boolean checkMethodCall(MethodCallExpr argument){
            for (var objectArguments : argument.getArguments()) {
                if (Util.isNumber(objectArguments.toString())) {
                    MethodUsage verification = new MethodUsage(currentMethod.getNameAsString(), "",
                            argument.getRange().get().begin.line+"");
                    if (!instances.contains(verification)){
                        instances.add(verification);
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean checkObject(ObjectCreationExpr argument){
            for (var objectArguments : argument.getArguments()) {
                if (Util.isNumber(objectArguments.toString())) {
                    MethodUsage verification = new MethodUsage(currentMethod.getNameAsString(), "",
                            argument.getRange().get().begin.line+"");
                    if (!instances.contains(verification)){
                        instances.add(verification);
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean checkBinary(BinaryExpr argument) {
            if (Util.isNumber(argument.getLeft().toString()) ||
                    Util.isNumber(argument.getRight().toString())) {
                MethodUsage verification = new MethodUsage(currentMethod.getNameAsString(), "",
                        argument.getRange().get().begin.line+"");
                if (!instances.contains(verification)){
                    instances.add(verification);
                    return true;
                }
            }
            else if (argument.getRight() instanceof MethodCallExpr && checkMethodCall((MethodCallExpr) argument.getRight())) {
                return true;
            }
            else if (argument.getLeft() instanceof MethodCallExpr && checkMethodCall((MethodCallExpr) argument.getLeft())) {
                return true;
            }
            else if (argument.getRight() instanceof ObjectCreationExpr && checkObject((ObjectCreationExpr) argument.getRight())) {
                return true;
            }
            else if (argument.getLeft() instanceof ObjectCreationExpr && checkObject((ObjectCreationExpr) argument.getLeft())) {
                return true;
            }
            else if (argument.getRight() instanceof BinaryExpr && checkBinary((BinaryExpr) argument.getRight())) {
                return true;
            }
            else if (argument.getLeft() instanceof BinaryExpr && checkBinary((BinaryExpr) argument.getLeft())) {
                return true;
            }
            return false;
        }
    }
}
