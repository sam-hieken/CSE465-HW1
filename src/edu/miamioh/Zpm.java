package edu.miamioh;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Zpm {
	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.err.println("Error: Only 1 argument allowed (ZPM file path).");
			System.exit(-1);
		}
		
		run(args[0]);
	}
		
	public static void run(String filename) throws IOException {
		final Interpreter interpreter = new Interpreter();
		final BufferedReader br = new BufferedReader(new FileReader(filename));
		
		String line = null;
		int lineNumber = 0;
		
		while ((line = br.readLine()) != null) {
			++lineNumber;
			
			// Skip if just a blank line.
			if (line.isBlank()) continue;
			
			else if (!interpreter.interpret(line)) {
				System.err.println("RUNTIME ERROR: line " + lineNumber);
				break;
			}
		}
		
		br.close();
	}
}
