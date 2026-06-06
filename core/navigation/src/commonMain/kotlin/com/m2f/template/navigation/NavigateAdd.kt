package com.m2f.template.navigation

/**
 * Pure, Compose-free navigation primitive for appending a destination to the back stack from any
 * entry point (the app's `navigate` lambda, external bridges into the Compose nav graph, etc.).
 *
 * INVARIANT: this helper APPENDS only — it NEVER clears the back stack. Keeping a single
 * `backStack.clear()` writer in the app avoids races where one site wipes navigation history while
 * another is mid-append. External callbacks that marshal onto the Compose main thread should route
 * through here so they can never strand or wipe the user's navigation history.
 *
 * Idempotent-on-top: if [route] is already the top of [stack] (`lastOrNull()`), the call is a no-op
 * — re-triggering "open this destination" while already on it does not push a duplicate.
 *
 * @param stack the live back stack (a `mutableStateListOf<Route>` in production).
 * @param route the destination to append when it is not already the top.
 */
public fun navigateAdd(stack: MutableList<Route>, route: Route) {
    if (stack.lastOrNull() != route) {
        stack.add(route)
    }
}
