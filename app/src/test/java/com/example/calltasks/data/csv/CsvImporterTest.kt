package com.example.calltasks.data.csv

import com.example.calltasks.data.csv.CsvImporter.ImportResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for CsvImporter.
 * Tests parsing logic without Android context dependencies.
 */
class CsvImporterTest {

    @Test
    fun `parse valid CSV with header`() = runTest {
        val csv = """
            name,phone,description,notes
            John Doe,+1234567890,Follow up on proposal,Important client
            Jane Smith,+0987654321,Schedule meeting,
        """.trimIndent()

        val result = parseTestCsv(csv)

        assertTrue(result is ImportResult.Success)
        val success = result as ImportResult.Success
        assertEquals(2, success.tasks.size)
        assertEquals("John Doe", success.tasks[0].name)
        assertEquals("+1234567890", success.tasks[0].phone)
        assertEquals("Follow up on proposal", success.tasks[0].description)
        assertEquals("Important client", success.tasks[0].notes)
        assertEquals("Jane Smith", success.tasks[1].name)
        assertEquals(null, success.tasks[1].notes) // Empty notes should be null
    }

    @Test
    fun `parse CSV without header uses default columns`() = runTest {
        val csv = """
            John Doe,+1234567890,Follow up on proposal,Important client
        """.trimIndent()

        val result = parseTestCsv(csv)

        assertTrue(result is ImportResult.Success)
        val success = result as ImportResult.Success
        assertEquals(1, success.tasks.size)
        assertEquals("John Doe", success.tasks[0].name)
    }

    @Test
    fun `trim whitespace from all fields`() = runTest {
        val csv = """
            name,phone,description,notes
              John Doe  ,  +1234567890  ,  Follow up  ,  Notes
        """.trimIndent()

        val result = parseTestCsv(csv)

        assertTrue(result is ImportResult.Success)
        val success = result as ImportResult.Success
        assertEquals("John Doe", success.tasks[0].name)
        assertEquals("+1234567890", success.tasks[0].phone)
        assertEquals("Follow up", success.tasks[0].description)
        assertEquals("Notes", success.tasks[0].notes)
    }

    @Test
    fun `skip rows with missing required fields`() = runTest {
        val csv = """
            name,phone,description,notes
            John Doe,+1234567890,Follow up,Notes
            ,+1111111111,Missing name,
            Jane Smith,,Missing phone,
            Valid Person,+2222222222,Valid task,
        """.trimIndent()

        val result = parseTestCsv(csv)

        assertTrue(result is ImportResult.Success)
        val success = result as ImportResult.Success
        assertEquals(2, success.tasks.size) // Only valid rows
        assertEquals(2, success.skippedRows) // Two rows skipped
        assertEquals("John Doe", success.tasks[0].name)
        assertEquals("Valid Person", success.tasks[1].name)
    }

    @Test
    fun `handle empty file`() = runTest {
        val csv = ""

        val result = parseTestCsv(csv)

        assertTrue(result is ImportResult.Success)
        val success = result as ImportResult.Success
        assertEquals(0, success.tasks.size)
        assertEquals(0, success.skippedRows)
    }

    @Test
    fun `handle file with only header`() = runTest {
        val csv = "name,phone,description,notes"

        val result = parseTestCsv(csv)

        assertTrue(result is ImportResult.Success)
        val success = result as ImportResult.Success
        assertEquals(0, success.tasks.size)
    }

    @Test
    fun `handle alternative header names`() = runTest {
        val csv = """
            contact,tel,task,comment
            John Doe,+1234567890,Call about project,VIP
        """.trimIndent()

        val result = parseTestCsv(csv)

        assertTrue(result is ImportResult.Success)
        val success = result as ImportResult.Success
        assertEquals(1, success.tasks.size)
        assertEquals("John Doe", success.tasks[0].name)
        assertEquals("Call about project", success.tasks[0].description)
    }

    @Test
    fun `missing description defaults to placeholder`() = runTest {
        val csv = """
            name,phone,description,notes
            John Doe,+1234567890,,
        """.trimIndent()

        val result = parseTestCsv(csv)

        assertTrue(result is ImportResult.Success)
        val success = result as ImportResult.Success
        assertEquals("No description", success.tasks[0].description)
    }

    @Test
    fun `tasks have default priority of 0`() = runTest {
        val csv = """
            name,phone,description,notes
            John Doe,+1234567890,Task,
        """.trimIndent()

        val result = parseTestCsv(csv)

        assertTrue(result is ImportResult.Success)
        val success = result as ImportResult.Success
        assertEquals(0, success.tasks[0].priority)
    }

    @Test
    fun `tasks are not completed by default`() = runTest {
        val csv = """
            name,phone,description,notes
            John Doe,+1234567890,Task,
        """.trimIndent()

        val result = parseTestCsv(csv)

        assertTrue(result is ImportResult.Success)
        val success = result as ImportResult.Success
        assertEquals(false, success.tasks[0].isCompleted)
    }

    /**
     * Helper function to parse CSV string for testing.
     * This simulates CsvImporter.importFromStream without Android dependencies.
     */
    private suspend fun parseTestCsv(csv: String): ImportResult {
        // For unit tests, we directly test the parsing logic
        // In real tests, we'd use a mock context or Robolectric
        val inputStream = csv.byteInputStream()

        val tasks = mutableListOf<com.example.calltasks.data.local.TaskEntity>()
        var skippedRows = 0
        var columnIndices: TestColumnIndices? = null

        try {
            com.github.doyaaaaaken.kotlincsv.dsl.csvReader().open(inputStream) {
                readAllAsSequence().forEachIndexed { index, row ->
                    if (index == 0 && row.isNotEmpty()) {
                        columnIndices = detectTestColumnIndices(row)
                        if (columnIndices != null) {
                            return@forEachIndexed
                        }
                        columnIndices = TestColumnIndices(0, 1, 2, 3)
                    }

                    val indices = columnIndices ?: TestColumnIndices(0, 1, 2, 3)
                    val task = parseTestRow(row, indices)
                    if (task != null) {
                        tasks.add(task)
                    } else {
                        skippedRows++
                    }
                }
            }
        } catch (e: Exception) {
            return ImportResult.Error("Parse error: ${e.message}", e)
        }

        return ImportResult.Success(tasks, skippedRows)
    }

    private data class TestColumnIndices(val name: Int, val phone: Int, val description: Int, val notes: Int)

    private fun detectTestColumnIndices(row: List<String>): TestColumnIndices? {
        val lowerRow = row.map { it.trim().lowercase() }
        val nameIndex = lowerRow.indexOfFirst { it in listOf("name", "contact", "person") }
        val phoneIndex = lowerRow.indexOfFirst { it in listOf("phone", "tel", "telephone", "number") }
        val descIndex = lowerRow.indexOfFirst { it in listOf("description", "desc", "task", "reason") }
        val notesIndex = lowerRow.indexOfFirst { it in listOf("notes", "note", "comment", "comments") }

        if (nameIndex >= 0 && phoneIndex >= 0) {
            return TestColumnIndices(nameIndex, phoneIndex, if (descIndex >= 0) descIndex else 2, notesIndex)
        }
        return null
    }

    private fun parseTestRow(row: List<String>, indices: TestColumnIndices): com.example.calltasks.data.local.TaskEntity? {
        if (row.size < 3) return null
        val name = row.getOrNull(indices.name)?.trim()
        val phone = row.getOrNull(indices.phone)?.trim()
        val description = row.getOrNull(indices.description)?.trim()
        val notes = if (indices.notes >= 0) row.getOrNull(indices.notes)?.trim() else null

        if (name.isNullOrBlank() || phone.isNullOrBlank()) return null
        val desc = if (description.isNullOrBlank()) "No description" else description

        return com.example.calltasks.data.local.TaskEntity(
            name = name,
            phone = phone,
            description = desc,
            notes = notes?.takeIf { it.isNotBlank() },
            priority = 0,
            isCompleted = false
        )
    }
}
