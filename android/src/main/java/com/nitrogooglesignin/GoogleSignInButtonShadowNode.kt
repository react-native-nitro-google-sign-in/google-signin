package com.nitrogooglesignin

import com.facebook.react.uimanager.LayoutShadowNode
import com.facebook.react.uimanager.PixelUtil
import com.facebook.yoga.YogaMeasureFunction
import com.facebook.yoga.YogaMeasureMode
import com.facebook.yoga.YogaMeasureOutput
import com.facebook.yoga.YogaNode

/**
 * Paper (old architecture) intrinsic size for [GoogleSignInButton].
 *
 * Under Fabric / New Architecture this shadow node is not used — the JS
 * wrapper applies default width/height from `size` instead.
 */
internal class GoogleSignInButtonShadowNode : LayoutShadowNode(), YogaMeasureFunction {
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
    val intrinsicWidth = PixelUtil.toPixelFromDIP(STANDARD_WIDTH_DP).toInt()
    val intrinsicHeight = PixelUtil.toPixelFromDIP(BUTTON_HEIGHT_DP).toInt()

    val measuredWidth =
      when (widthMode) {
        YogaMeasureMode.EXACTLY -> width.toInt()
        YogaMeasureMode.AT_MOST -> minOf(width.toInt(), intrinsicWidth)
        else -> intrinsicWidth
      }

    val measuredHeight =
      when (heightMode) {
        YogaMeasureMode.EXACTLY -> height.toInt()
        YogaMeasureMode.AT_MOST -> minOf(height.toInt(), intrinsicHeight)
        else -> intrinsicHeight
      }

    return YogaMeasureOutput.make(measuredWidth, measuredHeight)
  }

  companion object {
    private const val STANDARD_WIDTH_DP = 230f
    private const val BUTTON_HEIGHT_DP = 48f
  }
}
