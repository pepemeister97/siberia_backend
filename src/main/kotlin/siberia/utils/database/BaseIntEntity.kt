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

@Serializable
class EMPTY

abstract class BaseIntEntity<OutputDto : SerializableAny>(id: EntityID<Int>, table: BaseIntIdTable): IntEntity(id) {
    val createdAt by table.createdAt
    var updatedAt by table.updatedAt

    abstract fun toOutputDto(): OutputDto
    val json = Json { ignoreUnknownKeys = true }

    companion object {
        inline fun <reified T: Any> createRollbackInstanceTemplate(instanceKlass: KClass<T>): T {
            val args = List(instanceKlass.primaryConstructor?.parameters?.size ?: 0) { null }
            return instanceKlass.primaryConstructor?.call(*args.toTypedArray()) ?: throw Exception("Failed build rollback instance")
        }
    }

//    inline fun <reified Demand : Any, reified Model: BaseIntIdTable> createSlice(demand: Demand, model: Model) {
//        val demandDtoKlass = Demand::class
//        val modelObjectKlass = Model::class
//        val slice = mutableListOf<Any>()
//        demandDtoKlass.primaryConstructor?.parameters?.forEach { arg ->
//            val field = demandDtoKlass.memberProperties.first { it.name == arg.name }
//            val fieldValue = field.call(demand)
//            if (fieldValue == true) {
//                val modelField = (modelObjectKlass.memberProperties.firstOrNull {
//                  it.name == arg.name
//                } ?: return@forEach).call(model) ?: return@forEach
//                slice.add(modelField)
//            }
//        }
//    }

    inline fun <reified Output : SerializableAny, reified Update : SerializableAny> initRollbackInstance(onUpdate: Update, output: SerializableAny = toOutputDto()): Update {
        val updateDtoKlass = Update::class
        val outputDtoKlass = Output::class
        val rollbackInstance = createRollbackInstanceTemplate(updateDtoKlass)
        updateDtoKlass.primaryConstructor?.parameters?.forEach { param ->
            val prop = updateDtoKlass.memberProperties.first { it.name == param.name }
            val currentProp = try { outputDtoKlass.memberProperties.first { it.name == param.name } } catch (_: Exception) { null }
                ?: return@forEach
            val currentValue = currentProp.call(output)
            val onUpdateValue = prop.call(onUpdate)
            val defaultValue = prop.call(rollbackInstance)
            if (onUpdateValue != currentValue && onUpdateValue != defaultValue) {
                if (prop is KMutableProperty<*>)
                    prop.setter.call(rollbackInstance, currentValue)
            }
        }

        return rollbackInstance
    }


    @Serializable
    data class EventInstance <T: SerializableAny, R: SerializableAny> (
        val rollbackInstance: T,
        val afterInstance: R
    )

    @OptIn(InternalSerializationApi::class)
    inline fun <reified Output : SerializableAny, reified Update : SerializableAny> createEncodedRollbackUpdateDto(onUpdate: Update, output: SerializableAny = toOutputDto()): String {
        val updateDtoKlass = Update::class
        val rollbackDto = initRollbackInstance<Output, Update>(onUpdate, output)

        val eventInstance = EventInstance(
            rollbackDto, onUpdate
        )

        return json.encodeToString(EventInstance.serializer(updateDtoKlass.serializer(), updateDtoKlass.serializer()), eventInstance)
    }

    inline fun <reified Output : OutputDto, reified Update : SerializableAny> getRollbackInstance(
        onUpdate: Update,
        output: OutputDto = toOutputDto()
    ): Update = initRollbackInstance<Output, Update>(onUpdate, output)

    @OptIn(InternalSerializationApi::class)
    protected inline fun <reified Output : OutputDto> createRollbackRemoveDto(): String {
        val outputDtoKlass = Output::class
        val outputDto = toOutputDto()

        return json.encodeToString(outputDtoKlass.serializer(), outputDto as Output)
    }

    @OptIn(InternalSerializationApi::class)
    protected inline fun <reified Output : SerializableAny> createRollbackRemoveDto(dto: Output): String {
        val outputDtoKlass = Output::class

        val eventInstance = EventInstance(
            dto, EMPTY()
        )

        return json.encodeToString(EventInstance.serializer(outputDtoKlass.serializer(), EMPTY.serializer()), eventInstance)
    }

}

val BaseIntEntity<*>.idValue: Int
    get() = this.id.value