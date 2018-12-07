package org.preesm.maven.genfeature;

import java.io.File;
import java.util.Arrays;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.CommandLineException;

/**
 * The Class GenerateAllInOneP2Feature.
 */
@Mojo(name = "generate-feature", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class GenerateAllInOneP2Feature extends AbstractMojo {

  public static final String CHARSET = "UTF-8";

  /** The Constant SECOND_LEVEL_FOLDER_NAME. */
  public static final String SECOND_LEVEL_FOLDER_NAME = "secondLevel";

  /** The Constant SECOND_LEVEL_FEATURE_PROJECT. */
  public static final String SECOND_LEVEL_FEATURE_PROJECT = "feature";

  /** The Constant FEATURE_FILE_NAME. */
  public static final String FEATURE_FILE_NAME = "feature.xml";

  /** The Constant CATEGORY_FILE_NAME. */
  public static final String CATEGORY_FILE_NAME = "category.xml";

  /** The Constant BUILD_PROPERTIES_FILE_NAME. */
  public static final String BUILD_PROPERTIES_FILE_NAME = "build.properties";

  /** The Constant SECOND_LEVEL_SITE_PROJECT. */
  public static final String SECOND_LEVEL_SITE_PROJECT = "site";

  /** The Constant MAVEN_PROJECT_FILE_NAME. */
  public static final String MAVEN_PROJECT_FILE_NAME = "pom.xml";

  /** The Constant TYCHO_VERSION. */
  public static final String TYCHO_VERSION = "1.2.0";

  /** The project. */
  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  public MavenProject project;

  /** The current working directory. */
  @Parameter(defaultValue = "${project.build.directory}", required = true)
  public File currentWorkingDirectory;

  /** The input site. */
  @Parameter(defaultValue = "${project.build.directory}/repository/", property = "inputSite", required = true)
  public File inputSite;

  /** The output directory. */
  @Parameter(defaultValue = "${project.build.directory}/repository-featured/", property = "outputDirectory", required = true)
  public File outputDirectory;

  /** The feature name. */
  @Parameter(defaultValue = "All in One Dependencies", property = "featureName", required = true)
  public String featureName;

  /** The feature id. */
  @Parameter(defaultValue = "aio.deps", property = "featureId", required = true)
  public String featureId;

  /** The feature provider. */
  @Parameter(defaultValue = "Provider", property = "featureProvider", required = true)
  public String featureProvider;

  /**
   * Call 2 nd level.
   *
   * @throws MavenInvocationException
   *           the maven invocation exception
   */
  private void call2ndLevel() throws MavenInvocationException {
    final InvocationRequest request = new DefaultInvocationRequest();
    request.setDebug(false);
    request.setPomFile(new File(this.currentWorkingDirectory.getAbsolutePath() + "/" + GenerateAllInOneP2Feature.SECOND_LEVEL_FOLDER_NAME + "/"
        + GenerateAllInOneP2Feature.MAVEN_PROJECT_FILE_NAME));
    request.setGoals(Arrays.asList("clean", "package"));
    final Invoker invoker = new DefaultInvoker();
    final InvocationResult result = invoker.execute(request);
    if (result.getExitCode() != 0) {
      final CommandLineException executionException = result.getExecutionException();
      throw new IllegalStateException("Build failed: return code != 0", executionException);
    }
  }

  /**
   *
   */
  @Override
  public void execute() throws MojoFailureException {
    getLog().info("Starting all-in-one feature generation for all jars in ");
    getLog().info(this.inputSite.getAbsolutePath());

    try {
      new SecondLevelGenerator().generate(this);

      getLog().info("Calling 2nd level");
      call2ndLevel();
      getLog().info("2nd level done");

      getLog().info("Copy generated repository");
      FileUtils.copyDirectoryStructure(new File(this.currentWorkingDirectory.getAbsolutePath() + "/" + GenerateAllInOneP2Feature.SECOND_LEVEL_FOLDER_NAME + "/"
          + GenerateAllInOneP2Feature.SECOND_LEVEL_SITE_PROJECT + "/target/repository"), this.outputDirectory);
    } catch (final Exception e) {
      getLog().error(e);
      throw new MojoFailureException(e, "Could not execute second level", e.getMessage());
    }
    getLog().info("Featured repository generated.");
  }

}
