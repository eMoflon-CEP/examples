package org.emoflon.flight.model.generator.continuousStream;

import java.util.ArrayList;
import java.util.List;

import org.emoflon.flight.model.generator.ModelGenerator;
import org.emoflon.flight.model.util.LongDateHelper;
import org.emoflon.flight.model.util.ModelParser;

import Flights.Flight;
import Flights.FlightsFactory;
import Flights.Gate;
import Flights.Plane;
import Flights.Planes;
import Flights.Route;
import Flights.Routes;
import Flights.TimeStamp;

public class ContinuousFlightGenerator extends ModelGenerator{
	/**
	 * instance of FlightsFactory
	 */
	private FlightsFactory factory = FlightsFactory.eINSTANCE;
	/**
	 * prevents possible trouble with automatic plane resolve
	 */
	private int allTimeRepeat = 0;
	/**
	 * dummyFlights parsed from the '.flightgen' file
	 */
	private List<DummyFlight> dummyFlights;
	/**
	 * @param filePath of the corresponding '.flightgen' file
	 * @param routes in the model to map to the dummy flights
	 * @param planes in the model to map to the dummy flights
	 */
	public ContinuousFlightGenerator(String filePath, Routes routes, Planes planes) {
		dummyFlights = generateDummyFlights(filePath, routes, planes);
	}
	/**
	 * @param repeat rate
	 * @param startDate for flight generation (!make sure, that there are no overlapping generations)
	 * @return a list of flights generated from dummy flights parsed from the '.flightgen' file
	 */
	public List<Flight> createContinuousFlights(int repeat, long startDate) {
		List<Flight> flights = new ArrayList<Flight>();
		for (int r = 0; r < repeat; r++) { // loop for repeat
			allTimeRepeat++;
			for (DummyFlight flight : dummyFlights) { // loop through flights
				flights.add(flight.convertToFlight(r, startDate));
			}
		}
		return flights;
	}
	/**
	 * @param filePath of the corresponding '.flightgen' file
	 * @param routes in the model to map to the dummy flights
	 * @param planes in the model to map to the dummy flights
	 * @return a parsed list of dummy flights
	 */
	private List<DummyFlight> generateDummyFlights(String filePath, Routes routes, Planes planes) {
		ArrayList<DummyFlight> dummyFlights = new ArrayList<DummyFlight>();
		ArrayList<String[]> dummyFlightStrings = ModelParser.parseFile(filePath);
		for (String[] dummyFlightString: dummyFlightStrings) {
			String routeID = dummyFlightString[0];
			long startDeparture = Long.parseLong(dummyFlightString[1]);
			long startArrival = Long.parseLong(dummyFlightString[2]);
			long repeatRate = Long.parseLong(dummyFlightString[3]);
			String[] planesString = dummyFlightString[4].split(",");
			Plane[] planesArr = new Plane[planesString.length];
			for(int i=0; i<planesString.length;i++) {
				String planeString = planesString[i];
				planesArr[i] = getPlaneWithID(planes, planeString);
			}
			Route route = getRouteWithID(routes, routeID);
			dummyFlights.add(new DummyFlight(route, startDeparture, startArrival, repeatRate, planesArr));
		}
		return dummyFlights;
	}
	
	class DummyFlight {
		/**
		 * of the flight
		 */
		private Route route;
		/**
		 * start departure of the flight
		 */
		private long startDeparture;
		/**
		 * start arrival of the flight
		 */
		private long startArrival;
		/**
		 * repeat-rate for the flight. Must be at least 86400000 (=1 DAY)
		 */
		private long repeatRate;
		/**
		 * list of planes mapped to this flight
		 */
		private Plane[] planes;
		
		public DummyFlight(Route route, long startDeparture, long startArrival, long repeatRate, Plane[] planes) {
			this.route=route;
			this.startDeparture = startDeparture;
			this.startArrival = startArrival;
			this.repeatRate = repeatRate;
			this.planes = planes;
		}
		/**
		 * @param repeat times since start
		 * @return unique ID for flight with routeID and date
		 */
		private String createUFlightID(long startDate, int repeat) {
			return route.getID() + LongDateHelper.getStringDDMMYYYY(getRepeatedDepartureTime(repeat,startDate));
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
		 * @param planeIndex of the plane for specific flight from planes array
		 * @param startDate for flight generation
		 * @return a flight object representation of the dummy flight for this specific date
		 */
		public Flight convertToFlight(int repeat, int planeIndex, long startDate) {
			Flight flight = factory.createFlight();
			flight.setID(createUFlightID(startDate, repeat));
			Gate source = getRandomGate(route.getSrc());
			Gate target = getRandomGate(route.getTrg());
			TimeStamp departureStamp = factory.createTimeStamp();
			departureStamp.setTime(getRepeatedDepartureTime(repeat, startDate));
			flight.setDeparture(departureStamp);
			TimeStamp arrivalStamp = factory.createTimeStamp();
			arrivalStamp.setTime(getRepeatedArrivalTime(repeat, startDate));
			flight.setArrival(arrivalStamp);
			flight.setRoute(route);
			flight.setPlane(planes[planeIndex]);
			flight.setSrc(source);
			flight.setTrg(target);
			
			return flight;
		}
		/**
		 * @param repeat times since start
		 * @param startDate for flight generation
		 * @return a flight object representation of the dummy flight for this specific date
		 * for which the plane is derived from the amount of repeats
		 */
		public Flight convertToFlight(int repeat, long startDate) {
			return convertToFlight(repeat, (repeat+allTimeRepeat) % planes.length, startDate);
		}
	}
}
