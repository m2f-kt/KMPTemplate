# Graph Report - .  (2026-05-03)

## Corpus Check
- Large corpus: 467 files · ~117,928 words. Semantic extraction will be expensive (many Claude tokens). Consider running on a subfolder, or use --no-semantic to run AST-only.

## Summary
- 2054 nodes · 1689 edges · 393 communities detected
- Extraction: 99% EXTRACTED · 1% INFERRED · 0% AMBIGUOUS · INFERRED: 17 edges (avg confidence: 0.83)
- Token cost: 0 input · 0 output

## God Nodes (most connected - your core abstractions)
1. `GroupRoutesTest` - 17 edges
2. `FakePrivacyApiBuilder` - 15 edges
3. `PrivacyApi` - 14 edges
4. `PrivacyApiImpl` - 14 edges
5. `AccountDeletionViewModelTest` - 12 edges
6. `core:sdk Module` - 12 edges
7. `FakeGroupApiBuilder` - 11 edges
8. `AccountDeletionViewModel` - 11 edges
9. `AdminPanelViewModel` - 11 edges
10. `PgVectorStorage` - 11 edges

## Surprising Connections (you probably didn't know these)
- `Getting Started 8-Step Walkthrough` --references--> `gradlew devSetup`  [EXTRACTED]
  docs/GETTING-STARTED.md → README.md
- `Getting Started 8-Step Walkthrough` --references--> `gradlew seedData`  [EXTRACTED]
  docs/GETTING-STARTED.md → README.md
- `Client→Server Data Flow` --references--> `core:sdk Module`  [EXTRACTED]
  docs/ARCHITECTURE.md → CLAUDE.md
- `Koin Module Registration Pattern` --references--> `server Module (Ktor Entry Point)`  [EXTRACTED]
  docs/ARCHITECTURE.md → CLAUDE.md
- `server:core:database Module` --implements--> `MigrationRegistry Concept`  [EXTRACTED]
  CLAUDE.md → docs/ARCHITECTURE.md

## Hyperedges (group relationships)
- **Pencil Design <-> Compose Code Deterministic Mapping Chain** — claudemd_terminal_design_generator, claudemd_terminal_design_implementer, claudemd_terminal_design_sync, claudemd_pencil_pen_file, module_app_designsystem, claudemd_terminal_theme [EXTRACTED 0.95]
- **Client Feature Submodule Contract/Impl/Wire Pattern** — claudemd_contract_impl_wire, module_app_auth, module_app_admin, module_app_dashboard, module_app_documents, module_app_profile, module_core_navigation [EXTRACTED 0.95]
- **End-to-End Login Request Flow** — arch_login_example, module_app_auth, module_core_sdk, module_server_auth, claudemd_mvi_pattern, claudemd_arrow_raise [EXTRACTED 0.90]
- **Launcher icon set — all platform/density renditions of the app launcher icon (iOS 1024 + Android mdpi/hdpi/xhdpi/xxhdpi/xxxhdpi square and round)** — launcher_variant_ios_1024, launcher_variant_android_mdpi_square, launcher_variant_android_mdpi_round, launcher_variant_android_hdpi_square, launcher_variant_android_hdpi_round, launcher_variant_android_xhdpi_square, launcher_variant_android_xhdpi_round, launcher_variant_android_xxhdpi_square, launcher_variant_android_xxhdpi_round, launcher_variant_android_xxxhdpi_square, launcher_variant_android_xxxhdpi_round [EXTRACTED 1.00]

## Communities

### Community 0 - "Architecture & Conventions"
Cohesion: 0.05
Nodes (58): Client→Server Data Flow, Env.kt Configuration, Koin Module Registration Pattern, Login Concrete Example Flow, MigrationRegistry Concept, Server Feature Module Layout (routes/service/repository/tables/migrations), apiCall() Wrapper, AuthInterceptor (+50 more)

### Community 1 - "API Routes Catalog"
Cohesion: 0.04
Nodes (51): Accept, ActiveExport, AddMember, Ai, Assistant, Auth, Avatar, ById (+43 more)

### Community 2 - "AppError Hierarchy"
Cohesion: 0.04
Nodes (48): AccessDenied, AgentFailed, AgentNotFound, AI, AlreadyAccepted, AlreadyExists, AppError, Auth (+40 more)

### Community 3 - "AdminPanelMutation"
Cohesion: 0.07
Nodes (29): AdminPanelMutation, AppendMembers, HideCreateGroupDialog, HideInviteDialog, HideRemoveMemberDialog, HideRevokeDialog, RemoveMemberFromList, SetCreateGroupError (+21 more)

### Community 4 - "AdminPanelIntent"
Cohesion: 0.1
Nodes (20): AdminPanelIntent, CancelRemoveMember, CancelRevoke, CloseCreateGroupDialog, CloseInviteDialog, ConfirmRemoveMember, ConfirmRevokeInvitation, CreateGroupNameChanged (+12 more)

### Community 5 - "Server Test Helpers"
Cohesion: 0.1
Nodes (4): FakeEmbeddingProvider, NoOpEmailService, TestDatabase, TestMinIO

### Community 6 - "GroupRoutes"
Cohesion: 0.1
Nodes (1): GroupRoutesTest

### Community 7 - "ConsentService"
Cohesion: 0.11
Nodes (3): ConsentServiceTest, FakeConsentRepository, FakeLegalDocumentRepository

### Community 8 - "ProfileMutation"
Cohesion: 0.11
Nodes (15): CancelEdit, HideCropDialog, ProfileMutation, SetAvatarUrl, SetEditEmail, SetEditName, SetFieldErrors, SetLoading (+7 more)

### Community 9 - "MviViewModelDsl"
Cohesion: 0.12
Nodes (11): CounterEvent, CounterIntent, CounterModel, CounterMutation, CounterViewModel, Decrement, Done, Increment (+3 more)

### Community 10 - "FakePrivacyApiBuilder"
Cohesion: 0.12
Nodes (1): FakePrivacyApiBuilder

### Community 11 - "PrivacyApi"
Cohesion: 0.13
Nodes (1): PrivacyApi

### Community 12 - "PrivacyApiImpl"
Cohesion: 0.13
Nodes (1): PrivacyApiImpl

### Community 13 - "DashboardScreen"
Cohesion: 0.13
Nodes (0): 

### Community 14 - "PrivacyDtos"
Cohesion: 0.14
Nodes (13): ConsentStatus, ConsentType, DataExportResponse, DeletionRequest, DeletionResponse, DeletionStatus, ExportStatus, GrantConsentRequest (+5 more)

### Community 15 - "ProfileIntent"
Cohesion: 0.14
Nodes (11): CancelEditing, CropCancelled, CropConfirmed, EditEmailChanged, EditNameChanged, ImageSelected, LoadProfile, LogoutClicked (+3 more)

### Community 16 - "Groups"
Cohesion: 0.14
Nodes (6): AddRevokedAtColumnMigration, CreateGroupsTableMigration, CreateInvitationsTableMigration, CreateMembershipsTableMigration, SeedDefaultGroupMigration, SeedDevTestGroupsMigration

### Community 17 - "RelativeTime"
Cohesion: 0.15
Nodes (10): DaysAgo, Fallback, HoursAgo, JustNow, MinutesAgo, MonthsAgo, RelativeBucket, WeeksAgo (+2 more)

### Community 18 - "AccountDeletionScreen"
Cohesion: 0.15
Nodes (0): 

### Community 19 - "AccountDeletionViewModel"
Cohesion: 0.15
Nodes (1): AccountDeletionViewModelTest

### Community 20 - "RegisterMutation"
Cohesion: 0.15
Nodes (12): RegisterMutation, SetConfirmPassword, SetEmail, SetFieldErrors, SetFirstName, SetInvitationEmail, SetInvitationToken, SetLastName (+4 more)

### Community 21 - "InvitationRepository"
Cohesion: 0.15
Nodes (2): InvitationRecord, InvitationRepository

### Community 22 - "GroupServiceImpl"
Cohesion: 0.15
Nodes (1): GroupServiceImpl

### Community 23 - "PgVectorStorage"
Cohesion: 0.15
Nodes (2): PgVectorStorage, SimilarChunk

### Community 24 - "Launcher Icons"
Cohesion: 0.21
Nodes (13): Android adaptive icons — Android system that masks launcher icons (ic_launcher_round.png used by launchers requesting circular masks on older devices, plus adaptive foreground/background composition on newer ones), App launcher icon: white outlined isometric cube containing a centered hexagon, set on a flat cobalt/royal blue square background — minimal, geometric, modern tech mark suggesting modularity, structure, and a dimensional 'box of code', Android launcher icon variant — hdpi (~72x72) round ic_launcher_round (adaptive icon system), Android launcher icon variant — hdpi (~72x72) square ic_launcher, Android launcher icon variant — mdpi (~48x48) round ic_launcher_round (adaptive icon system), Android launcher icon variant — mdpi (~48x48) square ic_launcher, Android launcher icon variant — xhdpi (~96x96) round ic_launcher_round (adaptive icon system), Android launcher icon variant — xhdpi (~96x96) square ic_launcher (+5 more)

### Community 25 - "FakeGroupApiBuilder"
Cohesion: 0.17
Nodes (1): FakeGroupApiBuilder

### Community 26 - "AccountDeletionViewModel"
Cohesion: 0.17
Nodes (1): AccountDeletionViewModel

### Community 27 - "LoginMutation"
Cohesion: 0.17
Nodes (11): LoginMutation, ResetState, SetAcceptingInvitation, SetEmail, SetInvitationEmail, SetInvitationToken, SetLoading, SetPassword (+3 more)

### Community 28 - "AdminPanelViewModel"
Cohesion: 0.17
Nodes (1): AdminPanelViewModel

### Community 29 - "Privacy"
Cohesion: 0.17
Nodes (5): CreateAccountDeletionRequestsTableMigration, CreateConsentRecordsTableMigration, CreateDataExportRequestsTableMigration, CreateLegalDocumentsTableMigration, SeedMarketingAnalyticsDocumentsMigration

### Community 30 - "Auth"
Cohesion: 0.17
Nodes (5): CreatePasswordResetTokensTableMigration, CreateRefreshTokensTableMigration, CreateRolesTableAndMigrateUsersMigration, CreateUsersTableMigration, DropProcessingRestrictedFromUsersMigration

### Community 31 - "FakeSdkBuilder"
Cohesion: 0.18
Nodes (1): FakeSdkBuilder

### Community 32 - "GroupApiImpl"
Cohesion: 0.18
Nodes (1): GroupApiImpl

### Community 33 - "GroupApi"
Cohesion: 0.18
Nodes (1): GroupApi

### Community 34 - "RegisterIntent"
Cohesion: 0.18
Nodes (10): ConfirmPasswordChanged, EmailChanged, FirstNameChanged, LastNameChanged, PasswordChanged, RegisterIntent, SetInvitationEmail, SetInvitationToken (+2 more)

### Community 35 - "DashboardViewModel"
Cohesion: 0.18
Nodes (1): DashboardViewModelTest

### Community 36 - "ExposedUserRepository"
Cohesion: 0.18
Nodes (1): ExposedUserRepository

### Community 37 - "GroupRepository"
Cohesion: 0.18
Nodes (2): GroupRecord, GroupRepository

### Community 38 - "MembershipRepository"
Cohesion: 0.18
Nodes (3): MembershipRecord, MembershipRepository, MemberWithUserRecord

### Community 39 - "DocumentRoutes"
Cohesion: 0.18
Nodes (1): DocumentRoutesTest

### Community 40 - "TerminalRipple"
Cohesion: 0.2
Nodes (2): TerminalRippleIndication, TerminalRippleNode

### Community 41 - "AccountDeletionIntent"
Cohesion: 0.2
Nodes (9): AccountDeletionIntent, CancelDeletion, ConfirmDeletion, Load, LogOut, ProceedToReAuth, ReAuthenticate, SetReason (+1 more)

### Community 42 - "PrivacySettingsViewModel"
Cohesion: 0.2
Nodes (1): PrivacySettingsViewModelTest

### Community 43 - "RegisterScreen"
Cohesion: 0.2
Nodes (0): 

### Community 44 - "RegisterMemberMutation"
Cohesion: 0.2
Nodes (9): RegisterMemberMutation, SetEmail, SetFieldErrors, SetFirstName, SetLastName, SetLoading, SetPassword, SetRole (+1 more)

### Community 45 - "ProfileViewModel"
Cohesion: 0.2
Nodes (1): ProfileViewModel

### Community 46 - "ProfileScreen"
Cohesion: 0.2
Nodes (0): 

### Community 47 - "ExposedDataExportRepository"
Cohesion: 0.2
Nodes (1): ExposedDataExportRepository

### Community 48 - "DataExportRepository"
Cohesion: 0.2
Nodes (2): DataExportRecord, DataExportRepository

### Community 49 - "UserRepository"
Cohesion: 0.2
Nodes (2): UserRecord, UserRepository

### Community 50 - "InvitationRoutes"
Cohesion: 0.2
Nodes (1): InvitationRoutesTest

### Community 51 - "MembershipRepositoryImpl"
Cohesion: 0.2
Nodes (1): MembershipRepositoryImpl

### Community 52 - "DocumentRepository"
Cohesion: 0.2
Nodes (2): DocumentRepository, DocumentRow

### Community 53 - "GroupRole"
Cohesion: 0.22
Nodes (5): Admin, GroupRole, GroupRoleSerializer, Member, Owner

### Community 54 - "UserRole"
Cohesion: 0.22
Nodes (5): Admin, PowerAdmin, User, UserRole, UserRoleSerializer

### Community 55 - "GroupDtos"
Cohesion: 0.22
Nodes (8): AddMemberRequest, CreateGroupRequest, GroupResponse, MemberResponse, MembershipSummary, PaginatedMemberResponse, RegisterMemberRequest, UpdateGroupRequest

### Community 56 - "FakeInvitationApiBuilder"
Cohesion: 0.22
Nodes (1): FakeInvitationApiBuilder

### Community 57 - "FakeAuthApiBuilder"
Cohesion: 0.22
Nodes (1): FakeAuthApiBuilder

### Community 58 - "PrivacySettingsViewModel"
Cohesion: 0.22
Nodes (1): PrivacySettingsViewModel

### Community 59 - "AccountDeletionMutation"
Cohesion: 0.22
Nodes (8): AccountDeletionMutation, SetConfirmationToken, SetError, SetLoading, SetPendingDeletion, SetReason, SetStep, SetUserEmail

### Community 60 - "LoginIntent"
Cohesion: 0.22
Nodes (8): EmailChanged, LoginIntent, PasswordChanged, RememberMeChanged, Reset, SetInvitationEmail, SetInvitationToken, SubmitLoginClicked

### Community 61 - "InviteAcceptMutation"
Cohesion: 0.22
Nodes (8): InviteAcceptMutation, SetAccepting, SetAcceptSuccess, SetError, SetInvitationDetails, SetLoadingInvitation, SetLoggedIn, SetToken

### Community 62 - "AdminPanelViewModel"
Cohesion: 0.22
Nodes (1): AdminPanelViewModelTest

### Community 63 - "Env"
Cohesion: 0.22
Nodes (7): Ai, Auth, Email, Http, OAuth, S3, ServerConfig

### Community 64 - "ExposedAccountDeletionRepository"
Cohesion: 0.22
Nodes (1): ExposedAccountDeletionRepository

### Community 65 - "AccountDeletionRepository"
Cohesion: 0.22
Nodes (2): AccountDeletionRecord, AccountDeletionRepository

### Community 66 - "FileRoutes"
Cohesion: 0.22
Nodes (1): FileRoutesTest

### Community 67 - "AppLocale.wasmJs"
Cohesion: 0.22
Nodes (0): 

### Community 68 - "UserTier"
Cohesion: 0.25
Nodes (6): Admin, Free, Paid, PowerAdmin, Premium, UserTier

### Community 69 - "FakeUserApiBuilder"
Cohesion: 0.25
Nodes (1): FakeUserApiBuilder

### Community 70 - "AuthApi"
Cohesion: 0.25
Nodes (1): AuthApi

### Community 71 - "InvitationApiImpl"
Cohesion: 0.25
Nodes (1): InvitationApiImpl

### Community 72 - "AuthApiImpl"
Cohesion: 0.25
Nodes (1): AuthApiImpl

### Community 73 - "InvitationApi"
Cohesion: 0.25
Nodes (1): InvitationApi

### Community 74 - "TerminalCard"
Cohesion: 0.25
Nodes (1): CardVariant

### Community 75 - "TerminalTable"
Cohesion: 0.25
Nodes (0): 

### Community 76 - "LoginViewModel"
Cohesion: 0.25
Nodes (1): LoginViewModelTest

### Community 77 - "AdminPanelScreen"
Cohesion: 0.25
Nodes (0): 

### Community 78 - "RegisterMemberIntent"
Cohesion: 0.25
Nodes (7): EmailChanged, FirstNameChanged, LastNameChanged, PasswordChanged, RegisterMemberIntent, RoleChanged, SubmitRegisterMember

### Community 79 - "DashboardMutation"
Cohesion: 0.25
Nodes (7): DashboardMutation, SetAvatarUrl, SetLoading, SetMembership, SetNavItem, SetSystemAdmin, SetUserName

### Community 80 - "DocumentsMutation"
Cohesion: 0.25
Nodes (7): AddDocument, DocumentsMutation, RemoveDocument, SetDocuments, SetError, SetLoading, SetUploading

### Community 81 - "VectorColumnType"
Cohesion: 0.25
Nodes (1): VectorColumnType

### Community 82 - "ExposedConsentRepository"
Cohesion: 0.25
Nodes (1): ExposedConsentRepository

### Community 83 - "ConsentRepository"
Cohesion: 0.25
Nodes (2): ConsentRecord, ConsentRepository

### Community 84 - "GroupService"
Cohesion: 0.25
Nodes (1): GroupService

### Community 85 - "AuthDtos"
Cohesion: 0.29
Nodes (6): AuthResponse, ForgotPasswordRequest, LoginRequest, RefreshTokenRequest, RegisterRequest, ResetPasswordRequest

### Community 86 - "FakeDocumentApiBuilder"
Cohesion: 0.29
Nodes (1): FakeDocumentApiBuilder

### Community 87 - "UserApi"
Cohesion: 0.29
Nodes (1): UserApi

### Community 88 - "UserApiImpl"
Cohesion: 0.29
Nodes (1): UserApiImpl

### Community 89 - "ConsentGateViewModel"
Cohesion: 0.29
Nodes (1): ConsentGateViewModel

### Community 90 - "AccountDeletionEvent"
Cohesion: 0.29
Nodes (6): AccountDeletionEvent, DeletionCancelled, DeletionScheduled, LoggedOut, NavigateToLogin, ShowError

### Community 91 - "PrivacySettingsIntent"
Cohesion: 0.29
Nodes (6): DownloadExport, Load, PrivacySettingsIntent, RequestExport, ToggleConsent, ViewDocument

### Community 92 - "PrivacySettingsMutation"
Cohesion: 0.29
Nodes (6): PrivacySettingsMutation, SetConsents, SetDeletionStatus, SetError, SetExportStatus, SetLoading

### Community 93 - "ConsentGateViewModel"
Cohesion: 0.29
Nodes (1): ConsentGateViewModelTest

### Community 94 - "InviteAcceptIntent"
Cohesion: 0.29
Nodes (6): AcceptInvitation, GoToLogin, GoToRegister, InviteAcceptIntent, LoadInvitation, RequestNewInvitation

### Community 95 - "LoginViewModel"
Cohesion: 0.29
Nodes (1): LoginViewModel

### Community 96 - "LoginScreen"
Cohesion: 0.29
Nodes (0): 

### Community 97 - "RegisterViewModel"
Cohesion: 0.29
Nodes (1): RegisterViewModel

### Community 98 - "RegisterViewModel"
Cohesion: 0.29
Nodes (1): RegisterViewModelTest

### Community 99 - "AuthRoutes"
Cohesion: 0.29
Nodes (5): ForgotPasswordRoute, InviteAcceptRoute, LoginRoute, OAuthCallbackRoute, RegisterRoute

### Community 100 - "RegisterMemberViewModel"
Cohesion: 0.29
Nodes (1): RegisterMemberViewModelTest

### Community 101 - "DashboardIntent"
Cohesion: 0.29
Nodes (6): AdminPanelClicked, DashboardIntent, LoadDashboard, LogoutClicked, NavItemSelected, RefreshProfile

### Community 102 - "DocumentsViewModel"
Cohesion: 0.29
Nodes (1): DocumentsViewModel

### Community 103 - "ContentView"
Cohesion: 0.29
Nodes (4): ComposeView, ContentView, UIViewControllerRepresentable, View

### Community 104 - "StructuredOutput"
Cohesion: 0.29
Nodes (1): StructuredOutputTest

### Community 105 - "build.gradle"
Cohesion: 0.33
Nodes (1): Check

### Community 106 - "AiDtos"
Cohesion: 0.33
Nodes (5): AgentRequest, AgentResponse, ChatRequest, ChatResponse, ChatStreamFrame

### Community 107 - "DocumentApi"
Cohesion: 0.33
Nodes (1): DocumentApi

### Community 108 - "DocumentApiImpl"
Cohesion: 0.33
Nodes (1): DocumentApiImpl

### Community 109 - "MviViewModel"
Cohesion: 0.33
Nodes (1): MviViewModel

### Community 110 - "ConsentGateIntent"
Cohesion: 0.33
Nodes (5): AcceptAll, ConsentGateIntent, LoadRequiredConsents, ToggleConsent, ViewDocument

### Community 111 - "PrivacySettingsScreen"
Cohesion: 0.33
Nodes (0): 

### Community 112 - "PrivacySettingsEvent"
Cohesion: 0.33
Nodes (5): ExportReady, NavigateToDeletion, NavigateToDocument, PrivacySettingsEvent, ShowError

### Community 113 - "ConsentGateMutation"
Cohesion: 0.33
Nodes (5): ConsentGateMutation, SetConsents, SetError, SetLoading, UpdateConsentToggle

### Community 114 - "RegisterEvent"
Cohesion: 0.33
Nodes (5): NavigateToConsentGate, NavigateToDashboard, NavigateToGroup, RegisterEvent, ViewLegalDocument

### Community 115 - "InviteAcceptViewModel"
Cohesion: 0.33
Nodes (1): InviteAcceptViewModel

### Community 116 - "InviteAcceptEvent"
Cohesion: 0.33
Nodes (5): InviteAcceptEvent, NavigateToGroup, NavigateToLogin, NavigateToRegister, RequestedNewInvitation

### Community 117 - "DashboardMockData"
Cohesion: 0.33
Nodes (5): ActivityItem, DashboardMockData, DeploymentStatus, MetricItem, ProcessItem

### Community 118 - "ProfileViewModel"
Cohesion: 0.33
Nodes (1): ProfileViewModelTest

### Community 119 - "DocumentsIntent"
Cohesion: 0.33
Nodes (5): DeleteDocument, DocumentsIntent, LoadDocuments, RefreshDocuments, UploadFile

### Community 120 - "VectorMigrations"
Cohesion: 0.33
Nodes (2): AddDocumentsTableAndEmbeddingColumnsMigration, EnablePgvectorAndCreateEmbeddingsTableMigration

### Community 121 - "InvalidField"
Cohesion: 0.33
Nodes (1): InvalidField

### Community 122 - "ExposedLegalDocumentRepository"
Cohesion: 0.33
Nodes (1): ExposedLegalDocumentRepository

### Community 123 - "LegalDocumentRepository"
Cohesion: 0.33
Nodes (2): LegalDocumentRecord, LegalDocumentRepository

### Community 124 - "EmailService"
Cohesion: 0.33
Nodes (1): EmailServiceTest

### Community 125 - "RagService"
Cohesion: 0.33
Nodes (2): RagContext, RagService

### Community 126 - "Test / FakeSdkBuilder"
Cohesion: 0.4
Nodes (6): FakeSdkBuilder Test Fake, ViewModel test{} DSL with Turbine, 80% Minimum Test Coverage (Kover), Kotest Assertions Only Convention, TDD Workflow (Tests First), core:testing Module

### Community 127 - "InvitationDtos"
Cohesion: 0.4
Nodes (4): AcceptInvitationRequest, AcceptInvitationResponse, CreateInvitationRequest, InvitationResponse

### Community 128 - "ValidationSupport"
Cohesion: 0.4
Nodes (0): 

### Community 129 - "ViewModelContext"
Cohesion: 0.4
Nodes (1): ViewModelTestContext

### Community 130 - "Statement"
Cohesion: 0.4
Nodes (4): EventStatement, IntentStatement, ModelStatement, Statement

### Community 131 - "FakeFileApiBuilder"
Cohesion: 0.4
Nodes (1): FakeFileApiBuilder

### Community 132 - "ImagePicker.ios"
Cohesion: 0.4
Nodes (1): ImagePickerDelegate

### Community 133 - "ImagePicker"
Cohesion: 0.4
Nodes (1): ImagePickerResult

### Community 134 - "TerminalInput"
Cohesion: 0.4
Nodes (0): 

### Community 135 - "TerminalButton"
Cohesion: 0.4
Nodes (1): ButtonVariant

### Community 136 - "TerminalReorderableList"
Cohesion: 0.4
Nodes (1): ReorderState

### Community 137 - "TerminalList"
Cohesion: 0.4
Nodes (1): ListItemState

### Community 138 - "TerminalRadarChart"
Cohesion: 0.4
Nodes (2): RadarDataPoint, RadarSeries

### Community 139 - "TerminalLineChart"
Cohesion: 0.4
Nodes (2): ChartDataPoint, ChartSeries

### Community 140 - "LegalDocumentViewModel"
Cohesion: 0.4
Nodes (1): LegalDocumentViewModel

### Community 141 - "ConsentGateScreen"
Cohesion: 0.4
Nodes (0): 

### Community 142 - "ConsentGateEvent"
Cohesion: 0.4
Nodes (4): ConsentCompleted, ConsentGateEvent, NavigateToDocument, ShowError

### Community 143 - "LegalDocumentMutation"
Cohesion: 0.4
Nodes (4): LegalDocumentMutation, SetDocument, SetError, SetLoading

### Community 144 - "LegalDocumentViewModel"
Cohesion: 0.4
Nodes (1): LegalDocumentViewModelTest

### Community 145 - "PrivacyRoutes"
Cohesion: 0.4
Nodes (4): AccountDeletionRoute, ConsentGateRoute, LegalDocumentRoute, PrivacySettingsRoute

### Community 146 - "LoginEvent"
Cohesion: 0.4
Nodes (4): LoginEvent, NavigateToConsentGate, NavigateToDashboard, NavigateToGroup

### Community 147 - "OAuthHandler.jvm"
Cohesion: 0.4
Nodes (1): OAuthHandler

### Community 148 - "OAuthHandler.android"
Cohesion: 0.4
Nodes (1): OAuthHandler

### Community 149 - "RegisterMemberViewModel"
Cohesion: 0.4
Nodes (1): RegisterMemberViewModel

### Community 150 - "ProfileSidebar"
Cohesion: 0.4
Nodes (0): 

### Community 151 - "PrivacyJobScheduler"
Cohesion: 0.4
Nodes (2): PrivacyJob, PrivacyJobScheduler

### Community 152 - "ExportContributor"
Cohesion: 0.4
Nodes (3): ExportContributor, ExportFile, ExportSection

### Community 153 - "DocumentIngestionService"
Cohesion: 0.4
Nodes (1): DocumentIngestionService

### Community 154 - "ExposedPersistenceStorage"
Cohesion: 0.4
Nodes (1): ExposedPersistenceStorage

### Community 155 - "DocumentDtos"
Cohesion: 0.5
Nodes (3): DocumentListResponse, DocumentResponse, DocumentUploadRequest

### Community 156 - "PreferencesStorage"
Cohesion: 0.5
Nodes (1): PreferencesStorage

### Community 157 - "ViewModel"
Cohesion: 0.5
Nodes (1): ViewModelTest

### Community 158 - "ErrorMapper"
Cohesion: 0.5
Nodes (0): 

### Community 159 - "FileApiImpl"
Cohesion: 0.5
Nodes (1): FileApiImpl

### Community 160 - "FileApi"
Cohesion: 0.5
Nodes (1): FileApi

### Community 161 - "PlatformConfig.wasmJs"
Cohesion: 0.5
Nodes (0): 

### Community 162 - "DateFormatting"
Cohesion: 0.5
Nodes (0): 

### Community 163 - "TerminalBadge"
Cohesion: 0.5
Nodes (1): BadgeVariant

### Community 164 - "TerminalAlert"
Cohesion: 0.5
Nodes (1): AlertVariant

### Community 165 - "TerminalBarChart"
Cohesion: 0.5
Nodes (1): BarData

### Community 166 - "TerminalDropdownMenu"
Cohesion: 0.5
Nodes (0): 

### Community 167 - "TerminalSwipeReveal"
Cohesion: 0.5
Nodes (0): 

### Community 168 - "LegalDocumentIntent"
Cohesion: 0.5
Nodes (3): LegalDocumentIntent, Load, SwitchLocale

### Community 169 - "LegalDocumentScreen"
Cohesion: 0.5
Nodes (0): 

### Community 170 - "OAuthHandler.ios"
Cohesion: 0.5
Nodes (1): OAuthHandler

### Community 171 - "OAuthHandler"
Cohesion: 0.5
Nodes (1): OAuthHandler

### Community 172 - "OAuthHandler.wasmJs"
Cohesion: 0.5
Nodes (1): OAuthHandler

### Community 173 - "AdminPanelEvent"
Cohesion: 0.5
Nodes (3): AdminPanelEvent, GroupCreated, NavigateToRegisterMember

### Community 174 - "DashboardBottomNav"
Cohesion: 0.5
Nodes (1): BottomTab

### Community 175 - "DashboardEvent"
Cohesion: 0.5
Nodes (3): DashboardEvent, NavigateToAdmin, NavigateToLogin

### Community 176 - "DashboardViewModel"
Cohesion: 0.5
Nodes (1): DashboardViewModel

### Community 177 - "ProfileEvent"
Cohesion: 0.5
Nodes (3): NavigateToLogin, NavigateToPrivacySettings, ProfileEvent

### Community 178 - "ProfileModel"
Cohesion: 0.5
Nodes (1): ProfileModel

### Community 179 - "Migration"
Cohesion: 0.5
Nodes (1): Migration

### Community 180 - "MigrationRegistry"
Cohesion: 0.5
Nodes (1): MigrationRegistry

### Community 181 - "UserService"
Cohesion: 0.5
Nodes (1): UserService

### Community 182 - "OAuthRoutes"
Cohesion: 0.5
Nodes (0): 

### Community 183 - "Ai"
Cohesion: 0.5
Nodes (1): CreateConversationsTableMigration

### Community 184 - "UserTools"
Cohesion: 0.5
Nodes (1): UserTools

### Community 185 - "RelevanceDetector"
Cohesion: 0.5
Nodes (2): RelevanceCheck, RelevanceDetector

### Community 186 - "HealthRoutes"
Cohesion: 0.5
Nodes (2): HealthStatus, ServiceHealth

### Community 187 - "App / OSApp"
Cohesion: 0.5
Nodes (1): iOSApp

### Community 188 - "MainActivity"
Cohesion: 0.5
Nodes (1): MainActivity

### Community 189 - "CLAUDE"
Cohesion: 0.5
Nodes (4): Self-Improvement Lessons Loop, Plan Mode Default, Subagent Strategy, Workflow Orchestration Rules

### Community 190 - "CLAUDE"
Cohesion: 0.5
Nodes (4): Arrow Raise Server Error Pattern, Either<AppError, T> Client Error Type, @Resource Type-Safe Ktor Routes, core:models Module

### Community 191 - "UserDtos"
Cohesion: 0.67
Nodes (2): UpdateProfileRequest, UserResponse

### Community 192 - "AuthInterceptor"
Cohesion: 0.67
Nodes (1): AuthInterceptor

### Community 193 - "ImageDecoder.ios"
Cohesion: 0.67
Nodes (0): 

### Community 194 - "TerminalText"
Cohesion: 0.67
Nodes (0): 

### Community 195 - "TerminalTextarea"
Cohesion: 0.67
Nodes (0): 

### Community 196 - "TerminalTooltip"
Cohesion: 0.67
Nodes (0): 

### Community 197 - "TerminalProgress"
Cohesion: 0.67
Nodes (0): 

### Community 198 - "TerminalDivider"
Cohesion: 0.67
Nodes (0): 

### Community 199 - "TerminalAvatar"
Cohesion: 0.67
Nodes (0): 

### Community 200 - "TerminalKbd"
Cohesion: 0.67
Nodes (0): 

### Community 201 - "TerminalSwitch"
Cohesion: 0.67
Nodes (0): 

### Community 202 - "TerminalCheckbox"
Cohesion: 0.67
Nodes (0): 

### Community 203 - "TerminalRadio"
Cohesion: 0.67
Nodes (0): 

### Community 204 - "TerminalShadows"
Cohesion: 0.67
Nodes (2): TerminalShadow, TerminalShadows

### Community 205 - "ConsentGateModel"
Cohesion: 0.67
Nodes (2): ConsentGateModel, ConsentItem

### Community 206 - "LegalDocumentEvent"
Cohesion: 0.67
Nodes (2): LegalDocumentEvent, ShowError

### Community 207 - "AccountDeletionModel"
Cohesion: 0.67
Nodes (2): AccountDeletionModel, DeletionStep

### Community 208 - "InviteAcceptScreen"
Cohesion: 0.67
Nodes (0): 

### Community 209 - "DeepLinkCheckers"
Cohesion: 0.67
Nodes (0): 

### Community 210 - "RegisterMemberEvent"
Cohesion: 0.67
Nodes (2): RegisterMemberEvent, RegistrationSuccess

### Community 211 - "AdminRoutes"
Cohesion: 0.67
Nodes (2): AdminPanelRoute, RegisterMemberRoute

### Community 212 - "DashboardSidebar"
Cohesion: 0.67
Nodes (0): 

### Community 213 - "FreeTierContent"
Cohesion: 0.67
Nodes (0): 

### Community 214 - "PaidTierContent"
Cohesion: 0.67
Nodes (0): 

### Community 215 - "AdminTierContent"
Cohesion: 0.67
Nodes (0): 

### Community 216 - "PowerAdminTierContent"
Cohesion: 0.67
Nodes (0): 

### Community 217 - "DocumentsEvent"
Cohesion: 0.67
Nodes (2): DocumentsEvent, UploadSuccess

### Community 218 - "DataSource"
Cohesion: 0.67
Nodes (1): DataSource

### Community 219 - "DomainError"
Cohesion: 0.67
Nodes (1): DomainError

### Community 220 - "ServerStrings"
Cohesion: 0.67
Nodes (1): ServerStrings

### Community 221 - "ExportProcessorJob"
Cohesion: 0.67
Nodes (1): ExportProcessorJob

### Community 222 - "ExportCleanupJob"
Cohesion: 0.67
Nodes (1): ExportCleanupJob

### Community 223 - "DeletionExecutorJob"
Cohesion: 0.67
Nodes (1): DeletionExecutorJob

### Community 224 - "PrivacyErrors"
Cohesion: 0.67
Nodes (1): ConsentRequired

### Community 225 - "SmtpEmailService"
Cohesion: 0.67
Nodes (1): SmtpEmailService

### Community 226 - "AuthErrors"
Cohesion: 0.67
Nodes (1): InvalidCredentials

### Community 227 - "EmailService"
Cohesion: 0.67
Nodes (1): EmailService

### Community 228 - "RoleAuthorization"
Cohesion: 0.67
Nodes (1): RoleConfig

### Community 229 - "AuthWireModule"
Cohesion: 0.67
Nodes (0): 

### Community 230 - "GroupRoutes"
Cohesion: 0.67
Nodes (0): 

### Community 231 - "InvitationRoutes"
Cohesion: 0.67
Nodes (0): 

### Community 232 - "GroupErrors"
Cohesion: 0.67
Nodes (1): GroupNotFound

### Community 233 - "InvitationErrors"
Cohesion: 0.67
Nodes (1): InvitationNotFound

### Community 234 - "StructuredOutputService"
Cohesion: 0.67
Nodes (1): StructuredOutputService

### Community 235 - "AiErrors"
Cohesion: 0.67
Nodes (1): AgentExecutionFailed

### Community 236 - "FileErrors"
Cohesion: 0.67
Nodes (1): FileTooLarge

### Community 237 - "AppLocale.ios"
Cohesion: 0.67
Nodes (0): 

### Community 238 - "AppLocale"
Cohesion: 0.67
Nodes (0): 

### Community 239 - "AppLocale.jvm"
Cohesion: 0.67
Nodes (0): 

### Community 240 - "AppLocale.android"
Cohesion: 0.67
Nodes (0): 

### Community 241 - "ARCHITECTURE"
Cohesion: 0.67
Nodes (3): Context Parameters for DI (Configuration, R2dbcDatabase), Rationale: Threads Dependencies Without Global State, -Xcontext-parameters Compiler Flag

### Community 242 - "Route"
Cohesion: 1.0
Nodes (1): Route

### Community 243 - "ErrorResponse"
Cohesion: 1.0
Nodes (1): ErrorResponse

### Community 244 - "FileDtos"
Cohesion: 1.0
Nodes (1): FileResponse

### Community 245 - "StringKey"
Cohesion: 1.0
Nodes (1): StringKey

### Community 246 - "MviViewModelDsl"
Cohesion: 1.0
Nodes (0): 

### Community 247 - "PlatformEngine.ios"
Cohesion: 1.0
Nodes (0): 

### Community 248 - "PlatformConfig.ios"
Cohesion: 1.0
Nodes (0): 

### Community 249 - "PlatformEngine"
Cohesion: 1.0
Nodes (0): 

### Community 250 - "Sdk"
Cohesion: 1.0
Nodes (1): Sdk

### Community 251 - "PlatformConfig"
Cohesion: 1.0
Nodes (0): 

### Community 252 - "ApiClient"
Cohesion: 1.0
Nodes (0): 

### Community 253 - "PlatformEngine.jvm"
Cohesion: 1.0
Nodes (0): 

### Community 254 - "PlatformConfig.jvm"
Cohesion: 1.0
Nodes (0): 

### Community 255 - "PlatformEngine.wasmJs"
Cohesion: 1.0
Nodes (0): 

### Community 256 - "PlatformConfig.android"
Cohesion: 1.0
Nodes (0): 

### Community 257 - "PlatformEngine.android"
Cohesion: 1.0
Nodes (0): 

### Community 258 - "ImageDecoder"
Cohesion: 1.0
Nodes (0): 

### Community 259 - "TerminalTheme"
Cohesion: 1.0
Nodes (0): 

### Community 260 - "TerminalBorders"
Cohesion: 1.0
Nodes (1): TerminalBorders

### Community 261 - "TerminalGap"
Cohesion: 1.0
Nodes (1): TerminalGap

### Community 262 - "TerminalTypography"
Cohesion: 1.0
Nodes (1): TerminalTypography

### Community 263 - "TerminalSpacing"
Cohesion: 1.0
Nodes (1): TerminalSpacing

### Community 264 - "TerminalColors"
Cohesion: 1.0
Nodes (1): TerminalColors

### Community 265 - "TerminalOpacity"
Cohesion: 1.0
Nodes (1): TerminalOpacity

### Community 266 - "TerminalRadius"
Cohesion: 1.0
Nodes (1): TerminalRadius

### Community 267 - "ImageDecoder.jvm"
Cohesion: 1.0
Nodes (0): 

### Community 268 - "ImagePicker.jvm"
Cohesion: 1.0
Nodes (0): 

### Community 269 - "ImageDecoder.wasmJs"
Cohesion: 1.0
Nodes (0): 

### Community 270 - "ImagePicker.wasmJs"
Cohesion: 1.0
Nodes (0): 

### Community 271 - "ImageDecoder.android"
Cohesion: 1.0
Nodes (0): 

### Community 272 - "ImagePicker.android"
Cohesion: 1.0
Nodes (0): 

### Community 273 - "LegalDocumentModel"
Cohesion: 1.0
Nodes (1): LegalDocumentModel

### Community 274 - "PrivacySettingsModel"
Cohesion: 1.0
Nodes (1): PrivacySettingsModel

### Community 275 - "PrivacyNavigation"
Cohesion: 1.0
Nodes (0): 

### Community 276 - "InviteLinkChecker.ios"
Cohesion: 1.0
Nodes (0): 

### Community 277 - "OAuthCallbackChecker.ios"
Cohesion: 1.0
Nodes (0): 

### Community 278 - "Platform.ios"
Cohesion: 1.0
Nodes (0): 

### Community 279 - "StringKeyResolver"
Cohesion: 1.0
Nodes (0): 

### Community 280 - "Platform"
Cohesion: 1.0
Nodes (0): 

### Community 281 - "InviteLinkChecker"
Cohesion: 1.0
Nodes (0): 

### Community 282 - "InviteAcceptModel"
Cohesion: 1.0
Nodes (1): InviteAcceptModel

### Community 283 - "OAuthCallbackChecker"
Cohesion: 1.0
Nodes (0): 

### Community 284 - "OAuthCallbackHandler"
Cohesion: 1.0
Nodes (0): 

### Community 285 - "LoginModel"
Cohesion: 1.0
Nodes (1): LoginModel

### Community 286 - "RegisterModel"
Cohesion: 1.0
Nodes (1): RegisterModel

### Community 287 - "Platform.jvm"
Cohesion: 1.0
Nodes (0): 

### Community 288 - "OAuthCallbackChecker.jvm"
Cohesion: 1.0
Nodes (0): 

### Community 289 - "InviteLinkChecker.jvm"
Cohesion: 1.0
Nodes (0): 

### Community 290 - "OAuthCallbackChecker.wasmJs"
Cohesion: 1.0
Nodes (0): 

### Community 291 - "InviteLinkChecker.wasmJs"
Cohesion: 1.0
Nodes (0): 

### Community 292 - "Platform.wasmJs"
Cohesion: 1.0
Nodes (0): 

### Community 293 - "InviteLinkChecker.android"
Cohesion: 1.0
Nodes (0): 

### Community 294 - "Platform.android"
Cohesion: 1.0
Nodes (0): 

### Community 295 - "OAuthCallbackChecker.android"
Cohesion: 1.0
Nodes (0): 

### Community 296 - "AuthNavigation"
Cohesion: 1.0
Nodes (0): 

### Community 297 - "AdminPanelModel"
Cohesion: 1.0
Nodes (1): AdminPanelModel

### Community 298 - "RegisterMemberModel"
Cohesion: 1.0
Nodes (1): RegisterMemberModel

### Community 299 - "RegisterMemberScreen"
Cohesion: 1.0
Nodes (0): 

### Community 300 - "AdminNavigation"
Cohesion: 1.0
Nodes (0): 

### Community 301 - "DashboardModel"
Cohesion: 1.0
Nodes (1): DashboardModel

### Community 302 - "DashboardRoute"
Cohesion: 1.0
Nodes (1): DashboardRoute

### Community 303 - "DashboardNavigation"
Cohesion: 1.0
Nodes (0): 

### Community 304 - "PremiumTierContent"
Cohesion: 1.0
Nodes (0): 

### Community 305 - "ProfileRoute"
Cohesion: 1.0
Nodes (1): ProfileRoute

### Community 306 - "ProfileNavigation"
Cohesion: 1.0
Nodes (0): 

### Community 307 - "DocumentsModel"
Cohesion: 1.0
Nodes (1): DocumentsModel

### Community 308 - "DocumentsScreen"
Cohesion: 1.0
Nodes (0): 

### Community 309 - "DocumentsRoute"
Cohesion: 1.0
Nodes (1): DocumentsRoute

### Community 310 - "DocumentsNavigation"
Cohesion: 1.0
Nodes (0): 

### Community 311 - "DocumentEmbeddingsTable"
Cohesion: 1.0
Nodes (1): DocumentEmbeddingsTable

### Community 312 - "DocumentsTable"
Cohesion: 1.0
Nodes (1): DocumentsTable

### Community 313 - "MigrationsTable"
Cohesion: 1.0
Nodes (1): MigrationsTable

### Community 314 - "Configuration"
Cohesion: 1.0
Nodes (1): Configuration

### Community 315 - "Error"
Cohesion: 1.0
Nodes (0): 

### Community 316 - "SecurityPlugin"
Cohesion: 1.0
Nodes (0): 

### Community 317 - "LegalDocumentServiceImpl"
Cohesion: 1.0
Nodes (1): LegalDocumentServiceImpl

### Community 318 - "ConsentServiceImpl"
Cohesion: 1.0
Nodes (1): ConsentServiceImpl

### Community 319 - "DeletionRoutes"
Cohesion: 1.0
Nodes (0): 

### Community 320 - "LegalRoutes"
Cohesion: 1.0
Nodes (0): 

### Community 321 - "ExportRoutes"
Cohesion: 1.0
Nodes (0): 

### Community 322 - "ConsentRoutes"
Cohesion: 1.0
Nodes (0): 

### Community 323 - "LegalDocumentsTable"
Cohesion: 1.0
Nodes (1): LegalDocumentsTable

### Community 324 - "AccountDeletionRequestsTable"
Cohesion: 1.0
Nodes (1): AccountDeletionRequestsTable

### Community 325 - "DataExportRequestsTable"
Cohesion: 1.0
Nodes (1): DataExportRequestsTable

### Community 326 - "ConsentRecordsTable"
Cohesion: 1.0
Nodes (1): ConsentRecordsTable

### Community 327 - "PrivacyWireModule"
Cohesion: 1.0
Nodes (0): 

### Community 328 - "OAuthService"
Cohesion: 1.0
Nodes (1): GoogleUserInfo

### Community 329 - "AuthServiceImpl"
Cohesion: 1.0
Nodes (1): AuthServiceImpl

### Community 330 - "UserRoutes"
Cohesion: 1.0
Nodes (0): 

### Community 331 - "RolesTable"
Cohesion: 1.0
Nodes (1): RolesTable

### Community 332 - "UsersTable"
Cohesion: 1.0
Nodes (1): UsersTable

### Community 333 - "InvitationServiceImpl"
Cohesion: 1.0
Nodes (1): InvitationServiceImpl

### Community 334 - "InvitationsTable"
Cohesion: 1.0
Nodes (1): InvitationsTable

### Community 335 - "UserGroupMembershipsTable"
Cohesion: 1.0
Nodes (1): UserGroupMembershipsTable

### Community 336 - "GroupsTable"
Cohesion: 1.0
Nodes (1): GroupsTable

### Community 337 - "InvitationService"
Cohesion: 1.0
Nodes (1): InvitationService

### Community 338 - "GroupWireModule"
Cohesion: 1.0
Nodes (0): 

### Community 339 - "ChatAgent"
Cohesion: 1.0
Nodes (0): 

### Community 340 - "ChatStreamingStrategy"
Cohesion: 1.0
Nodes (0): 

### Community 341 - "ConversationsTable"
Cohesion: 1.0
Nodes (1): ConversationsTable

### Community 342 - "DocumentRoutes"
Cohesion: 1.0
Nodes (0): 

### Community 343 - "AiWireModule"
Cohesion: 1.0
Nodes (0): 

### Community 344 - "FileRoutes"
Cohesion: 1.0
Nodes (0): 

### Community 345 - "Application"
Cohesion: 1.0
Nodes (0): 

### Community 346 - "Server"
Cohesion: 1.0
Nodes (0): 

### Community 347 - "AvatarRoutes"
Cohesion: 1.0
Nodes (0): 

### Community 348 - "MainViewController"
Cohesion: 1.0
Nodes (0): 

### Community 349 - "AppNavHost"
Cohesion: 1.0
Nodes (0): 

### Community 350 - "LocaleSelector"
Cohesion: 1.0
Nodes (0): 

### Community 351 - "main"
Cohesion: 1.0
Nodes (0): 

### Community 352 - "settings.gradle"
Cohesion: 1.0
Nodes (0): 

### Community 353 - "StorageModule"
Cohesion: 1.0
Nodes (0): 

### Community 354 - "Annotations"
Cohesion: 1.0
Nodes (0): 

### Community 355 - "SdkModule"
Cohesion: 1.0
Nodes (0): 

### Community 356 - "TerminalPreview"
Cohesion: 1.0
Nodes (0): 

### Community 357 - "PrivacyModule"
Cohesion: 1.0
Nodes (0): 

### Community 358 - "AuthScreen"
Cohesion: 1.0
Nodes (0): 

### Community 359 - "AuthModule"
Cohesion: 1.0
Nodes (0): 

### Community 360 - "AdminModule"
Cohesion: 1.0
Nodes (0): 

### Community 361 - "DashboardModule"
Cohesion: 1.0
Nodes (0): 

### Community 362 - "ProfileModule"
Cohesion: 1.0
Nodes (0): 

### Community 363 - "DocumentsModule"
Cohesion: 1.0
Nodes (0): 

### Community 364 - "Startup"
Cohesion: 1.0
Nodes (0): 

### Community 365 - "Module"
Cohesion: 1.0
Nodes (0): 

### Community 366 - "DataExportServiceImpl"
Cohesion: 1.0
Nodes (0): 

### Community 367 - "DataExportService"
Cohesion: 1.0
Nodes (0): 

### Community 368 - "ConsentService"
Cohesion: 1.0
Nodes (0): 

### Community 369 - "LegalDocumentService"
Cohesion: 1.0
Nodes (0): 

### Community 370 - "EmailModule"
Cohesion: 1.0
Nodes (0): 

### Community 371 - "AuthService"
Cohesion: 1.0
Nodes (0): 

### Community 372 - "GroupModule"
Cohesion: 1.0
Nodes (0): 

### Community 373 - "Models"
Cohesion: 1.0
Nodes (0): 

### Community 374 - "AiModule"
Cohesion: 1.0
Nodes (0): 

### Community 375 - "AssistantAgent"
Cohesion: 1.0
Nodes (0): 

### Community 376 - "AiRoutes"
Cohesion: 1.0
Nodes (0): 

### Community 377 - "FileModule"
Cohesion: 1.0
Nodes (0): 

### Community 378 - "S3FileService"
Cohesion: 1.0
Nodes (0): 

### Community 379 - "FileService"
Cohesion: 1.0
Nodes (0): 

### Community 380 - "FileWireModule"
Cohesion: 1.0
Nodes (0): 

### Community 381 - "ServerModule"
Cohesion: 1.0
Nodes (0): 

### Community 382 - "Config"
Cohesion: 1.0
Nodes (0): 

### Community 383 - "SharedModule"
Cohesion: 1.0
Nodes (0): 

### Community 384 - "spa-routing"
Cohesion: 1.0
Nodes (0): 

### Community 385 - "AppModule"
Cohesion: 1.0
Nodes (0): 

### Community 386 - "LocalAppLocale"
Cohesion: 1.0
Nodes (0): 

### Community 387 - "AndroidModule"
Cohesion: 1.0
Nodes (0): 

### Community 388 - "kover-convention.gradle"
Cohesion: 1.0
Nodes (0): 

### Community 389 - "server-module-convention.gradle"
Cohesion: 1.0
Nodes (0): 

### Community 390 - "kmp-library-convention.gradle"
Cohesion: 1.0
Nodes (0): 

### Community 391 - "CONTRIBUTING"
Cohesion: 1.0
Nodes (1): Conventional Commit Types

### Community 392 - "CLAUDE"
Cohesion: 1.0
Nodes (1): Compose Callbacks Pattern (no direct ViewModel)

## Knowledge Gaps
- **587 isolated node(s):** `Check`, `Route`, `GroupRole`, `Owner`, `Admin` (+582 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **Thin community `Route`** (2 nodes): `Route.kt`, `Route`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `ErrorResponse`** (2 nodes): `ErrorResponse.kt`, `ErrorResponse`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `FileDtos`** (2 nodes): `FileDtos.kt`, `FileResponse`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `StringKey`** (2 nodes): `StringKey.kt`, `StringKey`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `MviViewModelDsl`** (2 nodes): `MviViewModelTestDsl.kt`, `test()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `PlatformEngine.ios`** (2 nodes): `PlatformEngine.ios.kt`, `platformEngine()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `PlatformConfig.ios`** (2 nodes): `PlatformConfig.ios.kt`, `defaultBaseUrl()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `PlatformEngine`** (2 nodes): `PlatformEngine.kt`, `platformEngine()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Sdk`** (2 nodes): `Sdk.kt`, `Sdk`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `PlatformConfig`** (2 nodes): `PlatformConfig.kt`, `defaultBaseUrl()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `ApiClient`** (2 nodes): `ApiClient.kt`, `createApiClient()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `PlatformEngine.jvm`** (2 nodes): `PlatformEngine.jvm.kt`, `platformEngine()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `PlatformConfig.jvm`** (2 nodes): `PlatformConfig.jvm.kt`, `defaultBaseUrl()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `PlatformEngine.wasmJs`** (2 nodes): `PlatformEngine.wasmJs.kt`, `platformEngine()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `PlatformConfig.android`** (2 nodes): `PlatformConfig.android.kt`, `defaultBaseUrl()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `PlatformEngine.android`** (2 nodes): `PlatformEngine.android.kt`, `platformEngine()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `ImageDecoder`** (2 nodes): `ImageDecoder.kt`, `rememberDecodedImage()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `TerminalTheme`** (2 nodes): `TerminalTheme.kt`, `TerminalTheme()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `TerminalBorders`** (2 nodes): `TerminalBorders.kt`, `TerminalBorders`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `TerminalGap`** (2 nodes): `TerminalGap.kt`, `TerminalGap`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `TerminalTypography`** (2 nodes): `TerminalTypography.kt`, `TerminalTypography`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `TerminalSpacing`** (2 nodes): `TerminalSpacing.kt`, `TerminalSpacing`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `TerminalColors`** (2 nodes): `TerminalColors.kt`, `TerminalColors`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `TerminalOpacity`** (2 nodes): `TerminalOpacity.kt`, `TerminalOpacity`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `TerminalRadius`** (2 nodes): `TerminalRadius.kt`, `TerminalRadius`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `ImageDecoder.jvm`** (2 nodes): `ImageDecoder.jvm.kt`, `rememberDecodedImage()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `ImagePicker.jvm`** (2 nodes): `ImagePicker.jvm.kt`, `rememberImagePickerLauncher()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `ImageDecoder.wasmJs`** (2 nodes): `ImageDecoder.wasmJs.kt`, `rememberDecodedImage()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `ImagePicker.wasmJs`** (2 nodes): `ImagePicker.wasmJs.kt`, `rememberImagePickerLauncher()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `ImageDecoder.android`** (2 nodes): `ImageDecoder.android.kt`, `rememberDecodedImage()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `ImagePicker.android`** (2 nodes): `ImagePicker.android.kt`, `rememberImagePickerLauncher()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `LegalDocumentModel`** (2 nodes): `LegalDocumentModel.kt`, `LegalDocumentModel`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `PrivacySettingsModel`** (2 nodes): `PrivacySettingsModel.kt`, `PrivacySettingsModel`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `PrivacyNavigation`** (2 nodes): `PrivacyNavigation.kt`, `privacyEntries()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `InviteLinkChecker.ios`** (2 nodes): `InviteLinkChecker.ios.kt`, `checkInviteLink()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `OAuthCallbackChecker.ios`** (2 nodes): `OAuthCallbackChecker.ios.kt`, `checkOAuthCallback()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Platform.ios`** (2 nodes): `Platform.ios.kt`, `showAppleSignIn()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `StringKeyResolver`** (2 nodes): `StringKeyResolver.kt`, `resolveStringKey()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Platform`** (2 nodes): `Platform.kt`, `showAppleSignIn()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `InviteLinkChecker`** (2 nodes): `InviteLinkChecker.kt`, `checkInviteLink()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `InviteAcceptModel`** (2 nodes): `InviteAcceptModel.kt`, `InviteAcceptModel`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `OAuthCallbackChecker`** (2 nodes): `OAuthCallbackChecker.kt`, `checkOAuthCallback()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `OAuthCallbackHandler`** (2 nodes): `OAuthCallbackHandler.kt`, `OAuthCallbackHandler()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `LoginModel`** (2 nodes): `LoginModel.kt`, `LoginModel`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `RegisterModel`** (2 nodes): `RegisterModel.kt`, `RegisterModel`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Platform.jvm`** (2 nodes): `Platform.jvm.kt`, `showAppleSignIn()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `OAuthCallbackChecker.jvm`** (2 nodes): `OAuthCallbackChecker.jvm.kt`, `checkOAuthCallback()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `InviteLinkChecker.jvm`** (2 nodes): `InviteLinkChecker.jvm.kt`, `checkInviteLink()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `OAuthCallbackChecker.wasmJs`** (2 nodes): `OAuthCallbackChecker.wasmJs.kt`, `checkOAuthCallback()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `InviteLinkChecker.wasmJs`** (2 nodes): `InviteLinkChecker.wasmJs.kt`, `checkInviteLink()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Platform.wasmJs`** (2 nodes): `Platform.wasmJs.kt`, `showAppleSignIn()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `InviteLinkChecker.android`** (2 nodes): `InviteLinkChecker.android.kt`, `checkInviteLink()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Platform.android`** (2 nodes): `Platform.android.kt`, `showAppleSignIn()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `OAuthCallbackChecker.android`** (2 nodes): `OAuthCallbackChecker.android.kt`, `checkOAuthCallback()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `AuthNavigation`** (2 nodes): `AuthNavigation.kt`, `authEntries()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `AdminPanelModel`** (2 nodes): `AdminPanelModel.kt`, `AdminPanelModel`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `RegisterMemberModel`** (2 nodes): `RegisterMemberModel.kt`, `RegisterMemberModel`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `RegisterMemberScreen`** (2 nodes): `RegisterMemberScreen.kt`, `RegisterMemberScreen()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `AdminNavigation`** (2 nodes): `AdminNavigation.kt`, `adminEntries()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `DashboardModel`** (2 nodes): `DashboardModel.kt`, `DashboardModel`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `DashboardRoute`** (2 nodes): `DashboardRoute.kt`, `DashboardRoute`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `DashboardNavigation`** (2 nodes): `DashboardNavigation.kt`, `dashboardEntries()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `PremiumTierContent`** (2 nodes): `PremiumTierContent.kt`, `PremiumTierContent()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `ProfileRoute`** (2 nodes): `ProfileRoute.kt`, `ProfileRoute`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `ProfileNavigation`** (2 nodes): `ProfileNavigation.kt`, `profileEntries()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `DocumentsModel`** (2 nodes): `DocumentsModel.kt`, `DocumentsModel`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `DocumentsScreen`** (2 nodes): `DocumentsScreen.kt`, `DocumentsScreen()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `DocumentsRoute`** (2 nodes): `DocumentsRoute.kt`, `DocumentsRoute`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `DocumentsNavigation`** (2 nodes): `DocumentsNavigation.kt`, `documentsEntries()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `DocumentEmbeddingsTable`** (2 nodes): `DocumentEmbeddingsTable.kt`, `DocumentEmbeddingsTable`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `DocumentsTable`** (2 nodes): `DocumentsTable.kt`, `DocumentsTable`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `MigrationsTable`** (2 nodes): `MigrationsTable.kt`, `MigrationsTable`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Configuration`** (2 nodes): `Configuration.kt`, `Configuration`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Error`** (2 nodes): `Error.kt`, `preferredLanguage()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `SecurityPlugin`** (2 nodes): `SecurityPlugin.kt`, `configureSecurity()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `LegalDocumentServiceImpl`** (2 nodes): `LegalDocumentServiceImpl.kt`, `LegalDocumentServiceImpl`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `ConsentServiceImpl`** (2 nodes): `ConsentServiceImpl.kt`, `ConsentServiceImpl`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `DeletionRoutes`** (2 nodes): `DeletionRoutes.kt`, `deletionRoutes()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `LegalRoutes`** (2 nodes): `LegalRoutes.kt`, `legalRoutes()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `ExportRoutes`** (2 nodes): `ExportRoutes.kt`, `exportRoutes()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `ConsentRoutes`** (2 nodes): `ConsentRoutes.kt`, `consentRoutes()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `LegalDocumentsTable`** (2 nodes): `LegalDocumentsTable.kt`, `LegalDocumentsTable`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `AccountDeletionRequestsTable`** (2 nodes): `AccountDeletionRequestsTable.kt`, `AccountDeletionRequestsTable`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `DataExportRequestsTable`** (2 nodes): `DataExportRequestsTable.kt`, `DataExportRequestsTable`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `ConsentRecordsTable`** (2 nodes): `ConsentRecordsTable.kt`, `ConsentRecordsTable`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `PrivacyWireModule`** (2 nodes): `PrivacyWireModule.kt`, `registerPrivacyWireMigrations()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `OAuthService`** (2 nodes): `OAuthService.kt`, `GoogleUserInfo`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `AuthServiceImpl`** (2 nodes): `AuthServiceImpl.kt`, `AuthServiceImpl`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `UserRoutes`** (2 nodes): `UserRoutes.kt`, `userRoutes()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `RolesTable`** (2 nodes): `RolesTable.kt`, `RolesTable`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `UsersTable`** (2 nodes): `UsersTable.kt`, `UsersTable`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `InvitationServiceImpl`** (2 nodes): `InvitationServiceImpl.kt`, `InvitationServiceImpl`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `InvitationsTable`** (2 nodes): `InvitationsTable.kt`, `InvitationsTable`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `UserGroupMembershipsTable`** (2 nodes): `UserGroupMembershipsTable.kt`, `UserGroupMembershipsTable`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `GroupsTable`** (2 nodes): `GroupsTable.kt`, `GroupsTable`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `InvitationService`** (2 nodes): `InvitationService.kt`, `InvitationService`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `GroupWireModule`** (2 nodes): `GroupWireModule.kt`, `registerGroupWireMigrations()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `ChatAgent`** (2 nodes): `ChatAgent.kt`, `streamChat()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `ChatStreamingStrategy`** (2 nodes): `ChatStreamingStrategy.kt`, `chatStreamingStrategy()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `ConversationsTable`** (2 nodes): `ConversationsTable.kt`, `ConversationsTable`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `DocumentRoutes`** (2 nodes): `DocumentRoutes.kt`, `documentRoutes()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `AiWireModule`** (2 nodes): `AiWireModule.kt`, `registerAiWireMigrations()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `FileRoutes`** (2 nodes): `FileRoutes.kt`, `fileRoutes()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Application`** (2 nodes): `Application.kt`, `main()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Server`** (2 nodes): `Server.kt`, `startServer()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `AvatarRoutes`** (2 nodes): `AvatarRoutes.kt`, `avatarRoutes()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `MainViewController`** (2 nodes): `MainViewController.kt`, `MainViewController()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `AppNavHost`** (2 nodes): `AppNavHost.kt`, `AppNavHost()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `LocaleSelector`** (2 nodes): `LocaleSelector.kt`, `LocaleSelector()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `main`** (2 nodes): `main.kt`, `main()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `settings.gradle`** (1 nodes): `settings.gradle.kts`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `StorageModule`** (1 nodes): `StorageModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Annotations`** (1 nodes): `Annotations.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `SdkModule`** (1 nodes): `SdkModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `TerminalPreview`** (1 nodes): `TerminalPreview.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `PrivacyModule`** (1 nodes): `PrivacyModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `AuthScreen`** (1 nodes): `AuthScreen.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `AuthModule`** (1 nodes): `AuthModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `AdminModule`** (1 nodes): `AdminModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `DashboardModule`** (1 nodes): `DashboardModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `ProfileModule`** (1 nodes): `ProfileModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `DocumentsModule`** (1 nodes): `DocumentsModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Startup`** (1 nodes): `Startup.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Module`** (1 nodes): `Module.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `DataExportServiceImpl`** (1 nodes): `DataExportServiceImpl.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `DataExportService`** (1 nodes): `DataExportService.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `ConsentService`** (1 nodes): `ConsentService.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `LegalDocumentService`** (1 nodes): `LegalDocumentService.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `EmailModule`** (1 nodes): `EmailModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `AuthService`** (1 nodes): `AuthService.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `GroupModule`** (1 nodes): `GroupModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Models`** (1 nodes): `Models.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `AiModule`** (1 nodes): `AiModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `AssistantAgent`** (1 nodes): `AssistantAgent.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `AiRoutes`** (1 nodes): `AiRoutes.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `FileModule`** (1 nodes): `FileModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `S3FileService`** (1 nodes): `S3FileService.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `FileService`** (1 nodes): `FileService.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `FileWireModule`** (1 nodes): `FileWireModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `ServerModule`** (1 nodes): `ServerModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Config`** (1 nodes): `Config.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `SharedModule`** (1 nodes): `SharedModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `spa-routing`** (1 nodes): `spa-routing.js`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `AppModule`** (1 nodes): `AppModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `LocalAppLocale`** (1 nodes): `LocalAppLocale.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `AndroidModule`** (1 nodes): `AndroidModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `kover-convention.gradle`** (1 nodes): `kover-convention.gradle.kts`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `server-module-convention.gradle`** (1 nodes): `server-module-convention.gradle.kts`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `kmp-library-convention.gradle`** (1 nodes): `kmp-library-convention.gradle.kts`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `CONTRIBUTING`** (1 nodes): `Conventional Commit Types`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `CLAUDE`** (1 nodes): `Compose Callbacks Pattern (no direct ViewModel)`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What connects `Check`, `Route`, `GroupRole` to the rest of the system?**
  _587 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Architecture & Conventions` be split into smaller, more focused modules?**
  _Cohesion score 0.05 - nodes in this community are weakly interconnected._
- **Should `API Routes Catalog` be split into smaller, more focused modules?**
  _Cohesion score 0.04 - nodes in this community are weakly interconnected._
- **Should `AppError Hierarchy` be split into smaller, more focused modules?**
  _Cohesion score 0.04 - nodes in this community are weakly interconnected._
- **Should `AdminPanelMutation` be split into smaller, more focused modules?**
  _Cohesion score 0.07 - nodes in this community are weakly interconnected._
- **Should `AdminPanelIntent` be split into smaller, more focused modules?**
  _Cohesion score 0.1 - nodes in this community are weakly interconnected._
- **Should `Server Test Helpers` be split into smaller, more focused modules?**
  _Cohesion score 0.1 - nodes in this community are weakly interconnected._