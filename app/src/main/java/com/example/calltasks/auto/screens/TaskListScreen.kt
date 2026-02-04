package com.example.calltasks.auto.screens

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template

/**
 * Android Auto screen showing the list of prioritized tasks.
 * Uses ListTemplate with max 6 items per Android Auto guidelines.
 *
 * Full implementation will be added in Epic 4.
 */
class TaskListScreen(carContext: CarContext) : Screen(carContext) {

    override fun onGetTemplate(): Template {
        val itemListBuilder = ItemList.Builder()

        // Placeholder - will load tasks from repository in Epic 4
        itemListBuilder.addItem(
            Row.Builder()
                .setTitle("No tasks loaded")
                .addText("Import tasks in the mobile app")
                .build()
        )

        return ListTemplate.Builder()
            .setTitle("Call Tasks")
            .setHeaderAction(Action.APP_ICON)
            .setSingleList(itemListBuilder.build())
            .build()
    }
}
