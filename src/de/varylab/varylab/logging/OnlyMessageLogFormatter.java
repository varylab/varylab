package de.varylab.varylab.logging;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class OnlyMessageLogFormatter extends Formatter {

	@Override
	public String format(LogRecord log) {
		String className = log.getSourceClassName();
		className = className.substring(className.lastIndexOf('.') + 1);
		String methodName = log.getSourceMethodName();
		return methodName + " " + log.getMessage();
	}

}
