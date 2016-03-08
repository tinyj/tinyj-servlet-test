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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class WriterSplitter extends Writer {

  protected final Writer slave1;
  protected final Writer slave2;

  public WriterSplitter(Writer slave1, OutputStreamWriter slave2) {
    this.slave1 = slave1;
    this.slave2 = slave2;
  }

  @Override
  public void write(char[] cbuf, int off, int len) throws IOException {
    slave1.write(cbuf, off, len);
    slave2.write(cbuf, off, len);
  }

  @Override
  public void flush() throws IOException {
    slave1.flush();
    slave2.flush();
  }

  @Override
  public void close() throws IOException {
    slave1.close();
    slave2.close();
  }
}
