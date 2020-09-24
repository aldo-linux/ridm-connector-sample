package com.ridm.connector.domain;

import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name="groups")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="group_id")
    private Long id;

    @Type(type="json-node")
    @Column(name="group_info", columnDefinition = "JSON")
    private JsonNode groupInfo;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    public Group() {
    }

    public Group(JsonNode groupInfo) {
        this.groupInfo = groupInfo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public JsonNode getGroupInfo() {
        return groupInfo;
    }

    public void setGroupInfo(JsonNode groupInfo) {
        this.groupInfo = groupInfo;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Group group = (Group) o;
        return id.equals(group.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
