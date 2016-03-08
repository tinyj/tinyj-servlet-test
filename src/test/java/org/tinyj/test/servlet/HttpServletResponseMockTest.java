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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Locale;

import static java.nio.charset.Charset.defaultCharset;
import static org.assertj.core.api.Assertions.assertThat;

public class HttpServletResponseMockTest {

  public static final Charset UTF8 = Charset.forName("UTF-8");
  private ByteArrayOutputStream outputStream;
  private HttpServletResponseMock response;

  @BeforeMethod
  public void setUp() throws Exception {
    outputStream = new ByteArrayOutputStream();
    response = new HttpServletResponseMock(outputStream);
  }

  @Test
  public void default_status_is_200_OK() throws Exception {
    // when
    response.close();

    // then
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(toString(outputStream)).isEqualTo("HTTP/1.1 200 OK\r\n" +
                                                 "Content-Length: 0\r\n" +
                                                 "\r\n");
  }

  @Test
  public void status_can_be_set() throws Exception {
    // given
    assertThat(response.getStatus()).isEqualTo(200);

    // when
    response.setStatus(204);

    // then
    assertThat(response.getStatus()).isEqualTo(204);

    // when
    response.close();

    // then
    assertThat(toString(outputStream)).isEqualTo("HTTP/1.1 204 No Content\r\n" +
                                                 "Content-Length: 0\r\n" +
                                                 "\r\n");
  }

  @Test
  public void custom_status_message() throws Exception {
    // when
    response.setStatus(999, "Not the Beast");

    // then
    assertThat(response.getStatus()).isEqualTo(999);

    // when
    response.close();

    // when
    assertThat(toString(outputStream)).isEqualTo("HTTP/1.1 999 Not the Beast\r\n" +
                                                 "Content-Length: 0\r\n" +
                                                 "\r\n");
  }

  @Test
  public void size_is_calculated_on_close() throws Exception {
    // setup
    String messageBody = "message body";

    // when
    response.getWriter().append(messageBody).close();

    //then
    assertThat(toString(outputStream)).isEqualTo("HTTP/1.1 200 OK\r\n" +
                                                 "Content-Length: " + messageBody.length() + "\r\n" +
                                                 "\r\n" +
                                                 messageBody);
  }

  @Test
  public void size_is_not_calculated_on_flushBuffer() throws Exception {
    // setup
    String messageBody = "message body";

    // when
    response.getWriter().append(messageBody).flush();

    // then
    assertThat(toString(outputStream)).isEqualTo("HTTP/1.1 200 OK\r\n" +
                                                 "\r\n" +
                                                 messageBody);
  }

  @Test
  public void headers() throws Exception {
    // given
    response.setHeader("X-Singleton", "i was first");
    response.setHeader("X-Singleton", "there can only be one");
    response.addHeader("X-List", "first");
    response.addHeader("X-List", "second");
    response.setDateHeader("X-Singleton-Date", 100000000);
    response.setDateHeader("X-Singleton-Date", 200000000);
    response.addDateHeader("X-List-Date", 300000000);
    response.addDateHeader("X-List-Date", 400000000);
    response.setIntHeader("X-Singleton-Int", 100000000);
    response.setIntHeader("X-Singleton-Int", 200000000);
    response.addIntHeader("X-List-Int", 300000000);
    response.addIntHeader("X-List-Int", 400000000);

    // when
    response.getWriter().close();

    // then
    assertThat(toString(outputStream).split("\r\n"))
        .hasSize(11)
        .startsWith("HTTP/1.1 200 OK")
        .contains(
            "Content-Length: 0",
            "X-List-Date: Sun, 4 Jan 1970 11:20:00 GMT",
            "X-List-Date: Mon, 5 Jan 1970 15:06:40 GMT",
            "X-List-Int: 300000000",
            "X-List-Int: 400000000",
            "X-List: first",
            "X-List: second",
            "X-Singleton-Date: Sat, 3 Jan 1970 07:33:20 GMT",
            "X-Singleton-Int: 200000000",
            "X-Singleton: there can only be one");
  }

  @Test
  public void set_content_length_is_not_overwritten() throws Exception {
    // given
    response.setContentLength(123);

    // when
    response.getWriter().append("message body").flush();

    // then
    assertThat(toString(outputStream)).isEqualTo("HTTP/1.1 200 OK\r\n" +
                                                 "Content-Length: 123\r\n" +
                                                 "\r\n" +
                                                 "message body");
  }

  @Test
  public void contentType_can_be_added() throws Exception {
    response.setContentType("text/plain");

    // when
    response.close();

    // then
    assertThat(toString(this.outputStream)).isEqualTo("HTTP/1.1 200 OK\r\n" +
                                                      "Content-Length: 0\r\n" +
                                                      "Content-Type: text/plain\r\n" +
                                                      "\r\n");
  }

  @Test
  public void contentType_is_extended_with_ISO_8859_1_charset_if_writer_is_claimed_before_locale_or_encoding_are_set() throws Exception {
    response.setContentType("text/plain");
    assertThat(response.getContentType()).isEqualTo("text/plain");
    assertThat(response.getCharacterEncoding()).isEqualTo("ISO-8859-1");

    // when
    PrintWriter writer = response.getWriter();

    // then
    assertThat(response.getContentType()).isEqualTo("text/plain; charset=ISO-8859-1");
    assertThat(response.getCharacterEncoding()).isEqualTo("ISO-8859-1");

    // when
    writer.close();

    // then
    assertThat(response.getContentType()).isEqualTo("text/plain; charset=ISO-8859-1");
    assertThat(response.getCharacterEncoding()).isEqualTo("ISO-8859-1");
    assertThat(toString(this.outputStream)).isEqualTo("HTTP/1.1 200 OK\r\n" +
                                                      "Content-Length: 0\r\n" +
                                                      "Content-Type: text/plain; charset=ISO-8859-1\r\n" +
                                                      "\r\n");
  }

  @Test
  public void contentType_is_extended_with_defaultCharset_if_writer_is_claimed_when_only_locale_was_set() throws Exception {
    // given
    response.setContentType("text/plain");
    assertThat(response.getContentType()).isEqualTo("text/plain");
    assertThat(response.getCharacterEncoding()).isEqualTo("ISO-8859-1");

    // when
    response.setLocale(Locale.ENGLISH);

    // then
    assertThat(response.getContentType()).isEqualTo("text/plain");
    String charset = defaultCharset().name();
    assertThat(response.getCharacterEncoding()).isEqualTo(charset);

    // when
    PrintWriter writer = response.getWriter();

    // then
    assertThat(response.getContentType()).isEqualTo("text/plain; charset=" + charset);
    assertThat(response.getCharacterEncoding()).isEqualTo(charset);

    // when
    writer.close();

    // then
    assertThat(response.getContentType()).isEqualTo("text/plain; charset=" + charset);
    assertThat(response.getCharacterEncoding()).isEqualTo(charset);
    assertThat(toString(this.outputStream)).isEqualTo("HTTP/1.1 200 OK\r\n" +
                                                      "Content-Length: 0\r\n" +
                                                      "Content-Language: en\r\n" +
                                                      "Content-Type: text/plain; charset=" + charset + "\r\n" +
                                                      "\r\n");
  }


  @Test
  public void contentType_is_not_extended_with_defaultCharset_if_outputStren_is_claimed() throws Exception {
    // given
    response.setContentType("text/plain");
    assertThat(response.getContentType()).isEqualTo("text/plain");
    assertThat(response.getCharacterEncoding()).isEqualTo("ISO-8859-1");

    // when
    response.setLocale(Locale.ENGLISH);

    // then
    assertThat(response.getContentType()).isEqualTo("text/plain");
    String charset = defaultCharset().name();
    assertThat(response.getCharacterEncoding()).isEqualTo(charset);

    // when
    OutputStream writer = response.getOutputStream();

    // then
    assertThat(response.getContentType()).isEqualTo("text/plain");
    assertThat(response.getCharacterEncoding()).isEqualTo(charset);

    // when
    writer.close();

    // then
    assertThat(response.getContentType()).isEqualTo("text/plain");
    assertThat(response.getCharacterEncoding()).isEqualTo(charset);
    assertThat(toString(this.outputStream)).isEqualTo("HTTP/1.1 200 OK\r\n" +
                                                      "Content-Length: 0\r\n" +
                                                      "Content-Language: en\r\n" +
                                                      "Content-Type: text/plain\r\n" +
                                                      "\r\n");
  }

  @Test
  public void contentType_is_extended_with_charset() throws Exception {
    // given
    response.setCharacterEncoding("UTF-8");
    assertThat(response.getCharacterEncoding()).isEqualTo("UTF-8");

    // when
    response.setContentType("text/plain");

    //then
    assertThat(response.getCharacterEncoding()).isEqualTo("UTF-8");
    assertThat(response.getContentType()).isEqualTo("text/plain; charset=UTF-8");

    // when
    response.setCharacterEncoding("ISO-8859-15");

    // then
    assertThat(response.getContentType()).isEqualTo("text/plain; charset=ISO-8859-15");
    assertThat(response.getCharacterEncoding()).isEqualTo("ISO-8859-15");

    // when
    PrintWriter writer = response.getWriter();

    // then
    assertThat(response.getContentType()).isEqualTo("text/plain; charset=ISO-8859-15");
    assertThat(response.getCharacterEncoding()).isEqualTo("ISO-8859-15");

    // when
    response.setCharacterEncoding("Big5");

    // then
    assertThat(response.getContentType()).isEqualTo("text/plain; charset=ISO-8859-15");
    assertThat(response.getCharacterEncoding()).isEqualTo("ISO-8859-15");

    // when
    writer.close();

    // then
    assertThat(response.getContentType()).isEqualTo("text/plain; charset=ISO-8859-15");
    assertThat(response.getCharacterEncoding()).isEqualTo("ISO-8859-15");
    assertThat(toString(this.outputStream)).isEqualTo("HTTP/1.1 200 OK\r\n" +
                                                      "Content-Length: 0\r\n" +
                                                      "Content-Type: text/plain; charset=ISO-8859-15\r\n" +
                                                      "\r\n");
  }

  @Test
  public void commited_after_close() throws Exception {
    // given
    assertThat(response.isCommitted()).isFalse();

    // when
    response.close();

    // then
    assertThat(response.isCommitted()).isTrue();
  }

  @Test
  public void commited_after_flushBuffer() throws Exception {
    // given
    assertThat(response.isCommitted()).isFalse();

    // when
    response.flushBuffer();

    // then
    assertThat(response.isCommitted()).isTrue();
  }

  protected String toString(ByteArrayOutputStream output) {
    return new String(output.toByteArray(), UTF8);
  }
}
