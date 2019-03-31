package se.bjurr.violations.comments.github.maven;

import static org.apache.maven.plugins.annotations.LifecyclePhase.NONE;
import static se.bjurr.violations.comments.github.lib.ViolationCommentsToGitHubApi.violationCommentsToGitHubApi;
import static se.bjurr.violations.lib.ViolationsApi.violationsApi;

import java.util.ArrayList;
import java.util.List;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
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
    if (pullRequestId == null || pullRequestId.equalsIgnoreCase("false")) {
      getLog().info("No pull request id defined, will not send violation comments to GitHub.");
      return;
    }
    if (violations == null || violations.isEmpty()) {
      getLog().info("No violations configured.");
      return;
    }
    final Integer pullRequestIdInt = Integer.valueOf(pullRequestId);
    if (oAuth2Token != null) {
      getLog().info("Using OAuth2Token");
    } else if (username != null && password != null) {
      getLog().info("Using username/password: " + username.substring(0, 1) + ".../*********");
    } else {
      getLog()
          .error(
              "No OAuth2 token and no username/email specified. Will not comment any pull request.");
      return;
    }

    getLog()
        .info(
            "Will comment PR "
                + repositoryOwner
                + "/"
                + repositoryName
                + "/"
                + pullRequestId
                + " on "
                + gitHubUrl);

    List<Violation> allParsedViolations = new ArrayList<>();
    for (final ViolationConfig configuredViolation : violations) {
      final List<Violation> parsedViolations =
          violationsApi() //
              .findAll(Parser.valueOf(configuredViolation.getParser())) //
              .inFolder(configuredViolation.getFolder()) //
              .withPattern(configuredViolation.getPattern()) //
              .withReporter(configuredViolation.getReporter()) //
              .violations();
      allParsedViolations = Filtering.withAtLEastSeverity(allParsedViolations, minSeverity);
      allParsedViolations.addAll(parsedViolations);
    }

    try {
      violationCommentsToGitHubApi() //
          .withoAuth2Token(oAuth2Token) //
          .withUsername(username) //
          .withPassword(password) //
          .withGitHubUrl(gitHubUrl) //
          .withPullRequestId(pullRequestIdInt) //
          .withRepositoryName(repositoryName) //
          .withRepositoryOwner(repositoryOwner) //
          .withViolations(allParsedViolations) //
          .withCreateCommentWithAllSingleFileComments(createCommentWithAllSingleFileComments) //
          .withCreateSingleFileComments(createSingleFileComments) //
          .withCommentOnlyChangedContent(commentOnlyChangedContent) //
          .withKeepOldComments(keepOldComments) //
          .withCommentTemplate(commentTemplate) //
          .withMaxNumberOfViolations(maxNumberOfViolations) //
          .toPullRequest();
    } catch (final Exception e) {
      getLog().error("", e);
    }
  }
}
