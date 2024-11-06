package org.example.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;

public class JsonToGenericObject {
  public static Object parseJson(String jsonString) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();

      if (jsonString.trim().startsWith("[")) {
        return objectMapper.readValue(jsonString, new TypeReference<List<Object>>() {});
      } else {
        return objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
      }
    } catch (Exception e) {
      e.printStackTrace();
      return e; // Return null or handle the exception as needed
    }
  }
}
