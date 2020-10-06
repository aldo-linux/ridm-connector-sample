package com.ridm.connector.repository;

import com.ridm.connector.domain.Application;
import org.springframework.data.repository.CrudRepository;

public interface ApplicationRepository extends CrudRepository<Application, Long> {
}
