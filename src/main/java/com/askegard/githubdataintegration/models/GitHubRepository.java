package com.askegard.githubdataintegration.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GitHubRepository(
        String name,
        @JsonProperty("url")
        String html_url
) {
}
