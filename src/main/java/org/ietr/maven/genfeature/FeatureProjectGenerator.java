package org.ietr.maven.genfeature;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import org.codehaus.plexus.util.FileUtils;

/**
 * The Class FeatureProjectGenerator.
 */
public class FeatureProjectGenerator {

  /**
   * Generate build properties.
   *
   * @param generateAllInOneP2Feature
   *          the generate all in one P 2 feature
   * @throws FileNotFoundException
   *           the file not found exception
   * @throws UnsupportedEncodingException
   *           the unsupported encoding exception
   */
  private void generateBuildProperties(final GenerateAllInOneP2Feature generateAllInOneP2Feature) throws FileNotFoundException, UnsupportedEncodingException {
    PrintWriter writer;
    final String buildPropertiesContent = "bin.includes = " + GenerateAllInOneP2Feature.FEATURE_FILE_NAME;

    writer = new PrintWriter(
        generateAllInOneP2Feature.currentWorkingDirectory.getAbsoluteFile() + "/" + GenerateAllInOneP2Feature.SECOND_LEVEL_FOLDER_NAME + "/"
            + GenerateAllInOneP2Feature.SECOND_LEVEL_FEATURE_PROJECT + "/" + GenerateAllInOneP2Feature.BUILD_PROPERTIES_FILE_NAME,
        GenerateAllInOneP2Feature.CHARSET);
    writer.println(buildPropertiesContent);
    writer.close();
  }

  /**
   * Generate feature file.
   *
   * @param generateAllInOneP2Feature
   *          the generate all in one P 2 feature
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  private void generateFeatureFile(final GenerateAllInOneP2Feature generateAllInOneP2Feature) throws IOException {

    final List<PluginIU> pluginList = generatePluginList(generateAllInOneP2Feature.inputSite);

    final StringBuilder buffer = new StringBuilder();
    buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    buffer.append("\n");
    buffer.append("<feature id=\"" + generateAllInOneP2Feature.project.getGroupId() + ".").append(generateAllInOneP2Feature.featureId).append("\" label=\"")
        .append(generateAllInOneP2Feature.featureName)
        .append("\" version=\"" + generateAllInOneP2Feature.project.getVersion() + "\" provider-name=\"" + generateAllInOneP2Feature.featureProvider + "\">\n");
    buffer.append("\n");
    for (final PluginIU plugin : pluginList) {
      buffer.append("\t" + plugin.generateFeatureSection() + "\n");
    }
    buffer.append("\n");
    buffer.append("</feature>\n");
    buffer.append("\n");

    final PrintWriter writer = new PrintWriter(
        generateAllInOneP2Feature.currentWorkingDirectory.getAbsoluteFile() + "/" + GenerateAllInOneP2Feature.SECOND_LEVEL_FOLDER_NAME + "/"
            + GenerateAllInOneP2Feature.SECOND_LEVEL_FEATURE_PROJECT + "/" + GenerateAllInOneP2Feature.FEATURE_FILE_NAME,
        GenerateAllInOneP2Feature.CHARSET);
    writer.println(buffer.toString());
    writer.close();
  }

  /**
   * Generate feature pom file.
   *
   * @param generateAllInOneP2Feature
   *          the generate all in one P 2 feature
   * @throws FileNotFoundException
   *           the file not found exception
   * @throws UnsupportedEncodingException
   *           the unsupported encoding exception
   */
  private void generateFeaturePomFile(final GenerateAllInOneP2Feature generateAllInOneP2Feature) throws IOException {
    final String pomContent = PomGenerator.generateFeaturePomFile(generateAllInOneP2Feature);
    final PrintWriter writer = new PrintWriter(
        generateAllInOneP2Feature.currentWorkingDirectory.getAbsoluteFile() + "/" + GenerateAllInOneP2Feature.SECOND_LEVEL_FOLDER_NAME + "/"
            + GenerateAllInOneP2Feature.SECOND_LEVEL_FEATURE_PROJECT + "/" + GenerateAllInOneP2Feature.MAVEN_PROJECT_FILE_NAME,
        GenerateAllInOneP2Feature.CHARSET);
    writer.println(pomContent);
    writer.close();
  }

  /**
   * Generate plugin list.
   *
   * @param inputSite
   *          the input site
   * @return the list
   * @throws IOException
   */
  private final List<PluginIU> generatePluginList(final File inputSite) throws IOException {
    final List<PluginIU> pluginList = new ArrayList<>();
    final String pluginsPath = inputSite.getAbsolutePath() + "/plugins";

    final File pluginFolder = new File(pluginsPath);
    final File[] listFiles = pluginFolder.listFiles();
    if (listFiles == null) {
      throw new IOException("Plugin folder has a null children list");
    }

    for (final File file : listFiles) {
      final String fileName = file.getName();
      final String fileExtension = FileUtils.extension(fileName);
      final boolean isDirectory = file.isDirectory();
      final boolean isJar = "jar".equalsIgnoreCase(fileExtension);
      if (isDirectory || !isJar) {
        // skip directories and non jar files
        continue;
      }
      pluginList.add(PluginIU.build(fileName));
    }
    return pluginList;
  }

  /**
   * Generate project.
   *
   * @param generateAllInOneP2Feature
   *          the generate all in one P 2 feature
   * @throws FileNotFoundException
   *           the file not found exception
   * @throws UnsupportedEncodingException
   *           the unsupported encoding exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public void generateProject(final GenerateAllInOneP2Feature generateAllInOneP2Feature) throws IOException {
    generateFeaturePomFile(generateAllInOneP2Feature);
    generateFeatureFile(generateAllInOneP2Feature);
    generateBuildProperties(generateAllInOneP2Feature);
  }
}
