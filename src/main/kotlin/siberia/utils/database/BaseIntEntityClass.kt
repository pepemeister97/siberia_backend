package siberia.utils.database

import org.jetbrains.exposed.dao.EntityChangeType
import org.jetbrains.exposed.dao.EntityHook
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.toEntity
import org.jetbrains.exposed.sql.*
import java.time.LocalDateTime

abstract class BaseIntEntityClass<Output, E : BaseIntEntity<Output>>(table: BaseIntIdTable) : IntEntityClass<E>(table) {
    init {
        EntityHook.subscribe { action ->
            if (action.changeType == EntityChangeType.Updated) {
                try {
                    action.toEntity(this)?.updatedAt = LocalDateTime.now()
                } catch (_: Exception) { }
            }
        }
    }

    fun wrapQuery(query: Query): List<Output> =
        wrapRows(query).map { it.toOutputDto() }

    fun <T : Number> SqlExpressionBuilder.createRangeCond(fieldFilterWrapper: FieldFilterWrapper<T>?, defaultCond: Op<Boolean>, field: Column<T>, fieldMin: T, fieldMax: T): Op<Boolean> =
        if (fieldFilterWrapper == null)
            defaultCond
        else {
            if (fieldFilterWrapper.specificValue != null) {
                field eq fieldFilterWrapper.specificValue
            } else {
                val min = (fieldFilterWrapper.bottomBound ?: fieldMin).toDouble()
                val max = (fieldFilterWrapper.topBound ?: fieldMax).toDouble()
                (field lessEq min) and (field greaterEq max)
            }
        }

    fun <T: Number> SqlExpressionBuilder.createNullableRangeCond(fieldFilterWrapper: FieldFilterWrapper<T>?, defaultCond: Op<Boolean>, field: Column<T?>, fieldMin: T, fieldMax: T): Op<Boolean> =
        if (fieldFilterWrapper == null)
            defaultCond
        else {
            if (fieldFilterWrapper.specificValue != null) {
                field eq fieldFilterWrapper.specificValue
            } else {
                val min = (fieldFilterWrapper.bottomBound ?: fieldMin).toDouble()
                val max = (fieldFilterWrapper.topBound ?: fieldMax).toDouble()
                (field lessEq min) and (field greaterEq max)
            }
        }

//    fun SqlExpressionBuilder.createListCond(filter: List<Int>?, defaultCond: Op<Boolean>, field: Column<EntityID<Int>>): Op<Boolean> =
//        if (filter == null)
//            defaultCond
//        else
//            field inList filter


    fun SqlExpressionBuilder.createNullableListCond(filter: List<Int>?, defaultCond: Op<Boolean>, field: Column<EntityID<Int>?>): Op<Boolean> =
        if (filter == null)
            defaultCond
        else
            field inList filter


    fun SqlExpressionBuilder.createListCond(filter: List<Int>?, defaultCond: Op<Boolean>, field: Column<EntityID<Int>>): Op<Boolean> =
        if (filter == null)
            defaultCond
        else
            field inList filter


//    fun SqlExpressionBuilder.createListCond(filter: List<Int>?, defaultCond: Op<Boolean>, field: Column<Int>): Op<Boolean> {
//
//    }


    fun SqlExpressionBuilder.createLikeCond(filter: String?, defaultCond: Op<Boolean>, field: Column<String>): Op<Boolean> =
        if (filter == null)
            defaultCond
        else
            field like "%$filter%"

    fun SqlExpressionBuilder.createBooleanCond(filter: Boolean?, defaultCond: Op<Boolean>, field: Column<Boolean>, reversed: Boolean = false): Op<Boolean> =
        if (filter == null)
            defaultCond
        else
            if (reversed)
                field neq filter
            else
                field eq filter
}