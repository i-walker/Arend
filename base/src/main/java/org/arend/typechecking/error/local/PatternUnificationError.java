package org.arend.typechecking.error.local;

import org.arend.core.context.param.DependentLink;
import org.arend.core.pattern.ExpressionPattern;
import org.arend.ext.error.TypecheckingError;
import org.arend.ext.prettyprinting.PrettyPrinterConfig;
import org.arend.ext.prettyprinting.doc.LineDoc;
import org.arend.term.concrete.Concrete;
import org.jetbrains.annotations.NotNull;

import static org.arend.ext.prettyprinting.doc.DocFactory.*;

public class PatternUnificationError extends TypecheckingError {
  public final DependentLink parameter;
  public final ExpressionPattern pattern;

  public PatternUnificationError(DependentLink parameter, ExpressionPattern pattern, @NotNull Concrete.SourceNode cause) {
    super("", cause);
    this.parameter = parameter;
    this.pattern = pattern;
  }

  @Override
  public LineDoc getShortHeaderDoc(PrettyPrinterConfig ppConfig) {
    return hList(text("Cannot unify parameter '" + parameter.getName() + "' with pattern "), termLine(pattern.toPatternExpression(), ppConfig), text(" since the parameter is matched by an idp constructor"));
  }
}
