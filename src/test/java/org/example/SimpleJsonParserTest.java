package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.util.JsonToGenericObject;
import org.junit.Assert;
import org.junit.Test;

public class SimpleJsonParserTest {

  @Test
  public void jsonParser_WhenJsonStringIsValid_shouldConvert() throws JsonProcessingException {
    String jsonString = "{ \"name\": \"Alice\", \"age\": 30, \"isStudent\": false, \"scores\": [88.5, 90.0, 95.75], \"address\": {\"city\": \"Wonderland\", \"zip\": null} }";
    Object expected = JsonToGenericObject.parseJson(jsonString);

    Object actual = SimpleJsonParser.parse(jsonString);

    // Convert both to JSON strings for comparison
    ObjectMapper objectMapper = new ObjectMapper();
    String expectedJson = objectMapper.writeValueAsString(expected);
    String actualJson = objectMapper.writeValueAsString(actual);

    // Assert that the JSON strings are equal
    Assert.assertEquals(expectedJson, actualJson);
  }

  @Test(expected = IllegalArgumentException.class)
  public void jsonParser_whenJsonStringIsInValid_shouldResponseError() {
    String invalidJsonString = "{ name: \"Alice\", age: 30, isStudent: false }"; // Missing quotes around keys
    Object actual = SimpleJsonParser.parse(invalidJsonString);
  }

}
