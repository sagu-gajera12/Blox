package org.example;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DBCheckPoint {

  public static void main(String[] args) {
    // Sample data for original and migrated datasets
    List<Map<String, String>> originalData = Arrays.asList(
        Map.of("id", "1", "name", "Alice", "age", "30"),
        Map.of("id", "2", "name", "Bob", "age", "25")
    );

    List<Map<String, String>> migratedData = Arrays.asList(
        Map.of("id", "1", "name", "Alice", "age", "30"),
        Map.of("id", "2", "name", "Bob", "age", "25")
    );

    // Run validation
    if (validateDataIntegrity(originalData, migratedData)) {
      System.out.println("Data integrity check passed!");
    } else {
      System.out.println("Data integrity check failed!");
    }
  }

  public static boolean validateDataIntegrity(List<Map<String, String>> originalData, List<Map<String, String>> migratedData) {
    // Row Count Check
    if (originalData.size() != migratedData.size()) {
      System.out.println("Row count does not match!");
      return false;
    }

    // Row-by-Row Hash Check
    for (int i = 0; i < originalData.size(); i++) {
      String originalHash = hashRow(originalData.get(i));
      String migratedHash = hashRow(migratedData.get(i));

      if (!originalHash.equals(migratedHash)) {
        System.out.println("Data mismatch found in row " + (i + 1));
        System.out.println("Original Row: " + originalData.get(i));
        System.out.println("Migrated Row: " + migratedData.get(i));
        return false;
      }
    }
    return true;
  }

  private static String hashRow(Map<String, String> rowData) {
    // Concatenate values in sorted order to ensure consistent hashing
    StringBuilder concatenatedValues = new StringBuilder();
    rowData.keySet().stream().sorted().forEach(key -> {
      concatenatedValues.append(key);
      concatenatedValues.append(rowData.get(key));
    });

    return generateHash(concatenatedValues.toString());
  }

  private static String generateHash(String input) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] hashBytes = md.digest(input.getBytes());
      StringBuilder sb = new StringBuilder();

      for (byte b : hashBytes) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("Hashing algorithm not found", e);
    }
  }
}