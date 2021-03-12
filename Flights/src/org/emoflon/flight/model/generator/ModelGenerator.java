package org.emoflon.flight.model.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.emoflon.flight.model.util.ModelParser;

import Flights.Airport;
import Flights.Airports;
import Flights.Booking;
import Flights.Bookings;
import Flights.Flight;
import Flights.FlightContainer;
import Flights.FlightsFactory;
import Flights.Gate;
import Flights.Person;
import Flights.Persons;
import Flights.Plane;
import Flights.Planes;
import Flights.Route;
import Flights.Routes;
import Flights.TimeStamp;
import Flights.Travel;

public class ModelGenerator {
	/**
	 * instance of FlightsFactory
	 */
	private FlightsFactory factory = FlightsFactory.eINSTANCE;
	final static Random rnd = new Random("ConstantSeed42".hashCode());
	/**
	 * @param airports containing the desired airport
	 * @param ID identifying the desired airport
	 * @return the airport from the container or 'null' if no airport with the given ID is found
	 */
	protected static Airport getAirportWithID(Airports airports, String ID) {
		for (Airport airport : airports.getAirports())
			if (airport.getID().equals(ID))
				return airport;
		return null;
	}
	/**
	 * @param airports container to search in
	 * @param ID identifying the desired airport
	 * @return a random gate in the desired airport or 'null' if no airport with the given ID is found
	 */
	protected static Gate getRandomGateWithID(Airports airports, String ID) {
		return getRandomGate(getAirportWithID(airports, ID));
	}
	/**
	 * @param airport  
	 * @return a random gate from the specified airport or 'null' if no airport with the given ID is found
	 */
	protected static Gate getRandomGate(Airport airport) {
		if(airport==null) return null;
		int size = airport.getGates().size();
//		int rand = (int) (Math.random() * size);
		int rand = (int) (rnd.nextDouble() * size);
		return airport.getGates().get(rand);
	}
	/**
	 * @param routes containing the desired route
	 * @param ID identifying the desired route
	 * @return the route from the container or 'null' if no route with the given ID is found
	 */
	protected static Route getRouteWithID(Routes routes, String ID) {
		for (Route route : routes.getRoutes())
			if (route.getID().equals(ID))
				return route;
		return null;
	}
	/**
	 * @param planes containing the desired plane
	 * @param ID identifying the desired plane
	 * @return the plane from the container or 'null' if no plane with the given ID is found
	 */
	protected static Plane getPlaneWithID(Planes planes, String ID) {
		for (Plane plane : planes.getPlanes())
			if (plane.getID().equals(ID))
				return plane;
		return null;
	}
	/**
	 * @param flightContainer containing the desired flight
	 * @param ID identifying the desired flight
	 * @return the flight from the container or 'null' if no flight with the given ID is found
	 */
	protected static Flight getFlightWithID(FlightContainer flightContainer, String ID) {
		for (Flight flight : flightContainer.getFlights())
			if (flight.getID().equals(ID))
				return flight;
		System.out.println("No matching flight");
		return null;
	}
	/**
	 * @param persons containing the desired person
	 * @param ID identifying the desired person
	 * @return the person from the container or 'null' if no person with the given ID is found
	 */
	protected static Person getPersonWithID(Persons persons, String ID) {
		for (Person person : persons.getPersons())
			if (person.getID().equals(ID))
				return person;
		return null;
	}
	/**
	 * @param fileName of the corresponding '.airports' file
	 * @return an airport container filled with airport parsed from the '.airports' file
	 */
	protected Airports parseAirportsWithGates(String fileName) {
		List<Airport> airportList = parseAirprortListWithGates(fileName);
		Airports airports = factory.createAirports();
		airports.getAirports().addAll(airportList);
		return airports;
	}
	/**
	 * @param fileName of the corresponding '.airports' file
	 * @return a list of airports filled with airport objects parsed from the '.airports' file
	 */
	protected List<Airport> parseAirprortListWithGates(String fileName) {
		// TODO: ARRAYLIST check for alternative
		ArrayList<Airport> airportList = new ArrayList<Airport>();
		ArrayList<String[]> airportStrings = ModelParser.parseFile(fileName);
		for (String[] airportString : airportStrings) {
			String ID = airportString[0];
			int numOfGates = Integer.parseInt(airportString[1]);
			double size = Double.parseDouble(airportString[2]);
			airportList.add(createAiportWithGates(ID, numOfGates, size));
		}
		return airportList;
	}
	/**
	 * @param fileName of the corresponding '.routes' file
	 * @param airports containing all airports named in the '.routes' file
	 * @return a route container filled with route objects parsed from the '.routes' file
	 */
	protected Routes parseRoutes(String fileName, Airports airports) {
		List<Route> routeList = parseRouteList(fileName, airports);
		Routes routes = factory.createRoutes();
		routes.getRoutes().addAll(routeList);
		return routes;
	}
	/**
	 * @param fileName of the corresponding '.routes' file
	 * @param airports containing all airports named in the '.routes' file
	 * @return a route list filled with route objects parsed from the '.routes' file
	 */
	protected List<Route> parseRouteList(String fileName, Airports airports) {
		// TODO: ARRAYLIST check for alternative
		ArrayList<Route> routeList = new ArrayList<Route>();
		ArrayList<String[]> routeStrings = ModelParser.parseFile(fileName);
		for (String[] routeString : routeStrings) {
			String ID = routeString[0];
			Airport source = getAirportWithID(airports, routeString[1]);
			Airport target = getAirportWithID(airports, routeString[2]);
			long duration = Long.parseLong(routeString[3]);

			routeList.add(createRoute(ID, source, target, duration));
		}
		return routeList;
	}
	
	/**
	 * @param fileName of the corresponding '.flights' file
	 * @param airports containing all airports named in the '.flights' file
	 * @param routes containing all routes named in the '.flights' file
	 * @param planes containing all planes named in the '.flights' file
	 * @return a flight container filled with flight objects parsed from the '.flights' file
	 */
	protected FlightContainer parseFlightContainer(String fileName, Airports airports, Routes routes, Planes planes) {
		List<Flight> flightList = parseFlightList(fileName, airports, routes, planes);
		FlightContainer flights = factory.createFlightContainer();
		flights.getFlights().addAll(flightList);
		return flights;
	}
	/**
	 * @param fileName of the corresponding '.flights' file
	 * @param airports containing all airports named in the '.flights' file
	 * @param routes containing all routes named in the '.flights' file
	 * @param planes containing all planes named in the '.flights' file
	 * @return a flight list filled with flight objects as declared in the '.flights' file
	 */
	protected List<Flight> parseFlightList(String fileName, Airports airports, Routes routes, Planes planes) {
		// TODO: ARRAYLIST check for alternative
		ArrayList<Flight> flights = new ArrayList<Flight>();
		ArrayList<String[]> flightStrings = ModelParser.parseFile(fileName);

		for (String[] flightString : flightStrings) {
			String ID = flightString[0];
			Route route = getRouteWithID(routes, flightString[1]);
			long departure = Long.parseLong(flightString[2]);
			long arrival = Long.parseLong(flightString[3]);
			Plane plane = getPlaneWithID(planes, flightString[4]);
			Gate source = getRandomGate(route.getSrc());
			Gate target = getRandomGate(route.getTrg());

			flights.add(createFlight(ID, departure, arrival, route, plane, source, target));
		}

		return flights;
	}
	/**
	 * @param fileName of the corresponding '.planes' file
	 * @return a plane container filled with plane objects parsed from the '.planes' file
	 */
	protected Planes parsePlanes(String fileName) {
		List<Plane> planeList = parsePlaneList(fileName);
		Planes planes = factory.createPlanes();
		planes.getPlanes().addAll(planeList);

		return planes;
	}
	/**
	 * @param fileName of the corresponding '.planes' file
	 * @return a plane list filled with plane objects parsed from the '.planes' file
	 */
	protected List<Plane> parsePlaneList(String fileName) {
		// TODO: ARRAYLIST check for alternative
		ArrayList<Plane> planeList = new ArrayList<Plane>();
		ArrayList<String[]> planeStrings = ModelParser.parseFile(fileName);
		for (String[] planeString : planeStrings) {
			String ID = planeString[0];
			int capacity = Integer.parseInt(planeString[1]);
			planeList.add(createPlane(ID, capacity));
		}
		return planeList;
	}
	/**
	 * @param fileName of the corresponding '.persons' file
	 * @return a person container filled with person objects parsed from the '.persons' file
	 */
	protected Persons parsePersons(String fileName) {
		List<Person> personList = parsePersonList(fileName);
		Persons persons = factory.createPersons();
		persons.getPersons().addAll(personList);
		return persons;
	}
	/**
	 * @param fileName of the corresponding '.persons' file
	 * @return a person list filled with person objects parsed from the '.persons' file
	 */
	protected List<Person> parsePersonList(String fileName) {
		// TODO: ARRAYLIST check for alternative
		ArrayList<Person> personList = new ArrayList<Person>();
		ArrayList<String[]> personsString = ModelParser.parseFile(fileName);
		for (String[] personString : personsString)
			personList.add(createPerson(personString[0]));
		return personList;
	}
	/**
	 * @param fileName of the corresponding '.bookings' file
	 * @param persons containing all persons named in the '.bookings' file
	 * @param flightContainer containing all bookings named in the '.bookings' file
	 * @return a booking container filled with bookings objects parsed from the '.bookings' file
	 */
	protected Bookings parseBookings(String fileName, Persons persons, FlightContainer flightContainer) {
		List<Booking> bookingList = parseBookingList(fileName, persons, flightContainer);
		Bookings bookings = factory.createBookings();
		bookings.getBookings().addAll(bookingList);
		return bookings;
	}
	/**
	 * @param fileName of the corresponding '.bookings' file
	 * @param persons containing all persons named in the '.bookings' file
	 * @param flightContainer containing all bookings named in the '.bookings' file
	 * @return a booking list filled with bookings objects parsed from the '.bookings' file
	 */
	protected List<Booking> parseBookingList(String fileName, Persons persons, FlightContainer flightContainer) {
		// TODO: ARRAYLIST check for alternative
		ArrayList<Booking> bookingList = new ArrayList<Booking>();
		ArrayList<String[]> bookingsString = ModelParser.parseFile(fileName);
		for (String[] bookingString : bookingsString) {
			String ID = bookingString[0];
			List<Travel> travels = parseTravelList(persons, flightContainer, bookingString[1]);
			bookingList.add(createBooking(ID, travels));
		}
		return bookingList;
	}
	/**
	 * @param persons containing all persons named in the travelsString
	 * @param flightContainer containing all flights named in the travelsString
	 * @param travelsString containing travels in string format as parsed from parseBookingList
	 * @return a travel list filled with travel objects parsed from the travelsString
	 */
	private List<Travel> parseTravelList(Persons persons, FlightContainer flightContainer, String travelsString) {
		String[] travelString = travelsString.split(",");
		// TODO: ARRAYLIST check for alternative
		ArrayList<Travel> travels = new ArrayList<Travel>();
		for (String travel : travelString)
			travels.add(parseTravel(persons, flightContainer, travel));
		return travels;
	}

	/**
	 * @param persons containing all persons named in the travelString
	 * @param flightContainer containing all flights named in the travelString
	 * @param travelString containing a travel in string format as parsed from parseTravelList
	 * @return a travel object parsed from the travelString
	 */
	private Travel parseTravel(Persons persons, FlightContainer flightContainer, String travelString) {
		String[] splitTravelString = travelString.split("\\|");
		String ID = splitTravelString[0];
		Person person = getPersonWithID(persons, splitTravelString[1]);
		String[] travelFlights = splitTravelString[2].split(":");
		List<Flight> flights = new ArrayList<Flight>();
		for (String flight : travelFlights)
			flights.add(getFlightWithID(flightContainer, flight));
		return createTravel(ID, person, flights);
	}
	/**
	 * @param ID of the booking
	 * @param travels in booking
	 * @return a booking object
	 */
	protected Booking createBooking(String ID, List<Travel> travels) {
		Booking booking = factory.createBooking();
		booking.setID(ID);
		booking.getTravels().addAll(travels);
		return booking;
	}

	/**
	 * @param ID of the plane
	 * @param capacity of the plane
	 * @return a plane object
	 */
	protected Plane createPlane(String ID, int capacity) {
		Plane plane = factory.createPlane();
		plane.setID(ID);
		plane.setCapacity(capacity);
		return plane;
	}

	/**
	 * @param ID of the flight
	 * @param departure of the flight
	 * @param arrival of the flight
	 * @param route of the flight
	 * @param plane of the flight
	 * @param source gate of the airport, where the plane is departing from
	 * @param target gate of the airport, where the plane is arriving at
	 * @return a flight object
	 */
	protected Flight createFlight(String ID, long departure, long arrival, Route route, Plane plane, Gate source,
			Gate target) {
		Flight flight = factory.createFlight();
		flight.setID(ID);
		TimeStamp departureStamp = factory.createTimeStamp();
		departureStamp.setTime(departure);
		flight.setDeparture(departureStamp);
		TimeStamp arrivalStamp = factory.createTimeStamp();
		arrivalStamp.setTime(arrival);
		flight.setArrival(arrivalStamp);
		flight.setRoute(route);
		flight.setPlane(plane);
		flight.setSrc(source);
		flight.setTrg(target);
		return flight;
	}
	/**
	 * @param ID of the person
	 * @return a person object
	 */
	protected Person createPerson(String ID) {
		Person person = factory.createPerson();
		person.setID(ID);
		return person;
	}
	/**
	 * @param ID of the travel
	 * @param person performing the travel
	 * @param flights included in the travel
	 * @return a travel object
	 */
	protected Travel createTravel(String ID, Person person, List<Flight> flights) {
		Travel travel = factory.createTravel();
		travel.setID(ID);
		travel.setPerson(person);
		travel.getFlights().addAll(flights);
		return travel;
	}
	/**
	 * @param ID of the route
	 * @param source airport, where the route is starting from
	 * @param target airport, where the route is arriving at
	 * @param duration of the route
	 * @return a route object
	 */
	protected Route createRoute(String ID, Airport source, Airport target, long duration) {
		Route route = factory.createRoute();
		route.setID(ID);
		route.setSrc(source);
		route.setTrg(target);
		route.setDuration((int) duration);
		return route;
	}
	/**
	 * @param ID of the airport
	 * @param numOfGates number of gates in the airport
	 * @param size of the airport (normally below 1.0)
	 * @return a airport object including generated gates
	 */
	protected Airport createAiportWithGates(String ID, int numOfGates, double size) {
		Airport airport = createAirport(ID, size);
		ArrayList<Gate> gates = new ArrayList<Gate>();
		for (int i = 0; i < numOfGates; i++) {
			String gateID = (ID + "-T1-A" + i);
			gates.add(createGate(gateID, i));
		}
		airport.getGates().addAll(gates);
		return airport;
	}
	/**
	 * @param ID of the airport
	 * @param size of the airport (normally below 1.0)
	 * @return a airport object without gates
	 */
	protected Airport createAirport(String ID, double size) {
		Airport airport = factory.createAirport();
		airport.setID(ID);
		airport.setSize(size);
		return airport;
	}
	/**
	 * @param ID of the gate
	 * @param position of the gate
	 * @return a gate object
	 */
	protected Gate createGate(String ID, int position) {
		Gate gate = factory.createGate();
		gate.setID(ID);
		gate.setPosition(position);
		return gate;
	}
}
