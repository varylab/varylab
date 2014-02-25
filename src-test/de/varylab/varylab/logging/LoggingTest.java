package de.varylab.varylab.logging;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Locale;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.varylab.varylab.startup.StaticSetup;

public class LoggingTest {

	@BeforeClass
	public static void setLocale() {
		Locale.setDefault(Locale.GERMAN);
	}
	
	@Test
	public void testSimpleLogging() throws Exception {
		InputStream logConfIn = getClass().getResourceAsStream("logging.properties");
		
		LogManager.getLogManager().readConfiguration(logConfIn);
		Logger log1 = Logger.getLogger("de.varylab.varylab.logging.test1");
		Logger log2 = Logger.getLogger("de.varylab.varylab.logging.test2");
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PrintStream myOut = new PrintStream(out);
		System.setErr(myOut);
		log1.severe("This is a severe test message");
		log1.warning("This is a warning message");
		String result1 = new String(out.toByteArray());
		
		out.reset();
		log2.severe("This is a severe test message");
		log2.warning("This is a warning message");
		String result2 = new String(out.toByteArray());		

		Assert.assertTrue(
			result1.toUpperCase().contains("SCHWERWIEGEND") 
		);
		Assert.assertTrue(
			result1.toUpperCase().contains("SCHWERWIEGEND") &&
			result2.toUpperCase().contains("WARNUNG") 
		);		
	}
	
	@Test
	public void testInitLogging() throws Exception {
		StaticSetup.initLogging();
	}

}
