package com.ridm.connid.connector;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface ConnInstance  {


    void setConnectorName(String connectorName);

    String getConnectorName();

    void setDisplayName(String displayName);

    String getDisplayName();

    void setLocation(String location);

    String getLocation();

    void setVersion(String version);

    String getVersion();

    void setBundleName(String bundleName);

    String getBundleName();

    Set<String> getCapabilities();

    boolean add(String resource);

    List<? extends String> getResources();

    void setConf(Collection<String> conf);

    Set<String> getConf();

    void setConnRequestTimeout(Integer timeout);

    Integer getConnRequestTimeout();

}
