package com.teamdark.ai

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAdapter(private val list: MutableList<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val root: LinearLayout = v.findViewById(R.id.root)
        val avatar: TextView = v.findViewById(R.id.avatar)
        val col: LinearLayout = v.findViewById(R.id.col)
        val senderName: TextView = v.findViewById(R.id.senderName)
        val msgTime: TextView = v.findViewById(R.id.msgTime)
        val bubble: LinearLayout = v.findViewById(R.id.bubble)
        val msgText: TextView = v.findViewById(R.id.msgText)
        val msgImage: ImageView = v.findViewById(R.id.msgImage)
        val codeCard: LinearLayout = v.findViewById(R.id.codeCard)
        val codeLang: TextView = v.findViewById(R.id.codeLang)
        val codeText: TextView = v.findViewById(R.id.codeText)
        val btnCopyCode: TextView = v.findViewById(R.id.btnCopyCode)
        val aiActions: LinearLayout = v.findViewById(R.id.aiActions)
        val btnCopy: TextView = v.findViewById(R.id.btnCopy)
        val btnShare: TextView = v.findViewById(R.id.btnShare)
    }

    private val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(p: ViewGroup, vt: Int): VH {
        val v = LayoutInflater.from(p.context).inflate(R.layout.item_message, p, false)
        return VH(v)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(h: VH, pos: Int) {
        val m = list[pos]
        val ctx = h.itemView.context

        // entry animation (subtle, professional)
        val anim = AlphaAnimation(0f, 1f).apply { duration = 220 }
        h.itemView.startAnimation(anim)

        h.msgTime.text = try { timeFmt.format(Date(m.time)) } catch (_: Exception) { "" }

        if (m.imageUri != null) {
            h.msgImage.visibility = View.VISIBLE
            try { h.msgImage.setImageURI(Uri.parse(m.imageUri)) }
            catch (_: Exception) { h.msgImage.visibility = View.GONE }
        } else h.msgImage.visibility = View.GONE

        // split code blocks: first ```...``` part
        val parts = splitCode(m.text)
        val mainText = parts.first
        val code = parts.second
        val lang = parts.third

        if (m.isUser) {
            // user right side, avatar right
            h.root.gravity = Gravity.END
            h.avatar.text = "YOU"
            h.avatar.setBackgroundResource(R.drawable.avatar_user)
            h.avatar.setTextColor(0xFF8B9BB4.toInt())
            h.senderName.text = "You"
            h.senderName.setTextColor(0xFF8B9BB4.toInt())
            h.col.gravity = Gravity.END
            h.bubble.setBackgroundResource(R.drawable.bubble_user)
            h.msgText.text = mainText.ifEmpty { m.text }
            h.msgText.setTextColor(0xFFFFFFFF.toInt())
            h.aiActions.visibility = View.GONE
            // avatar last (right): move avatar to end
            h.root.removeAllViews()
            h.root.addView(h.col)
            h.root.addView(h.avatar)
            val lp = (h.avatar.layoutParams as? LinearLayout.LayoutParams)
            lp?.setMargins(9, 0, 0, 0)
            h.avatar.layoutParams = lp
        } else {
            h.root.gravity = Gravity.START
            // restore order avatar first
            h.root.removeAllViews()
            h.root.addView(h.avatar)
            h.root.addView(h.col)
            val lp = (h.col.layoutParams as? LinearLayout.LayoutParams)
            lp?.setMargins(9, 0, 0, 0)
            h.col.layoutParams = lp

            h.avatar.text = "TD"
            h.avatar.setBackgroundResource(R.drawable.avatar_ai)
            h.avatar.setTextColor(0xFFFFFFFF.toInt())
            h.col.gravity = Gravity.START
            val isTask = m.text.startsWith("📋") || m.text.startsWith("🔧") || m.text.startsWith("✅") || m.text.startsWith("🤖")
            h.senderName.text = if (isTask) "TEAMDARK Agent • task" else "TEAMDARK Agent"
            h.senderName.setTextColor(0xFF10B981.toInt())
            h.bubble.setBackgroundResource(R.drawable.bubble_ai)
            h.msgText.text = mainText.ifEmpty { m.text }
            h.msgText.setTextColor(0xFFF8FAFC.toInt())
            h.aiActions.visibility = View.VISIBLE
            h.btnCopy.setOnClickListener {
                copy(ctx, m.text)
                Toast.makeText(ctx, "Copied", Toast.LENGTH_SHORT).show()
            }
            h.btnShare.setOnClickListener {
                val i = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"; putExtra(Intent.EXTRA_TEXT, m.text)
                }
                ctx.startActivity(Intent.createChooser(i, "Share via"))
            }
            h.msgText.setOnLongClickListener { copy(ctx, m.text); Toast.makeText(ctx, "Copied", Toast.LENGTH_SHORT).show(); true }
        }

        // code card
        if (code != null && code.isNotBlank()) {
            h.codeCard.visibility = View.VISIBLE
            h.codeLang.text = "● " + (if (lang.isNotBlank()) lang else "code")
            h.codeText.text = code
            h.btnCopyCode.setOnClickListener {
                copy(ctx, code)
                Toast.makeText(ctx, "Code copied", Toast.LENGTH_SHORT).show()
            }
        } else {
            h.codeCard.visibility = View.GONE
        }
    }

    private fun splitCode(s: String): Triple<String, String?, String> {
        return try {
            val a = s.indexOf("```")
            if (a == -1) return Triple(s, null, "")
            val b = s.indexOf("```", a + 3)
            if (b == -1) return Triple(s, null, "")
            var head = s.substring(a + 3, b)
            var lang = ""
            val nl = head.indexOf("\n")
            if (nl != -1) {
                val first = head.substring(0, nl).trim()
                if (first.length in 1..12 && !first.contains(" ")) {
                    lang = first
                    head = head.substring(nl + 1)
                }
            }
            val main = (s.substring(0, a).trim() + "\n" + s.substring(b + 3).trim()).trim()
            Triple(main, head.trim(), lang)
        } catch (_: Exception) { Triple(s, null, "") }
    }

    private fun copy(ctx: Context, s: String) {
        val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("TD", s))
    }

    fun add(m: ChatMessage, rv: RecyclerView) {
        list.add(m)
        notifyItemInserted(list.size - 1)
        rv.smoothScrollToPosition(list.size - 1)
    }

    fun clear(rv: RecyclerView) {
        list.clear()
        notifyDataSetChanged()
    }
}
