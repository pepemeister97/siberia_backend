package siberia.modules.gallery.data.dao

import org.jetbrains.exposed.dao.id.EntityID
import siberia.modules.gallery.data.dto.ImageOutputDto
import siberia.modules.gallery.data.dto.ImageUpdateDto
import siberia.modules.gallery.data.models.GalleryModel
import siberia.modules.user.data.dao.UserDao

import siberia.utils.database.BaseIntEntity
import siberia.utils.database.BaseIntEntityClass
import siberia.utils.database.idValue

class GalleryDao(id: EntityID<Int>): BaseIntEntity<ImageOutputDto>(id, GalleryModel) {

    companion object: BaseIntEntityClass<ImageOutputDto, GalleryDao>(GalleryModel)

    var url by GalleryModel.url
    var name by GalleryModel.name
    var description by GalleryModel.description
    var original by GalleryModel.original

    var author by UserDao optionalReferencedOn GalleryModel.authorId
    override fun toOutputDto(): ImageOutputDto =
        ImageOutputDto(
            idValue,
            name,
            url,
            author?.login,
            description,
            original
        )

    fun loadUpdate(imageUpdateDto: ImageUpdateDto) {
        name = imageUpdateDto.name ?: name
        description = imageUpdateDto.description ?: description
    }
}