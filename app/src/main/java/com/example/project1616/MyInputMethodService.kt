package com.example.project1616

import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.os.Build
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.widget.PopupWindow
import androidx.annotation.RequiresApi

class MyInputMethodService : InputMethodService(), KeyboardView.OnKeyboardActionListener  {
    private var keyboardView: KeyboardView? = null
    private var keyboard: Keyboard? = null
    private var caps = false
    private var lock = false
    private var KeyboardState: Int = R.integer.keyboard_latin
    private var to_down = false

    private fun korean_to_initial_cons(i: Char): Int
    {
        when (i)
        {
            'ㄱ' -> { return 0}
            'ㄲ' -> { return 1 }
            'ㄴ' -> { return 2 }
            'ㄷ' -> { return 3 }
            'ㄸ' -> { return 4 }
            'ㄹ' -> { return 5 }
            'ㅁ' -> { return 6 }
            'ㅂ' -> { return 7 }
            'ㅃ' -> { return 8 }
            'ㅅ' -> { return 9 }
            'ㅆ' -> { return 10 }
            'ㅇ' -> { return 11 }
            'ㅈ' -> { return 12 }
            'ㅉ' -> { return 13 }
            'ㅊ' -> { return 14 }
            'ㅋ' -> { return 15 }
            'ㅌ' -> { return 16 }
            'ㅍ' -> { return 17 }
            'ㅎ' -> { return 18 }
            else -> { return 0 }
        }
    }
    private fun korean_final_to_initial(i: Int): Int
    {
        when(i)
        {
            1 -> { return 0 }
            2 -> { return 1 }
            4 -> { return 2 }
            7 -> { return 3 }
            8 -> { return 5 }
            16 -> { return 6 }
            17 -> { return 7 }
            19 -> { return 9 }
            20 -> { return 10 }
            21 -> { return 11 }
            22 -> { return 12 }
            23 -> { return 24 }
            24 -> { return 15 }
            25 -> { return 16 }
            26 -> { return 17 }
            27 -> { return 18 }
            else -> { return 0 }
        }
    }
    private fun korean_to_final_cons(i: Char): Int
    {
        when (i)
        {
            'ㄱ' -> { return 1 }
            'ㄲ' -> { return 2 }
            'ㄳ' -> { return 3 }
            'ㄴ' -> { return 4 }
            'ㄵ' -> { return 5 }
            'ㄶ' -> { return 6 }
            'ㄷ' -> { return 7 }
            'ㄹ' -> { return 8 }
            'ㄺ' -> { return 9 }
            'ㄻ' -> { return 10 }
            'ㄼ' -> { return 11 }
            'ㄽ' -> { return 12 }
            'ㄾ' -> { return 13 }
            'ㄿ' -> { return 14 }
            'ㅀ' -> { return 15 }
            'ㅁ' -> { return 16 }
            'ㅂ' -> { return 17 }
            'ㅄ' -> { return 18 }
            'ㅅ' -> { return 19 }
            'ㅆ' -> { return 20 }
            'ㅇ' -> { return 21 }
            'ㅈ' -> { return 22 }
            'ㅊ' -> { return 23 }
            'ㅋ' -> { return 24 }
            'ㅌ' -> { return 25 }
            'ㅍ' -> { return 26 }
            'ㅎ' -> { return 27 }
            else -> { return 0 }
        }
    }
    private fun korean_combine_final_cons(i: Int, j:Int):Int
    {
        when(i)
        {
            1 ->
            {
                when(j)
                {
                    19 -> return 3
                    else -> return 0
                }
            }
            4 ->
            {
                when(j)
                {
                    22 -> return 5
                    27 -> return 6
                    else -> return 0
                }
            }
            8 ->
            {
                when(j)
                {
                    1 -> return 9
                    16 -> return 10
                    17 -> return 11
                    19 -> return 12
                    25 -> return 13
                    26 -> return 14
                    27 -> return 15
                    else -> return 0
                }
            }
            17 ->
            {
                when(j)
                {
                    19 -> return 18
                    else -> return 0
                }
            }
            else -> { return 0 }
        }
    }
    private fun korean_split_final_cons(i: Int):Array<Int>
    {
        when(i)
        {
            2 -> return arrayOf(1,0)
            3 -> return arrayOf(1,9)
            5 -> return arrayOf(4,12)
            6 -> return arrayOf(4,18)
            9 -> return arrayOf(8,0)
            10 -> return arrayOf(8,6)
            11 -> return arrayOf(8,7)
            12 -> return arrayOf(8,9)
            13 -> return arrayOf(8,16)
            14 -> return arrayOf(8,17)
            15 -> return arrayOf(8,18)
            18 -> return arrayOf(17,9)
            20 -> return arrayOf(19,9)
            else -> return arrayOf(0,0)
        }
    }
    private fun korean_to_vowel(i: Char): Int
    {
        return i.code - 'ㅏ'.code
    }
    private fun korean_combine_vowel(i: Int, j: Int): Int
    {
        when (i)
        {
            8 ->
            {
                when (j)
                {
                    0 -> {return 9}
                    1 -> {return 10}
                    20 -> {return 11}
                    else -> {return -1}
                }
            }
            13 ->
            {
                when (j)
                {
                    4 -> {return 14}
                    5 -> {return 15}
                    20 -> {return 16}
                    else -> {return -1}
                }
            }
            18 ->
            {
                when (j)
                {
                    20 -> {return 19}
                    else -> {return -1}
                }
            }
            else -> { return -1 }
        }
    }

    override fun onPress(i: Int) {}
    override fun onRelease(i: Int) {}
    override fun onText(charSequence: CharSequence) {
        val inputConnection = currentInputConnection
        if (inputConnection != null) {
            var code = charSequence.toString()
            if(caps or lock) {
                code.replace("ß","ẞ")
                code.replace("ı","İ")
                if(!lock)
                {
                    caps = !caps
                    keyboard!!.isShifted = caps
                    keyboardView!!.invalidateAllKeys()
                }
                code = code.uppercase()
            }
            inputConnection.commitText(code, 1)
            if(to_down)
            {
                keyboard = Keyboard(this, R.xml.keys_layout, KeyboardState)
                keyboardView!!.keyboard = keyboard
                keyboardView!!.setOnKeyboardActionListener(this)
                keyboard!!.setShifted(false)
                caps = false
                to_down = false
            }
            if (KeyboardState == R.integer.keyboard_korean)
            {
                val total_text = inputConnection.getTextBeforeCursor(2, 0)
                val first = total_text?.first()
                val last = total_text?.last()
                if ((12593 <= first?.code!! && first?.code!! <= 12622)
                    && (12623 <= last?.code!! && last?.code!! <= 12643))
                {
                    inputConnection.deleteSurroundingText(2,0)
                    inputConnection.commitText((korean_to_initial_cons(first)*588 + korean_to_vowel(last)*28 + 44032).toChar().toString(), 1)
                }
                if ((44032 <= first?.code!! && first?.code!! <= 55203) && ((first?.code!! - 44032)%28 == 0)
                    && (12623 <= last?.code!! && last?.code!! <= 12643))
                {
                    val cons = ((first?.code!! - 44032)/588)
                    val combivowel = korean_combine_vowel(((first?.code!! - 44032)/28)%21, korean_to_vowel(last))
                    if(combivowel != -1)
                    {
                        inputConnection.deleteSurroundingText(2, 0)
                        inputConnection.commitText((cons*588 + combivowel*28 + 44032).toChar().toString(), 1)
                    }
                }
                if ((44032 <= first?.code!! && first?.code!! <= 55203)
                    && (12593 <= last?.code!! && last?.code!! <= 12622))
                {
                    if((korean_to_final_cons(last) != 0) && ((first?.code!! - 44032)%28 == 0))
                    {
                        inputConnection.deleteSurroundingText(2, 0)
                        inputConnection.commitText((first.code + korean_to_final_cons(last)).toChar().toString(), 1)
                    }
                    else if (korean_to_final_cons(last) != 0 && korean_combine_final_cons((first.code - 44032) % 28, korean_to_final_cons(last)) != 0)
                    {
                        inputConnection.deleteSurroundingText(2, 0)
                        inputConnection.commitText((((first.code - 44032)/28)*28 + korean_combine_final_cons((first.code - 44032) % 28, korean_to_final_cons(last)) + 44032).toChar().toString(), 1)
                    }
                }
                if ((44032 <= first?.code!! && first?.code!! <= 55203)
                    && (12623 <= last?.code!! && last?.code!! <= 12643))
                {
                    if (korean_split_final_cons((first.code - 44032)%28)[0] != 0)
                    {
                        inputConnection.deleteSurroundingText(2, 0)
                        inputConnection.commitText((((first.code - 44032) / 28) * 28 + korean_split_final_cons((first.code - 44032) % 28)[0] + 44032).toChar().toString() + (korean_split_final_cons((first.code - 44032) % 28)[1] * 588 + korean_to_vowel(last) * 28 + 44032).toChar().toString(), 1)
                    }
                    else if ((first.code - 44032)%28 != 0)
                    {
                        inputConnection.deleteSurroundingText(2, 0)
                        inputConnection.commitText((((first.code - 44032) / 28) * 28 +44032).toChar().toString() + (korean_final_to_initial((first.code - 44032)%28)*588 + korean_to_vowel(last)*28+44032).toChar().toString() ,1)
                    }
                }
            }
        }
    }
    override fun swipeLeft() {}
    override fun swipeRight() {}
    override fun swipeDown() {}
    override fun swipeUp() {}


    override fun onCreateInputView(): View? {
        caps = false
        lock = false
        keyboardView = layoutInflater.inflate(R.layout.keyboard_view, null) as KeyboardView
        keyboard = Keyboard(this, R.xml.keys_layout, KeyboardState)
        keyboardView!!.setPopupParent(keyboardView)
        keyboardView!!.keyboard = keyboard
        keyboardView!!.setOnKeyboardActionListener(this)
        return keyboardView
    }

    override fun onWindowHidden() {
        keyboardView?.closing()
        super.onWindowHidden()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
        val inputConnection = currentInputConnection
        if (inputConnection != null)
        {
            when (primaryCode)
            {
                Keyboard.KEYCODE_DELETE ->
                {
                    val selectedText = inputConnection.getSelectedText(0)
                    if (TextUtils.isEmpty(selectedText))
                    {
                        try
                        {
                            val text_last = inputConnection.getTextBeforeCursor(1, 0)
                            if (text_last?.first()?.code!! < 56320)
                            {
                                inputConnection.deleteSurroundingText(1, 0)
                            }
                            else
                            {
                                inputConnection.deleteSurroundingText(2, 0)
                            }
                        }
                        catch (e: Exception) {}
                    } else
                    {
                        inputConnection.commitText("", 1)
                    }
                    keyboard!!.isShifted = caps
                    keyboardView!!.invalidateAllKeys()
                }
                Keyboard.KEYCODE_SHIFT -> {
                    if(caps && !lock)
                    {
                        lock = !lock
                    }
                    else
                    {
                        caps = !caps
                        lock = false
                        keyboard!!.isShifted = caps
                        keyboardView!!.invalidateAllKeys()
                    }
                }
                Keyboard.KEYCODE_DONE -> inputConnection.sendKeyEvent(
                    KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER)
                )
                10 -> {
                    keyboard = Keyboard(this, R.xml.keys_layout, KeyboardState)
                    keyboardView!!.keyboard = keyboard
                    keyboardView!!.setOnKeyboardActionListener(this)
                    keyboard!!.setShifted(false)
                    caps = false
                    lock = false
                }
                0 -> {
                    keyboard = Keyboard(this, R.xml.keys_layout, R.integer.keyboard_latin)
                    KeyboardState = R.integer.keyboard_latin
                    keyboardView!!.keyboard = keyboard
                    keyboardView!!.setOnKeyboardActionListener(this)
                    keyboard!!.setShifted(false)
                    caps = false
                    lock = false
                }
                1 -> {
                    keyboard = Keyboard(this, R.xml.keys_layout, R.integer.keyboard_symbols1)
                    keyboardView!!.keyboard = keyboard
                    keyboardView!!.setOnKeyboardActionListener(this)
                    keyboard!!.setShifted(false)
                    caps = false
                    lock = false
                }
                2 -> {
                    keyboard = Keyboard(this, R.xml.keys_layout, R.integer.keyboard_symbols2)
                    keyboardView!!.keyboard = keyboard
                    keyboardView!!.setOnKeyboardActionListener(this)
                    keyboard!!.setShifted(false)
                    caps = false
                    lock = false
                }
                3 -> {
                    keyboard = Keyboard(this, R.xml.keys_layout, R.integer.keyboard_symbols3)
                    keyboardView!!.keyboard = keyboard
                    keyboardView!!.setOnKeyboardActionListener(this)
                    keyboard!!.setShifted(false)
                    caps = false
                    lock = false
                }
                4 -> {
                    keyboard = Keyboard(this, R.xml.keys_layout, R.integer.keyboard_math)
                    keyboardView!!.keyboard = keyboard
                    keyboardView!!.setOnKeyboardActionListener(this)
                    keyboard!!.setShifted(false)
                    caps = false
                    lock = false
                }
                5 -> {
                    keyboard = Keyboard(this, R.xml.keys_layout, R.integer.keyboard_diacritics)
                    keyboardView!!.keyboard = keyboard
                    keyboardView!!.setOnKeyboardActionListener(this)
                    keyboard!!.setShifted(false)
                    caps = false
                    lock = false
                }
                6 -> {
                    keyboard = Keyboard(this, R.xml.keys_layout, R.integer.keyboard_greek)
                    KeyboardState = R.integer.keyboard_greek
                    keyboardView!!.keyboard = keyboard
                    keyboardView!!.setOnKeyboardActionListener(this)
                    keyboard!!.setShifted(false)
                    caps = false
                    lock = false
                }
                7 -> {
                    keyboard = Keyboard(this, R.xml.keys_layout, R.integer.keyboard_cyrillic)
                    KeyboardState = R.integer.keyboard_cyrillic
                    keyboardView!!.keyboard = keyboard
                    keyboardView!!.setOnKeyboardActionListener(this)
                    keyboard!!.setShifted(false)
                    caps = false
                    lock = false
                }
                8 -> {
                    keyboard = Keyboard(this, R.xml.keys_layout, R.integer.keyboard_hebrew)
                    KeyboardState = R.integer.keyboard_hebrew
                    keyboardView!!.keyboard = keyboard
                    keyboardView!!.setOnKeyboardActionListener(this)
                    keyboard!!.setShifted(false)
                    caps = false
                    lock = false
                }
                -9 -> {
                    keyboard = Keyboard(this, R.xml.keys_layout, R.integer.keyboard_korean)
                    KeyboardState = R.integer.keyboard_korean
                    keyboardView!!.keyboard = keyboard
                    keyboardView!!.setOnKeyboardActionListener(this)
                    keyboard!!.setShifted(false)
                    caps = false
                    lock = false
                }
                -10 -> {
                    keyboard = Keyboard(this, R.xml.keys_layout, R.integer.keyboard_korean_alt)
                    keyboardView!!.keyboard = keyboard
                    keyboardView!!.setOnKeyboardActionListener(this)
                    keyboard!!.setShifted(false)
                    caps = false
                    lock = false
                    to_down = true
                }
                -11 -> {
                    keyboard = Keyboard(this, R.xml.keys_layout, R.integer.keyboard_arabic)
                    KeyboardState = R.integer.keyboard_arabic
                    keyboardView!!.keyboard = keyboard
                    keyboardView!!.setOnKeyboardActionListener(this)
                    keyboard!!.setShifted(false)
                    caps = false
                    lock = false
                }
                12 -> {
                    keyboard = Keyboard(this, R.xml.keys_layout, R.integer.keyboard_diacritics2)
                    keyboardView!!.keyboard = keyboard
                    keyboardView!!.setOnKeyboardActionListener(this)
                    keyboard!!.setShifted(false)
                    caps = false
                    lock = false
                }
                -13 -> {
                    keyboard = Keyboard(this, R.xml.keys_layout, R.integer.keyboard_georgian_mkhedruli)
                    KeyboardState = R.integer.keyboard_georgian_mkhedruli
                    keyboardView!!.keyboard = keyboard
                    keyboardView!!.setOnKeyboardActionListener(this)
                    keyboard!!.setShifted(false)
                    caps = false
                    lock = false
                }
                14 -> {
                    keyboard = Keyboard(this, R.xml.keys_layout, R.integer.keyboard_emoji1)
                    keyboardView!!.keyboard = keyboard
                    keyboardView!!.setOnKeyboardActionListener(this)
                    keyboard!!.setShifted(false)
                    caps = false
                    lock = false
                }
                15 -> {
                    keyboard = Keyboard(this, R.xml.keys_layout, R.integer.keyboard_ipa)
                    KeyboardState = R.integer.keyboard_ipa
                    keyboardView!!.keyboard = keyboard
                    keyboardView!!.setOnKeyboardActionListener(this)
                    keyboard!!.setShifted(false)
                    caps = false
                    lock = false
                }
                -16 -> {
                    keyboard = Keyboard(this, R.xml.keys_layout, R.integer.keyboard_hiragana)
                    KeyboardState = R.integer.keyboard_hiragana
                    keyboardView!!.keyboard = keyboard
                    keyboardView!!.setOnKeyboardActionListener(this)
                    keyboard!!.setShifted(false)
                    caps = false
                    lock = false
                }
                -17 -> {
                    keyboard = Keyboard(this, R.xml.keys_layout, R.integer.keyboard_katakana)
                    KeyboardState = R.integer.keyboard_katakana
                    keyboardView!!.keyboard = keyboard
                    keyboardView!!.setOnKeyboardActionListener(this)
                    keyboard!!.setShifted(false)
                    caps = false
                    lock = false
                }
                -18 -> {
                    keyboard = Keyboard(this, R.xml.keys_layout, R.integer.keyboard_ethiopic)
                    KeyboardState = R.integer.keyboard_ethiopic
                    keyboardView!!.keyboard = keyboard
                    keyboardView!!.setOnKeyboardActionListener(this)
                    keyboard!!.setShifted(false)
                    caps = false
                    lock = false
                }
                -19 -> {
                    keyboard = Keyboard(this, R.xml.keys_layout, R.integer.keyboard_armenian)
                    KeyboardState = R.integer.keyboard_armenian
                    keyboardView!!.keyboard = keyboard
                    keyboardView!!.setOnKeyboardActionListener(this)
                    keyboard!!.setShifted(false)
                    caps = false
                    lock = false
                }
                -20 -> {
                    keyboard = Keyboard(this, R.xml.keys_layout, R.integer.keyboard_georgian_asomtavruli)
                    KeyboardState = R.integer.keyboard_georgian_asomtavruli
                    keyboardView!!.keyboard = keyboard
                    keyboardView!!.setOnKeyboardActionListener(this)
                    keyboard!!.setShifted(false)
                    caps = false
                    lock = false
                }
                -21 -> {
                    keyboard = Keyboard(this, R.xml.keys_layout, R.integer.keyboard_georgian_nuskhuri)
                    KeyboardState = R.integer.keyboard_georgian_nuskhuri
                    keyboardView!!.keyboard = keyboard
                    keyboardView!!.setOnKeyboardActionListener(this)
                    keyboard!!.setShifted(false)
                    caps = false
                    lock = false
                }
                -22 -> {
                    keyboard = Keyboard(this, R.xml.keys_layout, R.integer.keyboard_tibetan)
                    KeyboardState = R.integer.keyboard_tibetan
                    keyboardView!!.keyboard = keyboard
                    keyboardView!!.setOnKeyboardActionListener(this)
                    keyboard!!.setShifted(false)
                    caps = false
                    lock = false
                }
                -23 -> {
                    keyboard = Keyboard(this, R.xml.keys_layout, R.integer.keyboard_tibetan_sub)
                    keyboardView!!.keyboard = keyboard
                    keyboardView!!.setOnKeyboardActionListener(this)
                    keyboard!!.setShifted(false)
                    caps = false
                    lock = false
                    to_down = true
                }
                -80 -> {
                    keyboard = Keyboard(this, R.xml.keys_layout, R.integer.keyboard_fraktur)
                    KeyboardState = R.integer.keyboard_fraktur
                    keyboardView!!.keyboard = keyboard
                    keyboardView!!.setOnKeyboardActionListener(this)
                    keyboard!!.setShifted(false)
                    caps = false
                    lock = false
                }
                -81 -> {
                    keyboard = Keyboard(this, R.xml.keys_layout, R.integer.keyboard_fraktur_cap)
                    keyboardView!!.keyboard = keyboard
                    keyboardView!!.setOnKeyboardActionListener(this)
                    keyboard!!.setShifted(false)
                    caps = false
                    lock = false
                    to_down = true
                }
                -82 -> {
                    keyboard = Keyboard(this, R.xml.keys_layout, R.integer.keyboard_dubstruck)
                    KeyboardState = R.integer.keyboard_dubstruck
                    keyboardView!!.keyboard = keyboard
                    keyboardView!!.setOnKeyboardActionListener(this)
                    keyboard!!.setShifted(false)
                    caps = false
                    lock = false
                }
                -83 -> {
                    keyboard = Keyboard(this, R.xml.keys_layout, R.integer.keyboard_dubstruck_cap)
                    keyboardView!!.keyboard = keyboard
                    keyboardView!!.setOnKeyboardActionListener(this)
                    keyboard!!.setShifted(false)
                    caps = false
                    lock = false
                    to_down = true
                }
                -84 -> {
                    keyboard = Keyboard(this, R.xml.keys_layout, R.integer.keyboard_cursive)
                    KeyboardState = R.integer.keyboard_cursive
                    keyboardView!!.keyboard = keyboard
                    keyboardView!!.setOnKeyboardActionListener(this)
                    keyboard!!.setShifted(false)
                    caps = false
                    lock = false
                }
                -85 -> {
                    keyboard = Keyboard(this, R.xml.keys_layout, R.integer.keyboard_cursive_cap)
                    keyboardView!!.keyboard = keyboard
                    keyboardView!!.setOnKeyboardActionListener(this)
                    keyboard!!.setShifted(false)
                    caps = false
                    lock = false
                    to_down = true
                }
                -86 -> {
                    keyboard = Keyboard(this, R.xml.keys_layout, R.integer.keyboard_it)
                    KeyboardState = R.integer.keyboard_it
                    keyboardView!!.keyboard = keyboard
                    keyboardView!!.setOnKeyboardActionListener(this)
                    keyboard!!.setShifted(false)
                    caps = false
                    lock = false
                }
                -87 -> {
                    keyboard = Keyboard(this, R.xml.keys_layout, R.integer.keyboard_it_cap)
                    keyboardView!!.keyboard = keyboard
                    keyboardView!!.setOnKeyboardActionListener(this)
                    keyboard!!.setShifted(false)
                    caps = false
                    lock = false
                    to_down = true
                }
                -88 -> {
                    keyboard = Keyboard(this, R.xml.keys_layout, R.integer.keyboard_mono)
                    KeyboardState = R.integer.keyboard_mono
                    keyboardView!!.keyboard = keyboard
                    keyboardView!!.setOnKeyboardActionListener(this)
                    keyboard!!.setShifted(false)
                    caps = false
                    lock = false
                }
                -89 -> {
                    keyboard = Keyboard(this, R.xml.keys_layout, R.integer.keyboard_mono_cap)
                    keyboardView!!.keyboard = keyboard
                    keyboardView!!.setOnKeyboardActionListener(this)
                    keyboard!!.setShifted(false)
                    caps = false
                    lock = false
                    to_down = true
                }
                -90 -> {
                    keyboard = Keyboard(this, R.xml.keys_layout, R.integer.keyboard_superscript)
                    KeyboardState = R.integer.keyboard_superscript
                    keyboardView!!.keyboard = keyboard
                    keyboardView!!.setOnKeyboardActionListener(this)
                    keyboard!!.setShifted(false)
                    caps = false
                    lock = false
                }
                -91 -> {
                    keyboard = Keyboard(this, R.xml.keys_layout, R.integer.keyboard_subscript)
                    KeyboardState = R.integer.keyboard_subscript
                    keyboardView!!.keyboard = keyboard
                    keyboardView!!.setOnKeyboardActionListener(this)
                    keyboard!!.setShifted(false)
                    caps = false
                    lock = false
                }
                -92 -> {
                    keyboard = Keyboard(this, R.xml.keys_layout, R.integer.keyboard_bold)
                    KeyboardState = R.integer.keyboard_bold
                    keyboardView!!.keyboard = keyboard
                    keyboardView!!.setOnKeyboardActionListener(this)
                    keyboard!!.setShifted(false)
                    caps = false
                    lock = false
                }
                -93 -> {
                    keyboard = Keyboard(this, R.xml.keys_layout, R.integer.keyboard_bold_cap)
                    keyboardView!!.keyboard = keyboard
                    keyboardView!!.setOnKeyboardActionListener(this)
                    keyboard!!.setShifted(false)
                    caps = false
                    lock = false
                    to_down = true
                }
                -94 -> {
                    keyboard = Keyboard(this, R.xml.keys_layout, R.integer.keyboard_full)
                    KeyboardState = R.integer.keyboard_full
                    keyboardView!!.keyboard = keyboard
                    keyboardView!!.setOnKeyboardActionListener(this)
                    keyboard!!.setShifted(false)
                    caps = false
                    lock = false
                }
                -95 -> {
                    keyboard = Keyboard(this, R.xml.keys_layout, R.integer.keyboard_full_cap)
                    keyboardView!!.keyboard = keyboard
                    keyboardView!!.setOnKeyboardActionListener(this)
                    keyboard!!.setShifted(false)
                    caps = false
                    lock = false
                    to_down = true
                }
                -96 -> {
                    keyboard = Keyboard(this, R.xml.keys_layout, R.integer.keyboard_sans)
                    KeyboardState = R.integer.keyboard_sans
                    keyboardView!!.keyboard = keyboard
                    keyboardView!!.setOnKeyboardActionListener(this)
                    keyboard!!.setShifted(false)
                    caps = false
                    lock = false
                }
                -97 -> {
                    keyboard = Keyboard(this, R.xml.keys_layout, R.integer.keyboard_sans_cap)
                    keyboardView!!.keyboard = keyboard
                    keyboardView!!.setOnKeyboardActionListener(this)
                    keyboard!!.setShifted(false)
                    caps = false
                    lock = false
                    to_down = true
                }
                -98 -> {
                    keyboard = Keyboard(this, R.xml.keys_layout, R.integer.keyboard_superscript_cap)
                    KeyboardState = R.integer.keyboard_superscript_cap
                    keyboardView!!.keyboard = keyboard
                    keyboardView!!.setOnKeyboardActionListener(this)
                    keyboard!!.setShifted(false)
                    caps = false
                    lock = false
                }
                -99 -> {
                    keyboard = Keyboard(this, R.xml.keys_layout, R.integer.keyboard_menu)
                    keyboardView!!.keyboard = keyboard
                    keyboardView!!.setOnKeyboardActionListener(this)
                    keyboard!!.setShifted(false)
                    caps = false
                    lock = false
                }
                -101 -> {
                    keyboard = Keyboard(this, R.xml.keys_layout, R.integer.keyboard_full_symb)
                    keyboardView!!.keyboard = keyboard
                    keyboardView!!.setOnKeyboardActionListener(this)
                    keyboard!!.setShifted(false)
                    caps = false
                    lock = false
                }
                -102 -> {
                    keyboard = Keyboard(this, R.xml.keys_layout, R.integer.keyboard_itgr)
                    KeyboardState = R.integer.keyboard_itgr
                    keyboardView!!.keyboard = keyboard
                    keyboardView!!.setOnKeyboardActionListener(this)
                    keyboard!!.setShifted(false)
                    caps = false
                    lock = false
                }
                -103 -> {
                    keyboard = Keyboard(this, R.xml.keys_layout, R.integer.keyboard_itgr_cap)
                    keyboardView!!.keyboard = keyboard
                    keyboardView!!.setOnKeyboardActionListener(this)
                    keyboard!!.setShifted(false)
                    caps = false
                    lock = false
                    to_down = true
                }
                else -> {
                    var code = primaryCode.toChar().toString()
                    if(caps or lock) {
                        if(code =="ß")
                        {
                            code = "ẞ"
                        }
                        if(code =="ı")
                        {
                            code = "İ"
                        }
                        if(!lock)
                        {
                            caps = !caps
                            keyboard!!.isShifted = caps
                            keyboardView!!.invalidateAllKeys()
                        }
                        code = code.uppercase()
                    }
                    inputConnection.commitText(code, 1)
                }
            }
        }
    }
}