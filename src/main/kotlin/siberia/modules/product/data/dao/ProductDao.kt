package siberia.modules.product.data.dao

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.select
import siberia.modules.brand.data.dao.BrandDao
import siberia.modules.category.data.dao.CategoryDao
import siberia.modules.collection.data.dao.CollectionDao
import siberia.modules.product.data.dto.ProductFullOutputDto
import siberia.modules.product.data.dto.ProductListItemOutputDto
import siberia.modules.product.data.dto.ProductOutputDto
import siberia.modules.product.data.dto.ProductUpdateDto
import siberia.modules.product.data.models.ProductModel
import siberia.modules.stock.data.models.StockToProductModel
import siberia.utils.database.BaseIntEntity
import siberia.utils.database.BaseIntEntityClass
import siberia.utils.database.idValue

class ProductDao(id: EntityID<Int>): BaseIntEntity<ProductOutputDto>(id, ProductModel) {

    companion object: BaseIntEntityClass<ProductOutputDto, ProductDao>(ProductModel)

    var photo by ProductModel.photo
    var vendorCode by ProductModel.vendorCode
    var barcode by ProductModel.barcode

    private val _brandId by ProductModel.brand
    val brandId: Int? get() = _brandId?.value
    var brand by BrandDao optionalReferencedOn ProductModel.brand

    var name by ProductModel.name
    var description by ProductModel.description
    var lastPurchasePrice by ProductModel.lastPurchasePrice
    val cost by ProductModel.cost
    val lastPurchaseDate by ProductModel.lastPurchaseDate
    var distributorPrice by ProductModel.distributorPrice
    var professionalPrice by ProductModel.professionalPrice
    var commonPrice by ProductModel.commonPrice

    private val _categoryId by ProductModel.category
    val categoryId: Int? get() = _categoryId?.value
    var category by CategoryDao optionalReferencedOn ProductModel.category

    private val _collectionId by ProductModel.collection
    val collectionId: Int? get() = _collectionId?.value
    var collection by CollectionDao optionalReferencedOn ProductModel.collection

    var color by ProductModel.color
    var amountInBox by ProductModel.amountInBox
    var expirationDate by ProductModel.expirationDate
    var link by ProductModel.link

//    Future iterations
//    var size by ProductModel.size
//    var volume by ProductModel.volume

    override fun toOutputDto(): ProductOutputDto =
        ProductOutputDto(
            idValue, photo, vendorCode, barcode,
            brandId, name, description, lastPurchasePrice,
            cost, lastPurchaseDate, distributorPrice,
            professionalPrice, commonPrice, categoryId,
            collectionId, color, amountInBox,
            expirationDate, link, // size, volume,
        )

    fun fullOutput(): ProductFullOutputDto {
        val quantity = StockToProductModel.select {
            StockToProductModel.product eq this@ProductDao.id
        }.sumOf { it[StockToProductModel.amount] }
        return ProductFullOutputDto(
            idValue, photo, vendorCode, barcode,
            brand?.toOutputDto(), name, description, lastPurchasePrice,
            cost, lastPurchaseDate, distributorPrice,
            professionalPrice, commonPrice, category?.toOutputDto(),
            collection?.toOutputDto(), color, amountInBox,
            expirationDate, link, quantity
            //size, volume
        )
    }

    val listItemDto: ProductListItemOutputDto get() = ProductListItemOutputDto(
        id = idValue, name = name, vendorCode = vendorCode, price = commonPrice
    )

    fun loadUpdateDto(productUpdateDto: ProductUpdateDto) {
        photo = productUpdateDto.photoName ?: photo
        vendorCode = productUpdateDto.vendorCode ?: vendorCode
        barcode = productUpdateDto.barcode ?: barcode

        brand = if (productUpdateDto.brand != 0 && productUpdateDto.brand != null) BrandDao[productUpdateDto.brand]
                else if (productUpdateDto.brand == 0) null
                else brand

        name = productUpdateDto.name ?: name
        description = productUpdateDto.description ?: description
        distributorPrice = productUpdateDto.distributorPrice ?: distributorPrice
        professionalPrice = productUpdateDto.professionalPrice ?: professionalPrice
        commonPrice = productUpdateDto.commonPrice ?: commonPrice
        category = if (productUpdateDto.category != null) CategoryDao[productUpdateDto.category] else category

        collection = if (productUpdateDto.collection != 0 && productUpdateDto.collection != null) CollectionDao[productUpdateDto.collection]
                    else if (productUpdateDto.collection == 0) null
                    else collection

        color = productUpdateDto.color ?: color
        amountInBox = productUpdateDto.amountInBox ?: amountInBox
        expirationDate = productUpdateDto.expirationDate ?: expirationDate
        link = productUpdateDto.description ?: description
    }
}