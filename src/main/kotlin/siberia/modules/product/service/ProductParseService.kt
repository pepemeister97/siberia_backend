package siberia.modules.product.service

import org.kodein.di.DI
import org.kodein.di.instance
import siberia.exceptions.ValidateException
import siberia.modules.auth.data.dto.AuthorizedUser
import siberia.modules.product.data.dto.ProductCreateDto
import siberia.utils.kodein.KodeinService
import siberia.utils.parse.CSVParser

class ProductParseService (di: DI) : KodeinService(di) {
    private val productService: ProductService by instance()
    fun parseCSVtoProductDto(authorizedUser: AuthorizedUser, bytes : ByteArray) : List<ProductCreateDto> {
        val listOfAny = CSVParser.parseCSV(bytes, ProductCreateDto::class)

        val listOfDto = listOfAny.filterIsInstance<ProductCreateDto>()
            .apply {
                if (size != listOfAny.size)
                    throw ValidateException()
            }

        listOfDto.forEach {
            productService.create(authorizedUser, it)
        }

        return listOfDto
    }
}