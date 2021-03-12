package org.emoflon.flight.scenario;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.Random;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.emoflon.flight.model.generator.SimpleModelGenerator;
import org.emoflon.flight.model.util.LongDateHelper;

import Flights.Flight;
import Flights.FlightModel;
import Flights.FlightsFactory;
import Flights.TimeStamp;

public class ScenarioRunner {
	
	private SimpleModelGenerator modelGenerator;
	private FlightModel model;
	private ScenarioGenerator eventGenerator;
	private Random rnd;
	private double flightEventProbability = 0.1;
	
	private Queue<Flight> flights;
	private Queue<Flight> inFlight;
	
	private FlightsFactory factory = FlightsFactory.eINSTANCE;
	
	public void initModel(final String definitionFolder) {
		modelGenerator = new SimpleModelGenerator();
		model = modelGenerator.generateSimpleModel(definitionFolder);
		
		//Set initial global time to earliest flight
		long minTime = model.getFlights().getFlights().stream()
			.map(flight -> flight.getDeparture().getTime())
			.reduce((long)0, (min, value) -> ((min<value)?min:value));
		TimeStamp globalTime = factory.createTimeStamp();
		globalTime.setTime(minTime);
		model.setGlobalTime(globalTime);
		
		Comparator<Flight> departureTimeSort = Comparator.comparing(flight -> flight.getDeparture().getTime());
		flights = new PriorityBlockingQueue<Flight>(10, departureTimeSort);
		
		Comparator<Flight> arrivalTimeSort = Comparator.comparing(flight -> flight.getDeparture().getTime());
		inFlight = new PriorityBlockingQueue<Flight>(10, arrivalTimeSort);
		
		for(Flight flight : model.getFlights().getFlights()) {
			flights.add(flight);
		}
	}
	
	public void initModelEventGenerator() {
		eventGenerator = new ScenarioGenerator();
		rnd = new Random();
	}
	
	public void initModelEventGenerator(long eventSeed, long flightSeed, long chaosSeed, double chaosFactor, double flightEventProbability) {
		eventGenerator = new ScenarioGenerator(eventSeed, flightSeed, chaosSeed, chaosFactor);
		this.flightEventProbability = flightEventProbability;
		rnd = new Random(flightSeed);
	}
	
	public FlightModel getModel() {
		return model;
	}
	
//	public void advanceTime(int numberOfFlights) {
//		if(flights.isEmpty())
//			return;
//		
//		LinkedList<Flight> candidates = new LinkedList<>();
//		for(int i = 0; i<numberOfFlights; i++) {
//			if(flights.isEmpty())
//				break;
//			
//			Flight flight = flights.poll();
//			inFlight.add(flight);
//			candidates.add(flight);
//		}
//		
//		for(Flight flight : candidates) {
//			eventGenerator.runScenario(flight, flightEventProbability);
//		}
//		model.setGlobalTime(LongDateHelper.createTimeStamp(candidates.getLast().getDeparture(), 0));
//		
//		while(!inFlight.isEmpty() && inFlight.peek().getArrival().getTime() <= model.getGlobalTime().getTime()) {
//			EcoreUtil.delete(inFlight.poll());
//		}
//	}

//	public void advanceTimeRnd() {
//		if(flights.isEmpty())
//			return;
//		
//		int nextFlights = 1+rnd.nextInt(flights.size()-1);
//		LinkedList<Flight> candidates = new LinkedList<>();
//		for(int i = 0; i<nextFlights; i++) {
//			if(flights.isEmpty())
//				break;
//			
//			Flight flight = flights.poll();
//			inFlight.add(flight);
//			candidates.add(flight);
//		}
//		
//		for(Flight flight : candidates) {
//			eventGenerator.runScenario(flight, flightEventProbability);
//		}
//		model.setGlobalTime(LongDateHelper.createTimeStamp(candidates.getLast().getDeparture(), 0));
//		
//		while(!inFlight.isEmpty() && inFlight.peek().getArrival().getTime() <= model.getGlobalTime().getTime()) {
//			EcoreUtil.delete(inFlight.poll());
//		}
//	}
	
	public boolean advanceTime() {
		Flight flight = null;
		if(!flights.isEmpty()) {
			 flight = flights.poll();
			 inFlight.add(flight);
			 model.setGlobalTime(LongDateHelper.createTimeStamp(flight.getDeparture(), 0));
		}
		
		if(flight == null && !inFlight.isEmpty()) {
			flight = inFlight.poll();
			model.setGlobalTime(LongDateHelper.createTimeStamp(flight.getArrival(), 0));
		}
		
		if(flight == null)
			return false;
		
		eventGenerator.runScenario(flight, flightEventProbability);
		
		if(!flights.isEmpty() && inFlight.isEmpty()) {
			EcoreUtil.delete(flight);
			return false;
		}
		
		while(!inFlight.isEmpty() && inFlight.peek().getArrival().getTime() <= model.getGlobalTime().getTime()) {
			EcoreUtil.delete(inFlight.poll());
		}
		
		return true;

	}
	
}
