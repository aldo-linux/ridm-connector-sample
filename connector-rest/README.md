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
@Service
public class PetApiDelegateImpl implements PetApiDelegate {

    private final NativeWebRequest request;

    public PetApiDelegateImpl(NativeWebRequest request) {
        this.request = request;
    }

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.ofNullable(request);
    }
}
```


2) ProvisioningApiDelegateImpl.java

```

```