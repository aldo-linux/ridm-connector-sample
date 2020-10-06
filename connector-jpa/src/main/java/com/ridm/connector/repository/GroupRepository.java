package com.ridm.connector.repository;

import com.ridm.connector.domain.Application;
import com.ridm.connector.domain.Group;
import org.springframework.data.repository.CrudRepository;

public interface GroupRepository extends CrudRepository<Group, Long> {
}
