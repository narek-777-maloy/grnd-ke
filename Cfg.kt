package com.admin.panel

import android.content.Context
import android.content.SharedPreferences

internal object Cfg {
    fun p(c: Context): SharedPreferences = c.getSharedPreferences("ap", Context.MODE_PRIVATE)
    fun def(k: String): String = when (k) {
        "ans" -> "/ans"
        "re" -> "/re"
        "go" -> "/goto"
        "gh" -> "/gethere"
        "prefix" -> "Приветствую, "
        "delay" -> "900"
        else -> ""
    }
    fun s(c: Context, k: String): String = p(c).getString(k, def(k)) ?: def(k)
    fun auto(c: Context): Boolean = p(c).getBoolean("auto", true)
    fun delay(c: Context): Long = s(c, "delay").trim().toLongOrNull() ?: 900L
}
