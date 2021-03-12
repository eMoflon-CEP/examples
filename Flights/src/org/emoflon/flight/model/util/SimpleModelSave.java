package org.emoflon.flight.model.util;

import java.io.IOException;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.emoflon.flight.model.generator.EvaluationModelGenerator;
import org.emoflon.flight.model.generator.continuousStream.ContinuousBookingGenerator;
import org.emoflon.flight.model.generator.continuousStream.ContinuousFlightGenerator;

import Flights.FlightModel;

public class SimpleModelSave {
	public final static int flightOffset = 4;
	
	
	public static FlightModel generateFlightModel(String definitionFolder, int size, long startDate) {
		ContinuousBookingGenerator bookingGenerator;
		ContinuousFlightGenerator flightGenerator;
		
		EvaluationModelGenerator gen = new EvaluationModelGenerator();
		FlightModel model = gen.generateSimpleModel(definitionFolder);
		
		flightGenerator = new ContinuousFlightGenerator(definitionFolder+"/simple.flightgen", model.getRoutes(), model.getPlanes());
		bookingGenerator = new ContinuousBookingGenerator(definitionFolder+"/simple.bookingcflightgen", definitionFolder+"/simple.bookingncflightgen", model.getPersons());
		
		model.getFlights().getFlights().addAll(flightGenerator.createContinuousFlights(size+flightOffset, startDate));
		model.getBookings().getBookings().addAll(bookingGenerator.createContiniousBookings(size, startDate, model.getFlights()));
		
		
		return model;
	}
	
	public static void main(String[] args) {
		
		String definitionFolder = "src\\org\\emoflon\\flight\\model\\definitions\\";
		URI uri = URI.createURI("instances/test_1.xmi");

		long startDate = LongDateHelper.getDate(01, 01, 2021);
		int amountDays = 1;
		
		
		FlightModel model = generateFlightModel(definitionFolder, amountDays, startDate);
		
		ResourceSet rs = new ResourceSetImpl();
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap()
		.put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());
		XMIResource output = (XMIResource) rs.createResource(uri);
		
		output.getContents().add(model);
		EcoreUtil.resolveAll(output);
		
		Map<Object, Object> saveOptions = output.getDefaultSaveOptions();
		saveOptions.put(XMIResource.OPTION_ENCODING, "UTF-8");
		saveOptions.put(XMIResource.OPTION_USE_XMI_TYPE,Boolean.TRUE);
		saveOptions.put(XMIResource.OPTION_SAVE_TYPE_INFORMATION, Boolean.TRUE);
		saveOptions.put(XMIResource.OPTION_SCHEMA_LOCATION_IMPLEMENTATION, Boolean.TRUE);
		
		try {
			output.save(saveOptions);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
