# github-data-integration
A project to retrieve GitHub user information by username and is a coding exercise for Branch.

Project source: https://github.com/gage-askegard/github-data-integration
## Architecture
- Language: Java 21
- Framework: Spring Boot

The application runs on Java 21 and Spring Boot, as instructed by the requirements, and uses a standard `src/[main|test]/[java|resources]` structure.
I used Spring Initialzr to create the base project structure with spring boot starter dependencies and the maven wrapper. 
Maven was used for dependency management as I am more familiar with it than Gradle at the moment. Unit testing uses the Mockito
library as it is a standard and easy to use mock testing library. I did not add a full integration test for time constraint purposes, but one could be
added to `GitHubDataIntegrationApplicationTests` .

The package structure is as follows:
- clients - contains clients for communicating with external services, like GitHub
- exceptions - contains custom exceptions used by the project
- models - contains the data models used by the project
- rest - Contains the REST controller
- The main application is located in the base package to allow component scanning to catch all sub packages without extra configuration
## Decisions
- The main decisions I had to make when creating the project were Maven vs. Gradle for dependency management, and how to communicate
with GitHub's API. As stated above, maven was chosen as it was quicker for me to get that set up than Gradle would be, due to a higher familiarity with
Maven. Both would work fine for this project though. I decided to go with Feign for interacting with GitHub's API, with
`GitHubService` being the service interface and `GitHubClient` as my client class. This was also a decision based partially in familiarity,
but I also find Feign very straightforward to work with, and it abstracts away some of the tedious details of working with Http clients.
Being able to just call a Java service method is great for getting an application spun up quickly.
- Other smaller decisions I made were to create a custom exception class for wrapping API failures in and using records for the models. The custom
exception is generally good practice as it gives the developers more control over the errors that their clients see. I also went 
with records as the objects I needed to model were fairly small and simple. If they needed more custom logic or I needed to
model the full objects returned by the GitHub API, I would choose a standard class. The `@JsonIgnoreProperties(ignoreUnknown = true)`
annotation also made using records more feasible as that annotation allows me to ignore the other fields coming from requests that 
the application doesn't need to care about. Similarly the `@JsonProperty` annotation was useful for allowing me to reuse the
`GitHubRepository` model in `GitHubUserInfo` since the only change was the name of the `url` field. 
- I opted to have `GitHubUserControllerTest` use `mockMvc` for its unit testing so that the endpoint mapping and status codes could be tested.
I felt this was a good choice since I did not add a full integration test.

## Application Usage
### Requirements
- Java 21
- Maven 3.X.X 
  - The exact version doesn't matter much because this project has a maven wrapper that will download the appropriate version to use. However,
you need to use a maven command to install the wrapper to add or update the necessary wrapper files. To do this run `mvn wrapper:wrapper`
### Run the Application
- From the command line
  - `./mvnw spring-boot:run` (Unix-based systems) `mvnw.cmd spring-boot:run` (Windows systems) or `mvn spring-boot:run` (any system if not using the wrapper)
- From the IDE
  - Navigate to `GitHubDataIntegrationApplication` and select the 'run' icon next to the class name.
- The application is now running and the dispatcher servlet is listening on `localhost:8080/`
### API Usage
- The only endpoint is at `localhost:8080/gitHubUserInfo/{username}`
  - `{username}` is a GitHub user's username
  - This will return a summary of the user's info along with summaries of their repositories.
- Example Response:
```
{
  "user_name": "gage-askegard",
  "display_name": null,
  "avatar": "https://avatars.githubusercontent.com/u/11622955?v=4",
  "geo_location": null,
  "email": null,
  "url": "https://github.com/gage-askegard",
  "created_at": "2015-03-24 01:48:04",
  "repos": [
  {
    "name": "curbee-code-challenge",
    "url": "https://api.github.com/repos/gage-askegard/curbee-code-challenge"
  },
  {
    "name": "deck-of-cards",
    "url": "https://api.github.com/repos/gage-askegard/deck-of-cards"
  },
  {
    "name": "github-data-integration",
    "url": "https://api.github.com/repos/gage-askegard/github-data-integration"
  },
  {
    "name": "mindex-code-challenge",
    "url": "https://api.github.com/repos/gage-askegard/mindex-code-challenge"
  }]
}
```
### Running Tests
- All tests can be run from the command line via `./mvnw verify` `mvnw.cmd verify` or `mvn verify`
- Individual tests can be run from their respective classes within the IDE or filters can be used on command line to filter to specific classes

## Future Improvements
There are a handful of things that I would add to this project to improve the quality and make it more production ready:
- Cache invalidation/timeout
  - The cache in the controller does not have a TTL, so entries will stay in it until the application is restarted. This could be accomplished with
a local cache library addition, or an extra `ConcurrentHashMap` to keep track of when keys were last updated so that old entries can be expired.
- Error page
  - This application currently uses the stock error page, which is not very helpful for debugging issues. `ResponseStatusException` was used in
the controller to propagate the proper status codes on failures, but ideally a failure would at least show a helpful message.
- Logging
  - Production applications should have logging in place to keep track of errors that could be bugs, or indicate Git Hub's API is having issues.
- Properties for GitHub client
  - The `GitHubClient` uses static variables to set properties on the Feign client and configuration. Keeping these in a central
properties file would allow them to be changed a bit easier, especially if we ever had more clients that would share the same properties.
    - I attempted to put the properties in `application.properties` but was having issues getting that to work, so I opted for static variables 
  for time purposes
- Integration test
  - I would add a simple integration test to ensure the main endpoint works end-to-end, which could then be leveraged in a CI/CD pipeline.
