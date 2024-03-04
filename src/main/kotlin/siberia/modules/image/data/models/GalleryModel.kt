package siberia.modules.image.data.models

import siberia.utils.database.BaseIntIdTable

object GalleryModel : BaseIntIdTable() {
    val photo = text("photo")
    val name = text("image_name")
    val authorId = integer("authorId")
    val description = text("description")
}