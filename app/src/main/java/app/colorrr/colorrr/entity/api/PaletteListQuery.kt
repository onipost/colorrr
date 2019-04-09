package app.colorrr.colorrr.entity.api

class PaletteListQuery(var locale: String)

class PaletteUserListQuery(val user_id: Int, val locale: String, val session_id: String)