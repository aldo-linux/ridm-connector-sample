package com.ridm.connid.connector;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import org.identityconnectors.framework.common.objects.*;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ServiceNowRESTConnectorTest {

	BundleManager bundleManager = new BundleManager();

	public Map<Object, Object> getDataConnection() {
		String BASE_ADDRESS = "C:/scriptsGroovy/src/main/java/com/rest/connector/";
		Map<Object, Object> dataConnection = new HashMap<>();
		Map<Object, Object> properties = new HashMap<>();

		properties.put("baseAddress", "https://dev95318.service-now.com/");
//		properties.put("accept", "");
//		properties.put("contentType", "");
		properties.put("username", "admin");
		properties.put("password", "Hola$123");
		properties.put("testScriptFileName", BASE_ADDRESS + "test.groovy");
		properties.put("schemaScriptFileName", BASE_ADDRESS + "schema.groovy");
		properties.put("createScriptFileName", BASE_ADDRESS + "create.groovy");
		properties.put("searchScriptFileName", BASE_ADDRESS + "read.groovy");
		properties.put("syncScriptFileName", BASE_ADDRESS + "sync.groovy");
		properties.put("updateScriptFileName", BASE_ADDRESS + "update.groovy");
		properties.put("deleteScriptFileName", BASE_ADDRESS + "delete.groovy");

		dataConnection.put("connectorName", "rest");
		dataConnection.put("configurationProperties", properties);

		return dataConnection;
	} 
	
	@Test
	public void test1TestConnectionAccount() {

		Map<Object, Object> dataConnection = getDataConnection();
		Connector conn = bundleManager.getConnector(dataConnection);
		boolean result = conn.test();
		Assert.assertTrue(result);
	}
	@Test
	public void test2GetSchemaAccount() {

		Map<Object, Object> dataConnection = getDataConnection();
		Connector conn = bundleManager.getConnector(dataConnection);
		Map<Object, Object> schema = conn.getObjectClassInfo();
		Assert.assertTrue(schema.size() > 0);
	}

	@Test
	public void test3CreateAccount() {

		Map<Object, Object> dataConnection = getDataConnection();
		Connector conn = bundleManager.getConnector(dataConnection);
		AtomicReference<Boolean> propagationAttempted = new AtomicReference<>();

		Set<Attribute> attributes = new HashSet<>();
		UUID uuid = UUID.randomUUID();
		attributes.add(AttributeBuilder.build("state", "1"));
		attributes.add(AttributeBuilder.build("short_description", "description from REST connector"));
		attributes.add(AttributeBuilder.build("active", "true"));
		attributes.add(AttributeBuilder.build("__NAME__", uuid.toString()));

		Uid uid = conn.create(ObjectClass.ACCOUNT, attributes, null, propagationAttempted);

		System.out.println("uid test: "+uid.getName());

		Assert.assertEquals("__UID__", uid.getName());


	}

	@Test
	public void test4SyncAccount() {

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
		Assert.assertTrue(results.size() > 0);
	}

	@Test
	public void test5ReadAccount() {

		Map<Object, Object> dataConnection = getDataConnection();
		Connector conn = bundleManager.getConnector(dataConnection);

		Attribute accountToRead = AttributeBuilder.build("short_description", "description from REST connector");

		ConnectorObject response = conn.getObject(ObjectClass.ACCOUNT, accountToRead, false, null);
		Assert.assertEquals(response.getAttributeByName("active").getValue().get(0), "true");
	 }

	 @Test
	 public void test6UpdateAccount() {


		 Map<Object, Object> dataConnection = getDataConnection();
		 Connector conn = bundleManager.getConnector(dataConnection);
		 AtomicReference<Boolean> propagationAttempted = new AtomicReference<>();

		 Set<Attribute> attrs = new HashSet<>();
		 attrs.add(AttributeBuilder.build( "state", "2" ));

		 // Get unique identifier
		 Attribute accountToUpdate = AttributeBuilder.build( "short_description", "description from REST connector" );
		 ConnectorObject response = conn.getObject(ObjectClass.ACCOUNT, accountToUpdate, false, null);

		 Uid uid = response.getUid();

		 Uid accountUpdated = conn.update(ObjectClass.ACCOUNT, uid, attrs, null, propagationAttempted);

		 Assert.assertEquals("__UID__", accountUpdated.getName());
	 }

	 @Test
	 public void test7DeleteAccount() {

		 Map<Object, Object> dataConnection = getDataConnection();
		 Connector conn = bundleManager.getConnector(dataConnection);
		 AtomicReference<Boolean> propagationAttempted = new AtomicReference<>();
		 Attribute account = AttributeBuilder.build( "short_description", "description from REST connector" );
		 ConnectorObject response = conn.getObject(ObjectClass.ACCOUNT, account, false, null);

		 Uid uid = response.getUid();

		 boolean result = conn.delete(ObjectClass.ACCOUNT, uid, null, propagationAttempted);
		 Assert.assertTrue(result);
	 }
}
