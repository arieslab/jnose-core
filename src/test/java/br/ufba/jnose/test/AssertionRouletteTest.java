package br.ufba.jnose.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import org.junit.jupiter.api.*;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;

import br.ufba.jnose.core.testsmelldetector.testsmell.SmellyElement;
import br.ufba.jnose.core.testsmelldetector.testsmell.smell.AssertionRoulette;

@DisplayName("Assertion Roulette Test")
public class AssertionRouletteTest {

	public AssertionRoulette assertionTest;
	FileInputStream fileInputStream;
	CompilationUnit compilationUnit;
	SmellyElement smellyElementList;
	

	@BeforeEach
	public void setUp() throws Exception {
		assertionTest = new AssertionRoulette();
		fileInputStream = new FileInputStream(new File("src/test/java/br/ufba/jnose/test/fixtures/AssertionRouletteFixture.java"));
	}
	
	@Test
	@DisplayName("should get number of tests")
	public void should_get_number_of_tests() {
		try{ 
			CompilationUnit compilationUnit = JavaParser.parse(fileInputStream);
			assertionTest.runAnalysis(compilationUnit,new CompilationUnit(),"Aux","");
			ArrayList<SmellyElement> testes = assertionTest.list();
			
			assertTrue(testes.size() == 2);
		}
		catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	@Test
	@DisplayName("should get test smells informations")
	public void should_get_test_smells_informations() {
		try{ 
			CompilationUnit compilationUnit = JavaParser.parse(fileInputStream);
			assertionTest.runAnalysis(compilationUnit,new CompilationUnit(),"Aux","");
			ArrayList<SmellyElement> testes = assertionTest.list();
			
//			for(SmellyElement t: testes) {
//				System.out.println(t.getHasSmell());
//				System.out.println(t.getElementName());
//				System.out.println(t.getRange());
//				System.out.println("");
//			}
		
			assertEquals(testes.get(0).getRange(), "13-13");
			assertEquals(testes.get(1).getRange(), "14-14");
			assertEquals(testes.get(0).getElementName(),"should_be_assertion_roulette");
		}
		catch (Exception e) {
	        e.printStackTrace();
	    }
	}


}
