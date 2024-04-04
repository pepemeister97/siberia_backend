package siberia.modules.product.service

import org.kodein.di.DI
import siberia.modules.product.data.dto.ProductCreateDto
import siberia.modules.product.data.dto.csv.ProductCreateCsvMapper
import siberia.utils.kodein.KodeinService

class ProductParseService (di: DI) : KodeinService(di) {
    fun parseCSVtoProductDto(bytes : ByteArray) : List<ProductCreateDto> {
        val productCreateCsvMapper = ProductCreateCsvMapper()
        return productCreateCsvMapper.readList(bytes)
    }
}