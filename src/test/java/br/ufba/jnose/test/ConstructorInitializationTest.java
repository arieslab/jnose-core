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
import br.ufba.jnose.core.testsmelldetector.testsmell.smell.ConstructorInitialization;

public class ConstructorInitializationTest {

	public ConstructorInitialization constructorTest;
	FileInputStream fileInputStream;
	CompilationUnit compilationUnit;
	SmellyElement smellyElementList;

	JavaParser javaParser;

	@Before
	public void setUp() throws Exception {
		constructorTest = new ConstructorInitialization();
		fileInputStream = new FileInputStream(new File("src/test/java/br/ufba/jnose/test/fixtures/ConstructorFixture.java"));
		javaParser = new JavaParser();
	}
	
	@Test
	public void should_get_number_of_tests() {
		try{ 
			CompilationUnit compilationUnit = javaParser.parse(fileInputStream).getResult().get();
			constructorTest.runAnalysis(compilationUnit,new CompilationUnit(),"ConstructorFixture","");
			ArrayList<SmellyElement> testes = constructorTest.listTests();
			
			assertTrue(testes.size() == 1);
		}
		catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	@Test
	public void should_get_test_smells_informations() {
		try{ 
			CompilationUnit compilationUnit = javaParser.parse(fileInputStream).getResult().get();
			constructorTest.runAnalysis(compilationUnit,new CompilationUnit(),"ConstructorFixture","");
			ArrayList<SmellyElement> testes = constructorTest.listTests();

			assertEquals(testes.get(0).getRange(),"8-10");
			assertEquals(testes.get(0).getElementName(),"ConstructorFixture");	
		}
		catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	

}
