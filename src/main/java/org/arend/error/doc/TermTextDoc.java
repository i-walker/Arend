package org.arend.error.doc;

public class TermTextDoc extends TextDoc {
  private final boolean myFirst;

  TermTextDoc(String text, boolean isFirst) {
    super(text);
    myFirst = isFirst;
  }

  public boolean isFirst() {
    return myFirst;
  }
}
