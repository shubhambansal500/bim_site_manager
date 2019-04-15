package com.hotsquid.controller;

import java.util.ArrayList;
import java.util.List;

import org.bimserver.client.BimServerClient;
import org.bimserver.client.json.JsonBimServerClientFactory;
import org.bimserver.emf.IfcModelInterface;
import org.bimserver.interfaces.objects.SProject;
import org.bimserver.models.ifc2x3tc1.IfcBuildingStorey;
import org.bimserver.models.ifc2x3tc1.IfcDistributionControlElement;
import org.bimserver.models.ifc2x3tc1.IfcObject;
import org.bimserver.models.ifc2x3tc1.IfcRelContainedInSpatialStructure;
import org.bimserver.shared.ChannelConnectionException;
import org.bimserver.shared.UsernamePasswordAuthenticationInfo;
import org.bimserver.shared.exceptions.BimServerClientException;
import org.bimserver.shared.exceptions.ServerException;
import org.bimserver.shared.exceptions.ServiceException;
import org.bimserver.shared.exceptions.UserException;
import org.bimserver.shared.interfaces.ServiceInterface;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Connecting {
	List<String> names = new ArrayList<String>();
	@RequestMapping(value = "/getAllDetectors", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public List<String> bimConnection() {
		try {
			BimServerClient client = isBimServerConnected();
			List<SProject> projects = getAllSProjects(client);
			names = extractIfcObject(client, projects);
		} catch (BimServerClientException e) {
			e.printStackTrace();
		} catch (ServiceException e) {
			e.printStackTrace();
		} catch (ChannelConnectionException e) {
			e.printStackTrace();
		}
		return names;
	}

	private static BimServerClient isBimServerConnected()
			throws BimServerClientException, ServiceException, ChannelConnectionException {
		JsonBimServerClientFactory factory = new JsonBimServerClientFactory("http://localhost:8082");
		BimServerClient client = factory.create(new UsernamePasswordAuthenticationInfo("admin@bimserver.org", "admin"));
		if (client.isConnected()) {
			System.out.println("Connected");
		}
		return client;
	}

	private static List<SProject> getAllSProjects(BimServerClient client) throws ServerException, UserException {
		ServiceInterface serviceInterface = client.getServiceInterface();
		List<SProject> projects = serviceInterface.getAllWritableProjects();
		return projects;
	}

	private static List<String> extractIfcObject(BimServerClient client, List<SProject> projects)
			throws UserException, ServerException {
		List<String> detectorNames = new ArrayList<String>();
		for (SProject project : projects) {
			if (project.getName().equals("WestRiverSideHospitalFireAlarm_1.ifc")) {
				IfcModelInterface model = client.getModel(project, project.getLastRevisionId(), false, true, true);
				List<IfcBuildingStorey> levels = new ArrayList<IfcBuildingStorey>();
				levels.addAll(model.getAllWithSubTypes(IfcBuildingStorey.class));
				List<IfcRelContainedInSpatialStructure> spatialStructure = new ArrayList<IfcRelContainedInSpatialStructure>();
				extractLevel(levels, spatialStructure);
				List<IfcObject> relatedElements = new ArrayList<IfcObject>();
				extractRelatedElements(spatialStructure, relatedElements);
				System.out.println("All Devices");
				extractDetectors(detectorNames, relatedElements);
			}
		}
		return detectorNames;
	}

	private static void extractLevel(List<IfcBuildingStorey> allLevels,
			List<IfcRelContainedInSpatialStructure> spatialStructure) {
		for (IfcBuildingStorey level : allLevels) {
			if (level.getName().equals("Level 5")) {
				System.out.println(level.getName());
				spatialStructure.addAll(level.getContainsElements());
			}
		}
	}

	private static void extractRelatedElements(List<IfcRelContainedInSpatialStructure> spatialStructure,
			List<IfcObject> relatedElements) {
		for (IfcRelContainedInSpatialStructure spl : spatialStructure) {
			relatedElements.addAll(spl.getRelatedElements());
		}
	}

	private static void extractDetectors(List<String> detectorNames, List<IfcObject> relatedElements) {
		for (IfcObject detectors : relatedElements) {
			if (detectors instanceof IfcDistributionControlElement) {
				System.out.println(detectors.getName());
				detectorNames.add(detectors.getName());
			}
		}
	}
}
