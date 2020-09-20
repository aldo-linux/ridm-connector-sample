package com.ridm.connector.domain;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name="accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="account_id")
    private Long id;

    @Column(name = "account_info") // JSON containing the information of the account
    private String accountInfo;

    @ManyToOne
    @JoinColumn(name = "application_id")
    private Application application;

    public Account() {
    }

    public Account(String accountInfo, Application application) {
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

    public String getAccountInfo() {
        return accountInfo;
    }

    public void setAccountInfo(String accountInfo) {
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
                Objects.equals(accountInfo, account.accountInfo) &&
                Objects.equals(application, account.application);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
