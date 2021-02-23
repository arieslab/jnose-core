package br.ufba.jnose.core.testsmelldetector.testsmell.smell.tests;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;

import br.ufba.jnose.core.testsmelldetector.testsmell.MethodUsage;
import br.ufba.jnose.core.testsmelldetector.testsmell.SmellyElement;
import br.ufba.jnose.core.testsmelldetector.testsmell.smell.EmptyTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
	
	@Before
	public void setUp() throws Exception {
		emptytest = new EmptyTest();
		fileInputStream = new FileInputStream(new File("src/main/java/br/ufba/jnose/core/testsmelldetector/testsmell/smell/tests/fixtures/Aux.java"));
	}	
	
	@Test
	public void should_get_smells() {
		try{ 
			//System.out.print(System.getProperty("user.dir"));
			CompilationUnit compilationUnit = JavaParser.parse(fileInputStream);
			emptytest.runAnalysis(compilationUnit,new CompilationUnit(),"Aux","");
			ArrayList<SmellyElement> testes = emptytest.list();
			
			for(SmellyElement t: testes) {
				System.out.println(t.getHasSmell());
				System.out.println(t.getElementName());
				System.out.println(t.getRange());
				System.out.println("");
			}
			//assertFalse(emptytest.list().isEmpty());
			//assertTrue(testes.size() == 1);
			//assertEquals(testes.get(0).getElementName(),"should_be_empty_test");
		}
		catch (Exception e) {
	        e.printStackTrace();
	    }
	}
}
