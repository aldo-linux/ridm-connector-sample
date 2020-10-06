package com.ridm.connid.connector;

import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.identityconnectors.common.security.GuardedByteArray;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.api.APIConfiguration;
import org.identityconnectors.framework.api.ConfigurationProperties;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.api.ConnectorInfo;
import org.identityconnectors.framework.api.operations.*;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.spi.SearchResultsHandler;
import org.json.simple.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ClassUtils;

public class ConnectorProxy implements Connector {

	private static final Logger LOG = LoggerFactory.getLogger(ConnectorProxy.class);

	private static final Integer DEFAULT_PAGE_SIZE = 100;

	private static final long REQUEST_TIMEOUT = 15;

	/**
	 * Connector facade wrapped instance.
	 */
	@Autowired
	private ConnectorFacade connector;

	/**
	 * Active connector instance.
	 */
	private ConnInstance connInstance;

	@Autowired
	private AsyncConnectorProxy asyncFacade;

	// TODO @Hector: add ConnectionProperties JSON as an attribute with setters and getters

	private Map<Object, Object> connectionProperties;

	public void setConnectionProperties(Map<Object, Object> connectionProperties) {
		this.connectionProperties = connectionProperties;
	}
	public Map<Object, Object> getConnectionProperties(){
		return connectionProperties;
	}

	/**
	 * Use the passed connector instance to build a ConnectorFacade that will be
	 * used to make all wrapped calls.
	 *
	 * @param connInstance the connector instance
	 * @see ConnectorInfo
	 * @see APIConfiguration
	 * @see ConfigurationProperties
	 * @see ConnectorFacade
	 */
	public ConnectorProxy(ConnectorFacade connector, AsyncConnectorProxy asyncFacade) {
		this.connector = connector;
		this.connInstance = null;
		this.asyncFacade = asyncFacade;
	}

	@Override
	public Uid authenticate(final String username, final String password, final OperationOptions options) {
		Uid result = null;

		
		if (connector.getSupportedOperations().contains(AuthenticationApiOp.class)) {
			Future<Uid> future = asyncFacade.authenticate(connector, username,
					new GuardedString(password.toCharArray()), options);
			try {
				result = future.get(REQUEST_TIMEOUT, TimeUnit.SECONDS);
				LOG.info("Authentication successful");
			} catch (java.util.concurrent.TimeoutException e) {
				future.cancel(true);
				//throw new TimeoutException("Request timeout");
			} catch (Exception e) {
				LOG.error("Connector request execution failure", e);
				if (e.getCause() instanceof RuntimeException) {
					throw (RuntimeException) e.getCause();
				} else {
					throw new RuntimeException(e.getCause());
				}
			}
		} else {
			LOG.warn("Authenticate was attempted, although the connector only has these capabilities: {}. No action.",
					connector.getSupportedOperations());
		}		 

		return result;
	}

	@Override
	public Uid create(final ObjectClass objectClass, final Set<Attribute> attrs, final OperationOptions options,
			final AtomicReference<Boolean> propagationAttempted) {

		Uid result = null;

		if (connector.getSupportedOperations().contains(CreateApiOp.class)) {
			propagationAttempted.set(true);

			Future<Uid> future = asyncFacade.create(connector, objectClass, attrs, options);
			try {
				result = future.get(REQUEST_TIMEOUT, TimeUnit.SECONDS);
				LOG.info("Account was created successful");
			} catch (java.util.concurrent.TimeoutException e) {
				future.cancel(true);
				//throw new TimeoutException("Request timeout");
			} catch (Exception e) {
				LOG.error("Connector request execution failure", e);
				if (e.getCause() instanceof RuntimeException) {
					throw (RuntimeException) e.getCause();
				} else {
					throw new RuntimeException(e.getCause());
				}
			}
		} else {
			LOG.warn("Create was attempted, although the connector only has these capabilities: {}. No action.",
					connector.getSupportedOperations());
		}
		 
		return result;
	}

	@Override
	public Uid update(final ObjectClass objectClass, final Uid uid, final Set<Attribute> attrs,
			final OperationOptions options, final AtomicReference<Boolean> propagationAttempted) {

		Uid result = null;

		
		if (connector.getSupportedOperations().contains(UpdateApiOp.class)) {
			propagationAttempted.set(true);

			Future<Uid> future = asyncFacade.update(connector, objectClass, uid, attrs, options);

			try {
				result = future.get(REQUEST_TIMEOUT, TimeUnit.SECONDS);
				LOG.info("Account was updated successful");
			} catch (java.util.concurrent.TimeoutException e) {
				future.cancel(true);
				//throw new TimeoutException("Request timeout");
			} catch (Exception e) {
				LOG.error("Connector request execution failure", e);
				if (e.getCause() instanceof RuntimeException) {
					throw (RuntimeException) e.getCause();
				} else {
					throw new RuntimeException(e.getCause());
				}
			}
		} else {
			LOG.warn(
					"Update for {} was attempted, although the "
							+ "connector only has these capabilities: {}. No action.",
					uid.getUidValue(), connector.getSupportedOperations());
		}

		return result;
	}

	@Override
	public Boolean delete(final ObjectClass objectClass, final Uid uid, final OperationOptions options,
						  final AtomicReference<Boolean> propagationAttempted) {

		boolean result = false;
		if (connector.getSupportedOperations().contains(DeleteApiOp.class)) {
			propagationAttempted.set(true);

			Future<Uid> future = asyncFacade.delete(connector, objectClass, uid, options);

			try {
				future.get(REQUEST_TIMEOUT, TimeUnit.SECONDS);
				result = true;
				LOG.info("Account was deleted successful");
			} catch (java.util.concurrent.TimeoutException e) {
				future.cancel(true);
				//throw new TimeoutException("Request timeout");
			} catch (Exception e) {
				LOG.error("Connector request execution failure", e);
				if (e.getCause() instanceof RuntimeException) {
					throw (RuntimeException) e.getCause();
				} else {
					throw new RuntimeException(e.getCause());
				}
			}
		} else {
			LOG.warn("Delete for {} was attempted, although the connector only has these capabilities: {}. No action.",
					uid.getUidValue(), connector.getSupportedOperations());
		}
		return result;
		 
	}

	@Transactional
	@Override
	public void sync(final ObjectClass objectClass, final SyncToken token, final SyncResultsHandler handler,
			final OperationOptions options) {

		if (connector.getSupportedOperations().contains(SyncApiOp.class)) {
			connector.sync(objectClass, token, handler, options);
			LOG.info("Sync all account was successful");
		} else {
			LOG.warn("Sync was attempted, although the connector only has these capabilities: {}. No action.",
					connector.getSupportedOperations());
		}

		// TODO @Hector: What does sync operation consist of? should this method return something?
		// TBD: handler must be created here and return list or result o return sync token sync
		 
	}

	@Override
	public SyncToken getLatestSyncToken(final ObjectClass objectClass) {
		SyncToken result = null;

		
		if (connector.getSupportedOperations().contains(SyncApiOp.class)) {
			Future<SyncToken> future = asyncFacade.getLatestSyncToken(connector, objectClass);

			try {
				result = future.get(REQUEST_TIMEOUT, TimeUnit.SECONDS);
				LOG.info("Get the sync token was successful");
			} catch (java.util.concurrent.TimeoutException e) {
				future.cancel(true);
				//throw new TimeoutException("Request timeout");
			} catch (Exception e) {
				LOG.error("Connector request execution failure", e);
				if (e.getCause() instanceof RuntimeException) {
					throw (RuntimeException) e.getCause();
				} else {
					throw new RuntimeException(e.getCause());
				}
			}
		} else {
			LOG.warn("getLatestSyncToken was attempted, although the "
					+ "connector only has these capabilities: {}. No action.", connector.getSupportedOperations());
		}
		 
		return result;
	}

	@Transactional
	@Override
	public void fullReconciliation(final ObjectClass objectClass, final SyncResultsHandler handler,
			final OperationOptions options) {

		Connector.super.fullReconciliation(objectClass, handler, options);

		// TODO @Hector: What does fullReconcilliation operation consist of? should this method return something?
	}

	/*
	 * @Transactional
	 * 
	 * @Override public void filteredReconciliation(final ObjectClass objectClass,
	 * final ReconFilterBuilder filterBuilder, final SyncResultsHandler handler,
	 * final OperationOptions options) {
	 * 
	 * // Connector.super.filteredReconciliation(objectClass, filterBuilder,
	 * handler, // options); }
	 */
	@Override
	public Map<Object, Object> getObjectClassInfo() {

		Future<Set<ObjectClassInfo>> future = null;

		if (connector.getSupportedOperations().contains(SchemaApiOp.class)) {
			future = asyncFacade.getObjectClassInfo(connector);
		} else {
			LOG.warn("Search was attempted, although the connector only has these capabilities: {}. No action.",
					connector.getSupportedOperations());
			return null;
		}

		Set<ObjectClassInfo> result = new HashSet<>();
		Map<Object, Object> objectTypes = new HashMap<>();
		Map<Object, Object> schema = new HashMap<>();
		Map<Object, Object> properties = new HashMap<>();
		try {
//			return future.get(connInstance.getConnRequestTimeout(), TimeUnit.SECONDS);
			result = future.get(REQUEST_TIMEOUT, TimeUnit.SECONDS);
			for(ObjectClassInfo objectClass : result) {
				// Get each attribute
				for(AttributeInfo attribute: objectClass.getAttributeInfo()) {
					// Set jsonSchema
					Map<Object, Object> property = new HashMap<>();
					property.put("type", attribute.getType().getSimpleName().toLowerCase());
					property.put("nativeName", attribute.getName());
					// alternative in another connectors
					// property.put("nativeName", attribute.getNativeName());
					property.put("nativeType", attribute.getType().getSimpleName().toLowerCase());
					if(attribute.getFlags().size() > 0) {
						JSONArray Flags = new JSONArray();
						for(AttributeInfo.Flags flag: attribute.getFlags() ) {
							Flags.add(flag.name().toString());
						}
						property.put("flags", Flags);
					}
					properties.put(attribute.getName(), property);
				}
			}
			schema.put("id","__ACCOUNT__");
			schema.put("type","object");
			schema.put("nativeType","__ACCOUNT__");
			schema.put("properties",properties);
			objectTypes.put("account", schema);
			LOG.info("Schema was obtained successful");
		} catch (java.util.concurrent.TimeoutException e) {
			future.cancel(true);
			//throw new TimeoutException("Request timeout");
		} catch (Exception e) {
			LOG.error("Connector request execution failure", e);
			if (e.getCause() instanceof RuntimeException) {
				throw (RuntimeException) e.getCause();
			} else {
				throw new RuntimeException(e.getCause());
			}
		}


		return objectTypes;
		 
	}

	@Override
	public boolean validate() {
		boolean result = false;
		Future<String> future = asyncFacade.test(connector);
		try {
			future.get(REQUEST_TIMEOUT, TimeUnit.SECONDS);
			result = true;
			LOG.info("The properties were validated correctly");
		} catch (java.util.concurrent.TimeoutException e) {
			future.cancel(true);
			//throw new TimeoutException("Request timeout");
		} catch (Exception e) {
			LOG.error("Connector request execution failure", e);
			if (e.getCause() instanceof RuntimeException) {
				throw (RuntimeException) e.getCause();
			} else {
				throw new RuntimeException(e.getCause());
			}
		}
		return result;
	}

	@Override
	public boolean test() {
		boolean result = false;
		Future<String> future = asyncFacade.test(connector);
		try {
			future.get(REQUEST_TIMEOUT, TimeUnit.SECONDS);
			result = true;
			LOG.info("Connector was done successful");
		} catch (java.util.concurrent.TimeoutException e) {
			future.cancel(true);
			//throw new TimeoutException("Request timeout");
		} catch (Exception e) {
			LOG.error("Connector request execution failure", e);
			if (e.getCause() instanceof RuntimeException) {
				throw (RuntimeException) e.getCause();
			} else {
				throw new RuntimeException(e.getCause());
			}
		}
		// @Hector:  What is the difference between test() and that validate()
		// The difference between both is validate only check configuration properties are defined correctly and test tries to connect with them
		return result;
	}

	//@Override
	
	public ConnectorObject getObject(final ObjectClass objectClass, final Attribute connObjectKey,
			final boolean ignoreCaseMatch, final OperationOptions options) {

		Future<ConnectorObject> future = null;

		if (connector.getSupportedOperations().contains(SearchApiOp.class)) {
			future = asyncFacade.getObject(connector, objectClass, connObjectKey, ignoreCaseMatch, options);
		} else {
			LOG.warn("Search was attempted, although the connector only has these capabilities: {}. No action.",
					connector.getSupportedOperations());
		}

		try {
			LOG.info("Account was obtained successfully");
			return future == null ? null : future.get(REQUEST_TIMEOUT, TimeUnit.SECONDS);
		} catch (java.util.concurrent.TimeoutException e) {
			future.cancel(true);
			//throw new TimeoutException("Request timeout");
		} catch (Exception e) {
			LOG.error("Connector request execution failure", e);
			if (e.getCause() instanceof RuntimeException) {
				throw (RuntimeException) e.getCause();
			} else {
				throw new RuntimeException(e.getCause());
			}
		}

		return null;

	}
	 

	@Override
	public SearchResult search(final ObjectClass objectClass, final Filter filter, final SearchResultsHandler handler,
			final OperationOptions options) {

		SearchResult result = null;

		
		if (connector.getSupportedOperations().contains(SearchApiOp.class)) {
			if (options.getPageSize() == null && options.getPagedResultsCookie() == null) {
				OperationOptionsBuilder builder = new OperationOptionsBuilder(options).setPageSize(DEFAULT_PAGE_SIZE)
						.setPagedResultsOffset(-1);

				final String[] cookies = new String[] { null };
				do {
					if (cookies[0] != null) {
						builder.setPagedResultsCookie(cookies[0]);
					}

					result = connector.search(objectClass, filter, new SearchResultsHandler() {

						@Override
						public void handleResult(final SearchResult result) {
							handler.handleResult(result);
							cookies[0] = result.getPagedResultsCookie();
						}

						@Override
						public boolean handle(final ConnectorObject connectorObject) {
							return handler.handle(connectorObject);
						}
					}, builder.build());
				} while (cookies[0] != null);
			} else {
				result = connector.search(objectClass, filter, handler, options);
			}
		} else {
			LOG.info("Search was attempted, although the connector only has these capabilities: {}. No action.",
					connector.getSupportedOperations());
		}
		 

		return result;
	}

	@Override
	public void dispose() {
		//remove final in connector and connInstance
		this.connector = null;
		this.connInstance = null;
		this.asyncFacade = null;
//		 connector.dispose();
	}

	@Override
	public ConnInstance getConnInstance() {
		return connInstance;
	}

	private static Object getPropertyValue(final String propType, final List<?> values) {
		Object value = null;

		try {
			Class<?> propertySchemaClass = ClassUtils.forName(propType, ClassUtils.getDefaultClassLoader());

			if (GuardedString.class.equals(propertySchemaClass)) {
				value = new GuardedString(values.get(0).toString().toCharArray());
			} else if (GuardedByteArray.class.equals(propertySchemaClass)) {
				value = new GuardedByteArray((byte[]) values.get(0));
			} else if (Character.class.equals(propertySchemaClass) || Character.TYPE.equals(propertySchemaClass)) {
				value = values.get(0) == null || values.get(0).toString().isEmpty() ? null
						: values.get(0).toString().charAt(0);
			} else if (Integer.class.equals(propertySchemaClass) || Integer.TYPE.equals(propertySchemaClass)) {
				value = Integer.parseInt(values.get(0).toString());
			} else if (Long.class.equals(propertySchemaClass) || Long.TYPE.equals(propertySchemaClass)) {
				value = Long.parseLong(values.get(0).toString());
			} else if (Float.class.equals(propertySchemaClass) || Float.TYPE.equals(propertySchemaClass)) {
				value = Float.parseFloat(values.get(0).toString());
			} else if (Double.class.equals(propertySchemaClass) || Double.TYPE.equals(propertySchemaClass)) {
				value = Double.parseDouble(values.get(0).toString());
			} else if (Boolean.class.equals(propertySchemaClass) || Boolean.TYPE.equals(propertySchemaClass)) {
				value = Boolean.parseBoolean(values.get(0).toString());
			} else if (URI.class.equals(propertySchemaClass)) {
				value = URI.create(values.get(0).toString());
			} else if (File.class.equals(propertySchemaClass)) {
				value = new File(values.get(0).toString());
			} else if (String[].class.equals(propertySchemaClass)) {
				value = values.toArray(new String[] {});
			} else {
				value = values.get(0) == null ? null : values.get(0).toString();
			}
		} catch (Exception e) {
			LOG.error("Invalid ConnConfProperty specified: {} {}", propType, values, e);
		}

		return value;
	}

	@Override
	public String toString() {
		return "ConnectorFacadeProxy{" + "connector=" + connector + '\n' + "capabitilies="
				+ connInstance.getCapabilities() + '}';
	}

}
