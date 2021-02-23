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
import br.ufba.jnose.core.testsmelldetector.testsmell.smell.VerboseTest;

public class VerboseTestTest {

	public VerboseTest verboseTest;
	FileInputStream fileInputStream;
	CompilationUnit compilationUnit;
	SmellyElement smellyElementList;
	
	@Before
	public void setUp() throws Exception {
		verboseTest = new VerboseTest();
		fileInputStream = new FileInputStream(new File("src/main/java/br/ufba/jnose/core/testsmelldetector/testsmell/smell/tests/fixtures/VerboseFixture.java"));
		//CompilationUnit compilationUnit = JavaParser.parse(fileInputStream);
	}

	@Test
	public void should_get_smell_informations() {
		try{ 
			CompilationUnit compilationUnit = JavaParser.parse(fileInputStream);
			verboseTest.runAnalysis(compilationUnit,new CompilationUnit(),"Aux","");
			ArrayList<SmellyElement> testes = verboseTest.list();
			
			assertTrue(testes.size() == 1);
			assertEquals(testes.get(0).getRange(),"10-23");
			assertEquals(testes.get(0).getElementName(),"should_be_verbose_test");
		}
		catch (Exception e) {
	        e.printStackTrace();
	    }
	}
}
