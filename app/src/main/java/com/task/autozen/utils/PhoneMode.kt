package com.task.autozen.utils

enum class PhoneMode(val mode: Int) {
    SILENT(0),
    VIBRATE(1),
    NORMAL(2);

    companion object {
        fun fromInt(mode: Int): PhoneMode {
            return entries.find { it.mode == mode } ?: NORMAL
        }
    }
}
