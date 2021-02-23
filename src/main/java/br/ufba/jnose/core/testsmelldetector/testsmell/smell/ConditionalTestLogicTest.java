package br.ufba.jnose.core.testsmelldetector.testsmell.smell;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;

import br.ufba.jnose.core.testsmelldetector.testsmell.SmellyElement;

public class ConditionalTestLogicTest {

	public ConditionalTestLogic conditionalTest;
	FileInputStream fileInputStream;
	CompilationUnit compilationUnit;
	SmellyElement smellyElementList;
	
	@Before
	public void setUp() throws Exception {
		conditionalTest = new ConditionalTestLogic();
		fileInputStream = new FileInputStream(new File("src/main/java/br/ufba/jnose/core/testsmelldetector/testsmell/smell/Aux.java"));
	}	
	
	@Test
	public void should_get_smells() { //Separar em 6 testes!!
		try{ 
			//System.out.print(System.getProperty("user.dir"));
			CompilationUnit compilationUnit = JavaParser.parse(fileInputStream);
			conditionalTest.runAnalysis(compilationUnit,new CompilationUnit(),"Aux","");
			ArrayList<SmellyElement> testes = conditionalTest.list();
			
			for(SmellyElement t: testes) {
				System.out.println(t.getHasSmell());
				System.out.println(t.getElementName());
				System.out.println(t.getRange());
				System.out.println("");
			}
			assertFalse(conditionalTest.list().isEmpty());
			assertTrue(testes.size() == 6);
			assertEquals(testes.get(0).getElementName(),"should_be_conditional_one");
			assertEquals(testes.get(1).getElementName(),"should_be_conditional_two");
			assertEquals(testes.get(2).getElementName(),"should_be_conditional_three");
			assertEquals(testes.get(3).getElementName(),"should_be_conditional_four");
			assertEquals(testes.get(4).getElementName(),"should_be_conditional_five");
			assertEquals(testes.get(5).getElementName(),"should_be_conditional_six");
			assertEquals(testes.get(5).getElementName(),"should_be_conditional_seven");
			// pegar o range
		}
		catch (Exception e) {
	        e.printStackTrace();
	    }
	}

}
