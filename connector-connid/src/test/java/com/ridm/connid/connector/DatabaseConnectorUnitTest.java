package com.ridm.connid.connector;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.SyncDelta;
import org.identityconnectors.framework.common.objects.SyncResultsHandler;
import org.identityconnectors.framework.common.objects.Uid;

/**
 * Unit test for TestDatabase.
 */
//DatabaseConnectorUnitTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DatabaseConnectorUnitTest {
	BundleManager bundleManager = new BundleManager();
	
	public Map<Object, Object> getDataConnection() {
		Map<Object, Object> dataConnection = new HashMap<>();
		Map<String, String> properties = new HashMap<>();
		
		// PostgreSQL
//		properties.put("jdbcDriver", "org.postgresql.Driver");
//	    properties.put("jdbcUrlTemplate", "jdbc:postgresql://%h:%p/%d");
//	    properties.put("host", "localhost");
//	    properties.put("port", "5400");
//	    properties.put("database", "dockerdb");
//	    properties.put("user", "docker");
//	    properties.put("password", "1234");
//	    properties.put("table", "demo");
//	    properties.put("keyColumn", "id");
	    
		
		// MySQL
		properties.put("jdbcDriver", "com.mysql.cj.jdbc.Driver");
		properties.put("jdbcUrlTemplate", "jdbc:mysql://%h:%p/%d");
		properties.put("host", "localhost");
		properties.put("port", "3306");
		properties.put("database", "connhector");
		properties.put("user", "connuser");
		properties.put("password", "connpasswd");
		properties.put("table", "example");
		properties.put("keyColumn", "id");
		properties.put("changeLogColumn", "created_at");
		
		dataConnection.put("connectorName", "database");
		dataConnection.put("configurationProperties", properties);
		
		System.out.println("JSON: " + dataConnection);
		return dataConnection;
	} 
	
	@Test
	public void test1TestConnectionDatabaseAccount() {
		
		Map<Object, Object> dataConnection = getDataConnection();
		Connector conn = bundleManager.getConnector(dataConnection);
		boolean result = conn.test();
		assertTrue(result);
	}
	@Test
	public void test2GetSchemaDatabaseAccount() {
		
		Map<Object, Object> dataConnection = getDataConnection();
		Connector conn = bundleManager.getConnector(dataConnection);
		Map<Object, Object> schema = conn.getObjectClassInfo();
		assertTrue(schema.size() > 0);
	}
	 
	@Test
	public void test3CreateDatabaseAccount() {
		
		Map<Object, Object> dataConnection = getDataConnection();
		Connector conn = bundleManager.getConnector(dataConnection);
		AtomicReference<Boolean> propagationAttempted = new AtomicReference<>();
		
		
		Set<Attribute> attributes = new HashSet<>();
	    UUID uuid = UUID.randomUUID();
	    attributes.add(AttributeBuilder.build("displayname", "Raul Caceres"));
	    attributes.add(AttributeBuilder.build("firstname", "Raul"));
	    attributes.add(AttributeBuilder.build("__NAME__", uuid.toString()));
	    
	    Uid uid = conn.create(ObjectClass.ACCOUNT, attributes, null, propagationAttempted);
		
		System.out.println("uid test: "+uid.getName());
	    
	    assertEquals("__UID__", uid.getName());
	}
	
	@Test
	public void test4SyncDatabaseAccount() {
		
		Map<Object, Object> dataConnection = getDataConnection();
		Connector conn = bundleManager.getConnector(dataConnection);
		
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
	public void test5ReadDatabaseAccount() {
		
		Map<Object, Object> dataConnection = getDataConnection();
		Connector conn = bundleManager.getConnector(dataConnection);
		
		Attribute accountToRead = AttributeBuilder.build("firstname", "Raul");
		    
		ConnectorObject response = conn.getObject(ObjectClass.ACCOUNT, accountToRead, false, null);
		assertEquals(response.getAttributeByName("firstname").getValue().get(0), "Raul");
	}
	
	@Test
	public void test6UpdateDatabaseAccount() {
		
		Map<Object, Object> dataConnection = getDataConnection();
		Connector conn = bundleManager.getConnector(dataConnection);
		AtomicReference<Boolean> propagationAttempted = new AtomicReference<>();
		
		Set<Attribute> attrs = new HashSet<>();
		attrs.add(AttributeBuilder.build( "displayName", "Raul Caceres edited" ));
		attrs.add(AttributeBuilder.build( "firstname", "Raul" ));
		
		Attribute accountToUpdate = AttributeBuilder.build( "firstname", "Raul" );
		ConnectorObject response = conn.getObject(ObjectClass.ACCOUNT, accountToUpdate, false, null);
		 
		Uid uid = response.getUid();
		
		Uid accountUpdated = conn.update(ObjectClass.ACCOUNT, uid, attrs, null, propagationAttempted);
	    
	    assertEquals("__UID__", accountUpdated.getName());
	}

	 @Test
	 public void test7DeleteDatabaseAccount() {
		
		Map<Object, Object> dataConnection = getDataConnection();
		Connector conn = bundleManager.getConnector(dataConnection);
		AtomicReference<Boolean> propagationAttempted = new AtomicReference<>();
		Attribute account = AttributeBuilder.build( "firstname", "Raul" );
		
		ConnectorObject response = conn.getObject(ObjectClass.ACCOUNT, account, false, null);
		
		Uid uid = response.getUid();

		boolean result = conn.delete(ObjectClass.ACCOUNT, uid, null, propagationAttempted);
		assertTrue(result);
	 }
	 public static void main(String[] args) {
		
		
	}


}
