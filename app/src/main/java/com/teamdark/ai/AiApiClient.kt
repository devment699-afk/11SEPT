package com.teamdark.ai

import android.content.Context
import android.os.Handler
import android.os.Looper
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Simple multi-AI HTTP client — no extra dependency.
 * OpenAI-compatible providers + Gemini supported. Baaki providers OpenAI-compatible endpoint use karte hain.
 */
object AiApiClient {

    interface Cb {
        fun onOk(text: String)
        fun onErr(msg: String)
    }

    fun chat(ctx: Context, providerId: String, userMsg: String, cb: Cb) {
        val key = ApiKeyManager.getKey(ctx, providerId)
        if (key.isEmpty()) {
            cb.onErr("NO_KEY")
            return
        }
        Thread {
            try {
                val out = when (providerId) {
                    "gemini" -> callGemini(key, userMsg)
                    "anthropic" -> callAnthropic(key, userMsg)
                    "groq" -> callOpenAiCompat("https://api.groq.com/openai/v1/chat/completions", key, pickModel(providerId), userMsg)
                    "openrouter" -> callOpenAiCompat("https://openrouter.ai/api/v1/chat/completions", key, "openrouter/auto", userMsg)
                    "deepseek" -> callOpenAiCompat("https://api.deepseek.com/chat/completions", key, "deepseek-chat", userMsg)
                    "mistral" -> callOpenAiCompat("https://api.mistral.ai/v1/chat/completions", key, "mistral-large-latest", userMsg)
                    "together" -> callOpenAiCompat("https://api.together.xyz/v1/chat/completions", key, "meta-llama/Meta-Llama-3.1-70B-Instruct-Turbo", userMsg)
                    "perplexity" -> callOpenAiCompat("https://api.perplexity.ai/chat/completions", key, "llama-3.1-sonar-small-128k-online", userMsg)
                    "cohere" -> callCohere(key, userMsg)
                    "custom1", "custom2" -> callCustom(key, userMsg)
                    else -> callOpenAiCompat("https://api.openai.com/v1/chat/completions", key, "gpt-4o-mini", userMsg)
                }
                post { cb.onOk(out) }
            } catch (e: Exception) {
                val m = (e.message ?: "network error").take(180)
                post { cb.onErr(m) }
            }
        }.start()
    }

    private fun post(r: () -> Unit) = Handler(Looper.getMainLooper()).post(r)

    private fun pickModel(p: String): String {
        return when (p) {
            "groq" -> "llama-3.3-70b-versatile"
            else -> "gpt-4o-mini"
        }
    }

    private fun callOpenAiCompat(endpoint: String, key: String, model: String, msg: String): String {
        val body = JSONObject()
        body.put("model", model)
        val arr = JSONArray()
        val sys = JSONObject(); sys.put("role", "system")
        sys.put("content", "You are TEAMDARK.AI, a helpful coding specialist for C++, Java, Python, Lua, APK and mod basics. Reply in Hinglish-friendly style, with code blocks.")
        val um = JSONObject(); um.put("role", "user"); um.put("content", msg)
        arr.put(sys); arr.put(um)
        body.put("messages", arr)
        body.put("temperature", 0.7)
        val res = postJson(endpoint, mapOf("Authorization" to "Bearer $key"), body.toString())
        val j = JSONObject(res)
        return j.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content").trim()
    }

    private fun callGemini(key: String, msg: String): String {
        val ep = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$key"
        val body = JSONObject()
        val parts = JSONArray(); val p = JSONObject(); p.put("text", msg); parts.put(p)
        val content = JSONObject(); content.put("parts", parts)
        body.put("contents", JSONArray().put(content))
        val res = postJson(ep, emptyMap(), body.toString())
        val j = JSONObject(res)
        return j.getJSONArray("candidates").getJSONObject(0)
            .getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text").trim()
    }

    private fun callAnthropic(key: String, msg: String): String {
        val ep = "https://api.anthropic.com/v1/messages"
        val body = JSONObject()
        body.put("model", "claude-3-5-sonnet-20241022")
        body.put("max_tokens", 1500)
        val arr = JSONArray()
        val um = JSONObject(); um.put("role", "user"); um.put("content", msg)
        arr.put(um)
        body.put("messages", arr)
        val res = postJson(ep, mapOf("x-api-key" to key, "anthropic-version" to "2023-06-01"), body.toString())
        val j = JSONObject(res)
        return j.getJSONArray("content").getJSONObject(0).getString("text").trim()
    }

    private fun callCohere(key: String, msg: String): String {
        val ep = "https://api.cohere.com/v1/chat"
        val body = JSONObject()
        body.put("model", "command-r-plus")
        body.put("message", msg)
        val res = postJson(ep, mapOf("Authorization" to "Bearer $key"), body.toString())
        return JSONObject(res).optString("text", "Empty response").trim()
    }

    private fun callCustom(keyRaw: String, msg: String): String {
        // format: baseURL|key  e.g. https://xxx/v1|sk-xxx
        val parts = keyRaw.split("|")
        if (parts.size < 2) throw Exception("Custom key format galat — baseURL|key likho")
        val base = parts[0].trim().trimEnd('/')
        val k = parts[1].trim()
        return callOpenAiCompat("$base/chat/completions", k, "auto", msg)
    }

    private fun postJson(urlS: String, headers: Map<String, String>, json: String): String {
        val url = URL(urlS)
        val c = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 30000
            readTimeout = 60000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            for ((k, v) in headers) setRequestProperty(k, v)
        }
        OutputStreamWriter(c.outputStream).use { it.write(json) }
        val code = c.responseCode
        val stream = if (code in 200..299) c.inputStream else c.errorStream
        val txt = BufferedReader(InputStreamReader(stream)).use { it.readText() }
        if (code !in 200..299) throw Exception("HTTP $code: ${txt.take(160)}")
        return txt
    }
}
