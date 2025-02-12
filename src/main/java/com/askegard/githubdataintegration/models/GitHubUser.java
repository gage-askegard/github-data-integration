package com.askegard.githubdataintegration.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents a user in GitHub
 *
 * @param login      Username of the user
 * @param name       Display name of the user
 * @param avatar_url URL for the user's avatar
 * @param location   Self-reported location of the user
 * @param email      User's email address
 * @param html_url   URL of the user's GitHub page
 * @param created_at UTC timestamp at which the user was created
 */
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
