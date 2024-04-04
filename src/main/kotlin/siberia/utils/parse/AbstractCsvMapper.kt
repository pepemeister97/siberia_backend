package siberia.utils.parse

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import io.ktor.server.plugins.*
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

abstract class AbstractCsvMapper <T: Any> {
    abstract val required: Map<String, Boolean>
    abstract val klass: KClass<T>


    private val reader = csvReader {
        delimiter = ';'
    }

    protected fun parseCSV(csv : ByteArray) : List<T?> {
        val rows : List<Map<String, String>>

        try {
            rows = reader.readAllWithHeader(csv.decodeToString())
        } catch (e : Exception){
            throw BadRequestException("Bad CSV File")
        }

        return mapping(rows)
    }
    private fun mapping(rows : List<Map<String, String>>) : MutableList<T?> {

        val returnList = mutableListOf<T?>()

        rows.forEach { row ->
            val checked = required.toMutableMap()

            val instance = try {
                createEmptyInstanceTemplate(klass)
            } catch (_: Exception) {
                returnList.add(null)
                return@forEach
            }

            val map = mutableMapOf<String, Pair<Any?, String>>()
            klass.primaryConstructor?.parameters?.forEach {
                map[it.name!!] = Pair(null, it.type.toString())
            }

            row.forEach {
                if (map.containsKey(it.key)) {
                    val type = map[it.key]!!.second
                    map[it.key] = Pair(it.value as Any?, type)
                    checked.remove(it.key)
                }
            }

            if (checked.isNotEmpty()) {
                returnList.add(null)
                return@forEach
            }

            klass.primaryConstructor?.parameters?.forEach { param ->
                val prop = klass.memberProperties.first { it.name == param.name }
                val currentValue = map[param.name]!!.first
                if (prop is KMutableProperty<*>) {
                    try {
                        prop.setter.call(instance, currentValue)
                    }catch (e : Exception) {
                        when (map[param.name]!!.second) {
                            "kotlin.Int?" -> {
                                prop.setter.call(instance, currentValue.toString().toIntOrNull())
                            }
                            "kotlin.Double?" -> {
                                prop.setter.call(instance, currentValue.toString().toDoubleOrNull())
                            }
                            "kotlin.Long?" -> {
                                prop.setter.call(instance, currentValue.toString().toLongOrNull())
                            }
                            "kotlin.Float?" -> {
                                prop.setter.call(instance, currentValue.toString().toFloatOrNull())
                            }
                            "kotlin.Boolean?" -> {
                                prop.setter.call(instance, currentValue.toString().toBoolean())
                            }
                            else -> throw BadRequestException("Unsupported MediaType in CSV File")
                        }
                    }
                }
            }

            returnList.add(instance)
        }
        return returnList
    }

    private fun createEmptyInstanceTemplate(instanceKlass: KClass<T>): T {
        val args = List(instanceKlass.primaryConstructor?.parameters?.size ?: 0) { null }
        return instanceKlass.primaryConstructor?.call(*args.toTypedArray()) ?: throw Exception("Failed build empty instance")
    }
}