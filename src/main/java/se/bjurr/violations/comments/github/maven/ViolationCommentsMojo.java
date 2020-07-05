package se.bjurr.violations.comments.github.maven;

import static org.apache.maven.plugins.annotations.LifecyclePhase.NONE;
import static se.bjurr.violations.comments.github.lib.ViolationCommentsToGitHubApi.violationCommentsToGitHubApi;
import static se.bjurr.violations.lib.ViolationsApi.violationsApi;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import se.bjurr.violations.lib.ViolationsLogger;
import se.bjurr.violations.lib.model.SEVERITY;
import se.bjurr.violations.lib.model.Violation;
import se.bjurr.violations.lib.reports.Parser;
import se.bjurr.violations.lib.util.Filtering;

@Mojo(name = "violation-comments", defaultPhase = NONE)
public class ViolationCommentsMojo extends AbstractMojo {
  @Parameter(property = "repositoryOwner", required = false)
  private String repositoryOwner;

  @Parameter(property = "repositoryName", required = false)
  private String repositoryName;
  /**
   * Travis will define TRAVIS_PULL_REQUEST as "false" if not a PR, and an integer if a PR. Having
   * this as String makes life easier =)
   */
  @Parameter(property = "pullRequestId", required = false)
  private String pullRequestId;

  @Parameter(property = "gitHubUrl", required = false)
  private String gitHubUrl;

  @Parameter(property = "oAuth2Token", required = false)
  private String oAuth2Token;

  @Parameter(property = "username", required = false)
  private String username;

  @Parameter(property = "password", required = false)
  private String password;

  @Parameter(property = "createCommentWithAllSingleFileComments", required = false)
  private boolean createCommentWithAllSingleFileComments;

  @Parameter(property = "createSingleFileComments", required = false, defaultValue = "true")
  private boolean createSingleFileComments;

  @Parameter(property = "violations", required = false)
  private List<ViolationConfig> violations;

  @Parameter(property = "commentOnlyChangedContent", required = false, defaultValue = "true")
  private boolean commentOnlyChangedContent;

  @Parameter(property = "commentOnlyChangedFiles", required = false, defaultValue = "true")
  private boolean commentOnlyChangedFiles;

  @Parameter(property = "minSeverity", required = false, defaultValue = "INFO")
  private SEVERITY minSeverity;

  @Parameter(property = "keepOldComments", required = false)
  private boolean keepOldComments;

  @Parameter(property = "commentTemplate", required = false)
  private String commentTemplate;

  @Parameter(property = "maxNumberOfViolations", required = false)
  private Integer maxNumberOfViolations;

  @Override
  public void execute() throws MojoExecutionException {
    if (this.pullRequestId == null || this.pullRequestId.equalsIgnoreCase("false")) {
      this.getLog().info("No pull request id defined, will not send violation comments to GitHub.");
      return;
    }
    if (this.violations == null || this.violations.isEmpty()) {
      this.getLog().info("No violations configured.");
      return;
    }
    final Integer pullRequestIdInt = Integer.valueOf(this.pullRequestId);
    if (this.oAuth2Token != null) {
      this.getLog().info("Using OAuth2Token");
    } else if (this.username != null && this.password != null) {
      this.getLog()
          .info("Using username/password: " + this.username.substring(0, 1) + ".../*********");
    } else {
      this.getLog()
          .error(
              "No OAuth2 token and no username/email specified. Will not comment any pull request.");
      return;
    }

    this.getLog()
        .info(
            "Will comment PR "
                + this.repositoryOwner
                + "/"
                + this.repositoryName
                + "/"
                + this.pullRequestId
                + " on "
                + this.gitHubUrl);

    final ViolationsLogger violationsLogger =
        new ViolationsLogger() {

          @Override
          public void log(final Level level, final String string) {
            if (level == Level.FINE) {
              ViolationCommentsMojo.this.getLog().debug(string);
            } else if (level == Level.SEVERE) {
              ViolationCommentsMojo.this.getLog().error(string);
            } else if (level == Level.WARNING) {
              ViolationCommentsMojo.this.getLog().warn(string);
            } else {
              ViolationCommentsMojo.this.getLog().info(string);
            }
          }

          @Override
          public void log(final Level level, final String string, final Throwable t) {
            if (level == Level.FINE) {
              ViolationCommentsMojo.this.getLog().debug(string, t);
            } else if (level == Level.SEVERE) {
              ViolationCommentsMojo.this.getLog().error(string, t);
            } else if (level == Level.WARNING) {
              ViolationCommentsMojo.this.getLog().warn(string, t);
            } else {
              ViolationCommentsMojo.this.getLog().info(string);
            }
          }
        };

    Set<Violation> allParsedViolations = new TreeSet<>();
    for (final ViolationConfig configuredViolation : this.violations) {
      final Set<Violation> parsedViolations =
          violationsApi()
              .withViolationsLogger(violationsLogger)
              .findAll(Parser.valueOf(configuredViolation.getParser())) //
              .inFolder(configuredViolation.getFolder()) //
              .withPattern(configuredViolation.getPattern()) //
              .withReporter(configuredViolation.getReporter()) //
              .violations();
      allParsedViolations = Filtering.withAtLEastSeverity(allParsedViolations, this.minSeverity);
      allParsedViolations.addAll(parsedViolations);
    }

    try {
      violationCommentsToGitHubApi()
          .withViolationsLogger(violationsLogger)
          .withoAuth2Token(this.oAuth2Token)
          .withUsername(this.username)
          .withPassword(this.password)
          .withGitHubUrl(this.gitHubUrl)
          .withPullRequestId(pullRequestIdInt)
          .withRepositoryName(this.repositoryName)
          .withRepositoryOwner(this.repositoryOwner)
          .withViolations(allParsedViolations)
          .withCreateCommentWithAllSingleFileComments(
              this.createCommentWithAllSingleFileComments) //
          .withCreateSingleFileComments(this.createSingleFileComments) //
          .withCommentOnlyChangedContent(this.commentOnlyChangedContent) //
          .withCommentOnlyChangedFiles(this.commentOnlyChangedFiles) //
          .withKeepOldComments(this.keepOldComments) //
          .withCommentTemplate(this.commentTemplate) //
          .withMaxNumberOfViolations(this.maxNumberOfViolations) //
          .toPullRequest();
    } catch (final Exception e) {
      this.getLog().error("", e);
    }
  }
}
