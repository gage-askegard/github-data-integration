package com.askegard.githubdataintegration.rest;

import com.askegard.githubdataintegration.clients.GitHubClient;
import com.askegard.githubdataintegration.exceptions.ServiceCallException;
import com.askegard.githubdataintegration.models.GitHubRepository;
import com.askegard.githubdataintegration.models.GitHubUser;
import com.askegard.githubdataintegration.models.GitHubUserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class GitHubUserController {
    /**
     * Date format of the timestamps on users fetched from GitHub
     */
    private static final DateTimeFormatter GIT_HUB_DATE_FORMAT = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd'T'HH:mm:ssX")
            .toFormatter();

    /**
     * Date format desired on the {@link GitHubUserInfo} returned by this controller
     */
    private static final DateTimeFormatter OUTPUT_DATE_FORMAT = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm:ss")
            .toFormatter();

    @Autowired
    private GitHubClient gitHubClient;

    private final Map<String, GitHubUserInfo> userInfoCache = new ConcurrentHashMap<>();

    /**
     * Fetches the information about a GitHub user with the given username. The returned data will include general user
     * information as well as a summary of their repositories. Note: only public information is returned.
     *
     * @param username GitHub username of the user to find
     * @return Information about the GitHub user with the username
     * @throws ResponseStatusException if an error occurs while fetching user information
     */
    @GetMapping("/gitHubUserInfo/{username}")
    public GitHubUserInfo fetchGitHubUserInfo(@PathVariable(value = "username") final String username)
            throws ResponseStatusException {
        try {
            if (userInfoCache.containsKey(username)) {
                return userInfoCache.get(username);
            }

            final GitHubUser gitHubUser = gitHubClient.fetchUserByUsername(username);
            final List<GitHubRepository> gitHubRepos = gitHubClient.fetchUserRepositories(username);
            final GitHubUserInfo userInfo = mergeUserInfo(gitHubUser, gitHubRepos);
            userInfoCache.put(username, userInfo);
            return userInfo;
        } catch (ServiceCallException e) {
            throw new ResponseStatusException(HttpStatus.valueOf(e.getStatusCode()), e.getErrorBody(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", e);
        }
    }

    /**
     * Merges the given GitHub user and repositories into a single {@link GitHubUserInfo}
     *
     * @param gitHubUser  GitHub user to merge
     * @param gitHubRepos GitHub repos to merge
     * @return New GitHub user info constructed from the provided user and repositories
     */
    private static GitHubUserInfo mergeUserInfo(final GitHubUser gitHubUser, final List<GitHubRepository> gitHubRepos) {
        return new GitHubUserInfo(
                gitHubUser.login(),
                gitHubUser.name(),
                gitHubUser.avatar_url(),
                gitHubUser.location(),
                gitHubUser.email(),
                gitHubUser.html_url(),
                convertDateFormat(gitHubUser.created_at()),
                gitHubRepos);
    }

    /**
     * Converts the given timestamp string from the {@link #GIT_HUB_DATE_FORMAT} to {@link #OUTPUT_DATE_FORMAT}. If the
     * timestamp cannot be parsed into the GitHub format, the original timestamp will be returned
     *
     * @param timestampString Timestamp string to convert. Should match {@link #GIT_HUB_DATE_FORMAT}
     * @return The timestamp string converted to {@link #OUTPUT_DATE_FORMAT}
     */
    private static String convertDateFormat(final String timestampString) {
        final var date = LocalDateTime.parse(timestampString, GIT_HUB_DATE_FORMAT);
        return date.format(OUTPUT_DATE_FORMAT);
    }
}
