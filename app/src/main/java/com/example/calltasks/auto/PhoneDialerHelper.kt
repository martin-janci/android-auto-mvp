package com.example.calltasks.auto

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.car.app.CarContext

/**
 * Helper for initiating phone calls from Android Auto.
 * Uses ACTION_DIAL intent which doesn't require CALL_PHONE permission.
 */
object PhoneDialerHelper {

    private const val TAG = "PhoneDialerHelper"

    /**
     * Result of dial attempt.
     */
    sealed class DialResult {
        data object Success : DialResult()
        data class Error(val message: String) : DialResult()
    }

    /**
     * Initiate a phone call using ACTION_DIAL.
     * This opens the phone dialer with the number pre-filled.
     * User must confirm the call, which is safer for driving.
     *
     * @param carContext The car context for starting the activity
     * @param phoneNumber The phone number to dial
     * @return DialResult indicating success or error
     */
    fun dial(carContext: CarContext, phoneNumber: String): DialResult {
        val cleanNumber = cleanPhoneNumber(phoneNumber)

        if (cleanNumber.isBlank()) {
            Log.w(TAG, "Invalid phone number: empty after cleaning")
            return DialResult.Error("Invalid phone number")
        }

        if (!isValidPhoneNumber(cleanNumber)) {
            Log.w(TAG, "Invalid phone number format")
            return DialResult.Error("Invalid phone number format")
        }

        return try {
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$cleanNumber")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            carContext.startCarApp(dialIntent)
            Log.i(TAG, "Opened dialer successfully")
            DialResult.Success
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open dialer", e)
            DialResult.Error("Could not open phone dialer")
        }
    }

    /**
     * Clean a phone number by removing common formatting characters.
     */
    private fun cleanPhoneNumber(number: String): String {
        return number
            .trim()
            .replace(" ", "")
            .replace("-", "")
            .replace("(", "")
            .replace(")", "")
            .replace(".", "")
    }

    /**
     * Basic validation that a string looks like a phone number.
     * Accepts formats like: +1234567890, 1234567890, etc.
     */
    private fun isValidPhoneNumber(number: String): Boolean {
        if (number.length < 3) return false // Too short

        // Allow + at start, then digits only
        val regex = Regex("^\\+?[0-9]{3,15}$")
        return regex.matches(number)
    }
}
