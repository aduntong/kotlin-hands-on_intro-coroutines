package tasks

import contributors.*
import kotlinx.coroutines.*

suspend fun loadContributorsConcurrent(service: GitHubService, req: RequestData): List<User> = coroutineScope {
    var repos = service
        .getOrgRepos(req.org)
//        .execute() // Executes request and blocks the current thread
        .also { logRepos(req, it) }
        .body() ?: emptyList<Repo>()
        // why this filter does not work???
        .filter { it.name.contains("stack") }
    repos = repos.filter { it.name.contains("stack") }
    val deferreds: List<Deferred<List<User>>> = repos.map { repo ->
        // first, create different new coroutine to send requests, on the same thread
        // then add Dispatchers.Default to run coroutines on different threads
        /**
         * 11812 [DefaultDispatcher-worker-2 @coroutine#3] INFO  Contributors - starting loading for kotlin-fullstack-sample
         # 11812 [DefaultDispatcher-worker-1 @coroutine#4] INFO  Contributors - starting loading for full-stack-web-jetbrains-night-sample
         # 11814 [DefaultDispatcher-worker-3 @coroutine#5] INFO  Contributors - starting loading for full-stack-spring-collaborative-todo-list-sample
         # 12345 [DefaultDispatcher-worker-2 @coroutine#4] INFO  Contributors - full-stack-web-jetbrains-night-sample: loaded 9 contributors
         # 12943 [DefaultDispatcher-worker-2 @coroutine#3] INFO  Contributors - kotlin-fullstack-sample: loaded 10 contributors
         # 13113 [DefaultDispatcher-worker-2 @coroutine#5] INFO  Contributors - full-stack-spring-collaborative-todo-list-sample: loaded 3 contributors
         */
        async(Dispatchers.Default) {
            log("starting loading for ${repo.name}")
            // load contributors for each repo
            service.getRepoContributors(req.org, repo.name)
                .also { logUsers(repo, it) }
                .bodyList()
        }
    }
    // List<List<User>>
//    deferreds.awaitAll()
    deferreds.awaitAll().flatten().aggregate()
//    ArrayList()
//    return repos.flatMap { repo ->
//        service
//            .getRepoContributors(req.org, repo.name)
////            .execute() // Executes request and blocks the current thread
//            .also { logUsers(repo, it) }
//            .bodyList()
//    }.aggregate()
}
