package org.arend.source;

import org.arend.module.ModulePath;

import javax.annotation.Nonnull;
import java.io.File;

/**
 * Represents a persisted module.
 */
public interface Source {
  /**
   * Gets the path to this source.
   *
   * @return path to the source.
   */
  @Nonnull
  ModulePath getModulePath();

  /**
   * Loads the source. Also loads all dependencies of this source.
   *
   * @param sourceLoader    the state of the loading process.
   *
   * @return true if loading succeeded, false otherwise.
   */
  boolean load(SourceLoader sourceLoader);

  /**
   * Gets the timestamp for this source.
   *
   * @return timestamp
   * @see File#lastModified
   */
  long getTimeStamp();

  /**
   * Checks if the source is available for loading.
   *
   * @return true if the source can be loaded and/or persisted, false otherwise.
   */
  boolean isAvailable();
}