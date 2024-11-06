package org.example;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BankTransferSystem {

  private final Map<String, Account> accounts = new HashMap<>();

  public BankTransferSystem() {
    // Create accounts for testing
      accounts.put("A", new Account("A", 1000.0));
    accounts.put("B", new Account("B", 500.0));
  }

  public BankTransferSystem(Map<String, Account> accounts) {
    this.accounts.putAll(accounts);
  }

  // Transfer money using 2PC protocol simulation
  public boolean transfer(String fromAccount, String toAccount, double amount) {
    Account sender = accounts.get(fromAccount);
    Account receiver = accounts.get(toAccount);

    if (sender == null || receiver == null) {
      throw new IllegalArgumentException("Account not found.");
    }

    if(amount <= 0) {
      throw new IllegalArgumentException("Amount must be positive.");
    }

    // Step 1: Prepare phase
    if (!sender.prepareDebit(amount)) {
      System.out.println("Insufficient funds or preparation failed for " + fromAccount);
      return false;
    }

    // Step 2: Commit phase - In a real system, this would involve distributed communication
    if (!receiver.commitCredit(amount)) {
      sender.rollbackDebit(amount); // Rollback if commit fails
      System.out.println("Transfer failed. Rolled back transaction.");
      return false;
    }

//    System.out.println("Transfer completed successfully from " + fromAccount + " to " + toAccount);
    return true;
  }

  public static void main(String[] args) {
    BankTransferSystem system = new BankTransferSystem();

    // Test transfer - success
    system.transfer("A", "B", 200);

    // Test transfer - insufficient funds
    system.transfer("A", "B", 1200);
  }

  public Account getAccount(String account) {
    return this.accounts.get(account);
  }
}

class Account {
  private final String accountId;
  private double balance;
  private final Lock lock = new ReentrantLock();
  private double reservedAmount = 0; // Reserved for 2PC prepare phase

  public Account(String accountId, double balance) {
    this.accountId = accountId;
    this.balance = balance;
  }

  // Prepare phase for debit - reserve the amount
  public boolean prepareDebit(double amount) {
    lock.lock();
    try {
      if (balance < amount) return false;
      balance -= amount;
      reservedAmount = amount; // Reserve amount for transaction
      return true;
    } finally {
      lock.unlock();
    }
  }

  // Commit phase for credit - add amount to balance
  public boolean commitCredit(double amount) {
    lock.lock();
    try {
      balance += amount;
      return true;
    } finally {
      lock.unlock();
    }
  }

  // Rollback phase for debit - release reserved amount
  public void rollbackDebit(double amount) {
    lock.lock();
    try {
      balance += reservedAmount;
      reservedAmount = 0; // Clear reservation
    } finally {
      lock.unlock();
    }
  }

  @Override
  public String toString() {
    return "Account{" + "accountId='" + accountId + '\'' + ", balance=" + balance + '}';
  }

  public double getBalance() {
    return balance;
  }
}
