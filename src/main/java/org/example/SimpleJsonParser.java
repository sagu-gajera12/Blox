package org.example;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

public class SimpleJsonParser {

  public static Object parse(String json) {
    json = json.trim();
    if (json.startsWith("{")) {
      return parseObject(new JsonReader(json));
    } else if (json.startsWith("[")) {
      return parseArray(new JsonReader(json));
    } else {
      throw new IllegalArgumentException("Invalid JSON input.");
    }
  }

  private static Map<String, Object> parseObject(JsonReader reader) {
    reader.expect('{');
    Map<String, Object> map = new LinkedHashMap<>();

    while (reader.hasNext()) {
      reader.skipWhitespace();
      String key = reader.readString();
      reader.skipWhitespace();
      reader.expect(':');
      Object value = parseValue(reader);
      map.put(key, value);
      reader.skipWhitespace();
      if (reader.peek() == '}') {
        reader.next();
        return map;
      } else {
        reader.expect(',');
      }
    }
    throw new IllegalArgumentException("Unterminated JSON object.");
  }

  private static List<Object> parseArray(JsonReader reader) {
    reader.expect('[');
    List<Object> list = new ArrayList<>();

    while (reader.hasNext()) {
      reader.skipWhitespace();
      list.add(parseValue(reader));

      reader.skipWhitespace();
      if (reader.peek() == ']') {
        reader.next();
        return list;
      } else {
        reader.expect(',');
      }
    }
    throw new IllegalArgumentException("Unterminated JSON array.");
  }

  private static Object parseValue(JsonReader reader) {
    reader.skipWhitespace();
    char next = reader.peek();

    if (next == '"') {
      return reader.readString();
    } else if (next == '{') {
      return parseObject(reader);
    } else if (next == '[') {
      return parseArray(reader);
    } else if (Character.isDigit(next) || next == '-') {
      return reader.readNumber();
    } else if (next == 't' || next == 'f') {
      return reader.readBoolean();
    } else if (next == 'n') {
      return reader.readNull();
    } else {
      throw new IllegalArgumentException("Unexpected character: " + next);
    }
  }

  private static class JsonReader {
    private final String json;
    private int index;

    JsonReader(String json) {
      this.json = json;
      this.index = 0;
    }

    boolean hasNext() {
      return index < json.length();
    }

    char next() {
      return json.charAt(index++);
    }

    char peek() {
      return json.charAt(index);
    }

    void expect(char expected) {
      char actual = next();
      if (actual != expected) {
        throw new IllegalArgumentException("Expected '" + expected + "' but got '" + actual + "'");
      }
    }

    void skipWhitespace() {
      while (hasNext() && Character.isWhitespace(peek())) {
        index++;
      }
    }

    String readString() {
      expect('"');
      StringBuilder sb = new StringBuilder();
      while (hasNext()) {
        char c = next();
        if (c == '"') {
          return sb.toString();
        } else if (c == '\\') {
          char escaped = next();
          switch (escaped) {
            case '"': sb.append('"'); break;
            case '\\': sb.append('\\'); break;
            case '/': sb.append('/'); break;
            case 'b': sb.append('\b'); break;
            case 'f': sb.append('\f'); break;
            case 'n': sb.append('\n'); break;
            case 'r': sb.append('\r'); break;
            case 't': sb.append('\t'); break;
            default: throw new IllegalArgumentException("Invalid escape sequence: \\" + escaped);
          }
        } else {
          sb.append(c);
        }
      }
      throw new IllegalArgumentException("Unterminated string.");
    }

    Object readNumber() {
      StringBuilder sb = new StringBuilder();
      char firstChar = peek();
      if (firstChar == '-') {
        sb.append(next());
      }
      while (hasNext() && (Character.isDigit(peek()) || peek() == '.')) {
        sb.append(next());
      }
      String numberStr = sb.toString();
      if (numberStr.contains(".")) {
        return new BigDecimal(numberStr);
      } else {
        return new BigInteger(numberStr);
      }
    }

    Boolean readBoolean() {
      if (json.startsWith("true", index)) {
        index += 4;
        return true;
      } else if (json.startsWith("false", index)) {
        index += 5;
        return false;
      }
      throw new IllegalArgumentException("Expected boolean value.");
    }

    Object readNull() {
      if (json.startsWith("null", index)) {
        index += 4;
        return null;
      }
      throw new IllegalArgumentException("Expected null value.");
    }
  }

  public static void main(String[] args) {
    String jsonString = "{ \"name\": \"Alice\", \"age\": 30, \"isStudent\": false, \"scores\": [88.5, 90.0, 95.75], \"address\": {\"city\": \"Wonderland\", \"zip\": null} }";
    Object result = parse(jsonString);
  }
}

