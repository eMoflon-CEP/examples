package org.emoflon.flight.model.definitions.generator;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.emoflon.flight.model.util.LongDateHelper;
import org.emoflon.flight.model.util.ModelParser;

public class BookingGenerator {
	/**
	 * output path
	 */
	static String outFilePath = "src\\org\\emoflon\\flight\\model\\definitions\\simple.bookings";
	/**
	 * folder where the ".bookingcflightgen" or ".bookingncflightgen" files are found
	 */
	static String genFolderPath = "src\\org\\emoflon\\flight\\model\\definitions\\";
	/**
	 * folder where the ".bookingcflightgen" or ".bookingncflightgen" files are found
	 */
	static String personsFilePath = "src\\org\\emoflon\\flight\\model\\definitions\\simple.persons";
	/**
	 * output header
	 */
	static String header = "//\r\n" + "// Bookings\r\n" + "// BOOKINGNO;(TRAVEL-ID|PERSON-ID|FLIGHTID(:FLIGHTID)*)*\r\n" + "//\n";
	/**
	 * number of times the generator should be run (for following days)
	 */
	static int repeat = 7;
	/**
	 * maximum of Persons, that can travel in a group
	 */
	private static int maxGroupSize = 4;
	/**
	 * days between start of a first travel an a new one
	 */
	private static int daysBetweenTravels = 2;
	/**
	 * rate of minimum plane capacity used for connecting flights
	 * (per connecting route)
	 */
	private static double connectingBookingRate = 0.1;
	/**
	 * rate of plane capacity used for direct flight
	 */
	private static double normalBookingRate = 0.75;
	/**
	 * number of bookings per day
	 */
	private static int bookingsPerDay = 4590;
	/**
	 * number of persons in the person list
	 */
	private static int personListSize = 14000;
	/**
	 * declares when looping through the person list again
	 */
	private static int loop = Math.min(bookingsPerDay * daysBetweenTravels, personListSize);
	/**
	 * person list
	 */
	private String[] personStrings = ModelParser.arrayParseFromFile(0, personsFilePath);
	/**
	 * start date for the generator
	 */
	private static long startDate = LongDateHelper.getDate(01, 01, 2020);
	/**
	 * seed for group building
	 */
	private static long groupSeed = 12345678;
	/**
	 * offset in persons list
	 */
	private int offset = 0;
	/**
	 * list of dummy connecting flights for booking generation
	 */
	private List<DummyFlight[]> connectingFlights;
	/**
	 * list of dummy non-connecting flights for booking generation
	 */
	private List<DummyFlight[]> nonConnectingFlights;
	/**
	 * random for group building
	 */
	private Random ran = new Random(groupSeed);
	
	public static void main(String[] args) {
		long tic = System.currentTimeMillis();
		try {
			PrintWriter pw = new PrintWriter(outFilePath);
			BookingGenerator bg = new BookingGenerator("simple.bookingcflightgen","simple.bookingncflightgen");

			List<String> generatedBookings = bg.generateBookings(repeat);
			pw.append(header);
			for (String s : generatedBookings) {
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
	 * @param fileNameConnectingFlights of the ".bookingcflightgen" file in genFolderPath
	 * @param fileNameNonConnectingFlights of the ".bookingncflightgen" file in genFolderPath
	 */
	public BookingGenerator(String fileNameConnectingFlights, String fileNameNonConnectingFlights) {
		connectingFlights = parseFlights(genFolderPath +fileNameConnectingFlights);
		nonConnectingFlights = parseFlights(genFolderPath + fileNameNonConnectingFlights);
	}
	/**
	 * @param repeat times to repeat generation staring from startDate
	 * @return a List containing bookings including travels in string format
	 */
	private List<String> generateBookings(int repeat) {
		ArrayList<String> bookings = new ArrayList<String>();		
		
		for (int rep = 0; rep <= repeat; rep++) { // loop for repeating days
			String date = LongDateHelper.getStringDDMMYYYY((startDate + rep * LongDateHelper.DAYINMS));
			addFlightsToBookingForRepDate(connectingFlights, rep, date, bookings, connectingBookingRate);
			addFlightsToBookingForRepDate(nonConnectingFlights, rep, date, bookings, normalBookingRate);
		}
		return bookings;
	}
	/**
	 * @param flightList a List containing dummy flight arrays with possible route combinations
	 * @param rep since startDate
	 * @param date string representation of current date
	 * @param bookings list of bookings in string format
	 * @param bookingRate booking rate for given flight combinations
	 */
	private void addFlightsToBookingForRepDate(List<DummyFlight[]> flightList, int rep, String date, List<String> bookings, double bookingRate) {
		for (DummyFlight[] flights : flightList) { // loop for connecting flights
			// string-array representation of flights in connecting travel
			String[] flightsString = new String[flights.length];
			int minCapacity = flights[0].capacity;
			for (int numFlights = 0; numFlights < flights.length; numFlights++) {
				minCapacity = Math.min(minCapacity, flights[numFlights].capacity);
				flightsString[numFlights] = flights[numFlights].getUUIDForDate(rep, startDate);
			}
			int personsOnFlight = (int) (minCapacity * bookingRate);
			for (int person = offset; person < personsOnFlight + offset; person++) { // loop though persons
				// TODO: possibility for more than one travel a day / person
				String bookingNo = personStrings[person % loop] + date;
				int groupSize = Math.min(ran.nextInt(maxGroupSize) + 1, personsOnFlight + offset - person);
				// creating travels
				DummyTravel[] travels = new DummyTravel[groupSize];
				for (int personsInBooking = 0; personsInBooking < groupSize; personsInBooking++) {
					String personString = personStrings[(person + personsInBooking) % loop];
					travels[personsInBooking] = new DummyTravel(personString, flightsString);
				}
				person += groupSize - 1; // already incremented one
				bookings.add(new DummyBooking(bookingNo, travels).toString());
			}
			offset += personsOnFlight; // increment offset for looping through persons
		}
	}	
	/**
	 * @param fileName of the ".bookingcflightgen" or ".bookingncflightgen" file in genFolderPath
	 * @return a List containing dummy flight arrays with possible route combinations
	 */
	private List<DummyFlight[]> parseFlights(String fileName) {
		ArrayList<DummyFlight[]> flightCombos = new ArrayList<DummyFlight[]>();
		ArrayList<String[]> dummyCFlightStrings = ModelParser.parseFile(fileName);
		for(String[] dummyCFlightString : dummyCFlightStrings) {
			DummyFlight[] dummyFlights = new DummyFlight[dummyCFlightString.length];
			for(int i=0; i < dummyCFlightString.length;i++) {
				String dummyFlightString = dummyCFlightString[i];
				String[] splitDummyFlight = dummyFlightString.split(",");
				String routeID = splitDummyFlight[0];
				int capacity = Integer.parseInt(splitDummyFlight[1]);
				int offset = Integer.parseInt(splitDummyFlight[2]);
				dummyFlights[i] = new DummyFlight(routeID, capacity, offset);
			}
			flightCombos.add(dummyFlights);
		}
		return flightCombos;
	}

	class DummyFlight {
		/**
		 * routeID for this flight
		 */
		private String routeID;
		/**
		 * minimum capacity for planes performing this flight
		 */
		int capacity;
		/**
		 * offset of days to the given date of this flight
		 */
		int offset;

		public DummyFlight(String routeID, int capacity, int offset) {
			this.routeID = routeID;
			this.capacity = capacity;
			this.offset = offset;
		}
		/**
		 * @param repeat since startDate
		 * @param startDate in ms (see Java.Date)
		 * @return String including routeID and Date
		 */
		public String getUUIDForDate(int repeat, long startDate) {
			long time = startDate + (repeat + offset) * LongDateHelper.DAYINMS;
			return routeID + LongDateHelper.getStringDDMMYYYY(time);
		}
	}

	class DummyBooking {
		/**
		 * unique booking ID
		 */
		String ID;
		/**
		 * travels in booking
		 */
		DummyTravel[] travels;

		public DummyBooking(String ID, DummyTravel[] travels) {
			this.ID = ID;
			this.travels = travels;
		}
		/**
		 * output booking as string in parse-able format
		 */
		public String toString() {
			String s = ID + ";";
			for (int i = 0; i < travels.length; i++) {
				s += travels[i];
				if (i < travels.length - 1)
					s += ",";
			}
			return s;
		}
	}

	class DummyTravel {
		/**
		 * unique travelID
		 */
		String travelID;
		/**
		 * personID performing this travel
		 */
		final String personID;
		/**
		 * flights included in this travel
		 */
		final String[] flights;

		public DummyTravel(String personID, String[] flights) {
			this.personID = personID;
			travelID = personID;
			this.flights = flights;
			for (String flight : flights)
				travelID += flight;
		}
		/**
		 * output travel as string in parse-able format
		 */
		public String toString() {
			String s = travelID + "|" + personID + "|";
			for (int i = 0; i < flights.length; i++) {
				s += flights[i];
				if (i < flights.length - 1)
					s += ":";
			}
			return s;
		}
	}
}