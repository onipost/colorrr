package app.colorrr.colorrr.entity.api

class CategoriesListQuery(
    var start: Int,
    var limit: Int,
    var need_images: Int,
    var images_start: Int,
    var images_limit: Int,
    var debug: Int,
    var locale: String,
    var session_id: String,
    var popular: Int? = null,
    var premium: Int? = null,
    var for_child: Int? = null,
    var recommended: Int? = null
)