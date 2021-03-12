package org.emoflon.flight.model.generator;

import Flights.FlightModel;
import Flights.FlightsFactory;

public class SimpleModelGenerator extends ModelGenerator{
	
	/**
	 * instance of FlightsFactory
	 */
	private FlightsFactory factory = FlightsFactory.eINSTANCE;
	
	/**
	 * @return a simple flight model containing, airports, routes, planes, flights, persons, and bookings,
	 *  parsed from the 'simple.*' files found in '/Flights/src/org/emoflon/flight/model/definitions'
	 */
	public FlightModel generateSimpleModel(String definitionFolder) {
		FlightModel model = factory.createFlightModel();
		model.setAirports(parseAirportsWithGates(definitionFolder+"/simple.airports"));
		model.setRoutes(parseRoutes(definitionFolder+"/simple.routes", model.getAirports()));
		model.setPlanes(parsePlanes(definitionFolder+"/simple.planes"));
		model.setFlights(parseFlightContainer(definitionFolder+"/simple.flights", model.getAirports(), model.getRoutes(), model.getPlanes()));
		model.setPersons(parsePersons(definitionFolder+"/simple.persons"));
		model.setBookings(parseBookings(definitionFolder+"/simple.bookings", model.getPersons(), model.getFlights()));
		
		return model;
	}
}
