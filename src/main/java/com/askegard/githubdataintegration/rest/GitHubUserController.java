package com.askegard.githubdataintegration.rest;

import com.askegard.githubdataintegration.clients.GitHubClient;
import com.askegard.githubdataintegration.models.GitHubRepository;
import com.askegard.githubdataintegration.models.GitHubUser;
import com.askegard.githubdataintegration.models.GitHubUserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class GitHubUserController {

    @Autowired
    private GitHubClient gitHubClient;

    private Map<String, GitHubUserInfo> userInfoCache = new ConcurrentHashMap<>();

    @GetMapping("/gitHubUserInfo/{username}")
    public @ResponseBody GitHubUserInfo fetchGitHubUserInfo(
          @PathVariable(value = "username") final String username)
    {
        if (userInfoCache.containsKey(username))
        {
            return userInfoCache.get(username);
        }

        final GitHubUser gitHubUser = gitHubClient.fetchUserByUsername(username);
        final List<GitHubRepository> gitHubRepos = gitHubClient.fetchUserRepositories(username);

        final GitHubUserInfo userInfo = mergeUserInfo(gitHubUser, gitHubRepos);
        userInfoCache.put(username, userInfo);
        return userInfo;
    }

    private static GitHubUserInfo mergeUserInfo(final GitHubUser gitHubUser, final List<GitHubRepository> gitHubRepos)
    {
        return new GitHubUserInfo(
                gitHubUser.login(),
                gitHubUser.name(),
                gitHubUser.avatar_url(),
                gitHubUser.location(),
                gitHubUser.email(),
                gitHubUser.html_url(),
                gitHubUser.created_at(),
                gitHubRepos);
    }
}
