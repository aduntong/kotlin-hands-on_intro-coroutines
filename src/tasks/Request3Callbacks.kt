package tasks

import contributors.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import java.util.concurrent.CountDownLatch

fun loadContributorsCallbacks(service: GitHubService, req: RequestData, updateResults: (List<User>) -> Unit) {
    service.getOrgReposCall(req.org).onResponse { responseRepos ->
        logRepos(req, responseRepos)
        var repos = responseRepos.bodyList()
        repos = repos.filter { it.name.contains("stack") }
//        val allUsers = mutableListOf<User>()
        val allUsers = Collections.synchronizedList(mutableListOf<User>())
        // 3rd, count down latch
        val countDownLatch = CountDownLatch(repos.size)
//        val totalRepos = repos.size
//        var finishRepos = 0
        // 2nd, use atomic
//        val numberOfProcessed = AtomicInteger()
//        for (repo in repos) {
//            service.getRepoContributorsCall(req.org, repo.name).onResponse { responseUsers ->
//                logUsers(repo, responseUsers)
//                val users = responseUsers.bodyList()
//                allUsers += users
//            }
//        }
//        // TODO: Why this code doesn't work? How to fix that?
//        updateResults(allUsers.aggregate())
        for ((index, repo) in repos.withIndex()) {   // #1
            service.getRepoContributorsCall(req.org, repo.name)
                .onResponse { responseUsers ->
                    logUsers(repo, responseUsers)
                    val users = responseUsers.bodyList()
                    allUsers += users
//                    finishRepos++
//                    if (index == repos.lastIndex) {    // #2
//                        updateResults(allUsers.aggregate())
//                    }
//                    if (numberOfProcessed.incrementAndGet() == repos.size) {
//                        updateResults(allUsers.aggregate())
//                    }
                }
        }
        // more direct than delegating the logic to the child threads
        countDownLatch.await()
        updateResults(allUsers.aggregate())
    }
}

inline fun <T> Call<T>.onResponse(crossinline callback: (Response<T>) -> Unit) {
    enqueue(object : Callback<T> {
        override fun onResponse(call: Call<T>, response: Response<T>) {
            callback(response)
        }

        override fun onFailure(call: Call<T>, t: Throwable) {
            log.error("Call failed", t)
        }
    })
}
