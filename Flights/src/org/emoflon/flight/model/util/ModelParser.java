package org.emoflon.flight.model.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ModelParser {
	/**
	 * @param fileName to parse in folder /Flights/src/org/emoflon/flight/model/definitions
	 * @return list of String arrays, containing each line of the file, that is not empty or begins with an '/',
	 *  splitting each line at the ';' character
	 */
	
	static Map<String, ArrayList<String[]>> cache = new HashMap<>();
	
	public static ArrayList<String[]> parseFile(String fileName) {
		if(cache.containsKey(fileName))
			return cache.get(fileName);
		try {
			File myObj = new File(fileName);
			ArrayList<String[]> out = new ArrayList<String[]>();
			Scanner myReader = new Scanner(myObj);
			while (myReader.hasNextLine()) {
				String data = myReader.nextLine();
				if (!(data.isEmpty() || data.charAt(0) == '/')) {
					String[] splitLine = data.split(";");
					out.add(splitLine);
				}
			}
			myReader.close();
			cache.put(fileName, out);
			return out;
		} catch (FileNotFoundException e) {
			System.err.println("File: "+fileName+" could not be found.");
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * @param index of the selected element in each split line
	 * @param filePath of the file to parse
	 * @return string array filled with the selected element for each line
	 */
	public static String[] arrayParseFromFile(int index, String filePath) {
		ArrayList<String[]> arrayList = parseFile(filePath);
		String[] out = new String[arrayList.size()];
		for(int i=0;i<arrayList.size();i++) {
			out[i] = arrayList.get(i)[0];
		}
		return out;
	}
}
