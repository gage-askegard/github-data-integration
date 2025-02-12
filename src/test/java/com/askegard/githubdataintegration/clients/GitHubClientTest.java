package com.askegard.githubdataintegration.clients;

import com.askegard.githubdataintegration.exceptions.ServiceCallException;
import com.askegard.githubdataintegration.models.GitHubRepository;
import com.askegard.githubdataintegration.models.GitHubUser;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GitHubClientTest {
    private static final GitHubUser GIT_HUB_USER = new GitHubUser(
            "octocat",
            "The Octocat",
            "https://avatars.githubusercontent.com/u/583231?v=4",
            "San Fransisco",
            "octocat@gh.com",
            "https://github.com/octocat",
            "2011-01-25T18:44:36Z");

    private static final GitHubRepository REPOSITORY_1 = new GitHubRepository("boysenberry-repo-1", "https://github.com/octocat/boysenberry-repo-1");
    private static final GitHubRepository REPOSITORY_2 = new GitHubRepository("boysenberry-repo-2", "https://github.com/octocat/boysenberry-repo-2");

    private static final String USERNAME = "octocat";
    private static final String REPOSITORY_RESPONSE = """
            [{
                "name": "boysenberry-repo-1",
                "url": "https://github.com/octocat/boysenberry-repo-1"
            },
            {
                "name": "boysenberry-repo-2",
                "url": "https://github.com/octocat/boysenberry-repo-2",
                "ignoredField": "should not be deserialized"
            }]
            """;

    @InjectMocks
    private GitHubClient gitHubClient;

    @Mock
    private GitHubService gitHubService;

    @BeforeEach
    void setup() {
        gitHubClient = new GitHubClient(gitHubService);
    }

    @Test
    void testFetchUserByUsername() throws Exception {
        when(gitHubService.fetchUserByUsername(USERNAME)).thenReturn(GIT_HUB_USER);
        final GitHubUser returnedUser = gitHubClient.fetchUserByUsername(USERNAME);
        assertEquals(GIT_HUB_USER, returnedUser, "The returned user was unexpected");
    }

    @Test
    void testFetchUserByUsername_nullUsername() {
        final Exception exception = assertThrows(IllegalArgumentException.class, () -> gitHubClient.fetchUserByUsername(null));
        assertEquals("username must not be null", exception.getMessage());
        verifyNoInteractions(gitHubService);
    }

    @Test
    void testFetchUserByUsername_FeignException() {
        final var feignException = mock(FeignException.class);
        when(feignException.contentUTF8()).thenReturn("Request failed");
        when(feignException.status()).thenReturn(500);

        when(gitHubService.fetchUserByUsername(USERNAME)).thenThrow(feignException);

        final ServiceCallException thrownException = assertThrows(ServiceCallException.class, () -> gitHubClient.fetchUserByUsername(USERNAME));
        assertEquals("Request failed", thrownException.getErrorBody(), "The error body was unexpected");
        assertEquals(500, thrownException.getStatusCode(), "The status code was unexpected");
        assertEquals(feignException, thrownException.getCause(), "The exception's cause was unexpected");
    }

    @Test
    void testFetchUserRepositories() throws Exception {
        final var response = Response.builder()
                .body(REPOSITORY_RESPONSE, StandardCharsets.UTF_8)
                .headers(Map.of())
                .request(mock(Request.class))
                .build();

        when(gitHubService.fetchUserRepositories(USERNAME, 1, 100)).thenReturn(response);

        final List<GitHubRepository> returnedRepos = gitHubClient.fetchUserRepositories(USERNAME);
        assertEquals(List.of(REPOSITORY_1, REPOSITORY_2), returnedRepos, "The returned repositories were unexpected");
    }

    @Test
    void testFetchUserRepositories_paginate() throws Exception {
        final var secondResponseBody = """
                [{ "name": "boysenberry-repo-3", "url": "https://github.com/octocat/boysenberry-repo-3" }]""";
        final var response1 = Response.builder()
                .body(REPOSITORY_RESPONSE, StandardCharsets.UTF_8)
                .headers(Map.of("link", List.of("<nextPageLink/>")))
                .request(mock(Request.class))
                .build();
        final var response2 = Response.builder()
                .body(secondResponseBody, StandardCharsets.UTF_8)
                .headers(Map.of())
                .request(mock(Request.class))
                .build();

        when(gitHubService.fetchUserRepositories(USERNAME, 1, 100)).thenReturn(response1);
        when(gitHubService.fetchUserRepositories(USERNAME, 2, 100)).thenReturn(response2);

        final List<GitHubRepository> returnedRepos = gitHubClient.fetchUserRepositories(USERNAME);
        final List<GitHubRepository> expectedRepos = List.of(
                REPOSITORY_1,
                REPOSITORY_2,
                new GitHubRepository("boysenberry-repo-3", "https://github.com/octocat/boysenberry-repo-3"));
        assertEquals(expectedRepos, returnedRepos, "The returned repositories were unexpected");
    }

    @Test
    void testFetchUserRepositories_nullUsername() {
        final Exception exception = assertThrows(IllegalArgumentException.class, () -> gitHubClient.fetchUserRepositories(null));
        assertEquals("username must not be null", exception.getMessage());
        verifyNoInteractions(gitHubService);
    }

    @Test
    void testFetchUserRepositories_FeignException() {
        final var feignException = mock(FeignException.class);
        when(feignException.contentUTF8()).thenReturn("Request failed");
        when(feignException.status()).thenReturn(500);

        when(gitHubService.fetchUserRepositories(USERNAME, 1, 100)).thenThrow(feignException);

        final ServiceCallException thrownException = assertThrows(ServiceCallException.class, () -> gitHubClient.fetchUserRepositories(USERNAME));
        assertEquals("Request failed", thrownException.getErrorBody(), "The error body was unexpected");
        assertEquals(500, thrownException.getStatusCode(), "The status code was unexpected");
        assertEquals(feignException, thrownException.getCause(), "The exception's cause was unexpected");
    }

    @Test
    void testFetchUserRepositories_ioException(){
        final var response = Response.builder()
                .body("This will fail to parse", StandardCharsets.UTF_8)
                .headers(Map.of())
                .request(mock(Request.class))
                .build();

        when(gitHubService.fetchUserRepositories(USERNAME, 1, 100)).thenReturn(response);

        final ServiceCallException thrownException = assertThrows(ServiceCallException.class, () -> gitHubClient.fetchUserRepositories(USERNAME));
        assertNotNull(thrownException.getErrorBody(), "Expected error body to be set");
        assertEquals(500, thrownException.getStatusCode(), "The status code was unexpected");
    }
}