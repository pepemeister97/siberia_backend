package siberia.modules.brand.data.models

import siberia.utils.database.BaseIntIdTable

object BrandModel : BaseIntIdTable() {
    val name = text("name")
}