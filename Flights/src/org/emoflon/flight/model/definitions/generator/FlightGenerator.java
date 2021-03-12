package org.emoflon.flight.model.definitions.generator;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.emoflon.flight.model.util.LongDateHelper;
import org.emoflon.flight.model.util.ModelParser;


public class FlightGenerator {
	/**
	 * output path
	 */
	static String outFilePath = "src\\org\\emoflon\\flight\\model\\definitions\\simple.flights";
	/**
	 * Path to the '.flightgen' gen file
	 */
	static String genFilePath = "src\\org\\emoflon\\flight\\model\\definitions\\simple.flightgen";
	/**
	 * output header
	 */
	static String header = "//\r\n" + "// Flights\r\n"
			+ "// Flight-ID;Route-ID;Departure(in ms);Arrival(in ms);Plane-ID\r\n" + "//\n";
	/**
	 * number of times the generator should be run (for following days)
	 */
	static int repeat = 14;
	
	/**
	 * prevents possible trouble with automatic plane resolve
	 */
	private int allTimeRepeat = 0;

	public static void main(String[] args) {
		long tic = System.currentTimeMillis();
		FlightGenerator fg = new FlightGenerator();
		List<String> generatedFlights = fg.generateFlights(repeat,LongDateHelper.getDate(01,01,2020));
		try {
			PrintWriter pw = new PrintWriter(outFilePath);
			pw.append(header);
			for (String s : generatedFlights) {
				pw.append(s + "\n");
			}
			pw.flush();
			pw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		long toc = System.currentTimeMillis();
		
		System.out.println("Finished in: " + (toc - tic) + " ms");
	}
	/**
	 * @param fileName of the corresponding '.flightgen' file in '/Flights/src/org/emoflon/flight/model/definitions'
	 * @return a list of DummyFlights parsed from the flightgen file.
	 */
	private List<DummyFlight> generateDummyFlights(String fileName) {
		ArrayList<DummyFlight> dummyFlights = new ArrayList<FlightGenerator.DummyFlight>();
		ArrayList<String[]> dummyFlightStrings = ModelParser.parseFile(fileName);
		for (String[] dummyFlightString: dummyFlightStrings) {
			String routeID = dummyFlightString[0];
			long startDeparture = Long.parseLong(dummyFlightString[1]);
			long startArrival = Long.parseLong(dummyFlightString[2]);
			long repeatRate = Long.parseLong(dummyFlightString[3]);
			String[] planes = dummyFlightString[4].split(",");
			dummyFlights.add(new DummyFlight(routeID, startDeparture, startArrival, repeatRate, planes));
		}
		return dummyFlights;
	}
	/**
	 * @param repeat rate 
	 * @return a list of flights in a string representation
	 */
	private List<String> generateFlights(int repeat, long startDate) {
		ArrayList<String> flightsString = new ArrayList<String>();

		List<DummyFlight> dummyFlights = generateDummyFlights(genFilePath);

		for (int r = 0; r < repeat; r++) { // loop for repeat
			allTimeRepeat++;
			for (DummyFlight flight : dummyFlights) { // loop through flights
				flightsString.add(flight.createString(r,startDate));
			}
		}

		return flightsString;
	}

	class DummyFlight {
		/**
		 * starting departure of flight
		 */
		long startDeparture;
		/**
		 * starting arrival of flight
		 */
		long startArrival;
		/**
		 * repeating rate of flight
		 */
		long repeatRate;
		/**
		 * routeID of flight
		 */
		String routeID;
		/**
		 * planes for flight
		 */
		String[] planes;

		public DummyFlight(String routeID, long startDeparture, long startArrival, long repeatRate, String[] planes) {
			super();
			this.startDeparture = startDeparture;
			this.startArrival = startArrival;
			this.repeatRate = repeatRate;
			this.routeID = routeID;
			this.planes = planes;
		}

		/**
		 * @param repeat times since start
		 * @return unique ID for flight with routeID and date
		 */
		private String createUID(int repeat, long startDate) {
			return routeID + LongDateHelper.getStringDDMMYYYY(getRepeatedDepartureTime(repeat,startDate));
		}

		/**
		 * @param repeat times since start
		 * @return departure for repeat-times flight since start
		 */
		private long getRepeatedDepartureTime(int repeat, long startDate) {
			return startDeparture + (repeat * repeatRate) + startDate;
		}

		/**
		 * @param repeat times since start
		 * @return arrival for repeat-times flight since start
		 */
		private long getRepeatedArrivalTime(int repeat, long startDate) {
			return startArrival + (repeat * repeatRate) + startDate;
		}

		/**
		 * @param repeat times since start
		 * @return parse-able string representation of the flight without the plane-ID
		 */
		private String createStringWithoutPlane(int repeat, long startDate) {
			return createUID(repeat,startDate) + ";" + routeID + ";" + getRepeatedDepartureTime(repeat,startDate) + ";"
					+ getRepeatedArrivalTime(repeat,startDate);
		}

		/**
		 * @param repeat     times since start
		 * @param planeIndex specifies the plane from the array of planes via the index
		 * @return parse-able string representation of the flight including the plane-ID
		 */
		public String createString(int repeat, int planeIndex, long startDate) {
			return createStringWithoutPlane(repeat,startDate) + ";" + planes[planeIndex];
		}

		/**
		 * @param repeat times since start
		 * @return parse-able string representation of the flight including the plane-ID
		 */
		public String createString(int repeat, long startDate) {
			return createString(repeat, (repeat+allTimeRepeat) % planes.length, startDate);
		}
	}
}
