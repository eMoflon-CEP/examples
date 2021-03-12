package org.emoflon.flight.model.generator;

import Flights.FlightModel;
import Flights.FlightsFactory;

public class EvaluationModelGenerator extends SimpleModelGenerator {
	
	/**
	 * instance of FlightsFactory
	 */
	private FlightsFactory factory = FlightsFactory.eINSTANCE;
	
	/**
	 * @return a simple flight model containing, airports, routes, planes, and persons,
	 *  parsed from the 'simple.*' files found in '/Flights/src/org/emoflon/flight/model/definitions'
	 */
	public FlightModel generateSimpleModel(String definitionFolder) {
		FlightModel model = factory.createFlightModel();
		model.setAirports(parseAirportsWithGates(definitionFolder+"/simple.airports"));
		model.setRoutes(parseRoutes(definitionFolder+"/simple.routes", model.getAirports()));
		model.setPlanes(parsePlanes(definitionFolder+"/simple.planes"));
		model.setPersons(parsePersons(definitionFolder+"/simple.persons"));
		model.setFlights(factory.createFlightContainer());
		model.setBookings(factory.createBookings());
		
		return model;
	}
}
