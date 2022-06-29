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
import br.ufba.jnose.core.testsmelldetector.testsmell.smell.GeneralFixture;

public class GeneralFixtureTest {
	
	public GeneralFixture generalTest;
	FileInputStream fileInputStream;
	CompilationUnit compilationUnit;
	SmellyElement smellyElementList;

	JavaParser javaParser;

	@Before
	public void setUp() throws Exception {
		generalTest = new GeneralFixture();
		fileInputStream = new FileInputStream(new File("src/test/java/br/ufba/jnose/test/fixtures/GeneralFixtureFixture.java"));
		javaParser = new JavaParser();
	}
	
	@Test
	public void should_get_number_of_tests() {
		try{ 
			CompilationUnit compilationUnit = javaParser.parse(fileInputStream).getResult().get();
			generalTest.runAnalysis(compilationUnit,new CompilationUnit(),"Aux","");
			ArrayList<SmellyElement> testes = generalTest.list();
			
			assertTrue(testes.size() == 1);
		}
		catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	@Test
	public void should_get_smells() {
		try{ 
			CompilationUnit compilationUnit = javaParser.parse(fileInputStream).getResult().get();
			generalTest.runAnalysis(compilationUnit,new CompilationUnit(),"Aux","");
			ArrayList<SmellyElement> testes = generalTest.list();
			
			assertFalse(generalTest.list().isEmpty());
			assertTrue(testes.size() == 1);
			assertEquals(testes.get(0).getRange(), "13");
			assertEquals(testes.get(0).getElementName(),"setUp");
		}
		catch (Exception e) {
	        e.printStackTrace();
	    }
	}

}
