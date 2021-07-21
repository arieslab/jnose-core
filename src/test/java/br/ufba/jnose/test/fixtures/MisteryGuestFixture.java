package br.ufba.jnose.test.fixtures;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class MisteryGuestFixture {

	@Test
	public void should_be_Mistery_Guest() throws IOException{
	    File tempFile = File.createTempFile("test", ".txt");
	}
}
