package com.example.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

sealed class SyncResult<out T> {
    data class Success<T>(val data: T) : SyncResult<T>()
    data class Error(val message: String) : SyncResult<Nothing>()
}

/**
 * Direct HTTP bridge to the user's Google Apps Script Web App (`doGet` & `doPost`).
 * Follows Google Apps Script's 302 redirect to `script.googleusercontent.com/macros/echo`.
 */
class AppsScriptSyncService {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(35, TimeUnit.SECONDS)
        .writeTimeout(35, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    suspend fun importFromDrive(scriptUrl: String): SyncResult<DriveLedgerPayload> =
        withContext(Dispatchers.IO) {
            val cleanUrl = scriptUrl.trim()
            if (!isValidAppsScriptUrl(cleanUrl)) {
                return@withContext SyncResult.Error(
                    "Invalid URL. Use your deployed https://script.google.com/macros/s/.../exec URL.",
                )
            }

            try {
                val request = Request.Builder()
                    .url(cleanUrl)
                    .get()
                    .header("Accept", "application/json, text/plain")
                    .build()

                client.newCall(request).execute().use { response ->
                    val body = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        return@withContext SyncResult.Error(
                            "Google Drive request failed (HTTP ${response.code}).",
                        )
                    }
                    if (looksLikeHtmlAuthPage(body)) {
                        return@withContext SyncResult.Error(
                            "Google returned a sign-in page instead of JSON. In Apps Script Deploy settings, set 'Who has access' to 'Anyone'.",
                        )
                    }
                    val payload = DriveLedgerJsonCodec.decode(body)
                    SyncResult.Success(payload)
                }
            } catch (e: IOException) {
                SyncResult.Error("Network error: ${e.localizedMessage ?: "Check your internet connection"}")
            } catch (e: Exception) {
                SyncResult.Error("Failed to parse Drive JSON: ${e.localizedMessage ?: "Unexpected format"}")
            }
        }

    suspend fun saveToDrive(
        scriptUrl: String,
        payload: DriveLedgerPayload,
    ): SyncResult<Unit> = withContext(Dispatchers.IO) {
        val cleanUrl = scriptUrl.trim()
        if (!isValidAppsScriptUrl(cleanUrl)) {
            return@withContext SyncResult.Error(
                "Invalid URL. Use your deployed https://script.google.com/macros/s/.../exec URL.",
            )
        }

        try {
            val jsonString = DriveLedgerJsonCodec.encode(payload)
            // text/plain avoids CORS/content-type quirks while Apps Script reads e.postData.contents verbatim
            val requestBody = jsonString.toRequestBody("text/plain; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url(cleanUrl)
                .post(requestBody)
                .header("Accept", "application/json, text/plain")
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@withContext SyncResult.Error(
                        "Failed to save to Drive (HTTP ${response.code}).",
                    )
                }
                if (looksLikeHtmlAuthPage(body)) {
                    return@withContext SyncResult.Error(
                        "Access denied by Google Apps Script. Redeploy with 'Execute as: Me' and 'Who has access: Anyone'.",
                    )
                }
                // Verify expected {"status": "success"} when JSON is returned
                val trimmed = body.trim()
                if (trimmed.startsWith("{")) {
                    val obj = runCatching { JSONObject(trimmed) }.getOrNull()
                    val status = obj?.optString("status").orEmpty()
                    if (status.isNotEmpty() && !status.equals("success", ignoreCase = true)) {
                        return@withContext SyncResult.Error("Apps Script returned status: $status")
                    }
                }
                SyncResult.Success(Unit)
            }
        } catch (e: IOException) {
            SyncResult.Error("Network error: ${e.localizedMessage ?: "Check your internet connection"}")
        } catch (e: Exception) {
            SyncResult.Error("Sync failed: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    private fun looksLikeHtmlAuthPage(body: String): Boolean {
        val sample = body.trimStart().take(120).lowercase()
        return sample.startsWith("<!doctype html") ||
            sample.startsWith("<html") ||
            sample.contains("<title>google drive") ||
            sample.contains("accounts.google.com")
    }

    companion object {
        fun isValidAppsScriptUrl(url: String): Boolean {
            val trimmed = url.trim()
            return trimmed.startsWith("https://script.google.com/") && trimmed.length > 30
        }
    }
}
