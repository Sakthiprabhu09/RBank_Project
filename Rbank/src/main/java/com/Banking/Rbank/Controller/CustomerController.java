package com.Banking.Rbank.Controller;



import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Banking.Rbank.Model.Customer;
import com.Banking.Rbank.Model.Transaction;
import com.Banking.Rbank.Service.CustomerService;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

	@Autowired
	private CustomerService customerService;

	@PostMapping
	public ResponseEntity<Customer> createCustomer(@RequestBody Customer createCustomerRequest) {
		try {
			Customer customer = customerService.createCustomer(createCustomerRequest.getName(),
					createCustomerRequest.getEmail(), createCustomerRequest.getBalance());
			return new ResponseEntity<>(customer, HttpStatus.CREATED);
		} catch (IllegalArgumentException e) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST); // Error: Email already in use
		} catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR); // General error
		}
	}

	@PostMapping("/{accountId}/deposit")
	public ResponseEntity<?> depositFunds(@PathVariable Long accountId, @RequestBody Map<String, Double> requestBody) {
		try {
			Double amount = requestBody.get("amount");
			Double updatedBalance = customerService.depositFunds(accountId, amount);
			return ResponseEntity.ok(Map.of("updatedBalance", updatedBalance));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@GetMapping("/{accountId}/balance")
	public ResponseEntity<?> checkAccountBalance(@PathVariable Long accountId) {
		try {
			Double balance = customerService.getAccountBalance(accountId);
			return ResponseEntity.ok(Map.of("balance", balance));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@PostMapping("/{accountId}/withdraw")
	public ResponseEntity<?> withdrawFunds(@PathVariable Long accountId, @RequestBody Map<String, Double> request) {
		try {
			Double amount = request.get("amount");
			if (amount == null || amount <= 0) {
				throw new IllegalArgumentException("Withdrawal amount must be greater than zero.");
			}
			Double updatedBalance = customerService.withdrawFunds(accountId, amount);
			return ResponseEntity.ok(Map.of("updatedBalance", updatedBalance));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@PostMapping("/{accountId}/pos-purchase")
	public ResponseEntity<?> posPurchase(@PathVariable Long accountId, @RequestBody Map<String, Object> request) {
		try {
			Double amount = ((Number) request.get("amount")).doubleValue();
			String merchant = (String) request.get("merchant");

			if (amount == null || amount <= 0) {
				throw new IllegalArgumentException("Purchase amount must be greater than zero.");
			}
			if (merchant == null || merchant.isEmpty()) {
				throw new IllegalArgumentException("Merchant name must be provided.");
			}

			Map<String, Object> receipt = customerService.processPosPurchase(accountId, amount, merchant);
			return ResponseEntity.ok(receipt);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@PostMapping("/{accountId}/online-purchase")
	public ResponseEntity<?> onlinePurchase(@PathVariable Long accountId, @RequestBody Map<String, Object> request) {
		try {
			// Extract amount and merchant from request body
			Double amount = (Double) request.get("amount");
			String merchant = (String) request.get("merchant");

			if (amount == null || amount <= 0) {
				throw new IllegalArgumentException("Purchase amount must be greater than zero.");
			}
			if (merchant == null || merchant.isEmpty()) {
				throw new IllegalArgumentException("Merchant name is required.");
			}

			// Perform online purchase
			Map<String, Object> purchaseDetails = customerService.onlinePurchase(accountId, amount, merchant);

			return new ResponseEntity<>(purchaseDetails, HttpStatus.OK);
		} catch (IllegalArgumentException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		} catch (NoSuchElementException e) {
			return new ResponseEntity<>("Account not found.", HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			return new ResponseEntity<>("An error occurred while processing the purchase.",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/{accountId}/transactions")
	public ResponseEntity<List<Transaction>> getTransactionHistory(@PathVariable Long accountId) {
		try {
			List<Transaction> transactions = customerService.getTransactionHistory(accountId);
			return new ResponseEntity<>(transactions, HttpStatus.OK);
		} catch (IllegalArgumentException e) {
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
	}

	@GetMapping("/{id}")
	public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
		return customerService.getCustomerById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	// Update customer
	@PutMapping("/{id}")
	public ResponseEntity<Customer> updateCustomer(@PathVariable Long id, @RequestBody Customer customer) {
		Customer updatedCustomer = customerService.updateCustomer(id, customer.getName(), customer.getEmail(),
				customer.getBalance());
		return ResponseEntity.ok(updatedCustomer);
	}

	// Delete customer
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
		customerService.deleteCustomer(id);
		return ResponseEntity.noContent().build();
	}

	// Find customers by name (Spring Data JPA method)
	@GetMapping("/name/{name}")
	public ResponseEntity<List<Customer>> findByName(@PathVariable String name) {
		return ResponseEntity.ok(customerService.findByName(name));
	}

	// Find customer by email (JPQL query)
	@GetMapping("/email/{email}")
	public ResponseEntity<Customer> findCustomerByEmail(@PathVariable String email) {
		return ResponseEntity.ok(customerService.findCustomerByEmail(email));
	}

	// Find customers with balance greater than (Native query)
	@GetMapping("/balance/{balance}")
	public ResponseEntity<List<Customer>> findCustomersWithBalanceGreaterThan(@PathVariable Double balance) {
		return ResponseEntity.ok(customerService.findCustomersWithBalanceGreaterThan(balance));
	}
}

