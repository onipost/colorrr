package app.colorrr.colorrr.ui.categories

interface CategoriesInterface {
    fun onDataArrived(refreshList: Boolean, data: HashMap<String, Any>)
}