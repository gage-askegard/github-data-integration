package com.askegard.githubdataintegration.rest;

import com.askegard.githubdataintegration.clients.GitHubClient;
import com.askegard.githubdataintegration.exceptions.ServiceCallException;
import com.askegard.githubdataintegration.models.GitHubRepository;
import com.askegard.githubdataintegration.models.GitHubUser;
import com.askegard.githubdataintegration.models.GitHubUserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class GitHubUserControllerTest {
    @InjectMocks
    private GitHubUserController gitHubUserController;

    @Mock
    private GitHubClient gitHubClient;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(gitHubUserController)
                .build();
    }

    @Test
    void testFetchGitHubUserInfo() throws Exception {
        final var username = "octocat";
        final var gitHubUser = new GitHubUser(
                username,
                "The Octocat",
                "https://avatars.githubusercontent.com/u/583231?v=4",
                "San Fransisco",
                "octocat@gh.com",
                "https://github.com/octocat",
                "2011-01-25T18:44:36Z");
        final var foundRepositories = List.of(
                new GitHubRepository("boysenberry-repo-1", "https://github.com/octocat/boysenberry-repo-1"),
                new GitHubRepository("git-consortium", "https://github.com/octocat/git-consortium"));

        when(gitHubClient.fetchUserByUsername(username)).thenReturn(gitHubUser);
        when(gitHubClient.fetchUserRepositories(username)).thenReturn(foundRepositories);

        MvcResult result = mockMvc.perform(get("/gitHubUserInfo/" + username))
                .andExpect(status().isOk())
                .andReturn();

        final GitHubUserInfo userInfo = objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<>() {});
        final var expectedUserInfo = new GitHubUserInfo(
                username,
                gitHubUser.name(),
                gitHubUser.avatar_url(),
                gitHubUser.location(),
                gitHubUser.email(),
                gitHubUser.html_url(),
                "2011-01-25 18:44:36",
                foundRepositories);

        assertEquals(expectedUserInfo, userInfo, "The returned user info was unexpected");
    }

    @Test
    void testFetchGitHubUserInfo_cached() throws Exception {
        final var username = "octocat2";
        final var gitHubUser = new GitHubUser(
                username,
                "The Octocat",
                "https://avatars.githubusercontent.com/u/583231?v=4",
                "San Fransisco",
                "octocat@gh.com",
                "https://github.com/octocat",
                "2011-01-25T18:44:36Z");
        final var foundRepositories = List.of(
                new GitHubRepository("boysenberry-repo-1", "https://github.com/octocat/boysenberry-repo-1"),
                new GitHubRepository("git-consortium", "https://github.com/octocat/git-consortium"));

        when(gitHubClient.fetchUserByUsername(username)).thenReturn(gitHubUser);
        when(gitHubClient.fetchUserRepositories(username)).thenReturn(foundRepositories);

        // Fetch the user once
        MvcResult fetchedResult = mockMvc.perform(get("/gitHubUserInfo/" + username))
                .andExpect(status().isOk())
                .andReturn();
        final GitHubUserInfo fetchedUserInfo = objectMapper.readValue(fetchedResult.getResponse().getContentAsString(),
                new TypeReference<>() {});
        final var expectedUserInfo = new GitHubUserInfo(
                username,
                gitHubUser.name(),
                gitHubUser.avatar_url(),
                gitHubUser.location(),
                gitHubUser.email(),
                gitHubUser.html_url(),
                "2011-01-25 18:44:36",
                foundRepositories);
        assertEquals(expectedUserInfo, fetchedUserInfo, "The returned user info was unexpected");

        // Fetch the user again
        MvcResult cachedResult = mockMvc.perform(get("/gitHubUserInfo/" + username))
                .andExpect(status().isOk())
                .andReturn();
        final GitHubUserInfo cachedUserInfo = objectMapper.readValue(cachedResult.getResponse().getContentAsString(),
                new TypeReference<>() {});
        assertEquals(expectedUserInfo, cachedUserInfo, "The returned user info was unexpected");

        // Ensure the client was only called once
        verify(gitHubClient).fetchUserByUsername(username);
        verify(gitHubClient).fetchUserRepositories(username);
    }

    @Test
    void testFetchGitHubUserInfo_serviceCallException() throws Exception {
        final var username = "octocat3";

        when(gitHubClient.fetchUserByUsername(username))
                .thenThrow(new ServiceCallException("User not found", "failed", 404));

        mockMvc.perform(get("/gitHubUserInfo/" + username))
                .andExpect(status().isNotFound())
                .andReturn();

        verifyNoMoreInteractions(gitHubClient);
    }

    @Test
    void testFetchGitHubUserInfo_otherException() throws Exception {
        final var username = "octocat4";

        when(gitHubClient.fetchUserByUsername(username)).thenThrow(new IllegalArgumentException("Unexpected error"));

        mockMvc.perform(get("/gitHubUserInfo/" + username))
                .andExpect(status().isInternalServerError())
                .andReturn();

        verifyNoMoreInteractions(gitHubClient);
    }
}