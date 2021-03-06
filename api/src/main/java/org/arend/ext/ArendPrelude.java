package org.arend.ext;

import org.arend.ext.core.definition.CoreConstructor;
import org.arend.ext.core.definition.CoreDataDefinition;
import org.arend.ext.core.definition.CoreFunctionDefinition;

/**
 * Provides access to the definitions in the prelude.
 */
public interface ArendPrelude {
  CoreDataDefinition getInterval();
  CoreConstructor getLeft();
  CoreConstructor getRight();
  CoreFunctionDefinition getSqueeze();
  CoreFunctionDefinition getSqueezeR();
  CoreDataDefinition getNat();
  CoreConstructor getZero();
  CoreConstructor getSuc();
  CoreFunctionDefinition getPlus();
  CoreFunctionDefinition getMul();
  CoreFunctionDefinition getMinus();
  CoreDataDefinition getInt();
  CoreConstructor getPos();
  CoreConstructor getNeg();
  CoreFunctionDefinition getFromNat();
  CoreFunctionDefinition getCoerce();
  CoreFunctionDefinition getCoerce2();
  CoreDataDefinition getPath();
  CoreFunctionDefinition getEquality();
  CoreConstructor getPathCon();
  CoreFunctionDefinition getInProp();
  CoreFunctionDefinition getIdp();
  CoreFunctionDefinition getAt();
  CoreFunctionDefinition getIso();
  CoreDataDefinition getLessOrEq();
  CoreConstructor getZeroLessOrEq();
  CoreConstructor getSucLessOrEq();
  CoreFunctionDefinition getDivMod();
  CoreFunctionDefinition getDiv();
  CoreFunctionDefinition getMod();
  CoreFunctionDefinition getDivModProp();
  CoreFunctionDefinition getModProp();
}
