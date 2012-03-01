/*
 * Copyright 2000-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.diagnostic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

class DevelopersLoader {
  private static final String DEVELOPERS_LIST_URL = "http://ea-engine.labs.intellij.net/data?category=developers";
  private static final String DATA_CHARSET = "utf-8";

  private DevelopersLoader() {
  }

  public static Collection<Developer> fetchDevelopers() throws IOException {
    try {
      List<Developer> developers = new LinkedList<Developer>();
      developers.add(Developer.NULL);

      URL url = new URL(DEVELOPERS_LIST_URL);
      BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), DATA_CHARSET));

      try {
        while (reader.ready()) {
          String line = reader.readLine();
          if (line == null) break;
          int i = line.indexOf('\t');
          if (i == -1) throw new IOException("Protocol error");
          int id = Integer.parseInt(line.substring(0, i));
          String name = line.substring(i + 1);
          developers.add(new Developer(id, name));
        }
        return developers;
      } finally {
        reader.close();
      }
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }
}
