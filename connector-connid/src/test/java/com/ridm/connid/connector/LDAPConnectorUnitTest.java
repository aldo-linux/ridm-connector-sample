package com.ridm.connid.connector;

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

import org.identityconnectors.framework.common.objects.*;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LDAPConnectorUnitTest {
	BundleManager bundleManager = new BundleManager();
	
	public Map<Object, Object> getDataConnection() {
		Map<Object, Object> dataConnection = new HashMap<>();
		Map<Object, Object> properties = new HashMap<>();
			
		// Apache Directory Server
		properties.put("host", "localhost");
	    properties.put("port", Integer.valueOf(1389));
	    properties.put("principal", "cn=Directory Manager");
//	    properties.put("principal", "cn=admin,dc=example,dc=com");
	    properties.put("credentials", "password");
	    
	    properties.put("baseContexts", "ou=Users,dc=example,dc=com");
		
	    dataConnection.put("connectorName", "ldap");
	    dataConnection.put("configurationProperties", properties);
		
		return dataConnection;
	} 
	
	// Test connection
	@Test
	public void test01TestConnectionLDAPAccount() {
		
		Map<Object, Object> dataConnection = getDataConnection();
		Connector conn = bundleManager.getConnector(dataConnection);
		boolean result = conn.test();
		assertTrue(result);
	}
	@Test
	public void test02GetSchemaLDAPAccount() {
		
		Map<Object, Object> dataConnection = getDataConnection();
		Connector conn = bundleManager.getConnector(dataConnection);
		Map<Object, Object> schema = conn.getObjectClassInfo();
		assertTrue(schema.size() > 0);
	}
	
	@Test
	public void test03CreateLDAPAccount() {
		
		Map<Object, Object> dataConnection = getDataConnection();
		Connector conn = bundleManager.getConnector(dataConnection);
		AtomicReference<Boolean> propagationAttempted = new AtomicReference<>();
		
		Set<Attribute> attributes = new HashSet<>();
	    UUID uuid = UUID.randomUUID();
//		attributes.add(AttributeBuilder.build("cn", "Raul"));
		attributes.add(AttributeBuilder.build("description", "Example description"));
		attributes.add(AttributeBuilder.build("givenName", "Raul"));
		attributes.add(AttributeBuilder.build("mail", "raulcaceres@mail.com"));
		attributes.add(AttributeBuilder.build("title", "Boss"));
		attributes.add(AttributeBuilder.build("uid", uuid.toString()));
		attributes.add(AttributeBuilder.build("sn", "Worker"));
		attributes.add(AttributeBuilder.build("userPassword", "hello123"));
		attributes.add(AttributeBuilder.build("__NAME__", "cn=Raul Caceres,ou=Users,dc=example,dc=com"));
	    
		Uid uid = conn.create(ObjectClass.ACCOUNT, attributes, null, propagationAttempted);
		
		System.out.println("uid test: "+uid.getName());
	    
	    assertEquals("__UID__", uid.getName());
	}
	
	@Test
	public void test04SyncLDAPAccount() {
		
		Map<Object, Object> dataConnection = getDataConnection();
		Connector conn = bundleManager.getConnector(dataConnection);

		SyncToken token = null;
//		token = conn.getLatestSyncToken(ObjectClass.ACCOUNT);
		
		final List<SyncDelta> results = new ArrayList<>();
	    SyncResultsHandler handler = new SyncResultsHandler() {
	        public boolean handle(SyncDelta delta) {
	            results.add(delta);
	            return true;
	        } 
	    };
		
		conn.sync(ObjectClass.ACCOUNT, token, handler, null);
		assertTrue(results.size() > 0);
	}
	
	@Test
	public void test05ReadLDAPAccount() {
		
		Map<Object, Object> dataConnection = getDataConnection();
		Connector conn = bundleManager.getConnector(dataConnection);
		
		Attribute accountToRead = AttributeBuilder.build("mail", "raulcaceres@mail.com");
		    
		ConnectorObject response = conn.getObject(ObjectClass.ACCOUNT, accountToRead, false, null);
		assertEquals(response.getAttributeByName("description").getValue().get(0), "Example description");
		assertEquals(response.getAttributeByName("givenName").getValue().get(0), "Raul");
	 }
	
	 @Test
	 public void test06UpdateLDAPAccount() {
		 
		Map<Object, Object> dataConnection = getDataConnection();
		Connector conn = bundleManager.getConnector(dataConnection);
		AtomicReference<Boolean> propagationAttempted = new AtomicReference<>();
		
		Set<Attribute> attrs = new HashSet<>();
		attrs.add(AttributeBuilder.build( "givenName", "Raul edited" ));
		
		// Get unique identifier
		Attribute accountToUpdate = AttributeBuilder.build("mail", "raulcaceres@mail.com");
		ConnectorObject response = conn.getObject(ObjectClass.ACCOUNT, accountToUpdate, false, null);
		 
		Uid uid = response.getUid();
		
		Uid accountUpdated = conn.update(ObjectClass.ACCOUNT, uid, attrs, null, propagationAttempted);
			    
	    assertEquals("__UID__", accountUpdated.getName());
		
	 }
//	@Test
	public void testGellAllAccounts() {
		
//		Map<Object, Object> account = new HashMap<Object, Object>();
//		account.put("__NAME__", "ou=Users,dc=example,dc=com");
//		    
//		String uid = JSONValue.toJSONString(account); 
//			  
//		App dao = new App();
//		
//		dao.readAccount(getDataConnection(), uid);
	 }	
	 
	 @Test
	 public void test07DeleteLDAPAccount() {
		 
		Map<Object, Object> dataConnection = getDataConnection();
		Connector conn = bundleManager.getConnector(dataConnection);
		AtomicReference<Boolean> propagationAttempted = new AtomicReference<>();
		Attribute account = AttributeBuilder.build("mail", "raulcaceres@mail.com");
		ConnectorObject response = conn.getObject(ObjectClass.ACCOUNT, account, false, null);
		
		Uid uid = response.getUid();

		boolean result = conn.delete(ObjectClass.ACCOUNT, uid, null, propagationAttempted);
		assertTrue(result);
		
	 }

	public Map<Object, Object> getDataConnectionGroup() {
		Map<Object, Object> dataConnection = new HashMap<>();
		Map<Object, Object> properties = new HashMap<>();

		// Apache Directory Server
		properties.put("host", "localhost");
		properties.put("port", Integer.valueOf(1389));
		properties.put("principal", "cn=Directory Manager");
//		properties.put("principal", "cn=admin,dc=example,dc=com");
		properties.put("credentials", "password");

		properties.put("baseContexts", "ou=Groups,dc=example,dc=com");

		dataConnection.put("connectorName", "ldap");
		dataConnection.put("configurationProperties", properties);

		return dataConnection;
	}

	// Test connection
	@Test
	public void test08TestConnectionLDAPGroup() {

		Map<Object, Object> dataConnection = getDataConnectionGroup();
		Connector conn = bundleManager.getConnector(dataConnection);
		boolean result = conn.test();
		assertTrue(result);
	}
	@Test
	public void test09GetSchemaLDAPGroup() {

		Map<Object, Object> dataConnection = getDataConnectionGroup();
		Connector conn = bundleManager.getConnector(dataConnection);
		Map<Object, Object> schema = conn.getObjectClassInfo();
		assertTrue(schema.size() > 0);
	}

	@Test
	public void test10CreateLDAPGroup() {

		Map<Object, Object> dataConnection = getDataConnectionGroup();
		Connector conn = bundleManager.getConnector(dataConnection);
		AtomicReference<Boolean> propagationAttempted = new AtomicReference<>();

		Set<Attribute> attributes = new HashSet<>();
		UUID uuid = UUID.randomUUID();
		attributes.add(AttributeBuilder.build("description", "This is a example of a group"));
//		attributes.add(AttributeBuilder.build("seeAlso", "Check Paynalli website"));
		attributes.add(AttributeBuilder.build("__NAME__", "cn=demoGroup,ou=Groups,dc=example,dc=com"));

		Uid uid = conn.create(ObjectClass.GROUP, attributes, null, propagationAttempted);

		System.out.println("uid test: "+uid.getName());

		assertEquals("__UID__", uid.getName());
	}

	@Test
	public void test11SyncLDAPGroup() {

		Map<Object, Object> dataConnection = getDataConnectionGroup();
		Connector conn = bundleManager.getConnector(dataConnection);

		SyncToken token = null;
//		token = conn.getLatestSyncToken(ObjectClass.ACCOUNT);

		final List<SyncDelta> results = new ArrayList<>();
		SyncResultsHandler handler = new SyncResultsHandler() {
			public boolean handle(SyncDelta delta) {
				results.add(delta);
				return true;
			}
		};

		conn.sync(ObjectClass.GROUP, token, handler, null);
		assertTrue(results.size() > 0);
	}

	@Test
	public void test12ReadLDAPGroup() {

		Map<Object, Object> dataConnection = getDataConnectionGroup();
		Connector conn = bundleManager.getConnector(dataConnection);

		Attribute accountToRead = AttributeBuilder.build("__NAME__", "cn=demoGroup,ou=Groups,dc=example,dc=com");

		ConnectorObject response = conn.getObject(ObjectClass.GROUP, accountToRead, false, null);
		assertEquals(response.getAttributeByName("description").getValue().get(0), "This is a example of a group");
	}

	@Test
	public void test13UpdateLDAPGroup() {

		Map<Object, Object> dataConnection = getDataConnectionGroup();
		Connector conn = bundleManager.getConnector(dataConnection);
		AtomicReference<Boolean> propagationAttempted = new AtomicReference<>();

		Set<Attribute> attrs = new HashSet<>();
		attrs.add(AttributeBuilder.build( "description", "This is a example of a group edited" ));

		// Get unique identifier
		Attribute accountToUpdate = AttributeBuilder.build("__NAME__", "cn=demoGroup,ou=Groups,dc=example,dc=com");
		ConnectorObject response = conn.getObject(ObjectClass.GROUP, accountToUpdate, false, null);

		Uid uid = response.getUid();

		Uid accountUpdated = conn.update(ObjectClass.GROUP, uid, attrs, null, propagationAttempted);

		assertEquals("__UID__", accountUpdated.getName());

	}

	@Test
	public void test14DeleteLDAPGroup() {

		Map<Object, Object> dataConnection = getDataConnectionGroup();
		Connector conn = bundleManager.getConnector(dataConnection);
		AtomicReference<Boolean> propagationAttempted = new AtomicReference<>();
		Attribute account = AttributeBuilder.build("__NAME__", "cn=demoGroup,ou=Groups,dc=example,dc=com");
		ConnectorObject response = conn.getObject(ObjectClass.GROUP, account, false, null);

		Uid uid = response.getUid();

		boolean result = conn.delete(ObjectClass.GROUP, uid, null, propagationAttempted);
		assertTrue(result);

	}

}
