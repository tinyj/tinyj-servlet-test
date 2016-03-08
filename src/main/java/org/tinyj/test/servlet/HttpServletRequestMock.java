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
package org.tinyj.test.servlet;

import org.tinyj.test.servlet.support.CookieFormatter;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.security.Principal;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static java.util.Collections.*;
import static java.util.stream.Collectors.toMap;
import static org.tinyj.test.servlet.support.QueryStringFormatter.parseQueryString;

public class HttpServletRequestMock
    implements HttpServletRequest {

  private ServletInputStream input;
  private HashMap<String, Object> attributes;

  private String authType;
  private Principal userPrincipal;
  private String remoteUser;
  private HttpSessionMock session;
  private String requestedSessionId = null;

  private String protocol = "HTTP/1.1";
  private String method = "GET";
  private String contextPath = "";
  private String servletPath = "";
  private String path = null;

  private String queryString;

  private HashMap<String, List<String>> headers = new HashMap<>();
  private Map<String, String[]> parameters = new HashMap<>();

  private String scheme = "http";
  private int localPort = 8080;
  private String localIp = "127.0.0.1";

  private String localHost = "localHost";
  private int remotePort = Integer.MAX_VALUE;
  private String remoteIp = "0.0.0.0";
  private String remoteHost = "example.org";

  public HttpServletRequestMock() {
    this.input = new InStream();
  }


  /*--- headers ---*/

  @Override
  public String getHeader(String name) {
    return headers.getOrDefault(name.toLowerCase(), singletonList(null)).get(0);
  }

  @Override
  public Enumeration<String> getHeaders(String name) {
    return enumeration(headers.getOrDefault(name.toLowerCase(), emptyList()));
  }

  @Override
  public long getDateHeader(String name) {
    String header = getHeader(name);
    if (header == null) {
      return -1;
    }
    return Instant.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(header)).toEpochMilli();
  }

  @Override
  public int getIntHeader(String name) {
    String header = getHeader(name);
    if (header == null) {
      return -1;
    }
    return parseInt(header);
  }

  @Override
  public Enumeration<String> getHeaderNames() {
    return enumeration(headers.keySet());
  }

  @Override
  public Cookie[] getCookies() {
    Cookie[] cookies = headers.getOrDefault("Cookie", emptyList()).stream()
        .map(CookieFormatter::parseCookie)
        .toArray(Cookie[]::new);
    return cookies.length > 0 ? cookies : null;
  }

  @Override
  public Locale getLocale() {
    return getLocales().nextElement();
  }

  @Override
  public Enumeration<Locale> getLocales() {
    return enumeration(singleton(Locale.getDefault()));
  }


  /*--- request line ---*/

  @Override
  public String getProtocol() {
    return protocol;
  }

  @Override
  public String getScheme() {
    return scheme;
  }

  @Override
  public String getMethod() {
    return method;
  }

  @Override
  public String getPathInfo() {
    return path;
  }

  @Override
  public String getPathTranslated() {
    return null;
  }

  @Override
  public String getContextPath() {
    return contextPath;
  }

  @Override
  public String getServletPath() {
    return servletPath;
  }

  @Override
  public String getQueryString() {
    return queryString;
  }

  @Override
  public String getRequestURI() {
    return contextPath + servletPath + Optional.ofNullable(path).orElse("/");
  }

  @Override
  public StringBuffer getRequestURL() {
    return new StringBuffer(getScheme() + "://" + getServerName() + ':' + getServerPort() + getRequestURI());
  }


  /* connection */

  @Override
  public int getRemotePort() {
    return remotePort;
  }

  @Override
  public String getRemoteAddr() {
    return remoteIp;
  }

  @Override
  public String getRemoteHost() {
    return remoteHost;
  }

  @Override
  public int getLocalPort() {
    return localPort;
  }

  @Override
  public int getServerPort() {
    String host = getHeader("Host");
    if (host != null) {
      return URI.create(host).getPort();
    }
    return getLocalPort();
  }

  @Override
  public String getLocalName() {
    return localHost;
  }

  @Override
  public String getServerName() {
    String host = getHeader("Host");
    if (host != null) {
      return URI.create(host).getHost();
    }
    return getLocalName();
  }

  @Override
  public String getLocalAddr() {
    return localIp;
  }

  @Override
  public boolean isSecure() {
    return false;
  }


  /*--- authentication ---*/

  @Override
  public String getAuthType() {
    return authType;
  }

  @Override
  public String getRemoteUser() {
    return remoteUser;
  }

  @Override
  public boolean isUserInRole(String role) {
    return false;
  }

  @Override
  public Principal getUserPrincipal() {
    return userPrincipal;
  }

  @Override
  public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
    return false;
  }

  @Override
  public void login(String username, String password) throws ServletException {
    withAuthentication("BasicAuth", () -> username);
  }

  @Override
  public void logout() throws ServletException {
    remoteUser = null;
    authType = null;
    userPrincipal = null;
  }


  /*--- session ---*/

  @Override
  public HttpSessionMock getSession(boolean create) {
    return session != null ? session : create ? new HttpSessionMock() : null;
  }

  @Override
  public HttpSessionMock getSession() {
    return session;
  }

  @Override
  public String changeSessionId() {
    return getSession(true).getId();
  }

  @Override
  public String getRequestedSessionId() {
    return requestedSessionId;
  }

  @Override
  public boolean isRequestedSessionIdValid() {
    return requestedSessionId != null;
  }

  @Override
  public boolean isRequestedSessionIdFromCookie() {
    return false;
  }

  @Override
  public boolean isRequestedSessionIdFromURL() {
    return false;
  }

  @Override
  public boolean isRequestedSessionIdFromUrl() {
    return false;
  }


  /*--- attributes ---*/

  @Override
  public Object getAttribute(String name) {
    return attributes.get(name);
  }

  @Override
  public Enumeration<String> getAttributeNames() {
    return enumeration(attributes.keySet());
  }

  @Override
  public void setAttribute(String name, Object o) {
    attributes.put(name, o);
  }

  @Override
  public void removeAttribute(String name) {
    attributes.remove(name);
  }


  /*--- content ---*/

  @Override
  public String getCharacterEncoding() {
    String header = getHeader("Content-Type");
    String[] parts = header.split("; charset=", 2);
    return parts.length > 1 ? parts[1] : null;
  }

  @Override
  public void setCharacterEncoding(String env) throws UnsupportedEncodingException {

  }

  @Override
  public int getContentLength() {
    String header = getHeader("Content-Length");
    return header != null ? parseInt(header) : -1;
  }

  @Override
  public long getContentLengthLong() {
    String header = getHeader("Content-Length");
    return header != null ? parseLong(header) : -1;
  }

  @Override
  public String getContentType() {
    return getHeader("Content-Type");
  }

  @Override
  public ServletInputStream getInputStream() throws IOException {
    return input;
  }

  @Override
  public BufferedReader getReader() throws IOException {
    return new BufferedReader(new InputStreamReader(getInputStream(), getCharacterEncoding()));
  }


  /*--- multipart ---*/

  @Override
  public Collection<Part> getParts() throws IOException, ServletException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Part getPart(String name) throws IOException, ServletException {
    throw new UnsupportedOperationException();
  }


  /*--- parameters ---*/

  @Override
  public String getParameter(String name) {
    String[] parameters = this.parameters.get(name);
    return parameters != null ? parameters[0] : null;
  }

  @Override
  public Enumeration<String> getParameterNames() {
    return enumeration(parameters.keySet());
  }

  @Override
  public String[] getParameterValues(String name) {
    return this.parameters.get(name);
  }

  @Override
  public Map<String, String[]> getParameterMap() {
    return new HashMap<>(parameters);
  }


  /*--- context ---*/

  @Override
  public RequestDispatcher getRequestDispatcher(String path) {
    return null;
  }

  @Override
  public String getRealPath(String path) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ServletContext getServletContext() {
    throw new UnsupportedOperationException();
  }

  @Override
  public AsyncContext startAsync() throws IllegalStateException {
    throw new IllegalStateException();
  }

  @Override
  public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
    throw new IllegalStateException();
  }

  @Override
  public boolean isAsyncStarted() {
    return false;
  }

  @Override
  public boolean isAsyncSupported() {
    return false;
  }

  @Override
  public AsyncContext getAsyncContext() {
    throw new IllegalStateException();
  }

  @Override
  public DispatcherType getDispatcherType() {
    return DispatcherType.REQUEST;
  }

  @Override
  public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
    return null;
  }


  /*--- setters ---*/

  public HttpServletRequestMock withAuthentication(String authType, Principal principal) {
    this.authType = authType;
    this.userPrincipal = principal;
    this.remoteUser = principal.getName();
    return this;
  }

  public HttpServletRequestMock withAttributes(HashMap<String, Object> attributes) {
    this.attributes = attributes;
    return this;
  }

  public HttpServletRequestMock withSession(HttpSessionMock session) {
    this.session = session;
    return this;
  }

  public HttpServletRequestMock withRequestedSessionId(String requestedSessionId) {
    this.requestedSessionId = requestedSessionId;
    return this;
  }

  public HttpServletRequestMock withProtocol(String protocol) {
    this.protocol = protocol;
    return this;
  }

  public HttpServletRequestMock withMethod(String method) {
    this.method = method;
    return this;
  }

  public HttpServletRequestMock withContextPath(String contextPath) {
    this.contextPath = contextPath;
    return this;
  }

  public HttpServletRequestMock withServletPath(String servletPath) {
    this.servletPath = servletPath;
    return this;
  }

  public HttpServletRequestMock withPath(String path) {
    this.path = path;
    return this;
  }

  public HttpServletRequestMock withHeaders(HashMap<String, List<String>> headers) {
    this.headers.putAll(headers);
    return this;
  }


  public HttpServletRequestMock withParameters(HashMap<String, List<String>> parameters) {
    this.parameters.putAll(parameters.entrySet().stream()
        .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
        .collect(toMap(Map.Entry::getKey, e -> (String[]) e.getValue().toArray())));
    return this;
  }

  public HttpServletRequestMock withQueryString(String queryString) {
    this.queryString = queryString;
    withParameters(parseQueryString(queryString, "UTF-8"));
    return this;
  }

  public HttpServletRequestMock withScheme(String scheme) {
    this.scheme = scheme;
    return this;
  }

  public HttpServletRequestMock withLocalPort(int localPort) {
    this.localPort = localPort;
    return this;
  }

  public HttpServletRequestMock withLocalIp(String localIp) {
    this.localIp = localIp;
    return this;
  }

  public HttpServletRequestMock withLocalHost(String localHost) {
    this.localHost = localHost;
    return this;
  }

  public HttpServletRequestMock withRemotePort(int remotePort) {
    this.remotePort = remotePort;
    return this;
  }

  public HttpServletRequestMock withRemoteIp(String remoteIp) {
    this.remoteIp = remoteIp;
    return this;
  }

  public HttpServletRequestMock withRemoteHost(String remoteHost) {
    this.remoteHost = remoteHost;
    return this;
  }

  public HttpServletRequest withBody(String body) {
    final ByteBuffer buffer = ByteBuffer.wrap(body.getBytes());
    input = new ServletInputStream() {
      @Override
      public boolean isFinished() {
        return !buffer.hasRemaining();
      }

      @Override
      public boolean isReady() {
        return buffer.hasRemaining();
      }

      @Override
      public void setReadListener(ReadListener readListener) {
      }

      @Override
      public int read() throws IOException {
        if (buffer.hasRemaining()) {
          return buffer.get();
        }
        return -1;
      }
    };
    return this;
  }

  private static class InStream extends ServletInputStream {

    @Override
    public boolean isFinished() {
      return true;
    }

    @Override
    public boolean isReady() {
      return false;
    }

    @Override
    public void setReadListener(ReadListener readListener) {
      try {
        readListener.onAllDataRead();
      } catch (IOException e) {
        throw new RuntimeException();
      }
    }

    @Override
    public int read() throws IOException {
      return -1;
    }
  }
}
