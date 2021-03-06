package org.arend.naming.reference;

import org.arend.ext.reference.Precedence;
import org.arend.ext.typechecking.MetaDefinition;
import org.jetbrains.annotations.NotNull;

public class MetaReferable implements GlobalReferable {
  private Precedence myPrecedence;
  private final String myName;
  private MetaDefinition myDefinition;

  public MetaReferable(Precedence precedence, String name, MetaDefinition definition) {
    myPrecedence = precedence;
    myName = name;
    myDefinition = definition;
  }

  public MetaDefinition getDefinition() {
    return myDefinition;
  }

  public void setDefinition(MetaDefinition definition) {
    myDefinition = definition;
  }

  public void setPrecedence(Precedence precedence) {
    myPrecedence = precedence;
  }

  @NotNull
  @Override
  public String textRepresentation() {
    return myName;
  }

  @NotNull
  @Override
  public Precedence getPrecedence() {
    return myPrecedence;
  }

  @NotNull
  @Override
  public Kind getKind() {
    return Kind.OTHER;
  }
}
