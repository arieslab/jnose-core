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
import br.ufba.jnose.core.testsmelldetector.testsmell.smell.VerboseTest;

public class VerboseTestTest {

	public VerboseTest verboseTest;
	FileInputStream fileInputStream;
	CompilationUnit compilationUnit;
	SmellyElement smellyElementList;

	JavaParser javaParser;
	
	@Before
	public void setUp() throws Exception {
		verboseTest = new VerboseTest();
		fileInputStream = new FileInputStream(new File("src/test/java/br/ufba/jnose/test/fixtures/VerboseFixture.java"));
		//CompilationUnit compilationUnit = JavaParser.parse(fileInputStream);
		javaParser = new JavaParser();
	}
	
	@Test
	public void should_get_number_of_tests() {
		try{ 
			CompilationUnit compilationUnit = javaParser.parse(fileInputStream).getResult().get();
			verboseTest.runAnalysis(compilationUnit,new CompilationUnit(),"Aux","");
			ArrayList<SmellyElement> testes = verboseTest.list();

			assertTrue(testes.size() == 1);
		}
		catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	@Test
	public void should_get_smell_informations() {
		try{ 
			CompilationUnit compilationUnit = javaParser.parse(fileInputStream).getResult().get();
			verboseTest.runAnalysis(compilationUnit,new CompilationUnit(),"Aux","");
			ArrayList<SmellyElement> testes = verboseTest.list();
			
			assertEquals(testes.get(0).getRange(),"10-41");
			assertEquals(testes.get(0).getElementName(),"should_be_verbose_test");
		}
		catch (Exception e) {
	        e.printStackTrace();
	    }
	}
}
