package app.colorrr.colorrr.entity.api

class FeedQuery(
    var start: Int,
    var limit: Int,
    var sort: String?,
    var session_id: String
)

class FeedCommentsQuery(
    var start: Int,
    var limit: Int,
    var post_id: Int
)

class FeedLikeQuery(
    var post_id: Int,
    var state: Int,
    var session_id: String
)

class FeedReportQuery(
    var id: Int,
    var session_id: String
)

class FeedDeleteQuery(
    var id: Int,
    var session_id: String
)