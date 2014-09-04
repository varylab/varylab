package de.varylab.varylab.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class TrivialLogFormatter extends Formatter {

	@Override
	public String format(LogRecord log) {
		String className = log.getSourceClassName();
		className = className.substring(className.lastIndexOf('.') + 1);
		String methodName = log.getSourceMethodName();
		String message = log.getLevel() + "["+ log.getSequenceNumber() + "]" + className + "." +  methodName + "(): " + log.getMessage();
		if (log.getLevel().intValue() >= Level.SEVERE.intValue() && log.getThrown() != null) {
			StringWriter sw = new StringWriter();
			PrintWriter w = new PrintWriter(sw);
			log.getThrown().printStackTrace(w);
			w.close();
			message += "\n" + sw.toString(); 
		}
		return message;
	}

}
