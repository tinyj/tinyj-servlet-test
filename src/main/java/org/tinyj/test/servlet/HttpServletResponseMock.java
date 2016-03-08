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

import org.tinyj.test.servlet.support.WriterSplitter;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.*;

import static java.time.ZoneOffset.UTC;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Map.Entry;
import static java.util.stream.Collectors.toList;
import static org.tinyj.test.servlet.support.CookieFormatter.formatCookie;
import static org.tinyj.test.servlet.support.HttpStatus.getMessageFor;

public class HttpServletResponseMock
    implements HttpServletResponse {

  public static final Charset ASCII = Charset.forName("ASCII");

  protected OutputStream output;
  protected Locale locale = null;

  protected String encoding = null;
  protected boolean commited = false;

  protected int status;
  protected String statusMessage;
  protected Map<String, List<String>> headers = new HashMap<>();
  protected ByteArrayOutputStream buffer = new ByteArrayOutputStream();
  protected PrintWriter writer;
  protected ServletOutputStream stream;

  protected Integer commitedStatus = null;
  protected String commitedStatusMessage = null;
  protected Map<String, List<String>> commitedHeaders = new HashMap<>();

  protected final ByteArrayOutputStream headerRecorder = new ByteArrayOutputStream();
  protected final ByteArrayOutputStream bodyRecorder = new ByteArrayOutputStream();
  private Writer bodyWriter = new StringWriter();

  public HttpServletResponseMock() {
    this(new OutputStream() {
      @Override
      public void write(int b) throws IOException {
      }
    });
  }

  public HttpServletResponseMock(OutputStream output) {
    reset();
    this.output = output;
  }

  @Override
  public boolean containsHeader(String name) {
    return headers.containsKey(name);
  }

  @Override
  public String encodeUrl(String url) {
    return url;
  }

  @Override
  public String encodeURL(String url) {
    return url;
  }

  @Override
  public String encodeRedirectUrl(String url) {
    return url;
  }

  @Override
  public String encodeRedirectURL(String url) {
    return url;
  }

  @Override
  public int getStatus() {
    return status;
  }

  @Override
  public void setStatus(int sc) {
    if (isCommitted()) {
      throw new IllegalStateException();
    }
    status = sc;
  }

  @Override
  public void setStatus(int sc, String sm) {
    setStatus(sc);
    statusMessage = sm;
  }

  @Override
  public void setHeader(String name, String value) {
    headers.put(name, new ArrayList<>(singletonList(value)));
  }

  @Override
  public void addHeader(String name, String value) {
    List<String> values = headers.getOrDefault(name, new ArrayList<>());
    values.add(value);
    headers.put(name, values);
  }

  @Override
  public void setDateHeader(String name, long date) {
    setHeader(name, RFC_1123_DATE_TIME.format(Instant.ofEpochMilli(date).atOffset(UTC)));
  }

  @Override
  public void addDateHeader(String name, long date) {
    addHeader(name, RFC_1123_DATE_TIME.format(Instant.ofEpochMilli(date).atOffset(UTC)));
  }

  @Override
  public void setIntHeader(String name, int value) {
    setHeader(name, Integer.toString(value));
  }

  @Override
  public void addIntHeader(String name, int value) {
    addHeader(name, Integer.toString(value));
  }

  @Override
  public void addCookie(Cookie cookie) {
    addHeader("Set-Cookie", formatCookie(cookie));
  }

  @Override
  public String getHeader(String name) {
    return getHeaders(name).stream().findFirst().orElse(null);
  }

  @Override
  public Collection<String> getHeaders(String name) {
    return headers.getOrDefault(name, emptyList());
  }

  @Override
  public Collection<String> getHeaderNames() {
    return headers.entrySet().stream()
        .filter(e -> !e.getValue().isEmpty())
        .map(Entry::getKey)
        .collect(toList());
  }

  @Override
  public void setContentLength(int len) {
    setHeader("Content-Length", Integer.toString(len));
  }

  @Override
  public void setContentLengthLong(long len) {
    setHeader("Content-Length", Long.toString(len));
  }

  @Override
  public String getContentType() {
    return getHeader("Content-Type");
  }

  @Override
  public void setContentType(String type) {
    if (isCommitted()) {
      return;
    }
    setHeader("Content-Type", type);
    String[] parts = type.split("; charset=", 2);
    setCharacterEncoding(parts.length > 1 ? parts[1] : encoding);
  }

  @Override
  public String getCharacterEncoding() {
    return Optional.ofNullable(encoding).orElse(locale != null ? Charset.defaultCharset().name() : "ISO-8859-1");
  }

  @Override
  public void setCharacterEncoding(String charset) {
    if (isCommitted() || writer != null) {
      return;
    }
    String contentType = getHeader("Content-Type");
    if (contentType != null) {
      contentType = contentType.split("; charset=", 2)[0];
      setHeader("Content-Type", charset == null ? contentType : contentType + "; charset=" + charset);
    }
    encoding = charset;
  }

  @Override
  public Locale getLocale() {
    return Optional.ofNullable(locale).orElse(Locale.getDefault());
  }

  @Override
  public void setLocale(Locale loc) {
    locale = loc;
    setHeader("Content-Language", loc.getLanguage());
  }

  @Override
  public boolean isCommitted() {
    return commited;
  }

  public void close() throws IOException {
    if (stream != null && stream.isReady()) {
      if (writer != null) {
        writer.close();
      } else {
        stream.close();
      }
      return;
    }
    if (!isCommitted()) {
      if (getHeader("Content-Length") == null
          && !Objects.equals(getHeader("Transfer-Encoding"), "identity")) {
        setContentLength(buffer.size());
      }
      commit();
    }
    buffer.writeTo(output);
    buffer.writeTo(bodyRecorder);
    buffer.close();
    output.close();
  }

  @Override
  public void flushBuffer() throws IOException {
    if (stream != null && stream.isReady()) {
      if (writer != null) {
        writer.flush();
      } else {
        stream.flush();
      }
      return;
    }
    if (!isCommitted()) {
      commit();
    }
    buffer.writeTo(output);
    buffer.writeTo(bodyRecorder);
    buffer.reset();
    output.flush();
  }

  public void commit() throws IOException {
    if (isCommitted()) {
      throw new IllegalStateException();
    }
    commited = true;

    commitedStatus = status;
    commitedStatusMessage = statusMessage;
    commitedHeaders.putAll(headers);

    byte[] statusLine = toAscii("HTTP/1.1 " + Integer.toString(status) + ' '
                                + Optional.ofNullable(statusMessage).orElse(getMessageFor(status)) + "\r\n");
    headerRecorder.write(statusLine);
    for (Entry<String, List<String>> header : headers.entrySet()) {
      for (String value : header.getValue()) {
        byte[] headerLine = toAscii(header.getKey() + ": " + value + "\r\n");
        headerRecorder.write(headerLine);
      }
    }
    headerRecorder.write(toAscii("\r\n"));

    headerRecorder.writeTo(output);
    output.flush();
  }

  @Override
  public void resetBuffer() {
    if (isCommitted()) {
      throw new IllegalStateException();
    }
    buffer.reset();
  }

  @Override
  public void reset() {
    if (isCommitted()) {
      throw new IllegalStateException();
    }
    status = 200;
    statusMessage = null;
    headers.clear();
    buffer.reset();
    writer = null;
    stream = null;
  }

  @Override
  public ServletOutputStream getOutputStream() throws IOException {
    if (writer != null) {
      throw new IllegalStateException();
    }
    if (stream != null) {
      return stream;
    }
    stream = new OutStream();
    return stream;
  }

  @Override
  public PrintWriter getWriter() throws IOException {
    if (writer != null) {
      return writer;
    }
    if (stream != null) {
      throw new IllegalStateException();
    }
    setCharacterEncoding(getCharacterEncoding());
    OutputStreamWriter writer1 = new OutputStreamWriter(getOutputStream(), getCharacterEncoding());
    Writer writer2 = HttpServletResponseMock.this.bodyWriter;
    writer = new PrintWriter(new WriterSplitter(writer2, writer1));
    return writer;
  }

  @Override
  public int getBufferSize() {
    return Integer.MAX_VALUE - 8;
  }

  @Override
  public void setBufferSize(int size) {
    if (isCommitted()) {
      throw new IllegalStateException();
    }
  }

  @Override
  public void sendRedirect(String location) throws IOException {
    setStatus(302);
    setHeader("Location", location);
    getOutputStream().flush();
  }

  @Override
  public void sendError(int sc, String msg) throws IOException {
    setStatus(sc);
    setContentType("text/html");
    getWriter().append(msg).flush();
  }

  @Override
  public void sendError(int sc) throws IOException {
    setStatus(sc);
    setContentType("text/html");
    getOutputStream().flush();
  }

  protected static byte[] toAscii(String string) {
    return string.getBytes(ASCII);
  }

  public Integer getCommitedStatus() {
    return commitedStatus;
  }

  public String getCommitedStatusMessage() {
    return commitedStatusMessage;
  }

  public Map<String, List<String>> getCommitedHeaders() {
    return commitedHeaders;
  }

  public byte[] getHeaderBytes() {
    return headerRecorder.toByteArray();
  }

  public byte[] getSendBodyBytes() {
    return bodyRecorder.toByteArray();
  }

  public String getSendBody() {
    return bodyWriter.toString();
  }

  class OutStream extends ServletOutputStream {

    private boolean ready = true;

    @Override
    public boolean isReady() {
      return ready;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
      try {
        writeListener.onWritePossible();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void write(int b) throws IOException {
      if (!isReady()) {
        throw new IllegalStateException();
      }
      buffer.write(b);
    }

    @Override
    protected void finalize() throws Throwable {
      super.finalize();
      close();
    }

    @Override
    public void flush() throws IOException {
      if (isReady()) {
        ready = false;
        flushBuffer();
        ready = true;
      }
    }

    @Override
    public void close() throws IOException {
      ready = false;
      HttpServletResponseMock.this.close();
    }
  }
}
