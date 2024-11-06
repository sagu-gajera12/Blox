package org.example;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ApiRateLimiterTest {
  private ApiRateLimiter rateLimiter;

  @Before
  public void setUp() {
    rateLimiter = new ApiRateLimiter();
  }

  @Test
  public void apiRateLimitTest_whenTestCallsUnderLimit_shouldSuccess() {
    // Test that a client can make calls successfully under the limit
    String clientId = "ClientA";
    for (int i = 1; i <= 15; i++) {
      String response = rateLimiter.safeCall(clientId, "Call #" + i);
      assertEquals("API Response for input: Call #" + i, response);
    }
  }

  @Test
  public void apiRateLimitTest_whenTestRateLimitExceed_shouldFail() {
    // Test that exceeding the rate limit results in a penalty
    String clientId = "ClientA";
    for (int i = 1; i <= 15; i++) {
      rateLimiter.safeCall(clientId, "Call #" + i);
    }

    // Attempting the 16th call should trigger a penalty
    try {
      rateLimiter.safeCall(clientId, "Call #16");
      fail("Expected IllegalStateException for exceeding rate limit");
    } catch (IllegalStateException e) {
      assertTrue(e.getMessage().contains("Rate limit exceeded for client ClientA. Penalty applied for 1 minute."));
    }
  }

  @Test
  public void apiRateLimitTest_testPenaltyDuration() throws InterruptedException {
    // Test that the penalty lasts for the expected duration
    String clientId = "ClientA";
    for (int i = 1; i <= 15; i++) {
      rateLimiter.safeCall(clientId, "Call #" + i);
    }

    try {
      rateLimiter.safeCall(clientId, "Call #16"); // Trigger penalty
    }catch (Exception e) {

    }

    // Wait for less than penalty time and try again
    Thread.sleep(50000); // Sleep for 50 seconds
    try {
      rateLimiter.safeCall(clientId, "Call #17");
      fail("Expected IllegalStateException for penalty duration not elapsed");
    } catch (IllegalStateException e) {
      assertTrue(e.getMessage().contains("API is under penalty for client ClientA. Please try after"));
    }

    // Wait for the remainder of the penalty time
    Thread.sleep(70000); // Wait for 1 minute and 10 seconds to ensure the penalty is over

    // Now the call should succeed
    String response = rateLimiter.safeCall(clientId, "Call #18");
    assertEquals("API Response for input: Call #18", response);
  }

  @Test
  public void apiRateLimitTest_testIndependentClients_shouldSuccess() {
    // Test that different clients do not affect each other
    String clientA = "ClientA";
    String clientB = "ClientB";

    // Client A makes 15 successful calls
    for (int i = 1; i <= 15; i++) {
      String response = rateLimiter.safeCall(clientA, "Call #" + i);
      assertEquals("API Response for input: Call #" + i, response);
    }

    // Client B makes calls without penalty
    for (int i = 1; i <= 14; i++) {
      String response = rateLimiter.safeCall(clientB, "Call #" + i);
      assertEquals("API Response for input: Call #" + i, response);
    }

    // Client A now exceeds the limit and gets a penalty
    try {
      rateLimiter.safeCall(clientA, "Call #16");
      fail("Expected IllegalStateException for exceeding rate limit");
    } catch (IllegalStateException e) {
      assertTrue(e.getMessage().contains("Rate limit exceeded for client ClientA. Penalty applied for 1 minute."));
    }

    // Client B should still be able to make calls
    String response = rateLimiter.safeCall(clientB, "Call #15");
    assertEquals("API Response for input: Call #15", response);
  }
}
