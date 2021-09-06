package net.jimblackler.jsongenerator;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Random;

public class StringUtils {
  static String randomString(Random random, int minLength, int maxLength) {
    if (maxLength <= minLength) {
      throw new IllegalStateException();
    }
    StringBuilder stringBuilder = new StringBuilder();
    int length = random.nextInt(maxLength - minLength) + minLength;
    for (int idx = 0; idx != length; idx++) {
      char character;
      double d = random.nextDouble();
      if (d < 0.33) {
        // random lowercase
        character = (char) (random.nextInt(2) + 'a');
      }
      else if (d < 0.66) {
        // random uppercase
        character = (char) (random.nextInt(2) + 'A');
      }
      else {
        // random digit
        character = (char) (random.nextInt(1) + '0');
      }
      stringBuilder.append(character);
    }
    // We don't aim to handle strings that won't survive URL encoding with standard methods.
    String urlEncoded = URLEncoder.encode(stringBuilder.toString());
    return URLDecoder.decode(urlEncoded);
  }
}
