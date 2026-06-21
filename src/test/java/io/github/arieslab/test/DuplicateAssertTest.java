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
import io.github.arieslab.core.testsmelldetector.testsmell.smell.DuplicateAssert;

public class DuplicateAssertTest {

	public DuplicateAssert duplicateTest;
	FileInputStream fileInputStream;
	CompilationUnit compilationUnit;
	SmellyElement smellyElementList;

	@BeforeEach
	public void setUp() throws Exception {
		duplicateTest = new DuplicateAssert();
		fileInputStream = new FileInputStream(new File("src/test/java/io/github/arieslab/test/fixtures/DuplicateAssertFixture.java"));
	}
	
	@Test
	public void should_get_number_of_tests() {
		try{ 
			CompilationUnit compilationUnit = JavaParser.parse(fileInputStream);
			duplicateTest.runAnalysis(compilationUnit,new CompilationUnit(),"Aux","");
			ArrayList<SmellyElement> testes = duplicateTest.list();
			
			assertTrue(testes.size() == 1);
		}
		catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	@Test
	public void should_get_smells() {
		try{ 
			CompilationUnit compilationUnit = JavaParser.parse(fileInputStream);
			duplicateTest.runAnalysis(compilationUnit,new CompilationUnit(),"Aux","");
			ArrayList<SmellyElement> testes = duplicateTest.list();
			
			assertFalse(duplicateTest.list().isEmpty());
			assertEquals(testes.get(0).getRange(),"12, 13");
			assertEquals(testes.get(0).getElementName(),"should_be_duplicate_assert");
		}
		catch (Exception e) {
	        e.printStackTrace();
	    }
	}

}
