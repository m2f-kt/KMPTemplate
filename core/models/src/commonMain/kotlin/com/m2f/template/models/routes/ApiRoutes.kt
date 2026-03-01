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
    class Me(val parent: Users = Users()) {
        @Serializable @Resource("memberships")
        class Memberships(val parent: Me = Me())

        @Serializable @Resource("avatar")
        class Avatar(val parent: Me = Me())
    }

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

@Serializable
@Resource("/api/groups")
class Groups {
    @Serializable @Resource("create")
    class Create(val parent: Groups = Groups())

    @Serializable @Resource("{groupId}")
    class ById(val parent: Groups = Groups(), val groupId: String)

    @Serializable @Resource("{groupId}/update")
    class Update(val parent: Groups = Groups(), val groupId: String)

    @Serializable @Resource("{groupId}/delete")
    class Delete(val parent: Groups = Groups(), val groupId: String)

    @Serializable @Resource("list")
    class ListAll(val parent: Groups = Groups())

    @Serializable @Resource("{groupId}/members")
    class Members(val parent: Groups = Groups(), val groupId: String, val cursor: String? = null, val limit: Int = 20)

    @Serializable @Resource("{groupId}/members/add")
    class AddMember(val parent: Groups = Groups(), val groupId: String)

    @Serializable @Resource("{groupId}/members/{userId}/remove")
    class RemoveMember(val parent: Groups = Groups(), val groupId: String, val userId: String)

    @Serializable @Resource("{groupId}/members/register")
    class RegisterMember(val parent: Groups = Groups(), val groupId: String)

    @Serializable @Resource("{groupId}/invitations/create")
    class CreateInvitation(val parent: Groups = Groups(), val groupId: String)

    @Serializable @Resource("{groupId}/invitations")
    class ListInvitations(val parent: Groups = Groups(), val groupId: String)

    @Serializable @Resource("{groupId}/invitations/{invitationId}/revoke")
    class RevokeInvitation(val parent: Groups = Groups(), val groupId: String, val invitationId: String)

    @Serializable @Resource("{groupId}/invitations/{invitationId}/resend")
    class ResendInvitation(val parent: Groups = Groups(), val groupId: String, val invitationId: String)
}

/**
 * Invitation routes for viewing and accepting group invitations.
 * GET /{token} is public (no auth) so potential invitees can view invitation details.
 * POST /accept requires authentication to join the group.
 */
@Serializable
@Resource("/api/invitations")
class Invitations {
    @Serializable @Resource("{token}")
    class ByToken(val parent: Invitations = Invitations(), val token: String)

    @Serializable @Resource("accept")
    class Accept(val parent: Invitations = Invitations())
}

@Serializable
@Resource("/api/files")
class Files {
    @Serializable @Resource("upload")
    class Upload(val parent: Files = Files())

    @Serializable @Resource("{fileKey}")
    class Get(val parent: Files = Files(), val fileKey: String)
}

@Serializable
@Resource("/api/documents")
class Documents {
    @Serializable @Resource("upload")
    class Upload(val parent: Documents = Documents())

    @Serializable @Resource("list")
    class ListByGroup(val parent: Documents = Documents(), val groupId: String, val scope: String? = null)

    @Serializable @Resource("{documentId}")
    class ById(val parent: Documents = Documents(), val documentId: String)

    @Serializable @Resource("{documentId}/delete")
    class Delete(val parent: Documents = Documents(), val documentId: String)
}
