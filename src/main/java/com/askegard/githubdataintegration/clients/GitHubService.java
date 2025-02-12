package com.askegard.githubdataintegration.clients;

import com.askegard.githubdataintegration.models.GitHubUser;
import feign.Param;
import feign.RequestLine;
import feign.Response;

/**
 * Service for performing requests against GitHub's API
 */
public interface GitHubService {
    /**
     * Fetches the GitHub user with the given username
     *
     * @param username Username of the user to find
     * @return The GitHub user with the given username
     */
    @RequestLine("GET /users/{username}")
    GitHubUser fetchUserByUsername(@Param("username") String username);

    /**
     * Fetches the GitHub repositories of the user with the given username
     *
     * @param username Username of the user to find repositories for
     * @param page     Page number to fetch
     * @param perPage  Number of items to fetch per page
     * @return List of GitHub repositories belonging to the user with the username
     */
    @RequestLine("GET /users/{username}/repos?page={page}&per_page={perPage}")
    Response fetchUserRepositories(
            @Param("username") String username,
            @Param("page") int page,
            @Param("per_page") int perPage);
}
