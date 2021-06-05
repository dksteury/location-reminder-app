package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var reminders : MutableList<ReminderDTO>? = mutableListOf()) : ReminderDataSource {

    private var shouldReturnError = false

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) {
            return Result.Error("Reminders error")
        }
        reminders?.let { return Result.Success(ArrayList(it)) }
        return Result.Error("Reminders null")
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (shouldReturnError) {
            return Result.Error ("Reminder error")
        }
        reminders?.forEach {
            return when (id) {
                it.id -> Result.Success(it)
                else -> Result.Error("Reminder not found")
            }
        }
        return Result.Error("Reminder null")
    }

    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }
}