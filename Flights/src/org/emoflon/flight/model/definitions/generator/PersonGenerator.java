package org.emoflon.flight.model.definitions.generator;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PersonGenerator {
	/**
	 * output path
	 */
	static String outFilePath = "src\\org\\emoflon\\flight\\model\\definitions\\simple.persons";
	/**
	 * output header
	 */
	static String header = "//\r\n" + "// Persons\r\n" + "// PERSON-ID\r\n" + "//\n";
	/**
	 * amount of people to be generated
	 */
	static int amount = 14000;
	/**
	 * alphabet allowed to create new person-IDs
	 */
	private static String[] allowedAlphabet = { "C", "F", "G", "H", "J", "K", "L", "M", "N", "P", "R", "T", "V", "W", "X", "Y",
			"Z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
	private static long personSeed = 42;
	
	public static void main(String[] args) {
		long tic = System.currentTimeMillis();
		PersonGenerator pg = new PersonGenerator();
		List<String> generatedPersons = pg.generatePersons(amount);
		try {
			PrintWriter pw = new PrintWriter(outFilePath);
			for (String s: generatedPersons)
				pw.append(s + "\n");
			pw.flush();
			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long toc = System.currentTimeMillis();
		System.out.println("Finished in: " + (toc - tic) + " ms");
	 }

	/**
	 * @param amount of persons to be generated
	 * @return list of persons with unique person IDs
	 */
	private List<String> generatePersons(int amount) {
		ArrayList<String> generatedPersons = new ArrayList<String>();
		int alphabetLength = allowedAlphabet.length;
		Random ran = new Random(personSeed);

		for (int i = 0; i < amount; i++) {
			String personID = allowedAlphabet[ran.nextInt(alphabetLength)];
			for (int stringCnt = 1; stringCnt <= 9; stringCnt++)
				personID += allowedAlphabet[ran.nextInt(alphabetLength)];
			if (generatedPersons.contains(personID)) i--;
			else generatedPersons.add(personID);
		}

		return generatedPersons;
	}
}
