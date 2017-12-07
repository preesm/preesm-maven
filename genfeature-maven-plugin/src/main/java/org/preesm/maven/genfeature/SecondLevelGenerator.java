package org.preesm.maven.genfeature;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * The Class SecondLevelGenerator.
 */
public class SecondLevelGenerator {

  /**
   * Generate.
   *
   * @param generateAllInOneP2Feature
   *          the generate all in one P 2 feature
   * @throws MojoFailureException
   *           the mojo failure exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public void generate(final GenerateAllInOneP2Feature generateAllInOneP2Feature) throws IOException {

    // make directories
    new File(generateAllInOneP2Feature.currentWorkingDirectory.getAbsoluteFile() + "/" + GenerateAllInOneP2Feature.SECOND_LEVEL_FOLDER_NAME).mkdirs();
    new File(generateAllInOneP2Feature.currentWorkingDirectory.getAbsoluteFile() + "/" + GenerateAllInOneP2Feature.SECOND_LEVEL_FOLDER_NAME + "/"
        + GenerateAllInOneP2Feature.SECOND_LEVEL_FEATURE_PROJECT).mkdirs();
    new File(generateAllInOneP2Feature.currentWorkingDirectory.getAbsoluteFile() + "/" + GenerateAllInOneP2Feature.SECOND_LEVEL_FOLDER_NAME + "/"
        + GenerateAllInOneP2Feature.SECOND_LEVEL_SITE_PROJECT).mkdirs();

    generateFeature(generateAllInOneP2Feature);

  }

  /**
   * Generate feature.
   *
   * @param generateAllInOneP2Feature
   *          the generate all in one P 2 feature
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  private final void generateFeature(final GenerateAllInOneP2Feature generateAllInOneP2Feature) throws IOException {
    CheckParameters.checkParameters(generateAllInOneP2Feature);

    generateParentPomFile(generateAllInOneP2Feature);
    new FeatureProjectGenerator().generateProject(generateAllInOneP2Feature);
    new SiteProjectGenerator().generateProject(generateAllInOneP2Feature);
  }

  /**
   * Generate parent pom file.
   *
   * @param generateAllInOneP2Feature
   *          the generate all in one P 2 feature
   * @throws FileNotFoundException
   *           the file not found exception
   * @throws UnsupportedEncodingException
   *           the unsupported encoding exception
   */
  private void generateParentPomFile(final GenerateAllInOneP2Feature generateAllInOneP2Feature) throws FileNotFoundException, UnsupportedEncodingException {
    final StringBuilder buffer = new StringBuilder();

    buffer.append("<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
    buffer.append("  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n");
    buffer.append("  <modelVersion>4.0.0</modelVersion>\n");
    buffer.append("\n");
    buffer.append("  <artifactId>" + generateAllInOneP2Feature.project.getGroupId() + ".parent</artifactId>\n");
    buffer.append("  <groupId>" + generateAllInOneP2Feature.project.getGroupId() + "</groupId>\n");
    buffer.append("  <packaging>pom</packaging>\n");
    buffer.append("  <version>" + generateAllInOneP2Feature.project.getVersion() + "</version>\n");
    buffer.append("  \n");
    buffer.append("  <repositories>\n");
    buffer.append("    <repository>\n");
    buffer.append("      <id>original-site</id>\n");
    buffer.append("      <layout>p2</layout>\n");
    buffer.append("      <url>");
    buffer.append(generateAllInOneP2Feature.inputSite.toURI().toString());
    buffer.append("</url>\n");
    buffer.append("    </repository>\n");
    buffer.append("  </repositories>\n");
    buffer.append("  \n");
    buffer.append("  <modules>\n");
    buffer.append("  <module>feature</module>\n");
    buffer.append("  <module>site</module>\n");
    buffer.append("  </modules>\n");
    buffer.append("</project>\n");
    buffer.append("");

    final PrintWriter writer = new PrintWriter(generateAllInOneP2Feature.currentWorkingDirectory.getAbsoluteFile() + "/"
        + GenerateAllInOneP2Feature.SECOND_LEVEL_FOLDER_NAME + "/" + GenerateAllInOneP2Feature.MAVEN_PROJECT_FILE_NAME, "UTF-8");
    writer.println(buffer.toString());
    writer.close();
  }
}
