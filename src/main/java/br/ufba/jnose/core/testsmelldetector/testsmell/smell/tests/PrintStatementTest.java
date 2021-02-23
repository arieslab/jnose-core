package br.ufba.jnose.core.testsmelldetector.testsmell.smell.tests;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;

import br.ufba.jnose.core.testsmelldetector.testsmell.SmellyElement;
import br.ufba.jnose.core.testsmelldetector.testsmell.smell.PrintStatement;

public class PrintStatementTest {
	
	public PrintStatement printTest;
	FileInputStream fileInputStream;
	CompilationUnit compilationUnit;
	SmellyElement smellyElementList;

	@Before
	public void setUp() throws Exception {
		printTest = new PrintStatement();
		fileInputStream = new FileInputStream(new File("src/main/java/br/ufba/jnose/core/testsmelldetector/testsmell/smell/tests/Aux.java"));
	}
	
	@Test
	public void should_get_smells() { // REFATORAR EM 3 TESTES
		try{ 
			//System.out.print(System.getProperty("user.dir"));
			CompilationUnit compilationUnit = JavaParser.parse(fileInputStream);
			printTest.runAnalysis(compilationUnit,new CompilationUnit(),"Aux","");
			ArrayList<SmellyElement> testes = printTest.list();
			
			for(SmellyElement t: testes) {
				System.out.println(t.getHasSmell());
				System.out.println(t.getElementName());
				System.out.println(t.getRange());
				System.out.println("");
			}
			assertFalse(printTest.list().isEmpty());
			assertTrue(testes.size() == 7);
			assertEquals(testes.get(4).getElementName(),"should_be_print_statement_one");
			assertEquals(testes.get(5).getElementName(),"should_be_print_statement_two");
			assertEquals(testes.get(6).getElementName(),"should_be_print_statement_three");
			
		}
		catch (Exception e) {
	        e.printStackTrace();
	    }
	}

}
