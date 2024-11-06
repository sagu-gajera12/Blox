package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class DatabaseCheckpointTest {

  private boolean checkDataConsistency(List<Map<String, String>> originalData, List<Map<String, String>> copiedData) {
    return originalData.equals(copiedData);
  }

  @Test
  public void testCheckDataConsistency_WhenDataIsIdentical_ShouldReturnTrue() {
    // Given: Original data and copied data are the same
    List<Map<String, String>> originalData = new ArrayList<>();
    originalData.add(new HashMap<>(Map.of("id", "1", "name", "Alice", "age", "30")));
    originalData.add(new HashMap<>(Map.of("id", "2", "name", "Bob", "age", "25")));

    List<Map<String, String>> copiedData = new ArrayList<>();
    copiedData.add(new HashMap<>(Map.of("id", "1", "name", "Alice", "age", "30")));
    copiedData.add(new HashMap<>(Map.of("id", "2", "name", "Bob", "age", "25")));

    // When: Checking data consistency
    boolean isConsistent = checkDataConsistency(originalData, copiedData);

    // Then: The result should be true
    Assert.assertTrue(isConsistent);
  }

  @Test
  public void testCheckDataConsistency_WhenDataIsDifferent_ShouldReturnFalse() {
    // Given: Original data
    List<Map<String, String>> originalData = new ArrayList<>();
    originalData.add(new HashMap<>(Map.of("id", "1", "name", "Alice", "age", "30")));
    originalData.add(new HashMap<>(Map.of("id", "2", "name", "Bob", "age", "25")));

    // Copied data has a different age for Bob
    List<Map<String, String>> copiedData = new ArrayList<>();
    copiedData.add(new HashMap<>(Map.of("id", "1", "name", "Alice", "age", "30")));
    copiedData.add(new HashMap<>(Map.of("id", "2", "name", "Bob", "age", "26"))); // Age is different

    // When: Checking data consistency
    boolean isConsistent = checkDataConsistency(originalData, copiedData);

    // Then: The result should be false
    Assert.assertFalse(isConsistent);
  }

  @Test
  public void testCheckDataConsistency_WhenDataSizeIsDifferent_ShouldReturnFalse() {
    // Given: Original data
    List<Map<String, String>> originalData = new ArrayList<>();
    originalData.add(new HashMap<>(Map.of("id", "1", "name", "Alice", "age", "30")));
    originalData.add(new HashMap<>(Map.of("id", "2", "name", "Bob", "age", "25")));
    originalData.add(new HashMap<>(Map.of("id", "3", "name", "Alice", "age", "25")));


    // Copied data has a less records
    List<Map<String, String>> copiedData = new ArrayList<>();
    copiedData.add(new HashMap<>(Map.of("id", "1", "name", "Alice", "age", "30")));
    copiedData.add(new HashMap<>(Map.of("id", "2", "name", "Bob", "age", "25"))); // Size is different

    // When: Checking data consistency
    boolean isConsistent = checkDataConsistency(originalData, copiedData);

    // Then: The result should be false
    Assert.assertFalse(isConsistent);
  }

  @Test
  public void testCheckDataConsistency_WhenDataKeyIsDifferent_ShouldReturnFalse() {
    // Given: Original data
    List<Map<String, String>> originalData = new ArrayList<>();
    originalData.add(new HashMap<>(Map.of("id", "1", "name", "Alice", "age", "30")));
    originalData.add(new HashMap<>(Map.of("id", "2", "name", "Bob", "age", "25")));

    // Copied data has a wrong key
    List<Map<String, String>> copiedData = new ArrayList<>();
    copiedData.add(new HashMap<>(Map.of("id", "1", "name", "Alice", "age", "30")));
    copiedData.add(new HashMap<>(Map.of("id", "2", "name1", "Bob", "age", "25")));

    // When: Checking data consistency
    boolean isConsistent = checkDataConsistency(originalData, copiedData);

    // Then: The result should be true
    Assert.assertFalse(isConsistent);
  }
}
