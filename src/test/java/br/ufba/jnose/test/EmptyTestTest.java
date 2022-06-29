package br.ufba.jnose.test;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;

import br.ufba.jnose.core.testsmelldetector.testsmell.SmellyElement;
import br.ufba.jnose.core.testsmelldetector.testsmell.smell.EmptyTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

public class EmptyTestTest {
	
	public EmptyTest emptytest;
	FileInputStream fileInputStream;
	CompilationUnit compilationUnit;
	SmellyElement smellyElementList;

	JavaParser javaParser;
	
	@Before
	public void setUp() throws Exception {
		emptytest = new EmptyTest();
		fileInputStream = new FileInputStream(new File("src/test/java/br/ufba/jnose/test/fixtures/EmptyFixture.java"));
		javaParser = new JavaParser();
	}
	
	@Test
	public void should_get_number_of_tests() {
		try{ 
			CompilationUnit compilationUnit = javaParser.parse(fileInputStream).getResult().get();
			emptytest.runAnalysis(compilationUnit,new CompilationUnit(),"Aux","");
			ArrayList<SmellyElement> testes = emptytest.list();
			
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
			emptytest.runAnalysis(compilationUnit,new CompilationUnit(),"Aux","");
			ArrayList<SmellyElement> testes = emptytest.list();
			
			
			assertEquals(testes.get(0).getRange(),"10-12");
			assertEquals(testes.get(0).getElementName(),"should_be_empty_test");
		}
		catch (Exception e) {
	        e.printStackTrace();
	    }
	}
}
