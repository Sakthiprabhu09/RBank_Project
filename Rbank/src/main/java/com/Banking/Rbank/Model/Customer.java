package com.Banking.Rbank.Model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "customers1")
public class Customer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "accountNumber", nullable = false, unique = true)
	private String accountNumber;

	@Column(name = "email", nullable = false, unique = true)
	private String email;

	@Column(name = "balance", nullable = false)
	private Double balance;

	@OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Transaction> transactions = new ArrayList<>();

//	@Override
//	public String toString() {
//		return "Customer [id=" + id + ", name=" + name + ", accountNumber=" + accountNumber + ", email=" + email
//				+ ", balance=" + balance + "]";
//	}

	public Customer() {
		super();
	}

	public Customer(Long id, String name, String accountNumber, String email, Double balance) {
		super();
		this.id = id;
		this.name = name;
		this.accountNumber = accountNumber;
		this.email = email;
		this.balance = balance;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Double getBalance() {
		return balance;
	}

	public void setBalance(Double balance) {
		this.balance = balance;
	}

}
