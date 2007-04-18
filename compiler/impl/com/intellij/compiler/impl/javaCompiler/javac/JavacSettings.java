package com.intellij.compiler.impl.javaCompiler.javac;

import com.intellij.openapi.components.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.DefaultJDOMExternalizer;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vfs.CharsetToolkit;
import org.jdom.Element;

import java.nio.charset.Charset;
import java.util.StringTokenizer;

@State(
  name = "JavacSettings",
  storages = {
    @Storage(id = "default", file = "$PROJECT_FILE$")
   ,@Storage(id = "dir", file = "$PROJECT_CONFIG_DIR$/compiler.xml", scheme = StorageScheme.DIRECTORY_BASED)
    }
)
public class JavacSettings implements PersistentStateComponent<Element>, ProjectComponent {
  private static final Logger LOG = Logger.getInstance("#com.intellij.compiler.impl.javaCompiler.javac.JavacSettings");

  public boolean DEBUGGING_INFO = true;
  public boolean GENERATE_NO_WARNINGS = false;
  public boolean DEPRECATION = true;
  public String ADDITIONAL_OPTIONS_STRING = "";
  public int MAXIMUM_HEAP_SIZE = 128;

  private boolean myTestsUseExternalCompiler = false;

  public void disposeComponent() {
  }

  public void initComponent() { }

  public void projectClosed() {
  }

  public void projectOpened() {
  }

  public Element getState() {
    try {
      final Element e = new Element("state");
      writeExternal(e);
      return e;
    }
    catch (WriteExternalException e1) {
      LOG.error(e1);
      return null;
    }
  }

  public void loadState(Element state) {
    try {
      readExternal(state);
    }
    catch (InvalidDataException e) {
      LOG.error(e);
    }
  }

  public boolean isTestsUseExternalCompiler() {
    return myTestsUseExternalCompiler;
  }

  public void setTestsUseExternalCompiler(boolean testsUseExternalCompiler) {
    myTestsUseExternalCompiler = testsUseExternalCompiler;
  }

  @SuppressWarnings({"HardCodedStringLiteral"})
  public String getOptionsString() {
    StringBuffer options = new StringBuffer();
    if(DEBUGGING_INFO) {
      options.append("-g ");
    }
    if(DEPRECATION) {
      options.append("-deprecation ");
    }
    if(GENERATE_NO_WARNINGS) {
      options.append("-nowarn ");
    }
    boolean isEncodingSet = false;
    final StringTokenizer tokenizer = new StringTokenizer(ADDITIONAL_OPTIONS_STRING, " \t\r\n");
    while(tokenizer.hasMoreTokens()) {
      String token = tokenizer.nextToken();
      if("-g".equals(token)) {
        continue;
      }
      if("-deprecation".equals(token)) {
        continue;
      }
      if("-nowarn".equals(token)) {
        continue;
      }
      options.append(token);
      options.append(" ");
      if ("-encoding".equals(token)) {
        isEncodingSet = true;
      }
    }
    if (!isEncodingSet) {
      final Charset ideCharset = CharsetToolkit.getIDEOptionsCharset();
      if ((CharsetToolkit.getDefaultSystemCharset() != ideCharset)) {
        options.append("-encoding ");
        options.append(ideCharset.name());
      }
    }
    return options.toString();
  }

  public static JavacSettings getInstance(Project project) {
    return project.getComponent(JavacSettings.class);
  }

  public String getComponentName() {
    return "JavacSettings";
  }

  public void readExternal(Element element) throws InvalidDataException {
    DefaultJDOMExternalizer.readExternal(this, element);
  }

  public void writeExternal(Element element) throws WriteExternalException {
    DefaultJDOMExternalizer.writeExternal(this, element);
  }
}