package br.ufba.jnose.core.testsmelldetector.testsmell.smell.tests.fixtures;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class Aux {

	@Before
	public void setUp() throws Exception {
		number1 = 1;
		number2 = 2;
	}
	
	int number1;
	int number2;
	public int out_setup = 0;

	@Test
	public void should_be_empty_test() {
	}
	
	
	public Aux() {
		Aux aux = new Aux();
	}
	
	
	@Test
	public void should_be_conditional_one() {
		if (true) {
		}	
	}
	
	@Test
	public void should_be_conditional_two() {
		for (int i = 0; i < 2; i++) {
		}		
	}
	
	@Test
	public void should_be_conditional_three() {
		int i = 0;
		while (i < 2) {
			i++;
		}
	}
	
	@Test
	public void should_be_conditional_four() {
		int i = 0;
		System.out.println((i <= 2) ? i : 2);
	}
	
	@Test
	public void should_be_conditional_five() {
		String[] cars = {"Volvo", "BMW", "Ford", "Mazda"};
		for (String i : cars) {
		  System.out.println(i);
		}	
	}
	
	@Test
	public void should_be_conditional_six() {
		int day = 4;
		switch (day) {
		  case 1:
		    System.out.println("Monday");
		    break;
		  case 2:
		    System.out.println("Tuesday");
		    break;
		}		
	}
	@Test
	public void should_be_conditional_seven() {
		int i = 0;
		do {
		  System.out.println(i);
		  i++;
		}
		while (i < 2);
	}
	
	@Test
	public void should_be_print_statement_one() {
		System.out.println("test with println");
	}
	
	@Test
	public void should_be_print_statement_two() {
		int a = 1;
		int b = 2;
		System.out.printf("test with printf = %d",(a + b));
	}
	
	@Test
	public void should_be_print_statement_three() {
		System.out.print("test with print");
	}
	
	@Test
	public void should_be_duplicate_assert() {
		assertEquals("","");
		assertEquals("","");
		assertEquals("","");
	}
	
	@Test
	public void should_be_Mistery_Guest() throws IOException{
	    File tempFile = File.createTempFile("test", ".txt");
	}
	
	@Test
	public void should_be_resource_optimism() throws Exception {
		File file = new File( "file.txt" );
	    FileWriter fw = new FileWriter( file );
	    fw.write("my text");
	    fw.close();
	    file.delete();
	}
	
	@Test
	public void should_not_be_resource_optimism() throws Exception {
		File file = new File( "file.txt" );
	    if(file.exists()) {
	    	FileWriter fw = new FileWriter( file );
	    	fw.write("my text");
	    }
	}
	
	@Test
	public void should_be_redundant_assertion() {
	    assertEquals(true, true);
	}
	
	@Test
	public void should_be_redundant_assertion_two() {
	    assertEquals(false, false);
	    assertEquals(false, false);
	    assertEquals(false, false);
	}
	
	
	@Test
	public void should_be_sensitive_equality() {
		//assertEquals(calc.toString(), "my string");
	}
	
	@Test
	public void should_be_sleep_test() throws InterruptedException {
		Thread.sleep(500);
	}
	
	@Test
	public void should_not_be_general_fixture(){
		assertEquals(number1, number2);  // uses both fields instantiated within the setUp method
	}

	@Test
	public void should_be_general_fixture(){
	    assertEquals("explanation", number1, 2); // uses only the number1 field
	}
	
	// Dependent test is a test smell that depends on the result of another test
	@Test
	public void should_be_dependent_test_step_one() {
		out_setup = 5;
	    assertEquals(out_setup, 5);
	}
	
	@Test
	public void should_be_dependent_test_two_step_two() {
	    assertEquals(out_setup, 5); // variable set in the previous test
	}
	
	@Test
	public void should_be_magic_number() {
		assertEquals(2 + 3,5);
	}
	
}
