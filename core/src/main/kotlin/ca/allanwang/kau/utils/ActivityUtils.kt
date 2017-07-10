package ca.allanwang.kau.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.support.annotation.ColorInt
import android.support.annotation.RequiresApi
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.view.Menu
import android.view.View
import ca.allanwang.kau.R
import com.mikepenz.iconics.typeface.IIcon
import org.jetbrains.anko.contentView

/**
 * Created by Allan Wang on 2017-06-21.
 */

/**
 * Restarts an activity from itself with a fade animation
 * Keeps its existing extra bundles and has a builder to accept other parameters
 */
fun Activity.restart(builder: Intent.() -> Unit = {}) {
    val i = Intent(this, this::class.java)
    i.putExtras(intent.extras)
    i.builder()
    startActivity(i)
    overridePendingTransition(R.anim.kau_fade_in, R.anim.kau_fade_out) //No transitions
    finish()
    overridePendingTransition(R.anim.kau_fade_in, R.anim.kau_fade_out)
}

fun Activity.finishSlideOut() {
    finish()
    overridePendingTransition(R.anim.kau_fade_in, R.anim.kau_slide_out_right_top)
}

var Activity.navigationBarColor: Int
    get() = if (buildIsLollipopAndUp) window.navigationBarColor else Color.BLACK
    set(value) {
        if (buildIsLollipopAndUp) window.navigationBarColor = value
    }

var Activity.statusBarColor: Int
    get() = if (buildIsLollipopAndUp) window.statusBarColor else Color.BLACK
    set(value) {
        if (buildIsLollipopAndUp) window.statusBarColor = value
    }

var Activity.statusBarLight: Boolean
    @SuppressLint("InlinedApi")
    get() = if (buildIsMarshmallowAndUp) window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR > 0 else false
    @SuppressLint("InlinedApi")
    set(value) {
        if (buildIsMarshmallowAndUp) {
            val flags = window.decorView.systemUiVisibility
            window.decorView.systemUiVisibility =
                    if (value) flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    else flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
    }

/**
 * Themes the base menu icons and adds iicons programmatically based on ids
 *
 * Call in [Activity.onCreateOptionsMenu]
 */
fun Activity.setMenuIcons(menu: Menu, @ColorInt color: Int = Color.WHITE, vararg iicons: Pair<Int, IIcon>) {
    iicons.forEach { (id, iicon) ->
        menu.findItem(id).icon = iicon.toDrawable(this, sizeDp = 18, color = color)
    }
}

fun Activity.hideKeyboard() = currentFocus.hideKeyboard()

fun Activity.showKeyboard() = currentFocus.showKeyboard()

fun Activity.snackbar(text: String, duration: Int = Snackbar.LENGTH_LONG, builder: Snackbar.() -> Unit = {})
        = contentView!!.snackbar(text, duration, builder)

fun Activity.snackbar(@StringRes textId: Int, duration: Int = Snackbar.LENGTH_LONG, builder: Snackbar.() -> Unit = {})
        = contentView!!.snackbar(textId, duration, builder)