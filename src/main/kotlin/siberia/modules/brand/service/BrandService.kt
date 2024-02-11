package siberia.modules.brand.service

import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.brand.data.dao.BrandDao
import siberia.modules.brand.data.dto.BrandOutputDto
import siberia.modules.brand.data.dto.BrandInputDto
import siberia.modules.brand.data.dto.BrandRemoveResultDto
import siberia.modules.brand.data.dto.BrandUpdateDto
import siberia.modules.user.data.dao.UserDao
import siberia.utils.kodein.KodeinService

class BrandService(di: DI) : KodeinService(di) {
    fun create(authorizedUser: AuthorizedUser, brandInputDto: BrandInputDto): BrandOutputDto = transaction {
        val userDao = UserDao[authorizedUser.id]
        val brandDao = BrandDao.new(userDao.login) {
            name = brandInputDto.name
        }.toOutputDto()
        commit()

        brandDao
    }

    fun update(authorizedUser: AuthorizedUser, brandId: Int, brandUpdateDto: BrandUpdateDto): BrandOutputDto = transaction {
        val userDao = UserDao[authorizedUser.id]
        val brandDao = BrandDao[brandId]
        brandDao.loadAndFlush(userDao.login, brandUpdateDto)
        commit()

        brandDao.toOutputDto()
    }

    fun remove(authorizedUser: AuthorizedUser, brandId: Int): BrandRemoveResultDto = transaction {
        val userDao = UserDao[authorizedUser.id]
        val brandDao = BrandDao[brandId]
        brandDao.delete(userDao.login)
        commit()

        BrandRemoveResultDto(
            success = true,
            message = "Brand successfully removed"
        )
    }

    fun getAll(): List<BrandOutputDto> = transaction {
        BrandDao.all().map { it.toOutputDto() }
    }

    fun getOne(brandId: Int): BrandOutputDto = transaction {
        BrandDao[brandId].toOutputDto()
    }
}