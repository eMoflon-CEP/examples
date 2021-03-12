package org.emoflon.flight.scenario;

/**
 * events that could happen to a flight, scaled from smallest to biggest impact
 *
 */
public enum ScenarioEvent {
	BackWind05HEarly,
	MisssingClearens05H,
	MisssingClearens1H,
	BadWeather1H,
	MechanicalIssues1H,
	WaitingForCrew1H,
	BadWeather2H,
	MechanicalIssues2H,
	BirdStrike2H,
	WaitingForCrew2H,
	MechanicalIssues4H,
	BadWeather4H,
//  no support for flight cancelation atm
//	MechanicalIssuesCanceled,
//	LackOfCrewCanceled
}
