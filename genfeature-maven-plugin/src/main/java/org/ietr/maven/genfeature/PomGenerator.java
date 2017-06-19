package org.ietr.maven.genfeature;

public class PomGenerator {

  private static final String TYCHO_GROUP_ID = "org.eclipse.tycho";

  private PomGenerator() {
  }

  public static final String generateFeaturePomFile(final GenerateAllInOneP2Feature generateAllInOneP2Feature) {
    final StringBuilder buffer = new StringBuilder();
    PomGenerator.headers(buffer);
    PomGenerator.artifactId(generateAllInOneP2Feature.project.getGroupId() + "." + generateAllInOneP2Feature.featureId, buffer);
    PomGenerator.packaging("eclipse-feature", buffer);

    PomGenerator.parent(generateAllInOneP2Feature.project.getGroupId() + ".parent", generateAllInOneP2Feature.project.getGroupId(),
        generateAllInOneP2Feature.project.getVersion(), "..", buffer);

    startPlugins(buffer);
    plugin("tycho-maven-plugin", TYCHO_GROUP_ID, GenerateAllInOneP2Feature.TYCHO_VERSION, "<extensions>true</extensions>", buffer);
    endPlugins(buffer);

    PomGenerator.footer(buffer);

    return buffer.toString();
  }

  private static void endPlugins(StringBuilder buffer) {
    buffer.append("    </plugins>\n");
    buffer.append("  </build>\n");
  }

  private static void startPlugins(StringBuilder buffer) {
    buffer.append("  <build>\n");
    buffer.append("    <plugins>\n");
  }

  public static final String generateSitePomFile(final GenerateAllInOneP2Feature generateAllInOneP2Feature) {
    final StringBuilder buffer = new StringBuilder();

    PomGenerator.headers(buffer);
    PomGenerator.artifactId("org.ietr.externaldeps.dependency.site", buffer);
    PomGenerator.packaging("eclipse-repository", buffer);
    PomGenerator.parent(generateAllInOneP2Feature.project.getGroupId() + ".parent", generateAllInOneP2Feature.project.getGroupId(),
        generateAllInOneP2Feature.project.getVersion(), "..", buffer);

    startPlugins(buffer);

    plugin("tycho-maven-plugin", TYCHO_GROUP_ID, GenerateAllInOneP2Feature.TYCHO_VERSION, "<extensions>true</extensions>", buffer);

    String extraConfig = "<configuration>\n" + "<includeAllDependencies>true</includeAllDependencies>\n" + "<compress>false</compress>\n" + "<repositoryName>"
        + generateAllInOneP2Feature.featureProvider + " Update Site</repositoryName>\n" + "</configuration>\n";
    plugin("tycho-p2-repository-plugin", TYCHO_GROUP_ID, GenerateAllInOneP2Feature.TYCHO_VERSION, extraConfig, buffer);

    endPlugins(buffer);

    PomGenerator.footer(buffer);

    return buffer.toString();
  }

  private static final void headers(final StringBuilder buffer) {
    buffer.append("<project xmlns=\"http://maven.apache.org/POM/4.0.0\" ");
    buffer.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
    buffer.append("xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n");
    buffer.append("<modelVersion>4.0.0</modelVersion>\n");
    buffer.append("\n");
  }

  private static void footer(final StringBuilder buffer) {
    buffer.append("</project>\n");
    buffer.append("\n");
    buffer.append("");
  }

  private static final void plugin(final String artifactId, final String groupId, final String version, final String extraConfig, final StringBuilder buffer) {

    buffer.append("<plugin>\n");
    groupId(groupId, buffer);
    artifactId(artifactId, buffer);
    version(version, buffer);
    buffer.append(extraConfig + "\n");
    buffer.append("</plugin>\n");
  }

  private static void packaging(final String packagingType, final StringBuilder buffer) {
    buffer.append("<packaging>");
    buffer.append(packagingType);
    buffer.append("</packaging>\n");
    buffer.append("\n");
  }

  private static void parent(final String artifactId, final String groupId, final String version, final String relativePath, final StringBuilder buffer) {
    buffer.append("<parent>\n");

    PomGenerator.artifactId(artifactId, buffer);
    PomGenerator.groupId(groupId, buffer);
    PomGenerator.version(version, buffer);
    PomGenerator.relativePath(relativePath, buffer);

    buffer.append("</parent>\n");
    buffer.append("\n");
  }

  private static void relativePath(final String relativePath, final StringBuilder buffer) {
    buffer.append("<relativePath>" + relativePath + "</relativePath>\n");
  }

  private static void version(final String version, final StringBuilder buffer) {
    buffer.append("<version>" + version + "</version>\n");
  }

  private static void groupId(final String groupId, final StringBuilder buffer) {
    buffer.append("<groupId>" + groupId + "</groupId>\n");
  }

  private static void artifactId(final String artifactId, final StringBuilder buffer) {
    buffer.append("<artifactId>" + artifactId + "</artifactId>\n");
  }
}
