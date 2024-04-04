package siberia.modules.product.data.dto.csv

import siberia.exceptions.ValidateException
import siberia.modules.product.data.dto.ProductCreateDto
import siberia.utils.parse.AbstractCsvMapper
import kotlin.reflect.KClass

class ProductCreateCsvMapper : AbstractCsvMapper<ProductCreateDto>() {
    override val required: Map<String, Boolean>
        get() = mapOf(
            "vendorCode"          to true,
            "eanCode"             to true,
            "barcode"             to true,
            "name"                to true,
            "description"         to true,
            "commonPrice"         to true,
            "distributorPercent"  to true,
            "professionalPercent" to true,
            "category"            to true,
            "color"               to true,
            "amountInBox"         to true,
            "expirationDate"      to true,
            "link"                to true,
        )
    override val klass: KClass<ProductCreateDto>
        get() = ProductCreateDto::class

    fun readList(bytes : ByteArray): List<ProductCreateDto> {
        val list = parseCSV(bytes)

        return list.filterNotNull()
            .apply {
                if (size != list.size)
                    throw ValidateException()
            }
    }
}