package com.askegard.githubdataintegration.clients;

import com.askegard.githubdataintegration.models.GitHubRepository;
import com.askegard.githubdataintegration.models.GitHubUser;
import feign.Client;
import feign.Feign;
import feign.FeignException;
import feign.httpclient.ApacheHttpClient;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class GitHubClient {

    private static final String URL_BASE = "https://api.github.com";
    private static final int CONNECT_TIMEOUT = 15000;
    private static final int CONNECTION_REQUEST_TIMEOUT = 15000;
    private static final int SOCKET_TIMEOUT = 120000;
    private static final String USER_AGENT = "GitHubDataIntegration";

    private GitHubService gitHubService;

    public GitHubClient()
    {
        final var requestConfig = RequestConfig.custom()
                .setSocketTimeout(SOCKET_TIMEOUT)
                .setConnectTimeout(CONNECT_TIMEOUT)
                .setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT)
                .build();

        final Client client = new ApacheHttpClient(HttpClientBuilder.create()
                .setMaxConnPerRoute(Integer.MAX_VALUE)
                .setMaxConnTotal(Integer.MAX_VALUE)
                .setDefaultRequestConfig(requestConfig)
                .setUserAgent(USER_AGENT)
                .build());

        gitHubService = Feign.builder()
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .client(client)
                .target(GitHubService.class, URL_BASE);
    }

    public GitHubUser fetchUserByUsername(final String username)
    {
        Objects.requireNonNull(username);

        try
        {
            return gitHubService.fetchUserByUsername(username);
        }
        catch (FeignException e)
        {
            // wrap in new type
            return null;
        }
    }

    public List<GitHubRepository> fetchUserRepositories(final String username)
    {
        Objects.requireNonNull(username);

        try
        {
            return gitHubService.fetchUserRepositories(username);
        }
        catch (FeignException e)
        {
            return null;
        }
    }
}
