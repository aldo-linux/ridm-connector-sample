package com.ridm.connid.connector;

//import static org.junit.Assert.assertTrue;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Collection;
import java.util.HashSet;
import java.util.List;
//import java.util.Optional;
import java.util.Set;
import java.net.URL;
import java.io.File;
//import net.tirasa.connid.bundles.ldap.LdapConfiguration;
//import net.tirasa.connid.bundles.ldap.LdapConnector;
import org.identityconnectors.common.IOUtil;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.api.APIConfiguration;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.api.ConnectorFacadeFactory;
import org.identityconnectors.framework.api.ConnectorInfoManager;
import org.identityconnectors.framework.api.ConnectorInfoManagerFactory;
//import org.identityconnectors.framework.api.operations.SearchApiOp;
import org.identityconnectors.framework.api.ConnectorKey;
import org.identityconnectors.framework.api.ConnectorInfo;
//import org.identityconnectors.framework.api.ConfigurationProperty;
import org.identityconnectors.framework.api.ConfigurationProperties;
//import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
//import org.identityconnectors.framework.common.objects.ConnectorMessages;
import org.identityconnectors.framework.common.objects.ConnectorObject;
//import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.Schema;
//import org.identityconnectors.framework.common.objects.ObjectClass;
//import org.identityconnectors.framework.common.objects.OperationOptions;
//import org.identityconnectors.framework.common.objects.OperationOptions;
//import org.identityconnectors.framework.common.objects.OperationOptionsBuilder;
//import org.identityconnectors.framework.common.objects.OperationalAttributes;
//import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.Uid;
//import org.identityconnectors.framework.common.objects.filter.Filter;
//import org.identityconnectors.framework.spi.Configuration;
//import org.identityconnectors.framework.spi.Connector;
//import org.identityconnectors.framework.spi.operations.SearchOp;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.FilterBuilder;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
  public static final String[] SMALL_COMPANY_DN = {"ou=Users,dc=example,dc=com"};

  @Test
  public void testCreateLdapAccount() throws Exception {
    // Use the ConnectorInfoManager to retrieve a ConnectorInfo object for the connector
    ConnectorInfoManagerFactory fact = ConnectorInfoManagerFactory.getInstance();
    File bundleDirectory = new File("C:/Users/igniter/Downloads/bundles");
    URL url = IOUtil.makeURL(bundleDirectory, "/net.tirasa.connid.bundles.ldap-1.5.4.jar");
    ConnectorInfoManager manager = fact.getLocalManager(url);
    ConnectorKey connectorValues =  manager.getConnectorInfos().get(0).getConnectorKey();
    // System.out.println("bundleName: " + connectorValues.getBundleName() + " bundleVersion: " + connectorValues.getBundleVersion() + " connectorName: " + connectorValues.getConnectorName());
    ConnectorKey key = new ConnectorKey(connectorValues.getBundleName(), connectorValues.getBundleVersion(), connectorValues.getConnectorName());
    ConnectorInfo info = manager.findConnectorInfo(key);

    // From the ConnectorInfo object, create the default APIConfiguration.
    APIConfiguration apiConfig = info.createDefaultAPIConfiguration();

    // From the default APIConfiguration, retrieve the ConfigurationProperties.
    ConfigurationProperties properties = apiConfig.getConfigurationProperties();

    // Print out what the properties are (not necessary)
    // List propertyNames = properties.getPropertyNames();
    //for(String propName : propertyNames) {
    //    ConfigurationProperty prop = properties.getProperty(propName);
    //    System.out.println("Property Name: " + prop.getName() + "\tProperty Type: " + prop.getType());
   // }

    // Set all of the ConfigurationProperties needed by the connector.
    // Assume embedded ApacheDS in Apache Directory Studio
    
    properties.setPropertyValue("host", "localhost");
    properties.setPropertyValue("port", 10389);
    properties.setPropertyValue("principal", "uid=admin,ou=system");
    properties.setPropertyValue("credentials", new GuardedString("secret".toCharArray()));
    
    properties.setPropertyValue("baseContexts", SMALL_COMPANY_DN);


    // Use the ConnectorFacadeFactory's newInstance() method to get a new connector.
    ConnectorFacade conn = ConnectorFacadeFactory.getInstance().newInstance(apiConfig);

    // Make sure we have set up the Configuration properly
    conn.validate();
    
    // Set options
//    OperationOptions options = null;
    // Check authentication
//    conn.authenticate(ObjectClass.ACCOUNT, "uid=admin,ou=system",new GuardedString("secret".toCharArray()) , options);
    
    // Create an Account
//    Set<Attribute> attributes = new HashSet<Attribute>();
//    Name name = new Name("uid=Raul.Caceres,ou=Users,dc=example,dc=com" );
//    attributes.add(name);
////    attributes.add(AttributeBuilder.build("uid", "2"));
//    attributes.add(AttributeBuilder.build("cn", "Raul"));
//    attributes.add(AttributeBuilder.build("givenName", "Raul g"));
//    attributes.add(AttributeBuilder.build("sn", "Worker"));
//
//    final Uid uid = conn.create(ObjectClass.ACCOUNT, attributes, null);
//
//    ConnectorObject newAccount = conn.getObject(ObjectClass.ACCOUNT, uid, null);
    
//    assertEquals(name, newAccount.getName());
    //TODO checar como hacer un search
    Filter filter = (FilterBuilder.equalTo(AttributeBuilder.build("__NAME__","ou=Users,dc=example,dc=com")));
    
    final List<ConnectorObject> results = new ArrayList<ConnectorObject>();
    ResultsHandler handler = new ResultsHandler() {
        public boolean handle(ConnectorObject obj) {
            results.add(obj);
            return true;
        } 
    };

    // Search the account
    conn.search(ObjectClass.ACCOUNT, filter, handler, null);
    

  }
  
//  @Test
  public void testCheckConnectiont() throws Exception {
    // Use the ConnectorInfoManager to retrieve a ConnectorInfo object for the connector
    ConnectorInfoManagerFactory fact = ConnectorInfoManagerFactory.getInstance();
    File bundleDirectory = new File("C:/Users/igniter/Downloads/bundles");
    URL url = IOUtil.makeURL(bundleDirectory, "/net.tirasa.connid.bundles.ldap-1.5.4.jar");
    ConnectorInfoManager manager = fact.getLocalManager(url);
    ConnectorKey connectorValues =  manager.getConnectorInfos().get(0).getConnectorKey();
    ConnectorKey key = new ConnectorKey(connectorValues.getBundleName(), connectorValues.getBundleVersion(), connectorValues.getConnectorName());
    ConnectorInfo info = manager.findConnectorInfo(key);

    // From the ConnectorInfo object, create the default APIConfiguration.
    APIConfiguration apiConfig = info.createDefaultAPIConfiguration();

    // From the default APIConfiguration, retrieve the ConfigurationProperties.
    ConfigurationProperties properties = apiConfig.getConfigurationProperties();

    // Set all of the ConfigurationProperties needed by the connector.
    // Assume embedded ApacheDS in Apache Directory Studio
    
    properties.setPropertyValue("host", "localhost");
    properties.setPropertyValue("port", 10389);
    properties.setPropertyValue("principal", "uid=admin,ou=system");
    properties.setPropertyValue("credentials", new GuardedString("secret".toCharArray()));
    
    properties.setPropertyValue("baseContexts", SMALL_COMPANY_DN);


    // Use the ConnectorFacadeFactory's newInstance() method to get a new connector.
    ConnectorFacade conn = ConnectorFacadeFactory.getInstance().newInstance(apiConfig);

    // Make sure we have set up the Configuration properly
    conn.validate();
    
    // Test configuration
    try{
    	conn.test();
    	System.out.println("The Connection was successful");
    }
    catch(RuntimeException e) {
    	System.out.println("The Connection failed");
    }
    
    // Check Schema
    conn.schema();
    

  }
}
