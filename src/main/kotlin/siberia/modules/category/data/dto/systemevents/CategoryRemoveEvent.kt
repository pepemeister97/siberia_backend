package siberia.modules.category.data.dto.systemevents

import siberia.conf.AppConf

data class CategoryRemoveEvent(
    override val author: String, val removedCategoryName: String
) : CategoryEvent() {
    override val eventType: Int
        get() = AppConf.eventTypes.removeEvent
    override val eventDescription: String
        get() = "Category $removedCategoryName was removed."
    override val eventObjectName: String
        get() = removedCategoryName
}