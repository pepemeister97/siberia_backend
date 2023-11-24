package siberia.utils.kodein

import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.kodein.di.Instance
import org.kodein.di.instance
import org.kodein.type.jvmType

inline fun <reified T : Any> DI.MainBuilder.bindSingleton(crossinline callback: (DI) -> T) {
    bind<T>() with singleton { callback(this@singleton.di) }
}

fun Application.kodeinApplication(
    kodeinMapper: DI.MainBuilder.(Application) -> Unit = {}
) {
    val application = this

    val kodein = DI {
        bind<Application>() with instance(application)
        kodeinMapper(this, application)
    }

    routing {
        for (bind in kodein.container.tree.bindings) {
            val bindClass = bind.key.type.jvmType as? Class<*>?
            if (bindClass != null && KodeinController::class.java.isAssignableFrom(bindClass)) {
                val res by kodein.Instance(bind.key.type)
                println("Registering '$res' routes...")
                (res as KodeinController).apply { registerRoutes() }
            }
        }
    }
}