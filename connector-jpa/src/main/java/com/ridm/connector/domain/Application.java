package com.ridm.connector.domain;

import javax.persistence.*;

@Entity
@Table(name="applications")
public class Application {

    @Id
    @Column(name="application_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(name="name")
    private String name;
    @Column(name="description")
    private String description;
    @ManyToOne
    @JoinColumn(name = "connection_id")
    private Connection connection;

    public Application() {
    }

    public Application(String name, String description, Connection connection) {
        this.name = name;
        this.description = description;
        this.connection = connection;
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

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Application{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", connection=" + connection +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Application that = (Application) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
