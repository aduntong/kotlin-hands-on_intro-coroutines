package tasks

import contributors.*
import retrofit2.Response

fun loadContributorsBlocking(service: GitHubService, req: RequestData) : List<User> {
    var repos = service
        .getOrgReposCall(req.org)
        .execute() // Executes request and blocks the current thread
        .also { logRepos(req, it) }
        .body() ?: emptyList<Repo>()
        // why this filter does not work???
        .filter { it.name.contains("stack") }
    repos = repos.filter {it.name.contains("stack")}
    return repos.flatMap { repo ->
        service
            .getRepoContributorsCall(req.org, repo.name)
            .execute() // Executes request and blocks the current thread
            .also { logUsers(repo, it) }
            .bodyList()
    }.aggregate()
}

fun <T> Response<List<T>>.bodyList(): List<T> {
    return body() ?: emptyList()
}