package org.arend.core.context.param;

import org.arend.core.expr.ReferenceExpression;
import org.arend.core.expr.type.Type;
import org.arend.core.subst.ExprSubstitution;
import org.arend.core.subst.LevelSubstitution;

import java.util.List;

public class UntypedDependentLink implements DependentLink {
  private String myName;
  protected DependentLink myNext;

  public UntypedDependentLink(String name, DependentLink next) {
    assert next instanceof UntypedDependentLink || next instanceof TypedDependentLink;
    myName = name;
    myNext = next;
  }

  protected UntypedDependentLink(String name) {
    myName = name;
    myNext = EmptyDependentLink.getInstance();
  }

  @Override
  public Type getType() {
    return myNext.getType();
  }

  @Override
  public boolean isExplicit() {
    return myNext.isExplicit();
  }

  @Override
  public void setExplicit(boolean isExplicit) {

  }

  @Override
  public void setType(Type type) {

  }

  @Override
  public DependentLink getNext() {
    return myNext;
  }

  @Override
  public void setNext(DependentLink next) {
    myNext = next;
  }

  @Override
  public void setName(String name) {
    myName = name;
  }

  @Override
  public TypedDependentLink getNextTyped(List<String> names) {
    DependentLink link = this;
    for (; link instanceof UntypedDependentLink; link = link.getNext()) {
      if (names != null) {
        names.add(link.getName());
      }
    }
    if (names != null) {
      names.add(link.getName());
    }
    return (TypedDependentLink) link;
  }

  @Override
  public boolean hasNext() {
    return true;
  }

  @Override
  public String getName() {
    return myName;
  }

  @Override
  public DependentLink subst(ExprSubstitution exprSubst, LevelSubstitution levelSubst, int size) {
    if (size == 1) {
      TypedDependentLink result = new TypedDependentLink(isExplicit(), myName, getType(), EmptyDependentLink.getInstance());
      exprSubst.add(this, new ReferenceExpression(result));
      return result;
    } else
    if (size > 0) {
      UntypedDependentLink result = new UntypedDependentLink(myName);
      exprSubst.add(this, new ReferenceExpression(result));
      result.myNext = myNext.subst(exprSubst, levelSubst, size - 1);
      return result;
    } else {
      return EmptyDependentLink.getInstance();
    }
  }

  @Override
  public String toString() {
    return DependentLink.toString(this);
  }
}