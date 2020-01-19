package com.joshualorett.nebula.shared

import androidx.lifecycle.Observer

/**
 * Data that is exposed via a LiveData that represents an event. Once the event id handled, all
 * subsequent calls will return null.
 * Created by Joshua on 9/21/2019.
 */
class OneShotEvent<out T>(private val content: T) {
    var hasBeenHandled = false
        private set // Don't allow direct modification.

    /***
     * Gets content even if already handled.
     */
    fun peekContent(): T = content

    /**
     * Returns the content once and prevents all subsequent uses.
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }
}

/**
 * Observes for non-handled events.
 */
class OneShotEventObserver<T>(private val onEventUnhandledContent: (T) -> Unit) : Observer<OneShotEvent<T>> {
    override fun onChanged(event: OneShotEvent<T>?) {
        event?.getContentIfNotHandled()?.let { value ->
            onEventUnhandledContent(value)
        }
    }
}