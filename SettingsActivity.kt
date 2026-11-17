package com.admin.panel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class SettingsActivity : Activity() {
    private val fields = arrayOf(
        "ans" to "Команда ответа на репорт", "re" to "Команда слежки",
        "go" to "ТП к игроку", "gh" to "ТП игрока к себе",
        "prefix" to "Приставка к быстрым ответам", "delay" to "Пауза между двумя командами (мс)"
    )
    private lateinit var edits: Array<EditText>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pad = (16 * resources.displayMetrics.density).toInt()
        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(pad, pad, pad, pad) }
        setContentView(ScrollView(this).apply { addView(layout) })
        layout.addView(TextView(this).apply { text = "Админ-клавиатура"; textSize = 22f })
        layout.addView(TextView(this).apply {
            text = "1) Нажми «Включить» и включи «Админ-клава».\n" +
                "2) Открой чат игры, нажми «Выбрать» и выбери «Админ-клава».\n" +
                "3) Вводи ID цифрами. После 2 цифр появятся кнопки. Наказания подтверждаются вторым нажатием.\n"
        })
        layout.addView(button("1. Включить клавиатуру") { startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) })
        layout.addView(button("2. Выбрать клавиатуру") {
            (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker()
        })
        edits = Array(fields.size) { i ->
            layout.addView(TextView(this).apply { text = fields[i].second; setPadding(0, pad / 2, 0, 0) })
            EditText(this).also { it.setSingleLine(true); it.setText(Cfg.s(this, fields[i].first)); layout.addView(it) }
        }
        val auto = CheckBox(this).apply { text = "Сразу отправлять (Enter) после подстановки"; isChecked = Cfg.auto(this@SettingsActivity) }
        layout.addView(auto)
        layout.addView(button("Сохранить") {
            val editor = Cfg.p(this).edit()
            fields.forEachIndexed { i, field -> editor.putString(field.first, edits[i].text.toString()) }
            editor.putBoolean("auto", auto.isChecked).apply()
            Toast.makeText(this, "Сохранено", Toast.LENGTH_SHORT).show()
        })
    }

    private fun button(label: String, action: () -> Unit) = Button(this).apply {
        text = label
        setOnClickListener { action() }
    }
}
