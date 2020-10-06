package com.ridm.connid.connector;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.OperationOptionsBuilder;
import org.identityconnectors.framework.common.objects.SyncResultsHandler;
import org.identityconnectors.framework.common.objects.SyncToken;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.SearchResult;
import org.identityconnectors.framework.spi.SearchResultsHandler;

/**
 * Entry point for making requests on underlying connector bundles.
 */
public interface Connector {

    /**
     * Authenticate user on a connector instance.
     *
     * @param username the name based credential for authentication
     * @param password the password based credential for authentication
     * @param options ConnId's OperationOptions
     * @return Uid of the account that was used to authenticate
     */
    Uid authenticate(String username, String password, OperationOptions options);

    /**
     * Create user / group on a connector instance.
     *
     * @param objectClass ConnId's object class
     * @param attrs attributes for creation
     * @param options ConnId's OperationOptions
     * @param propagationAttempted if creation is actually performed (based on connector instance's capabilities)
     * @return Uid for created object
     */
    Uid create(
            ObjectClass objectClass,
            Set<Attribute> attrs,
            OperationOptions options,
            AtomicReference<Boolean> propagationAttempted);

    /**
     * Update user / group on a connector instance.
     *
     * @param objectClass ConnId's object class
     * @param uid user to be updated
     * @param attrs attributes for update
     * @param options ConnId's OperationOptions
     * @param propagationAttempted if creation is actually performed (based on connector instance's capabilities)
     * @return Uid for updated object
     */
    Uid update(
            ObjectClass objectClass,
            Uid uid,
            Set<Attribute> attrs,
            OperationOptions options,
            AtomicReference<Boolean> propagationAttempted);

    /**
     * Delete user / group on a connector instance.
     * @param objectClass ConnId's object class
     * @param uid user to be deleted
     * @param options ConnId's OperationOptions
     * @param propagationAttempted if deletion is actually performed (based on connector instance's capabilities)
     * @return A flag to check if account was deleted or not
     */
    Boolean delete(
            ObjectClass objectClass,
            Uid uid,
            OperationOptions options,
            AtomicReference<Boolean> propagationAttempted);

    /**
     * Fetches all remote objects (for use during full reconciliation).
     *
     * @param objectClass ConnId's object class.
     * @param handler to be used to handle deltas.
     * @param options ConnId's OperationOptions.
     */
    default void fullReconciliation(ObjectClass objectClass, SyncResultsHandler handler, OperationOptions options) {
        //filteredReconciliation(objectClass, null, handler, options);
    }

    /**
     * Fetches remote objects (for use during filtered reconciliation).
     *
     * @param objectClass ConnId's object class.
     * @param filterBuilder reconciliation filter builder
     * @param handler to be used to handle deltas.
     * @param options ConnId's OperationOptions.
     */
/*
    default void filteredReconciliation(
            ObjectClass objectClass,
           ReconFilterBuilder filterBuilder,
            SyncResultsHandler handler,
            OperationOptions options) {

        Filter filter = null;
        OperationOptions actualOptions = options;
        if (filterBuilder != null) {
            filter = filterBuilder.build();
            actualOptions = filterBuilder.build(actualOptions);
        }

        search(objectClass, filter, new SearchResultsHandler() {

            @Override
            public void handleResult(final SearchResult result) {
                // nothing to do
            }

            @Override
            public boolean handle(final ConnectorObject object) {
                return handler.handle(new SyncDeltaBuilder().
                        setObject(object).
                        setDeltaType(SyncDeltaType.CREATE_OR_UPDATE).
                        setToken(new SyncToken("")).
                        build());
            }
        }, actualOptions);
    }
*/
    /**
     * Sync remote objects from a connector instance.
     *
     * @param objectClass ConnId's object class
     * @param token to be passed to the underlying connector
     * @param handler to be used to handle deltas
     * @param options ConnId's OperationOptions
     */
    void sync(ObjectClass objectClass, SyncToken token, SyncResultsHandler handler, OperationOptions options);

    /**
     * Read latest sync token from a connector instance.
     *
     * @param objectClass ConnId's object class.
     * @return latest sync token
     */
    SyncToken getLatestSyncToken(ObjectClass objectClass);

    /**
     * Get remote object.
     *
     * @param objectClass ConnId's object class
     * @param connObjectKey ConnId's key attribute
     * @param ignoreCaseMatch whether match should be performed regardless of the value case
     * @param options ConnId's OperationOptions
     * @return ConnId's connector object for given uid
     */
    ConnectorObject getObject(
            ObjectClass objectClass,
            Attribute connObjectKey,
            boolean ignoreCaseMatch,
            OperationOptions options);

    /**
     * Search for remote objects.
     *
     * @param objectClass ConnId's object class
     * @param filter search filter
     * @param handler class responsible for working with the objects returned from the search; may be null.
     * @param options ConnId's OperationOptions
     * @return search result
     */
    SearchResult search(
            ObjectClass objectClass,
            Filter filter,
            SearchResultsHandler handler,
            OperationOptions options);

    /**
     * Search for remote objects.
     *
     * @param objectClass ConnId's object class
     * @param filter search filter
     * @param handler class responsible for working with the objects returned from the search; may be null.
     * @param pageSize requested page results page size
     * @param pagedResultsCookie an opaque cookie which is used by the connector to track its position in the set of
     * query results
     * @param orderBy the sort keys which should be used for ordering the {@link ConnectorObject} returned by
     * search request
     * @param options ConnId's OperationOptions
     * @return search result
     */
    default SearchResult search(
            ObjectClass objectClass,
            Filter filter,
            SearchResultsHandler handler,
            int pageSize,
            String pagedResultsCookie,
//            List<OrderByClause> orderBy,
            OperationOptions options) {

        OperationOptionsBuilder builder = new OperationOptionsBuilder().setPageSize(pageSize).setPagedResultsOffset(-1);
        if (pagedResultsCookie != null) {
            builder.setPagedResultsCookie(pagedResultsCookie);
        }
//        builder.setSortKeys(orderBy.stream().
 //               map(clause -> new SortKey(clause.getField(), clause.getDirection() == OrderByClause.Direction.ASC)).
//                collect(Collectors.toList()));

        builder.setAttributesToGet(options.getAttributesToGet());

        return search(objectClass, filter, handler, builder.build());
    }

    /**
     * Builds metadata description of ConnId {@link ObjectClass}.
     *
     * @return metadata description of ConnId ObjectClass
     */
    Map<Object, Object> getObjectClassInfo();

    /**
     * Validate connector instance.
     * @return A flag to know if configuration is valid
     */
    boolean validate();

    /**
     * Check connection.
     * @return A flag to know if test is successful or not
     */
    boolean test();

    /**
     * Dispose of any resources associated with connector instance.
     */
    void dispose();

    /**
     * Getter for active connector instance.
     *
     * @return active connector instance.
     */
    ConnInstance getConnInstance();
}
