package org.emoflon.flight.scenario;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.PriorityBlockingQueue;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.emoflon.flight.model.generator.EvaluationModelGenerator;
import org.emoflon.flight.model.generator.SimpleModelGenerator;
import org.emoflon.flight.model.generator.continuousStream.ContinuousBookingGenerator;
import org.emoflon.flight.model.generator.continuousStream.ContinuousFlightGenerator;
import org.emoflon.flight.model.util.LongDateHelper;

import Flights.Flight;
import Flights.FlightModel;
import Flights.FlightsFactory;
import Flights.TimeStamp;

public class EvaluationScenarioRunner extends ScenarioRunner {
	
	private SimpleModelGenerator modelGenerator;
	private FlightModel model;
	private ScenarioGenerator eventGenerator;
	private ContinuousBookingGenerator bookingGenerator;
	private ContinuousFlightGenerator flightGenerator;
//	private Random rnd;
	private double flightEventProbability = 0.1;
	
	private Queue<Flight> flights;
	private Queue<Flight> inFlight;
	
	private FlightsFactory factory = FlightsFactory.eINSTANCE;
	private long bookingsUntilDate = LongDateHelper.getDate(01, 01, 2020);
	private long flightsUntilDate = bookingsUntilDate;
	private int flightOffset = 4;
	private int initalStepSize = 8;
	
	public EvaluationScenarioRunner() {
	}
	
	public EvaluationScenarioRunner(int initalStepSize, int flightOffset) {
		this.initalStepSize = initalStepSize;
		this.flightOffset = flightOffset;
	}
	
	@Override
	public void initModel(final String definitionFolder) {
		modelGenerator = new EvaluationModelGenerator();
		model = modelGenerator.generateSimpleModel(definitionFolder);
		
		flightGenerator = new ContinuousFlightGenerator(definitionFolder+"/simple.flightgen", model.getRoutes(), model.getPlanes());
		bookingGenerator = new ContinuousBookingGenerator(definitionFolder+"/simple.bookingcflightgen", definitionFolder+"/simple.bookingncflightgen", model.getPersons());
		
		model.getFlights().getFlights().addAll(flightGenerator.createContinuousFlights(initalStepSize+flightOffset, flightsUntilDate));
		model.getBookings().getBookings().addAll(bookingGenerator.createContiniousBookings(initalStepSize, bookingsUntilDate, model.getFlights()));
		bookingsUntilDate += LongDateHelper.DAYINMS * initalStepSize;
		flightsUntilDate = bookingsUntilDate + LongDateHelper.DAYINMS * flightOffset;
		
		//Set initial global time to earliest flight
		long minTime = model.getFlights().getFlights().stream()
			.map(flight -> flight.getDeparture().getTime())
			.reduce(bookingsUntilDate, (min, value) -> ((min<value)?min:value));
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
		
		System.out.println("Initial number of flights: "+ flights.size());
		System.out.println("Initial number of bookings: "+ model.getBookings().getBookings().size());
		System.out.println("Initial number of travels: "+ model.getBookings().getBookings().stream().flatMap(booking -> booking.getTravels().stream()).distinct().count());
		System.out.println("Initial number of Persons: "+ model.getPersons().getPersons().size());
	}
	
	public void initModelEventGenerator() {
		eventGenerator = new ScenarioGenerator();
//		rnd = new Random();
	}
	
	public void initModelEventGenerator(long eventSeed, long flightSeed, long chaosSeed, double chaosFactor, double flightEventProbability) {
		eventGenerator = new ScenarioGenerator(eventSeed, flightSeed, chaosSeed, chaosFactor);
		this.flightEventProbability = flightEventProbability;
//		rnd = new Random(flightSeed);
	}
	
	public FlightModel getModel() {
		return model;
	}
	
	public void addFlightsAndBookings(int step) {
		addFlights(step);
		addBookings(step);
	}
	
	private void addFlights(int step) {
		List<Flight> newFlights = flightGenerator.createContinuousFlights(step, flightsUntilDate);
		model.getFlights().getFlights().addAll(newFlights);
		flightsUntilDate += LongDateHelper.DAYINMS * step;
		
		for(Flight flight : newFlights) {
			flights.add(flight);
		}
	}
	
	private void addBookings(int step) {
		model.getBookings().getBookings().addAll(bookingGenerator.createContiniousBookings(step, bookingsUntilDate, model.getFlights()));
		bookingsUntilDate += LongDateHelper.DAYINMS * step;
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
	
	private int changes = 0;
	
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
		
		if(eventGenerator.runScenario(flight, flightEventProbability)) {
			changes++;
		}
		
		if(!flights.isEmpty() && inFlight.isEmpty()) {
			EcoreUtil.delete(flight);
			return false;
		}
		
		while(!inFlight.isEmpty() && inFlight.peek().getArrival().getTime() <= model.getGlobalTime().getTime()) {
			EcoreUtil.delete(inFlight.poll());
		}
		
		return true;

	}
	
	public boolean runForDays(double days) {
		long timePeriod = (long)(((double)LongDateHelper.DAYINMS)*days);
		System.out.println("Running simulation for "+LongDateHelper.deltaAsString(timePeriod));
		
		long dT = 0;
		long initialTime = model.getGlobalTime().getTime();
		System.out.println("Starting at "+LongDateHelper.getStringDDMMYYYY(initialTime));
		boolean flightsLeft = true;
		while(dT < timePeriod && flightsLeft) {
			flightsLeft = advanceTime();
			dT = model.getGlobalTime().getTime() - initialTime;
		}
		System.out.println("Ending at "+LongDateHelper.getStringDDMMYYYY(model.getGlobalTime().getTime()));
		System.out.println("Simulation actually ran for "+LongDateHelper.deltaAsString(model.getGlobalTime().getTime()-initialTime));
		
		long leftOver = bookingsUntilDate - model.getGlobalTime().getTime();
		System.out.println("Time until bookigns run out(1): "+LongDateHelper.deltaAsString(leftOver));
		if(leftOver-initalStepSize*LongDateHelper.DAYINMS <= LongDateHelper.DAYINMS*(flightOffset-1))
			addFlightsAndBookings(flightOffset);
		
		leftOver = bookingsUntilDate - model.getGlobalTime().getTime();
		System.out.println("Time until bookigns run out(2): "+LongDateHelper.deltaAsString(leftOver));
		
		System.out.println("Current number of applied changes: "+changes);
		return flightsLeft;
	}
	
}
