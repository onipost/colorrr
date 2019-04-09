package app.colorrr.colorrr.entity.api

class UpdateUserQuery(var name: String, var email: String, var image_path: String, var session_id: String)

class UpdateUserPasswordQuery(var old_password: String, var new_password: String, var session_id: String)