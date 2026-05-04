# Graph Report - .  (2026-05-04)

## Corpus Check
- 450 files · ~202,987 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 1964 nodes · 1579 edges · 385 communities detected
- Extraction: 100% EXTRACTED · 0% INFERRED · 0% AMBIGUOUS
- Token cost: 0 input · 0 output

## God Nodes (most connected - your core abstractions)
1. `GroupRoutesTest` - 17 edges
2. `FakePrivacyApiBuilder` - 15 edges
3. `PrivacyApi` - 14 edges
4. `PrivacyApiImpl` - 14 edges
5. `AccountDeletionViewModelTest` - 12 edges
6. `FakeGroupApiBuilder` - 11 edges
7. `AccountDeletionViewModel` - 11 edges
8. `AdminPanelViewModel` - 11 edges
9. `PgVectorStorage` - 11 edges
10. `GroupApiImpl` - 10 edges

## Surprising Connections (you probably didn't know these)
- None detected - all connections are within the same source files.

## Communities

### Community 0 - "Community 0"
Cohesion: 0.04
Nodes (51): Accept, ActiveExport, AddMember, Ai, Assistant, Auth, Avatar, ById (+43 more)

### Community 1 - "Community 1"
Cohesion: 0.04
Nodes (48): AccessDenied, AgentFailed, AgentNotFound, AI, AlreadyAccepted, AlreadyExists, AppError, Auth (+40 more)

### Community 2 - "Community 2"
Cohesion: 0.07
Nodes (29): AdminPanelMutation, AppendMembers, HideCreateGroupDialog, HideInviteDialog, HideRemoveMemberDialog, HideRevokeDialog, RemoveMemberFromList, SetCreateGroupError (+21 more)

### Community 3 - "Community 3"
Cohesion: 0.1
Nodes (20): AdminPanelIntent, CancelRemoveMember, CancelRevoke, CloseCreateGroupDialog, CloseInviteDialog, ConfirmRemoveMember, ConfirmRevokeInvitation, CreateGroupNameChanged (+12 more)

### Community 4 - "Community 4"
Cohesion: 0.1
Nodes (4): FakeEmbeddingProvider, NoOpEmailService, TestDatabase, TestMinIO

### Community 5 - "Community 5"
Cohesion: 0.1
Nodes (1): GroupRoutesTest

### Community 6 - "Community 6"
Cohesion: 0.11
Nodes (3): ConsentServiceTest, FakeConsentRepository, FakeLegalDocumentRepository

### Community 7 - "Community 7"
Cohesion: 0.11
Nodes (15): CancelEdit, HideCropDialog, ProfileMutation, SetAvatarUrl, SetEditEmail, SetEditName, SetFieldErrors, SetLoading (+7 more)

### Community 8 - "Community 8"
Cohesion: 0.12
Nodes (11): CounterEvent, CounterIntent, CounterModel, CounterMutation, CounterViewModel, Decrement, Done, Increment (+3 more)

### Community 9 - "Community 9"
Cohesion: 0.12
Nodes (1): FakePrivacyApiBuilder

### Community 10 - "Community 10"
Cohesion: 0.13
Nodes (1): PrivacyApi

### Community 11 - "Community 11"
Cohesion: 0.13
Nodes (1): PrivacyApiImpl

### Community 12 - "Community 12"
Cohesion: 0.13
Nodes (0): 

### Community 13 - "Community 13"
Cohesion: 0.14
Nodes (13): ConsentStatus, ConsentType, DataExportResponse, DeletionRequest, DeletionResponse, DeletionStatus, ExportStatus, GrantConsentRequest (+5 more)

### Community 14 - "Community 14"
Cohesion: 0.14
Nodes (11): CancelEditing, CropCancelled, CropConfirmed, EditEmailChanged, EditNameChanged, ImageSelected, LoadProfile, LogoutClicked (+3 more)

### Community 15 - "Community 15"
Cohesion: 0.14
Nodes (6): AddRevokedAtColumnMigration, CreateGroupsTableMigration, CreateInvitationsTableMigration, CreateMembershipsTableMigration, SeedDefaultGroupMigration, SeedDevTestGroupsMigration

### Community 16 - "Community 16"
Cohesion: 0.15
Nodes (10): DaysAgo, Fallback, HoursAgo, JustNow, MinutesAgo, MonthsAgo, RelativeBucket, WeeksAgo (+2 more)

### Community 17 - "Community 17"
Cohesion: 0.15
Nodes (0): 

### Community 18 - "Community 18"
Cohesion: 0.15
Nodes (1): AccountDeletionViewModelTest

### Community 19 - "Community 19"
Cohesion: 0.15
Nodes (12): RegisterMutation, SetConfirmPassword, SetEmail, SetFieldErrors, SetFirstName, SetInvitationEmail, SetInvitationToken, SetLastName (+4 more)

### Community 20 - "Community 20"
Cohesion: 0.15
Nodes (2): InvitationRecord, InvitationRepository

### Community 21 - "Community 21"
Cohesion: 0.15
Nodes (1): GroupServiceImpl

### Community 22 - "Community 22"
Cohesion: 0.15
Nodes (2): PgVectorStorage, SimilarChunk

### Community 23 - "Community 23"
Cohesion: 0.17
Nodes (1): FakeGroupApiBuilder

### Community 24 - "Community 24"
Cohesion: 0.17
Nodes (1): AccountDeletionViewModel

### Community 25 - "Community 25"
Cohesion: 0.17
Nodes (11): LoginMutation, ResetState, SetAcceptingInvitation, SetEmail, SetInvitationEmail, SetInvitationToken, SetLoading, SetPassword (+3 more)

### Community 26 - "Community 26"
Cohesion: 0.17
Nodes (1): AdminPanelViewModel

### Community 27 - "Community 27"
Cohesion: 0.17
Nodes (5): CreateAccountDeletionRequestsTableMigration, CreateConsentRecordsTableMigration, CreateDataExportRequestsTableMigration, CreateLegalDocumentsTableMigration, SeedMarketingAnalyticsDocumentsMigration

### Community 28 - "Community 28"
Cohesion: 0.17
Nodes (5): CreatePasswordResetTokensTableMigration, CreateRefreshTokensTableMigration, CreateRolesTableAndMigrateUsersMigration, CreateUsersTableMigration, DropProcessingRestrictedFromUsersMigration

### Community 29 - "Community 29"
Cohesion: 0.18
Nodes (1): FakeSdkBuilder

### Community 30 - "Community 30"
Cohesion: 0.18
Nodes (1): GroupApiImpl

### Community 31 - "Community 31"
Cohesion: 0.18
Nodes (1): GroupApi

### Community 32 - "Community 32"
Cohesion: 0.18
Nodes (10): ConfirmPasswordChanged, EmailChanged, FirstNameChanged, LastNameChanged, PasswordChanged, RegisterIntent, SetInvitationEmail, SetInvitationToken (+2 more)

### Community 33 - "Community 33"
Cohesion: 0.18
Nodes (1): DashboardViewModelTest

### Community 34 - "Community 34"
Cohesion: 0.18
Nodes (1): ExposedUserRepository

### Community 35 - "Community 35"
Cohesion: 0.18
Nodes (2): GroupRecord, GroupRepository

### Community 36 - "Community 36"
Cohesion: 0.18
Nodes (3): MembershipRecord, MembershipRepository, MemberWithUserRecord

### Community 37 - "Community 37"
Cohesion: 0.18
Nodes (1): DocumentRoutesTest

### Community 38 - "Community 38"
Cohesion: 0.2
Nodes (2): TerminalRippleIndication, TerminalRippleNode

### Community 39 - "Community 39"
Cohesion: 0.2
Nodes (9): AccountDeletionIntent, CancelDeletion, ConfirmDeletion, Load, LogOut, ProceedToReAuth, ReAuthenticate, SetReason (+1 more)

### Community 40 - "Community 40"
Cohesion: 0.2
Nodes (1): PrivacySettingsViewModelTest

### Community 41 - "Community 41"
Cohesion: 0.2
Nodes (0): 

### Community 42 - "Community 42"
Cohesion: 0.2
Nodes (9): RegisterMemberMutation, SetEmail, SetFieldErrors, SetFirstName, SetLastName, SetLoading, SetPassword, SetRole (+1 more)

### Community 43 - "Community 43"
Cohesion: 0.2
Nodes (1): ProfileViewModel

### Community 44 - "Community 44"
Cohesion: 0.2
Nodes (0): 

### Community 45 - "Community 45"
Cohesion: 0.2
Nodes (1): ExposedDataExportRepository

### Community 46 - "Community 46"
Cohesion: 0.2
Nodes (2): DataExportRecord, DataExportRepository

### Community 47 - "Community 47"
Cohesion: 0.2
Nodes (2): UserRecord, UserRepository

### Community 48 - "Community 48"
Cohesion: 0.2
Nodes (1): InvitationRoutesTest

### Community 49 - "Community 49"
Cohesion: 0.2
Nodes (1): MembershipRepositoryImpl

### Community 50 - "Community 50"
Cohesion: 0.2
Nodes (2): DocumentRepository, DocumentRow

### Community 51 - "Community 51"
Cohesion: 0.22
Nodes (5): Admin, GroupRole, GroupRoleSerializer, Member, Owner

### Community 52 - "Community 52"
Cohesion: 0.22
Nodes (5): Admin, PowerAdmin, User, UserRole, UserRoleSerializer

### Community 53 - "Community 53"
Cohesion: 0.22
Nodes (8): AddMemberRequest, CreateGroupRequest, GroupResponse, MemberResponse, MembershipSummary, PaginatedMemberResponse, RegisterMemberRequest, UpdateGroupRequest

### Community 54 - "Community 54"
Cohesion: 0.22
Nodes (1): FakeInvitationApiBuilder

### Community 55 - "Community 55"
Cohesion: 0.22
Nodes (1): FakeAuthApiBuilder

### Community 56 - "Community 56"
Cohesion: 0.22
Nodes (1): PrivacySettingsViewModel

### Community 57 - "Community 57"
Cohesion: 0.22
Nodes (8): AccountDeletionMutation, SetConfirmationToken, SetError, SetLoading, SetPendingDeletion, SetReason, SetStep, SetUserEmail

### Community 58 - "Community 58"
Cohesion: 0.22
Nodes (8): EmailChanged, LoginIntent, PasswordChanged, RememberMeChanged, Reset, SetInvitationEmail, SetInvitationToken, SubmitLoginClicked

### Community 59 - "Community 59"
Cohesion: 0.22
Nodes (8): InviteAcceptMutation, SetAccepting, SetAcceptSuccess, SetError, SetInvitationDetails, SetLoadingInvitation, SetLoggedIn, SetToken

### Community 60 - "Community 60"
Cohesion: 0.22
Nodes (1): AdminPanelViewModelTest

### Community 61 - "Community 61"
Cohesion: 0.22
Nodes (7): Ai, Auth, Email, Http, OAuth, S3, ServerConfig

### Community 62 - "Community 62"
Cohesion: 0.22
Nodes (1): ExposedAccountDeletionRepository

### Community 63 - "Community 63"
Cohesion: 0.22
Nodes (2): AccountDeletionRecord, AccountDeletionRepository

### Community 64 - "Community 64"
Cohesion: 0.22
Nodes (1): FileRoutesTest

### Community 65 - "Community 65"
Cohesion: 0.22
Nodes (0): 

### Community 66 - "Community 66"
Cohesion: 0.25
Nodes (6): Admin, Free, Paid, PowerAdmin, Premium, UserTier

### Community 67 - "Community 67"
Cohesion: 0.25
Nodes (1): FakeUserApiBuilder

### Community 68 - "Community 68"
Cohesion: 0.25
Nodes (1): AuthApi

### Community 69 - "Community 69"
Cohesion: 0.25
Nodes (1): InvitationApiImpl

### Community 70 - "Community 70"
Cohesion: 0.25
Nodes (1): AuthApiImpl

### Community 71 - "Community 71"
Cohesion: 0.25
Nodes (1): InvitationApi

### Community 72 - "Community 72"
Cohesion: 0.25
Nodes (1): CardVariant

### Community 73 - "Community 73"
Cohesion: 0.25
Nodes (0): 

### Community 74 - "Community 74"
Cohesion: 0.25
Nodes (1): LoginViewModelTest

### Community 75 - "Community 75"
Cohesion: 0.25
Nodes (0): 

### Community 76 - "Community 76"
Cohesion: 0.25
Nodes (7): EmailChanged, FirstNameChanged, LastNameChanged, PasswordChanged, RegisterMemberIntent, RoleChanged, SubmitRegisterMember

### Community 77 - "Community 77"
Cohesion: 0.25
Nodes (7): DashboardMutation, SetAvatarUrl, SetLoading, SetMembership, SetNavItem, SetSystemAdmin, SetUserName

### Community 78 - "Community 78"
Cohesion: 0.25
Nodes (7): AddDocument, DocumentsMutation, RemoveDocument, SetDocuments, SetError, SetLoading, SetUploading

### Community 79 - "Community 79"
Cohesion: 0.25
Nodes (1): VectorColumnType

### Community 80 - "Community 80"
Cohesion: 0.25
Nodes (1): ExposedConsentRepository

### Community 81 - "Community 81"
Cohesion: 0.25
Nodes (2): ConsentRecord, ConsentRepository

### Community 82 - "Community 82"
Cohesion: 0.25
Nodes (1): GroupService

### Community 83 - "Community 83"
Cohesion: 0.29
Nodes (6): AuthResponse, ForgotPasswordRequest, LoginRequest, RefreshTokenRequest, RegisterRequest, ResetPasswordRequest

### Community 84 - "Community 84"
Cohesion: 0.29
Nodes (1): FakeDocumentApiBuilder

### Community 85 - "Community 85"
Cohesion: 0.29
Nodes (1): UserApi

### Community 86 - "Community 86"
Cohesion: 0.29
Nodes (1): UserApiImpl

### Community 87 - "Community 87"
Cohesion: 0.29
Nodes (1): ConsentGateViewModel

### Community 88 - "Community 88"
Cohesion: 0.29
Nodes (6): AccountDeletionEvent, DeletionCancelled, DeletionScheduled, LoggedOut, NavigateToLogin, ShowError

### Community 89 - "Community 89"
Cohesion: 0.29
Nodes (6): DownloadExport, Load, PrivacySettingsIntent, RequestExport, ToggleConsent, ViewDocument

### Community 90 - "Community 90"
Cohesion: 0.29
Nodes (6): PrivacySettingsMutation, SetConsents, SetDeletionStatus, SetError, SetExportStatus, SetLoading

### Community 91 - "Community 91"
Cohesion: 0.29
Nodes (1): ConsentGateViewModelTest

### Community 92 - "Community 92"
Cohesion: 0.29
Nodes (6): AcceptInvitation, GoToLogin, GoToRegister, InviteAcceptIntent, LoadInvitation, RequestNewInvitation

### Community 93 - "Community 93"
Cohesion: 0.29
Nodes (1): LoginViewModel

### Community 94 - "Community 94"
Cohesion: 0.29
Nodes (0): 

### Community 95 - "Community 95"
Cohesion: 0.29
Nodes (1): RegisterViewModel

### Community 96 - "Community 96"
Cohesion: 0.29
Nodes (1): RegisterViewModelTest

### Community 97 - "Community 97"
Cohesion: 0.29
Nodes (5): ForgotPasswordRoute, InviteAcceptRoute, LoginRoute, OAuthCallbackRoute, RegisterRoute

### Community 98 - "Community 98"
Cohesion: 0.29
Nodes (1): RegisterMemberViewModelTest

### Community 99 - "Community 99"
Cohesion: 0.29
Nodes (6): AdminPanelClicked, DashboardIntent, LoadDashboard, LogoutClicked, NavItemSelected, RefreshProfile

### Community 100 - "Community 100"
Cohesion: 0.29
Nodes (1): DocumentsViewModel

### Community 101 - "Community 101"
Cohesion: 0.29
Nodes (4): ComposeView, ContentView, UIViewControllerRepresentable, View

### Community 102 - "Community 102"
Cohesion: 0.29
Nodes (1): StructuredOutputTest

### Community 103 - "Community 103"
Cohesion: 0.33
Nodes (1): Check

### Community 104 - "Community 104"
Cohesion: 0.33
Nodes (5): AgentRequest, AgentResponse, ChatRequest, ChatResponse, ChatStreamFrame

### Community 105 - "Community 105"
Cohesion: 0.33
Nodes (1): DocumentApi

### Community 106 - "Community 106"
Cohesion: 0.33
Nodes (1): DocumentApiImpl

### Community 107 - "Community 107"
Cohesion: 0.33
Nodes (1): MviViewModel

### Community 108 - "Community 108"
Cohesion: 0.33
Nodes (5): AcceptAll, ConsentGateIntent, LoadRequiredConsents, ToggleConsent, ViewDocument

### Community 109 - "Community 109"
Cohesion: 0.33
Nodes (0): 

### Community 110 - "Community 110"
Cohesion: 0.33
Nodes (5): ExportReady, NavigateToDeletion, NavigateToDocument, PrivacySettingsEvent, ShowError

### Community 111 - "Community 111"
Cohesion: 0.33
Nodes (5): ConsentGateMutation, SetConsents, SetError, SetLoading, UpdateConsentToggle

### Community 112 - "Community 112"
Cohesion: 0.33
Nodes (5): NavigateToConsentGate, NavigateToDashboard, NavigateToGroup, RegisterEvent, ViewLegalDocument

### Community 113 - "Community 113"
Cohesion: 0.33
Nodes (1): InviteAcceptViewModel

### Community 114 - "Community 114"
Cohesion: 0.33
Nodes (5): InviteAcceptEvent, NavigateToGroup, NavigateToLogin, NavigateToRegister, RequestedNewInvitation

### Community 115 - "Community 115"
Cohesion: 0.33
Nodes (5): ActivityItem, DashboardMockData, DeploymentStatus, MetricItem, ProcessItem

### Community 116 - "Community 116"
Cohesion: 0.33
Nodes (1): ProfileViewModelTest

### Community 117 - "Community 117"
Cohesion: 0.33
Nodes (5): DeleteDocument, DocumentsIntent, LoadDocuments, RefreshDocuments, UploadFile

### Community 118 - "Community 118"
Cohesion: 0.33
Nodes (2): AddDocumentsTableAndEmbeddingColumnsMigration, EnablePgvectorAndCreateEmbeddingsTableMigration

### Community 119 - "Community 119"
Cohesion: 0.33
Nodes (1): InvalidField

### Community 120 - "Community 120"
Cohesion: 0.33
Nodes (1): ExposedLegalDocumentRepository

### Community 121 - "Community 121"
Cohesion: 0.33
Nodes (2): LegalDocumentRecord, LegalDocumentRepository

### Community 122 - "Community 122"
Cohesion: 0.33
Nodes (1): EmailServiceTest

### Community 123 - "Community 123"
Cohesion: 0.33
Nodes (2): RagContext, RagService

### Community 124 - "Community 124"
Cohesion: 0.4
Nodes (4): AcceptInvitationRequest, AcceptInvitationResponse, CreateInvitationRequest, InvitationResponse

### Community 125 - "Community 125"
Cohesion: 0.4
Nodes (0): 

### Community 126 - "Community 126"
Cohesion: 0.4
Nodes (1): ViewModelTestContext

### Community 127 - "Community 127"
Cohesion: 0.4
Nodes (4): EventStatement, IntentStatement, ModelStatement, Statement

### Community 128 - "Community 128"
Cohesion: 0.4
Nodes (1): FakeFileApiBuilder

### Community 129 - "Community 129"
Cohesion: 0.4
Nodes (1): ImagePickerDelegate

### Community 130 - "Community 130"
Cohesion: 0.4
Nodes (1): ImagePickerResult

### Community 131 - "Community 131"
Cohesion: 0.4
Nodes (0): 

### Community 132 - "Community 132"
Cohesion: 0.4
Nodes (1): ButtonVariant

### Community 133 - "Community 133"
Cohesion: 0.4
Nodes (1): ReorderState

### Community 134 - "Community 134"
Cohesion: 0.4
Nodes (1): ListItemState

### Community 135 - "Community 135"
Cohesion: 0.4
Nodes (2): RadarDataPoint, RadarSeries

### Community 136 - "Community 136"
Cohesion: 0.4
Nodes (2): ChartDataPoint, ChartSeries

### Community 137 - "Community 137"
Cohesion: 0.4
Nodes (1): LegalDocumentViewModel

### Community 138 - "Community 138"
Cohesion: 0.4
Nodes (0): 

### Community 139 - "Community 139"
Cohesion: 0.4
Nodes (4): ConsentCompleted, ConsentGateEvent, NavigateToDocument, ShowError

### Community 140 - "Community 140"
Cohesion: 0.4
Nodes (4): LegalDocumentMutation, SetDocument, SetError, SetLoading

### Community 141 - "Community 141"
Cohesion: 0.4
Nodes (1): LegalDocumentViewModelTest

### Community 142 - "Community 142"
Cohesion: 0.4
Nodes (4): AccountDeletionRoute, ConsentGateRoute, LegalDocumentRoute, PrivacySettingsRoute

### Community 143 - "Community 143"
Cohesion: 0.4
Nodes (4): LoginEvent, NavigateToConsentGate, NavigateToDashboard, NavigateToGroup

### Community 144 - "Community 144"
Cohesion: 0.4
Nodes (1): OAuthHandler

### Community 145 - "Community 145"
Cohesion: 0.4
Nodes (1): OAuthHandler

### Community 146 - "Community 146"
Cohesion: 0.4
Nodes (1): RegisterMemberViewModel

### Community 147 - "Community 147"
Cohesion: 0.4
Nodes (0): 

### Community 148 - "Community 148"
Cohesion: 0.4
Nodes (2): PrivacyJob, PrivacyJobScheduler

### Community 149 - "Community 149"
Cohesion: 0.4
Nodes (3): ExportContributor, ExportFile, ExportSection

### Community 150 - "Community 150"
Cohesion: 0.4
Nodes (1): DocumentIngestionService

### Community 151 - "Community 151"
Cohesion: 0.4
Nodes (1): ExposedPersistenceStorage

### Community 152 - "Community 152"
Cohesion: 0.5
Nodes (3): DocumentListResponse, DocumentResponse, DocumentUploadRequest

### Community 153 - "Community 153"
Cohesion: 0.5
Nodes (1): PreferencesStorage

### Community 154 - "Community 154"
Cohesion: 0.5
Nodes (1): ViewModelTest

### Community 155 - "Community 155"
Cohesion: 0.5
Nodes (0): 

### Community 156 - "Community 156"
Cohesion: 0.5
Nodes (1): FileApiImpl

### Community 157 - "Community 157"
Cohesion: 0.5
Nodes (1): FileApi

### Community 158 - "Community 158"
Cohesion: 0.5
Nodes (0): 

### Community 159 - "Community 159"
Cohesion: 0.5
Nodes (0): 

### Community 160 - "Community 160"
Cohesion: 0.5
Nodes (1): BadgeVariant

### Community 161 - "Community 161"
Cohesion: 0.5
Nodes (1): AlertVariant

### Community 162 - "Community 162"
Cohesion: 0.5
Nodes (1): BarData

### Community 163 - "Community 163"
Cohesion: 0.5
Nodes (0): 

### Community 164 - "Community 164"
Cohesion: 0.5
Nodes (0): 

### Community 165 - "Community 165"
Cohesion: 0.5
Nodes (3): LegalDocumentIntent, Load, SwitchLocale

### Community 166 - "Community 166"
Cohesion: 0.5
Nodes (0): 

### Community 167 - "Community 167"
Cohesion: 0.5
Nodes (1): OAuthHandler

### Community 168 - "Community 168"
Cohesion: 0.5
Nodes (1): OAuthHandler

### Community 169 - "Community 169"
Cohesion: 0.5
Nodes (1): OAuthHandler

### Community 170 - "Community 170"
Cohesion: 0.5
Nodes (3): AdminPanelEvent, GroupCreated, NavigateToRegisterMember

### Community 171 - "Community 171"
Cohesion: 0.5
Nodes (1): BottomTab

### Community 172 - "Community 172"
Cohesion: 0.5
Nodes (3): DashboardEvent, NavigateToAdmin, NavigateToLogin

### Community 173 - "Community 173"
Cohesion: 0.5
Nodes (1): DashboardViewModel

### Community 174 - "Community 174"
Cohesion: 0.5
Nodes (3): NavigateToLogin, NavigateToPrivacySettings, ProfileEvent

### Community 175 - "Community 175"
Cohesion: 0.5
Nodes (1): ProfileModel

### Community 176 - "Community 176"
Cohesion: 0.5
Nodes (1): Migration

### Community 177 - "Community 177"
Cohesion: 0.5
Nodes (1): MigrationRegistry

### Community 178 - "Community 178"
Cohesion: 0.5
Nodes (1): UserService

### Community 179 - "Community 179"
Cohesion: 0.5
Nodes (0): 

### Community 180 - "Community 180"
Cohesion: 0.5
Nodes (1): CreateConversationsTableMigration

### Community 181 - "Community 181"
Cohesion: 0.5
Nodes (1): UserTools

### Community 182 - "Community 182"
Cohesion: 0.5
Nodes (2): RelevanceCheck, RelevanceDetector

### Community 183 - "Community 183"
Cohesion: 0.5
Nodes (2): HealthStatus, ServiceHealth

### Community 184 - "Community 184"
Cohesion: 0.5
Nodes (1): iOSApp

### Community 185 - "Community 185"
Cohesion: 0.5
Nodes (1): MainActivity

### Community 186 - "Community 186"
Cohesion: 0.67
Nodes (2): UpdateProfileRequest, UserResponse

### Community 187 - "Community 187"
Cohesion: 0.67
Nodes (1): AuthInterceptor

### Community 188 - "Community 188"
Cohesion: 0.67
Nodes (0): 

### Community 189 - "Community 189"
Cohesion: 0.67
Nodes (0): 

### Community 190 - "Community 190"
Cohesion: 0.67
Nodes (0): 

### Community 191 - "Community 191"
Cohesion: 0.67
Nodes (0): 

### Community 192 - "Community 192"
Cohesion: 0.67
Nodes (0): 

### Community 193 - "Community 193"
Cohesion: 0.67
Nodes (0): 

### Community 194 - "Community 194"
Cohesion: 0.67
Nodes (0): 

### Community 195 - "Community 195"
Cohesion: 0.67
Nodes (0): 

### Community 196 - "Community 196"
Cohesion: 0.67
Nodes (0): 

### Community 197 - "Community 197"
Cohesion: 0.67
Nodes (0): 

### Community 198 - "Community 198"
Cohesion: 0.67
Nodes (0): 

### Community 199 - "Community 199"
Cohesion: 0.67
Nodes (2): TerminalShadow, TerminalShadows

### Community 200 - "Community 200"
Cohesion: 0.67
Nodes (2): ConsentGateModel, ConsentItem

### Community 201 - "Community 201"
Cohesion: 0.67
Nodes (2): LegalDocumentEvent, ShowError

### Community 202 - "Community 202"
Cohesion: 0.67
Nodes (2): AccountDeletionModel, DeletionStep

### Community 203 - "Community 203"
Cohesion: 0.67
Nodes (0): 

### Community 204 - "Community 204"
Cohesion: 0.67
Nodes (0): 

### Community 205 - "Community 205"
Cohesion: 0.67
Nodes (2): RegisterMemberEvent, RegistrationSuccess

### Community 206 - "Community 206"
Cohesion: 0.67
Nodes (2): AdminPanelRoute, RegisterMemberRoute

### Community 207 - "Community 207"
Cohesion: 0.67
Nodes (0): 

### Community 208 - "Community 208"
Cohesion: 0.67
Nodes (0): 

### Community 209 - "Community 209"
Cohesion: 0.67
Nodes (0): 

### Community 210 - "Community 210"
Cohesion: 0.67
Nodes (0): 

### Community 211 - "Community 211"
Cohesion: 0.67
Nodes (0): 

### Community 212 - "Community 212"
Cohesion: 0.67
Nodes (2): DocumentsEvent, UploadSuccess

### Community 213 - "Community 213"
Cohesion: 0.67
Nodes (1): DataSource

### Community 214 - "Community 214"
Cohesion: 0.67
Nodes (1): DomainError

### Community 215 - "Community 215"
Cohesion: 0.67
Nodes (1): ServerStrings

### Community 216 - "Community 216"
Cohesion: 0.67
Nodes (1): ExportProcessorJob

### Community 217 - "Community 217"
Cohesion: 0.67
Nodes (1): ExportCleanupJob

### Community 218 - "Community 218"
Cohesion: 0.67
Nodes (1): DeletionExecutorJob

### Community 219 - "Community 219"
Cohesion: 0.67
Nodes (1): ConsentRequired

### Community 220 - "Community 220"
Cohesion: 0.67
Nodes (1): SmtpEmailService

### Community 221 - "Community 221"
Cohesion: 0.67
Nodes (1): InvalidCredentials

### Community 222 - "Community 222"
Cohesion: 0.67
Nodes (1): EmailService

### Community 223 - "Community 223"
Cohesion: 0.67
Nodes (1): RoleConfig

### Community 224 - "Community 224"
Cohesion: 0.67
Nodes (0): 

### Community 225 - "Community 225"
Cohesion: 0.67
Nodes (0): 

### Community 226 - "Community 226"
Cohesion: 0.67
Nodes (0): 

### Community 227 - "Community 227"
Cohesion: 0.67
Nodes (1): GroupNotFound

### Community 228 - "Community 228"
Cohesion: 0.67
Nodes (1): InvitationNotFound

### Community 229 - "Community 229"
Cohesion: 0.67
Nodes (1): StructuredOutputService

### Community 230 - "Community 230"
Cohesion: 0.67
Nodes (1): AgentExecutionFailed

### Community 231 - "Community 231"
Cohesion: 0.67
Nodes (1): FileTooLarge

### Community 232 - "Community 232"
Cohesion: 0.67
Nodes (0): 

### Community 233 - "Community 233"
Cohesion: 0.67
Nodes (0): 

### Community 234 - "Community 234"
Cohesion: 0.67
Nodes (0): 

### Community 235 - "Community 235"
Cohesion: 0.67
Nodes (0): 

### Community 236 - "Community 236"
Cohesion: 1.0
Nodes (1): Route

### Community 237 - "Community 237"
Cohesion: 1.0
Nodes (1): ErrorResponse

### Community 238 - "Community 238"
Cohesion: 1.0
Nodes (1): FileResponse

### Community 239 - "Community 239"
Cohesion: 1.0
Nodes (1): StringKey

### Community 240 - "Community 240"
Cohesion: 1.0
Nodes (0): 

### Community 241 - "Community 241"
Cohesion: 1.0
Nodes (0): 

### Community 242 - "Community 242"
Cohesion: 1.0
Nodes (0): 

### Community 243 - "Community 243"
Cohesion: 1.0
Nodes (0): 

### Community 244 - "Community 244"
Cohesion: 1.0
Nodes (1): Sdk

### Community 245 - "Community 245"
Cohesion: 1.0
Nodes (0): 

### Community 246 - "Community 246"
Cohesion: 1.0
Nodes (0): 

### Community 247 - "Community 247"
Cohesion: 1.0
Nodes (0): 

### Community 248 - "Community 248"
Cohesion: 1.0
Nodes (0): 

### Community 249 - "Community 249"
Cohesion: 1.0
Nodes (0): 

### Community 250 - "Community 250"
Cohesion: 1.0
Nodes (0): 

### Community 251 - "Community 251"
Cohesion: 1.0
Nodes (0): 

### Community 252 - "Community 252"
Cohesion: 1.0
Nodes (0): 

### Community 253 - "Community 253"
Cohesion: 1.0
Nodes (0): 

### Community 254 - "Community 254"
Cohesion: 1.0
Nodes (1): TerminalBorders

### Community 255 - "Community 255"
Cohesion: 1.0
Nodes (1): TerminalGap

### Community 256 - "Community 256"
Cohesion: 1.0
Nodes (1): TerminalTypography

### Community 257 - "Community 257"
Cohesion: 1.0
Nodes (1): TerminalSpacing

### Community 258 - "Community 258"
Cohesion: 1.0
Nodes (1): TerminalColors

### Community 259 - "Community 259"
Cohesion: 1.0
Nodes (1): TerminalOpacity

### Community 260 - "Community 260"
Cohesion: 1.0
Nodes (1): TerminalRadius

### Community 261 - "Community 261"
Cohesion: 1.0
Nodes (0): 

### Community 262 - "Community 262"
Cohesion: 1.0
Nodes (0): 

### Community 263 - "Community 263"
Cohesion: 1.0
Nodes (0): 

### Community 264 - "Community 264"
Cohesion: 1.0
Nodes (0): 

### Community 265 - "Community 265"
Cohesion: 1.0
Nodes (0): 

### Community 266 - "Community 266"
Cohesion: 1.0
Nodes (0): 

### Community 267 - "Community 267"
Cohesion: 1.0
Nodes (1): LegalDocumentModel

### Community 268 - "Community 268"
Cohesion: 1.0
Nodes (1): PrivacySettingsModel

### Community 269 - "Community 269"
Cohesion: 1.0
Nodes (0): 

### Community 270 - "Community 270"
Cohesion: 1.0
Nodes (0): 

### Community 271 - "Community 271"
Cohesion: 1.0
Nodes (0): 

### Community 272 - "Community 272"
Cohesion: 1.0
Nodes (0): 

### Community 273 - "Community 273"
Cohesion: 1.0
Nodes (0): 

### Community 274 - "Community 274"
Cohesion: 1.0
Nodes (0): 

### Community 275 - "Community 275"
Cohesion: 1.0
Nodes (0): 

### Community 276 - "Community 276"
Cohesion: 1.0
Nodes (1): InviteAcceptModel

### Community 277 - "Community 277"
Cohesion: 1.0
Nodes (0): 

### Community 278 - "Community 278"
Cohesion: 1.0
Nodes (0): 

### Community 279 - "Community 279"
Cohesion: 1.0
Nodes (1): LoginModel

### Community 280 - "Community 280"
Cohesion: 1.0
Nodes (1): RegisterModel

### Community 281 - "Community 281"
Cohesion: 1.0
Nodes (0): 

### Community 282 - "Community 282"
Cohesion: 1.0
Nodes (0): 

### Community 283 - "Community 283"
Cohesion: 1.0
Nodes (0): 

### Community 284 - "Community 284"
Cohesion: 1.0
Nodes (0): 

### Community 285 - "Community 285"
Cohesion: 1.0
Nodes (0): 

### Community 286 - "Community 286"
Cohesion: 1.0
Nodes (0): 

### Community 287 - "Community 287"
Cohesion: 1.0
Nodes (0): 

### Community 288 - "Community 288"
Cohesion: 1.0
Nodes (0): 

### Community 289 - "Community 289"
Cohesion: 1.0
Nodes (0): 

### Community 290 - "Community 290"
Cohesion: 1.0
Nodes (0): 

### Community 291 - "Community 291"
Cohesion: 1.0
Nodes (1): AdminPanelModel

### Community 292 - "Community 292"
Cohesion: 1.0
Nodes (1): RegisterMemberModel

### Community 293 - "Community 293"
Cohesion: 1.0
Nodes (0): 

### Community 294 - "Community 294"
Cohesion: 1.0
Nodes (0): 

### Community 295 - "Community 295"
Cohesion: 1.0
Nodes (1): DashboardModel

### Community 296 - "Community 296"
Cohesion: 1.0
Nodes (1): DashboardRoute

### Community 297 - "Community 297"
Cohesion: 1.0
Nodes (0): 

### Community 298 - "Community 298"
Cohesion: 1.0
Nodes (0): 

### Community 299 - "Community 299"
Cohesion: 1.0
Nodes (1): ProfileRoute

### Community 300 - "Community 300"
Cohesion: 1.0
Nodes (0): 

### Community 301 - "Community 301"
Cohesion: 1.0
Nodes (1): DocumentsModel

### Community 302 - "Community 302"
Cohesion: 1.0
Nodes (0): 

### Community 303 - "Community 303"
Cohesion: 1.0
Nodes (1): DocumentsRoute

### Community 304 - "Community 304"
Cohesion: 1.0
Nodes (0): 

### Community 305 - "Community 305"
Cohesion: 1.0
Nodes (1): DocumentEmbeddingsTable

### Community 306 - "Community 306"
Cohesion: 1.0
Nodes (1): DocumentsTable

### Community 307 - "Community 307"
Cohesion: 1.0
Nodes (1): MigrationsTable

### Community 308 - "Community 308"
Cohesion: 1.0
Nodes (1): Configuration

### Community 309 - "Community 309"
Cohesion: 1.0
Nodes (0): 

### Community 310 - "Community 310"
Cohesion: 1.0
Nodes (0): 

### Community 311 - "Community 311"
Cohesion: 1.0
Nodes (1): LegalDocumentServiceImpl

### Community 312 - "Community 312"
Cohesion: 1.0
Nodes (1): ConsentServiceImpl

### Community 313 - "Community 313"
Cohesion: 1.0
Nodes (0): 

### Community 314 - "Community 314"
Cohesion: 1.0
Nodes (0): 

### Community 315 - "Community 315"
Cohesion: 1.0
Nodes (0): 

### Community 316 - "Community 316"
Cohesion: 1.0
Nodes (0): 

### Community 317 - "Community 317"
Cohesion: 1.0
Nodes (1): LegalDocumentsTable

### Community 318 - "Community 318"
Cohesion: 1.0
Nodes (1): AccountDeletionRequestsTable

### Community 319 - "Community 319"
Cohesion: 1.0
Nodes (1): DataExportRequestsTable

### Community 320 - "Community 320"
Cohesion: 1.0
Nodes (1): ConsentRecordsTable

### Community 321 - "Community 321"
Cohesion: 1.0
Nodes (0): 

### Community 322 - "Community 322"
Cohesion: 1.0
Nodes (1): GoogleUserInfo

### Community 323 - "Community 323"
Cohesion: 1.0
Nodes (1): AuthServiceImpl

### Community 324 - "Community 324"
Cohesion: 1.0
Nodes (0): 

### Community 325 - "Community 325"
Cohesion: 1.0
Nodes (1): RolesTable

### Community 326 - "Community 326"
Cohesion: 1.0
Nodes (1): UsersTable

### Community 327 - "Community 327"
Cohesion: 1.0
Nodes (1): InvitationServiceImpl

### Community 328 - "Community 328"
Cohesion: 1.0
Nodes (1): InvitationsTable

### Community 329 - "Community 329"
Cohesion: 1.0
Nodes (1): UserGroupMembershipsTable

### Community 330 - "Community 330"
Cohesion: 1.0
Nodes (1): GroupsTable

### Community 331 - "Community 331"
Cohesion: 1.0
Nodes (1): InvitationService

### Community 332 - "Community 332"
Cohesion: 1.0
Nodes (0): 

### Community 333 - "Community 333"
Cohesion: 1.0
Nodes (0): 

### Community 334 - "Community 334"
Cohesion: 1.0
Nodes (0): 

### Community 335 - "Community 335"
Cohesion: 1.0
Nodes (1): ConversationsTable

### Community 336 - "Community 336"
Cohesion: 1.0
Nodes (0): 

### Community 337 - "Community 337"
Cohesion: 1.0
Nodes (0): 

### Community 338 - "Community 338"
Cohesion: 1.0
Nodes (0): 

### Community 339 - "Community 339"
Cohesion: 1.0
Nodes (0): 

### Community 340 - "Community 340"
Cohesion: 1.0
Nodes (0): 

### Community 341 - "Community 341"
Cohesion: 1.0
Nodes (0): 

### Community 342 - "Community 342"
Cohesion: 1.0
Nodes (0): 

### Community 343 - "Community 343"
Cohesion: 1.0
Nodes (0): 

### Community 344 - "Community 344"
Cohesion: 1.0
Nodes (0): 

### Community 345 - "Community 345"
Cohesion: 1.0
Nodes (0): 

### Community 346 - "Community 346"
Cohesion: 1.0
Nodes (0): 

### Community 347 - "Community 347"
Cohesion: 1.0
Nodes (0): 

### Community 348 - "Community 348"
Cohesion: 1.0
Nodes (0): 

### Community 349 - "Community 349"
Cohesion: 1.0
Nodes (0): 

### Community 350 - "Community 350"
Cohesion: 1.0
Nodes (0): 

### Community 351 - "Community 351"
Cohesion: 1.0
Nodes (0): 

### Community 352 - "Community 352"
Cohesion: 1.0
Nodes (0): 

### Community 353 - "Community 353"
Cohesion: 1.0
Nodes (0): 

### Community 354 - "Community 354"
Cohesion: 1.0
Nodes (0): 

### Community 355 - "Community 355"
Cohesion: 1.0
Nodes (0): 

### Community 356 - "Community 356"
Cohesion: 1.0
Nodes (0): 

### Community 357 - "Community 357"
Cohesion: 1.0
Nodes (0): 

### Community 358 - "Community 358"
Cohesion: 1.0
Nodes (0): 

### Community 359 - "Community 359"
Cohesion: 1.0
Nodes (0): 

### Community 360 - "Community 360"
Cohesion: 1.0
Nodes (0): 

### Community 361 - "Community 361"
Cohesion: 1.0
Nodes (0): 

### Community 362 - "Community 362"
Cohesion: 1.0
Nodes (0): 

### Community 363 - "Community 363"
Cohesion: 1.0
Nodes (0): 

### Community 364 - "Community 364"
Cohesion: 1.0
Nodes (0): 

### Community 365 - "Community 365"
Cohesion: 1.0
Nodes (0): 

### Community 366 - "Community 366"
Cohesion: 1.0
Nodes (0): 

### Community 367 - "Community 367"
Cohesion: 1.0
Nodes (0): 

### Community 368 - "Community 368"
Cohesion: 1.0
Nodes (0): 

### Community 369 - "Community 369"
Cohesion: 1.0
Nodes (0): 

### Community 370 - "Community 370"
Cohesion: 1.0
Nodes (0): 

### Community 371 - "Community 371"
Cohesion: 1.0
Nodes (0): 

### Community 372 - "Community 372"
Cohesion: 1.0
Nodes (0): 

### Community 373 - "Community 373"
Cohesion: 1.0
Nodes (0): 

### Community 374 - "Community 374"
Cohesion: 1.0
Nodes (0): 

### Community 375 - "Community 375"
Cohesion: 1.0
Nodes (0): 

### Community 376 - "Community 376"
Cohesion: 1.0
Nodes (0): 

### Community 377 - "Community 377"
Cohesion: 1.0
Nodes (0): 

### Community 378 - "Community 378"
Cohesion: 1.0
Nodes (0): 

### Community 379 - "Community 379"
Cohesion: 1.0
Nodes (0): 

### Community 380 - "Community 380"
Cohesion: 1.0
Nodes (0): 

### Community 381 - "Community 381"
Cohesion: 1.0
Nodes (0): 

### Community 382 - "Community 382"
Cohesion: 1.0
Nodes (0): 

### Community 383 - "Community 383"
Cohesion: 1.0
Nodes (0): 

### Community 384 - "Community 384"
Cohesion: 1.0
Nodes (0): 

## Knowledge Gaps
- **552 isolated node(s):** `Check`, `Route`, `GroupRole`, `Owner`, `Admin` (+547 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **Thin community `Community 236`** (2 nodes): `Route.kt`, `Route`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 237`** (2 nodes): `ErrorResponse.kt`, `ErrorResponse`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 238`** (2 nodes): `FileDtos.kt`, `FileResponse`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 239`** (2 nodes): `StringKey.kt`, `StringKey`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 240`** (2 nodes): `MviViewModelTestDsl.kt`, `test()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 241`** (2 nodes): `PlatformEngine.ios.kt`, `platformEngine()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 242`** (2 nodes): `PlatformConfig.ios.kt`, `defaultBaseUrl()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 243`** (2 nodes): `PlatformEngine.kt`, `platformEngine()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 244`** (2 nodes): `Sdk.kt`, `Sdk`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 245`** (2 nodes): `PlatformConfig.kt`, `defaultBaseUrl()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 246`** (2 nodes): `ApiClient.kt`, `createApiClient()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 247`** (2 nodes): `PlatformEngine.jvm.kt`, `platformEngine()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 248`** (2 nodes): `PlatformConfig.jvm.kt`, `defaultBaseUrl()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 249`** (2 nodes): `PlatformEngine.wasmJs.kt`, `platformEngine()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 250`** (2 nodes): `PlatformConfig.android.kt`, `defaultBaseUrl()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 251`** (2 nodes): `PlatformEngine.android.kt`, `platformEngine()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 252`** (2 nodes): `ImageDecoder.kt`, `rememberDecodedImage()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 253`** (2 nodes): `TerminalTheme.kt`, `TerminalTheme()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 254`** (2 nodes): `TerminalBorders.kt`, `TerminalBorders`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 255`** (2 nodes): `TerminalGap.kt`, `TerminalGap`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 256`** (2 nodes): `TerminalTypography.kt`, `TerminalTypography`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 257`** (2 nodes): `TerminalSpacing.kt`, `TerminalSpacing`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 258`** (2 nodes): `TerminalColors.kt`, `TerminalColors`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 259`** (2 nodes): `TerminalOpacity.kt`, `TerminalOpacity`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 260`** (2 nodes): `TerminalRadius.kt`, `TerminalRadius`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 261`** (2 nodes): `ImageDecoder.jvm.kt`, `rememberDecodedImage()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 262`** (2 nodes): `ImagePicker.jvm.kt`, `rememberImagePickerLauncher()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 263`** (2 nodes): `ImageDecoder.wasmJs.kt`, `rememberDecodedImage()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 264`** (2 nodes): `ImagePicker.wasmJs.kt`, `rememberImagePickerLauncher()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 265`** (2 nodes): `ImageDecoder.android.kt`, `rememberDecodedImage()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 266`** (2 nodes): `ImagePicker.android.kt`, `rememberImagePickerLauncher()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 267`** (2 nodes): `LegalDocumentModel.kt`, `LegalDocumentModel`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 268`** (2 nodes): `PrivacySettingsModel.kt`, `PrivacySettingsModel`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 269`** (2 nodes): `PrivacyNavigation.kt`, `privacyEntries()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 270`** (2 nodes): `InviteLinkChecker.ios.kt`, `checkInviteLink()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 271`** (2 nodes): `OAuthCallbackChecker.ios.kt`, `checkOAuthCallback()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 272`** (2 nodes): `Platform.ios.kt`, `showAppleSignIn()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 273`** (2 nodes): `StringKeyResolver.kt`, `resolveStringKey()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 274`** (2 nodes): `Platform.kt`, `showAppleSignIn()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 275`** (2 nodes): `InviteLinkChecker.kt`, `checkInviteLink()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 276`** (2 nodes): `InviteAcceptModel.kt`, `InviteAcceptModel`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 277`** (2 nodes): `OAuthCallbackChecker.kt`, `checkOAuthCallback()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 278`** (2 nodes): `OAuthCallbackHandler.kt`, `OAuthCallbackHandler()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 279`** (2 nodes): `LoginModel.kt`, `LoginModel`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 280`** (2 nodes): `RegisterModel.kt`, `RegisterModel`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 281`** (2 nodes): `Platform.jvm.kt`, `showAppleSignIn()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 282`** (2 nodes): `OAuthCallbackChecker.jvm.kt`, `checkOAuthCallback()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 283`** (2 nodes): `InviteLinkChecker.jvm.kt`, `checkInviteLink()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 284`** (2 nodes): `OAuthCallbackChecker.wasmJs.kt`, `checkOAuthCallback()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 285`** (2 nodes): `InviteLinkChecker.wasmJs.kt`, `checkInviteLink()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 286`** (2 nodes): `Platform.wasmJs.kt`, `showAppleSignIn()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 287`** (2 nodes): `InviteLinkChecker.android.kt`, `checkInviteLink()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 288`** (2 nodes): `Platform.android.kt`, `showAppleSignIn()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 289`** (2 nodes): `OAuthCallbackChecker.android.kt`, `checkOAuthCallback()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 290`** (2 nodes): `AuthNavigation.kt`, `authEntries()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 291`** (2 nodes): `AdminPanelModel.kt`, `AdminPanelModel`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 292`** (2 nodes): `RegisterMemberModel.kt`, `RegisterMemberModel`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 293`** (2 nodes): `RegisterMemberScreen.kt`, `RegisterMemberScreen()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 294`** (2 nodes): `AdminNavigation.kt`, `adminEntries()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 295`** (2 nodes): `DashboardModel.kt`, `DashboardModel`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 296`** (2 nodes): `DashboardRoute.kt`, `DashboardRoute`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 297`** (2 nodes): `DashboardNavigation.kt`, `dashboardEntries()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 298`** (2 nodes): `PremiumTierContent.kt`, `PremiumTierContent()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 299`** (2 nodes): `ProfileRoute.kt`, `ProfileRoute`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 300`** (2 nodes): `ProfileNavigation.kt`, `profileEntries()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 301`** (2 nodes): `DocumentsModel.kt`, `DocumentsModel`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 302`** (2 nodes): `DocumentsScreen.kt`, `DocumentsScreen()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 303`** (2 nodes): `DocumentsRoute.kt`, `DocumentsRoute`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 304`** (2 nodes): `DocumentsNavigation.kt`, `documentsEntries()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 305`** (2 nodes): `DocumentEmbeddingsTable.kt`, `DocumentEmbeddingsTable`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 306`** (2 nodes): `DocumentsTable.kt`, `DocumentsTable`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 307`** (2 nodes): `MigrationsTable.kt`, `MigrationsTable`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 308`** (2 nodes): `Configuration.kt`, `Configuration`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 309`** (2 nodes): `Error.kt`, `preferredLanguage()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 310`** (2 nodes): `SecurityPlugin.kt`, `configureSecurity()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 311`** (2 nodes): `LegalDocumentServiceImpl.kt`, `LegalDocumentServiceImpl`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 312`** (2 nodes): `ConsentServiceImpl.kt`, `ConsentServiceImpl`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 313`** (2 nodes): `DeletionRoutes.kt`, `deletionRoutes()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 314`** (2 nodes): `LegalRoutes.kt`, `legalRoutes()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 315`** (2 nodes): `ExportRoutes.kt`, `exportRoutes()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 316`** (2 nodes): `ConsentRoutes.kt`, `consentRoutes()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 317`** (2 nodes): `LegalDocumentsTable.kt`, `LegalDocumentsTable`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 318`** (2 nodes): `AccountDeletionRequestsTable.kt`, `AccountDeletionRequestsTable`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 319`** (2 nodes): `DataExportRequestsTable.kt`, `DataExportRequestsTable`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 320`** (2 nodes): `ConsentRecordsTable.kt`, `ConsentRecordsTable`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 321`** (2 nodes): `PrivacyWireModule.kt`, `registerPrivacyWireMigrations()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 322`** (2 nodes): `OAuthService.kt`, `GoogleUserInfo`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 323`** (2 nodes): `AuthServiceImpl.kt`, `AuthServiceImpl`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 324`** (2 nodes): `UserRoutes.kt`, `userRoutes()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 325`** (2 nodes): `RolesTable.kt`, `RolesTable`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 326`** (2 nodes): `UsersTable.kt`, `UsersTable`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 327`** (2 nodes): `InvitationServiceImpl.kt`, `InvitationServiceImpl`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 328`** (2 nodes): `InvitationsTable.kt`, `InvitationsTable`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 329`** (2 nodes): `UserGroupMembershipsTable.kt`, `UserGroupMembershipsTable`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 330`** (2 nodes): `GroupsTable.kt`, `GroupsTable`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 331`** (2 nodes): `InvitationService.kt`, `InvitationService`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 332`** (2 nodes): `GroupWireModule.kt`, `registerGroupWireMigrations()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 333`** (2 nodes): `ChatAgent.kt`, `streamChat()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 334`** (2 nodes): `ChatStreamingStrategy.kt`, `chatStreamingStrategy()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 335`** (2 nodes): `ConversationsTable.kt`, `ConversationsTable`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 336`** (2 nodes): `DocumentRoutes.kt`, `documentRoutes()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 337`** (2 nodes): `AiWireModule.kt`, `registerAiWireMigrations()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 338`** (2 nodes): `FileRoutes.kt`, `fileRoutes()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 339`** (2 nodes): `Application.kt`, `main()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 340`** (2 nodes): `Server.kt`, `startServer()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 341`** (2 nodes): `AvatarRoutes.kt`, `avatarRoutes()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 342`** (2 nodes): `MainViewController.kt`, `MainViewController()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 343`** (2 nodes): `AppNavHost.kt`, `AppNavHost()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 344`** (2 nodes): `LocaleSelector.kt`, `LocaleSelector()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 345`** (2 nodes): `main.kt`, `main()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 346`** (1 nodes): `settings.gradle.kts`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 347`** (1 nodes): `StorageModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 348`** (1 nodes): `Annotations.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 349`** (1 nodes): `SdkModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 350`** (1 nodes): `TerminalPreview.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 351`** (1 nodes): `PrivacyModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 352`** (1 nodes): `AuthScreen.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 353`** (1 nodes): `AuthModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 354`** (1 nodes): `AdminModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 355`** (1 nodes): `DashboardModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 356`** (1 nodes): `ProfileModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 357`** (1 nodes): `DocumentsModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 358`** (1 nodes): `Startup.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 359`** (1 nodes): `Module.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 360`** (1 nodes): `DataExportServiceImpl.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 361`** (1 nodes): `DataExportService.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 362`** (1 nodes): `ConsentService.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 363`** (1 nodes): `LegalDocumentService.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 364`** (1 nodes): `EmailModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 365`** (1 nodes): `AuthService.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 366`** (1 nodes): `GroupModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 367`** (1 nodes): `Models.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 368`** (1 nodes): `AiModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 369`** (1 nodes): `AssistantAgent.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 370`** (1 nodes): `AiRoutes.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 371`** (1 nodes): `FileModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 372`** (1 nodes): `S3FileService.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 373`** (1 nodes): `FileService.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 374`** (1 nodes): `FileWireModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 375`** (1 nodes): `ServerModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 376`** (1 nodes): `Config.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 377`** (1 nodes): `SharedModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 378`** (1 nodes): `spa-routing.js`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 379`** (1 nodes): `AppModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 380`** (1 nodes): `LocalAppLocale.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 381`** (1 nodes): `AndroidModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 382`** (1 nodes): `kover-convention.gradle.kts`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 383`** (1 nodes): `server-module-convention.gradle.kts`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 384`** (1 nodes): `kmp-library-convention.gradle.kts`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What connects `Check`, `Route`, `GroupRole` to the rest of the system?**
  _552 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Community 0` be split into smaller, more focused modules?**
  _Cohesion score 0.04 - nodes in this community are weakly interconnected._
- **Should `Community 1` be split into smaller, more focused modules?**
  _Cohesion score 0.04 - nodes in this community are weakly interconnected._
- **Should `Community 2` be split into smaller, more focused modules?**
  _Cohesion score 0.07 - nodes in this community are weakly interconnected._
- **Should `Community 3` be split into smaller, more focused modules?**
  _Cohesion score 0.1 - nodes in this community are weakly interconnected._
- **Should `Community 4` be split into smaller, more focused modules?**
  _Cohesion score 0.1 - nodes in this community are weakly interconnected._
- **Should `Community 5` be split into smaller, more focused modules?**
  _Cohesion score 0.1 - nodes in this community are weakly interconnected._