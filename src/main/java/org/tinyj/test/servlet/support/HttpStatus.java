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

public enum HttpStatus {

  CONTINUE(100, "Continue"),
  SWITCHING_PROTOCOLS(101, "Switching Protocols"),
  PROCESSING(102, "Processing"),

  OK(200, "OK"),
  CREATED(201, "Created"),
  ACCEPTED(202, "Accepted"),
  NON_AUTHORITATIVE_INFORMATION(203, "Non-authoritative Information"),
  NO_CONTENT(204, "No Content"),
  RESET_CONTENT(205, "Reset Content"),
  PARTIAL_CONTENT(206, "Partial Content"),
  MULTI_STATUS(207, "Multi-Status"),
  ALREADY_REPORTED(208, "Already Reported"),
  IM_USED(226, "IM Used"),

  MULTIPLE_CHOICES(300, "Multiple Choices"),
  MOVED_PERMANENTLY(301, "Moved Permanently"),
  FOUND(302, "Found"),
  SEE_OTHER(303, "See Other"),
  NOT_MODIFIED(304, "Not Modified"),
  USE_PROXY(305, "Use Proxy"),
  TEMPORARY_REDIRECT(307, "Temporary Redirect"),
  PERMANENT_REDIRECT(308, "Permanent Redirect"),

  BAD_REQUEST(400, "Bad Request"),
  UNAUTHORIZED(401, "Unauthorized"),
  PAYMENT_REQUIRED(402, "Payment Required"),
  FORBIDDEN(403, "Forbidden"),
  NOT_FOUND(404, "Not Found"),
  METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
  NOT_ACCEPTABLE(406, "Not Acceptable"),
  PROXY_AUTHENTICATION_REQUIRED(407, "Proxy Authentication Required"),
  REQUEST_TIMEOUT(408, "Request Timeout"),
  CONFLICT(409, "Conflict"),
  GONE(410, "Gone"),
  LENGTH_REQUIRED(411, "Length Required"),
  PRECONDITION_FAILED(412, "Precondition Failed"),
  PAYLOAD_TOO_LARGE(413, "Payload Too Large"),
  REQUEST_URI_TOO_LONG(414, "Request-URI Too Long"),
  UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
  REQUESTED_RANGE_NOT_SATISFIABLE(416, "Requested Range Not Satisfiable"),
  EXPECTATION_FAILED(417, "Expectation Failed"),
  I_AM_A_TEAPOT(418, "I'm a teapot"),
  MISDIRECTED_REQUEST(421, "Misdirected Request"),
  UNPROCESSABLE_ENTITY(422, "Unprocessable Entity"),
  LOCKED(423, "Locked"),
  FAILED_DEPENDENCY(424, "Failed Dependency"),
  UPGRADE_REQUIRED(426, "Upgrade Required"),
  PRECONDITION_REQUIRED(428, "Precondition Required"),
  TOO_MANY_REQUESTS(429, "Too Many Requests"),
  REQUEST_HEADER_FIELDS_TOO_LARGE(431, "Request Header Fields Too Large"),
  UNAVAILABLE_FOR_LEGAL_REASONS(451, "Unavailable For Legal Reasons"),
  CLIENT_CLOSED_REQUEST(499, "Client Closed Request"),

  INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
  NOT_IMPLEMENTED(501, "Not Implemented"),
  BAD_GATEWAY(502, "Bad Gateway"),
  SERVICE_UNAVAILABLE(503, "Service Unavailable"),
  GATEWAY_TIMEOUT(504, "Gateway Timeout"),
  HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version Not Supported"),
  VARIANT_ALSO_NEGOTIATES(506, "Variant Also Negotiates"),
  INSUFFICIENT_STORAGE(507, "Insufficient Storage"),
  LOOP_DETECTED(508, "Loop Detected"),
  NOT_EXTENDED(510, "Not Extended"),
  NETWORK_AUTHENTICATION_REQUIRED(511, "Network Authentication Required"),
  NETWORK_CONNECT_TIMEOUT_ERROR(599, "Network Connect Timeout Error");

  public final int code;
  public final String message;

  HttpStatus(int code, String message) {
    this.code = code;
    this.message = message;
  }

  public static String getMessageFor(int code) {
    for (HttpStatus status : values()) {
      if (status.code == code) {
        return status.message;
      }
    }
    if (code < 100) {
      return "Unknown";
    }
    if (code < 200) {
      return "Informational";
    }
    if (code < 300) {
      return "Success";
    }
    if (code < 400) {
      return "Redirection";
    }
    if (code < 500) {
      return "Client Error";
    }
    if (code < 600) {
      return "Server Error";
    }
    return "Unknown";
  }
}
