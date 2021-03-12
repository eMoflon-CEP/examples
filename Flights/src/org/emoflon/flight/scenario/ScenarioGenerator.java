package org.emoflon.flight.scenario;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.emoflon.flight.model.util.LongDateHelper;

import Flights.Flight;
import Flights.FlightContainer;
import Flights.FlightsFactory;
import Flights.TimeStamp;

public class ScenarioGenerator {
	/**
	 * seed for random event selection
	 */
	private long eventSeed = 42;
	/**
	 * 'chaos'-factor of events
	 */
	private double eventFactor = 1.0;
	/**
	 * seed for random flight selection
	 */
	private long flightSeed = 42;
	/**
	 * seed for events to happen
	 */
	private long chaosSeed = 42;
	/**
	 * chance multiplier for 'chaos' to happen
	 */
	private double chaosFactor = 0.01;
	/**
	 * random for events to happen (quantity)
	 */
	private Random ranChaos;
	/**
	 * random for which event to happen (quality)
	 */
	private Random ranEvent;
	/**
	 * random to pick flight from list
	 */
	private Random ranFlight;

	/**
	 * default constructor with predefined seeds
	 */
	public ScenarioGenerator() {
		ranChaos = new Random(chaosSeed);
		ranEvent = new Random(eventSeed);
		ranFlight = new Random(flightSeed);
	}

	/**
	 * @param eventSeed for random event selection
	 * @param flightSeed for random flight selection
	 * @param chaosSeed for events to happen
	 * @param chaosFactor chance multiplier for events to happen
	 */
	public ScenarioGenerator(long eventSeed, long flightSeed, long chaosSeed, double chaosFactor) {
		this.eventSeed = eventSeed;
		this.flightSeed = flightSeed;
		this.chaosSeed = chaosSeed;
		this.chaosFactor = chaosFactor;
		ranEvent = new Random(eventSeed);
		ranFlight = new Random(flightSeed);
		ranChaos = new Random(chaosSeed);
	}
	
	/**
	 * @param flights list of flights, that should be included in this scenario
	 */
	public boolean runScenario(final Flight flight, double eventProbability) {
		if(ranEvent.nextDouble() <= eventProbability) {
//			ScenarioEvent event = ScenarioEvent.values()[ranEvent
//			        .nextInt((int) (ScenarioEvent.values().length * eventFactor))];
//			runEvent(event, flight);
			continuousDelayEvent(flight);
			return true;
		}
		return false;
	}

	/**
	 * @param flights list of flights, that should be included in this scenario
	 */
	public void runScenario(List<Flight> flights) {
		int chaos = ranChaos.nextInt((int) (flights.size() * chaosFactor));

		for (int i = 0; i < chaos; i++) {
			// TODO handle canceled Flights
			ScenarioEvent event = ScenarioEvent.values()[ranEvent
					.nextInt((int) (ScenarioEvent.values().length * eventFactor))];
			int flightNum = ranFlight.nextInt(flights.size());
			Flight flight = flights.get(flightNum);

			runEvent(event, flight);
		}
	}

	/**
	 * @param flights container, to filter from by arrival in default time-frame
	 * @param currentTime start time from timeFrameSelection
	 */
	public void runScenario(FlightContainer flights, long currentTime) {
		List<Flight> filteredFlights = filterFlights(flights, currentTime);
		runScenario(filteredFlights);
	}

	/**
	 * @param flights container to filter from 
	 * @param currentTime start time for the time-frame
	 * @return a list of flights, which arrival is between currentTime and default time-frame(2 days)
	 */
	private List<Flight> filterFlights(FlightContainer flights, long currentTime) {
		return filterFlights(flights, currentTime, LongDateHelper.DAYINMS * 2);
	}
	
	/**
	 * @param flights container to filter from 
	 * @param currentTime start time for the time-frame
	 * @param timeFrame of arrivals to filter from
	 * @return a list of flights, which arrival is between currentTime and time-frame
	 */
	private List<Flight> filterFlights(FlightContainer flights, long currentTime, long timeFrame) {
		List<Flight> filteredFlights = new ArrayList<Flight>();
		for (Flight flight : flights.getFlights()) {
			if (currentTime <= flight.getArrival().getTime() && flight.getArrival().getTime() <= (currentTime + timeFrame))
				filteredFlights.add(flight);
		}
		return filteredFlights;
	}
	
	private void continuousDelayEvent(Flight flight) {
		final double avgDelay = 100;
		final double deviation = 200;
		long delay = LongDateHelper.getTimeInMs((int)(ranChaos.nextGaussian()*deviation + avgDelay));
		long arrival = flight.getArrival().getTime();
		TimeStamp arrivalStamp = FlightsFactory.eINSTANCE.createTimeStamp();
		arrivalStamp.setTime(arrival + delay);
		flight.setArrival(arrivalStamp);
	}

	/**
	 * @param event to happen
	 * @param flight for which the event should happen
	 */
	private void runEvent(ScenarioEvent event, Flight flight) {
		long arrival = flight.getArrival().getTime();
		TimeStamp arrivalStamp = FlightsFactory.eINSTANCE.createTimeStamp();

		switch (event) {
		case BackWind05HEarly:
			arrivalStamp.setTime(arrival - LongDateHelper.HOURINMS / 2);
			break;
		case MisssingClearens05H:
			arrivalStamp.setTime(arrival + LongDateHelper.HOURINMS / 2);
			break;
		case MisssingClearens1H:
			arrivalStamp.setTime(arrival + LongDateHelper.HOURINMS);
			break;
		case BadWeather1H:
			arrivalStamp.setTime(arrival + LongDateHelper.HOURINMS);
			break;
		case MechanicalIssues1H:
			arrivalStamp.setTime(arrival + LongDateHelper.HOURINMS);
			break;
		case WaitingForCrew1H:
			arrivalStamp.setTime(arrival + LongDateHelper.HOURINMS);
			break;
		case BadWeather2H:
			arrivalStamp.setTime(arrival + LongDateHelper.HOURINMS * 2);
			break;
		case MechanicalIssues2H:
			arrivalStamp.setTime(arrival + LongDateHelper.HOURINMS * 2);
			break;
		case BirdStrike2H:
			arrivalStamp.setTime(arrival + LongDateHelper.HOURINMS * 2);
			break;
		case WaitingForCrew2H:
			arrivalStamp.setTime(arrival + LongDateHelper.HOURINMS * 2);
			break;
		case MechanicalIssues4H:
			arrivalStamp.setTime(arrival + LongDateHelper.HOURINMS * 4);
			break;
		case BadWeather4H:
			arrivalStamp.setTime(arrival + LongDateHelper.HOURINMS * 4);
			break;
		// TODO handle canceled Flights

		default:
			break;
		}
		
		flight.setArrival(arrivalStamp);
	}

}
