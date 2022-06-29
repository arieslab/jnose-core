package br.ufba.jnose.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;

import br.ufba.jnose.core.testsmelldetector.testsmell.SmellyElement;
import br.ufba.jnose.core.testsmelldetector.testsmell.smell.RedundantAssertion;

public class RedundantAssertionTest {

	public RedundantAssertion redudantTest;
	FileInputStream fileInputStream;
	CompilationUnit compilationUnit;
	SmellyElement smellyElementList;

	JavaParser javaParser;
	
	@Before
	public void setUp() throws Exception {
		redudantTest = new RedundantAssertion();
		fileInputStream = new FileInputStream(new File("src/test/java/br/ufba/jnose/test/fixtures/RedundantAssertionFixture.java"));
		javaParser = new JavaParser();
	}
	
	@Test
	public void should_get_number_of_tests() {
		try{ 
			CompilationUnit compilationUnit = javaParser.parse(fileInputStream).getResult().get();
			redudantTest.runAnalysis(compilationUnit,new CompilationUnit(),"Aux","");
			ArrayList<SmellyElement> testes = redudantTest.list();
			
			assertTrue(testes.size() == 2);
		}
		catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	@Test
	public void should_get_smells() {
		try{ 
			//System.out.print(System.getProperty("user.dir"));
			CompilationUnit compilationUnit = javaParser.parse(fileInputStream).getResult().get();
			redudantTest.runAnalysis(compilationUnit,new CompilationUnit(),"Aux","");
//			ArrayList<SmellyElement> testes = redudantTest.list();
			
//			for(SmellyElement t: testes) {
//				System.out.println(t.getHasSmell());
//				System.out.println(t.getElementName());
//				System.out.println(t.getRange());
//				System.out.println("");
//			}
			assertFalse(redudantTest.list().isEmpty());
			//assertTrue(testes.size() == 2);
			//assertEquals(testes.get(0).getElementName(),"should_be_empty_test");
		}
		catch (Exception e) {
	        e.printStackTrace();
	    }
	}

}