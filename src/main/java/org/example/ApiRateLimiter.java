package org.example;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class ApiRateLimiter {
  private static final int MAX_CALLS_PER_MINUTE = 15;
  private static final long ONE_MINUTE = 60 * 1000L;
  private static final long PENALTY_TIME = 2 * ONE_MINUTE; // 1 minute wait + 1 penalty minute

  // Map to store call timestamps for each client
  private Map<String, ClientInfo> clientMap;

  public ApiRateLimiter() {
    clientMap = new HashMap<>();
  }

  public synchronized String safeCall(String clientId, String input) {
    long currentTime = System.currentTimeMillis();
    ClientInfo clientInfo = clientMap.getOrDefault(clientId, new ClientInfo());

    if (clientInfo.isPenalized) {
      if (currentTime < clientInfo.penaltyEndTime) {
        throw new IllegalStateException("API is under penalty for client " + clientId + ". Please try after " + (clientInfo.penaltyEndTime - currentTime) / 1000 + " seconds.");
      } else {
        clientInfo.isPenalized = false; // Penalty period has ended
      }
    }

    // Remove timestamps older than one minute
    while (!clientInfo.callTimestamps.isEmpty() && currentTime - clientInfo.callTimestamps.peek() > ONE_MINUTE) {
      clientInfo.callTimestamps.poll();
    }

    // Check if we can make a call
    if (clientInfo.callTimestamps.size() < MAX_CALLS_PER_MINUTE) {
      clientInfo.callTimestamps.add(currentTime);
      clientMap.put(clientId, clientInfo); // Update the client state
      return call_me(input); // Make the API call safely
    } else {
      // Trigger penalty
      triggerPenalty(clientInfo);
      throw new IllegalStateException("Rate limit exceeded for client " + clientId + ". Penalty applied for 1 minute.");
    }
  }

  private void triggerPenalty(ClientInfo clientInfo) {
    clientInfo.isPenalized = true;
    clientInfo.penaltyEndTime = System.currentTimeMillis() + PENALTY_TIME;
    clientInfo.callTimestamps.clear(); // Clear calls to reset after penalty
  }

  // Mock API function for demonstration
  public String call_me(String input) {
    return "API Response for input: " + input;
  }

  private static class ClientInfo {
    Queue<Long> callTimestamps;
    boolean isPenalized;
    long penaltyEndTime;

    ClientInfo() {
      callTimestamps = new LinkedList<>();
      isPenalized = false;
      penaltyEndTime = 0;
    }
  }

  public static void main(String[] args) {
  }
}
