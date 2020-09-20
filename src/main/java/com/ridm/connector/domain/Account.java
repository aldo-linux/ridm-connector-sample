package com.ridm.connector.domain;

import javax.persistence.*;

@Entity
@Table(name="accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="account_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "application_id")
    private Application application;


}
