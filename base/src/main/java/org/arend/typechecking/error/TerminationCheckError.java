package org.arend.typechecking.error;

import org.arend.core.definition.Definition;
import org.arend.ext.error.GeneralError;
import org.arend.ext.prettyprinting.PrettyPrinterConfig;
import org.arend.ext.prettyprinting.doc.Doc;
import org.arend.naming.reference.GlobalReferable;
import org.arend.typechecking.termination.CompositeCallMatrix;
import org.arend.typechecking.termination.RecursiveBehavior;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.stream.Collectors;

import static org.arend.ext.prettyprinting.doc.DocFactory.*;

public class TerminationCheckError extends GeneralError {
  public final GlobalReferable definition;
  public final Set<RecursiveBehavior<Definition>> behaviors;

  public TerminationCheckError(Definition def, Set<RecursiveBehavior<Definition>> behaviors) {
    super(Level.ERROR, "Termination check failed");
    definition = def.getReferable();
    this.behaviors = behaviors;
  }

  @Override
  public Object getCause() {
    return definition;
  }

  @Override
  public Doc getBodyDoc(PrettyPrinterConfig ppConfig) {
    return vList(behaviors.stream().map(rb -> printBehavior(rb, ppConfig)).collect(Collectors.toList()));
  }

  private static Doc printBehavior(RecursiveBehavior rb, PrettyPrinterConfig ppConfig) {
    return hang(text(rb.initialCallMatrix instanceof CompositeCallMatrix ? "Problematic sequence of recursive calls:" : "Problematic recursive call:"), rb.initialCallMatrix.getMatrixLabel(ppConfig));
  }

  @NotNull
  @Override
  public Stage getStage() {
    return Stage.TYPECHECKER;
  }

  @Override
  public boolean hasExpressions() {
    return true;
  }
}
