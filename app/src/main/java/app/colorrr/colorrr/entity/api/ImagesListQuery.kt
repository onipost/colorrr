package app.colorrr.colorrr.entity.api

class ImagesListQuery(
    var start: Int,
    var limit: Int,
    var category_id: Int,
    var debug: Int,
    var locale: String,
    var session_id: String
)

class ImagesListForChildQuery(
    var start: Int,
    var limit: Int,
    var category_id: Int,
    var for_child: Int,
    var debug: Int,
    var locale: String,
    var session_id: String
)