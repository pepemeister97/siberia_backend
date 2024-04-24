package siberia.modules.files.service

import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import siberia.modules.gallery.data.models.GalleryModel
import siberia.utils.kodein.KodeinService

class FilesService(di: DI) : KodeinService(di) {
    fun getOriginal(filename : String) : String? = transaction{
        with (GalleryModel.select {
            GalleryModel.url eq filename
        }.map {
            it[GalleryModel.original]
        }) {
            if (this.isEmpty()) null
            else this.first()
        }
    }
}