package app.colorrr.colorrr.entity.api

class CreateUserQuery(var name: String, var email: String, var password: String, var facebook_id: String, var image_path: String, var locale: String)


class CreateUserAnonymousQuery(var locale: String)