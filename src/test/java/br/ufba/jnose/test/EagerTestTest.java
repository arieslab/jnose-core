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
import br.ufba.jnose.core.testsmelldetector.testsmell.smell.EagerTest;

public class EagerTestTest {

	public EagerTest eagerTest;
	FileInputStream fileInputStream;
	FileInputStream fileInputStream2;
	CompilationUnit compilationUnit;
	SmellyElement smellyElementList;

	JavaParser javaParser;
	
	@Before
	public void setUp() throws Exception {
		eagerTest = new EagerTest();
		fileInputStream = new FileInputStream(new File("src/test/java/br/ufba/jnose/test/fixtures/EagerFixture.java"));
		fileInputStream2 = new FileInputStream(new File("src/test/java/br/ufba/jnose/test/fixtures/LazyClassFixture.java"));
		javaParser = new JavaParser();
	}	
	
	@Test
	public void should_get_number_of_tests() {
		try{ 
			CompilationUnit compilationUnit = javaParser.parse(fileInputStream).getResult().get();
			CompilationUnit compilationUnit2 = javaParser.parse(fileInputStream2).getResult().get();
			eagerTest.runAnalysis(compilationUnit,compilationUnit2,"LazyFixture","LazyClassFixture");
			ArrayList<SmellyElement> testes = eagerTest.list();
			
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
			CompilationUnit compilationUnit2 = javaParser.parse(fileInputStream2).getResult().get();
			eagerTest.runAnalysis(compilationUnit,compilationUnit2,"EagerFixture","LazyClassFixture");
			ArrayList<SmellyElement> testes = eagerTest.list();
						
			assertEquals(testes.get(0).getRange(),"12, 13, 14");
			assertEquals(testes.get(0).getElementName(),"should_be_eager_test");
		}
		catch (Exception e) {
	        e.printStackTrace();
	    }
	}

}
