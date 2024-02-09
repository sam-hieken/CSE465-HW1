package edu.miamioh;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Interpreter {
	private final Map<String, Object> variables;

	public Interpreter() {
		this(new HashMap<>());
	}

	public Interpreter(Map<String, Object> variables) {
		this.variables = variables;
	}

	/**
	 * Interprets a line.
	 * 
	 * @return True if successful, false if error occurred.
	 */
	public boolean interpret(String line) {
		line = line.trim();
		
		// Splits the line by spaces and stores it in a list.
		final List<String> div = new ArrayList<>(Arrays.asList(line.split("\\s")));

		// No valid line has less than two tokens.
		if (div.size() < 2) return false;
		
		final String operation = div.get(0);

		switch (operation) {

		case "FOR":
			return loop(div);
			
		case "PRINT":
			return print(div.get(1));
			
		default:
			return setVariable(line);
		}
	}
	
	/**
	 * Loop code certain number of times.
	 * @param line The entire line holding the loop (including FOR and ENDFOR)
	 * @return false if runtime error occurs
	 */
	private boolean loop(List<String> line) {
		// Error checks
		
		final String times = line.get(1);
		final String endFor = line.get(line.size() - 1);
		
		// Runtime error, either number not specified, or endfor not specified.
		if (!isNumeric(times) || !endFor.equals("ENDFOR"))
			return false;
		
		final String loopContent = String.join(" ", line.subList(2, line.size() - 1));
		
		// Actual loop logic
		
		return loop(loopContent, Integer.parseInt(times));
	}

	/**
	 * Loop code certain number of times. Called internally by {@link #loop(List)}
	 * @param loopContent Every line to operate on inside the loop (separated by semicolons).
	 * @param i The number of iterations.
	 * @return false if runtime error occurs
	 */
	private boolean loop(String loopContent, int i) {
		// ChatGPT
		final Pattern pattern = Pattern.compile("(?<=;)(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        final String[] resultArray = pattern.split(loopContent);
        
        for (int j = 0; j < i; j++) 
	        for (String line : resultArray)
	        	if (!interpret(line))
	        		return false;
	       
        return true;
	}
	
	/**
	 * Set a variable.
	 * @param <T>
	 * @param line The entire line containing the operation.
	 * @return false if runtime error occurs.
	 */
	private <T> boolean setVariable(String line) {
		if (!line.endsWith(" ;"))
			 return false;
		
		final String[] div = line.split("\\s");
		
		// Need a name, operation, and value at minimum for assignment.
		if (div.length < 3)
			return false;

		OpType op;

		switch (div[1]) {

		case "=": 
			op = OpType.SET;
			break;
		case "+=": 
			op = OpType.ADD;
			break; 
		case "-=": 
			op = OpType.SUBTRACT;
			break; 
		case "*=": 
			op = OpType.MULTIPLY;
			break; 

		default: return false;

		}
		
		return setVariable(line, op);
	}
	
	/**
	 * Intermediary between {@link #setVariable(List)} and {@link #setVariable(String, Object, OpType)}
	 * @param line The entire line, divided by whitespace.
	 * @param opType The type of operation.
	 * @return false if runtime error occurs.
	 */
	private boolean setVariable(String line, OpType opType) {
		final String[] div = line.split("\\s");
		
		final String name = div[0];
		final String op = div[1];
		
		// div[2] isn't sufficient; if it's a string literal, could
		// have spaces in it.
		final String val = line.substring(
				// Get's the part of string between operator and line end.
				line.indexOf(op) + op.length(), line.lastIndexOf(" ;"))
				.trim();
		
		// String
		if (val.startsWith("\"") && val.endsWith("\""))
			return setVariable(name, val.substring(1, val.length() - 1), opType);
		
		// Integer
		else if (isNumeric(val))
			return setVariable(name, Integer.parseInt(val), opType);
		
		// Not int or String, so we treat as variable.
		
		final Object obj = variables.get(val);
		
		// Variable passed as value not declared; runtime error.
		if (obj == null) return false;
		
		return setVariable(name, obj, opType);
	}
	
	/**
	 * Sets a variable.
	 * @param name The variable name (case sensitive)
	 * @param value The variable's value (String or Integer only)
	 * @param opType What to do to the previous value of the variable with regards to the new value.
	 * @return false if runtime error occurs
	 */
	private <T> boolean setVariable(String name, T value, OpType opType) {
		final boolean isNumber = value instanceof Integer;
		
		// Ensure value is either a String or Integer.
		if (!isNumber && !(value instanceof String))
			return false;
		
		// No checks needed; variables can change datatypes
		else if (opType == OpType.SET) {
			variables.put(name, value);
			return true;
		}
		
		final Object prevValue = variables.get(name);
		final boolean prevIsNumber = prevValue instanceof Integer;
		
		// No value assigned to variable already; cannot operate 
		// on it, runtime error.
		if (prevValue == null) return false;
		
		// Different datatypes; cannot operate on it, runtime error.
		else if (prevIsNumber != isNumber) return false;
		
		switch (opType) {
		
		case ADD:
			variables.put(name, isNumber 
					? (int) prevValue + (int) value 
					: (String) prevValue + (String) value);
			return true;
			
		case SUBTRACT:
			// Can't subtract strings
			if (!isNumber) return false;
			variables.put(name, (int) prevValue - (int) value);
			return true;
			
		case MULTIPLY:
			// Can't subtract strings
			if (!isNumber) return false;
			variables.put(name, (int) prevValue * (int) value);
			return true;
			
		// Debugging only; this should never be reached.
		default: throw new RuntimeException("Severe error occurred.");
		
		}
	}
	
	public boolean print(String variableName) {
		final Object value = variables.get(variableName);
		
		if (value == null) return false;
		
		System.out.println(variableName + "=" + value);
		return true;
	}

	// https://stackoverflow.com/questions/1102891/how-to-check-if-a-string-is-numeric-in-java
	private boolean isNumeric(String str) {
		return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
	}
}
