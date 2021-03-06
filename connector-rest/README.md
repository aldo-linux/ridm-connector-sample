## Generate sources
```
./mvnw generate-sources
```

## Run
```
./mvnw spring-boot:run
```

## Code Generation Explanation

The generate-sources command will generate the code in the directory: "target/source/java". Since we have specify we want to generate the code using the "delegatePattern>true</delegatePattern>" option, it would generate the Java Interfaces below:

- ConnectorApiDelegate.java
- ProvisioningApiDelegate.java

Now, we only need to implement the classes accourdingly:

1) ConnectorApiDelegateImpl.java

```
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

```

2) ProvisioningApiDelegateImpl.java

```

```