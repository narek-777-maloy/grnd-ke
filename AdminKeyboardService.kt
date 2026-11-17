package com.admin.panel

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class AdminKeyboardService : InputMethodService() {
    companion object {
        private const val BG = 0xFF15181D.toInt()
        private const val KEY = 0xFF2B303A.toInt()
        private const val DANGER = 0xFF5A2E22.toInt()
        private const val ARMED = 0xFFB3261E.toInt()
        private const val ACCENT = 0xFF2F6FED.toInt()
    }
    private val id = StringBuilder()
    private val handler = Handler(Looper.getMainLooper())
    private var tab = 0
    private var armedKey: String? = null
    private var armedAt = 0L
    private lateinit var idView: TextView
    private lateinit var autoView: TextView
    private lateinit var hint: TextView
    private val tabViews = arrayOfNulls<TextView>(4)
    private lateinit var menu: LinearLayout
    private lateinit var grid: GridLayout

    override fun onEvaluateFullscreenMode() = false

    override fun onCreateInputView(): View {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(BG); setPadding(dp(4), dp(2), dp(4), dp(4)) }
        val top = LinearLayout(this)
        idView = TextView(this).apply { setTextColor(Color.WHITE); textSize = 17f; setTypeface(null, Typeface.BOLD); gravity = Gravity.CENTER_VERTICAL; setPadding(dp(8), 0, 0, 0) }
        top.addView(idView, LinearLayout.LayoutParams(0, dp(44), 2.4f))
        for (i in 1..10) { val digit = (i % 10).toString(); top.addView(key(digit, KEY, 19f) { addDigit(digit) }, cell(1f)) }
        val back = key("⌫", DANGER, 19f) { back() }
        back.setOnLongClickListener { id.setLength(0); refresh(); true }
        top.addView(back, cell(1.3f))
        top.addView(key("🌐", KEY, 17f) { (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker() }, cell(1f))
        root.addView(top)

        hint = TextView(this).apply { text = "Введи ID игрока (от 2 цифр), появятся кнопки"; setTextColor(0xFF8A93A0.toInt()); textSize = 13f; gravity = Gravity.CENTER }
        root.addView(hint, LinearLayout.LayoutParams(-1, dp(40)))
        menu = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val tabs = LinearLayout(this)
        val names = arrayOf("Ответы", "Работы", "Наказания", "Действия")
        for (i in names.indices) {
            tabViews[i] = key(names[i], KEY, 13f) { tab = i; armedKey = null; buildGrid() }
            tabs.addView(tabViews[i], cell(1f, 34))
        }
        autoView = key("", KEY, 13f) { Cfg.p(this).edit().putBoolean("auto", !Cfg.auto(this)).apply(); buildGrid() }
        tabs.addView(autoView, cell(1f, 34))
        menu.addView(tabs)
        grid = GridLayout(this)
        val widthDp = resources.displayMetrics.widthPixels / resources.displayMetrics.density
        grid.columnCount = maxOf(3, (widthDp / 135).toInt())
        val scroll = ScrollView(this).apply { addView(grid) }
        menu.addView(scroll, LinearLayout.LayoutParams(-1, dp(112)))
        root.addView(menu)
        buildGrid(); refresh()
        return root
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        id.setLength(0); refresh(); buildGrid()
    }
    private fun addDigit(d: String) { if (id.length < 4) id.append(d); refresh() }
    private fun back() { if (id.isNotEmpty()) id.setLength(id.length - 1); refresh() }
    private fun refresh() {
        if (!::idView.isInitialized) return
        idView.text = if (id.isEmpty()) "ID: —" else "ID: $id"
        val visible = id.length >= 2
        menu.visibility = if (visible) View.VISIBLE else View.GONE
        hint.visibility = if (visible) View.GONE else View.VISIBLE
    }

    private fun buildGrid() {
        if (!::grid.isInitialized) return
        for (i in 0..3) tabViews[i]?.let { setBg(it, if (i == tab) ACCENT else KEY) }
        autoView.text = if (Cfg.auto(this)) "⏎ вкл" else "⏎ выкл"
        grid.removeAllViews()
        when (tab) {
            0 -> Data.ANSWERS.forEach { a -> add(a[0], KEY) { send(Cfg.s(this, "prefix") + a[1], null, "") } }
            1 -> Data.JOBS.forEach { a -> add(a[0], KEY) { send(a[1], null, "") } }
            2 -> Data.PUNS.forEachIndexed { i, p ->
                val key = "p$i"
                val hasCommand = p[1].isNotEmpty()
                val armed = hasCommand && key == armedKey && System.currentTimeMillis() - armedAt < 3000
                add(if (armed) "ТОЧНО? ${p[0]}" else p[0], if (armed) ARMED else if (hasCommand) DANGER else KEY) {
                    if (hasCommand && !armed) {
                        armedKey = key; armedAt = System.currentTimeMillis(); buildGrid()
                        handler.postDelayed({ buildGrid() }, 3100)
                    } else { armedKey = null; send(p[3], p[1], p[2]) }
                }
            }
            else -> {
                add("Слежка ${Cfg.s(this, "re")}", KEY) { send(null, Cfg.s(this, "re"), "") }
                add("ТП к игроку ${Cfg.s(this, "go")}", KEY) { send(null, Cfg.s(this, "go"), "") }
                add("ТП ко мне ${Cfg.s(this, "gh")}", KEY) { send(null, Cfg.s(this, "gh"), "") }
            }
        }
    }

    private fun add(label: String, color: Int, action: () -> Unit) {
        val view = key(label, color, 12f, action).apply { maxLines = 2; setPadding(dp(4), 0, dp(4), 0) }
        val lp = GridLayout.LayoutParams(GridLayout.spec(GridLayout.UNDEFINED), GridLayout.spec(GridLayout.UNDEFINED, 1f)).apply {
            width = 0; height = dp(38); setMargins(dp(2), dp(2), dp(2), dp(2))
        }
        grid.addView(view, lp)
    }

    private fun send(message: String?, command: String?, args: String?) {
        val playerId = id.toString()
        if (playerId.isEmpty()) return
        val commands = mutableListOf<String>()
        if (message != null) commands.add("${Cfg.s(this, "ans")} $playerId $message")
        if (!command.isNullOrEmpty()) commands.add("$command $playerId${if (args.isNullOrEmpty()) "" else " $args"}")
        val pressEnter = commands.size > 1 || Cfg.auto(this)
        var delay = 0L
        commands.forEachIndexed { index, text ->
            val enter = index < commands.lastIndex || pressEnter
            handler.postDelayed({ put(text, enter) }, delay)
            delay += Cfg.delay(this)
        }
        id.setLength(0); refresh()
    }

    private fun put(text: String, enter: Boolean) {
        val connection: InputConnection = currentInputConnection ?: return
        connection.beginBatchEdit()
        connection.deleteSurroundingText(10000, 10000)
        connection.commitText(text, 1)
        connection.endBatchEdit()
        if (enter && !sendDefaultEditorAction(true)) sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
    }

    private fun key(label: String, color: Int, size: Float, action: () -> Unit): TextView = TextView(this).apply {
        text = label; setTextColor(Color.WHITE); textSize = size; gravity = Gravity.CENTER; setBg(this, color)
        setOnTouchListener { view, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> view.alpha = 0.6f
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> view.alpha = 1f
            }
            false
        }
        setOnClickListener { performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); action() }
    }
    private fun setBg(view: View, color: Int) { view.background = GradientDrawable().apply { setColor(color); cornerRadius = dp(8).toFloat() } }
    private fun cell(weight: Float, height: Int = 44) = LinearLayout.LayoutParams(0, dp(height), weight)
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
