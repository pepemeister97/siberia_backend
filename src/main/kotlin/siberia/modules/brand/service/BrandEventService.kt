package siberia.modules.brand.service

import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import org.kodein.di.instance
import siberia.exceptions.BadRequestException
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.brand.data.dto.BrandInputDto
import siberia.modules.brand.data.dto.BrandUpdateDto
import siberia.modules.brand.data.models.BrandModel
import siberia.modules.logger.data.dto.SystemEventOutputDto
import siberia.modules.logger.data.models.SystemEventModel
import siberia.utils.kodein.KodeinEventService

class BrandEventService(di: DI) : KodeinEventService(di) {
    private val brandService: BrandService by instance()
    override fun rollbackUpdate(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) : Unit = transaction {
        val updateEventData = event.getRollbackData<BrandUpdateDto>()
        with(
            BrandModel.slice(BrandModel.id).select {
                BrandModel.id eq updateEventData.objectId
            }
            .map {
                it[BrandModel.id]
            }
        ) {
            if (this.isNotEmpty())
                brandService.update(authorizedUser, updateEventData.objectId, updateEventData.objectDto)
            else throw BadRequestException("rollback failed model removed")
        }
    }

    override fun rollbackRemove(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) = transaction {
        val createEventData = event.getRollbackData<BrandInputDto>()
        val createdBrand = brandService.create(authorizedUser, createEventData.objectDto, shadowed = true)

        SystemEventModel.replaceRemovedWithNewId(
            event.eventObjectTypeId,
            createEventData.objectId,
            createdBrand.id
        )
    }

    override fun rollbackCreate(authorizedUser: AuthorizedUser, event: SystemEventOutputDto) {
        TODO("Not yet implemented")
    }
}