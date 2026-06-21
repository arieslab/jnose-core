package io.github.arieslab.test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;

import io.github.arieslab.core.testsmelldetector.testsmell.SmellyElement;
import io.github.arieslab.core.testsmelldetector.testsmell.smell.ExceptionCatchingThrowing;

public class ExceptionCatchingThrowingTest {
	
	public ExceptionCatchingThrowing exceptionTest;
	FileInputStream fileInputStream;
	CompilationUnit compilationUnit;
	SmellyElement smellyElementList;

	@BeforeEach
	public void setUp() throws Exception {
		exceptionTest = new ExceptionCatchingThrowing();
		fileInputStream = new FileInputStream(new File("src/test/java/io/github/arieslab/test/fixtures/ExceptionFixture.java"));
	}
	
	@Test
	public void should_get_number_of_tests() {
		try{ 
			CompilationUnit compilationUnit = JavaParser.parse(fileInputStream);
			exceptionTest.runAnalysis(compilationUnit,new CompilationUnit(),"ExceptionFixture","");
			ArrayList<SmellyElement> testes = exceptionTest.list();
			
			assertTrue(testes.size() == 2);
		}
		catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	
	@Test
	public void should_get_smells() {
		try{ 
			CompilationUnit compilationUnit = JavaParser.parse(fileInputStream);
			exceptionTest.runAnalysis(compilationUnit,new CompilationUnit(),"ExceptionFixture","");
			ArrayList<SmellyElement> testes = exceptionTest.list();
		    assertEquals(testes.get(0).getElementName(),"should_be_expection_one");
		
		}
		catch (Exception e) {
	        e.printStackTrace();
	    }
	}

}
