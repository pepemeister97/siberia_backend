package siberia.utils.parse

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import io.ktor.server.plugins.*
import siberia.plugins.Logger
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

object CSVParser {

    private val reader = csvReader {
        delimiter = ';'
    }

    fun parseCSV(csv : ByteArray, dto : Any) : List<Any> {
        val rows : List<Map<String, String>>

        try {
            rows = reader.readAllWithHeader(csv.decodeToString())
        } catch (e : Exception){
            throw BadRequestException("Bad CSV File")
        }

        return mapping(rows, dto)
    }
    private fun mapping(rows : List<Map<String, String>>, dto : Any) : List<Any> {

        val returnList = mutableListOf<Any>()

        rows.forEach { row ->
            @Suppress("UNCHECKED_CAST")
            val instance = createEmptyInstanceTemplate(dto as KClass<Any>)

            val map = mutableMapOf<String, Pair<Any?, String>>()
            dto.primaryConstructor?.parameters?.forEach {
                map[it.name!!] = Pair(null, it.type.toString())
            }
            Logger.debug(map, "main")
            row.forEach {
                if (map.containsKey(it.key)) {
                    val type = map[it.key]!!.second
                    map[it.key] = Pair(it.value as Any?, type)
                }
            }
            dto.primaryConstructor?.parameters?.forEach { param ->
                val prop = dto.memberProperties.first { it.name == param.name }
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

    private inline fun <reified T: Any> createEmptyInstanceTemplate(instanceKlass: KClass<T>): T {
        val args = List(instanceKlass.primaryConstructor?.parameters?.size ?: 0) { null }
        return instanceKlass.primaryConstructor?.call(*args.toTypedArray()) ?: throw Exception("Failed build empty instance")
    }
}