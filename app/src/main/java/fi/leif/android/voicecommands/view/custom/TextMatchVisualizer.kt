package fi.leif.android.voicecommands.view.custom

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.RelativeLayout
import android.widget.TextView
import fi.leif.android.voicecommands.R

class TextMatchVisualizer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    companion object {
        const val BG_COLOR = Color.TRANSPARENT
        const val HIGHLIGHT_COLOR = Color.YELLOW

        const val FONT_SIZE_SP = 50f
        val       FONT_TYPEFACE: Typeface = Typeface.DEFAULT_BOLD

        const val ANIM_ALPHA_START = .8f
        const val ANIM_ALPHA_END = 0f
        const val ANIM_SCALE_START = 1f
        const val ANIM_SCALE_END = 3f
        const val ANIM_DURATION_MS = 2000L
    }

    private var textView: TextView
    private var parentLayout: RelativeLayout

    init {
        LayoutInflater.from(context).inflate(
            R.layout.text_match_visualizer, this, true)
        textView =  findViewById(R.id.text)
        parentLayout = findViewById(R.id.textLayout)
    }

    private var _prevMatch = ""
    fun highlightMatch(text: String, match: String?, animateMatch: Boolean = true) {
        if(match == null || text.indexOf(match) == -1) {
            textView.text = text
        } else {
            textView.text = getMatchHighlighted(text, match)
            if(animateMatch && _prevMatch != match) {
                _prevMatch = match
                if(_isRendered)
                    animateMatch(match)
            }
        }
    }

    private fun getMatchHighlighted(text: String, match: String): SpannableString {
        // Make a copy of the original text
        val spannableString = SpannableString(text)
        // Find the start index of the string to match
        val startIndex = text.indexOf(match)
        val endIndex = startIndex + match.length
        spannableString.setSpan(
            ForegroundColorSpan(HIGHLIGHT_COLOR),
            startIndex, endIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannableString
    }

    private var _isRendered = false
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        _isRendered = true
    }

    private fun animateMatch(stringToMatch: String) {
        val xys = getMatchedLinesXY(textView, stringToMatch) ?: return
        for (xy in xys) {
            val newTextView = TextView(context)
            newTextView.setTextColor(HIGHLIGHT_COLOR)
            newTextView.typeface = FONT_TYPEFACE
            newTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, FONT_SIZE_SP)
            newTextView.setBackgroundColor(BG_COLOR)
            newTextView.x = xy.x
            newTextView.y = xy.y
            newTextView.text = xy.text
            parentLayout.addView(newTextView)
            animate(newTextView)
        }
    }

    private fun getMatchedLinesXY(textView: TextView, match: String): List<TextXY>? {
        val matchStart= textView.text.indexOf(match)
        val matchEnd = matchStart + match.length
        if(matchStart == -1) return null

        val list: ArrayList<TextXY> = ArrayList()
        val lineCount = textView.lineCount
        for (i in 0 until lineCount) {
            val lineStart = textView.layout.getLineStart(i)
            val lineEnd = textView.layout.getLineEnd(i)
            var start = -1; var end = -1
            // Matched sentence begins on this line
            if(matchStart in lineStart..<lineEnd) {
                start = matchStart
                end = if (matchEnd < lineEnd) matchEnd else lineEnd
            }
            // Matched sentence ends on this line
            else if(matchEnd in lineStart..<lineEnd) {
                end = matchEnd
                start = if(matchStart > lineStart) matchStart else lineStart
            }
            // Matched sentence fills whole line
            else if(matchStart < lineStart && matchEnd > lineEnd) {
                start = lineStart
                end = lineEnd
            }
            // Add matched text lines with top-left XY coordinates
            if(start > -1 && end > -1) {
                val text = textView.text.subSequence(start, end).toString()
                val x = textView.layout.getPrimaryHorizontal(start)
                var y = 0
                if(i > 0) {
                    y = textView.layout.getLineTop(i) + textView.layout.topPadding
                }
                list.add(TextXY(x,y.toFloat(),text))
            }
        }
        return list
    }

    private class TextXY(val x: Float, val y: Float, val text: String)

    private val animatorSet = AnimatorSet()
    private fun animate(textView: TextView) {
        val alphaAnimator = ObjectAnimator.ofFloat(
            textView,
            "alpha",
            ANIM_ALPHA_START,
            ANIM_ALPHA_END
        )

        val scaleAnimatorX = ObjectAnimator.ofFloat(
            textView,
            "scaleX",
            ANIM_SCALE_START,
            ANIM_SCALE_END
        )

        val scaleAnimatorY = ObjectAnimator.ofFloat(
            textView,
            "scaleY",
            ANIM_SCALE_START,
            ANIM_SCALE_END
        )

        // Create animator set to run both animations together
        animatorSet.playTogether(alphaAnimator, scaleAnimatorX, scaleAnimatorY)
        animatorSet.duration = ANIM_DURATION_MS
        animatorSet.start()
    }

    fun stop() {
        animatorSet.cancel()
    }
}