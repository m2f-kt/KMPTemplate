package com.m2f.template.models.routes

import io.ktor.resources.Resource
import kotlinx.serialization.Serializable

@Serializable
@Resource("/api/auth")
class Auth {
    @Serializable @Resource("register")
    class Register(val parent: Auth = Auth())

    @Serializable @Resource("login")
    class Login(val parent: Auth = Auth())

    @Serializable @Resource("refresh")
    class Refresh(val parent: Auth = Auth())

    @Serializable @Resource("logout")
    class Logout(val parent: Auth = Auth())

    @Serializable @Resource("forgot-password")
    class ForgotPassword(val parent: Auth = Auth())

    @Serializable @Resource("reset-password")
    class ResetPassword(val parent: Auth = Auth())
}

@Serializable
@Resource("/api/users")
class Users {
    @Serializable @Resource("me")
    class Me(val parent: Users = Users())

    @Serializable @Resource("{id}")
    class ById(val parent: Users = Users(), val id: String)
}

@Serializable
@Resource("/api/ai")
class Ai {
    @Serializable @Resource("assistant")
    class Assistant(val parent: Ai = Ai())

    @Serializable @Resource("chat")
    class Chat(val parent: Ai = Ai()) {
        companion object {
            /** WebSocket path -- type-safe routing not supported for WebSockets (KTOR-4369) */
            const val WS_PATH = "/api/ai/chat/ws"
        }
    }
}
