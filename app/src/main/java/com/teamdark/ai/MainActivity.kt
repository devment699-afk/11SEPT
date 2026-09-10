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
    private var agentOn = true

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
        // LOGIN GATE — open hote hi login
        if (UserManager.current(this) == null) {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
            return
        }
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
                    startActivity(Intent(this, AuthActivity::class.java))
                    finish()
                }
            }
            true
        }
    }

    override fun onResume() {
        super.onResume()
        if (::statusLine.isInitialized) refreshStatus()
        // session out hua to gate
        if (::recycler.isInitialized && UserManager.current(this) == null) {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
        }
    }

    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show()

    private fun refreshStatus() {
        val u = UserManager.current(this) ?: return
        val prem = UserManager.isPremium(this)
        val keys = ApiKeyManager.countSet(this)
        val active = ApiKeyManager.activeLabel(this)
        statusLine.text = "ONLINE AGENT • $active • " + u.name + (if (prem) " 👑" else "")
        agentBadge.text = if (agentOn) "AGENT: ON" else "AGENT: OFF"
        agentBadge.setTextColor(if (agentOn) 0xFF10B981.toInt() else 0xFF94A3B8.toInt())
    }

    private fun setupChips() {
        val row = findViewById<LinearLayout>(R.id.chipRow)
        val chips = listOf("App Banao", "Bug Fix", "Python Tool", "Lua Script", "API Setup", "Agent Task")
        val prompts = mapOf(
            "App Banao" to "Mere liye ek notes app ka full plan + code do",
            "Bug Fix" to "Mere code me error hai, step by step fix karo: ",
            "Python Tool" to "Python me file organizer tool banao with steps",
            "Lua Script" to "Lua me game helper script banao with explain",
            "API Setup" to "API key kaise lagau? steps batao",
            "Agent Task" to "Ek calculator app banane ka agent plan + code do"
        )
        row.removeAllViews()
        for (c in chips) {
            val tv = TextView(this)
            tv.text = "⚡ $c"
            tv.textSize = 12f
            tv.setTextColor(0xFF10B981.toInt())
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
        val hi = if (u != null) u.name else "Boss"
        adapter.add(ChatMessage(false,
            "🤖 TEAMDARK Online Agent ready — Hello $hi!\n\n" +
            "Manus/Devin-style: main task ko steps me todke online AI se karta hoon.\n" +
            "• Keys: " + ApiKeyManager.countSet(this) + "/12 lagi hain (" + ApiKeyManager.activeLabel(this) + ")\n" +
            "• Agent: " + (if (agentOn) "ON" else "OFF") + " — premium me multi-step\n" +
            "• Bina key ke chat nahi chalega (offline hata diya)\n\n" +
            "Pehle ☰ > API Keys me key lagao, fir task bolo."
        ), recycler)
        if (!ApiKeyManager.hasAnyKey(this)) {
            ui.postDelayed({ showApiKeys() }, 800)
        }
    }

    // ---------- SEND — ONLINE ONLY ----------
    private fun send() {
        if (UserManager.current(this) == null) {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
            return
        }
        val text = input.text.toString().trim()
        val img = pendingImage
        if (text.isEmpty() && img == null) return
        if (!ApiKeyManager.hasAnyKey(this)) {
            adapter.add(ChatMessage(true, if (text.isEmpty()) "Photo bheji hai" else text, img?.toString()), recycler)
            adapter.add(ChatMessage(false,
                "🔑 Online agent ke liye API key chahiye.\n☰ > API Keys kholo, 12 me se koi 1 key lagao (Groq/OpenRouter free mil jati hai), fir dobara bhejo.\nOffline mode hata diya gaya hai."), recycler)
            showApiKeys()
            return
        }
        if (agentOn && UserManager.isPremium(this)) { agentSend(text, img); return }

        adapter.add(ChatMessage(true, if (text.isEmpty()) "Photo + task bheja" else text, img?.toString()), recycler)
        input.setText("")
        pendingImage = null
        previewBox.visibility = android.view.View.GONE

        adapter.add(ChatMessage(false, "🔧 Agent working…"), recycler)
        val prov = ApiKeyManager.getActive(this)
        val fullMsg = if (text.isEmpty()) "User ne photo bheji hai with no text. Ask what to do with it, step by step." else text
        AiApiClient.chat(this, prov, fullMsg, object : AiApiClient.Cb {
            override fun onOk(t: String) {
                removeTyping()
                adapter.add(ChatMessage(false, t + "\n\n— via " + ApiKeyManager.activeLabel(this@MainActivity)), recycler)
            }
            override fun onErr(m: String) {
                removeTyping()
                adapter.add(ChatMessage(false,
                    "❌ Online error: " + m.take(160) + "\n• Key check karo ☰ > API Keys\n• Net check karo\n• Dusra provider try karo"), recycler)
            }
        })
    }

    private fun removeTyping() {
        if (data.isNotEmpty() && (data.last().text.startsWith("✍") || data.last().text.startsWith("🔧"))) {
            data.removeAt(data.size - 1)
            adapter.notifyItemRemoved(data.size)
        }
    }

    private fun toggleAgent() {
        if (!UserManager.isPremium(this)) {
            adapter.add(ChatMessage(false,
                "🔒 Agent Mode PREMIUM hai.\nSingle online reply free (apni key se), multi-step agent premium me.\n☰ > Premium / Pay kholo."), recycler)
            showPremium()
            return
        }
        agentOn = !agentOn
        refreshStatus()
        toast(if (agentOn) "Agent ON" else "Agent OFF")
        adapter.add(ChatMessage(false,
            if (agentOn) "🤖 Agent ON — task bolo, steps me karunga."
            else "Agent OFF — single reply mode."), recycler)
    }

    private fun agentSend(text: String, img: Uri?) {
        val task = if (text.isEmpty()) "photo task" else text
        adapter.add(ChatMessage(true, "[AGENT TASK] $task", img?.toString()), recycler)
        input.setText("")
        pendingImage = null
        previewBox.visibility = android.view.View.GONE

        adapter.add(ChatMessage(false, "📋 Step 1/3: Task samjha — plan bana raha hoon…"), recycler)
        val prov = ApiKeyManager.getActive(this)
        ui.postDelayed({
            adapter.add(ChatMessage(false, "🔧 Step 2/3: Online AI se execute (" + ApiKeyManager.activeLabel(this) + ")…"), recycler)
            AiApiClient.chat(this, prov, "You are an autonomous coding agent like Manus/Devin. Do this task with plan + full code + how to run: $task", object : AiApiClient.Cb {
                override fun onOk(t: String) {
                    adapter.add(ChatMessage(false, "✅ Step 3/3: Done\n\n$t"), recycler)
                }
                override fun onErr(m: String) {
                    adapter.add(ChatMessage(false, "❌ Agent fail: " + m.take(160) + "\nKey/net check karo."), recycler)
                }
            })
        }, 900)
    }

    // ---------- AUTH ----------
    private fun showAuth() {
        val cur = UserManager.current(this)
        if (cur != null) {
            AlertDialog.Builder(this)
                .setTitle("👤 " + cur.name)
                .setMessage("Gmail: " + cur.email + "\nPremium: " + (if (cur.premium) "YES 👑 (" + cur.plan + ")" else "NO — ☰ > Premium se lo"))
                .setPositiveButton("Logout") { _, _ ->
                    UserManager.logout(this)
                    startActivity(Intent(this, AuthActivity::class.java))
                    finish()
                }
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
                if (r == "OK") { refreshStatus(); toast("Welcome!") } else toast(r)
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
            text = "ONLINE ke liye 1 key must hai (12 slots).\nActive: " + ApiKeyManager.activeLabel(this@MainActivity) + "\nFree keys: Groq / OpenRouter / Gemini se lo.\nCustom format: baseURL|key"
            setTextColor(0xFF94A3B8.toInt()); textSize = 12f
        }
        lay.addView(info)
        val edits = mutableMapOf<String, EditText>()
        for (p in ApiKeyManager.PROVIDERS) {
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
            val lbl = TextView(this).apply { text = p.label; setTextColor(0xFFF1F5F9.toInt()); textSize = 13f; layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f) }
            val use = TextView(this).apply {
                text = if (ApiKeyManager.getActive(this@MainActivity) == p.id) "● ACTIVE" else "USE"
                setTextColor(0xFF10B981.toInt()); textSize = 11f; setPadding(20, 14, 20, 14)
            }
            val pid = p.id
            use.setOnClickListener { ApiKeyManager.setActive(this, pid); refreshStatus(); toast("Active: " + p.label) }
            row.addView(lbl); row.addView(use)
            lay.addView(row)
            val et = EditText(this).apply { hint = p.hint; setText(ApiKeyManager.getKey(this@MainActivity, p.id)); textSize = 12f; setTextColor(0xFFF1F5F9.toInt()) }
            edits[p.id] = et
            lay.addView(et)
        }
        sv.addView(lay)
        AlertDialog.Builder(this)
            .setTitle("🔑 API Keys (online must)")
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
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
            return
        }
        if (UserManager.isPremium(this)) {
            AlertDialog.Builder(this)
                .setTitle("👑 Premium Active")
                .setMessage("Plan: " + cur.plan + "\nAgent Mode ready hai.")
                .setPositiveButton("OK", null).show()
            return
        }
        val lay = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(48, 32, 48, 16) }
        val tv = TextView(this).apply {
            text = "👑 TEAMDARK Premium (Agent unlock)\n\n• 1 MONTH — Rs 299\n• 3 MONTH — Rs 499\n• 1 YEAR — Rs 999\n\nUPI ID:\n" + UserManager.UPI_ID + "\n\nPay karke plan dabao, fir TG pe screenshot:\nSEND SCREENSHOT ON TG -: " + UserManager.TG_HANDLE
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
        ui.postDelayed({
            AlertDialog.Builder(this)
                .setTitle("Plan select karo (pay ke baad)")
                .setItems(arrayOf("1 MONTH — 299", "3 MONTH — 499", "1 YEAR — 999")) { _, which ->
                    val plan = when (which) { 0 -> "1 MONTH"; 1 -> "3 MONTH"; else -> "1 YEAR" }
                    val amt = UserManager.planAmount(plan)
                    val r = UserManager.addPending(this, plan, amt)
                    if (r == "OK") {
                        adapter.add(ChatMessage(false,
                            "✅ Request: $plan (Rs $amt)\n1. UPI " + UserManager.UPI_ID + " pe pay karo\n2. SEND SCREENSHOT ON TG -: " + UserManager.TG_HANDLE + "\n3. Owner YES ke baad Premium ON"), recycler)
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
        val eId = EditText(this).apply { hint = "Owner ID" }
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
            text = "Users: " + users.size + " | Pending: " + pend.size
            setTextColor(0xFFFACC15.toInt()); textSize = 12f
        })
        lay.addView(TextView(this).apply { text = "\n— USERS —"; setTextColor(0xFF10B981.toInt()); textSize = 13f })
        if (users.isEmpty()) lay.addView(TextView(this).apply { text = "Koi user nahi"; setTextColor(0xFF94A3B8.toInt()) })
        for (u in users) {
            lay.addView(TextView(this).apply {
                text = "• " + u.name + " | " + u.email + " | " + (if (u.premium) "PREMIUM " + u.plan else "FREE")
                setTextColor(0xFFF1F5F9.toInt()); textSize = 12f
            })
        }
        lay.addView(TextView(this).apply { text = "\n— REQUESTS (YES/NO) —"; setTextColor(0xFF10B981.toInt()); textSize = 13f })
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
        val eNew = EditText(this).apply { hint = "Naya owner password"; inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD }
        lay.addView(eNew)
        val chBtn = TextView(this).apply { text = "CHANGE PASSWORD"; setTextColor(0xFFFACC15.toInt()); gravity = Gravity.CENTER; setPadding(0, 16, 0, 16) }
        chBtn.setOnClickListener {
            val v = eNew.text.toString()
            if (v.length < 4) toast("Min 4 char") else { UserManager.setOwnerPass(this, v); toast("Changed") }
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
            .setMessage("User: " + (cur?.email ?: "Guest") + "\nKeys: " + keys + " / 12\nActive: " + ApiKeyManager.activeLabel(this) + "\nTG: " + UserManager.TG_HANDLE + "\nUPI: " + UserManager.UPI_ID + "\nv3.0 online-agent — Powered by DARK DEVEL")
            .setPositiveButton("Open TG") { _, _ ->
                try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(UserManager.TG_LINK))) } catch (_: Exception) {}
            }
            .setNegativeButton("Close", null)
            .show()
    }
}
