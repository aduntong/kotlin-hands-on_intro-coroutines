package tasks

import contributors.*

suspend fun loadContributorsSuspend(service: GitHubService, req: RequestData): List<User> {
    var repos = service
        .getOrgRepos(req.org)
//        .execute() // Executes request and blocks the current thread
        .also { logRepos(req, it) }
        .body() ?: emptyList<Repo>()
        // why this filter does not work???
        .filter { it.name.contains("stack") }
    repos = repos.filter { it.name.contains("stack") }
    return repos.flatMap { repo ->
        service
            .getRepoContributors(req.org, repo.name)
//            .execute() // Executes request and blocks the current thread
            .also { logUsers(repo, it) }
            .bodyList()
    }.aggregate()
}