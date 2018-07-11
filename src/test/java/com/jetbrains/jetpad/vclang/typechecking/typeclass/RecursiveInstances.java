package com.jetbrains.jetpad.vclang.typechecking.typeclass;

import com.jetbrains.jetpad.vclang.term.group.ChildGroup;
import com.jetbrains.jetpad.vclang.typechecking.TypeCheckingTestCase;
import org.junit.Test;

import static com.jetbrains.jetpad.vclang.typechecking.Matchers.instanceInference;
import static com.jetbrains.jetpad.vclang.typechecking.Matchers.typeMismatchError;

public class RecursiveInstances extends TypeCheckingTestCase {
  @Test
  public void instanceWithParameter() {
    typeCheckModule(
      "\\class A { | a : Nat }\n" +
      "\\class B\n" +
      "\\instance B-inst : B\n" +
      "\\instance A-inst {b : B} : A | a => 0\n" +
      "\\func f => a");
  }

  @Test
  public void noRecursiveInstance() {
    ChildGroup group = typeCheckModule(
      "\\class A { | a : Nat }\n" +
      "\\class B\n" +
      "\\instance A-inst {b : B} : A | a => 0\n" +
      "\\func f => a", 1);
    assertThatErrorsAre(instanceInference(getDefinition(group, "B").getReferable()));
  }

  @Test
  public void correctRecursiveInstance() {
    typeCheckModule(
      "\\class A { | a : Nat }\n" +
      "\\class B (n : Nat)\n" +
      "\\instance B-inst : B | n => 1\n" +
      "\\instance A-inst {b : B 1} : A | a => 0\n" +
      "\\func f => a");
  }

  @Test
  public void wrongRecursiveInstance() {
    ChildGroup group = typeCheckModule(
      "\\class A { | a : Nat }\n" +
      "\\class B (n : Nat)\n" +
      "\\instance B-inst : B 0\n" +
      "\\instance A-inst {b : B 1} : A | a => 0\n" +
      "\\func f => a", 1);
    assertThatErrorsAre(instanceInference(getDefinition(group, "B").getReferable()));
  }

  @Test
  public void wrongRecursiveInstance2() {
    typeCheckModule(
      "\\class A { | a : Nat }\n" +
      "\\data Data (A : \\Set)\n" +
      "\\data D\n" +
      "\\data D'\n" +
      "\\class B (X : \\Set)\n" +
      "\\instance B-inst : B (Data D)\n" +
      "\\instance A-inst {b : B (Data D')} : A | a => 0\n" +
      "\\func f => a", 1);
    assertThatErrorsAre(typeMismatchError());
  }

  @Test
  public void localRecursiveInstance() {
    typeCheckModule(
      "\\class A { | a : Nat }\n" +
      "\\class B (n : Nat)\n" +
      "\\instance A-inst {b : B 0} : A | a => 0\n" +
      "\\func f {c : B 0} => a");
  }

  @Test
  public void recursiveLocalInstance() {
    typeCheckModule(
      "\\class A (X : \\Set) { | x : X }\n" +
      "\\data Data (X : \\Set) | con X\n" +
      "\\instance Nat-inst : A Nat | x => 0\n" +
      "\\instance Data-inst {T : \\Set} {d : A T} : A (Data T) | x => con x\n" +
      "\\func f : Data Nat => x");
  }

  @Test
  public void recursiveLocalInstance2() {
    typeCheckModule(
      "\\class A (X Y : \\Set) { | x : X }\n" +
      "\\data Data (X : \\Set) | con X\n" +
      "\\instance Nat-inst : A Nat | x => 0 | Y => Nat\n" +
      "\\instance Data-inst {a : A} : A (Data a.X) | x => con x | Y => Nat\n" +
      "\\func f : Data Nat => x");
  }

  @Test
  public void noRecursiveLocalInstance() {
    ChildGroup group = typeCheckModule(
      "\\class A (X : \\Set) { | x : X }\n" +
      "\\data Data (X : \\Set) | con X\n" +
      "\\data Nat' | nat\n" +
      "\\instance Nat-inst : A Nat' | x => nat\n" +
      "\\instance Data-inst {T : \\Set} {d : A T} : A (Data T) | x => con x\n" +
      "\\func f : Data Nat => x", 1);
    assertThatErrorsAre(instanceInference(getDefinition(group, "A").getReferable()));
  }

  @Test
  public void noRecursiveLocalInstance2() {
    ChildGroup group = typeCheckModule(
      "\\class A (X Y : \\Set) { | x : X }\n" +
      "\\data Data (X : \\Set) | con X\n" +
      "\\instance Nat-inst : A Nat | x => 0 | Y => Nat\n" +
      "\\instance Data-inst {a : A} : A (Data a.Y) | x => con x | Y => Nat", 1);
    assertThatErrorsAre(instanceInference(getDefinition(group, "A").getReferable()));
  }
}