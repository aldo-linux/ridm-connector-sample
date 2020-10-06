package com.ridm.connid.connector;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.*;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class BundleManager {

	public static String BUNDLE_LOCATION;

    private static final Logger LOG = LoggerFactory.getLogger(BundleManager.class);

    public  BundleManager (){
		try (InputStream input = new FileInputStream("src/resources/config.properties")) {

			Properties prop = new Properties();

			// load a properties file
			prop.load(input);

			// get the property value and print it out
			BUNDLE_LOCATION = prop.getProperty("BUNDLE_LOCATION");

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}


    
    private List<ConnectorInfo> connectorInfoList = new ArrayList<>();
    
	public List<ConnectorInfo> loadBundles(final URI location) {

		// 1. Find bundles inside local directory
		File bundleDirectory = new File(location);
		String[] bundleFiles = bundleDirectory.list();
		if (bundleFiles == null) {
			LOG.error("FATAL: Connector Bundles Directory " + location.toString() + " not found");
			// throw new NotFoundException("Local bundles directory " + location);
		}

		List<URL> bundleFileURLs = new ArrayList<>();
		for (String file : bundleFiles) {
			try {
				bundleFileURLs.add(IOUtil.makeURL(bundleDirectory, file));
			} catch (IOException ignore) {
				// ignore exception and don't add bundle
				LOG.debug("{}/{} is not a valid connector bundle", bundleDirectory.toString(), file, ignore);
			}
		}

		if (bundleFileURLs.isEmpty()) {
			LOG.warn("No connector bundles found in {}", location);
		}
		LOG.debug("Configuring local connector server:" + "\n\tFiles: {}", bundleFileURLs);

		// 2. Get connector info manager
		List<ConnectorInfo> connInfoList = new ArrayList<>();
		for (URL url : bundleFileURLs) {
			// Use the ConnectorInfoManager to retrieve a ConnectorInfo object for the
			// connector
			ConnectorInfoManagerFactory fact = ConnectorInfoManagerFactory.getInstance();
			ConnectorInfoManager manager = fact.getLocalManager(url);
			ConnectorKey connectorValues = manager.getConnectorInfos().get(0).getConnectorKey();
			ConnectorKey key = new ConnectorKey(connectorValues.getBundleName(), connectorValues.getBundleVersion(),
					connectorValues.getConnectorName());
			ConnectorInfo info = manager.findConnectorInfo(key);
			connInfoList.add(info);
		}
		this.connectorInfoList = connInfoList;

		return this.connectorInfoList;
	}
	
	public List<ConnectorInfo> getAvailableBundles() {
		if(this.connectorInfoList.isEmpty())
			try {
				URI location = new URI(BundleManager.BUNDLE_LOCATION);
				this.connectorInfoList = this.loadBundles(location);
			} catch (Exception e) {
				LOG.error("FATAL: could not load bundles. " + e.getLocalizedMessage());
			}
		return this.connectorInfoList;

	}

	
    /**
     * Get a Connector instance given the connectorName and configurationProperties.
     *
     * @param connectorName connector bundle name (i.e. net.tirasa.connid.bundles.ldap-1.5.4.jar)
     * @param configurationProperties JSON object, the provisioner
     * @return Connector for created object
     */
	public Connector getConnector(Map<Object, Object> dataConnection) {
		
		HashMap<String, String> connectorNamesList = new HashMap<>();
		connectorNamesList.put("database", "Conector de tabla de base de datos");
		connectorNamesList.put("ldap", "net.tirasa.connid.bundles.ldap.LdapConnector");
		connectorNamesList.put("serviceNow", "Service Now Connector");
		connectorNamesList.put("csv", "net.tirasa.connid.bundles.csvdir.CSVDirConnector");
		connectorNamesList.put("cmd", "net.tirasa.connid.bundles.cmd.CmdConnector");
		connectorNamesList.put("rest", "REST Connector");
		
		String connectorName = dataConnection.get("connectorName").toString();
		Map<Object, Object> configurationProperties = (Map<Object, Object>) dataConnection.get("configurationProperties");
	    
	    List<ConnectorInfo> infos = this.getAvailableBundles();

	    ConnectorInfo info = null;
	    for(ConnectorInfo _info: infos){
	    	System.out.println("Displayname: " + _info.getConnectorDisplayName());
	    	if((connectorNamesList.get(connectorName).equals(_info.getConnectorDisplayName()))){
	    		info = _info;	    		
	    	}
	    }
		if(info == null){
			LOG.error("FATAL: Connector Bundle wasn't found");
			return null;
		}
	    // From the ConnectorInfo object, create the default APIConfiguration.
	    APIConfiguration apiConfig = info.createDefaultAPIConfiguration();
		
		 // From the default APIConfiguration, retrieve the ConfigurationProperties.
	    ConfigurationProperties properties = apiConfig.getConfigurationProperties();
	    
	    // Set all of the ConfigurationProperties needed by the connector.
	    for (Object key : configurationProperties.keySet()) {
	    	if(key.toString().equals("password") && (connectorName.equals("database") || connectorName.equals("serviceNow") || connectorName.equals("rest"))) {
	    		properties.setPropertyValue(key.toString(), new GuardedString(configurationProperties.get(key).toString().toCharArray()));	
	    	}else {
	    		if(key.toString().equals("credentials") && connectorName.equals("ldap")) {
	    			properties.setPropertyValue(key.toString(), new GuardedString(configurationProperties.get(key).toString().toCharArray()));
	    		}
	    		else {
	    			if(key.toString().equals("port") && connectorName.equals("ldap")) {
	    				properties.setPropertyValue(key.toString(), Integer.parseInt(configurationProperties.get(key).toString()) );	
	    			}else
	    				if(key.toString().equals("baseContexts") && connectorName.equals("ldap")) {
	    					String[] SMALL_COMPANY_DN = {configurationProperties.get(key).toString()};
	    					properties.setPropertyValue(key.toString(), SMALL_COMPANY_DN);
	    				} else {
	    					if(key.toString().equals("keyColumnNames") && connectorName.equals("csv")) {
	    						String[] COLUMN_KEY = {configurationProperties.get(key).toString()};
    							properties.setPropertyValue(key.toString(), COLUMN_KEY);
	    					} else {
	    						if(key.toString().equals("fields") && connectorName.equals("csv")) {
	    							String fields = configurationProperties.get(key).toString();
	    							String[] FIELDS = fields.split(",");
	    							properties.setPropertyValue(key.toString(), FIELDS);
	    	    				}
	    						else
	    							properties.setPropertyValue(key.toString(), configurationProperties.get(key));		    		
	    					}
	    					
	    				}
	    		}
	    	}
	    }
		// 1. Create a connection Facade
	    ConnectorFacade connector = ConnectorFacadeFactory.getInstance().newInstance(apiConfig);

	    AsyncConnectorProxy asyncConnector = new AsyncConnectorProxy();
		// 2. Create de Connector object
	    // 3. Assign the Connector Facade to the Connector Object
	    Connector conn = new ConnectorProxy(connector ,asyncConnector);



		// TODO @Hector: After creating the connector, it is good to call the getSchema() and validate()
		// 4. Invoke validate()
	    conn.validate();
		// 5. Invoke getSchema and assign it to the Connector instance: conn.setConnectionProperties()
//	    conn.getObjectClassInfo();

		return conn;
	}


}
