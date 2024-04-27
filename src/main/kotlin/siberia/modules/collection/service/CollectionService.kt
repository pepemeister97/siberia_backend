package siberia.modules.collection.service

import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.collection.data.dao.CollectionDao
import siberia.modules.collection.data.dto.CollectionInputDto
import siberia.modules.collection.data.dto.CollectionOutputDto
import siberia.modules.collection.data.dto.CollectionRemoveResultDto
import siberia.modules.collection.data.dto.CollectionUpdateDto
import siberia.modules.collection.data.models.CollectionModel
import siberia.modules.user.data.dao.UserDao
import siberia.utils.kodein.KodeinService

class CollectionService(di: DI) : KodeinService(di) {
    fun create(authorizedUser: AuthorizedUser, collectionInputDto: CollectionInputDto, shadowed: Boolean = false): CollectionOutputDto = transaction {
        val userDao = UserDao[authorizedUser.id]
        val collectionDao = CollectionDao.new(userDao.login, shadowed) {
            name = collectionInputDto.name
        }.toOutputDto()
        commit()

        collectionDao
    }

    fun update(authorizedUser: AuthorizedUser, collectionId: Int, collectionUpdateDto: CollectionUpdateDto): CollectionOutputDto = transaction {
        val userDao = UserDao[authorizedUser.id]
        val collectionDao = CollectionDao[collectionId]
        collectionDao.loadAndFlush(userDao.login, collectionUpdateDto)
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
        CollectionModel.selectAll()
            .orderBy(CollectionModel.name to SortOrder.ASC)
            .map {
                CollectionOutputDto(
                    id = it[CollectionModel.id].value,
                    name = it[CollectionModel.name]
                )
            }
    }

    fun getOne(collectionId: Int): CollectionOutputDto = transaction {
        CollectionDao[collectionId].toOutputDto()
    }
}