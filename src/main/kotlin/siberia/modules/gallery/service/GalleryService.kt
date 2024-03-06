package siberia.modules.gallery.service


import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.gallery.data.dao.GalleryDao
import siberia.modules.gallery.data.dto.ImageCreateDto
import siberia.modules.gallery.data.dto.ImageOutputDto
import siberia.modules.gallery.data.dto.ImageSearchFilterDto
import siberia.modules.gallery.data.models.GalleryModel
import siberia.modules.product.data.dto.ProductRemoveResultDto
import siberia.modules.transaction.data.dao.TransactionStatusDao.Companion.createLikeCond
import siberia.modules.transaction.data.dao.TransactionStatusDao.Companion.timeCond
import siberia.modules.user.data.dao.UserDao
import siberia.modules.user.data.models.UserModel
import siberia.utils.files.FilesUtil
import siberia.utils.kodein.KodeinService

class GalleryService(di: DI) : KodeinService(di) {
    fun create(authorizedUser: AuthorizedUser, images : List<ImageCreateDto>) = transaction{
        val userDao = UserDao[authorizedUser.id]
        val returnList = mutableListOf<ImageOutputDto>()
        images.forEach {
            val photoName = FilesUtil.buildName(it.photoName)
            returnList.add(
                GalleryDao.new {
                    url = photoName
                    name = it.name
                    author = userDao
                    description = it.description
                }.toOutputDto()
            )
            FilesUtil.upload(it.imageBase64, photoName)
        }

        commit()

        returnList
    }
    fun remove(imageId : Int) = transaction{
        val galleryDao = GalleryDao[imageId]

        galleryDao.delete()

        commit()

        ProductRemoveResultDto(
            success = true,
            message = "Image $imageId (${galleryDao.name}) successfully removed"
        )
    }
    fun getOne(imageId : Int) : ImageOutputDto = transaction{
        val image = GalleryDao[imageId]
        ImageOutputDto(
            imageId,
            image.name,
            image.url,
            image.author?.login,
            image.description
        )
    }
    fun getAll(filter: ImageSearchFilterDto?) : List<ImageOutputDto> = transaction {
        GalleryModel
            .leftJoin(UserModel)
            .select {
                createLikeCond(filter?.name, GalleryModel.id neq 0, GalleryModel.name) and
                timeCond(Pair(filter?.rangeStart, filter?.rangeEnd), GalleryModel.createdAt)
            }
            .map {
                ImageOutputDto(
                   id = it[GalleryModel.id].value,
                   name = it[GalleryModel.name],
                   url = it[GalleryModel.url],
                   author = it[UserModel.login],
                   description = null
               )
            }
    }
    fun filterExists(images : List<Int>) : List<Int> = transaction{
        val galleryDao = GalleryDao
        val returnList = mutableListOf<Int>()
        images.forEach {
            if (galleryDao.findById(it) != null) {
                returnList.add(it)
            }
        }
        returnList
    }
}