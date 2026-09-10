package com.teamdark.ai

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var input: EditText
    private lateinit var adapter: ChatAdapter
    private val data = mutableListOf<ChatMessage>()
    private var pendingImage: Uri? = null
    private val ui = Handler(Looper.getMainLooper())

    private lateinit var previewBox: LinearLayout
    private lateinit var previewImg: ImageView
    private lateinit var drawer: DrawerLayout
    private lateinit var statusLine: TextView
    private lateinit var agentBadge: TextView
    private var agentOn = false

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            pendingImage = uri
            previewBox.visibility = android.view.View.VISIBLE
            try { previewImg.setImageURI(uri) } catch (_: Exception) {}
            Toast.makeText(this, "Photo attached!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recycler = findViewById(R.id.recycler)
        input = findViewById(R.id.input)
        previewBox = findViewById(R.id.previewBox)
        previewImg = findViewById(R.id.previewImg)
        drawer = findViewById(R.id.drawer)
        statusLine = findViewById(R.id.statusLine)
        agentBadge = findViewById(R.id.agentBadge)

        adapter = ChatAdapter(data)
        recycler.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        recycler.adapter = adapter

        setupChips()
        welcome()
        refreshStatus()

        findViewById<TextView>(R.id.btnMenu).setOnClickListener { drawer.openDrawer(GravityCompat.START) }
        findViewById<TextView>(R.id.btnSend).setOnClickListener { send() }
        findViewById<TextView>(R.id.btnGallery).setOnClickListener { pickImage.launch("image/*") }
        findViewById<TextView>(R.id.btnRemoveImg).setOnClickListener {
            pendingImage = null
            previewBox.visibility = android.view.View.GONE
        }
        findViewById<TextView>(R.id.btnClear).setOnClickListener {
            adapter.clear(recycler)
            pendingImage = null
            previewBox.visibility = android.view.View.GONE
            welcome()
        }
        agentBadge.setOnClickListener { toggleAgent() }

        val nav = findViewById<NavigationView>(R.id.navView)
        nav.setNavigationItemSelectedListener { item ->
            drawer.closeDrawer(GravityCompat.START)
            when (item.itemId) {
                R.id.m_login -> showAuth()
                R.id.m_api -> showApiKeys()
                R.id.m_agent -> toggleAgent()
                R.id.m_premium -> showPremium()
                R.id.m_owner -> showOwnerLogin()
                R.id.m_settings -> showSettings()
                R.id.m_logout -> {
                    UserManager.logout(this)
                    refreshStatus()
                    toast("Logged out")
                    adapter.add(ChatMessage(false, "🚪 Logout ho gaya. Login karke premium + history unlock karo."), recycler)
                }
            }
            true
        }
    }

    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show()

    private fun refreshStatus() {
        val u = UserManager.current(this)
        val prem = UserManager.isPremium(this)
        val keys = ApiKeyManager.countSet(this)
        val active = ApiKeyManager.activeLabel(this)
        val who = if (u != null) u.name + (if (prem) " 👑" else "") else "Guest"
        statusLine.text = if (keys > 0) "API: $active • $who" else "OFFLINE • $who"
        agentBadge.text = if (agentOn) "AGENT: ON" else "AGENT: OFF"
        agentBadge.setTextColor(if (agentOn) 0xFF00F5FF.toInt() else 0xFF94A3B8.toInt())
    }

    private fun setupChips() {
        val row = findViewById<LinearLayout>(R.id.chipRow)
        val chips = listOf("C++ Starter", "Java App", "Python Script", "Lua Mod", "APK Guide", "Agent Demo")
        val prompts = mapOf(
            "C++ Starter" to "c++ me starter code do",
            "Java App" to "java me android button banao",
            "Python Script" to "python me file organizer banao",
            "Lua Mod" to "lua me mod script do",
            "APK Guide" to "apk structure samjhao",
            "Agent Demo" to "agent mode me calculator app banao"
        )
        row.removeAllViews()
        for (c in chips) {
            val tv = TextView(this)
            tv.text = "✦ $c"
            tv.textSize = 12f
            tv.setTextColor(0xFF00F5FF.toInt())
            tv.setPadding(28, 16, 28, 16)
            tv.background = getDrawable(R.drawable.bg_input)
            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            lp.marginEnd = 16
            tv.layoutParams = lp
            tv.gravity = Gravity.CENTER
            tv.setOnClickListener {
                input.setText(prompts[c] ?: c)
                send()
            }
            row.addView(tv)
        }
    }

    private fun welcome() {
        val u = UserManager.current(this)
        val hi = if (u != null) "Hello " + u.name + "! " else ""
        adapter.add(ChatMessage(false,
            "👑 Welcome " + hi + "to TEAMDARK.AI v2\n\n" +
            "• Free: offline unlimited chat + copy + gallery\n" +
            "• API: ☰ > API Keys me 12 AI keys lagao (10+ providers)\n" +
            "• Agent Mode (Premium): khud multi-step me kaam karega\n" +
            "• Premium: 1M 299 / 3M 499 / 1Y 999 — UPI + TG screenshot\n\n" +
            "Bolo kya banau?"
        ), recycler)
    }

    // ---------- SEND ----------
    private fun send() {
        val text = input.text.toString().trim()
        val img = pendingImage
        if (text.isEmpty() && img == null) return
        if (agentOn) { agentSend(text, img); return }

        adapter.add(ChatMessage(true, if (text.isEmpty()) "Photo bheji hai" else text, img?.toString()), recycler)
        input.setText("")
        pendingImage = null
        previewBox.visibility = android.view.View.GONE

        val typing = ChatMessage(false, "✍ TD typing…")
        adapter.add(typing, recycler)

        if (ApiKeyManager.hasAnyKey(this)) {
            val prov = ApiKeyManager.getActive(this)
            val fullMsg = if (text.isEmpty()) "User ne photo bheji hai. Photo analyze nahi kar sakte, politely next step pucho." else text
            AiApiClient.chat(this, prov, fullMsg, object : AiApiClient.Cb {
                override fun onOk(t: String) {
                    removeTyping()
                    val label = ApiKeyManager.activeLabel(this@MainActivity)
                    adapter.add(ChatMessage(false, t + "\n\n— via " + label), recycler)
                }
                override fun onErr(m: String) {
                    removeTyping()
                    if (m == "NO_KEY") {
                        adapter.add(ChatMessage(false, TeamDarkAIEngine.reply(text, img != null)), recycler)
                    } else {
                        adapter.add(ChatMessage(false,
                            "⚠ API error (" + m.take(120) + ")\nOffline mode se jawab de raha hoon:\n\n" +
                            TeamDarkAIEngine.reply(text, img != null)), recycler)
                    }
                }
            })
        } else {
            ui.postDelayed({
                removeTyping()
                adapter.add(ChatMessage(false, TeamDarkAIEngine.reply(text, img != null)), recycler)
            }, 550)
        }
    }

    private fun removeTyping() {
        if (data.isNotEmpty() && data.last().text.startsWith("✍")) {
            data.removeAt(data.size - 1)
            adapter.notifyItemRemoved(data.size)
        }
    }

    private fun toggleAgent() {
        if (!UserManager.isPremium(this)) {
            adapter.add(ChatMessage(false,
                "🔒 Agent Mode PREMIUM hai.\nFree me offline chat unlimited hai, lekin jo khud plan + code + fix kare (Nexus jaisa agent) wo Premium me hai.\n\nPlans: 1M 299 / 3M 499 / 1Y 999.\n☰ > Premium / Pay kholo."), recycler)
            showPremium()
            return
        }
        agentOn = !agentOn
        refreshStatus()
        toast(if (agentOn) "Agent ON" else "Agent OFF")
        adapter.add(ChatMessage(false,
            if (agentOn) "🤖 Agent ON — ab task bolo, main steps me karunga: plan, code, review. Ex: agent mode me calculator app banao"
            else "Agent OFF — normal chat on."), recycler)
    }

    private fun agentSend(text: String, img: Uri?) {
        if (!UserManager.isPremium(this)) { toggleAgent(); return }
        val task = if (text.isEmpty()) "photo task" else text
        adapter.add(ChatMessage(true, "[AGENT TASK] $task", img?.toString()), recycler)
        input.setText("")
        pendingImage = null
        previewBox.visibility = android.view.View.GONE

        adapter.add(ChatMessage(false, "🤖 Agent start...\nStep 1/3: Planning..."), recycler)

        val runOffline = {
            ui.postDelayed({
                adapter.add(ChatMessage(false,
                    "Step 2/3: Code ready (offline draft):\n\n" + TeamDarkAIEngine.reply(task, img != null) +
                    "\n\nStep 3/3: Review done — copy karke use karo. Detail chahiye to bolo 'isme login add karo'."), recycler)
            }, 1200)
        }

        if (ApiKeyManager.hasAnyKey(this)) {
            val prov = ApiKeyManager.getActive(this)
            ui.postDelayed({
                adapter.add(ChatMessage(false, "Step 2/3: AI se build kar raha hoon (" + ApiKeyManager.activeLabel(this) + ")..."), recycler)
                AiApiClient.chat(this, prov, "Do this task step by step with full code: $task", object : AiApiClient.Cb {
                    override fun onOk(t: String) {
                        adapter.add(ChatMessage(false, "Step 3/3: Done ✅\n\n$t"), recycler)
                    }
                    override fun onErr(m: String) {
                        adapter.add(ChatMessage(false, "API fail ($m) — offline draft de raha hoon."), recycler)
                        runOffline()
                    }
                })
            }, 900)
        } else {
            ui.postDelayed({ runOffline() }, 900)
        }
    }

    // ---------- AUTH ----------
    private fun showAuth() {
        val cur = UserManager.current(this)
        if (cur != null) {
            AlertDialog.Builder(this)
                .setTitle("👤 " + cur.name)
                .setMessage("Gmail: " + cur.email + "\nPremium: " + (if (cur.premium) "YES 👑 (" + cur.plan + ")" else "NO — ☰ > Premium se lo") + "\n\nNote: Ye login isi phone pe save rehta hai (demo). Sab phones ka central data ke liye Firebase backend lagega.")
                .setPositiveButton("Logout") { _, _ -> UserManager.logout(this); refreshStatus(); toast("Logged out") }
                .setNegativeButton("Close", null)
                .show()
            return
        }
        val lay = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(48, 32, 48, 16) }
        val eName = EditText(this).apply { hint = "Name" }
        val eMail = EditText(this).apply { hint = "Gmail"; inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS }
        val ePass = EditText(this).apply { hint = "Password (min 4)"; inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD }
        lay.addView(eName); lay.addView(eMail); lay.addView(ePass)
        AlertDialog.Builder(this)
            .setTitle("👤 Login / Register")
            .setView(lay)
            .setPositiveButton("Register") { _, _ ->
                val r = UserManager.register(this, eName.text.toString(), eMail.text.toString(), ePass.text.toString())
                if (r == "OK") { refreshStatus(); toast("Welcome!"); adapter.add(ChatMessage(false, "✅ Register done — ab free chat + premium le sakte ho."), recycler) }
                else toast(r)
            }
            .setNeutralButton("Login") { _, _ ->
                val r = UserManager.login(this, eMail.text.toString(), ePass.text.toString())
                if (r == "OK") { refreshStatus(); toast("Welcome back!") } else toast(r)
            }
            .setNegativeButton("Close", null)
            .show()
    }

    // ---------- API KEYS ----------
    private fun showApiKeys() {
        val sv = ScrollView(this)
        val lay = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(40, 24, 40, 24) }
        val info = TextView(this).apply {
            text = "12 AI slots — key sirf isi phone me save hogi.\nActive: " + ApiKeyManager.activeLabel(this@MainActivity) + "\nCustom format: baseURL|key"
            setTextColor(0xFF94A3B8.toInt()); textSize = 12f
        }
        lay.addView(info)
        val edits = mutableMapOf<String, EditText>()
        for (p in ApiKeyManager.PROVIDERS) {
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
            val lbl = TextView(this).apply { text = p.label; setTextColor(0xFFF1F5F9.toInt()); textSize = 13f; layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f) }
            val use = TextView(this).apply {
                text = if (ApiKeyManager.getActive(this@MainActivity) == p.id) "● ACTIVE" else "USE"
                setTextColor(0xFF00F5FF.toInt()); textSize = 11f; setPadding(20, 14, 20, 14)
            }
            use.setOnClickListener { ApiKeyManager.setActive(this, p.id); refreshStatus(); toast("Active: " + p.label); showApiKeys() }
            row.addView(lbl); row.addView(use)
            lay.addView(row)
            val et = EditText(this).apply { hint = p.hint; setText(ApiKeyManager.getKey(this@MainActivity, p.id)); textSize = 12f; setTextColor(0xFFF1F5F9.toInt()) }
            edits[p.id] = et
            lay.addView(et)
        }
        sv.addView(lay)
        AlertDialog.Builder(this)
            .setTitle("🔑 API Keys (12)")
            .setView(sv)
            .setPositiveButton("Save") { _, _ ->
                for ((id, et) in edits) ApiKeyManager.setKey(this, id, et.text.toString())
                refreshStatus()
                toast("Saved: " + ApiKeyManager.countSet(this) + " keys")
            }
            .setNegativeButton("Close", null)
            .show()
    }

    // ---------- PREMIUM ----------
    private fun showPremium() {
        val cur = UserManager.current(this)
        if (cur == null) {
            toast("Pehle Login/Register karo")
            showAuth()
            return
        }
        if (UserManager.isPremium(this)) {
            AlertDialog.Builder(this)
                .setTitle("👑 Premium Active")
                .setMessage("Plan: " + cur.plan + "\nAgent Mode ON kar sakte ho ☰ > Agent Mode.")
                .setPositiveButton("OK", null).show()
            return
        }
        val lay = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(48, 32, 48, 16) }
        val tv = TextView(this).apply {
            text = "👑 TEAMDARK Premium\n\n• 1 MONTH — Rs 299\n• 3 MONTH — Rs 499\n• 1 YEAR — Rs 999\n\nUPI ID:\n" + UserManager.UPI_ID + "\n(tap COPY in dialog)\n\nPay karke neeche apna plan dabao, fir TG pe screenshot bhejo:\nSEND SCREENSHOT ON TG -: " + UserManager.TG_HANDLE + "\n\nOwner YES karega tabhi Premium on hoga (fake payment rokne ke liye)."
            setTextColor(0xFFF1F5F9.toInt()); textSize = 14f
        }
        lay.addView(tv)
        val dlg = AlertDialog.Builder(this)
            .setTitle("Premium / Pay")
            .setView(lay)
            .setPositiveButton("Copy UPI") { _, _ -> copyText(UserManager.UPI_ID); toast("UPI copied") }
            .setNeutralButton("Open TG") { _, _ ->
                try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(UserManager.TG_LINK))) }
                catch (_: Exception) { toast("TG open nahi hua") }
            }
            .setNegativeButton("Close", null)
            .create()
        dlg.show()
        // extra plan buttons via second dialog
        ui.postDelayed({
            AlertDialog.Builder(this)
                .setTitle("Plan select karo (pay ke baad)")
                .setItems(arrayOf("1 MONTH — 299", "3 MONTH — 499", "1 YEAR — 999")) { _, which ->
                    val plan = when (which) { 0 -> "1 MONTH"; 1 -> "3 MONTH"; else -> "1 YEAR" }
                    val amt = UserManager.planAmount(plan)
                    val r = UserManager.addPending(this, plan, amt)
                    if (r == "OK") {
                        adapter.add(ChatMessage(false,
                            "✅ Request bhej di: $plan (Rs $amt)\n\nAb ye karo:\n1. UPI " + UserManager.UPI_ID + " pe Rs $amt pay karo\n2. SEND SCREENSHOT ON TG -: " + UserManager.TG_HANDLE + "\n3. Owner approval ke baad Premium ON"), recycler)
                        try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(UserManager.TG_LINK))) } catch (_: Exception) {}
                    } else toast(r)
                }
                .show()
        }, 400)
    }

    private fun copyText(s: String) {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("TD", s))
    }

    // ---------- OWNER ----------
    private fun showOwnerLogin() {
        val lay = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(48, 32, 48, 16) }
        val eId = EditText(this).apply { hint = "Owner ID"; setText("") }
        val ePass = EditText(this).apply { hint = "Owner Password"; inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD }
        lay.addView(eId); lay.addView(ePass)
        AlertDialog.Builder(this)
            .setTitle("🛡 Owner Access (sirf owner)")
            .setView(lay)
            .setPositiveButton("Open") { _, _ ->
                if (UserManager.checkOwner(this, eId.text.toString(), ePass.text.toString())) showOwnerPanel()
                else toast("Wrong owner login")
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showOwnerPanel() {
        val users = UserManager.allUsers(this)
        val pend = UserManager.pendingList(this)
        val sv = ScrollView(this)
        val lay = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(40, 24, 40, 24) }
        lay.addView(TextView(this).apply {
            text = "Users: " + users.size + " | Pending: " + pend.size + "\n(Default owner: " + UserManager.ownerId() + " — password Settings jaisa Owner Panel me change karo)"
            setTextColor(0xFFFACC15.toInt()); textSize = 12f
        })
        lay.addView(TextView(this).apply { text = "\n— USERS —"; setTextColor(0xFF00F5FF.toInt()); textSize = 13f })
        if (users.isEmpty()) lay.addView(TextView(this).apply { text = "Koi user nahi (isi phone ka demo data)"; setTextColor(0xFF94A3B8.toInt()) })
        for (u in users) {
            lay.addView(TextView(this).apply {
                text = "• " + u.name + " | " + u.email + " | " + (if (u.premium) "PREMIUM " + u.plan else "FREE")
                setTextColor(0xFFF1F5F9.toInt()); textSize = 12f
            })
        }
        lay.addView(TextView(this).apply { text = "\n— PREMIUM REQUESTS (YES/NO) —"; setTextColor(0xFF00F5FF.toInt()); textSize = 13f })
        if (pend.isEmpty()) lay.addView(TextView(this).apply { text = "Koi pending nahi"; setTextColor(0xFF94A3B8.toInt()) })
        for (p in pend) {
            val row = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, 12, 0, 12) }
            row.addView(TextView(this).apply {
                text = p.name + " | " + p.email + "\nPlan: " + p.plan + " Rs " + p.amount
                setTextColor(0xFFF1F5F9.toInt()); textSize = 12f
            })
            val btns = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            val yes = TextView(this).apply { text = "  YES  "; setTextColor(0xFF22C55E.toInt()); textSize = 14f; setPadding(24, 12, 24, 12) }
            val no = TextView(this).apply { text = "  NO  "; setTextColor(0xFFEC4899.toInt()); textSize = 14f; setPadding(24, 12, 24, 12) }
            yes.setOnClickListener { toast(UserManager.approve(this, p.email, true)); showOwnerPanel() }
            no.setOnClickListener { toast(UserManager.approve(this, p.email, false)); showOwnerPanel() }
            btns.addView(yes); btns.addView(no)
            row.addView(btns)
            lay.addView(row)
        }
        // change owner password
        val chTitle = TextView(this).apply { text = "\n— Owner password change —"; setTextColor(0xFF00F5FF.toInt()) }
        lay.addView(chTitle)
        val eNew = EditText(this).apply { hint = "Naya owner password"; inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD }
        lay.addView(eNew)
        val chBtn = TextView(this).apply { text = "CHANGE PASSWORD"; setTextColor(0xFFFACC15.toInt()); gravity = Gravity.CENTER; setPadding(0, 16, 0, 16) }
        chBtn.setOnClickListener {
            val v = eNew.text.toString()
            if (v.length < 4) toast("Min 4 char") else { UserManager.setOwnerPass(this, v); toast("Owner password changed") }
        }
        lay.addView(chBtn)
        sv.addView(lay)
        AlertDialog.Builder(this).setTitle("🛡 Owner Panel").setView(sv).setPositiveButton("Close", null).show()
    }

    private fun showSettings() {
        val keys = ApiKeyManager.countSet(this)
        val cur = UserManager.current(this)
        AlertDialog.Builder(this)
            .setTitle("⚙ Settings")
            .setMessage("User: " + (cur?.email ?: "Guest") + "\nKeys saved: " + keys + " / 12\nActive: " + ApiKeyManager.activeLabel(this) + "\n\n• Clear chat: ✦ NEW dabao\n• Keys: ☰ > API Keys\n• TG: " + UserManager.TG_HANDLE + "\n• UPI: " + UserManager.UPI_ID + "\n\nv2.0 — Powered by DARK DEVEL")
            .setPositiveButton("Open TG") { _, _ ->
                try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(UserManager.TG_LINK))) } catch (_: Exception) {}
            }
            .setNegativeButton("Close", null)
            .show()
    }
}
