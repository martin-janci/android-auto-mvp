package com.example.calltasks.data.csv

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.calltasks.data.local.TaskEntity
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

/**
 * Imports tasks from CSV files.
 *
 * Expected CSV format:
 * name,phone,description,notes
 *
 * - First row is treated as header (skipped)
 * - name, phone, description are required
 * - notes is optional
 * - Whitespace is trimmed from all fields
 * - Malformed rows are skipped with logging
 */
class CsvImporter(private val context: Context) {

    companion object {
        private const val TAG = "CsvImporter"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_PHONE = "phone"
        private const val COLUMN_DESCRIPTION = "description"
        private const val COLUMN_NOTES = "notes"
    }

    /**
     * Result of CSV import operation.
     */
    sealed class ImportResult {
        data class Success(val tasks: List<TaskEntity>, val skippedRows: Int) : ImportResult()
        data class Error(val message: String, val exception: Throwable? = null) : ImportResult()
    }

    /**
     * Import tasks from a CSV file URI.
     *
     * @param uri The content URI of the CSV file
     * @return ImportResult with either parsed tasks or an error
     */
    suspend fun importFromUri(uri: Uri): ImportResult = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext ImportResult.Error("Could not open file")

            inputStream.use { stream ->
                parseStream(stream)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to import CSV", e)
            ImportResult.Error("Failed to import: ${e.message}", e)
        }
    }

    /**
     * Import tasks from an input stream (useful for testing).
     */
    suspend fun importFromStream(inputStream: InputStream): ImportResult = withContext(Dispatchers.IO) {
        try {
            parseStream(inputStream)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse CSV stream", e)
            ImportResult.Error("Failed to parse: ${e.message}", e)
        }
    }

    private fun parseStream(inputStream: InputStream): ImportResult {
        val tasks = mutableListOf<TaskEntity>()
        var skippedRows = 0
        var hasHeader = false
        var columnIndices: ColumnIndices? = null

        try {
            csvReader().open(inputStream) {
                readAllAsSequence().forEachIndexed { index, row ->
                    if (index == 0) {
                        // Try to detect header row
                        columnIndices = detectColumnIndices(row)
                        if (columnIndices != null) {
                            hasHeader = true
                            return@forEachIndexed // Skip header row
                        }
                        // No header detected, use default column order
                        columnIndices = ColumnIndices(0, 1, 2, 3)
                    }

                    val indices = columnIndices ?: ColumnIndices(0, 1, 2, 3)
                    val task = parseRow(row, indices)
                    if (task != null) {
                        tasks.add(task)
                    } else {
                        skippedRows++
                        Log.w(TAG, "Skipped malformed row $index: $row")
                    }
                }
            }
        } catch (e: Exception) {
            return ImportResult.Error("CSV parsing error: ${e.message}", e)
        }

        if (tasks.isEmpty() && skippedRows == 0) {
            // Empty file
            return ImportResult.Success(emptyList(), 0)
        }

        return ImportResult.Success(tasks, skippedRows)
    }

    /**
     * Detect column indices from header row.
     * Returns null if the row doesn't look like a header.
     */
    private fun detectColumnIndices(row: List<String>): ColumnIndices? {
        val lowerRow = row.map { it.trim().lowercase() }

        val nameIndex = lowerRow.indexOfFirst { it == COLUMN_NAME || it == "contact" || it == "person" }
        val phoneIndex = lowerRow.indexOfFirst { it == COLUMN_PHONE || it == "tel" || it == "telephone" || it == "number" }
        val descIndex = lowerRow.indexOfFirst { it == COLUMN_DESCRIPTION || it == "desc" || it == "task" || it == "reason" }
        val notesIndex = lowerRow.indexOfFirst { it == COLUMN_NOTES || it == "note" || it == "comment" || it == "comments" }

        // If we found at least name and phone, consider it a header
        if (nameIndex >= 0 && phoneIndex >= 0) {
            return ColumnIndices(
                name = nameIndex,
                phone = phoneIndex,
                description = if (descIndex >= 0) descIndex else 2,
                notes = if (notesIndex >= 0) notesIndex else -1
            )
        }

        return null
    }

    /**
     * Parse a single CSV row into a TaskEntity.
     * Returns null if the row is malformed or missing required fields.
     */
    private fun parseRow(row: List<String>, indices: ColumnIndices): TaskEntity? {
        if (row.size < 3) return null

        val name = row.getOrNull(indices.name)?.trim()
        val phone = row.getOrNull(indices.phone)?.trim()
        val description = row.getOrNull(indices.description)?.trim()
        val notes = if (indices.notes >= 0) row.getOrNull(indices.notes)?.trim() else null

        // Name and phone are required
        if (name.isNullOrBlank() || phone.isNullOrBlank()) {
            return null
        }

        // Description defaults to empty string if not provided
        val desc = if (description.isNullOrBlank()) "No description" else description

        return TaskEntity(
            name = name,
            phone = phone,
            description = desc,
            notes = notes?.takeIf { it.isNotBlank() },
            priority = 0, // Will be set by prioritizer
            isCompleted = false
        )
    }

    /**
     * Indices of columns in the CSV.
     */
    private data class ColumnIndices(
        val name: Int,
        val phone: Int,
        val description: Int,
        val notes: Int
    )
}
