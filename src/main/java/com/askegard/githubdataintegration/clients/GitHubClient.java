package com.askegard.githubdataintegration.clients;

import com.askegard.githubdataintegration.exceptions.ServiceCallException;
import com.askegard.githubdataintegration.models.GitHubRepository;
import com.askegard.githubdataintegration.models.GitHubUser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Client;
import feign.Feign;
import feign.FeignException;
import feign.Response;
import feign.httpclient.ApacheHttpClient;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.*;

/**
 * Client for performing web requests defined by {@link GitHubService}
 */
@Component
public class GitHubClient {

    private static final String URL_BASE = "https://api.github.com";
    private static final int CONNECT_TIMEOUT = 15000;
    private static final int CONNECTION_REQUEST_TIMEOUT = 15000;
    private static final int SOCKET_TIMEOUT = 120000;
    private static final String USER_AGENT = "GitHubDataIntegration";
    private static final int REPOSITORY_PER_PAGE = 100;

    private final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final GitHubService gitHubService;

    /**
     * Constructs a new instance
     */
    public GitHubClient() {
        final var requestConfig = RequestConfig.custom()
                .setSocketTimeout(SOCKET_TIMEOUT)
                .setConnectTimeout(CONNECT_TIMEOUT)
                .setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT)
                .build();

        final Client client = new ApacheHttpClient(HttpClientBuilder.create()
                .setMaxConnPerRoute(Integer.MAX_VALUE)
                .setMaxConnTotal(Integer.MAX_VALUE)
                .setDefaultRequestConfig(requestConfig)
                .setUserAgent(USER_AGENT)
                .build());

        gitHubService = Feign.builder()
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .client(client)
                .target(GitHubService.class, URL_BASE);
    }

    /**
     * Constructs an instance for mock unit tests
     *
     * @param mockGitHubService Mocked GitHub service to stub calls for
     */
    GitHubClient(GitHubService mockGitHubService) {
        this.gitHubService = mockGitHubService;
    }

    /**
     * Fetches the GitHub user with the given username
     *
     * @param username Username of the user to find
     * @return The GitHub user with the given username
     * @throws ServiceCallException If an error occurs while fetching the user, including if the user is not found
     */
    public GitHubUser fetchUserByUsername(final String username) throws ServiceCallException {
        Assert.notNull(username, "username must not be null");

        try {
            return gitHubService.fetchUserByUsername(username);
        } catch (FeignException e) {
            throw new ServiceCallException(e);
        }
    }

    /**
     * Fetches the GitHub repositories of the user with the given username. This will paginate through all repositories
     * the user has if needed.
     *
     * @param username Username of the user to find repositories for
     * @return List of GitHub repositories belonging to the user with the username
     * @throws ServiceCallException If an error occurs while fetching the user, including if the user does not exist
     */
    public List<GitHubRepository> fetchUserRepositories(final String username) throws ServiceCallException {
        Assert.notNull(username, "username must not be null");

        try {
            int pageNumber = 1;
            final List<GitHubRepository> repos = new ArrayList<>();
            boolean pagesRemaining = true;
            while (pagesRemaining) {
                Response response = gitHubService.fetchUserRepositories(username, pageNumber, REPOSITORY_PER_PAGE);
                repos.addAll(objectMapper.readValue(response.body().asInputStream(), new TypeReference<>() {
                }));
                final Collection<String> linkHeader = response.headers().get("link");
                if (linkHeader == null || linkHeader.isEmpty()) {
                    pagesRemaining = false;
                }
                pageNumber++;
            }
            return repos;
        } catch (FeignException e) {
            throw new ServiceCallException(e);
        } catch (IOException e) {
            throw new ServiceCallException("Failed to parse repository response", e.getMessage(), 500);
        }
    }
}
