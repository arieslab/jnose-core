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
import br.ufba.jnose.core.testsmelldetector.testsmell.smell.SleepyTest;

public class SleepyTestTest {

	public SleepyTest sleepyTest;
	FileInputStream fileInputStream;
	CompilationUnit compilationUnit;
	SmellyElement smellyElementList;
	
	@Before
	public void setUp() throws Exception {
		sleepyTest = new SleepyTest();
		fileInputStream = new FileInputStream(new File("src/test/java/br/ufba/jnose/test/fixtures/SleepyFixture.java"));
	}	
	
	@Test
	public void should_get_number_of_tests() {
		try{ 
			CompilationUnit compilationUnit = JavaParser.parse(fileInputStream);
			sleepyTest.runAnalysis(compilationUnit,new CompilationUnit(),"Aux","");
			ArrayList<SmellyElement> testes = sleepyTest.list();
			
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
			sleepyTest.runAnalysis(compilationUnit,new CompilationUnit(),"Aux","");
			ArrayList<SmellyElement> testes = sleepyTest.list();
			
			assertEquals(testes.get(0).getRange(), "16-16");
			assertEquals(testes.get(0).getElementName(),"should_be_sleep_test");
		}
		catch (Exception e) {
	        e.printStackTrace();
	    }
	}

}