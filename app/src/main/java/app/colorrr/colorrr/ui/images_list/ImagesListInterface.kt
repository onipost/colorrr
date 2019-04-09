package app.colorrr.colorrr.ui.images_list

import app.colorrr.colorrr.entity.Category
import app.colorrr.colorrr.entity.ImageToCategory

interface ImagesListInterface {
    fun onCategoryArrived(category: Category)

    fun onImagesArrived(refreshList: Boolean, list: List<ImageToCategory>)
}