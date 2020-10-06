package com.ridm.connid.connector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.SyncDelta;
import org.identityconnectors.framework.common.objects.SyncResultsHandler;
import org.identityconnectors.framework.common.objects.Uid;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.kafka.core.KafkaTemplate;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CSVConnectorUnitTest {
	BundleManager bundleManager = new BundleManager();
	
	public Map<Object, Object> getDataConnection() {
		Map<Object, Object> dataConnection = new HashMap<>();
		Map<Object, Object> properties = new HashMap<>();
			
		// CSV
	    properties.put("sourcePath", "C:/Users/igniter/Documents/accountsCSV");
	    properties.put("fileMask", "accounts.csv");
	    properties.put("keyColumnNames", "id");
	    properties.put("fields", "id,firstName,lastName");
		
	    dataConnection.put("connectorName", "csv");
	    dataConnection.put("configurationProperties", properties);
		
		return dataConnection;
	} 
	
	// Test connection
	@Test
	public void test1TestConnectionAccount() {
		
		Map<Object, Object> dataConnection = getDataConnection();
		Connector conn = bundleManager.getConnector(dataConnection);
		boolean result = conn.test();
		assertTrue(result);
	}
	@Test
	public void test2GetSchemaAccount() {
		
		Map<Object, Object> dataConnection = getDataConnection();
		Connector conn = bundleManager.getConnector(dataConnection);
		Map<Object, Object> schema = conn.getObjectClassInfo();
		assertTrue(schema.size() > 0);
	}
	
	@Test
	public void test3CreateAccount() {
		
		Map<Object, Object> dataConnection = getDataConnection();
		Connector conn = bundleManager.getConnector(dataConnection);
		AtomicReference<Boolean> propagationAttempted = new AtomicReference<>();
	    
		Set<Attribute> attributes = new HashSet<>();
	    UUID uuid = UUID.randomUUID();
	    attributes.add(AttributeBuilder.build("firstName", "Raul"));
	    attributes.add(AttributeBuilder.build("lastName", "Caceres"));
	    attributes.add(AttributeBuilder.build("__NAME__", uuid.toString()));
	    
		Uid uid = conn.create(ObjectClass.ACCOUNT, attributes, null, propagationAttempted);
		
		System.out.println("uid test: "+uid.getName());
	    
	    assertEquals("__UID__", uid.getName());
	}
	
	@Test
	public void test4SyncAccount() {
		
		Map<Object, Object> dataConnection = getDataConnection();
		Connector conn = bundleManager.getConnector(dataConnection);

//		SyncHandler syncHandler = new SyncHandler(KafkaTemplate kafkaTemplate);
//		SyncResultsHandler handler = syncHandler.getSaveKafkaHandler();
//		final List<SyncDelta> results = syncHandler.getResults();

		final List<SyncDelta> results = new ArrayList<>();
	    SyncResultsHandler handler = new SyncResultsHandler() {
	        public boolean handle(SyncDelta delta) {
	            results.add(delta);
	            return true;
	        }
	    };
		
		conn.sync(ObjectClass.ACCOUNT, null, handler, null);
		assertTrue(results.size() > 0);
	}
	
	@Test
	public void test5ReadAccount() {
		
		Map<Object, Object> dataConnection = getDataConnection();
		Connector conn = bundleManager.getConnector(dataConnection);
		
	    Attribute accountToRead = AttributeBuilder.build( "firstName", "Raul" );
		
	    ConnectorObject response = conn.getObject(ObjectClass.ACCOUNT, accountToRead, false, null);
		assertEquals(response.getAttributeByName("FIRSTNAME").getValue().get(0), "Raul");
		assertEquals(response.getAttributeByName("LASTNAME").getValue().get(0), "Caceres");
	 }
	
	 @Test
	 public void test6UpdateAccount() {
		 
		Map<Object, Object> dataConnection = getDataConnection();
		Connector conn = bundleManager.getConnector(dataConnection);
		AtomicReference<Boolean> propagationAttempted = new AtomicReference<>();
		
		Set<Attribute> attrs = new HashSet<>();
		attrs.add(AttributeBuilder.build( "lastName", "Caceres edited" ));
		
		// Get unique identifier
		Attribute accountToUpdate = AttributeBuilder.build( "firstName", "Raul" );
		ConnectorObject response = conn.getObject(ObjectClass.ACCOUNT, accountToUpdate, false, null);
		 
		Uid uid = response.getUid();
	
		Uid accountUpdated = conn.update(ObjectClass.ACCOUNT, uid, attrs, null, propagationAttempted);
	    
	    assertEquals("__UID__", accountUpdated.getName());
	 }
	 
	 @Test
	 public void test7DeleteAccount() {
		 
		Map<Object, Object> dataConnection = getDataConnection();
		Connector conn = bundleManager.getConnector(dataConnection);
		AtomicReference<Boolean> propagationAttempted = new AtomicReference<>();
		Attribute account = AttributeBuilder.build( "firstName", "Raul" );
		ConnectorObject response = conn.getObject(ObjectClass.ACCOUNT, account, false, null);
		
		Uid uid = response.getUid();
		
		boolean result = conn.delete(ObjectClass.ACCOUNT, uid, null, propagationAttempted);
		assertTrue(result);
	 }

}
