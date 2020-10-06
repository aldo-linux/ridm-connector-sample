package com.ridm.connid.connector;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.identityconnectors.common.IOUtil;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.api.APIConfiguration;
import org.identityconnectors.framework.api.ConfigurationProperties;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.api.ConnectorFacadeFactory;
import org.identityconnectors.framework.api.ConnectorInfo;
import org.identityconnectors.framework.api.ConnectorInfoManager;
import org.identityconnectors.framework.api.ConnectorInfoManagerFactory;
import org.identityconnectors.framework.api.ConnectorKey;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ObjectClassInfo;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.SyncDelta;
import org.identityconnectors.framework.common.objects.SyncResultsHandler;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.FilterBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.springframework.stereotype.Component;

/**
 * Hello world!
 *
 */
@Component
public class App 
{
	private String bundleDirectory = "C:/Users/igniter/Downloads/bundles";
	
	private ConnectorFacade getConnector(String connectionType, String configurationProperties) throws Exception {
		String bundlePath = "";
		
		Object obj = JSONValue.parse(configurationProperties);  
	    JSONObject jsonObject = (JSONObject) obj;
	    
	    switch(connectionType) {
	    case "database":
	    	bundlePath = "/net.tirasa.connid.bundles.db.table-2.2.6.jar";
	    	break;
	    case "ldap":
	    	bundlePath = "/net.tirasa.connid.bundles.ldap-1.5.4.jar";
	    	break;
	    case "serviceNow":
	    	bundlePath = "/net.tirasa.connid.bundles.servicenow-1.0.0.jar";
	    	break;
	    case "csv":
	    	bundlePath = "/net.tirasa.connid.bundles.csvdir-0.8.8.jar";
	    	break;	
	    case "cmd":
	    	bundlePath = "/net.tirasa.connid.bundles.cmd-0.3.jar";
	    	break;	
	    case "azure":
	    	bundlePath = "";
	    	break;	
	    case "okta":
	    	bundlePath = "net.tirasa.connid.bundles.okta-1.0.0.jar";
	    	break;	
	    case "rest":
	    	bundlePath = "/net.tirasa.connid.bundles.rest-1.0.5.jar";
	    	break;	
	    }
	    
		 // Use the ConnectorInfoManager to retrieve a ConnectorInfo object for the connector
	    ConnectorInfoManagerFactory fact = ConnectorInfoManagerFactory.getInstance();
	    File bundleDirectory = new File(this.bundleDirectory);
	    URL url = IOUtil.makeURL(bundleDirectory, bundlePath);
	    ConnectorInfoManager manager = fact.getLocalManager(url);
	    ConnectorKey connectorValues =  manager.getConnectorInfos().get(0).getConnectorKey();
	    ConnectorKey key = new ConnectorKey(connectorValues.getBundleName(), connectorValues.getBundleVersion(), connectorValues.getConnectorName());
	    ConnectorInfo info = manager.findConnectorInfo(key);

	    // From the ConnectorInfo object, create the default APIConfiguration.
	    APIConfiguration apiConfig = info.createDefaultAPIConfiguration();
		
		 // From the default APIConfiguration, retrieve the ConfigurationProperties.
	    ConfigurationProperties properties = apiConfig.getConfigurationProperties();
	    
	    // Set all of the ConfigurationProperties needed by the connector.
	    for (Object JSONkey : jsonObject.keySet()) {
//    		String a = JSONkey.toString();
//	    	System.out.println("key: " +  a + " value: " + jsonObject.get(JSONkey).toString() );
	    	if(JSONkey.toString().equals("password") && (connectionType.equals("database") || connectionType.equals("serviceNow") || connectionType.equals("rest"))) {
	    		properties.setPropertyValue(JSONkey.toString(), new GuardedString(jsonObject.get(JSONkey).toString().toCharArray()));	
	    	}else {
	    		if(JSONkey.toString().equals("credentials") && connectionType.equals("ldap")) {
	    			properties.setPropertyValue(JSONkey.toString(), new GuardedString(jsonObject.get(JSONkey).toString().toCharArray()));
	    		}
	    		else {
	    			if(JSONkey.toString().equals("port") && connectionType.equals("ldap")) {
	    				properties.setPropertyValue(JSONkey.toString(), Integer.parseInt(jsonObject.get(JSONkey).toString()) );	
	    			}else
	    				if(JSONkey.toString().equals("baseContexts") && connectionType.equals("ldap")) {
	    					String[] SMALL_COMPANY_DN = {jsonObject.get(JSONkey).toString()};
	    					properties.setPropertyValue(JSONkey.toString(), SMALL_COMPANY_DN);
	    				} else {
	    					if(JSONkey.toString().equals("keyColumnNames") && connectionType.equals("csv")) {
	    						String[] COLUMN_KEY = {jsonObject.get(JSONkey).toString()};
    							properties.setPropertyValue(JSONkey.toString(), COLUMN_KEY);
	    					} else {
	    						if(JSONkey.toString().equals("fields") && connectionType.equals("csv")) {
	    							String fields = jsonObject.get(JSONkey).toString();
	    							String[] FIELDS = fields.split(",");
	    							properties.setPropertyValue(JSONkey.toString(), FIELDS);
	    	    				}
	    						else
	    							properties.setPropertyValue(JSONkey.toString(), jsonObject.get(JSONkey));		    		
	    					}
	    					
	    				}
	    		}
	    	}
	    }
	    
	    // Use the ConnectorFacadeFactory's newInstance() method to get a new connector.
	    ConnectorFacade connector = ConnectorFacadeFactory.getInstance().newInstance(apiConfig);
	    return connector;
	}
	
	// Test connection
	public boolean testConnection(String dataConnection) throws Exception { 
		
		// JSON with data connection
		Object objConnection = JSONValue.parse(dataConnection);  
	    JSONObject jsonObjectConnection = (JSONObject) objConnection;
		
	    // Get properties to use the bundle connector
		String connectorType = jsonObjectConnection.get("connectorName").toString();
		String configurationProperties = jsonObjectConnection.get("configurationProperties").toString();
		
		// Get connector
	    ConnectorFacade conn = getConnector(connectorType, configurationProperties);

	    // Make sure we have set up the Configuration properly
	    conn.validate();
	    
	    // Test configuration
	    try{
	    	conn.test();
	    	System.out.println("The Connection was successful");
	    	return true;
	    }
	    catch(RuntimeException e) {
	    	System.out.println("The Connection failed");
	    	return false;
	    }
	  }
	
	// Get Schema
	@SuppressWarnings("unchecked")
	public String getSchema(String dataConnection) throws Exception { 
		
		// JSON with data connection
		Object objConnection = JSONValue.parse(dataConnection);  
	    JSONObject jsonObjectConnection = (JSONObject) objConnection;
		
	    // Get properties to use the bundle connector
		String connectorType = jsonObjectConnection.get("connectorName").toString();
		String configurationProperties = jsonObjectConnection.get("configurationProperties").toString();
		
		// Get connector
	    ConnectorFacade conn = getConnector(connectorType, configurationProperties);

	    // Make sure we have set up the Configuration properly
	    conn.validate();
	    
	    // Get schema
	    Schema schema = conn.schema();
	    
	    Set<ObjectClassInfo> objectClasses = schema.getObjectClassInfo();  
	    
	    Map<Object, Object> account = new HashMap<Object, Object>();
	    Map<Object, Object> properties = new HashMap<Object, Object>();
	    
	    for(ObjectClassInfo objectClass : objectClasses) {
	    	// Get each attribute
	        for(AttributeInfo attribute: objectClass.getAttributeInfo()) {
	        	// Set jsonSchema
	        	Map<Object, Object> property = new HashMap<Object, Object>();
        		// TODO check if map this type to generic types in JSON
        		property.put("type", attribute.getType().getSimpleName().toLowerCase());
        		property.put("nativeName", attribute.getName());
        		// alternative in another connectors
        		// property.put("nativeName", attribute.getNativeName());
        		property.put("nativeType", attribute.getType().getSimpleName().toLowerCase());
	        	if(attribute.getFlags().size() > 0) {
	        		JSONArray Flags = new JSONArray();
        			for(AttributeInfo.Flags flag: attribute.getFlags() ) {
        				Flags.add(flag.name().toString());
//        				schemaAttributes.put(attribute.getName(), flag.name());
        			}
        			property.put("flags", Flags);
        		}
	        	properties.put(attribute.getName(), property);
	        }
	    }
	    account.put("id", "__ACCOUNT__");
	    account.put("type", "object");
	    account.put("properties", properties);
	    
	    String jsonSchema = JSONValue.toJSONString(account); 
	    System.out.println("JSON: " + jsonSchema);
	    return jsonSchema;
 
	  }
	
	// Create account
	public Uid createAccount(String dataConnection, String data, String domainName) throws Exception { 
		
		// JSON with data connection
		Object objConnection = JSONValue.parse(dataConnection);  
	    JSONObject jsonObjectConnection = (JSONObject) objConnection;
		
	    // Get properties to use the bundle connector
		String connectorType = jsonObjectConnection.get("connectorName").toString();
		String configurationProperties = jsonObjectConnection.get("configurationProperties").toString();
		
		// Get connector
	    ConnectorFacade conn = getConnector(connectorType, configurationProperties);
	    
	    // JSON with the account data to create
	    Object objData = JSONValue.parse(data);  
	    JSONObject jsonObjectData = (JSONObject) objData;
	    
	    // Set attributes to create
	    Set<Attribute> attributes = new HashSet<Attribute>();
	    UUID uuid = UUID.randomUUID();
	    Name name = new Name(uuid.toString());
	    if(connectorType.equals("ldap")) {
	    	name = new Name("uid=" + uuid.toString() + "," + domainName);
	    }
	    attributes.add(name);
	    for (Object JSONkey : jsonObjectData.keySet()) {
	    	attributes.add(AttributeBuilder.build( JSONkey.toString(), jsonObjectData.get(JSONkey).toString() ));
	    }
	    
	    Uid uid = conn.create(ObjectClass.ACCOUNT, attributes, null);
	    return uid;
	    
	  }
	
	// Read account
	public String readAccount(String dataConnection, String Uid) throws Exception {
		 
		// JSON with data connection
		Object objConnection = JSONValue.parse(dataConnection);  
	    JSONObject jsonObjectConnection = (JSONObject) objConnection;
		
	    // Get properties to use the bundle connector
		String connectorType = jsonObjectConnection.get("connectorName").toString();
		String configurationProperties = jsonObjectConnection.get("configurationProperties").toString();
		
		// Get connector
	    ConnectorFacade conn = getConnector(connectorType, configurationProperties);
	    
	    // JSON with the account unique identifier to read
	    Object objData = JSONValue.parse(Uid);  
	    JSONObject jsonObjectData = (JSONObject) objData;
	    
	    // Get unique identifier and value
	    Attribute accountToRead = null;
	    for (Object JSONkey : jsonObjectData.keySet()) {
	    	accountToRead = AttributeBuilder.build( JSONkey.toString(), jsonObjectData.get(JSONkey).toString() );
	    }
	    Filter filter = FilterBuilder.equalTo(accountToRead);
	    
	    final List<ConnectorObject> results = new ArrayList<ConnectorObject>();
	    ResultsHandler handler = new ResultsHandler() {
	        public boolean handle(ConnectorObject obj) {
	            results.add(obj);
	            return true;
	        } 
	    };

	    // Search the account
	    conn.search(ObjectClass.ACCOUNT, filter, handler, null);
	    // Get Uid
	    if(results.size() > 0) {	    	
	    	Uid id = results.get(0).getUid();
	    	ConnectorObject account = conn.getObject(ObjectClass.ACCOUNT, id, null);
	    	
	    	Map<String, String> accountReaded = new HashMap<String, String>(); 
	    	
	    	for(Attribute element: account.getAttributes()) {
	    		for(Object value: element.getValue()) {
	    			accountReaded.put(element.getName(), value.toString());
	    		}
	    	}
	    	
	    	String jsonAccount = JSONValue.toJSONString(accountReaded); 
	    	System.out.println("JSON: " + jsonAccount);
	    	return jsonAccount;
	    }else
	    	return "";
	    
	   
	 }
	
	// Update account
	public void updateAccount(String dataConnection, String data, String Uid) throws Exception {
		 
		// JSON with data connection
		Object objConnection = JSONValue.parse(dataConnection);  
	    JSONObject jsonObjectConnection = (JSONObject) objConnection;
		
	    // Get properties to use the bundle connector
		String connectorType = jsonObjectConnection.get("connectorName").toString();
		String configurationProperties = jsonObjectConnection.get("configurationProperties").toString();
		
		// Get connector
	    ConnectorFacade conn = getConnector(connectorType, configurationProperties);
	    
	    // JSON with the account data to update
	    Object objData = JSONValue.parse(data);  
	    JSONObject jsonObjectData = (JSONObject) objData;
	    
	    // JSON with the unique identifier 
	    Object objUid = JSONValue.parse(Uid);  
	    JSONObject jsonObjectUid = (JSONObject) objUid;
	    
	    // Get unique identifier and value
	    Attribute accountToUpdate = null;
	    for (Object JSONkey : jsonObjectUid.keySet()) {
	    	accountToUpdate = AttributeBuilder.build( JSONkey.toString(), jsonObjectUid.get(JSONkey).toString() );
	    }
	    Filter filter = (FilterBuilder.equalTo(accountToUpdate));
	    
	    final List<ConnectorObject> results = new ArrayList<ConnectorObject>();
	    ResultsHandler handler = new ResultsHandler() {
	        public boolean handle(ConnectorObject obj) {
	            results.add(obj);
	            return true;
	        } 
	    };
	    
	    // Search account to update
	    conn.search(ObjectClass.ACCOUNT, filter, handler, null);
	    Uid id = results.get(0).getUid();
	    
	    // Create attributes to update
	    Set<Attribute> attributesToUpdate = new HashSet<Attribute>();
	    for (Object JSONkey : jsonObjectData.keySet()) {
	    	attributesToUpdate.add(AttributeBuilder.build( JSONkey.toString(), jsonObjectData.get(JSONkey).toString() ));
	    }
	    
	    conn.update(ObjectClass.ACCOUNT, id, attributesToUpdate, null);
	    
	 }
	// Delete account
	public void deleteAccount(String dataConnection, String Uid) throws Exception {
		 
		// JSON with data connection
		Object objConnection = JSONValue.parse(dataConnection);  
	    JSONObject jsonObjectConnection = (JSONObject) objConnection;
		
	    // Get properties to use the bundle connector
		String connectorType = jsonObjectConnection.get("connectorName").toString();
		String configurationProperties = jsonObjectConnection.get("configurationProperties").toString();
		
		// Get connector
	    ConnectorFacade conn = getConnector(connectorType, configurationProperties);
	    
	    // JSON with the account unique identifier to delete
	    Object objData = JSONValue.parse(Uid);  
	    JSONObject jsonObjectData = (JSONObject) objData;
	    
	    // Get unique identifier and value
	    Attribute accountToDelete = null;
	    for (Object JSONkey : jsonObjectData.keySet()) {
	    	accountToDelete = AttributeBuilder.build( JSONkey.toString(), jsonObjectData.get(JSONkey).toString() );
	    }
	    Filter filter = (FilterBuilder.equalTo(accountToDelete));
	    
	    final List<ConnectorObject> results = new ArrayList<ConnectorObject>();
	    ResultsHandler handler = new ResultsHandler() {
	        public boolean handle(ConnectorObject obj) {
	            results.add(obj);
	            return true;
	        } 
	    };

	    // Search account to delete
	    conn.search(ObjectClass.ACCOUNT, filter, handler, null);
	    Uid id = results.get(0).getUid();
	    
	    conn.delete(ObjectClass.ACCOUNT, id, null);
	 }
	
	@SuppressWarnings("unchecked")
	public String syncAccounts(String dataConnection) throws Exception {
		// JSON with data connection
		Object objConnection = JSONValue.parse(dataConnection);  
		JSONObject jsonObjectConnection = (JSONObject) objConnection;
				
		// Get properties to use the bundle connector
		String connectorType = jsonObjectConnection.get("connectorName").toString();
		String configurationProperties = jsonObjectConnection.get("configurationProperties").toString();
		
		// Get connector
		ConnectorFacade conn = getConnector(connectorType, configurationProperties);
		
		final List<SyncDelta> results = new ArrayList<SyncDelta>();
	    SyncResultsHandler handler = new SyncResultsHandler() {
	        public boolean handle(SyncDelta delta) {
	            results.add(delta);
	            return true;
	        } 
	    };
	    
		conn.sync(ObjectClass.ACCOUNT, null, handler, null);
		
		
		JSONArray accounts = new JSONArray();

		for(SyncDelta result: results) {
			Map<String, String> account = new HashMap<String, String>();
			for(Attribute attribute: result.getObject().getAttributes()) {
				System.out.println("name: " + attribute.getName() + " value: "+ attribute.getValue().get(0));
				account.put(attribute.getName().toString(), attribute.getValue().get(0).toString());
			}
			accounts.add(account);
		}
		String accountsList = JSONValue.toJSONString(accounts); 
	    System.out.println("JSON: " + accountsList);
		return accountsList;
	}
	
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
    }
}
