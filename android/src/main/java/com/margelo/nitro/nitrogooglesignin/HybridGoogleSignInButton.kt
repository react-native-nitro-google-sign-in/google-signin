package com.margelo.nitro.nitrogooglesignin

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.View.MeasureSpec
import android.widget.FrameLayout
import androidx.annotation.Keep
import com.facebook.proguard.annotations.DoNotStrip
import com.facebook.react.uimanager.ThemedReactContext
import com.google.android.gms.common.SignInButton

private const val BUTTON_HEIGHT_DP = 48

@Keep
@DoNotStrip
internal class GoogleSignInButtonLayout(
  context: Context,
) : FrameLayout(context) {
  val signInButton: SignInButton = SignInButton(context)

  var buttonSize: GoogleSignInButtonNativeSize = GoogleSignInButtonNativeSize.STANDARD
    set(value) {
      field = value
      applyButtonSize()
      requestLayout()
    }

  var contentAlignment: GoogleSignInButtonContentAlignment =
    GoogleSignInButtonContentAlignment.CENTER
    set(value) {
      field = value
      requestLayout()
    }

  init {
    applyButtonSize()
    signInButton.setColorScheme(SignInButton.COLOR_LIGHT)
    addView(
      signInButton,
      LayoutParams(
        LayoutParams.WRAP_CONTENT,
        LayoutParams.WRAP_CONTENT,
      ),
    )
  }

  fun applyButtonSize() {
    signInButton.setSize(
      when (buttonSize) {
        GoogleSignInButtonNativeSize.WIDE -> SignInButton.SIZE_WIDE
        GoogleSignInButtonNativeSize.ICON -> SignInButton.SIZE_ICON_ONLY
        GoogleSignInButtonNativeSize.STANDARD -> SignInButton.SIZE_STANDARD
      },
    )
  }

  fun desiredWidthPx(): Int {
    val density = resources.displayMetrics.density
    val widthDp =
      when (buttonSize) {
        GoogleSignInButtonNativeSize.WIDE -> 312
        GoogleSignInButtonNativeSize.ICON -> 48
        GoogleSignInButtonNativeSize.STANDARD -> 230
      }
    return (widthDp * density).toInt()
  }

  fun desiredHeightPx(): Int =
    (BUTTON_HEIGHT_DP * resources.displayMetrics.density).toInt()

  private fun measureSignInButton(): Pair<Int, Int> {
    val widthSpec = MeasureSpec.makeMeasureSpec(desiredWidthPx(), MeasureSpec.EXACTLY)
    val heightSpec = MeasureSpec.makeMeasureSpec(desiredHeightPx(), MeasureSpec.EXACTLY)
    signInButton.measure(widthSpec, heightSpec)
    val width =
      if (signInButton.measuredWidth > 0) signInButton.measuredWidth else desiredWidthPx()
    val height =
      if (signInButton.measuredHeight > 0) signInButton.measuredHeight else desiredHeightPx()
    return width to height
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    val (childWidth, childHeight) = measureSignInButton()

    val width =
      when (MeasureSpec.getMode(widthMeasureSpec)) {
        MeasureSpec.EXACTLY -> MeasureSpec.getSize(widthMeasureSpec)
        MeasureSpec.AT_MOST ->
          minOf(MeasureSpec.getSize(widthMeasureSpec).coerceAtLeast(0), childWidth)
        else -> childWidth
      }.coerceAtLeast(childWidth)

    val height =
      when (MeasureSpec.getMode(heightMeasureSpec)) {
        MeasureSpec.EXACTLY -> MeasureSpec.getSize(heightMeasureSpec)
        MeasureSpec.AT_MOST ->
          minOf(MeasureSpec.getSize(heightMeasureSpec).coerceAtLeast(0), childHeight)
        else -> childHeight
      }.coerceAtLeast(childHeight)

    setMeasuredDimension(width, height)
  }

  override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
    val containerWidth = right - left
    val containerHeight = bottom - top
    val childWidth = signInButton.measuredWidth.coerceAtLeast(desiredWidthPx())
    val childHeight = signInButton.measuredHeight.coerceAtLeast(desiredHeightPx())

    val childLeft =
      when (contentAlignment) {
        GoogleSignInButtonContentAlignment.LEADING -> 0
        GoogleSignInButtonContentAlignment.TRAILING ->
          (containerWidth - childWidth).coerceAtLeast(0)
        GoogleSignInButtonContentAlignment.CENTER ->
          ((containerWidth - childWidth) / 2).coerceAtLeast(0)
      }
    val childTop = ((containerHeight - childHeight) / 2).coerceAtLeast(0)
    signInButton.layout(
      childLeft,
      childTop,
      childLeft + childWidth,
      childTop + childHeight,
    )
  }
}

@Keep
@DoNotStrip
class HybridGoogleSignInButton(
  context: ThemedReactContext,
) : HybridGoogleSignInButtonSpec() {
  private val buttonLayout = GoogleSignInButtonLayout(context)

  override val view: View = buttonLayout

  private val signInButton: SignInButton
    get() = buttonLayout.signInButton

  override var colorScheme: GoogleSignInButtonColorScheme =
    GoogleSignInButtonColorScheme.LIGHT
    set(value) {
      field = value
      signInButton.setColorScheme(
        when (value) {
          GoogleSignInButtonColorScheme.DARK -> SignInButton.COLOR_DARK
          GoogleSignInButtonColorScheme.LIGHT -> SignInButton.COLOR_LIGHT
        },
      )
    }

  override var size: GoogleSignInButtonNativeSize = GoogleSignInButtonNativeSize.STANDARD
    set(value) {
      field = value
      buttonLayout.buttonSize = value
    }

  override var contentAlignment: GoogleSignInButtonContentAlignment? =
    GoogleSignInButtonContentAlignment.CENTER
    set(value) {
      field = value
      buttonLayout.contentAlignment =
        value ?: GoogleSignInButtonContentAlignment.CENTER
    }

  override var disabled: Boolean = false
    set(value) {
      field = value
      signInButton.isEnabled = !value
      buttonLayout.isEnabled = !value
      signInButton.alpha = if (value) 0.55f else 1f
    }

  override var onPress: () -> Unit = {}

  init {
    buttonLayout.minimumHeight = buttonLayout.desiredHeightPx()
    buttonLayout.minimumWidth = buttonLayout.desiredWidthPx()
    signInButton.setOnClickListener {
      if (signInButton.isEnabled) {
        onPress()
      }
    }
  }

  override fun afterUpdate() {
    buttonLayout.applyButtonSize()
    buttonLayout.requestLayout()
  }
}
