package app.colorrr.colorrr.api.feed

import app.colorrr.colorrr.api.ApiParser
import app.colorrr.colorrr.entity.Comment
import app.colorrr.colorrr.entity.api.*
import com.google.gson.internal.LinkedTreeMap
import io.reactivex.Observable

class WorkerFeed(private val apiFeed: ApiFeed) {
    fun loadFeed(start: Int, limit: Int, sessionID: String): Observable<List<HashMap<String, Any>>> {
        return this.apiFeed.getFeedRx(FeedQuery(start, limit, null, sessionID)).flatMap {
            if (it.error_code == null && it.result is ArrayList<*>) {
                val result = ArrayList<HashMap<String, Any>>()
                for (item: Any in it.result)
                    result.add(ApiParser.parseFeed(item as LinkedTreeMap<*, *>))

                Observable.fromArray(result)
            } else {
                throw Throwable(it.error_code.toString())
            }
        }
    }

    fun loadFeedByFollows(start: Int, limit: Int, sessionID: String): Observable<List<HashMap<String, Any>>> {
        return this.apiFeed.getFeedByFollowsRx(FeedQuery(start, limit, null, sessionID)).flatMap {
            if (it.error_code == null && it.result is ArrayList<*>) {
                val result = ArrayList<HashMap<String, Any>>()
                for (item: Any in it.result)
                    result.add(ApiParser.parseFeed(item as LinkedTreeMap<*, *>))

                Observable.fromArray(result)
            } else {
                throw Throwable(it.error_code.toString())
            }
        }
    }

    fun loadFeedPopular(start: Int, limit: Int, sessionID: String): Observable<List<HashMap<String, Any>>> {
        return this.apiFeed.getFeedByLikesRx(FeedQuery(start, limit, "likedesc", sessionID)).flatMap {
            if (it.error_code == null && it.result is ArrayList<*>) {
                val result = ArrayList<HashMap<String, Any>>()
                for (item: Any in it.result)
                    result.add(ApiParser.parseFeed(item as LinkedTreeMap<*, *>))

                Observable.fromArray(result)
            } else {
                throw Throwable(it.error_code.toString())
            }
        }
    }

    fun loadComments(start: Int, limit: Int, postID: Int): Observable<List<Comment>> {
        return this.apiFeed.getFeedCommentsRx(FeedCommentsQuery(start, limit, postID)).flatMap {
            if (it.error_code == null && it.result is ArrayList<*>) {
                val result = ArrayList<Comment>()
                for (item: Any in it.result)
                    result.add(ApiParser.parseComment(item as LinkedTreeMap<*, *>))

                Observable.fromArray(result)
            } else {
                throw Throwable(it.error_code.toString())
            }
        }
    }

    fun like(postID: Int, state: Int, sessionID: String): Observable<Boolean> {
        return this.apiFeed.like(FeedLikeQuery(postID, state, sessionID)).flatMap {
            if (it.error_code == null && it.result is LinkedTreeMap<*, *>)
                Observable.just(true)
            else
                throw Throwable(it.error_code.toString())
        }
    }

    fun report(postID: Int, sessionID: String): Observable<Boolean> {
        return this.apiFeed.report(FeedReportQuery(postID, sessionID)).flatMap {
            if (it.error_code == null && it.result is Boolean)
                Observable.just(true)
            else
                throw Throwable(it.error_code.toString())
        }
    }

    fun delete(postID: Int, sessionID: String): Observable<Boolean> {
        return this.apiFeed.delete(FeedDeleteQuery(postID, sessionID)).flatMap {
            if (it.error_code == null && it.result is Boolean)
                Observable.just(true)
            else
                throw Throwable(it.error_code.toString())
        }
    }
}