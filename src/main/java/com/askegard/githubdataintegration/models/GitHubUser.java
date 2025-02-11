package com.askegard.githubdataintegration.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GitHubUser(
        String login,
        String name,
        String avatar_url,
        String location,
        String email,
        String html_url,
        String created_at
) {
}
