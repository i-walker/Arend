package org.arend.library;

import org.arend.ext.ArendExtension;
import org.arend.ext.DefaultArendExtension;
import org.arend.ext.error.ErrorReporter;
import org.arend.ext.module.ModulePath;
import org.arend.extImpl.ConcreteFactoryImpl;
import org.arend.extImpl.DefinitionContributorImpl;
import org.arend.extImpl.DefinitionProviderImpl;
import org.arend.library.classLoader.FileClassLoaderDelegate;
import org.arend.library.classLoader.MultiClassLoader;
import org.arend.library.error.LibraryError;
import org.arend.module.error.ExceptionError;
import org.arend.naming.reference.converter.IdReferableConverter;
import org.arend.naming.reference.converter.ReferableConverter;
import org.arend.prelude.Prelude;
import org.arend.source.BinarySource;
import org.arend.source.Source;
import org.arend.source.SourceLoader;
import org.arend.source.error.PersistingError;
import org.arend.term.group.ChildGroup;
import org.arend.typechecking.TypecheckerState;
import org.arend.typechecking.order.dependency.DependencyListener;
import org.arend.typechecking.order.dependency.DummyDependencyListener;
import org.arend.typechecking.order.listener.TypecheckingOrderingListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a library which can load modules in the binary format (see {@link #getBinarySource})
 * as well as ordinary modules (see {@link #getRawSource}).
 */
public abstract class SourceLibrary extends BaseLibrary {
  public enum Flag { RECOMPILE }
  private final EnumSet<Flag> myFlags = EnumSet.noneOf(Flag.class);
  private ArendExtension myExtension;

  /**
   * Creates a new {@code SourceLibrary}
   *
   * @param typecheckerState  the underling typechecker state of this library.
   */
  protected SourceLibrary(TypecheckerState typecheckerState) {
    super(typecheckerState);
  }

  /**
   * Adds a flag.
   */
  public void addFlag(Flag flag) {
    myFlags.add(flag);
  }

  /**
   * Removes a flag.
   */
  public void removeFlag(Flag flag) {
    myFlags.remove(flag);
  }

  /**
   * Gets the raw source (that is, the source containing not typechecked data) for a given module path.
   *
   * @param modulePath  a path to the source.
   *
   * @return the raw source corresponding to the given path or null if the source is not found.
   */
  @Nullable
  public abstract Source getRawSource(ModulePath modulePath);

  /**
   * Gets the binary source (that is, the source containing typechecked data) for a given module path.
   *
   * @param modulePath  a path to the source.
   *
   * @return the binary source corresponding to the given path or null if the source is not found.
   */
  @Nullable
  public abstract BinarySource getBinarySource(ModulePath modulePath);

  @Nullable
  @Override
  public ArendExtension getArendExtension() {
    return myExtension;
  }

  /**
   * Loads the header of this library.
   *
   * @param errorReporter a reporter for all errors that occur during the loading process.
   *
   * @return loaded library header, or null if some error occurred.
   */
  @Nullable
  protected abstract LibraryHeader loadHeader(ErrorReporter errorReporter);

  /**
   * Invoked by a source after it loads the group of a module.
   *
   * @param modulePath  the path to the loaded module.
   * @param group       the group of the loaded module or null if the group was not loaded.
   * @param isRaw       true if the module was loaded from a raw source, false otherwise.
   */
  public void onGroupLoaded(ModulePath modulePath, @Nullable ChildGroup group, boolean isRaw) {

  }

  /**
   * Invoked by a binary source after it is loaded.
   *
   * @param modulePath  the path to the loaded module.
   * @param isComplete  true if the module was loaded completely, false otherwise.
   */
  public void onBinaryLoaded(ModulePath modulePath, boolean isComplete) {

  }

  /**
   * Checks if this library has any raw sources.
   * Note that currently libraries without raw sources do not work properly with class synonyms.
   *
   * @return true if the library has raw sources, false otherwise.
   */
  public boolean hasRawSources() {
    return true;
  }

  /**
   * Gets a referable converter which is used during loading of binary sources without raw counterparts.
   *
   * @return a referable converter or null if the library does not have raw sources.
   */
  @Nullable
  public ReferableConverter getReferableConverter() {
    return IdReferableConverter.INSTANCE;
  }

  /**
   * Gets a dependency listener for definitions loaded from binary sources.
   *
   * @return a dependency listener.
   */
  @NotNull
  public DependencyListener getDependencyListener() {
    return DummyDependencyListener.INSTANCE;
  }

  /**
   * Indicates whether the library should be loaded if some errors occur.
   *
   * @return true if the library should be loaded despite errors, false otherwise.
   */
  protected boolean mustBeLoaded() {
    return false;
  }

  @Override
  public boolean load(LibraryManager libraryManager, TypecheckingOrderingListener typechecking) {
    if (isLoaded()) {
      return true;
    }

    LibraryHeader header = loadHeader(libraryManager.getLibraryErrorReporter());
    if (header == null) {
      return false;
    }
    if (!header.languageVersionRange.inRange(Prelude.VERSION)) {
      libraryManager.getLibraryErrorReporter().report(LibraryError.incorrectVersion(header.languageVersionRange));
      if (!mustBeLoaded()) {
        return false;
      }
    }

    MultiClassLoader<Library> classLoader = libraryManager.getClassLoader(isExternal());
    if (header.extBasePath != null && header.extMainClass != null) {
      classLoader.addDelegate(this, new FileClassLoaderDelegate(header.extBasePath));
    }

    Map<String, ArendExtension> dependenciesExtensions = new LinkedHashMap<>();
    for (LibraryDependency dependency : header.dependencies) {
      Library loadedDependency = libraryManager.loadDependency(this, dependency.name, typechecking);
      if (loadedDependency == null && !mustBeLoaded()) {
        classLoader.removeDelegate(this);
        return false;
      }

      if (loadedDependency != null) {
        libraryManager.registerDependency(this, loadedDependency);
        ArendExtension extension = loadedDependency.getArendExtension();
        if (extension != null) {
          dependenciesExtensions.put(dependency.name, extension);
        }
      }
    }

    libraryManager.beforeLibraryLoading(this);

    try {
      Class<?> extMainClass = null;
      if (header.extBasePath != null && header.extMainClass != null) {
        extMainClass = classLoader.loadClass(header.extMainClass);
        if (!ArendExtension.class.isAssignableFrom(extMainClass)) {
          libraryManager.getLibraryErrorReporter().report(LibraryError.incorrectExtensionClass(getName()));
          extMainClass = null;
        }
      }

      if (extMainClass != null) {
        myExtension = (ArendExtension) extMainClass.getDeclaredConstructor().newInstance();
      }
    } catch (Exception e) {
      classLoader.removeDelegate(this);
      libraryManager.getLibraryErrorReporter().report(new ExceptionError(e, "loading of library " + getName()));
    }
    if (myExtension == null && !dependenciesExtensions.isEmpty()) {
      myExtension = new DefaultArendExtension();
    }

    if (myExtension != null) {
      DefinitionContributorImpl contributor = new DefinitionContributorImpl(this, libraryManager.getLibraryErrorReporter(), libraryManager.getExtensionModuleScopeProvider(isExternal()));
      myExtension.declareDefinitions(contributor);
      contributor.disable();
    }

    try {
      SourceLoader sourceLoader = new SourceLoader(this, libraryManager);
      if (hasRawSources()) {
        for (ModulePath module : header.modules) {
          sourceLoader.preloadRaw(module);
        }
        sourceLoader.loadRawSources();
      }

      if (!myFlags.contains(Flag.RECOMPILE)) {
        for (ModulePath module : header.modules) {
          sourceLoader.loadBinary(module);
        }
      }
    } catch (Throwable e) {
      libraryManager.afterLibraryLoading(this, false);
      throw e;
    }

    if (myExtension != null) {
      myExtension.setDependencies(dependenciesExtensions);
      myExtension.setPrelude(new Prelude());
      myExtension.setConcreteFactory(new ConcreteFactoryImpl(null));
      myExtension.setModuleScopeProvider(getModuleScopeProvider());

      DefinitionProviderImpl provider = new DefinitionProviderImpl(typechecking);
      myExtension.load(provider);
      provider.disable();
    }

    libraryManager.afterLibraryLoading(this, true);

    return super.load(libraryManager, typechecking);
  }

  @Override
  public boolean containsModule(ModulePath modulePath) {
    Source source = getRawSource(modulePath);
    if (source != null && source.isAvailable()) {
      return true;
    }
    source = getBinarySource(modulePath);
    return source != null && source.isAvailable();
  }

  public boolean supportsPersisting() {
    return true;
  }

  public boolean persistModule(ModulePath modulePath, ReferableConverter referableConverter, ErrorReporter errorReporter) {
    BinarySource source = getBinarySource(modulePath);
    if (source == null) {
      errorReporter.report(new PersistingError(modulePath));
      return false;
    } else {
      return source.persist(this, referableConverter, errorReporter);
    }
  }

  public boolean deleteModule(ModulePath modulePath) {
    BinarySource source = getBinarySource(modulePath);
    return source != null && source.delete(this);
  }
}
