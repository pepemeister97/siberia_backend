package siberia.modules.product.data.models

import org.jetbrains.exposed.sql.ReferenceOption
import siberia.modules.gallery.data.models.GalleryModel
import siberia.utils.database.BaseIntIdTable

object ProductToImageModel: BaseIntIdTable() {
    val photo = reference("photo", GalleryModel, ReferenceOption.CASCADE, ReferenceOption.CASCADE)
    val product = reference("product", ProductModel, ReferenceOption.CASCADE, ReferenceOption.CASCADE)
}