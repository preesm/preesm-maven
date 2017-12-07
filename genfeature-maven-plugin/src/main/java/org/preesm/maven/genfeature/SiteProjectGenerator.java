package org.preesm.maven.genfeature;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

/**
 * The Class SiteProjectGenerator.
 */
public class SiteProjectGenerator {

  /**
   * Generate project.
   *
   * @param generateAllInOneP2Feature
   *          the generate all in one P 2 feature
   * @throws FileNotFoundException
   *           the file not found exception
   * @throws UnsupportedEncodingException
   *           the unsupported encoding exception
   */
  public void generateProject(final GenerateAllInOneP2Feature generateAllInOneP2Feature) throws IOException {

    generateSitePomFile(generateAllInOneP2Feature);
    generateSiteCategoryFile(generateAllInOneP2Feature);
  }

  /**
   * Generate site category file.
   *
   * @param generateAllInOneP2Feature
   *          the generate all in one P 2 feature
   * @throws FileNotFoundException
   *           the file not found exception
   * @throws UnsupportedEncodingException
   *           the unsupported encoding exception
   */
  private void generateSiteCategoryFile(final GenerateAllInOneP2Feature generateAllInOneP2Feature) throws FileNotFoundException, UnsupportedEncodingException {
    final StringBuilder buffer = new StringBuilder();

    buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    buffer.append("<site>\n");
    buffer.append("   <feature url=\"features/" + generateAllInOneP2Feature.project.getGroupId() + "." + generateAllInOneP2Feature.featureId + "\" id=\""
        + generateAllInOneP2Feature.project.getGroupId() + "." + generateAllInOneP2Feature.featureId + "\" >\n");
    buffer.append("      <category name=\"" + generateAllInOneP2Feature.featureId + "_cat\"/>\n");
    buffer.append("   </feature>\n");
    buffer.append("   \n");
    buffer.append("   <category-def name=\"" + generateAllInOneP2Feature.featureId + "_cat\" label=\"" + generateAllInOneP2Feature.featureName + "\">\n");
    buffer.append("   </category-def>\n");
    buffer.append("</site>\n");
    buffer.append("");

    final PrintWriter writer = new PrintWriter(
        generateAllInOneP2Feature.currentWorkingDirectory.getAbsoluteFile() + "/" + GenerateAllInOneP2Feature.SECOND_LEVEL_FOLDER_NAME + "/"
            + GenerateAllInOneP2Feature.SECOND_LEVEL_SITE_PROJECT + "/" + GenerateAllInOneP2Feature.CATEGORY_FILE_NAME,
        GenerateAllInOneP2Feature.CHARSET);
    writer.println(buffer);
    writer.close();
  }

  /**
   * Generate site pom file.
   *
   * @param generateAllInOneP2Feature
   *          the generate all in one P 2 feature
   * @throws FileNotFoundException
   *           the file not found exception
   * @throws UnsupportedEncodingException
   *           the unsupported encoding exception
   */
  private void generateSitePomFile(final GenerateAllInOneP2Feature generateAllInOneP2Feature) throws IOException {
    final String pomContent = PomGenerator.generateSitePomFile(generateAllInOneP2Feature);
    final PrintWriter writer = new PrintWriter(
        generateAllInOneP2Feature.currentWorkingDirectory.getAbsoluteFile() + "/" + GenerateAllInOneP2Feature.SECOND_LEVEL_FOLDER_NAME + "/"
            + GenerateAllInOneP2Feature.SECOND_LEVEL_SITE_PROJECT + "/" + GenerateAllInOneP2Feature.MAVEN_PROJECT_FILE_NAME,
        GenerateAllInOneP2Feature.CHARSET);
    writer.println(pomContent);
    writer.close();
  }

}
