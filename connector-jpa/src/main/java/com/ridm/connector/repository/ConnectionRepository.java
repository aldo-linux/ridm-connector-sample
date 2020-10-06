package com.ridm.connector.repository;

import com.ridm.connector.domain.Connection;
import org.springframework.data.repository.CrudRepository;

public interface ConnectionRepository extends CrudRepository<Connection, Long> {
}
