package org.arend.core.expr;

import org.arend.core.definition.ClassField;
import org.arend.core.expr.visitor.ExpressionVisitor;
import org.arend.core.expr.visitor.ExpressionVisitor2;
import org.arend.core.sort.Sort;
import org.arend.ext.core.expr.CoreExpressionVisitor;
import org.arend.ext.core.expr.CoreFieldCallExpression;
import org.arend.util.Decision;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class FieldCallExpression extends DefCallExpression implements CoreFieldCallExpression {
  private final Expression myArgument;

  private FieldCallExpression(ClassField definition, Sort sortArgument, Expression argument) {
    super(definition, sortArgument);
    myArgument = argument;
  }

  public static Expression make(ClassField definition, Sort sortArgument, Expression thisExpr) {
    if (definition.isProperty()) {
      return new FieldCallExpression(definition, sortArgument, thisExpr);
    }

    thisExpr = thisExpr.getUnderlyingExpression();
    if (thisExpr instanceof NewExpression) {
      Expression impl = ((NewExpression) thisExpr).getImplementation(definition);
      assert impl != null;
      return impl;
    } else if (thisExpr instanceof ReferenceExpression && ((ReferenceExpression) thisExpr).getBinding() instanceof ClassCallExpression.ClassCallBinding) {
      Expression impl = ((ClassCallExpression.ClassCallBinding) ((ReferenceExpression) thisExpr).getBinding()).getTypeExpr().getImplementation(definition, thisExpr);
      if (impl != null) {
        return impl;
      }
    } else if (thisExpr instanceof ErrorExpression && ((ErrorExpression) thisExpr).getExpression() != null) {
      return new FieldCallExpression(definition, sortArgument, ((ErrorExpression) thisExpr).replaceExpression(null));
    }

    return new FieldCallExpression(definition, sortArgument, thisExpr);
  }

  @NotNull
  @Override
  public Expression getArgument() {
    return myArgument;
  }

  @Override
  public List<? extends Expression> getDefCallArguments() {
    return Collections.singletonList(myArgument);
  }

  @NotNull
  @Override
  public ClassField getDefinition() {
    return (ClassField) super.getDefinition();
  }

  @Override
  public boolean canBeConstructor() {
    return !getDefinition().isProperty();
  }

  @Override
  public <P, R> R accept(ExpressionVisitor<? super P, ? extends R> visitor, P params) {
    return visitor.visitFieldCall(this, params);
  }

  @Override
  public <P1, P2, R> R accept(ExpressionVisitor2<? super P1, ? super P2, ? extends R> visitor, P1 param1, P2 param2) {
    return visitor.visitFieldCall(this, param1, param2);
  }

  @Override
  public <P, R> R accept(@NotNull CoreExpressionVisitor<? super P, ? extends R> visitor, P params) {
    return visitor.visitFieldCall(this, params);
  }

  @Override
  public Decision isWHNF() {
    if (getDefinition().isProperty()) {
      return Decision.YES;
    }
    if (myArgument.isInstance(NewExpression.class)) {
      return Decision.NO;
    }
    Expression type = myArgument.getType(false);
    if (type == null) {
      return Decision.MAYBE;
    }
    ClassCallExpression classCall = type.cast(ClassCallExpression.class);
    return classCall == null ? Decision.MAYBE : classCall.isImplemented(getDefinition()) ? Decision.NO : Decision.YES;
  }

  @Override
  public Expression getStuckExpression() {
    return myArgument.getStuckExpression();
  }
}
