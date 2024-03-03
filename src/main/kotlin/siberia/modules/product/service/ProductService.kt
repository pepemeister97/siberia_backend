package siberia.modules.product.service

import io.ktor.server.plugins.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNotNull
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import siberia.conf.AppConf
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.brand.data.dao.BrandDao
import siberia.modules.brand.data.dao.BrandDao.Companion.createRangeCond
import siberia.modules.category.data.dao.CategoryDao
import siberia.modules.collection.data.dao.CollectionDao
import siberia.modules.logger.data.models.SystemEventModel
import siberia.modules.product.data.dao.ProductDao
import siberia.modules.product.data.dto.*
import siberia.modules.product.data.dto.systemevents.ProductCreateEvent
import siberia.modules.product.data.models.ProductModel
import siberia.modules.rbac.data.dao.RoleDao.Companion.createNullableRangeCond
import siberia.modules.rbac.data.dao.RuleCategoryDao.Companion.createNullableListCond
import siberia.modules.stock.data.dao.StockDao
import siberia.modules.stock.data.dao.StockDao.Companion.createLikeCond
import siberia.modules.stock.data.dto.StockOutputDto
import siberia.modules.stock.data.models.StockModel
import siberia.modules.stock.data.models.StockToProductModel
import siberia.modules.transaction.data.dto.TransactionFullOutputDto
import siberia.modules.user.data.dao.UserDao
import siberia.plugins.Logger
import siberia.utils.files.FilesUtil
import siberia.utils.kodein.KodeinService
import java.time.LocalDateTime
import java.time.ZoneOffset

class ProductService(di: DI) : KodeinService(di) {
    fun create(authorizedUser: AuthorizedUser, productCreateDto: ProductCreateDto): ProductFullOutputDto = transaction {
        val userDao = UserDao[authorizedUser.id]
        val event = ProductCreateEvent(userDao.login, productCreateDto.name!!, productCreateDto.vendorCode!!)

        val photoName
        = if (!productCreateDto.fileAlreadyUploaded!! && !productCreateDto.photoName.isNullOrBlank())
            FilesUtil.buildName(productCreateDto.photoName!!)
        else if (productCreateDto.fileAlreadyUploaded!!) productCreateDto.photoName
        else ""

        val productDao = ProductDao.new {
            photo = photoName!!
            vendorCode = productCreateDto.vendorCode!!
            eanCode = productCreateDto.eanCode!!
            barcode = productCreateDto.barcode
            brand = if (productCreateDto.brand != null) BrandDao[productCreateDto.brand!!] else null
            name = productCreateDto.name!!
            description = productCreateDto.description!!
            distributorPrice = productCreateDto.distributorPrice!!
            professionalPrice = productCreateDto.professionalPrice!!
            commonPrice = productCreateDto.commonPrice!!
            category = if (productCreateDto.category != null) CategoryDao[productCreateDto.category!!] else null
            collection = if (productCreateDto.collection != null) CollectionDao[productCreateDto.collection!!] else null
            color = productCreateDto.color!!
            amountInBox = productCreateDto.amountInBox!!
            expirationDate = productCreateDto.expirationDate!!
            link = productCreateDto.link!!

//            Future iterations
//            size = productCreateDto.size
//            volume = productCreateDto.volume
        }

        SystemEventModel.logEvent(event)
        if (photoName != "" && !productCreateDto.fileAlreadyUploaded!!)
            FilesUtil.upload(productCreateDto.photoBase64!!, photoName!!)
        commit()

        productDao.fullOutput()
    }

    fun bulkInsert(authorizedUser: AuthorizedUser, list : List<ProductCreateDto>) : List<ProductListItemOutputDto> = transaction {
        val returnList = mutableListOf<ProductListItemOutputDto>()
        list.forEach {
            val product = create(authorizedUser, it)
            returnList.add(
                ProductListItemOutputDto(
                    product.id,
                    product.name,
                    product.vendorCode,
                    0.0,
                    product.commonPrice
                )
            )
        }
        returnList
    }

    fun update(authorizedUser: AuthorizedUser, productId: Int, productUpdateDto: ProductUpdateDto): ProductFullOutputDto = transaction {
        val userDao = UserDao[authorizedUser.id]
        val productDao = ProductDao[productId]
        val photoName =
            if (productUpdateDto.photoName != null &&
                productUpdateDto.photoName != "" &&
                productUpdateDto.fileAlreadyUploaded
            )
                FilesUtil.buildName(productUpdateDto.photoName!!)
            else if (
                productUpdateDto.photoName != null &&
                productUpdateDto.fileAlreadyUploaded
            )
                productUpdateDto.photoName!!
            else ""

        productUpdateDto.photoName = photoName
        productDao.loadAndFlush(userDao.login, productUpdateDto)

        if (photoName != "" && productUpdateDto.photoBase64 != null && !productUpdateDto.fileAlreadyUploaded)
            FilesUtil.upload(productUpdateDto.photoBase64!!, photoName)
        else if (photoName != "")
            throw BadRequestException("Photo base 64 must be provided")


        commit()

        productDao.fullOutput()
    }

    fun remove(authorizedUser: AuthorizedUser, productId: Int): ProductRemoveResultDto = transaction {
        val userDao = UserDao[authorizedUser.id]
        val productDao = ProductDao[productId]

        productDao.delete(userDao.login)
        commit()

        ProductRemoveResultDto(
            success = true,
            message = "Product $productId (${productDao.vendorCode}) successfully removed"
        )
    }

    private fun SqlExpressionBuilder.convertToOperator(productSearchDto: ProductSearchDto): Op<Boolean> {

        val searchFilterDto = productSearchDto.filters

        return createRangeCond(searchFilterDto?.amountInBox, (ProductModel.id neq 0), ProductModel.amountInBox, -1, Int.MAX_VALUE) and
            createRangeCond(searchFilterDto?.commonPrice, (ProductModel.id neq 0), ProductModel.commonPrice, -1.0, Double.MAX_VALUE) and
            createNullableRangeCond(searchFilterDto?.purchasePrice, (ProductModel.id neq 0), ProductModel.lastPurchasePrice, -1.0, Double.MAX_VALUE) and
            createRangeCond(searchFilterDto?.distributorPrice, (ProductModel.id neq 0), ProductModel.distributorPrice, -1.0, Double.MAX_VALUE) and
            createRangeCond(searchFilterDto?.professionalPrice, (ProductModel.id neq 0), ProductModel.professionalPrice, -1.0, Double.MAX_VALUE) and
            createNullableListCond(searchFilterDto?.brand, (ProductModel.id neq 0), ProductModel.brand) and
            createNullableListCond(searchFilterDto?.category, (ProductModel.id neq 0), ProductModel.category) and
            createNullableListCond(searchFilterDto?.collection, (ProductModel.id neq 0), ProductModel.collection) and
            createLikeCond(searchFilterDto?.name, (ProductModel.id neq 0), ProductModel.name) and
            createLikeCond(searchFilterDto?.color, (ProductModel.id neq 0), ProductModel.color) and
            createLikeCond(searchFilterDto?.vendorCode, (ProductModel.id neq 0), ProductModel.vendorCode) and
            createLikeCond(searchFilterDto?.description, (ProductModel.id neq 0), ProductModel.description)

//            Future iterations
//            createRangeCond(searchFilterDto.size, (ProductModel.id neq 0), ProductModel.size, -1.0, Double.MAX_VALUE) and
//            createRangeCond(searchFilterDto.volume, (ProductModel.id neq 0), ProductModel.volume, -1.0, Double.MAX_VALUE) and
    }

    private fun getByFilter(productSearchDto: ProductSearchDto, additionalFilters: Op<Boolean>): SizedIterable<ProductDao> = transaction {
        val paginationOutputDto = productSearchDto.pagination

        ProductDao.find {
            convertToOperator(productSearchDto) and
            additionalFilters
        }.let {
            if (paginationOutputDto == null)
                it
            else
                it.limit(paginationOutputDto.n, paginationOutputDto.offset)
        }
    }

    fun getByFilter(productSearchDto: ProductSearchDto): List<ProductListItemOutputDto> = transaction {
        getByFilter(productSearchDto, ProductModel.id.isNotNull()).map { it.listItemDto }
    }

    fun getOne(productId: Int): ProductFullOutputDto = transaction {
        ProductDao[productId].fullOutput()
    }

    fun updateLastPurchaseData(products: List<TransactionFullOutputDto.TransactionProductDto>, timestamp: LocalDateTime) {
        val timestampLong = timestamp.toEpochSecond(ZoneOffset.ofHours(AppConf.zoneOffset))
        products.forEach { product ->
            ProductModel.update({ ProductModel.id eq product.product.id }) {
                it[lastPurchasePrice] = (product.price ?: 0.0)
                it[lastPurchaseDate] = timestampLong
            }
        }
    }

    fun getAvailability(productId: Int): List<StockOutputDto> = transaction {
        //Check it exists
        ProductDao[productId]
         val stocks = StockToProductModel
            .slice(StockToProductModel.stock)
            .select { StockToProductModel.product eq productId }
            .map { it[StockToProductModel.stock] }

        StockDao.find { StockModel.id inList stocks }.map { it.toOutputDto() }
    }

    fun getAvailableByFilter(
        authorizedUser: AuthorizedUser,
        searchFilterDto: ProductSearchDto
    ): List<ProductListItemOutputDto> = transaction {

        val ordering = if (searchFilterDto.filters?.availability != null && searchFilterDto.filters.availability){
            listOf(StockToProductModel.amount to SortOrder.DESC_NULLS_LAST, ProductModel.id to SortOrder.ASC)
        } else{
            listOf(ProductModel.id to SortOrder.ASC)
        }

        ProductModel
            .join(
                StockToProductModel,
                JoinType.LEFT,
                additionalConstraint = {
                    (StockToProductModel.product eq ProductModel.id) and
                    (StockToProductModel.stock eq authorizedUser.stockId)
                }
            )
            .slice(
                ProductModel.id,
                ProductModel.name,
                ProductModel.vendorCode,
                ProductModel.commonPrice,
                StockToProductModel.amount,
                ProductModel.eanCode,
                ProductModel.photo
            )
            .select {
                convertToOperator(searchFilterDto)
            }
            .orderBy(*ordering.toTypedArray())
            .map {
                //If join returns nothing (no such product in stock) amount = 0
                val amount = try { it[StockToProductModel.amount].toDouble() } catch (_: Exception) { 0.0 }
                if (it[ProductModel.id].value < 4) {
                    Logger.debug(it[ProductModel.id].value, "main")
                    Logger.debug(amount, "main")
                }
                ProductListItemOutputDto(
                    id = it[ProductModel.id].value,
                    name = it[ProductModel.name],
                    vendorCode = it[ProductModel.vendorCode],
                    quantity = amount,
                    price = it[ProductModel.commonPrice],
                    fileName = it[ProductModel.photo],
                    eanCode = it[ProductModel.eanCode]
                )
            }
    }
}