package siberia.modules.product.service

import org.kodein.di.DI
import siberia.exceptions.ValidateException
import siberia.modules.product.data.dto.ProductCreateDto
import siberia.utils.kodein.KodeinService
import siberia.utils.parse.CSVParser

class ProductParseService (di: DI) : KodeinService(di) {
    fun parseCSVtoProductDto(bytes : ByteArray) : List<ProductCreateDto> {
        val list = CSVParser.parseCSV(bytes, ProductCreateDto::class)

        return  list.filterIsInstance<ProductCreateDto>()
            .apply {
                if (size != list.size)
                    throw ValidateException()
            }
    }
}