/*
 * Copyright 2018 Allan Wang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.allanwang.kau.colorpicker

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.res.ResourcesCompat
import ca.allanwang.kau.utils.colorToForeground
import ca.allanwang.kau.utils.dimen
import ca.allanwang.kau.utils.fadeIn
import ca.allanwang.kau.utils.fadeOut
import ca.allanwang.kau.utils.gone
import ca.allanwang.kau.utils.isColorDark
import ca.allanwang.kau.utils.isColorVisibleOn
import ca.allanwang.kau.utils.isVisible
import ca.allanwang.kau.utils.resolveColor
import ca.allanwang.kau.utils.tint
import ca.allanwang.kau.utils.toHexString
import ca.allanwang.kau.utils.visible
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.color.FillGridView
import java.util.Locale

/**
 * Created by Allan Wang on 2017-06-08.
 *
 * ColorPicker component of the ColorPickerDialog
 */
internal class ColorPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ScrollView(context, attrs, defStyleAttr) {
    val selectedColor: Int
        get() = _selectedColor
    private var _selectedColor: Int = -1
    private var isInSub: Boolean = false
    private var isInCustom: Boolean = false
    private var circleSize: Int = context.dimen(R.dimen.kau_color_circle_size).toInt()
    @SuppressLint("PrivateResource")
    private val backgroundColor = context.resolveColor(
        R.attr.md_background_color,
        if (context.resolveColor(android.R.attr.textColorPrimary).isColorDark) Color.WHITE else 0xff424242.toInt()
    )
    private val backgroundColorTint = backgroundColor.colorToForeground()
    private lateinit var dialog: MaterialDialog
    private lateinit var builder: ColorContract
    private lateinit var colorsTop: IntArray
    private var colorsSub: Array<IntArray>? = null
    private var topIndex: Int = -1
    private var subIndex: Int = -1
    private var colorIndex: Int
        get() = if (isInSub) subIndex else topIndex
        set(value) {
            if (isInSub) subIndex = value
            else {
                topIndex = value
                if (colorsSub != null && colorsSub!!.size > value) {
                    dialog.setActionButton(DialogAction.NEGATIVE, builder.backText)
                    isInSub = true
                    invalidateGrid()
                }
            }
        }

    private val gridView: FillGridView
    private val customFrame: LinearLayout
    private val customColorIndicator: View
    private val hexInput: EditText
    private val alphaLabel: TextView
    private val alphaSeekbar: SeekBar
    private val alphaValue: TextView
    private val redSeekbar: SeekBar
    private val redValue: TextView
    private val greenSeekbar: SeekBar
    private val greenValue: TextView
    private val blueSeekbar: SeekBar
    private val blueValue: TextView

    private var customHexTextWatcher: TextWatcher? = null
    private var customRgbListener: SeekBar.OnSeekBarChangeListener? = null

    init {
        //noinspection PrivateResource
        View.inflate(context, R.layout.md_dialog_colorchooser, this)
        gridView = findViewById(R.id.md_grid)
        customFrame = findViewById(R.id.md_colorChooserCustomFrame)
        customColorIndicator = findViewById(R.id.md_colorIndicator)
        hexInput = findViewById(R.id.md_hexInput)
        alphaLabel = findViewById(R.id.md_colorALabel)
        alphaSeekbar = findViewById(R.id.md_colorA)
        alphaValue = findViewById(R.id.md_colorAValue)
        redSeekbar = findViewById(R.id.md_colorR)
        redValue = findViewById(R.id.md_colorRValue)
        greenSeekbar = findViewById(R.id.md_colorG)
        greenValue = findViewById(R.id.md_colorGValue)
        blueSeekbar = findViewById(R.id.md_colorB)
        blueValue = findViewById(R.id.md_colorBValue)
    }

    fun bind(builder: ColorContract, dialog: MaterialDialog) {
        this.builder = builder
        this.dialog = dialog
        this.colorsTop = with(builder) {
            when {
                colorsTop != null -> colorsTop!!
                isAccent -> ColorPalette.ACCENT_COLORS
                else -> ColorPalette.PRIMARY_COLORS
            }
        }
        this.colorsSub = with(builder) {
            when {
                colorsTop != null -> colorsSub
                isAccent -> ColorPalette.ACCENT_COLORS_SUB
                else -> ColorPalette.PRIMARY_COLORS_SUB
            }
        }
        this._selectedColor = builder.defaultColor
        if (builder.allowCustom) {
            if (!builder.allowCustomAlpha) {
                alphaLabel.gone()
                alphaSeekbar.gone()
                alphaValue.gone()
                hexInput.hint = String.format("%06X", _selectedColor)
                hexInput.filters = arrayOf(InputFilter.LengthFilter(6))
            } else {
                hexInput.hint = String.format("%08X", _selectedColor)
                hexInput.filters = arrayOf(InputFilter.LengthFilter(8))
            }
        }
        if (findColor(_selectedColor) || !builder.allowCustom) isInCustom = true // when toggled this will be false
        toggleCustom()
    }

    fun backOrCancel() {
        if (isInSub) {
            dialog.setActionButton(DialogAction.NEGATIVE, builder.cancelText)
            //to top
            isInSub = false
            subIndex = -1
            invalidateGrid()
        } else {
            dialog.cancel()
        }
    }

    fun toggleCustom() {
        isInCustom = !isInCustom
        if (isInCustom) {
            isInSub = false
            if (builder.allowCustom) dialog.setActionButton(DialogAction.NEUTRAL, builder.presetText)
            dialog.setActionButton(DialogAction.NEGATIVE, builder.cancelText)
            customHexTextWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    _selectedColor = try {
                        Color.parseColor("#$s")
                    } catch (e: IllegalArgumentException) {
                        Color.BLACK
                    }
                    customColorIndicator.setBackgroundColor(_selectedColor)
                    if (alphaSeekbar.isVisible) {
                        val alpha = Color.alpha(_selectedColor)
                        alphaSeekbar.progress = alpha
                        alphaValue.text = String.format(Locale.CANADA, "%d", alpha)
                    }
                    redSeekbar.progress = Color.red(_selectedColor)
                    greenSeekbar.progress = Color.green(_selectedColor)
                    blueSeekbar.progress = Color.blue(_selectedColor)
                    isInSub = false
                    topIndex = -1
                    subIndex = -1
                    refreshColors()
                }

                override fun afterTextChanged(s: Editable?) {}
            }
            hexInput.setText(_selectedColor.toHexString(builder.allowCustomAlpha, false))
            hexInput.addTextChangedListener(customHexTextWatcher)
            customRgbListener = object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        val color = if (builder.allowCustomAlpha)
                            Color.argb(
                                alphaSeekbar.progress,
                                redSeekbar.progress,
                                greenSeekbar.progress,
                                blueSeekbar.progress
                            )
                        else Color.rgb(
                            redSeekbar.progress,
                            greenSeekbar.progress,
                            blueSeekbar.progress
                        )

                        hexInput.setText(color.toHexString(builder.allowCustomAlpha, false))
                    }
                    if (builder.allowCustomAlpha) alphaValue.text = alphaSeekbar.progress.toString()
                    redValue.text = redSeekbar.progress.toString()
                    greenValue.text = greenSeekbar.progress.toString()
                    blueValue.text = blueSeekbar.progress.toString()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) = Unit

                override fun onStopTrackingTouch(seekBar: SeekBar) = Unit
            }
            redSeekbar.setOnSeekBarChangeListener(customRgbListener)
            greenSeekbar.setOnSeekBarChangeListener(customRgbListener)
            blueSeekbar.setOnSeekBarChangeListener(customRgbListener)
            if (alphaSeekbar.isVisible)
                alphaSeekbar.setOnSeekBarChangeListener(customRgbListener)
            hexInput.setText(_selectedColor.toHexString(alphaSeekbar.isVisible, false))
            gridView.fadeOut(onFinish = { gridView.gone() })
            customFrame.fadeIn()
        } else {
            findColor(_selectedColor)
            if (builder.allowCustom) dialog.setActionButton(DialogAction.NEUTRAL, builder.customText)
            dialog.setActionButton(DialogAction.NEGATIVE, if (isInSub) builder.backText else builder.cancelText)
            gridView.fadeIn(onStart = this::invalidateGrid)
            customFrame.fadeOut(onFinish = { customFrame.gone() })
            hexInput.removeTextChangedListener(customHexTextWatcher)
            customHexTextWatcher = null
            alphaSeekbar.setOnSeekBarChangeListener(null)
            redSeekbar.setOnSeekBarChangeListener(null)
            greenSeekbar.setOnSeekBarChangeListener(null)
            blueSeekbar.setOnSeekBarChangeListener(null)
            customRgbListener = null
        }
    }

    fun refreshColors() {
        if (!isInCustom) findColor(_selectedColor)
        // Ensure that our tinted color is still visible against the background
        val visibleColor = if (_selectedColor.isColorVisibleOn(backgroundColor)) _selectedColor else backgroundColorTint
        if (builder.dynamicButtonColors) {
            dialog.getActionButton(DialogAction.POSITIVE).setTextColor(visibleColor)
            dialog.getActionButton(DialogAction.NEGATIVE).setTextColor(visibleColor)
            dialog.getActionButton(DialogAction.NEUTRAL).setTextColor(visibleColor)
        }
        if (!builder.allowCustom || !isInCustom) return
        if (builder.allowCustomAlpha)
            alphaSeekbar.visible().tint(visibleColor)
        redSeekbar.tint(visibleColor)
        greenSeekbar.tint(visibleColor)
        blueSeekbar.tint(visibleColor)
        hexInput.tint(visibleColor)
    }

    private fun findColor(@ColorInt color: Int): Boolean {
        topIndex = -1
        subIndex = -1
        colorsTop.forEachIndexed { index, topColor ->
            // First check for sub colors, then if the top color matches
            if (findSubColor(color, index) || topColor == color) {
                topIndex = index
                return true
            }
        }
        return false
    }

    private fun findSubColor(@ColorInt color: Int, topIndex: Int): Boolean {
        subIndex = colorsSub?.getOrNull(topIndex)?.indexOfFirst { color == it } ?: -1
        return subIndex != -1
    }

    private fun invalidateGrid() {
        if (gridView.adapter == null) {
            gridView.adapter = ColorGridAdapter()
            gridView.selector = ResourcesCompat.getDrawable(resources, R.drawable.kau_transparent, null)
        } else {
            (gridView.adapter as BaseAdapter).notifyDataSetChanged()
        }
    }

    inner class ColorGridAdapter : BaseAdapter(), OnClickListener, OnLongClickListener {
        override fun onClick(v: View) {
            val (pos, color) = v.tagData ?: return
            if (colorIndex == pos && isInSub)
                return
            circleAt(colorIndex)?.animateSelected(false)
            _selectedColor = color
            colorIndex = pos
            refreshColors()
            if (isInSub)
                circleAt(colorIndex)?.animateSelected(true)
            // Otherwise we are invalidating our grid, so there is no point in animating
        }

        private fun circleAt(index: Int): CircleView? =
            if (index == -1) null
            else gridView.getChildAt(index) as? CircleView

        private val View.tagData: Pair<Int, Int>?
            get() {
                val tags = (tag as? String)?.split(":") ?: return null
                val pos = tags[0].toIntOrNull() ?: return null
                val color = tags[1].toIntOrNull() ?: return null
                return pos to color
            }

        override fun onLongClick(v: View): Boolean {
            val (_, color) = v.tagData ?: return false
            (v as? CircleView)?.showHint(color) ?: return false
            return true
        }

        override fun getItem(position: Int): Int = if (isInSub) colorsSub!![topIndex][position] else colorsTop[position]

        override fun getCount(): Int = if (isInSub) colorsSub!![topIndex].size else colorsTop.size

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view: CircleView = convertView as? CircleView ?: CircleView(context).apply {
                layoutParams = AbsListView.LayoutParams(circleSize, circleSize)
                setOnClickListener(this@ColorGridAdapter)
                setOnLongClickListener(this@ColorGridAdapter)
            }
            val color: Int = getItem(position)
            return view.apply {
                setBackgroundColor(color)
                isSelected = colorIndex == position
                tag = "$position:$color"
            }
        }
    }
}
