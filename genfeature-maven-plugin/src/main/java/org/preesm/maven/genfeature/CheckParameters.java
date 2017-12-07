package org.preesm.maven.genfeature;

import java.io.File;
import java.io.IOException;

/**
 * The Class CheckParameters.
 */
public class CheckParameters {

  private CheckParameters() {
  }

  /**
   * Check feature name.
   *
   * @param featureName
   *          the feature name
   */
  private static final void checkFeatureName(final String featureName) {
    if (featureName == null) {
      throw new NullPointerException();
    }
    if (featureName.isEmpty()) {
      throw new IllegalArgumentException();
    }
  }

  /**
   * Check input folder.
   *
   * @param inputSite
   *          the input site
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  private static void checkInputFolder(final File inputSite) throws IOException {
    final boolean exists = inputSite.exists();
    if (!exists) {
      throw new IOException("Could not locate input folder [" + inputSite + "]");
    }
    final boolean canRead = inputSite.canRead();
    if (!canRead) {
      throw new IOException("Can not read input folder [" + inputSite + "]");
    }
    final boolean isDir = inputSite.isDirectory();
    if (!isDir) {
      throw new IOException("Input folder is not a directory [" + inputSite + "]");
    }
    final File[] list = inputSite.listFiles();
    if (list == null) {
      throw new IOException("Input folder has a null list of children");
    }
    final boolean isEmpty = list.length == 0;
    if (isEmpty) {
      throw new IOException("Input folder is empty [" + inputSite + "]");
    }

    boolean containsPluginFolder = false;
    boolean containsContentXml = false;

    for (final File file : list) {
      if ("plugins".equals(file.getName())) {
        containsPluginFolder = true;
        CheckParameters.checkPluginFolder(file);
      }
      if ("content.xml".equals(file.getName())) {
        containsContentXml = true;
      }
    }
    if (!containsContentXml) {
      throw new IllegalArgumentException("Could not locate content.xml file");
    }
    if (!containsPluginFolder) {
      throw new IllegalArgumentException("Could not locate plugins folder");
    }
  }

  /**
   * Check output folder.
   *
   * @param outputDirectory
   *          the output directory
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  private static void checkOutputFolder(final File outputDirectory) throws IOException {
    final boolean exists = outputDirectory.exists();
    if (exists) {
      final boolean canWrite = outputDirectory.canWrite();
      if (!canWrite) {
        throw new IOException("Output exists but is not writeable [" + outputDirectory + "]");
      }
      final boolean isDir = outputDirectory.isDirectory();
      if (!isDir) {
        throw new IOException("Output exists but is not a direcetory [" + outputDirectory + "]");
      }
    } else {
      final boolean mkdirs = outputDirectory.mkdirs();
      if (!mkdirs) {
        throw new IOException("Output exists but is not a direcetory [" + outputDirectory + "]");
      }
    }
  }

  /**
   * Check parameters.
   *
   * @param generateAllInOneP2Feature
   *          the generate all in one P 2 feature
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public static final void checkParameters(final GenerateAllInOneP2Feature generateAllInOneP2Feature) throws IOException {
    CheckParameters.checkInputFolder(generateAllInOneP2Feature.inputSite);
    CheckParameters.checkOutputFolder(generateAllInOneP2Feature.outputDirectory);
    CheckParameters.checkFeatureName(generateAllInOneP2Feature.featureName);
  }

  /**
   * Check plugin folder.
   *
   * @param inputPluginFolder
   *          the input plugin folder
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  private static final void checkPluginFolder(final File inputPluginFolder) throws IOException {

    final boolean exists = inputPluginFolder.exists();
    if (!exists) {
      throw new IOException("Could not locate input plugin folder [" + inputPluginFolder + "]");
    }
    final boolean canRead = inputPluginFolder.canRead();
    if (!canRead) {
      throw new IOException("Can not read input plugin folder [" + inputPluginFolder + "]");
    }
    final boolean isDir = inputPluginFolder.isDirectory();
    if (!isDir) {
      throw new IOException("Input plugin folder is not a directory [" + inputPluginFolder + "]");
    }
    final File[] list = inputPluginFolder.listFiles();
    if (list == null) {
      throw new IOException("Input plugin folder has a null list of children");
    }
    final boolean isEmpty = list.length == 0;
    if (isEmpty) {
      throw new IOException("Input plugin folder is empty [" + inputPluginFolder + "]");
    }
  }
}
