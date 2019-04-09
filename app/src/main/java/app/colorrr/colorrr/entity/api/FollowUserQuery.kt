package app.colorrr.colorrr.entity.api

class FollowUserQuery(var user_id: Int, var state: Int, var session_id: String)

class FollowersListQuery(var start: Int, var limit: Int, var user_id: Int, var session_id: String)