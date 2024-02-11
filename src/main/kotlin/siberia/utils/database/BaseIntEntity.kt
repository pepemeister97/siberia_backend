package siberia.utils.database

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.id.EntityID
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

typealias SerializableAny = @Serializable Any

abstract class BaseIntEntity<OutputDto : SerializableAny>(id: EntityID<Int>, table: BaseIntIdTable): IntEntity(id) {
    val createdAt by table.createdAt
    var updatedAt by table.updatedAt

    abstract fun toOutputDto(): OutputDto
    protected val json = Json { ignoreUnknownKeys = true }
    protected inline fun <reified T: Any> createRollbackInstanceTemplate(instanceKlass: KClass<T>): T {
        val args = List(instanceKlass.primaryConstructor?.parameters?.size ?: 0) { null }
        return instanceKlass.primaryConstructor?.call(*args.toTypedArray()) ?: throw Exception("Failed build rollback instance")
    }

    @OptIn(InternalSerializationApi::class)
    protected inline fun <reified Output : OutputDto, reified Update : SerializableAny> createRollbackUpdateDto(onUpdate: Update, output: OutputDto = toOutputDto()): String {
        val updateDtoKlass = Update::class
        val outputDtoKlass = Output::class
        val rollbackInstance = createRollbackInstanceTemplate(updateDtoKlass)
        updateDtoKlass.primaryConstructor?.parameters?.forEach { param ->
            val prop = updateDtoKlass.memberProperties.first { it.name == param.name }
            val currentProp = try { outputDtoKlass.memberProperties.first { it.name == param.name } } catch (_: Exception) { null }
                ?: return@forEach
            val currentValue = currentProp.call(output)
            val onUpdateValue = prop.call(rollbackInstance)
            val defaultValue = prop.call(onUpdate)
            if (onUpdateValue != defaultValue) {
                if (prop is KMutableProperty<*>)
                    prop.setter.call(rollbackInstance, currentValue)
            }
        }

        return json.encodeToString(updateDtoKlass.serializer(), rollbackInstance)
    }

    @OptIn(InternalSerializationApi::class)
    protected inline fun <reified Output : OutputDto> createRollbackRemoveDto(): String {
        val outputDtoKlass = Output::class
        val outputDto = toOutputDto()

        return json.encodeToString(outputDtoKlass.serializer(), outputDto as Output)
    }

    @OptIn(InternalSerializationApi::class)
    protected inline fun <reified Output : SerializableAny> createRollbackRemoveDto(dto: Output): String {
        val outputDtoKlass = Output::class

        return json.encodeToString(outputDtoKlass.serializer(), dto)
    }

}

val BaseIntEntity<*>.idValue: Int
    get() = this.id.value