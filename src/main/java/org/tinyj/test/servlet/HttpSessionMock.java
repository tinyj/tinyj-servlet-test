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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.time.Instant;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static java.util.Collections.emptyEnumeration;
import static java.util.Collections.enumeration;

class HttpSessionMock implements HttpSession {

  private String id = Long.toHexString(new Random().nextLong());
  private long lastAccessTime = Instant.now().toEpochMilli();
  private long creationTime = Instant.now().toEpochMilli();
  private int maxInactiveInterval = 0;
  private Map<String, Object> attributes = new HashMap<>();

  @Override
  public String getId() {
    return id;
  }

  @Override
  public long getCreationTime() {
    return creationTime;
  }

  @Override
  public long getLastAccessedTime() {
    return lastAccessTime;
  }

  @Override
  public void setMaxInactiveInterval(int interval) {
    maxInactiveInterval = interval;
  }

  @Override
  public int getMaxInactiveInterval() {
    return maxInactiveInterval;
  }

  @Override
  public Object getAttribute(String name) {
    return attributes.get(name);
  }

  @Override
  public void setAttribute(String name, Object value) {
    attributes.put(name, value);
  }

  @Override
  public void removeAttribute(String name) {
    attributes.remove(name);
  }

  @Override
  public Enumeration<String> getAttributeNames() {
    return enumeration(attributes.keySet());
  }

  @Override
  public Object getValue(String name) {
    return getAttribute(name);
  }

  @Override
  public void putValue(String name, Object value) {
    setAttribute(name, value);
  }

  @Override
  public void removeValue(String name) {
    removeAttribute(name);
  }

  @Override
  public String[] getValueNames() {
    return attributes.keySet().stream().toArray(String[]::new);
  }

  @Override
  public void invalidate() {
    setMaxInactiveInterval(-1);
    attributes.clear();
  }

  @Override
  public boolean isNew() {
    return false;
  }

  @Override
  public ServletContext getServletContext() {
    return null;
  }

  @Override
  public HttpSessionContext getSessionContext() {
    return new HttpSessionContext() {
      @Override
      public HttpSession getSession(String sessionId) {
        return null;
      }

      @Override
      public Enumeration<String> getIds() {
        return emptyEnumeration();
      }
    };
  }
}
