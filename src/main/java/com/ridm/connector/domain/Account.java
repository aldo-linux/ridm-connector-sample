package com.ridm.connector.domain;

import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name="accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="account_id")
    private Long id;

    @Type(type="json-node")
    @Column(name="account_info", columnDefinition = "JSON")
    private JsonNode accountInfo;

    @ManyToOne
    @JoinColumn(name = "application_id")
    private Application application;

    public Account() {
    }

    public Account(JsonNode accountInfo, Application application) {
        this.id = id;
        this.accountInfo = accountInfo;
        this.application = application;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public JsonNode getAccountInfo() {
        return accountInfo;
    }

    public void setAccountInfo(JsonNode accountInfo) {
        this.accountInfo = accountInfo;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return id.equals(account.id) &&
                Objects.equals(application, account.application);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
