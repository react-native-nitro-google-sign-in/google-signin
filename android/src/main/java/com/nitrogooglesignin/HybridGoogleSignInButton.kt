package com.nitrogooglesignin

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.View.MeasureSpec
import android.widget.Button
import android.widget.FrameLayout
import androidx.annotation.Keep
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.facebook.proguard.annotations.DoNotStrip
import com.facebook.react.uimanager.ThemedReactContext
import com.google.android.gms.base.R as GmsBaseR
import com.margelo.nitro.nitrogooglesignin.GoogleSignInButtonColorScheme
import com.margelo.nitro.nitrogooglesignin.GoogleSignInButtonContentAlignment
import com.margelo.nitro.nitrogooglesignin.GoogleSignInButtonNativeSize
import com.margelo.nitro.nitrogooglesignin.HybridGoogleSignInButtonSpec

private const val BUTTON_HEIGHT_DP = 48

/**
 * In-process Google Sign-In button using Play services branding assets
 * (same resources as GMS's local `SignInButton` placeholder).
 *
 * Avoids `com.google.android.gms.common.SignInButton`, which loads a Dynamite
 * remote view that can measure/paint as empty on some Android / Play services
 * combinations (see issue #53).
 */
@Keep
@DoNotStrip
internal class GoogleSignInButtonLayout(
  context: Context,
) : FrameLayout(context) {
  val signInButton: Button =
    Button(context, null, android.R.attr.buttonStyle).apply {
      isAllCaps = false
      transformationMethod = null
      typeface = Typeface.DEFAULT_BOLD
      setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
      stateListAnimator = null
      elevation = 0f
    }

  var buttonSize: GoogleSignInButtonNativeSize = GoogleSignInButtonNativeSize.STANDARD
    set(value) {
      field = value
      applyAppearance()
      requestLayout()
    }

  var colorScheme: GoogleSignInButtonColorScheme = GoogleSignInButtonColorScheme.LIGHT
    set(value) {
      field = value
      applyAppearance()
    }

  var contentAlignment: GoogleSignInButtonContentAlignment =
    GoogleSignInButtonContentAlignment.CENTER
    set(value) {
      field = value
      requestLayout()
    }

  init {
    addView(
      signInButton,
      LayoutParams(
        LayoutParams.WRAP_CONTENT,
        LayoutParams.WRAP_CONTENT,
      ),
    )
    applyAppearance()
  }

  fun applyAppearance() {
    val resources = context.resources
    val theme = context.theme
    val minPx = desiredHeightPx()
    signInButton.minHeight = minPx
    signInButton.minimumHeight = minPx
    signInButton.minWidth = desiredWidthPx()
    signInButton.minimumWidth = desiredWidthPx()

    val backgroundRes =
      when (buttonSize) {
        GoogleSignInButtonNativeSize.ICON ->
          if (colorScheme == GoogleSignInButtonColorScheme.DARK) {
            GmsBaseR.drawable.common_google_signin_btn_icon_dark
          } else {
            GmsBaseR.drawable.common_google_signin_btn_icon_light
          }
        else ->
          if (colorScheme == GoogleSignInButtonColorScheme.DARK) {
            GmsBaseR.drawable.common_google_signin_btn_text_dark
          } else {
            GmsBaseR.drawable.common_google_signin_btn_text_light
          }
      }

    val raw =
      ResourcesCompat.getDrawable(resources, backgroundRes, theme)
        ?: return
    val drawable = DrawableCompat.wrap(raw.mutate())
    ResourcesCompat.getColorStateList(
      resources,
      GmsBaseR.color.common_google_signin_btn_tint,
      theme,
    )?.let { tint ->
      DrawableCompat.setTintList(drawable, tint)
      DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_ATOP)
    }
    signInButton.background = drawable

    val textColorRes =
      if (colorScheme == GoogleSignInButtonColorScheme.DARK) {
        GmsBaseR.color.common_google_signin_btn_text_dark
      } else {
        GmsBaseR.color.common_google_signin_btn_text_light
      }
    signInButton.setTextColor(
      ResourcesCompat.getColorStateList(resources, textColorRes, theme),
    )

    when (buttonSize) {
      GoogleSignInButtonNativeSize.ICON -> {
        signInButton.text = null
        signInButton.contentDescription =
          resources.getString(GmsBaseR.string.common_signin_button_text_long)
      }
      GoogleSignInButtonNativeSize.WIDE -> {
        val label = resources.getString(GmsBaseR.string.common_signin_button_text_long)
        signInButton.text = label
        signInButton.contentDescription = label
      }
      GoogleSignInButtonNativeSize.STANDARD -> {
        val label = resources.getString(GmsBaseR.string.common_signin_button_text)
        signInButton.text = label
        signInButton.contentDescription = label
      }
    }

    signInButton.gravity = Gravity.CENTER
  }

  fun desiredWidthPx(): Int {
    val density = resources.displayMetrics.density
    val widthDp =
      when (buttonSize) {
        GoogleSignInButtonNativeSize.WIDE -> 312
        GoogleSignInButtonNativeSize.ICON -> BUTTON_HEIGHT_DP
        GoogleSignInButtonNativeSize.STANDARD -> 230
      }
    return (widthDp * density + 0.5f).toInt()
  }

  fun desiredHeightPx(): Int =
    (BUTTON_HEIGHT_DP * resources.displayMetrics.density + 0.5f).toInt()

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

  private val signInButton: Button
    get() = buttonLayout.signInButton

  override var colorScheme: GoogleSignInButtonColorScheme =
    GoogleSignInButtonColorScheme.LIGHT
    set(value) {
      field = value
      buttonLayout.colorScheme = value
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
    buttonLayout.applyAppearance()
    buttonLayout.requestLayout()
  }
}
