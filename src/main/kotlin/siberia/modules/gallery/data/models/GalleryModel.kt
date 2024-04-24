package siberia.modules.gallery.data.models

import org.jetbrains.exposed.sql.ReferenceOption
import siberia.modules.user.data.models.UserModel
import siberia.utils.database.BaseIntIdTable

object GalleryModel : BaseIntIdTable() {
    val url = text("url")
    val name = text("image_name")
    val authorId = reference("author", UserModel, ReferenceOption.SET_NULL, ReferenceOption.SET_NULL).nullable()
    val description = text("description")
    val original = text("url_original").nullable()
}