package wee.digital.widget.custom

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.icu.text.SimpleDateFormat
import android.os.*
import android.text.InputFilter
import android.text.InputType
import android.util.AttributeSet
import android.util.TypedValue
import android.view.*
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.ContextCompat
import androidx.viewbinding.ViewBinding
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import wee.digital.widget.R
import wee.digital.widget.custom.base.AppCustomView
import wee.digital.widget.databinding.InputBinding
import wee.digital.widget.extension.*
import java.util.*

class InputView : AppCustomView<InputBinding>,
    SimpleMotionTransitionListener,
    OnFocusChangeListener {

    private val colorFocused get() = ContextCompat.getColor(context, R.color.colorInputFocused)
    private val colorUnFocus get() = ContextCompat.getColor(context, R.color.colorInputUnFocused)
    private val colorError get() = ContextCompat.getColor(context, R.color.color_error)
    private val colorHint get() = ContextCompat.getColor(context, R.color.colorInputHint)

    constructor(context: Context, attrs: AttributeSet? = null) : super(context, attrs)

    /**
     * Init
     */
    override fun inflating(): (LayoutInflater, ViewGroup?, Boolean) -> ViewBinding {
        return InputBinding::inflate
    }

    override fun onInitialize(context: Context, types: TypedArray) {
        title = types.title
        onIconInitialize(types)
        onEditTextInitialize(vb.inputEditText, types)
        vb.inputViewLayout.addTransitionListener(this)
        noneOptional = types.getBoolean(R.styleable.AppCustomView_noneOptional, false)
        if (context is AppCompatActivity) post {
            showNoneOptional()
            updateUiOnTextChanged()
        }
    }

    private fun onIconInitialize(types: TypedArray) {
        val color = types.getColor(R.styleable.AppCustomView_android_tint, colorHint)
        if (color != -1) {
            vb.inputImageViewIcon.setColorFilter(color)
        }
        src = types.srcRes
    }

    fun addButtonClick(listener: ((View?) -> Unit)? = null) {
        vb.inputImageViewEnd.addClickListener(listener)
    }

    private fun onEditTextInitialize(it: AppCompatEditText, types: TypedArray) {
        it.onFocusChangeListener = this
        it.paintFlags = it.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
        it.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            types.getDimension(
                R.styleable.AppCustomView_android_textSize,
                getPixels(R.dimen.textSize15)
            )
        )
        it.maxLines = 1

        // Text filter
        val sFilters = arrayListOf<InputFilter>()

        val textAllCaps = types.getBoolean(R.styleable.AppCustomView_android_textAllCaps, false)
        if (textAllCaps) sFilters.add(InputFilter.AllCaps())

        val sMaxLength = types.getInt(R.styleable.AppCustomView_android_maxLength, 256)
        sFilters.add(InputFilter.LengthFilter(sMaxLength))

        val array = arrayOfNulls<InputFilter>(sFilters.size)
        it.filters = sFilters.toArray(array)

        // Input type
        val attrInputType = types.getInt(
            R.styleable.AppCustomView_android_inputType,
            EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        )
        when (attrInputType) {
            EditorInfo.TYPE_NULL -> {
                disableFocus()
            }
            EditorInfo.TYPE_CLASS_NUMBER -> {
                editText.addFilter(DIGIT_FILTER)
                it.inputType = attrInputType
            }
            else -> {
                it.inputType =
                    attrInputType or EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS or EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING
            }
        }

        it.maxLines = types.getInt(R.styleable.AppCustomView_android_maxLines, 1)

        // Ime option
        val imeOption = types.getInt(R.styleable.AppCustomView_android_imeOptions, -1)
        if (imeOption != -1) it.imeOptions = imeOption

        it.privateImeOptions = "nm,com.google.android.inputmethod.latin.noMicrophoneKey"

        // Gesture
        it.setOnLongClickListener {
            return@setOnLongClickListener true
        }
        it.setTextIsSelectable(false)
        it.customSelectionActionModeCallback = object : ActionMode.Callback {
            override fun onPrepareActionMode(mode: ActionMode, menu: Menu) = false
            override fun onDestroyActionMode(mode: ActionMode) {}
            override fun onCreateActionMode(mode: ActionMode, menu: Menu) = false
            override fun onActionItemClicked(mode: ActionMode, item: MenuItem) = false
        }
        it.isLongClickable = false
        it.setOnCreateContextMenuListener { menu, _, _ -> menu.clear() }
        it.setText(types.text)
        it.addTextChangedListener(object : SimpleTextWatcher() {
            override fun onTextChanged(s: String) {
                if (isTextSilent) {
                    return
                }
                if (hasError) {
                    error = null
                }
                if (hasCashWatcher) {
                    isTextSilent = true
                    val moneyText = s.moneyFormat("")
                    editText.setTextSilently(moneyText)
                    isTextSilent = false
                    onTextChanged?.invoke(moneyText)
                } else {
                    onTextChanged?.invoke(s)
                }
                updateUiClearButton()
                showNoneOptional()
            }
        })
    }

    /**
     * Motion
     */
    override fun onTransitionCompleted(layout: MotionLayout, currentId: Int) {
        when (currentId) {
            R.id.focused -> {
                vb.inputTextViewTitle.setBackgroundColor(parentBackgroundColor)
            }
        }
    }

    private fun setMotionState(id: Int, animated: Boolean = true) {
        if (vb.inputViewLayout.currentState == id) return
        when {
            animated -> {
                vb.inputViewLayout.transitionToState(id)
            }
            id == R.id.unfocused -> {
                vb.inputViewLayout.setTransition(R.id.unfocused, R.id.focused)
            }
            id == R.id.focused -> {
                vb.inputViewLayout.setTransition(R.id.focused, R.id.unfocused)
            }
        }
    }

    /**
     * [View] implements
     */
    override fun setEnabled(enabled: Boolean) {
        val oldEnable = isEnabled
        super.setEnabled(enabled)
        if (enabled) {
            enableFocus()
        } else {
            disableFocus()
        }
        if (oldEnable != enabled) {
            updateUiOnTextChanged()
        }
    }

    override fun setOnClickListener(listener: OnClickListener?) {
        if (null == listener) {
            enableFocus()
            editText.setOnClickListener(null)
        } else {
            vb.inputImageViewEnd.setImageResource(R.drawable.ic_drop_down)
            disableFocus()
            isClickable = true
            editText.addClickListener {
                listener.onClick(this)
            }
        }
    }

    override fun performClick(): Boolean {
        return editText.performClick()
    }

    override fun onDetachedFromWindow() {
        vb.inputViewLayout.clearAnimation()
        onFocusChange?.clear()
        super.onDetachedFromWindow()
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        superState?.let {
            val state = SaveState(superState)
            state.text = text
            return state
        } ?: run {
            return superState
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)
        when (state) {
            is SaveState -> {
                super.onRestoreInstanceState(state.superState)
                Handler(Looper.getMainLooper()).post {
                    val s = state.text
                    text = s
                }
            }
        }
    }

    /**
     * [InputView] properties
     */
    private val editText: EditText get() = vb.inputEditText
    private val parentBackgroundColor: Int get() = (parent as? View)?.backgroundColor ?: Color.WHITE
    private var tempHasFocus: Boolean? = null
    private var tempHasText: Boolean? = null
    private var tempHasError: Boolean? = null
    private var tempEnabled: Boolean? = null
    private var isTextSilent: Boolean = false
    val isTextEmpty: Boolean get() = text.isNullOrEmpty()
    val hasError: Boolean get() = !mError.isNullOrEmpty()
    val textLength: Int get() = text?.length ?: 0

    var text: String?
        get() {
            isTextSilent = true
            val s = editText.text?.toString()?.trimText
            isTextSilent = false
            return s
        }
        set(value) {
            isTextSilent = true
            editText.setText(value)
            updateUiClearButton()
            onFocusChange(null, hasFocus())
            isTextSilent = false
        }

    val trimText: String?
        get() {
            isTextSilent = true
            val s = editText.trimText
            isTextSilent = false
            return s
        }

    var title: String?
        get() = vb.inputTextViewTitle.text?.toString()
        set(value) {
            vb.inputTextViewTitle.text = value
        }

    var prefix: String?
        get() = vb.inputTextViewPrefix.text?.toString()
        set(value) {
            vb.inputTextViewPrefix.text = value
        }

    private var mError: String? = null

    var error: String?
        get() = mError
        set(value) {
            mError = value
            vb.inputTextViewError.isGone(value.isNullOrEmpty())
            vb.inputTextViewError.setHyperText(value)
            updateUiOnTextChanged()
        }

    var src: Int = 0
        set(value) {
            val isGone = value <= 0
            vb.inputImageViewIcon.isGone(isGone)
            vb.inputImageViewIcon.setImageResource(value)
        }

    var drawableEnd: Int = 0
        set(value) {
            if (value != field) {
                vb.inputImageViewEnd.setImageResource(value)
                field = value
            }
        }

    var inputType: Int = 0
        set(value) {
            editText.inputType = value
        }

    var maxLengths: Int = 0
        set(value) {
            val sFilters = arrayListOf<InputFilter>()
            sFilters.add(InputFilter.LengthFilter(value))
            val array = arrayOfNulls<InputFilter>(sFilters.size)
            editText.filters = sFilters.toArray(array)
        }

    var hasCashWatcher: Boolean = false
        set(value) {
            if (value) {
                inputType = InputType.TYPE_CLASS_NUMBER
            }
            field = value
        }

    /**
     * On text change
     */
    var onTextChanged: ((String) -> Unit)? = null

    /**
     * Clear button
     */
    var hasClearButton: Boolean = false
        set(value) {
            updateUiClearButton()
            if (value) {
                vb.inputImageViewEnd.addClickListener {
                    vb.inputTextViewError.text = null
                    text = null
                }
            }
            field = value
        }

    private fun updateUiClearButton() {
        if (hasClearButton) {
            drawableEnd = if (text.isNullOrEmpty()) 0 else R.drawable.ic_close_flat
        }
    }

    /**
     *  Optional
     */
    var noneOptional: Boolean = false

    private fun showNoneOptional() {
        if (noneOptional && text.isNullOrEmpty() && (!hasFocus() || hasOnClickListeners())) {
            vb.viewNoneOptional.setBackgroundResource(R.drawable.drw_oval)
        } else {
            vb.viewNoneOptional.setBackgroundResource(0)
        }
    }

    /**
     * Focus/Focusable
     */
    var onLostFocus: ((String?) -> Unit)? = null

    private var lostFocusText: String? = null

    private var onFocusChange: MutableList<(Boolean) -> Unit>? = null

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        if (hasFocus == tempHasFocus) return
        onFocusChange?.forEach { it(hasFocus) }
        updateUiOnTextChanged(hasFocus)
        if (!hasFocus) {
            lostFocusText = text
            onLostFocus?.invoke(text)
        }
    }

    override fun hasFocusable(): Boolean {
        return false
    }

    override fun isFocused(): Boolean {
        return false
    }

    override fun hasFocus(): Boolean {
        return editText.hasFocus()
    }

    override fun clearFocus() {
        editText.clearFocus()
        hideKeyboard()
    }

    override fun requestFocus(direction: Int, previouslyFocusedRect: Rect?): Boolean {
        editText.post {
            if (!editText.isFocused) {
                editText.requestFocus(FOCUS_DOWN)
                showKeyboard()
            }
        }
        return true
    }

    fun addOnFocusChangeListener(block: (Boolean) -> Unit) {
        if (onFocusChange == null) onFocusChange = mutableListOf()
        onFocusChange?.add(block)
    }

    fun disableFocus() {
        editText.also {
            it.isFocusable = false
            it.isCursorVisible = false
        }
    }

    fun enableFocus() {
        editText.also {
            it.isFocusable = true
            it.isCursorVisible = true
            it.isEnabled = true
            it.isFocusableInTouchMode = true
        }
    }

    /**
     * Update ui on error, focus, enable, text change
     */
    var isViewSilent: Boolean = false

    fun updateUiOnTextChanged(hasFocus: Boolean = editText.hasFocus(), animated: Boolean = true) {
        if (isViewSilent) return
        val hasText = !text.isNullOrEmpty()
        val hasError = !error.isNullOrEmpty()
        if (tempHasFocus == hasFocus && tempHasText == hasText && tempHasError == hasError && tempEnabled == isEnabled) {
            return
        }
        tempHasFocus = hasFocus
        tempHasText = hasText
        tempHasError = hasError
        tempEnabled = isEnabled
        when {
            !isEnabled -> {
                vb.inputTextViewTitle.setBackgroundColor(0)
                vb.inputEditTextBackground.setBackgroundResource(R.drawable.drw_input_bg_disable)
                if (hasError) {
                    vb.inputTextViewTitle.setTextColor(colorError)
                    vb.inputImageViewIcon.tint(colorError)
                } else {
                    vb.inputTextViewTitle.setTextColor(colorHint)
                    vb.inputImageViewIcon.tint(colorHint)
                }
                setMotionState(R.id.unfocused, animated)
            }
            // isEnabled && hasFocus
            hasFocus -> {
                if (editText.isFocusable) editText.select()
                vb.inputEditTextBackground.setBackgroundResource(0)
                vb.inputTextViewTitle.setBackgroundColor(parentBackgroundColor)
                if (hasError) {
                    vb.inputTextViewTitle.setTextColor(colorError)
                    vb.inputImageViewIcon.tint(colorError)
                    vb.inputEditText.backgroundTint(colorError)
                } else {
                    vb.inputTextViewTitle.setTextColor(colorFocused)
                    vb.inputImageViewIcon.tint(colorFocused)
                    vb.inputEditText.backgroundTint(colorFocused)
                }
                setMotionState(R.id.focused, animated)
                showNoneOptional()
            }
            // isEnabled && !hasFocus && hasText
            hasText -> {
                vb.inputEditTextBackground.setBackgroundResource(0)
                vb.inputTextViewTitle.setBackgroundColor(parentBackgroundColor)
                if (hasError) {
                    vb.inputTextViewTitle.setTextColor(colorError)
                    vb.inputImageViewIcon.tint(colorError)
                    vb.inputEditText.backgroundTint(colorError)
                } else {
                    vb.inputTextViewTitle.setTextColor(colorHint)
                    vb.inputImageViewIcon.tint(colorHint)
                    vb.inputEditText.backgroundTint(colorHint)
                }
                setMotionState(R.id.focused)
                showNoneOptional()
            }
            // isEnabled && !hasFocus && !hasText
            else -> {
                vb.inputEditTextBackground.setBackgroundResource(R.drawable.drw_input_bg_enable)
                vb.inputTextViewTitle.setBackgroundColor(Color.TRANSPARENT)
                vb.inputTextViewTitle.setTextColor(colorHint)
                if (hasError) {
                    vb.inputImageViewIcon.tint(colorError)
                    vb.inputEditText.backgroundTint(colorError)
                } else {
                    vb.inputImageViewIcon.tint(colorHint)
                    vb.inputEditText.backgroundTint(colorUnFocus)
                }
                setMotionState(R.id.unfocused, animated)
                showNoneOptional()
            }
        }
    }

    /**
     * Utils
     */
    fun addActionDoneListener(block: (String?) -> Unit) {
        editText.addActionDoneListener(block)
    }

    fun addActionNextListener(block: (String?) -> Unit) {
        editText.addActionNextListener(block)
    }

    fun clear() {
        text = null
        updateUiOnTextChanged()
    }

    fun showKeyboard() {
        editText.post {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm?.toggleSoftInput(
                InputMethodManager.SHOW_FORCED,
                InputMethodManager.HIDE_IMPLICIT_ONLY
            )
        }
    }

    fun hideKeyboard() {
        post {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(editText.windowToken, 0)
        }
    }

    fun addDateWatcher() {
        addOnFocusChangeListener {
            if (it) showKeyboard()
            editText.hint = if (it) "ngày/tháng/năm" else null
        }
        editText.addDateWatcher()
    }

    class DateArg(
        var minDate: Date = Calendar.getInstance().also { it.add(Calendar.YEAR, -100) }.time,
        var maxDate: Date = Calendar.getInstance().time,
        var startDate: Date = maxDate
    )

    fun addDatePicker(block: (DateArg.() -> Unit)? = null) {
        vb.inputImageViewEnd.setImageResource(R.drawable.ic_drop_down)
        editText.inputType = InputType.TYPE_CLASS_NUMBER
        editText.filters = arrayOf(InputFilter.LengthFilter(10))
        val callback = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            error = null
            text = "%02d/%02d/%s".format(dayOfMonth, monthOfYear + 1, year)
        }
        editText.addClickListener {
            val arg = DateArg()
            block?.invoke(arg)
            val activity = context as AppCompatActivity
            val cal = Calendar.getInstance()
            if (text.isNullOrEmpty()) {
                cal.time = arg.startDate
            } else try {
                cal.time = SimpleDateFormat("dd/MM/yyyy").parse(text)
            } catch (ignore: Exception) {
                cal.time = arg.startDate
            }

            val dialog = DatePickerDialog.newInstance(
                callback,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            dialog.minDate = Calendar.getInstance().also { it.time = arg.minDate }
            dialog.maxDate = Calendar.getInstance().also { it.time = arg.maxDate }
            dialog.version = DatePickerDialog.Version.VERSION_1
            dialog.accentColor = ContextCompat.getColor(context, R.color.color_primary)
            dialog.show(activity.supportFragmentManager, "DatePickerDialog")
        }
        vb.inputImageViewEnd.addClickListener {
            editText.performClick()
        }
    }

    fun filter(filterChars: CharArray) {
        val arrayList = arrayListOf<InputFilter>()
        editText.filters?.apply { arrayList.addAll(this) }
        arrayList.add(InputFilter { source, start, end, _, _, _ ->
            if (end > start) {
                for (index in start until end) {
                    if (!String(filterChars).contains(source[index].toString())) {
                        return@InputFilter ""
                    }
                }
            }
            null
        })
        editText.filters = arrayList.toArray(arrayOfNulls<InputFilter>(arrayList.size))
    }

    inner class SaveState : AbsSavedState {

        var text: String? = null

        constructor(superState: Parcelable) : super(superState)

        @RequiresApi(Build.VERSION_CODES.N)
        constructor(source: Parcel, loader: ClassLoader?) : super(source, loader) {
            text = source.readString()
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            text?.also { dest?.writeString(it) }
        }
    }

}
