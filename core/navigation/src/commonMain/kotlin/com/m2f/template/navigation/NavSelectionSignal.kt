package com.m2f.template.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * App-wide one-shot signal asking an already-mounted host shell to select a specific in-shell pane
 * from an EXTERNAL entry point, when the shell that owns those panes is already on top of the back
 * stack.
 *
 * Why this cannot be a route push: a route push can only add or swap a *destination* on the back
 * stack — it cannot reach inside an already-mounted shell and change which pane it shows. If the
 * shell route is a `data object`, pushing it again is a no-op; if it were a `data class` variant,
 * pushing it would mount a SECOND copy of the shell rather than re-selecting a pane on the existing
 * one (see [navigateAdd]). So external callers [request] a nav key; the shell's wire observes
 * [requestedNavKey], applies it (dispatching the shell's own pane-selection intent), then
 * [consume]s it.
 *
 * INVARIANT: setting this signal NEVER touches the back stack. Callers separately run [navigateAdd]
 * (append-only, idempotent) to ensure the shell is on top — no `backStack.clear()` is introduced.
 *
 * The nav-key strings are owned by the consuming feature. This type stays string-typed so
 * `core:navigation` carries no dependency on any feature module (which would create a cycle).
 */
class NavSelectionSignal {
    private val _requestedNavKey = MutableStateFlow<String?>(null)

    /** The pending nav key requested by an external caller, or `null` when nothing is pending. */
    val requestedNavKey: StateFlow<String?> = _requestedNavKey.asStateFlow()

    /** Request the host shell select the pane identified by [navKey]. Idempotent. */
    fun request(navKey: String) {
        _requestedNavKey.value = navKey
    }

    /** Clear the pending request once the shell has applied it. */
    fun consume() {
        _requestedNavKey.value = null
    }
}
