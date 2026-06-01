package com.nitrogooglesignin

import android.view.View
import com.facebook.react.uimanager.LayoutShadowNode
import com.facebook.react.uimanager.PixelUtil
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.yoga.YogaMeasureFunction
import com.facebook.yoga.YogaMeasureMode
import com.facebook.yoga.YogaMeasureOutput
import com.facebook.yoga.YogaNode
import com.google.android.gms.common.SignInButton

/**
 * Supplies intrinsic dimensions for the GMS [SignInButton], which often measures 0×0 otherwise.
 */
internal class GoogleSignInButtonShadowNode : LayoutShadowNode(), YogaMeasureFunction {
  private var measuredWidthPx = 0
  private var measuredHeightPx = 0

  init {
    setMeasureFunction(this)
  }

  override fun measure(
    node: YogaNode,
    width: Float,
    widthMode: YogaMeasureMode,
    height: Float,
    heightMode: YogaMeasureMode,
  ): Long {
    if (measuredWidthPx == 0) {
      measureIntrinsicSize(themedContext)
    }

    val measuredWidth =
      when (widthMode) {
        YogaMeasureMode.EXACTLY -> width.toInt()
        YogaMeasureMode.AT_MOST -> minOf(width.toInt(), measuredWidthPx)
        else -> measuredWidthPx
      }

    val measuredHeight =
      when (heightMode) {
        YogaMeasureMode.EXACTLY -> height.toInt()
        YogaMeasureMode.AT_MOST -> minOf(height.toInt(), measuredHeightPx)
        else -> measuredHeightPx
      }

    return YogaMeasureOutput.make(measuredWidth, measuredHeight)
  }

  private fun measureIntrinsicSize(context: ThemedReactContext) {
    val fallbackWidth = PixelUtil.toPixelFromDIP(WIDE_WIDTH_DP).toInt()
    val fallbackHeight = PixelUtil.toPixelFromDIP(BUTTON_HEIGHT_DP).toInt()

    val button = SignInButton(context)
    button.setSize(SignInButton.SIZE_WIDE)
    val unspecified = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    button.measure(unspecified, unspecified)

    measuredWidthPx =
      if (button.measuredWidth > 0) button.measuredWidth else fallbackWidth
    measuredHeightPx =
      if (button.measuredHeight > 0) button.measuredHeight else fallbackHeight
  }

  companion object {
    private const val WIDE_WIDTH_DP = 312f
    private const val BUTTON_HEIGHT_DP = 48f
  }
}
