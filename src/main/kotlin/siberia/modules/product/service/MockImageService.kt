package siberia.modules.product.service

import org.kodein.di.DI
import siberia.utils.kodein.KodeinService

class MockImageService(di: DI) : KodeinService(di) {
    fun filterExists(products: List<Int>): List<Int> {
        return products
    }

}