package com.ridm.connid.connector;

import org.junit.FixMethodOrder;

//import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.HashMap;
import java.util.HashSet;

import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.Uid;

/**
 * Unit test for TestServiceNow.
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ServiceNowConnectorUnitTest {
	BundleManager bundleManager = new BundleManager();
	
	public Map<Object, Object> getDataConnection() {
		Map<Object, Object> dataConnection = new HashMap<>();
		Map<Object, Object> properties = new HashMap<>();
			
		// Service Now
	    properties.put("baseAddress", "https://dev95318.service-now.com");
	    properties.put("username", "admin");
	    properties.put("password", "Hola$123");
		
	    dataConnection.put("connectorName", "serviceNow");
	    dataConnection.put("configurationProperties", properties);
		
		return dataConnection;
	} 
	
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
		attributes.add(AttributeBuilder.build("user_password", "123456"));
		attributes.add(AttributeBuilder.build("email", "alex@mail.com"));
		attributes.add(AttributeBuilder.build("first_name", "alex"));
		attributes.add(AttributeBuilder.build("last_name", "perez"));
		attributes.add(AttributeBuilder.build("user_name", "123456"));
		attributes.add(AttributeBuilder.build("__NAME__", uuid.toString()));
		

		Uid uid = conn.create(ObjectClass.ACCOUNT, attributes, null, propagationAttempted);
		
		System.out.println("uid test: "+uid.getName());
	    
	    assertEquals("__UID__", uid.getName());
	}
	
	
	@Test
	public void test5ReadAccount() {
		
		Map<Object, Object> dataConnection = getDataConnection();
		Connector conn = bundleManager.getConnector(dataConnection);
		
		Attribute accountToRead = AttributeBuilder.build("__NAME__", "123456");
		    
		ConnectorObject response = conn.getObject(ObjectClass.ACCOUNT, accountToRead, false, null);
		assertEquals(response.getAttributeByName("__NAME__").getValue().get(0), "123456");
	 }
	
	 @Test
	 public void test6UpdateAccount() {
		 
		Map<Object, Object> dataConnection = getDataConnection();
		Connector conn = bundleManager.getConnector(dataConnection);
		AtomicReference<Boolean> propagationAttempted = new AtomicReference<>();
		
		Set<Attribute> attrs = new HashSet<>();
		
		attrs.add(AttributeBuilder.build("email", "aleedited@mail.com"));
		attrs.add(AttributeBuilder.build("first_name", "alex edited"));
		attrs.add(AttributeBuilder.build("last_name", "perez edited"));
		
		// Get unique identifier
		Attribute accountToUpdate = AttributeBuilder.build( "__NAME__", "123456" );
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
		Attribute account = AttributeBuilder.build( "__NAME__", "123456" );
		ConnectorObject response = conn.getObject(ObjectClass.ACCOUNT, account, false, null);
		
		Uid uid = response.getUid();

		boolean result = conn.delete(ObjectClass.ACCOUNT, uid, null, propagationAttempted);
		assertTrue(result);
		
	 }
	 public static void main(String[] args) {
		
		
	}

}
