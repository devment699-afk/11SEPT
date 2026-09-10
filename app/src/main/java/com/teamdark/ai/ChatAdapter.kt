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
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(private val list: MutableList<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val root: LinearLayout = v.findViewById(R.id.root)
        val bubble: LinearLayout = v.findViewById(R.id.bubble)
        val msgText: TextView = v.findViewById(R.id.msgText)
        val msgImage: ImageView = v.findViewById(R.id.msgImage)
        val aiActions: LinearLayout = v.findViewById(R.id.aiActions)
        val btnCopy: TextView = v.findViewById(R.id.btnCopy)
        val btnShare: TextView = v.findViewById(R.id.btnShare)
    }

    override fun onCreateViewHolder(p: ViewGroup, vt: Int): VH {
        val v = LayoutInflater.from(p.context).inflate(R.layout.item_message, p, false)
        return VH(v)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(h: VH, pos: Int) {
        val m = list[pos]
        val ctx = h.itemView.context
        h.msgText.text = m.text

        // image
        if (m.imageUri != null) {
            h.msgImage.visibility = View.VISIBLE
            try { h.msgImage.setImageURI(Uri.parse(m.imageUri)) }
            catch (_: Exception) { h.msgImage.visibility = View.GONE }
        } else h.msgImage.visibility = View.GONE

        if (m.isUser) {
            h.root.gravity = Gravity.END
            h.bubble.setBackgroundResource(R.drawable.bubble_user)
            h.msgText.setTextColor(0xFFFFFFFF.toInt())
            h.aiActions.visibility = View.GONE
        } else {
            h.root.gravity = Gravity.START
            h.bubble.setBackgroundResource(R.drawable.bubble_ai)
            h.msgText.setTextColor(0xFFF1F5F9.toInt())
            h.aiActions.visibility = View.VISIBLE
            h.btnCopy.setOnClickListener {
                copy(ctx, m.text)
                Toast.makeText(ctx, "Copied!", Toast.LENGTH_SHORT).show()
            }
            h.btnShare.setOnClickListener {
                val i = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"; putExtra(Intent.EXTRA_TEXT, m.text)
                }
                ctx.startActivity(Intent.createChooser(i, "Share via"))
            }
            h.msgText.setOnLongClickListener { copy(ctx, m.text); Toast.makeText(ctx, "Copied!", Toast.LENGTH_SHORT).show(); true }
        }
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
        rv.scrollToPosition(0)
    }
}
