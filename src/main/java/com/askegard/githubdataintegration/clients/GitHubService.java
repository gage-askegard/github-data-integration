package com.askegard.githubdataintegration.clients;

import com.askegard.githubdataintegration.models.GitHubRepository;
import com.askegard.githubdataintegration.models.GitHubUser;
import feign.Param;
import feign.RequestLine;

import java.util.List;

public interface GitHubService {
    @RequestLine("GET /users/{username}")
    GitHubUser fetchUserByUsername(@Param("username") String username);

    @RequestLine("GET /users/{username}/repos")
    List<GitHubRepository> fetchUserRepositories(@Param("username") String username);
}
