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

import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.tinyj.test.servlet.support.QueryStringFormatter.parseQueryString;

public class QueryStringFormatter_parseTest {

  @Test
  public void parameters_are_separated_by_ampersand() throws Exception {

    HashMap<String, List<String>> parameters = parseQueryString("a&b", "UTF-8");

    assertThat(parameters).containsExactly(
        entry("a", singletonList(null)),
        entry("b", singletonList(null)));
  }

  @Test
  public void parameters_can_have_a_value() throws Exception {

    HashMap<String, List<String>> parameters = parseQueryString("a=text&b=", "UTF-8");

    assertThat(parameters).containsExactly(
        entry("a", singletonList("text")),
        entry("b", singletonList("")));
  }

  @Test
  public void parameters_can_be_repeated() throws Exception {

    HashMap<String, List<String>> parameters = parseQueryString("a=text&a&a=&a&a=data", "UTF-8");

    assertThat(parameters).containsExactly(
        entry("a", asList("text", null, "", null, "data")));
  }

  @Test
  public void names_are_decoded() throws Exception {

    HashMap<String, List<String>> parameters = parseQueryString("my+%3D+name", "UTF-8");

    assertThat(parameters).containsExactly(
        entry("my = name", singletonList(null)));
  }

  @Test
  public void values_are_encoded() throws Exception {

    HashMap<String, List<String>> parameters = parseQueryString("a=my+%3D+value", "UTF-8");

    assertThat(parameters).containsExactly(
        entry("a", singletonList("my = value")));
  }
}