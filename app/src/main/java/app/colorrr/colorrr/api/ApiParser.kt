package app.colorrr.colorrr.api

import app.colorrr.colorrr.App
import app.colorrr.colorrr.R
import app.colorrr.colorrr.entity.*
import com.google.gson.internal.LinkedTreeMap
import java.net.UnknownHostException

object ApiParser {
    fun parseError(error: Throwable): String {
        val appContext = App.getInstance().applicationContext
        return when {
            error is UnknownHostException -> appContext.getString(R.string.no_internet_connection)
            error.message == ApiWorker.ERROR_INVALID_CREDENTIALS -> appContext.getString(R.string.invalid_arguments_passed_to_request)
            error.message == ApiWorker.ERROR_INVALID_ARGUMENTS -> appContext.getString(R.string.invalid_arguments_passed_to_request)
            error.message == ApiWorker.ERROR_DB -> appContext.getString(R.string.database_connection_error)
            error.message == ApiWorker.ERROR_NOT_AUTHORIZED -> appContext.getString(R.string.you_are_not_authorised_to_perform_this_action)
            error.message == ApiWorker.ERROR_UNKNOWN -> appContext.getString(R.string.unknown_error_occurred_please_try_again_later)
            else -> error.localizedMessage
        }
    }

    fun parseImage(userID: Int, dataObj: LinkedTreeMap<*, *>): HashMap<String, Image> {
        val imagesMap = HashMap<String, Image>()
        if (dataObj.containsKey("user_image_id")) {
            imagesMap["unfinished"] = ImageUnfinished(
                (dataObj["user_image_id"] as Double).toInt(),
                (dataObj["id"] as Double).toInt(),
                userID,
                dataObj["user_image_timestamp"] as Double,
                0,
                dataObj["user_image_link_preview"] as String,
                dataObj["user_image_link_archive"] as String
            )
        }

        val related = ArrayList<Int>()
        val relatedImages = dataObj["related"] as ArrayList<*>
        relatedImages.forEach { it1 -> related.add((it1 as Double).toInt()) }

        imagesMap["original"] = ImageOriginal(
            (dataObj["id"] as Double).toInt(),
            dataObj["name"] as String,
            dataObj["timestamp"] as Double,
            (dataObj["for_child"] as Double).toInt(),
            (dataObj["coloring_count"] as Double).toInt(),
            (dataObj["premium_image"] as Double).toInt(),
            (dataObj["categoryID"] as Double).toInt(),
            dataObj["link_preview"] as String,
            dataObj["link_archive"] as String,
            related
        )

        return imagesMap
    }

    fun parseCategory(userID: Int, dataObj: LinkedTreeMap<*, *>): HashMap<String, Any> {
        val imagesArr = ArrayList<HashMap<String, Image>>()
        if (dataObj.contains("images")) {
            val imagesList = dataObj["images"] as ArrayList<*>
            imagesList.forEach {
                val imageObj = it as LinkedTreeMap<*, *>
                val related = ArrayList<Int>()
                val relatedImages = imageObj["related"] as ArrayList<*>
                relatedImages.forEach { it1 -> related.add((it1 as Double).toInt()) }

                val imagesMap = HashMap<String, Image>()
                if (imageObj.containsKey("user_image_id")) {
                    imagesMap["unfinished"] = ImageUnfinished(
                        (imageObj["user_image_id"] as Double).toInt(),
                        (imageObj["id"] as Double).toInt(),
                        userID,
                        imageObj["user_image_timestamp"] as Double,
                        0,
                        imageObj["user_image_link_preview"] as String,
                        imageObj["user_image_link_archive"] as String
                    )
                }

                imagesMap["original"] = ImageOriginal(
                    (imageObj["id"] as Double).toInt(),
                    imageObj["name"] as String,
                    imageObj["timestamp"] as Double,
                    (imageObj["for_child"] as Double).toInt(),
                    (imageObj["coloring_count"] as Double).toInt(),
                    (imageObj["premium_image"] as Double).toInt(),
                    (imageObj["categoryID"] as Double).toInt(),
                    imageObj["link_preview"] as String,
                    imageObj["link_archive"] as String,
                    related
                )

                imagesArr.add(imagesMap)
            }
        }

        val category = Category(
            (dataObj["id"] as Double).toInt(),
            dataObj["name"] as String,
            dataObj["description"] as String,
            (dataObj["show_in_feed"] as Double).toInt(),
            (dataObj["featured"] as Double).toInt(),
            dataObj["cover_image"] as String,
            dataObj["timestamp"] as Double,
            (dataObj["debug"] as Double).toInt()
        )

        val result = HashMap<String, Any>()
        result["category"] = category
        result["images"] = imagesArr

        return result
    }

    fun parseUserSingIn(dataObj: LinkedTreeMap<*, *>): HashMap<String, Any> {
        val user = this.parseUser(dataObj["user"] as LinkedTreeMap<*, *>)
        val session = Session(user.userID, dataObj["session_id"].toString())
        val result = HashMap<String, Any>()
        result["user"] = user
        result["session"] = session
        return result
    }

    fun parseUserSingUp(dataObj: LinkedTreeMap<*, *>): HashMap<String, Any> {
        val anonymous = if (dataObj.containsKey("is_anonymous"))
            dataObj["is_anonymous"] as Boolean
        else
            false

        val dataUser = dataObj["user"] as LinkedTreeMap<*, *>
        val userObj = User(
            (dataUser["id"] as Double).toInt(),
            dataUser["email"] as String,
            dataUser["name"] as String,
            dataUser["imagePath"] as String,
            1,
            0,
            1,
            0,
            anonymous,
            0,
            0,
            0,
            0
        )

        val user = UserCurrent(userObj, ArrayList(), ArrayList(), ArrayList())


        val session = Session(user.userID, dataObj["session_id"].toString())
        val newUser = dataObj["new_user"] as Boolean
        val result = HashMap<String, Any>()
        result["user"] = user
        result["session"] = session
        result["newUser"] = newUser
        return result
    }

    fun parseUser(dataObj: LinkedTreeMap<*, *>): UserCurrent {
        val user = User(
            (dataObj["id"] as Double).toInt(),
            dataObj["email"] as String,
            dataObj["name"] as String,
            dataObj["imagePath"] as String,
            (dataObj["settings_alerts"] as Double).toInt(),
            (dataObj["settings_dark_theme"] as Double).toInt(),
            (dataObj["settings_repaint"] as Double).toInt(),
            (dataObj["account_state"] as Double).toInt(),
            dataObj["is_anonymous"] as Boolean,
            (dataObj["followers_count"] as Double).toInt(),
            (dataObj["followed_count"] as Double).toInt(),
            (dataObj["likes_count"] as Double).toInt(),
            (dataObj["published_count"] as Double).toInt()
        )

        val publishedArr = dataObj["published"] as ArrayList<*>
        val published = ArrayList<ImagePublished>()
        publishedArr.forEach {
            val item = it as LinkedTreeMap<*, *>
            published.add(
                ImagePublished(
                    (item["id"] as Double).toInt(),
                    (item["image_id"] as Double).toInt(),
                    (item["userID"] as Double).toInt(),
                    item["timestamp"] as Double,
                    if (item.containsKey("has_timelapse")) {
                        (item["has_timelapse"] as Double).toInt()
                    } else {
                        0
                    },
                    item["userName"] as String,
                    item["userAvatar"] as String,
                    item["userEmail"] as String,
                    (item["likes_count"] as Double).toInt(),
                    (item["comments_count"] as Double).toInt(),
                    if (item.containsKey("liked")) {
                        (item["liked"] as Double).toInt()
                    } else {
                        0
                    },
                    if (item.containsKey("followed")) {
                        (item["followed"] as Double).toInt()
                    } else {
                        0
                    },
                    item["link_preview"] as String,
                    item["link_archive"] as String
                )
            )
        }

        val unfinishedArr = dataObj["unfinished"] as ArrayList<*>
        val unfinished = ArrayList<ImageUnfinished>()
        unfinishedArr.forEach {
            val item = it as LinkedTreeMap<*, *>
            unfinished.add(
                ImageUnfinished(
                    (item["id"] as Double).toInt(),
                    (item["imageID"] as Double).toInt(),
                    (item["userID"] as Double).toInt(),
                    item["timestamp"] as Double,
                    (item["feedID"] as Double).toInt(),
                    item["link_preview"] as String,
                    item["link_archive"] as String
                )
            )
        }

        val premium = ArrayList<ImagePremium>()
        val premiumImages = dataObj["premium_images"] as ArrayList<*>
        premiumImages.forEach {
            premium.add(ImagePremium(null, (it as Double).toInt(), user.userID))
        }

        return UserCurrent(user, published, unfinished, premium)
    }

    fun parseSystem(dataObj: LinkedTreeMap<*, *>): TosPrivacy {
        return TosPrivacy(1, dataObj["tos"] as String, dataObj["privacy_policy"] as String)
    }

    fun parsePalette(dataObj: LinkedTreeMap<*, *>): Palette {
        val colors = ArrayList<String>()
        for (item in dataObj["colors"] as ArrayList<*>)
            colors.add(item.toString())

        return Palette(
            (dataObj["id"] as Double).toInt(),
            (dataObj["userID"] as Double).toInt(),
            (dataObj["premium"] as Double).toInt(),
            dataObj["name"] as String,
            dataObj["main_color"] as String,
            (dataObj["debug"] as Double).toInt(),
            colors
        )
    }

    fun parseFileLoad(dataObj: LinkedTreeMap<*, *>): HashMap<String, String> {
        val result = HashMap<String, String>()
        result["path"] = dataObj["path"].toString()
        result["download_url"] = dataObj["download_url"].toString()
        return result
    }

    fun parseComment(name: String, imagePath: String, dataObj: LinkedTreeMap<*, *>): Comment {
        return Comment(
            (dataObj["id"] as Double).toInt(),
            (dataObj["postID"] as Double).toInt(),
            dataObj["text"] as String,
            dataObj["timestamp"] as Double,
            (dataObj["senderID"] as Double).toInt(),
            name,
            imagePath
        )
    }

    fun parseComment(dataObj: LinkedTreeMap<*, *>): Comment {
        return Comment(
            (dataObj["id"] as Double).toInt(),
            (dataObj["postID"] as Double).toInt(),
            dataObj["text"] as String,
            dataObj["timestamp"] as Double,
            (dataObj["userID"] as Double).toInt(),
            dataObj["userName"] as String,
            dataObj["userAvatar"] as String
        )
    }

    fun parseFeed(dataObj: LinkedTreeMap<*, *>): HashMap<String, Any> {
        val result = HashMap<String, Any>()

        var lastComment: Comment? = null
        if (dataObj["last_comment"] is LinkedTreeMap<*, *>)
            lastComment = parseComment(
                dataObj["userName"] as String,
                dataObj["userAvatar"] as String,
                dataObj["last_comment"] as LinkedTreeMap<*, *>
            )

        val feed = Feed(
            (dataObj["id"] as Double).toInt(),
            (dataObj["image_id"] as Double).toInt(),
            (dataObj["userID"] as Double).toInt(),
            dataObj["timestamp"] as Double,
            (dataObj["has_timelapse"] as Double).toInt(),
            dataObj["userName"] as String,
            dataObj["userAvatar"] as String,
            dataObj["userEmail"] as String,
            (dataObj["likes_count"] as Double).toInt(),
            (dataObj["comments_count"] as Double).toInt(),
            (dataObj["liked"] as Double).toInt(),
            (dataObj["followed"] as Double).toInt(),
            lastComment?.id ?: 0,
            dataObj["link_preview"] as String,
            dataObj["link_archive"] as String
        )

        lastComment?.let { result["comment"] = it }
        result["feed"] = feed

        return result
    }

    fun parseFollower(dataObj: LinkedTreeMap<*, *>): Follower {
        var isFollowed = if (dataObj.containsKey("is_followed")) (dataObj["is_followed"] as Double).toInt() else 0
        if (isFollowed > 1)
            isFollowed = 1

        return Follower(
            (dataObj["id"] as Double).toInt(),
            dataObj["name"] as String,
            dataObj["imagePath"] as String,
            isFollowed
        )
    }
}