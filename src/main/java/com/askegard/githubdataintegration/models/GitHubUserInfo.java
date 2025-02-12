package com.askegard.githubdataintegration.models;

import java.util.List;

/**
 * Represents a user from GitHub and their repositories
 *
 * @param user_name    Username of the user
 * @param display_name Display name of the user
 * @param avatar       URL of the user's avatar
 * @param geo_location Self-reported location of the user
 * @param email        Email address of the user
 * @param url          URL of the user's GitHub page
 * @param created_at   Timestamp at which the user was created
 * @param repos        List of the user's GitHub repositories
 */
public record GitHubUserInfo(
        String user_name,
        String display_name,
        String avatar,
        String geo_location,
        String email,
        String url,
        String created_at,
        List<GitHubRepository> repos
) {
}
