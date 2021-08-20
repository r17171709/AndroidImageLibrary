package com.stfalcon.imageviewer.common.extensions

import androidx.transition.Transition

internal fun Transition.addListener(
    onTransitionEnd: ((Transition) -> Unit)? = null,
    onTransitionResume: ((Transition) -> Unit)? = null,
    onTransitionPause: ((Transition) -> Unit)? = null,
    onTransitionCancel: ((Transition) -> Unit)? = null,
    onTransitionStart: ((Transition) -> Unit)? = null
) = addListener(
    object : Transition.TransitionListener {
        override fun onTransitionEnd(transition: Transition) {
            onTransitionEnd?.invoke(transition)
        }

        override fun onTransitionResume(transition: Transition) {
            onTransitionResume?.invoke(transition)
        }

        override fun onTransitionPause(transition: Transition) {
            onTransitionPause?.invoke(transition)
        }

        override fun onTransitionCancel(transition: Transition) {
            onTransitionCancel?.invoke(transition)
        }

        override fun onTransitionStart(transition: Transition) {
            onTransitionStart?.invoke(transition)
        }
    })