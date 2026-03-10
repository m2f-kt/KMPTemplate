package com.m2f.template.app.auth.wire

import com.m2f.template.app.auth.checkInviteLink as implCheckInviteLink
import com.m2f.template.app.auth.checkOAuthCallback as implCheckOAuthCallback

fun checkOAuthCallback(): Pair<String, String>? = implCheckOAuthCallback()

fun checkInviteLink(): String? = implCheckInviteLink()
