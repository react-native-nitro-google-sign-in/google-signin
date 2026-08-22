package com.nitrogooglesignin

import android.view.View
import com.facebook.react.uimanager.LayoutShadowNode
import com.facebook.react.uimanager.ReactStylesDiffMap
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.StateWrapper
import com.facebook.react.uimanager.ThemedReactContext
import com.margelo.nitro.R.id.associated_hybrid_view_tag
import com.margelo.nitro.nitrogooglesignin.views.HybridGoogleSignInButtonStateUpdater
import com.margelo.nitro.views.RecyclableView

/**
 * Nitro "GoogleSignInButton" view manager with Paper Yoga intrinsic sizing.
 * (The generated [com.margelo.nitro.nitrogooglesignin.views.HybridGoogleSignInButtonManager] is final.)
 *
 * Fabric does not use [GoogleSignInButtonShadowNode]; the JS wrapper supplies default dimensions.
 */
class GoogleSignInButtonViewManager : SimpleViewManager<View>() {
  private class HybridViewHolder(
    val hybridView: HybridGoogleSignInButton,
    var lastState: StateWrapper? = null,
  )

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
    view.setTag(associated_hybrid_view_tag, HybridViewHolder(hybridView))
    return view
  }

  override fun updateState(
    view: View,
    props: ReactStylesDiffMap,
    stateWrapper: StateWrapper,
  ): Any? {
    val holder =
      getHybridViewHolder(view)
        ?: throw Error("Couldn't find view $view in local views table!")
    val hybridView = holder.hybridView
    val oldState = holder.lastState
    val newState = stateWrapper

    hybridView.beforeUpdate()
    HybridGoogleSignInButtonStateUpdater.updateViewProps(hybridView, newState, oldState)
    hybridView.afterUpdate()
    holder.lastState = newState

    return super.updateState(view, props, newState)
  }

  override fun onDropViewInstance(view: View) {
    val holder = getHybridViewHolder(view)
    holder?.lastState = null
    holder?.hybridView?.onDropView()
    super.onDropViewInstance(view)
  }

  override fun prepareToRecycleView(reactContext: ThemedReactContext, view: View): View? {
    super.prepareToRecycleView(reactContext, view)
    val holder = getHybridViewHolder(view) ?: return null
    val hybridView = holder.hybridView
    holder.lastState = null

    @Suppress("USELESS_IS_CHECK")
    if (hybridView is RecyclableView) {
      hybridView.prepareForRecycle()
      return hybridView.view
    }
    return null
  }

  private fun getHybridViewHolder(view: View): HybridViewHolder? =
    view.getTag(associated_hybrid_view_tag) as? HybridViewHolder
}
