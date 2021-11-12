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
import br.ufba.jnose.core.testsmelldetector.testsmell.smell.UnknownTest;

public class UnknownTestTest {
	
	public UnknownTest unknownTest;
	FileInputStream fileInputStream;
	CompilationUnit compilationUnit;
	SmellyElement smellyElementList;

	@Before
	public void setUp() throws Exception {
		unknownTest = new UnknownTest();
		fileInputStream = new FileInputStream(new File("src/test/java/br/ufba/jnose/test/fixtures/UnkwonFixture.java"));
	}
	
	@Test
	public void should_get_number_of_tests() {
		try{ 
			CompilationUnit compilationUnit = JavaParser.parse(fileInputStream);
			unknownTest.runAnalysis(compilationUnit,new CompilationUnit(),"Aux","");
			ArrayList<SmellyElement> testes = unknownTest.list();
			
			assertTrue(testes.size() == 1);
		}
		catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	@Test
	public void should_get_smell_informations() {
		try{ 
			CompilationUnit compilationUnit = JavaParser.parse(fileInputStream);
			unknownTest.runAnalysis(compilationUnit,new CompilationUnit(),"Aux","");
			ArrayList<SmellyElement> testes = unknownTest.list();
			
			assertTrue(testes.size() == 1);
			assertEquals(testes.get(0).getElementName(),"should_be_unknown_test");
			assertEquals(testes.get(0).getRange(),"14-17");
		}
		catch (Exception e) {
	        e.printStackTrace();
	    }
	}

}