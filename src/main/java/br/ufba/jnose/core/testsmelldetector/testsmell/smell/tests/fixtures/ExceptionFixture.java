package br.ufba.jnose.core.testsmelldetector.testsmell.smell.tests.fixtures;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class ExceptionFixture {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void should_be_Exception_one() throws IOException{
	    File tempFile = File.createTempFile("test", ".txt");
	}
	
	@Test
	public void should_be_Exception_two() throws Exception {
		File file = new File( "file.txt" );
	    FileWriter fw = new FileWriter( file );
	    fw.write("my text");
	    fw.close();
	    file.delete();
	}
	
	@Test
	public void should_be_should_be_Exception_three() throws InterruptedException {
		Thread.sleep(500);
	}

}
