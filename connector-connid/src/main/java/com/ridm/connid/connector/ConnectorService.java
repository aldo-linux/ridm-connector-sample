package com.ridm.connid.connector;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.hyalinedto.api.DTO;
import org.hyalinedto.api.Hyaline;
import org.hyalinedto.exception.HyalineException;
import org.identityconnectors.framework.api.ConnectorInfo;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ObjectClassInfo;
import org.identityconnectors.framework.common.objects.SyncDelta;
import org.identityconnectors.framework.common.objects.SyncResultsHandler;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonProperty;

@RestController
@SpringBootApplication
public class ConnectorService {
    private static final Logger LOG = LoggerFactory.getLogger(BundleManager.class);

	@Autowired
	App dao;
	
	@Autowired
	BundleManager bundleManager;
	
//	@RequestMapping("/connhector/test")
//	String testConnHector() {
//		// App dao = new App();  - THIS IS NO LONGER REQUIRED THANKS TO SPRING!!!!!!
//		String dataConnection = getDataConnection(); // TODO: must get the Data Connection from the Connector Registry ( given an applicationID, return the data connection)
//		System.out.println("json connection: " + dataConnection);
//		Boolean result = Boolean.FALSE;
//		try {
//			result = dao.testConnection(dataConnection);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		return dataConnection;	
//	}

	@RequestMapping("/connector/test")
	Object testConnector() throws HyalineException {
		// App dao = new App();  - THIS IS NO LONGER REQUIRED THANKS TO SPRING!!!!!!
		Map<Object, Object> dataConnection = getDataConnection(); // TODO: must get the Data Connection from the Connector Registry ( given an applicationID, return the data connection)
		/*System.out.println("json connection: " + dataConnection);
		Boolean result = Boolean.FALSE;
		try {
			result = dao.testConnection(dataConnection);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/	
	    // Get properties to use the bundle connector
		// 1. conn = BundleManager.getConnector(dataConnection)
		Connector conn = bundleManager.getConnector(dataConnection);
		// 2. connector.validate()
		conn.test();
		return Hyaline.dtoFromScratch(new DTO(){
			@JsonProperty("dataConnection")
			Object _dataConnection = dataConnection;
		});	
	}
	
	@RequestMapping("/connector/validate")
	Object validateConnector() throws HyalineException {
		Map<Object, Object> dataConnection = getDataConnection(); // TODO: must get the Data Connection from the Connector Registry ( given an applicationID, return the data connection)
		
		Connector conn = bundleManager.getConnector(dataConnection);

		conn.validate();
		
		return Hyaline.dtoFromScratch(new DTO(){
			@JsonProperty("dataConnection")
			Object _dataConnection = dataConnection;
		});	
	}
//	
	@RequestMapping("/connector/get/schema")
	Object getSchemaConnector() throws HyalineException {
		Map<Object, Object> dataConnection = getDataConnection(); // TODO: must get the Data Connection from the Connector Registry ( given an applicationID, return the data connection)
		
		Connector conn = bundleManager.getConnector(dataConnection);

		Map<Object, Object> schema = conn.getObjectClassInfo();
		//schema is null because getObjectClassInfo() return null
		
		return Hyaline.dtoFromScratch(new DTO(){
			@JsonProperty("dataConnection")
			Object _dataConnection = dataConnection;
		});		
	}
	@RequestMapping("/connector/list")
	Object listConnectors( )throws HyalineException  {
		List<ConnectorInfo> result = bundleManager.getAvailableBundles();
		List<String> names = new ArrayList<>();
		for(ConnectorInfo info: result) {
			names.add(info.getConnectorDisplayName());
		}
		return Hyaline.dtoFromScratch(new DTO(){
			@JsonProperty("bundleList")
			Object bundleList = names;
		});
//		return result.toString();	
	}

	private static final String TOPIC = "test-topic";

	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;
//	@RequestMapping("/example")
//	void example( )  {
//			this.kafkaTemplate.send(TOPIC, "Hello from controller");
//			System.out.println("DONE");
//	}

	@RequestMapping("/sync")
	void sync( )  {

		Map<Object, Object> dataConnection = getDataConnection();

		Connector conn = bundleManager.getConnector(dataConnection);

		SyncHandler syncHandler = new SyncHandler(this.kafkaTemplate);
		SyncResultsHandler handler = syncHandler.getSaveKafkaHandler();

		conn.sync(ObjectClass.ACCOUNT, null, handler, null);

	}

	@KafkaListener(topics = TOPIC, groupId = "connector")
	public void listenGroupConnector(ConsumerRecord<?, ?> message) {
		System.out.println("Received Message in group: " + message.value());
	}
	
	
	// REMOVE THIS 
	public Map<Object, Object> getDataConnection() {
		Map<Object, Object> dataConnection = new HashMap<Object, Object>();
		Map<Object, Object> properties = new HashMap<Object, Object>();
			
		// Apache Directory Server
//		properties.put("host", "localhost");
//	    properties.put("port", new Integer(1389));
//	    properties.put("principal", "cn=Directory Manager");
//	    properties.put("credentials", "password");
//	    
//	    properties.put("baseContexts", "ou=Users,dc=example,dc=com");
//		

//		json.put("connectorName", "ldap");
		properties.put("sourcePath", "C:/Users/igniter/Documents/accountsCSV");
	    properties.put("fileMask", "accounts.csv");
	    properties.put("keyColumnNames", "id");
	    properties.put("fields", "id,firstName,lastName");
		
	    dataConnection.put("connectorName", "csv");
	    dataConnection.put("configurationProperties", properties);
		
		  
		return dataConnection;
	} 
}

