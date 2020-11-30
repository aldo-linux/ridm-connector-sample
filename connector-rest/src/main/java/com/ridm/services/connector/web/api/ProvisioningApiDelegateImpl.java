package com.ridm.services.connector.web.api;

import org.springframework.stereotype.Service;
import org.springframework.web.context.request.NativeWebRequest;

/*
 TODO: Implement this class ;-)
 */

@Service
public class ProvisioningApiDelegateImpl implements ProvisioningApi {

    private final NativeWebRequest request;

    public ProvisioningApiDelegateImpl(NativeWebRequest request) {
        this.request = request;
    }
}
