package com.ridm.connector.repository;

import com.ridm.connector.domain.Account;
import com.ridm.connector.domain.Application;
import org.springframework.data.repository.CrudRepository;

public interface AccountRepository extends CrudRepository<Account, Long> {
}
