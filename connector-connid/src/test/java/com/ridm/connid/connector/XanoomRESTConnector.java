package com.ridm.connid.connector;

import org.identityconnectors.framework.common.objects.*;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class XanoomRESTConnector {
    BundleManager bundleManager = new BundleManager();

    public Map<Object, Object> getDataConnection() {
        String BASE_ADDRESS = "C:/scriptsGroovy/src/main/java/com/rest/connector/xanoom/";
        Map<Object, Object> dataConnection = new HashMap<>();
        Map<Object, Object> properties = new HashMap<>();

        properties.put("baseAddress", "http://localhost:8080/v1");
//		properties.put("accept", "");
//		properties.put("contentType", "");
//        properties.put("clientSecret", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcGkiOiJhcGlUb2tlbiIsImlhdCI6MTYwMDQ4MTEzNywiZXhwIjoxNjAzMDczMTM3fQ.y37_D5bJ03_t7rreB9M9zm7LtJkv9WkBadsNMDoB3d0");
//        properties.put("cliendId", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcGkiOiJhcGlUb2tlbiIsImlhdCI6MTYwMDQ4MTEzNywiZXhwIjoxNjAzMDczMTM3fQ.y37_D5bJ03_t7rreB9M9zm7LtJkv9WkBadsNMDoB3d0");
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
        attributes.add(AttributeBuilder.build("firstName", "Raul"));
        attributes.add(AttributeBuilder.build("middleName", "Raul2"));
        attributes.add(AttributeBuilder.build("lastName", "Caceres"));
        attributes.add(AttributeBuilder.build("password", "Hello#123"));
        attributes.add(AttributeBuilder.build("phone", "9991234567"));
        attributes.add(AttributeBuilder.build("__NAME__", "raulcaceres@mail.com"));

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
        System.out.println(results);
        Assert.assertTrue(results.size() > 0);
    }

    @Test
    public void test5ReadAccount() {

        Map<Object, Object> dataConnection = getDataConnection();
        Connector conn = bundleManager.getConnector(dataConnection);

        Attribute accountToRead = AttributeBuilder.build("email", "raulcaceres@mail.com");

        ConnectorObject response = conn.getObject(ObjectClass.ACCOUNT, accountToRead, false, null);
        Assert.assertEquals(response.getAttributeByName("phone").getValue().get(0), "9991234567");
//        Assert.assertEquals(response.getAttributeByName("firstName").getValue().get(0), "Raul");
    }

    @Test
    public void test6UpdateAccount() {


        Map<Object, Object> dataConnection = getDataConnection();
        Connector conn = bundleManager.getConnector(dataConnection);
        AtomicReference<Boolean> propagationAttempted = new AtomicReference<>();

        Set<Attribute> attrs = new HashSet<>();
        attrs.add(AttributeBuilder.build( "firstName", "Raul Edited" ));

        // Get unique identifier
        Attribute accountToUpdate = AttributeBuilder.build( "email", "raulcaceres@mail.com" );
        ConnectorObject response = conn.getObject(ObjectClass.ACCOUNT, accountToUpdate, false, null);

        Uid uid = response.getUid();
        //alternative to get uid
//        Uid uid = new Uid("raulcaceres@mail.com");

        Uid accountUpdated = conn.update(ObjectClass.ACCOUNT, uid, attrs, null, propagationAttempted);

        Assert.assertEquals("__UID__", accountUpdated.getName());
    }

    @Test
    public void test7DeleteAccount() {

        Map<Object, Object> dataConnection = getDataConnection();
        Connector conn = bundleManager.getConnector(dataConnection);
        AtomicReference<Boolean> propagationAttempted = new AtomicReference<>();
        Attribute account = AttributeBuilder.build( "email", "raulcaceres@mail.com" );
        ConnectorObject response = conn.getObject(ObjectClass.ACCOUNT, account, false, null);

        Uid uid = response.getUid();
        //alternative to get uid
//        Uid uid = new Uid("raulcaceres@mail.com");

        boolean result = conn.delete(ObjectClass.ACCOUNT, uid, null, propagationAttempted);
        Assert.assertTrue(result);
    }
}
