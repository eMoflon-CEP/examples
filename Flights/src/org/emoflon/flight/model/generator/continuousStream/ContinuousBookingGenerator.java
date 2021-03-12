package org.emoflon.flight.model.generator.continuousStream;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.emoflon.flight.model.generator.ModelGenerator;
import org.emoflon.flight.model.util.LongDateHelper;
import org.emoflon.flight.model.util.ModelParser;

import Flights.Booking;
import Flights.Flight;
import Flights.FlightContainer;
import Flights.Person;
import Flights.Persons;
import Flights.Travel;

public class ContinuousBookingGenerator extends ModelGenerator{
	/**
	 * person list
	 */
	private final List<Person> persons;
	/**
	 * declares when looping through the person list again
	 */
	private int loop;
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
	private int personListSize;
	/**
	 * seed for group building
	 */
	private static long groupSeed = 12345678;
	/**
	 * random for group building
	 */
	private Random ran = new Random(groupSeed);
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
	 * @param filePathConnectingFlights of the ".bookingcflightgen" file
	 * @param filePathNonConnectingFlights of the ".bookingncflightgen" file
	 * @param persons list including the persons that should be mapped to the bookings
	 */
	public ContinuousBookingGenerator(String filePathConnectingFlights, String filePathNonConnectingFlights, Persons persons) {
		connectingFlights = parseFlights(filePathConnectingFlights);
		nonConnectingFlights = parseFlights(filePathNonConnectingFlights);
		this.persons = persons.getPersons();
		this.personListSize = this.persons.size();
		this.loop = Math.min(bookingsPerDay * daysBetweenTravels, personListSize);
	}
	/**
	 * @param repeat times to repeat generation staring from startDate
	 * @param startDate for generation, make sure there are no overlapping generation dates!
	 * @param flightContainer containing all flights for the given time frame needed for booking generation
	 * @return a list of generated bookings for possible connecting and non-connecting flights in time-frames
	 */
	public List<Booking> createContiniousBookings(int repeat, long startDate, FlightContainer flightContainer) {
		List<Booking> bookings = new ArrayList<Booking>();
		for(int repeatingDays = 0; repeatingDays < repeat; repeatingDays++) {
			bookings.addAll(createBookingsForFlightList(connectingFlights, flightContainer, repeatingDays, startDate, connectingBookingRate));
			bookings.addAll(createBookingsForFlightList(nonConnectingFlights, flightContainer, repeatingDays, startDate, normalBookingRate));
		}
		return bookings;
	}
	/**
	 * @param dummyFlightList a list of dummy flight arrays with possible route combinations
	 * @param flightContainer containing all flights for the given time frame needed for booking generation
	 * @param repeatingDays repeated days since startDate
	 * @param startDate for generation, make sure there are no overlapping generation dates!
	 * @param bookingRate for the given combinations list
	 * @return a list of generated bookings for each entry in the dummyFlightList
	 */
	private List<Booking> createBookingsForFlightList(List<DummyFlight[]> dummyFlightList, FlightContainer flightContainer, int repeatingDays, long startDate, double bookingRate) {
		ArrayList<Booking> bookings = new ArrayList<Booking>();
		String date = LongDateHelper.getStringDDMMYYYY(startDate+repeatingDays*LongDateHelper.DAYINMS);
		for(DummyFlight[] dummyFlights: dummyFlightList) {
			List<Flight> flights = new ArrayList<Flight>();
			int minCapacity = dummyFlights[0].capacity;
			for (int flightCnt = 0; flightCnt < dummyFlights.length;flightCnt++) {
				minCapacity = Math.min(minCapacity, dummyFlights[flightCnt].capacity);
				flights.add(dummyFlights[flightCnt].getFlight(flightContainer, repeatingDays, startDate));
			}
			int personsOnFlight = (int) (minCapacity * bookingRate);
			for(int personID = offset; personID < personsOnFlight + offset; personID++) {
				String bookingNo = persons.get(personID % loop).getID() + date;
				int groupSize = Math.min(ran.nextInt(maxGroupSize) + 1, personsOnFlight + offset - personID);
				List<Travel> travels = new ArrayList<Travel>();
				for(int personsInBooking = 0; personsInBooking < groupSize; personsInBooking++) {
					Person person = persons.get((personID + personsInBooking)% loop);
					String travelID = person.getID();
					for (Flight flight: flights)
						travelID += flight.getID();
					travels.add(createTravel(travelID, person, flights));
				}
				personID += groupSize -1;
				bookings.add(createBooking(bookingNo, travels));
			}
			offset += personsOnFlight;
		}

		return bookings;
	}
	/**
	 * @param filePath of the ".bookingcflightgen" or ".bookingncflightgen" file
	 * @return a List containing dummy flight arrays with possible route combinations
	 */
	private List<DummyFlight[]> parseFlights(String filePath) {
		List<DummyFlight[]> flightCombos = new ArrayList<DummyFlight[]>();
		List<String[]> dummyCFlightStrings = ModelParser.parseFile(filePath);
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
		 * route id of the flight
		 */
		private String routeID;
		/**
		 * day offset within the route
		 */
		private int dayOffset;
		/**
		 * minimum capacity of the planes performing this flight
		 */
		int capacity;
		
		public DummyFlight(String routeID, int capacity, int dayOffset) {
			this.routeID=routeID;
			this.capacity=capacity;
			this.dayOffset=dayOffset;
		}
		/**
		 * @param flights container, containing the specific flight
		 * @param repeatingDays offset from the starting date
		 * @param startDate startDate
		 * @return a flight object with the given parameter found in the FlightContainer. If no flight is found, this method returns 'null'
		 */
		public Flight getFlight(FlightContainer flights, int repeatingDays, long startDate) {
			long time = startDate + (repeatingDays + dayOffset) * LongDateHelper.DAYINMS;
			String flightID = routeID + LongDateHelper.getStringDDMMYYYY(time);
			return getFlightWithID(flights, flightID);
		}
	}
}
