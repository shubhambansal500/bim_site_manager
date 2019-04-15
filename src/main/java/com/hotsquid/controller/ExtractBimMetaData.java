package com.hotsquid.controller;

import java.util.ArrayList;
import java.util.List;

import org.bimserver.client.BimServerClient;
import org.bimserver.client.json.JsonBimServerClientFactory;
import org.bimserver.emf.IfcModelInterface;
import org.bimserver.interfaces.objects.SProject;
import org.bimserver.models.ifc2x3tc1.IfcBuildingStorey;
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
public class ExtractBimMetaData {
	List<String> names = new ArrayList<String>();
	@RequestMapping(value = "/getAllLevels", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public List<String> allevels() {
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
		List<String> finalArea = new ArrayList<String>();
		List<IfcBuildingStorey> levels = new ArrayList<IfcBuildingStorey>();
		for (SProject project : projects) {
			if (project.getName().equals("20160124OTC-Conference Center2")) {
				IfcModelInterface model = client.getModel(project, project.getLastRevisionId(), false, true, true);
				levels.addAll(model.getAllWithSubTypes(IfcBuildingStorey.class));
				List<IfcRelContainedInSpatialStructure> spatialStructure = new ArrayList<IfcRelContainedInSpatialStructure>();
				finalArea = extractLevel(levels, spatialStructure);
			}
		}
		return finalArea;
	}
	
	private static List<String> extractLevel(List<IfcBuildingStorey> allLevels,
			List<IfcRelContainedInSpatialStructure> spatialStructure) {
		List<String> area = new ArrayList<String>();
		for (IfcBuildingStorey level : allLevels) {
				System.out.println(level.getName());
				spatialStructure.addAll(level.getContainsElements());
				area.add(level.getName());
			}
		return area;
	}

}
