package app.colorrr.colorrr.entity

class UserCurrent(
    user: User,
    val publishedImages: ArrayList<ImagePublished>,
    val unfinishedImages: ArrayList<ImageUnfinished>,
    val premiumImages: ArrayList<ImagePremium>
) : User(
    user.userID,
    user.email,
    user.name,
    user.image,
    user.alerts,
    user.darkTheme,
    user.repaint,
    user.premium,
    user.isAnonymous,
    user.followersCount,
    user.followedCount,
    user.likesCount,
    user.publishedCount
)