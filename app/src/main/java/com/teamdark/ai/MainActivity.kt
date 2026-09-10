package com.teamdark.ai

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var input: EditText
    private lateinit var adapter: ChatAdapter
    private val data = mutableListOf<ChatMessage>()
    private var pendingImage: Uri? = null
    private val ui = Handler(Looper.getMainLooper())

    private lateinit var previewBox: LinearLayout
    private lateinit var previewImg: ImageView

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            try {
                contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: Exception) {}
            pendingImage = uri
            previewBox.visibility = android.view.View.VISIBLE
            previewImg.setImageURI(uri)
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

        adapter = ChatAdapter(data)
        recycler.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        recycler.adapter = adapter

        setupChips()
        welcome()

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
    }

    private fun setupChips() {
        val row = findViewById<LinearLayout>(R.id.chipRow)
        val chips = listOf("C++ Starter", "Java App", "Python Script", "Lua Mod", "APK Guide", "Hello TD")
        val prompts = mapOf(
            "C++ Starter" to "c++ me starter code do",
            "Java App" to "java me android button banao",
            "Python Script" to "python me file organizer banao",
            "Lua Mod" to "lua me mod script do",
            "APK Guide" to "apk structure samjhao",
            "Hello TD" to "hello"
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
        val w = ChatMessage(false,
            "👑 Welcome to TEAMDARK.AI\n\n" +
            "Main offline hoon — no API, unlimited, smooth.\n" +
            "• C++ • Java • Python • Lua • Mod basics\n" +
            "• ⧉ COPY har reply pe\n" +
            "• 🖼 se gallery photo bhejo\n\n" +
            "Bolo boss, kya banau? Upar chips dabao ya likho."
        )
        adapter.add(w, recycler)
    }

    private fun send() {
        val text = input.text.toString().trim()
        val img = pendingImage
        if (text.isEmpty() && img == null) return

        adapter.add(ChatMessage(true, if (text.isEmpty()) "📸 Photo bheji hai" else text, img?.toString()), recycler)
        input.setText("")
        pendingImage = null
        previewBox.visibility = android.view.View.GONE

        // typing indicator
        val typing = ChatMessage(false, "✍ TD typing…")
        adapter.add(typing, recycler)

        ui.postDelayed({
            // remove typing
            if (data.isNotEmpty() && data.last().text.startsWith("✍")) {
                data.removeAt(data.size - 1)
                adapter.notifyItemRemoved(data.size)
            }
            val hasImg = img != null
            val ans = TeamDarkAIEngine.reply(text, hasImg)
            // if image was sent, keep thumbnail in AI reply too for context
            adapter.add(ChatMessage(false, ans, null), recycler)
        }, 550)
    }
}
