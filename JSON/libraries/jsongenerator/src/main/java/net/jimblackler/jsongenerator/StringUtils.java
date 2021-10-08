/* Copyright (C) 2021 – University of Mons, University Antwerpen for the modification over the range of characters that can be generated.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
