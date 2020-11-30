package com.ridm.services.connector.web.api;

import com.ridm.services.connector.web.api.model.ConnectorProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.List;

/*
 TODO: Implement this class ;-)
 */
@Service
public class ConnectorApiDelegateImpl implements ConnectorApiDelegate {

    private final NativeWebRequest request;

    public ConnectorApiDelegateImpl(NativeWebRequest request) {
        this.request = request;
    }

    /*
     TODO: This is an example on how to override a method
     */
    @Override
    public ResponseEntity<List<ConnectorProperties>> getAllConnectors() {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType: MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString = "{ \"connectorRef\" : { \"key\" : \"connectorRef\" }, \"operationOptions\" : \"{}\", \"connectorId\" : \"046b6c7f-0b8a-43b9-b35d-6489e6daee91\", \"operationTimeout\" : { \"key\" : 0 }, \"name\" : \"name\", \"syncFailureHandler\" : \"{}\", \"configurationProperties\" : { \"key\" : \"configurationProperties\" }, \"objectTypes\" : \"{}\", \"resultsHandlerConfig\" : { \"key\" : true }, \"poolConfigOption\" : { \"key\" : 6 } }";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
            }
        });

        return new ResponseEntity<>(HttpStatus.OK);

    }
}
