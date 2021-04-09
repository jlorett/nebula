package com.joshualorett.nebula

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * Fake LifecycleOwner for testing.
 * Created by Joshua on 4/8/2021.
 */

class FakeLifecycleOwner(initialState: Lifecycle.State? = null, private val dispatcher: CoroutineDispatcher = Dispatchers.Main) : LifecycleOwner {
    private val registry: LifecycleRegistry = LifecycleRegistry.createUnsafe(this)

    init {
        initialState?.let {
            setState(it)
        }
    }

    override fun getLifecycle(): Lifecycle = registry

    fun setState(state: Lifecycle.State) {
        registry.currentState = state
    }

    fun pause() {
        runBlocking(dispatcher) {
            setState(Lifecycle.State.STARTED)
        }
    }

    fun destroy() {
        runBlocking(dispatcher) {
            setState(Lifecycle.State.DESTROYED)
        }
    }

    fun create() {
        runBlocking(dispatcher) {
            setState(Lifecycle.State.CREATED)
        }
    }

    fun start() {
        runBlocking(dispatcher) {
            setState(Lifecycle.State.STARTED)
        }
    }

    fun resume() {
        runBlocking(dispatcher) {
            setState(Lifecycle.State.RESUMED)
        }
    }

    private suspend fun getObserverCount(): Int {
        return withContext(dispatcher) {
            registry.observerCount
        }
    }
}