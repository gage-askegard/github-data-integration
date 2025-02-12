package com.askegard.githubdataintegration.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a GitHub repository
 *
 * @param name Name of the repository
 * @param html_url URL of the repository
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GitHubRepository(
        String name,
        @JsonProperty("url")
        String html_url
) {
}
