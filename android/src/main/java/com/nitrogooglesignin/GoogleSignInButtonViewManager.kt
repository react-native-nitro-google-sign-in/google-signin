package com.nitrogooglesignin

import android.view.View
import com.facebook.react.uimanager.LayoutShadowNode
import com.facebook.react.uimanager.ReactStylesDiffMap
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.StateWrapper
import com.facebook.react.uimanager.ThemedReactContext
import com.margelo.nitro.R.id.associated_hybrid_view_tag
import com.margelo.nitro.nitrogooglesignin.HybridGoogleSignInButton
import com.margelo.nitro.nitrogooglesignin.views.HybridGoogleSignInButtonStateUpdater
import com.margelo.nitro.views.RecyclableView

/**
 * Nitro "GoogleSignInButton" view manager with Yoga intrinsic sizing for GMS [SignInButton].
 * (The generated [com.margelo.nitro.nitrogooglesignin.views.HybridGoogleSignInButtonManager] is final.)
 */
class GoogleSignInButtonViewManager : SimpleViewManager<View>() {
  init {
    if (RecyclableView::class.java.isAssignableFrom(HybridGoogleSignInButton::class.java)) {
      super.setupViewRecycling()
    }
  }

  override fun getName(): String = "GoogleSignInButton"

  override fun createShadowNodeInstance(): LayoutShadowNode =
    GoogleSignInButtonShadowNode()

  @Suppress("UNCHECKED_CAST")
  override fun getShadowNodeClass(): Class<LayoutShadowNode> =
    GoogleSignInButtonShadowNode::class.java as Class<LayoutShadowNode>

  override fun createViewInstance(reactContext: ThemedReactContext): View {
    val hybridView = HybridGoogleSignInButton(reactContext)
    val view = hybridView.view
    view.setTag(associated_hybrid_view_tag, hybridView)
    return view
  }

  override fun updateState(
    view: View,
    props: ReactStylesDiffMap,
    stateWrapper: StateWrapper,
  ): Any? {
    val hybridView =
      getHybridView(view)
        ?: throw Error("Couldn't find view $view in local views table!")

    hybridView.beforeUpdate()
    HybridGoogleSignInButtonStateUpdater.updateViewProps(hybridView, stateWrapper)
    hybridView.afterUpdate()

    return super.updateState(view, props, stateWrapper)
  }

  override fun onDropViewInstance(view: View) {
    getHybridView(view)?.onDropView()
    super.onDropViewInstance(view)
  }

  override fun prepareToRecycleView(reactContext: ThemedReactContext, view: View): View? {
    super.prepareToRecycleView(reactContext, view)
    val hybridView = getHybridView(view) ?: return null

    @Suppress("USELESS_IS_CHECK")
    if (hybridView is RecyclableView) {
      hybridView.prepareForRecycle()
      return hybridView.view
    }
    return null
  }

  private fun getHybridView(view: View): HybridGoogleSignInButton? =
    view.getTag(associated_hybrid_view_tag) as? HybridGoogleSignInButton
}
