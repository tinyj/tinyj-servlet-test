/*
Copyright 2016 Eric Karge <e.karge@struction.de>

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package org.tinyj.test.servlet.support;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.stream;

public class QueryStringFormatter {

  public static HashMap<String, List<String>> parseQueryString(String queryString, String encoding) {
    String[] parts = queryString.split("&");
    HashMap<String, List<String>> parameters = new HashMap<>();
    stream(parts)
        .map(s -> s.split("=", 2))
        .forEach(s -> {
          parameters.computeIfAbsent(decode(s[0], encoding),
              name -> new ArrayList<>()).add(s.length > 1 ? decode(s[1], encoding) : null);
        });
    return parameters;
  }

  public static String formatQueryString(HashMap<String, List<String>> queries, String encoding) {
    return String.join("&", queries.entrySet().stream()
        .flatMap(e -> {
          List<String> list = e.getValue();
          return list.isEmpty()
                 ? Stream.of(e.getKey())
                 : list.stream().map(v -> encode(e.getKey(), encoding) + (v != null ? '=' + encode(v, encoding) : ""));
        })
        .toArray(String[]::new));
  }

  private static String decode(String toDecode, String charset) {
    try {
      return URLDecoder.decode(toDecode, charset);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  private static String encode(String toEncode, String charset) {
    try {
      return URLEncoder.encode(toEncode, charset);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }
}
