package com.Banking.Rbank.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.Banking.Rbank.Model.Customer;
import com.Banking.Rbank.Model.Transaction;
import com.Banking.Rbank.Repository.CustomerRepository;
import com.Banking.Rbank.Repository.TransactionRepository;

@Service
public class CustomerService {

	@Autowired
	private CustomerRepository cr;

	@Autowired
	private TransactionRepository transactionRepository;

	// 1. Create a new customer
	public Customer createCustomer(String name, String email, Double initialDeposit) {
		// Check if the email already exists
		if (cr.existsByEmail(email)) {
			throw new IllegalArgumentException("Email already in use");
		}

		// Generate unique account number
		String accountNumber = generateAccountNumber();

		// Create and save customer
		Customer customer = new Customer();
		customer.setName(name);
		customer.setEmail(email);
		customer.setBalance(initialDeposit);
		customer.setAccountNumber(accountNumber);

		return cr.save(customer);
	}

	// Helper method to generate a unique account number
	private String generateAccountNumber() {
		// Here we use a simple approach, but it could be more complex depending on
		// requirements
		return "ACC" + System.currentTimeMillis();
	}

	// 2.depositFunds
	public Double depositFunds(Long accountId, Double amount) {
		if (amount == null || amount <= 0) {
			throw new IllegalArgumentException("Deposit amount must be a positive value.");
		}

		Customer customer = cr.findById(accountId)
				.orElseThrow(() -> new IllegalArgumentException("Customer not found with ID: " + accountId));

		double updatedBalance = customer.getBalance() + amount;
		customer.setBalance(updatedBalance);

		cr.save(customer);

		return updatedBalance;
	}

	// 3. account balance
	public Double getAccountBalance(Long accountId) {
		Customer customer = cr.findById(accountId)
				.orElseThrow(() -> new IllegalArgumentException("Customer not found with ID: " + accountId));

		return customer.getBalance();
	}

	// withdraw
	public Double withdrawFunds(Long accountId, Double amount) {
		Customer customer = cr.findById(accountId)
				.orElseThrow(() -> new IllegalArgumentException("Customer not found with ID: " + accountId));

		if (amount > customer.getBalance()) {
			throw new IllegalArgumentException("Insufficient funds. Available balance: " + customer.getBalance());
		}

		customer.setBalance(customer.getBalance() - amount);
		cr.save(customer);

		return customer.getBalance();
	}

	// Point of sale (Purchase)
	public Map<String, Object> processPosPurchase(Long accountId, Double amount, String merchant) {
		Customer customer = cr.findById(accountId)
				.orElseThrow(() -> new IllegalArgumentException("Customer not found with ID: " + accountId));

		if (amount > customer.getBalance()) {
			throw new IllegalArgumentException("Insufficient funds. Available balance: " + customer.getBalance());
		}

		// Deduct the amount from the customer's balance
		customer.setBalance(customer.getBalance() - amount);
		cr.save(customer);

		// Create a purchase receipt
		Map<String, Object> receipt = new HashMap<>();
		receipt.put("transactionId", UUID.randomUUID().toString());
		receipt.put("accountId", accountId);
		receipt.put("merchant", merchant);
		receipt.put("amount", amount);
		receipt.put("remainingBalance", customer.getBalance());
		receipt.put("timestamp", LocalDateTime.now());

		return receipt;
	}

	// Online purchase
	public Map<String, Object> onlinePurchase(Long accountId, Double amount, String merchant) {
		// Fetch customer account
		Customer customer = cr.findById(accountId).orElseThrow(() -> new NoSuchElementException("Account not found"));

		// Check sufficient balance
		if (customer.getBalance() < amount) {
			throw new IllegalArgumentException("Insufficient funds for the purchase.");
		}

		// Deduct amount from balance
		customer.setBalance(customer.getBalance() - amount);
		cr.save(customer);

		// Create receipt details
		Map<String, Object> receipt = new HashMap<>();
		receipt.put("accountId", customer.getId());
		receipt.put("merchant", merchant);
		receipt.put("amount", amount);
		receipt.put("remainingBalance", customer.getBalance());
		receipt.put("timestamp", LocalDateTime.now());

		return receipt;
	}

	// Transactiom
	public List<Transaction> getTransactionHistory(Long customerId) {
		Customer customer = cr.findById(customerId)
				.orElseThrow(() -> new IllegalArgumentException("Customer not found"));

		return transactionRepository.findByCustomer_Id(customer.getId());
	}

	// Read customer by ID
	public Optional<Customer> getCustomerById(Long id) {
		return cr.findById(id);
	}

	// Update customer
	public Customer updateCustomer(Long id, String name, String email, Double balance) {
		Customer customer = cr.findById(id).orElseThrow(() -> new RuntimeException("Customer not found"));
		customer.setName(name);
		customer.setEmail(email);
		customer.setBalance(balance);
		return cr.save(customer);
	}

	// Delete customer
	public void deleteCustomer(Long id) {
		cr.deleteById(id);
	}

	// Custom Query Methods
	public List<Customer> findByName(String name) {
		return cr.findByName(name);
	}

	public Customer findCustomerByEmail(String email) {
		return cr.findCustomerByEmail(email);
	}

	public List<Customer> findCustomersWithBalanceGreaterThan(Double balance) {
		return cr.findCustomersWithBalanceGreaterThan(balance);
	}
}
