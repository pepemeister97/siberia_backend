package siberia.modules.category.data.dto.systemevents

import siberia.conf.AppConf

data class CategoryUpdateEvent(
    override val author: String, val updatedCategoryName: String
) : CategoryEvent() {
    override val eventType: Int
        get() = AppConf.eventTypes.updateEvent
    override val eventDescription: String
        get() = "Category $updatedCategoryName was updated."
    override val eventObjectName: String
        get() = updatedCategoryName
}