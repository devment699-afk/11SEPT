package com.teamdark.ai

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Local demo auth system.
 * NOTE: Ye on-device demo store hai — same phone pe register users + owner approvals dikhenge.
 * Real multi-phone sync (sab users ka data owner ke phone pe aana) ke liye Firebase/Supabase
 * backend chahiye. Uske liye yehi class backend se jodne layak banayi gayi hai.
 */
object UserManager {

    const val UPI_ID = "devilking8118-1@okaxis"
    const val TG_HANDLE = "@DARK_DEVEL0"
    const val TG_LINK = "https://t.me/DARK_DEVEL0"

    // Default owner — pehli baar login ke baad Owner Panel se password turant change kar lena.
    private const val DEF_OWNER_ID = "DARK_DEVEL"
    private const val DEF_OWNER_PASS = "TD@11SEPT"

    private const val PREF = "td_users"
    private const val K_USERS = "users_json"
    private const val K_SESSION = "session_email"
    private const val K_OWNER_PASS = "owner_pass"

    data class User(val name: String, val email: String, val pass: String, val premium: Boolean, val plan: String)
    data class Pending(val email: String, val name: String, val plan: String, val amount: String, val time: Long)

    private fun prefs(ctx: Context) = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)

    fun ownerId(): String = DEF_OWNER_ID

    fun ownerPass(ctx: Context): String {
        return prefs(ctx).getString(K_OWNER_PASS, DEF_OWNER_PASS) ?: DEF_OWNER_PASS
    }

    fun setOwnerPass(ctx: Context, v: String) {
        prefs(ctx).edit().putString(K_OWNER_PASS, v).apply()
    }

    fun checkOwner(ctx: Context, id: String, pass: String): Boolean {
        return id.trim() == DEF_OWNER_ID && pass == ownerPass(ctx)
    }

    private fun loadUsers(ctx: Context): MutableList<User> {
        val out = mutableListOf<User>()
        try {
            val s = prefs(ctx).getString(K_USERS, "[]") ?: "[]"
            val arr = JSONArray(s)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                out.add(User(
                    o.optString("name"), o.optString("email"),
                    o.optString("pass"), o.optBoolean("premium"),
                    o.optString("plan", "")
                ))
            }
        } catch (_: Exception) {}
        return out
    }

    private fun saveUsers(ctx: Context, list: List<User>) {
        val arr = JSONArray()
        for (u in list) {
            val o = JSONObject()
            o.put("name", u.name); o.put("email", u.email)
            o.put("pass", u.pass); o.put("premium", u.premium); o.put("plan", u.plan)
            arr.put(o)
        }
        prefs(ctx).edit().putString(K_USERS, arr.toString()).apply()
    }

    fun register(ctx: Context, name: String, email: String, pass: String): String {
        val e = email.trim().lowercase()
        if (name.trim().isEmpty()) return "Naam likho"
        if (!e.contains("@") || !e.contains(".")) return "Sahi Gmail likho"
        if (pass.length < 4) return "Password min 4 char"
        val list = loadUsers(ctx)
        if (list.any { it.email == e }) return "Ye Gmail already registered hai — Login karo"
        list.add(User(name.trim(), e, pass, false, ""))
        saveUsers(ctx, list)
        prefs(ctx).edit().putString(K_SESSION, e).apply()
        return "OK"
    }

    fun login(ctx: Context, email: String, pass: String): String {
        val e = email.trim().lowercase()
        val list = loadUsers(ctx)
        val u = list.find { it.email == e } ?: return "Account nahi mila — pehle Register karo"
        if (u.pass != pass) return "Wrong password"
        prefs(ctx).edit().putString(K_SESSION, e).apply()
        return "OK"
    }

    fun logout(ctx: Context) {
        prefs(ctx).edit().remove(K_SESSION).apply()
    }

    fun current(ctx: Context): User? {
        val e = prefs(ctx).getString(K_SESSION, null) ?: return null
        return loadUsers(ctx).find { it.email == e }
    }

    fun allUsers(ctx: Context): List<User> = loadUsers(ctx)

    fun userCount(ctx: Context): Int = loadUsers(ctx).size

    fun isPremium(ctx: Context): Boolean = current(ctx)?.premium == true

    // ---- premium requests (pending approvals) ----
    private fun loadPending(ctx: Context): MutableList<Pending> {
        val out = mutableListOf<Pending>()
        try {
            val s = prefs(ctx).getString("pending_json", "[]") ?: "[]"
            val arr = JSONArray(s)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                out.add(Pending(o.optString("email"), o.optString("name"), o.optString("plan"), o.optString("amount"), o.optLong("time")))
            }
        } catch (_: Exception) {}
        return out
    }

    private fun savePending(ctx: Context, list: List<Pending>) {
        val arr = JSONArray()
        for (p in list) {
            val o = JSONObject()
            o.put("email", p.email); o.put("name", p.name)
            o.put("plan", p.plan); o.put("amount", p.amount); o.put("time", p.time)
            arr.put(o)
        }
        prefs(ctx).edit().putString("pending_json", arr.toString()).apply()
    }

    fun pendingList(ctx: Context): List<Pending> = loadPending(ctx)

    fun addPending(ctx: Context, plan: String, amount: String): String {
        val u = current(ctx) ?: return "Pehle Login/Register karo"
        val list = loadPending(ctx).toMutableList()
        if (list.any { it.email == u.email }) return "Tumhari request already pending hai — owner approval ka wait karo"
        list.add(Pending(u.email, u.name, plan, amount, System.currentTimeMillis()))
        savePending(ctx, list)
        return "OK"
    }

    fun approve(ctx: Context, email: String, yes: Boolean): String {
        val list = loadPending(ctx).toMutableList()
        val p = list.find { it.email == email } ?: return "Request nahi mili"
        list.remove(p)
        savePending(ctx, list)
        if (yes) {
            val users = loadUsers(ctx).toMutableList()
            val idx = users.indexOfFirst { it.email == email }
            if (idx >= 0) {
                val u = users[idx]
                users[idx] = u.copy(premium = true, plan = p.plan)
                saveUsers(ctx, users)
            }
            return "Approved — ${p.email} ab PREMIUM hai"
        }
        return "Rejected — ${p.email}"
    }

    fun planAmount(plan: String): String {
        return when (plan) {
            "1 MONTH" -> "299"
            "3 MONTH" -> "499"
            "1 YEAR" -> "999"
            else -> ""
        }
    }
}
