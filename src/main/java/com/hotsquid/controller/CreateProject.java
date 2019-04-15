package com.hotsquid.controller;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.bimserver.client.BimServerClient;
import org.bimserver.client.json.JsonBimServerClientFactory;
import org.bimserver.interfaces.objects.SDeserializerPluginConfiguration;
import org.bimserver.interfaces.objects.SProject;
import org.bimserver.plugins.services.Flow;
import org.bimserver.shared.UsernamePasswordAuthenticationInfo;
import org.bimserver.shared.exceptions.BimServerClientException;
import org.bimserver.shared.exceptions.UserException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CreateProject {
	@GetMapping("/BimCreateProject")
	@ResponseBody
	public String createProject() throws BimServerClientException, Exception  {
		 try(JsonBimServerClientFactory factory = new JsonBimServerClientFactory("http://localhost:8082")){
			 BimServerClient client = factory.create(new UsernamePasswordAuthenticationInfo("admin@bimserver.org", "admin"));
			 SProject project = client.getServiceInterface().addProject("20160124OTC-Conference Center2", "ifc2x3tc1");
			 long poid = project.getOid();
			 String comment = "This is a comment";
			 checkInIfc(client, poid, comment);
		 }
		 return "Project check in done...!!";
	        
	}
	
		 private void checkInIfc(BimServerClient client, long poid, String comment)
				 throws org.bimserver.shared.exceptions.ServerException, UserException, IOException {
			 SDeserializerPluginConfiguration deserializer = client.getServiceInterface().getSuggestedDeserializerForExtension("ifc", poid);
			 Path demoIfcFile = Paths.get(System.getProperty("user.dir")+"/20160124OTC-Conference Center.ifc");
			 client.checkin(poid, comment, deserializer.getOid(), false, Flow.SYNC, demoIfcFile);
			 System.out.println("done--------");
		 }
    
}
