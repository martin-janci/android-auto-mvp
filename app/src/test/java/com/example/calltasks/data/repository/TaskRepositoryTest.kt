package com.example.calltasks.data.repository

import com.example.calltasks.data.local.TaskDao
import com.example.calltasks.data.local.TaskEntity
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Unit tests for TaskRepository.
 */
class TaskRepositoryTest {

    private lateinit var taskDao: TaskDao
    private lateinit var repository: TaskRepository

    private val testTask = TaskEntity(
        id = 1,
        name = "John Doe",
        phone = "+1234567890",
        description = "Follow up on proposal",
        notes = "Important client",
        priority = 1,
        isCompleted = false
    )

    @Before
    fun setup() {
        taskDao = mock()
        repository = TaskRepository(taskDao)
    }

    @Test
    fun `getAllTasks returns flow from dao`() = runTest {
        val tasks = listOf(testTask)
        whenever(taskDao.getAllTasks()).thenReturn(flowOf(tasks))

        val result = repository.getAllTasks().first()

        assertEquals(tasks, result)
    }

    @Test
    fun `getPendingTasks returns only non-completed tasks`() = runTest {
        val pendingTask = testTask.copy(isCompleted = false)
        whenever(taskDao.getPendingTasks()).thenReturn(flowOf(listOf(pendingTask)))

        val result = repository.getPendingTasks().first()

        assertEquals(1, result.size)
        assertEquals(false, result[0].isCompleted)
    }

    @Test
    fun `getTaskById returns task when exists`() = runTest {
        whenever(taskDao.getTaskById(1L)).thenReturn(testTask)

        val result = repository.getTaskById(1L)

        assertEquals(testTask, result)
    }

    @Test
    fun `getTaskById returns null when not found`() = runTest {
        whenever(taskDao.getTaskById(999L)).thenReturn(null)

        val result = repository.getTaskById(999L)

        assertNull(result)
    }

    @Test
    fun `insertTask calls dao insert`() = runTest {
        whenever(taskDao.insertTask(testTask)).thenReturn(1L)

        val result = repository.insertTask(testTask)

        assertEquals(1L, result)
        verify(taskDao).insertTask(testTask)
    }

    @Test
    fun `markComplete calls dao markComplete`() = runTest {
        repository.markComplete(1L)

        verify(taskDao).markComplete(1L)
    }

    @Test
    fun `updatePriority calls dao updatePriority`() = runTest {
        repository.updatePriority(1L, 5)

        verify(taskDao).updatePriority(1L, 5)
    }
}
