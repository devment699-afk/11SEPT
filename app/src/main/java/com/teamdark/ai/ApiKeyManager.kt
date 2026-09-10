package com.teamdark.ai

import android.content.Context

/**
 * 12 API slots — alag alag AI providers.
 * Keys sirf device me save hoti hain (SharedPrefs), kahin upload nahi hoti.
 */
object ApiKeyManager {

    data class Provider(val id: String, val label: String, val hint: String)

    val PROVIDERS = listOf(
        Provider("openai", "1. OpenAI", "sk-..."),
        Provider("anthropic", "2. Anthropic Claude", "sk-ant-..."),
        Provider("gemini", "3. Google Gemini", "AIza..."),
        Provider("groq", "4. Groq", "gsk_..."),
        Provider("openrouter", "5. OpenRouter", "sk-or-..."),
        Provider("deepseek", "6. DeepSeek", "sk-..."),
        Provider("mistral", "7. Mistral", "..."),
        Provider("cohere", "8. Cohere", "..."),
        Provider("together", "9. Together AI", "..."),
        Provider("perplexity", "10. Perplexity", "pplx-..."),
        Provider("custom1", "11. Custom OpenAI-Compatible #1", "baseURL|key"),
        Provider("custom2", "12. Custom OpenAI-Compatible #2", "baseURL|key")
    )

    private const val PREF = "td_api_keys"
    private const val KEY_ACTIVE = "active_provider"

    fun getKey(ctx: Context, id: String): String {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString("k_$id", "") ?: ""
    }

    fun setKey(ctx: Context, id: String, v: String) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putString("k_$id", v.trim()).apply()
    }

    fun getActive(ctx: Context): String {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(KEY_ACTIVE, "openai") ?: "openai"
    }

    fun setActive(ctx: Context, id: String) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putString(KEY_ACTIVE, id).apply()
    }

    fun activeLabel(ctx: Context): String {
        val a = getActive(ctx)
        return PROVIDERS.find { it.id == a }?.label ?: a
    }

    fun hasAnyKey(ctx: Context): Boolean {
        return PROVIDERS.any { getKey(ctx, it.id).isNotEmpty() }
    }

    fun countSet(ctx: Context): Int {
        return PROVIDERS.count { getKey(ctx, it.id).isNotEmpty() }
    }
}
