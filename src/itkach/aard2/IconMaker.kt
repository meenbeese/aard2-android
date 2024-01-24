package itkach.aard2

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.TypedValue

import com.kazy.fontdrawable.FontDrawable

object IconMaker {
    private const val CUSTOM_FONT_PATH = "fontawesome-4.2.0.ttf"

    const val IC_SEARCH = '\uf002'
    const val IC_BOOKMARK = '\uf02e'
    const val IC_BOOKMARK_O = '\uf097'
    const val IC_HISTORY = '\uf1da'
    const val IC_DICTIONARY = '\uf02d'
    const val IC_SETTINGS = '\uf013'
    const val IC_FILTER = '\uf0b0'
    const val IC_SORT_DESC = '\uf161'
    const val IC_SORT_ASC = '\uf160'
    const val IC_CLOCK = '\uf017'
    const val IC_LIST = '\uf03a'
    const val IC_TRASH = '\uf1f8'
    const val IC_LICENSE = '\uf19c'
    const val IC_EXTERNAL_LINK = '\uf08e'
    const val IC_FILE_ARCHIVE = '\uf1c6'
    const val IC_ERROR = '\uf071'
    const val IC_COPYRIGHT = '\uf1f9'
    const val IC_SELECT_ALL = '\uf046'
    const val IC_ADD = '\uf067'
    const val IC_ANGLE_UP = '\uf106'
    const val IC_ANGLE_DOWN = '\uf107'
    const val IC_STAR = '\uf005'
    const val IC_STAR_O = '\uf006'
    const val IC_BAN = '\uf05e'
    const val IC_FULLSCREEN = '\uf065'

    private fun make(context: Context, c: Char, sizeDp: Int, color: Int): FontDrawable {
        return FontDrawable.Builder(context, c, CUSTOM_FONT_PATH)
            .setSizeDp(sizeDp)
            .setColor(color)
            .build()
    }

    private fun makeWithColorRes(context: Context, c: Char, sizeDp: Int, colorRes: Int): FontDrawable {
        return make(context, c, sizeDp, context.resources.getColor(colorRes))
    }

    fun tab(context: Context, c: Char): FontDrawable {
        return makeWithColorRes(context, c, 21, R.color.tab_icon)
    }

    fun list(context: Context, c: Char): FontDrawable {
        return makeWithColorRes(context, c, 26, R.color.list_icon)
    }

    fun actionBar(context: Context, c: Char): FontDrawable {
        return makeWithColorRes(context, c, 26, R.color.actionbar_icon)
    }

    fun text(context: Context, c: Char): FontDrawable {
        val typedValue = TypedValue()
        val wasResolved = context.theme.resolveAttribute(android.R.attr.textColorSecondary, typedValue, true)
        return if (wasResolved) {
            val color = ContextCompat.getColor(context, typedValue.resourceId)
            make(context, c, 16, color)
        } else {
            makeWithColorRes(context, c, 16, R.color.list_icon)
        }
    }

    fun errorText(context: Context, c: Char): FontDrawable {
        return makeWithColorRes(context, c, 16, android.R.color.holo_red_dark)
    }

    fun emptyView(context: Context, c: Char): FontDrawable {
        return makeWithColorRes(context, c, 52, R.color.empty_view_icon)
    }
}
