package de.varylab.varylab.logging;

import java.util.logging.ConsoleHandler;
import java.util.logging.LogRecord;

public class StdoutConsoleHandler extends ConsoleHandler {

	@Override
	public void publish(LogRecord record) {
		String message = getFormatter().format(record);
		System.out.println(message);
	}
	
	@Override
	public void close() {
		System.out.flush();
	}

}
