package com.askegard.githubdataintegration.models;

import java.util.List;

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
