package com.Banking.Rbank.Repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.Banking.Rbank.Model.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
	public boolean existsByEmail(String email);

	// Spring Data JPA Method
	List<Customer> findByName(String name);

	// JPQL Query
	@Query("SELECT c FROM Customer c WHERE c.email = :email")
	Customer findCustomerByEmail(String email);

	// Native Query
	@Query(value = "SELECT * FROM customers1 WHERE balance > :balance", nativeQuery = true)
	List<Customer> findCustomersWithBalanceGreaterThan(Double balance);

}
