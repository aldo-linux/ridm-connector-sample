package com.ridm.connector.domain;

import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import javax.persistence.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import com.vladmihalcea.hibernate.type.json.*;

@Entity
@Table(name="connections")
public class Connection {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="connection_id")
    private Long id;
    @Column(name="name")
    private String name;
    @Column(name="description")
    private String description;
    @Type(type="json-node")
    @Column(name="config_properties", columnDefinition = "JSON")
    private JsonNode configProperties;

    public Connection() {
    }

    public Connection(String name, String description, JsonNode configProperties) {
        this.name = name;
        this.description = description;
        this.configProperties = configProperties;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public JsonNode getConfigProperties() {
        return configProperties;
    }

    public void setConfigProperties(JsonNode configProperties) {
        this.configProperties = configProperties;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Connection{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", configProperties='" + configProperties.asText() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Connection that = (Connection) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
