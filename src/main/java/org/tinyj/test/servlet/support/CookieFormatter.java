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

import javax.servlet.http.Cookie;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

import static java.lang.Integer.parseInt;

public class CookieFormatter {

  public static Cookie parseCookie(String string) {
    String[] records = string.split("; *");
    String[] nameValue = records[0].split(" *= *", 2);
    Cookie cookie = new Cookie(nameValue[0], nameValue[1]);
    for (String record : records) {
      String[] tagValue = record.split(" *= *", 2);
      switch (tagValue[0]) {
        case "Domain":
          cookie.setDomain(tagValue[1]);
          break;
        case "Path":
          cookie.setPath(tagValue[1]);
          break;
        case "Comment":
          cookie.setComment(tagValue[1]);
          break;
        case "Version":
          cookie.setVersion(parseInt(tagValue[1]));
          break;
        case "HttpOnly":
          cookie.setHttpOnly(true);
          break;
        case "Secure":
          cookie.setSecure(true);
          break;
        case "Max-Age":
          cookie.setMaxAge(parseInt(tagValue[1]));
          break;
        case "Expires":
          cookie.setMaxAge((int) Duration.between(Instant.now(), Instant.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(tagValue[1]))).getSeconds());
          break;
        case "Discard":
          cookie.setMaxAge(-1);
          break;
      }
    }
    return cookie;
  }

  public static String formatCookie(Cookie cookie) {
    StringBuilder sb = new StringBuilder(cookie.getName());
    sb.append('=').append(cookie.getValue());
    if (cookie.getDomain() != null) {
      sb.append("; Domain=").append(cookie.getDomain());
    }
    if (cookie.getPath() != null) {
      sb.append("; Path=").append(cookie.getPath());
    }
    if (cookie.getMaxAge() > 0) {
      sb.append("; Max-Age=").append(cookie.getMaxAge());
    }
    if (cookie.getComment() != null) {
      sb.append("; Comment=").append(cookie.getComment());
    }
    if (cookie.getVersion() > 0) {
      sb.append("; Version=").append(cookie.getVersion());
    }
    if (cookie.getSecure()) {
      sb.append("; Secure");
    }
    if (cookie.isHttpOnly()) {
      sb.append("; HttpOnly");
    }
    return sb.toString();
  }
}
