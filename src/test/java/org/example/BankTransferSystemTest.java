package org.example;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class BankTransferSystemTest {

  private BankTransferSystem bankTransferSystem;

  public void setUp() {
    // Initialize the bank transfer system with some test accounts
    Map<String, Account> accountMap = new HashMap<>();
    accountMap.put("A", new Account("A", 1000.0));
    accountMap.put("B", new Account("B", 500.0));
    bankTransferSystem = new BankTransferSystem(accountMap);
  }

  @Test
  public void testTransfer_Success() {
    // Positive test case: Transfer should succeed
    setUp();


    boolean result = bankTransferSystem.transfer("A", "B", 200);
    Assert.assertTrue(result);

    assertEquals( 800.0, bankTransferSystem.getAccount("A").getBalance(), 0);
    assertEquals(700, bankTransferSystem.getAccount("B").getBalance(), 0);

  }

  @Test
  public void testTransfer_InsufficientFunds() {
    // Negative test case: Transfer should fail due to insufficient funds
    setUp();
    boolean result = bankTransferSystem.transfer("A", "B", 1200);
    Assert.assertFalse(result);
    assertEquals(1000, bankTransferSystem.getAccount("A").getBalance(), 0);
  }

  @Test
  public void testTransfer_AccountNotFound() {
    // Negative test case: Transfer should fail if sender account is not found
    setUp();
    try {
      bankTransferSystem.transfer("C", "B", 100);
      Assert.fail("Expected IllegalArgumentException for non-existent sender account");
    } catch (IllegalArgumentException e) {
      assertEquals("Account not found.", e.getMessage());
    }

    // Negative test case: Transfer should fail if receiver account is not found
    try {
      bankTransferSystem.transfer("A", "C", 100);
      Assert.fail("Expected IllegalArgumentException for non-existent receiver account");
    } catch (IllegalArgumentException e) {
      assertEquals("Account not found.", e.getMessage());
    }
  }

  @Test
  public void testTransfer_ZeroAmount() {
    setUp();
    // Positive test case: Transfer of zero amount should succeed
    setUp();
    try {
      bankTransferSystem.transfer("A", "B", 0);
      Assert.fail("Expected IllegalArgumentException for negative amount transfer");
    } catch (IllegalArgumentException e) {
      assertEquals("Amount must be positive.", e.getMessage());
    }
  }

  @Test
  public void testTransfer_NegativeAmount() {
    // Negative test case: Transfer of negative amount should fail
    setUp();
    try {
      bankTransferSystem.transfer("A", "B", -100);
      Assert.fail("Expected IllegalArgumentException for negative amount transfer");
    } catch (IllegalArgumentException e) {
      assertEquals("Amount must be positive.", e.getMessage());
    }
  }

  @Test
  public void testTransfer_RollbackOnCommitFailure() {

    Account receiver = mock(Account.class);
    bankTransferSystem = new BankTransferSystem(new HashMap<>() {{
      put("A", new Account("A", 1000.0));
      put("B", receiver);
    }});

    when(bankTransferSystem.getAccount("B").commitCredit(anyDouble())).thenReturn(false);

    // Attempt to transfer from A to B which should fail
    boolean result = bankTransferSystem.transfer("A", "B", 200);
    Assert.assertFalse(result);
    assertEquals(1000, bankTransferSystem.getAccount("A").getBalance(), 0);
  }

  @Test
  public void testTransfer_ReserveReleaseOnRollback() {
    // Test to ensure reserved amount is cleared on rollback
    setUp();
    bankTransferSystem.transfer("A", "B", 200); // Successful transfer
    // Now we should ensure that account A's balance is 800
    // You would need to add a method to retrieve the account balance.
    Account accountA = bankTransferSystem.getAccount("A");
    assertEquals(800.0, accountA.getBalance(), 0.01);
  }

  @Test
  public void testTransfer_whenMultipleAccountTryingToTransferMoneyToSameAccount_AllMoneyShouldCredit()
      throws InterruptedException {
    Map<String, Account> accountMap = new HashMap<>();
    accountMap.put("A", new Account("A", 10000.0));
    accountMap.put("B", new Account("B", 500.0));
    accountMap.put("C", new Account("C", 10000));
    bankTransferSystem = new BankTransferSystem(accountMap);

    Thread t1 = new Thread(() -> {
      for(int i = 1; i <= 5000; i++) {
        bankTransferSystem.transfer("A", "B", 1);
      }
    });

    Thread t2 = new Thread(() -> {
      for(int i = 1; i <= 5000; i++) {
        bankTransferSystem.transfer("A", "B", 1);
      }
    });

    Thread t3 = new Thread(() -> {
      for(int i = 1; i <= 5000; i++) {
        bankTransferSystem.transfer("C", "B", 1);
      }
    });

    Thread t4 = new Thread(() -> {
      for(int i = 1; i <= 5000; i++) {
        bankTransferSystem.transfer("C", "B", 1);
      }
    });


    t1.start();
    t2.start();
    t3.start();
    t4.start();

    t1.join();
    t2.join();
    t3.join();
    t4.join();

    assertEquals(0, bankTransferSystem.getAccount("A").getBalance(), 0);
    assertEquals(20500, bankTransferSystem.getAccount("B").getBalance(), 0);
    assertEquals(0, bankTransferSystem.getAccount("C").getBalance(), 0);
  }
}
