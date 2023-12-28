package siberia.modules.collection.service

import siberia.utils.database.transaction
import org.kodein.di.DI
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.collection.data.dao.CollectionDao
import siberia.modules.collection.data.dto.CollectionInputDto
import siberia.modules.collection.data.dto.CollectionOutputDto
import siberia.modules.collection.data.dto.CollectionRemoveResultDto
import siberia.modules.user.data.dao.UserDao
import siberia.utils.kodein.KodeinService

class CollectionService(di: DI) : KodeinService(di) {
    fun create(authorizedUser: AuthorizedUser, collectionInputDto: CollectionInputDto): CollectionOutputDto = transaction {
        val userDao = UserDao[authorizedUser.id]
        val collectionDao = CollectionDao.new(userDao.login) {
            name = collectionInputDto.name
        }.toOutputDto()
        commit()

        collectionDao
    }

    fun update(authorizedUser: AuthorizedUser, collectionId: Int, collectionInputDto: CollectionInputDto): CollectionOutputDto = transaction {
        val userDao = UserDao[authorizedUser.id]
        val collectionDao = CollectionDao[collectionId]
        collectionDao.name = collectionInputDto.name
        collectionDao.flush(userDao.login)
        commit()

        collectionDao.toOutputDto()
    }

    fun remove(authorizedUser: AuthorizedUser, collectionId: Int): CollectionRemoveResultDto = transaction {
        val userDao = UserDao[authorizedUser.id]
        val collectionDao = CollectionDao[collectionId]
        collectionDao.delete(userDao.login)
        commit()

        CollectionRemoveResultDto(
            success = true,
            message = "Collection $collectionId successfully removed"
        )
    }

    fun getAll(): List<CollectionOutputDto> = transaction {
        CollectionDao.all().map { it.toOutputDto() }
    }

    fun getOne(collectionId: Int): CollectionOutputDto = transaction {
        CollectionDao[collectionId].toOutputDto()
    }
}