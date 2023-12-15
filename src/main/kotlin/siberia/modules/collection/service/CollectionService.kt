package siberia.modules.collection.service

import org.jetbrains.exposed.sql.transactions.transaction
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
        CollectionDao.new(userDao.login) {
            name = collectionInputDto.name
        }.toOutputDto()
    }

    fun update(authorizedUser: AuthorizedUser, collectionId: Int, collectionInputDto: CollectionInputDto): CollectionOutputDto = transaction {
        val userDao = UserDao[authorizedUser.id]
        val collectionDao = CollectionDao[collectionId]
        collectionDao.name = collectionInputDto.name
        collectionDao.flush(userDao.login)
        collectionDao.toOutputDto()
    }

    fun remove(authorizedUser: AuthorizedUser, collectionId: Int): CollectionRemoveResultDto = transaction {
        val userDao = UserDao[authorizedUser.id]
        val collectionDao = CollectionDao[collectionId]
        collectionDao.delete(userDao.login)
        CollectionRemoveResultDto(
            success = true,
            message = "Brand successfully removed"
        )
    }

    fun getAll(): List<CollectionOutputDto> = transaction {
        CollectionDao.all().map { it.toOutputDto() }
    }

    fun getOne(collectionId: Int): CollectionOutputDto = transaction {
        CollectionDao[collectionId].toOutputDto()
    }
}