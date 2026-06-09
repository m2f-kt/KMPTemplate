# Graph Report - .  (2026-06-08)

## Corpus Check
- 549 files · ~237,646 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2485 nodes · 2011 edges · 474 communities detected
- Extraction: 100% EXTRACTED · 0% INFERRED · 0% AMBIGUOUS
- Token cost: 0 input · 0 output

## God Nodes (most connected - your core abstractions)
1. `InMemorySettings` - 22 edges
2. `GroupRoutesTest` - 17 edges
3. `FakePrivacyApiBuilder` - 15 edges
4. `PrivacyApi` - 14 edges
5. `PrivacyApiImpl` - 14 edges
6. `AccountDeletionViewModelTest` - 12 edges
7. `PermissionAndNativeErrorTest` - 11 edges
8. `PermissionMappingTest` - 11 edges
9. `FakeGroupApiBuilder` - 11 edges
10. `AccountDeletionViewModel` - 11 edges

## Surprising Connections (you probably didn't know these)
- None detected - all connections are within the same source files.

## Communities

### Community 0 - "Community 0"
Cohesion: 0.04
Nodes (55): AccessDenied, AgentFailed, AgentNotFound, AI, AlreadyAccepted, AlreadyExists, AppError, Auth (+47 more)

### Community 1 - "Community 1"
Cohesion: 0.04
Nodes (51): Accept, ActiveExport, AddMember, Ai, Assistant, Auth, Avatar, ById (+43 more)

### Community 2 - "Community 2"
Cohesion: 0.07
Nodes (29): AdminPanelMutation, AppendMembers, HideCreateGroupDialog, HideInviteDialog, HideRemoveMemberDialog, HideRevokeDialog, RemoveMemberFromList, SetCreateGroupError (+21 more)

### Community 3 - "Community 3"
Cohesion: 0.09
Nodes (1): InMemorySettings

### Community 4 - "Community 4"
Cohesion: 0.1
Nodes (20): AdminPanelIntent, CancelRemoveMember, CancelRevoke, CloseCreateGroupDialog, CloseInviteDialog, ConfirmRemoveMember, ConfirmRevokeInvitation, CreateGroupNameChanged (+12 more)

### Community 5 - "Community 5"
Cohesion: 0.1
Nodes (4): FakeEmbeddingProvider, NoOpEmailService, TestDatabase, TestMinIO

### Community 6 - "Community 6"
Cohesion: 0.1
Nodes (1): GroupRoutesTest

### Community 7 - "Community 7"
Cohesion: 0.11
Nodes (4): AppKitLib, ApplicationServicesLib, CoreFoundationLib, ObjcBridge

### Community 8 - "Community 8"
Cohesion: 0.11
Nodes (3): ConsentServiceTest, FakeConsentRepository, FakeLegalDocumentRepository

### Community 9 - "Community 9"
Cohesion: 0.11
Nodes (15): CancelEdit, HideCropDialog, ProfileMutation, SetAvatarUrl, SetEditEmail, SetEditName, SetFieldErrors, SetLoading (+7 more)

### Community 10 - "Community 10"
Cohesion: 0.12
Nodes (11): CounterEvent, CounterIntent, CounterModel, CounterMutation, CounterViewModel, Decrement, Done, Increment (+3 more)

### Community 11 - "Community 11"
Cohesion: 0.12
Nodes (1): FakePrivacyApiBuilder

### Community 12 - "Community 12"
Cohesion: 0.13
Nodes (1): PrivacyApi

### Community 13 - "Community 13"
Cohesion: 0.13
Nodes (1): PrivacyApiImpl

### Community 14 - "Community 14"
Cohesion: 0.13
Nodes (0): 

### Community 15 - "Community 15"
Cohesion: 0.13
Nodes (7): FaithfulnessJudge, InjectionResistanceJudge, Judge, JudgeModel, JudgeResult, JudgeSample, JudgeSuite

### Community 16 - "Community 16"
Cohesion: 0.14
Nodes (13): ConsentStatus, ConsentType, DataExportResponse, DeletionRequest, DeletionResponse, DeletionStatus, ExportStatus, GrantConsentRequest (+5 more)

### Community 17 - "Community 17"
Cohesion: 0.14
Nodes (11): CancelEditing, CropCancelled, CropConfirmed, EditEmailChanged, EditNameChanged, ImageSelected, LoadProfile, LogoutClicked (+3 more)

### Community 18 - "Community 18"
Cohesion: 0.14
Nodes (6): AddRevokedAtColumnMigration, CreateGroupsTableMigration, CreateInvitationsTableMigration, CreateMembershipsTableMigration, SeedDefaultGroupMigration, SeedDevTestGroupsMigration

### Community 19 - "Community 19"
Cohesion: 0.15
Nodes (10): DaysAgo, Fallback, HoursAgo, JustNow, MinutesAgo, MonthsAgo, RelativeBucket, WeeksAgo (+2 more)

### Community 20 - "Community 20"
Cohesion: 0.15
Nodes (0): 

### Community 21 - "Community 21"
Cohesion: 0.15
Nodes (1): AccountDeletionViewModelTest

### Community 22 - "Community 22"
Cohesion: 0.15
Nodes (12): RegisterMutation, SetConfirmPassword, SetEmail, SetFieldErrors, SetFirstName, SetInvitationEmail, SetInvitationToken, SetLastName (+4 more)

### Community 23 - "Community 23"
Cohesion: 0.15
Nodes (2): InvitationRecord, InvitationRepository

### Community 24 - "Community 24"
Cohesion: 0.15
Nodes (1): GroupServiceImpl

### Community 25 - "Community 25"
Cohesion: 0.15
Nodes (2): PgVectorStorage, SimilarChunk

### Community 26 - "Community 26"
Cohesion: 0.17
Nodes (1): PermissionAndNativeErrorTest

### Community 27 - "Community 27"
Cohesion: 0.17
Nodes (1): PermissionMappingTest

### Community 28 - "Community 28"
Cohesion: 0.17
Nodes (1): FakeGroupApiBuilder

### Community 29 - "Community 29"
Cohesion: 0.17
Nodes (1): AccountDeletionViewModel

### Community 30 - "Community 30"
Cohesion: 0.17
Nodes (11): LoginMutation, ResetState, SetAcceptingInvitation, SetEmail, SetInvitationEmail, SetInvitationToken, SetLoading, SetPassword (+3 more)

### Community 31 - "Community 31"
Cohesion: 0.17
Nodes (1): AdminPanelViewModel

### Community 32 - "Community 32"
Cohesion: 0.17
Nodes (1): LangfuseSpanAdapterTest

### Community 33 - "Community 33"
Cohesion: 0.17
Nodes (5): CreateAccountDeletionRequestsTableMigration, CreateConsentRecordsTableMigration, CreateDataExportRequestsTableMigration, CreateLegalDocumentsTableMigration, SeedMarketingAnalyticsDocumentsMigration

### Community 34 - "Community 34"
Cohesion: 0.17
Nodes (5): CreatePasswordResetTokensTableMigration, CreateRefreshTokensTableMigration, CreateRolesTableAndMigrateUsersMigration, CreateUsersTableMigration, DropProcessingRestrictedFromUsersMigration

### Community 35 - "Community 35"
Cohesion: 0.18
Nodes (2): MacSecurityCliKeychainBackend, ProcessOutcome

### Community 36 - "Community 36"
Cohesion: 0.18
Nodes (1): FakeSdkBuilder

### Community 37 - "Community 37"
Cohesion: 0.18
Nodes (1): GroupApiImpl

### Community 38 - "Community 38"
Cohesion: 0.18
Nodes (1): GroupApi

### Community 39 - "Community 39"
Cohesion: 0.18
Nodes (10): ConfirmPasswordChanged, EmailChanged, FirstNameChanged, LastNameChanged, PasswordChanged, RegisterIntent, SetInvitationEmail, SetInvitationToken (+2 more)

### Community 40 - "Community 40"
Cohesion: 0.18
Nodes (1): DashboardViewModelTest

### Community 41 - "Community 41"
Cohesion: 0.18
Nodes (1): ExposedUserRepository

### Community 42 - "Community 42"
Cohesion: 0.18
Nodes (2): GroupRecord, GroupRepository

### Community 43 - "Community 43"
Cohesion: 0.18
Nodes (3): MembershipRecord, MembershipRepository, MemberWithUserRecord

### Community 44 - "Community 44"
Cohesion: 0.18
Nodes (1): DocumentRoutesTest

### Community 45 - "Community 45"
Cohesion: 0.2
Nodes (2): PermissionController, SystemSettingsPane

### Community 46 - "Community 46"
Cohesion: 0.2
Nodes (2): AuraRippleIndication, AuraRippleNode

### Community 47 - "Community 47"
Cohesion: 0.2
Nodes (9): AccountDeletionIntent, CancelDeletion, ConfirmDeletion, Load, LogOut, ProceedToReAuth, ReAuthenticate, SetReason (+1 more)

### Community 48 - "Community 48"
Cohesion: 0.2
Nodes (1): PrivacySettingsViewModelTest

### Community 49 - "Community 49"
Cohesion: 0.2
Nodes (0): 

### Community 50 - "Community 50"
Cohesion: 0.2
Nodes (9): RegisterMemberMutation, SetEmail, SetFieldErrors, SetFirstName, SetLastName, SetLoading, SetPassword, SetRole (+1 more)

### Community 51 - "Community 51"
Cohesion: 0.2
Nodes (1): ProfileViewModel

### Community 52 - "Community 52"
Cohesion: 0.2
Nodes (0): 

### Community 53 - "Community 53"
Cohesion: 0.2
Nodes (8): Ai, Auth, Email, Http, OAuth, Observability, S3, ServerConfig

### Community 54 - "Community 54"
Cohesion: 0.2
Nodes (1): ExposedDataExportRepository

### Community 55 - "Community 55"
Cohesion: 0.2
Nodes (2): DataExportRecord, DataExportRepository

### Community 56 - "Community 56"
Cohesion: 0.2
Nodes (2): UserRecord, UserRepository

### Community 57 - "Community 57"
Cohesion: 0.2
Nodes (1): InvitationRoutesTest

### Community 58 - "Community 58"
Cohesion: 0.2
Nodes (1): MembershipRepositoryImpl

### Community 59 - "Community 59"
Cohesion: 0.2
Nodes (2): DocumentRepository, DocumentRow

### Community 60 - "Community 60"
Cohesion: 0.22
Nodes (1): MacSecurityCliKeychainBackendTest

### Community 61 - "Community 61"
Cohesion: 0.22
Nodes (2): FakeDpapiBackend, InMemoryKeychainBackend

### Community 62 - "Community 62"
Cohesion: 0.22
Nodes (5): Admin, GroupRole, GroupRoleSerializer, Member, Owner

### Community 63 - "Community 63"
Cohesion: 0.22
Nodes (5): Admin, PowerAdmin, User, UserRole, UserRoleSerializer

### Community 64 - "Community 64"
Cohesion: 0.22
Nodes (8): AddMemberRequest, CreateGroupRequest, GroupResponse, MemberResponse, MembershipSummary, PaginatedMemberResponse, RegisterMemberRequest, UpdateGroupRequest

### Community 65 - "Community 65"
Cohesion: 0.22
Nodes (1): FakeInvitationApiBuilder

### Community 66 - "Community 66"
Cohesion: 0.22
Nodes (1): FakeAuthApiBuilder

### Community 67 - "Community 67"
Cohesion: 0.22
Nodes (1): StaticFloatState

### Community 68 - "Community 68"
Cohesion: 0.22
Nodes (1): PrivacySettingsViewModel

### Community 69 - "Community 69"
Cohesion: 0.22
Nodes (8): AccountDeletionMutation, SetConfirmationToken, SetError, SetLoading, SetPendingDeletion, SetReason, SetStep, SetUserEmail

### Community 70 - "Community 70"
Cohesion: 0.22
Nodes (8): EmailChanged, LoginIntent, PasswordChanged, RememberMeChanged, Reset, SetInvitationEmail, SetInvitationToken, SubmitLoginClicked

### Community 71 - "Community 71"
Cohesion: 0.22
Nodes (8): InviteAcceptMutation, SetAccepting, SetAcceptSuccess, SetError, SetInvitationDetails, SetLoadingInvitation, SetLoggedIn, SetToken

### Community 72 - "Community 72"
Cohesion: 0.22
Nodes (1): AdminPanelViewModelTest

### Community 73 - "Community 73"
Cohesion: 0.22
Nodes (1): ConfigurationValidateTest

### Community 74 - "Community 74"
Cohesion: 0.22
Nodes (2): Capture, LangfusePromptClientTest

### Community 75 - "Community 75"
Cohesion: 0.22
Nodes (2): FakeFetcher, LangfusePromptProviderTest

### Community 76 - "Community 76"
Cohesion: 0.22
Nodes (2): FakeModel, JudgeSuiteTest

### Community 77 - "Community 77"
Cohesion: 0.22
Nodes (2): LangfusePromptClient, PromptFetcher

### Community 78 - "Community 78"
Cohesion: 0.22
Nodes (1): ExposedAccountDeletionRepository

### Community 79 - "Community 79"
Cohesion: 0.22
Nodes (2): AccountDeletionRecord, AccountDeletionRepository

### Community 80 - "Community 80"
Cohesion: 0.22
Nodes (1): FileRoutesTest

### Community 81 - "Community 81"
Cohesion: 0.22
Nodes (0): 

### Community 82 - "Community 82"
Cohesion: 0.25
Nodes (6): Admin, Free, Paid, PowerAdmin, Premium, UserTier

### Community 83 - "Community 83"
Cohesion: 0.25
Nodes (7): Denied, Granted, NotDetermined, Permission, PermissionStatus, Restricted, Unknown

### Community 84 - "Community 84"
Cohesion: 0.25
Nodes (3): ApplicationServicesLib, AvMediaType, MacOsObjcBridge

### Community 85 - "Community 85"
Cohesion: 0.25
Nodes (1): PreferencesStorageTest

### Community 86 - "Community 86"
Cohesion: 0.25
Nodes (1): FakeUserApiBuilder

### Community 87 - "Community 87"
Cohesion: 0.25
Nodes (1): AuthApi

### Community 88 - "Community 88"
Cohesion: 0.25
Nodes (1): InvitationApiImpl

### Community 89 - "Community 89"
Cohesion: 0.25
Nodes (1): AuthApiImpl

### Community 90 - "Community 90"
Cohesion: 0.25
Nodes (1): InvitationApi

### Community 91 - "Community 91"
Cohesion: 0.25
Nodes (1): CardVariant

### Community 92 - "Community 92"
Cohesion: 0.25
Nodes (0): 

### Community 93 - "Community 93"
Cohesion: 0.25
Nodes (1): LoginViewModelTest

### Community 94 - "Community 94"
Cohesion: 0.25
Nodes (0): 

### Community 95 - "Community 95"
Cohesion: 0.25
Nodes (7): EmailChanged, FirstNameChanged, LastNameChanged, PasswordChanged, RegisterMemberIntent, RoleChanged, SubmitRegisterMember

### Community 96 - "Community 96"
Cohesion: 0.25
Nodes (7): DashboardMutation, SetAvatarUrl, SetLoading, SetMembership, SetNavItem, SetSystemAdmin, SetUserName

### Community 97 - "Community 97"
Cohesion: 0.25
Nodes (7): AddDocument, DocumentsMutation, RemoveDocument, SetDocuments, SetError, SetLoading, SetUploading

### Community 98 - "Community 98"
Cohesion: 0.25
Nodes (1): VectorColumnType

### Community 99 - "Community 99"
Cohesion: 0.25
Nodes (2): ConstantPromptProvider, PromptProvider

### Community 100 - "Community 100"
Cohesion: 0.25
Nodes (1): LangfuseSpanAdapter

### Community 101 - "Community 101"
Cohesion: 0.25
Nodes (2): NonClosingOpenTelemetry, OtelExporterConfig

### Community 102 - "Community 102"
Cohesion: 0.25
Nodes (1): ExposedConsentRepository

### Community 103 - "Community 103"
Cohesion: 0.25
Nodes (2): ConsentRecord, ConsentRepository

### Community 104 - "Community 104"
Cohesion: 0.25
Nodes (1): GroupService

### Community 105 - "Community 105"
Cohesion: 0.29
Nodes (6): AuthResponse, ForgotPasswordRequest, LoginRequest, RefreshTokenRequest, RegisterRequest, ResetPasswordRequest

### Community 106 - "Community 106"
Cohesion: 0.29
Nodes (2): OsAuthorizationCode, PermissionController

### Community 107 - "Community 107"
Cohesion: 0.29
Nodes (1): FakeDocumentApiBuilder

### Community 108 - "Community 108"
Cohesion: 0.29
Nodes (1): UserApi

### Community 109 - "Community 109"
Cohesion: 0.29
Nodes (1): UserApiImpl

### Community 110 - "Community 110"
Cohesion: 0.29
Nodes (1): RequireSecureBaseUrlTest

### Community 111 - "Community 111"
Cohesion: 0.29
Nodes (1): MapHttpErrorTest

### Community 112 - "Community 112"
Cohesion: 0.29
Nodes (2): AuraNavItem, AuraNavSection

### Community 113 - "Community 113"
Cohesion: 0.29
Nodes (1): ConsentGateViewModel

### Community 114 - "Community 114"
Cohesion: 0.29
Nodes (6): AccountDeletionEvent, DeletionCancelled, DeletionScheduled, LoggedOut, NavigateToLogin, ShowError

### Community 115 - "Community 115"
Cohesion: 0.29
Nodes (6): DownloadExport, Load, PrivacySettingsIntent, RequestExport, ToggleConsent, ViewDocument

### Community 116 - "Community 116"
Cohesion: 0.29
Nodes (6): PrivacySettingsMutation, SetConsents, SetDeletionStatus, SetError, SetExportStatus, SetLoading

### Community 117 - "Community 117"
Cohesion: 0.29
Nodes (1): ConsentGateViewModelTest

### Community 118 - "Community 118"
Cohesion: 0.29
Nodes (6): AcceptInvitation, GoToLogin, GoToRegister, InviteAcceptIntent, LoadInvitation, RequestNewInvitation

### Community 119 - "Community 119"
Cohesion: 0.29
Nodes (1): LoginViewModel

### Community 120 - "Community 120"
Cohesion: 0.29
Nodes (0): 

### Community 121 - "Community 121"
Cohesion: 0.29
Nodes (1): RegisterViewModel

### Community 122 - "Community 122"
Cohesion: 0.29
Nodes (1): RegisterViewModelTest

### Community 123 - "Community 123"
Cohesion: 0.29
Nodes (5): ForgotPasswordRoute, InviteAcceptRoute, LoginRoute, OAuthCallbackRoute, RegisterRoute

### Community 124 - "Community 124"
Cohesion: 0.29
Nodes (1): RegisterMemberViewModelTest

### Community 125 - "Community 125"
Cohesion: 0.29
Nodes (6): AdminPanelClicked, DashboardIntent, LoadDashboard, LogoutClicked, NavItemSelected, RefreshProfile

### Community 126 - "Community 126"
Cohesion: 0.29
Nodes (1): DocumentsViewModel

### Community 127 - "Community 127"
Cohesion: 0.29
Nodes (4): ComposeView, ContentView, UIViewControllerRepresentable, View

### Community 128 - "Community 128"
Cohesion: 0.29
Nodes (1): StructuredOutputTest

### Community 129 - "Community 129"
Cohesion: 0.33
Nodes (1): Check

### Community 130 - "Community 130"
Cohesion: 0.33
Nodes (1): NavSelectionSignalTest

### Community 131 - "Community 131"
Cohesion: 0.33
Nodes (1): WindowsDpapiBackend

### Community 132 - "Community 132"
Cohesion: 0.33
Nodes (5): AgentRequest, AgentResponse, ChatRequest, ChatResponse, ChatStreamFrame

### Community 133 - "Community 133"
Cohesion: 0.33
Nodes (1): DocumentApi

### Community 134 - "Community 134"
Cohesion: 0.33
Nodes (1): DocumentApiImpl

### Community 135 - "Community 135"
Cohesion: 0.33
Nodes (1): MviViewModel

### Community 136 - "Community 136"
Cohesion: 0.33
Nodes (0): 

### Community 137 - "Community 137"
Cohesion: 0.33
Nodes (1): AppKitLib

### Community 138 - "Community 138"
Cohesion: 0.33
Nodes (5): AcceptAll, ConsentGateIntent, LoadRequiredConsents, ToggleConsent, ViewDocument

### Community 139 - "Community 139"
Cohesion: 0.33
Nodes (0): 

### Community 140 - "Community 140"
Cohesion: 0.33
Nodes (5): ExportReady, NavigateToDeletion, NavigateToDocument, PrivacySettingsEvent, ShowError

### Community 141 - "Community 141"
Cohesion: 0.33
Nodes (5): ConsentGateMutation, SetConsents, SetError, SetLoading, UpdateConsentToggle

### Community 142 - "Community 142"
Cohesion: 0.33
Nodes (5): NavigateToConsentGate, NavigateToDashboard, NavigateToGroup, RegisterEvent, ViewLegalDocument

### Community 143 - "Community 143"
Cohesion: 0.33
Nodes (1): InviteAcceptViewModel

### Community 144 - "Community 144"
Cohesion: 0.33
Nodes (5): InviteAcceptEvent, NavigateToGroup, NavigateToLogin, NavigateToRegister, RequestedNewInvitation

### Community 145 - "Community 145"
Cohesion: 0.33
Nodes (5): ActivityItem, DashboardMockData, DeploymentStatus, MetricItem, ProcessItem

### Community 146 - "Community 146"
Cohesion: 0.33
Nodes (1): ProfileViewModelTest

### Community 147 - "Community 147"
Cohesion: 0.33
Nodes (5): DeleteDocument, DocumentsIntent, LoadDocuments, RefreshDocuments, UploadFile

### Community 148 - "Community 148"
Cohesion: 0.33
Nodes (2): AddDocumentsTableAndEmbeddingColumnsMigration, EnablePgvectorAndCreateEmbeddingsTableMigration

### Community 149 - "Community 149"
Cohesion: 0.33
Nodes (1): Configuration

### Community 150 - "Community 150"
Cohesion: 0.33
Nodes (1): InvalidField

### Community 151 - "Community 151"
Cohesion: 0.33
Nodes (1): TracePrivacyTest

### Community 152 - "Community 152"
Cohesion: 0.33
Nodes (2): Capture, LangfuseScoresClientTest

### Community 153 - "Community 153"
Cohesion: 0.33
Nodes (2): Capture, LangfuseDatasetClientTest

### Community 154 - "Community 154"
Cohesion: 0.33
Nodes (2): PatchCapture, PromotionGateTest

### Community 155 - "Community 155"
Cohesion: 0.33
Nodes (1): ExperimentSummaryTest

### Community 156 - "Community 156"
Cohesion: 0.33
Nodes (1): LangfuseSpanReshaping

### Community 157 - "Community 157"
Cohesion: 0.33
Nodes (1): LangfuseRestClient

### Community 158 - "Community 158"
Cohesion: 0.33
Nodes (1): ExposedLegalDocumentRepository

### Community 159 - "Community 159"
Cohesion: 0.33
Nodes (2): LegalDocumentRecord, LegalDocumentRepository

### Community 160 - "Community 160"
Cohesion: 0.33
Nodes (1): EmailServiceTest

### Community 161 - "Community 161"
Cohesion: 0.33
Nodes (2): RagContext, RagService

### Community 162 - "Community 162"
Cohesion: 0.33
Nodes (1): WindowLifecycleTest

### Community 163 - "Community 163"
Cohesion: 0.33
Nodes (1): PlatformDirs

### Community 164 - "Community 164"
Cohesion: 0.33
Nodes (1): FileLogWriter

### Community 165 - "Community 165"
Cohesion: 0.4
Nodes (4): AcceptInvitationRequest, AcceptInvitationResponse, CreateInvitationRequest, InvitationResponse

### Community 166 - "Community 166"
Cohesion: 0.4
Nodes (0): 

### Community 167 - "Community 167"
Cohesion: 0.4
Nodes (1): PreferencesStorage

### Community 168 - "Community 168"
Cohesion: 0.4
Nodes (1): ViewModelTestContext

### Community 169 - "Community 169"
Cohesion: 0.4
Nodes (4): EventStatement, IntentStatement, ModelStatement, Statement

### Community 170 - "Community 170"
Cohesion: 0.4
Nodes (1): FakeFileApiBuilder

### Community 171 - "Community 171"
Cohesion: 0.4
Nodes (1): ImagePickerDelegate

### Community 172 - "Community 172"
Cohesion: 0.4
Nodes (1): ImagePickerResult

### Community 173 - "Community 173"
Cohesion: 0.4
Nodes (0): 

### Community 174 - "Community 174"
Cohesion: 0.4
Nodes (1): ButtonVariant

### Community 175 - "Community 175"
Cohesion: 0.4
Nodes (1): ListItemState

### Community 176 - "Community 176"
Cohesion: 0.4
Nodes (1): ReorderState

### Community 177 - "Community 177"
Cohesion: 0.4
Nodes (2): ChartDataPoint, ChartSeries

### Community 178 - "Community 178"
Cohesion: 0.4
Nodes (2): RadarDataPoint, RadarSeries

### Community 179 - "Community 179"
Cohesion: 0.4
Nodes (1): LegalDocumentViewModel

### Community 180 - "Community 180"
Cohesion: 0.4
Nodes (0): 

### Community 181 - "Community 181"
Cohesion: 0.4
Nodes (4): ConsentCompleted, ConsentGateEvent, NavigateToDocument, ShowError

### Community 182 - "Community 182"
Cohesion: 0.4
Nodes (4): LegalDocumentMutation, SetDocument, SetError, SetLoading

### Community 183 - "Community 183"
Cohesion: 0.4
Nodes (1): LegalDocumentViewModelTest

### Community 184 - "Community 184"
Cohesion: 0.4
Nodes (4): AccountDeletionRoute, ConsentGateRoute, LegalDocumentRoute, PrivacySettingsRoute

### Community 185 - "Community 185"
Cohesion: 0.4
Nodes (4): LoginEvent, NavigateToConsentGate, NavigateToDashboard, NavigateToGroup

### Community 186 - "Community 186"
Cohesion: 0.4
Nodes (1): OAuthHandler

### Community 187 - "Community 187"
Cohesion: 0.4
Nodes (1): OAuthHandler

### Community 188 - "Community 188"
Cohesion: 0.4
Nodes (1): RegisterMemberViewModel

### Community 189 - "Community 189"
Cohesion: 0.4
Nodes (0): 

### Community 190 - "Community 190"
Cohesion: 0.4
Nodes (1): EnvTest

### Community 191 - "Community 191"
Cohesion: 0.4
Nodes (3): EmptyPromptCatalog, ExamplePromptCatalog, PromptCatalog

### Community 192 - "Community 192"
Cohesion: 0.4
Nodes (1): LangfusePromptProvider

### Community 193 - "Community 193"
Cohesion: 0.4
Nodes (2): PromotionGate, PromptPromoter

### Community 194 - "Community 194"
Cohesion: 0.4
Nodes (2): PrivacyJob, PrivacyJobScheduler

### Community 195 - "Community 195"
Cohesion: 0.4
Nodes (3): ExportContributor, ExportFile, ExportSection

### Community 196 - "Community 196"
Cohesion: 0.4
Nodes (1): DocumentIngestionService

### Community 197 - "Community 197"
Cohesion: 0.4
Nodes (1): ExposedPersistenceStorage

### Community 198 - "Community 198"
Cohesion: 0.5
Nodes (1): PlatformTest

### Community 199 - "Community 199"
Cohesion: 0.5
Nodes (1): NavSelectionSignal

### Community 200 - "Community 200"
Cohesion: 0.5
Nodes (3): DocumentListResponse, DocumentResponse, DocumentUploadRequest

### Community 201 - "Community 201"
Cohesion: 0.5
Nodes (1): PermissionController

### Community 202 - "Community 202"
Cohesion: 0.5
Nodes (1): PermissionControllerJvmTest

### Community 203 - "Community 203"
Cohesion: 0.5
Nodes (1): PermissionController

### Community 204 - "Community 204"
Cohesion: 0.5
Nodes (1): PermissionController

### Community 205 - "Community 205"
Cohesion: 0.5
Nodes (1): ViewModelTest

### Community 206 - "Community 206"
Cohesion: 0.5
Nodes (0): 

### Community 207 - "Community 207"
Cohesion: 0.5
Nodes (1): FileApiImpl

### Community 208 - "Community 208"
Cohesion: 0.5
Nodes (1): FileApi

### Community 209 - "Community 209"
Cohesion: 0.5
Nodes (0): 

### Community 210 - "Community 210"
Cohesion: 0.5
Nodes (0): 

### Community 211 - "Community 211"
Cohesion: 0.5
Nodes (0): 

### Community 212 - "Community 212"
Cohesion: 0.5
Nodes (1): BadgeVariant

### Community 213 - "Community 213"
Cohesion: 0.5
Nodes (1): ModeBadgeVariant

### Community 214 - "Community 214"
Cohesion: 0.5
Nodes (1): AlertVariant

### Community 215 - "Community 215"
Cohesion: 0.5
Nodes (0): 

### Community 216 - "Community 216"
Cohesion: 0.5
Nodes (0): 

### Community 217 - "Community 217"
Cohesion: 0.5
Nodes (0): 

### Community 218 - "Community 218"
Cohesion: 0.5
Nodes (1): BarData

### Community 219 - "Community 219"
Cohesion: 0.5
Nodes (3): LegalDocumentIntent, Load, SwitchLocale

### Community 220 - "Community 220"
Cohesion: 0.5
Nodes (0): 

### Community 221 - "Community 221"
Cohesion: 0.5
Nodes (1): OAuthHandler

### Community 222 - "Community 222"
Cohesion: 0.5
Nodes (1): OAuthHandler

### Community 223 - "Community 223"
Cohesion: 0.5
Nodes (1): OAuthHandler

### Community 224 - "Community 224"
Cohesion: 0.5
Nodes (3): AdminPanelEvent, GroupCreated, NavigateToRegisterMember

### Community 225 - "Community 225"
Cohesion: 0.5
Nodes (1): BottomTab

### Community 226 - "Community 226"
Cohesion: 0.5
Nodes (3): DashboardEvent, NavigateToAdmin, NavigateToLogin

### Community 227 - "Community 227"
Cohesion: 0.5
Nodes (1): DashboardViewModel

### Community 228 - "Community 228"
Cohesion: 0.5
Nodes (3): NavigateToLogin, NavigateToPrivacySettings, ProfileEvent

### Community 229 - "Community 229"
Cohesion: 0.5
Nodes (1): ProfileModel

### Community 230 - "Community 230"
Cohesion: 0.5
Nodes (1): iOSApp

### Community 231 - "Community 231"
Cohesion: 0.5
Nodes (1): Migration

### Community 232 - "Community 232"
Cohesion: 0.5
Nodes (1): MigrationRegistry

### Community 233 - "Community 233"
Cohesion: 0.5
Nodes (3): BootError, InvalidConfig, MissingRequiredEnv

### Community 234 - "Community 234"
Cohesion: 0.5
Nodes (1): TraceContext

### Community 235 - "Community 235"
Cohesion: 0.5
Nodes (1): LangfuseScoresClient

### Community 236 - "Community 236"
Cohesion: 0.5
Nodes (1): LangfuseDatasetClient

### Community 237 - "Community 237"
Cohesion: 0.5
Nodes (1): PromptRefreshScheduler

### Community 238 - "Community 238"
Cohesion: 0.5
Nodes (2): ExperimentSummary, JudgeAggregate

### Community 239 - "Community 239"
Cohesion: 0.5
Nodes (1): TlsTest

### Community 240 - "Community 240"
Cohesion: 0.5
Nodes (1): UserService

### Community 241 - "Community 241"
Cohesion: 0.5
Nodes (0): 

### Community 242 - "Community 242"
Cohesion: 0.5
Nodes (1): CreateConversationsTableMigration

### Community 243 - "Community 243"
Cohesion: 0.5
Nodes (1): UserTools

### Community 244 - "Community 244"
Cohesion: 0.5
Nodes (2): RelevanceCheck, RelevanceDetector

### Community 245 - "Community 245"
Cohesion: 0.5
Nodes (2): HealthStatus, ServiceHealth

### Community 246 - "Community 246"
Cohesion: 0.5
Nodes (1): SingleInstanceGuard

### Community 247 - "Community 247"
Cohesion: 0.5
Nodes (0): 

### Community 248 - "Community 248"
Cohesion: 0.5
Nodes (1): MainActivity

### Community 249 - "Community 249"
Cohesion: 0.5
Nodes (1): EnvLoader

### Community 250 - "Community 250"
Cohesion: 0.67
Nodes (0): 

### Community 251 - "Community 251"
Cohesion: 0.67
Nodes (0): 

### Community 252 - "Community 252"
Cohesion: 0.67
Nodes (0): 

### Community 253 - "Community 253"
Cohesion: 0.67
Nodes (0): 

### Community 254 - "Community 254"
Cohesion: 0.67
Nodes (0): 

### Community 255 - "Community 255"
Cohesion: 0.67
Nodes (2): UpdateProfileRequest, UserResponse

### Community 256 - "Community 256"
Cohesion: 0.67
Nodes (1): AuthInterceptor

### Community 257 - "Community 257"
Cohesion: 0.67
Nodes (0): 

### Community 258 - "Community 258"
Cohesion: 0.67
Nodes (0): 

### Community 259 - "Community 259"
Cohesion: 0.67
Nodes (1): ReducedMotionTest

### Community 260 - "Community 260"
Cohesion: 0.67
Nodes (0): 

### Community 261 - "Community 261"
Cohesion: 0.67
Nodes (0): 

### Community 262 - "Community 262"
Cohesion: 0.67
Nodes (0): 

### Community 263 - "Community 263"
Cohesion: 0.67
Nodes (0): 

### Community 264 - "Community 264"
Cohesion: 0.67
Nodes (0): 

### Community 265 - "Community 265"
Cohesion: 0.67
Nodes (0): 

### Community 266 - "Community 266"
Cohesion: 0.67
Nodes (0): 

### Community 267 - "Community 267"
Cohesion: 0.67
Nodes (0): 

### Community 268 - "Community 268"
Cohesion: 0.67
Nodes (0): 

### Community 269 - "Community 269"
Cohesion: 0.67
Nodes (0): 

### Community 270 - "Community 270"
Cohesion: 0.67
Nodes (0): 

### Community 271 - "Community 271"
Cohesion: 0.67
Nodes (0): 

### Community 272 - "Community 272"
Cohesion: 0.67
Nodes (0): 

### Community 273 - "Community 273"
Cohesion: 0.67
Nodes (2): AuraGlow, AuraGlows

### Community 274 - "Community 274"
Cohesion: 0.67
Nodes (2): AuraShadow, AuraShadows

### Community 275 - "Community 275"
Cohesion: 0.67
Nodes (0): 

### Community 276 - "Community 276"
Cohesion: 0.67
Nodes (2): ConsentGateModel, ConsentItem

### Community 277 - "Community 277"
Cohesion: 0.67
Nodes (2): LegalDocumentEvent, ShowError

### Community 278 - "Community 278"
Cohesion: 0.67
Nodes (2): AccountDeletionModel, DeletionStep

### Community 279 - "Community 279"
Cohesion: 0.67
Nodes (0): 

### Community 280 - "Community 280"
Cohesion: 0.67
Nodes (0): 

### Community 281 - "Community 281"
Cohesion: 0.67
Nodes (2): RegisterMemberEvent, RegistrationSuccess

### Community 282 - "Community 282"
Cohesion: 0.67
Nodes (2): AdminPanelRoute, RegisterMemberRoute

### Community 283 - "Community 283"
Cohesion: 0.67
Nodes (0): 

### Community 284 - "Community 284"
Cohesion: 0.67
Nodes (0): 

### Community 285 - "Community 285"
Cohesion: 0.67
Nodes (0): 

### Community 286 - "Community 286"
Cohesion: 0.67
Nodes (0): 

### Community 287 - "Community 287"
Cohesion: 0.67
Nodes (0): 

### Community 288 - "Community 288"
Cohesion: 0.67
Nodes (2): DocumentsEvent, UploadSuccess

### Community 289 - "Community 289"
Cohesion: 0.67
Nodes (1): DataSource

### Community 290 - "Community 290"
Cohesion: 0.67
Nodes (1): DomainError

### Community 291 - "Community 291"
Cohesion: 0.67
Nodes (1): ServerStrings

### Community 292 - "Community 292"
Cohesion: 0.67
Nodes (2): PromptConfig, PromptSpec

### Community 293 - "Community 293"
Cohesion: 0.67
Nodes (1): KoogJudgeModel

### Community 294 - "Community 294"
Cohesion: 0.67
Nodes (1): ExportProcessorJob

### Community 295 - "Community 295"
Cohesion: 0.67
Nodes (1): ExportCleanupJob

### Community 296 - "Community 296"
Cohesion: 0.67
Nodes (1): DeletionExecutorJob

### Community 297 - "Community 297"
Cohesion: 0.67
Nodes (1): ConsentRequired

### Community 298 - "Community 298"
Cohesion: 0.67
Nodes (1): SmtpEmailService

### Community 299 - "Community 299"
Cohesion: 0.67
Nodes (1): InvalidCredentials

### Community 300 - "Community 300"
Cohesion: 0.67
Nodes (1): EmailService

### Community 301 - "Community 301"
Cohesion: 0.67
Nodes (1): RoleConfig

### Community 302 - "Community 302"
Cohesion: 0.67
Nodes (0): 

### Community 303 - "Community 303"
Cohesion: 0.67
Nodes (0): 

### Community 304 - "Community 304"
Cohesion: 0.67
Nodes (0): 

### Community 305 - "Community 305"
Cohesion: 0.67
Nodes (1): GroupNotFound

### Community 306 - "Community 306"
Cohesion: 0.67
Nodes (1): InvitationNotFound

### Community 307 - "Community 307"
Cohesion: 0.67
Nodes (1): StructuredOutputService

### Community 308 - "Community 308"
Cohesion: 0.67
Nodes (1): AgentExecutionFailed

### Community 309 - "Community 309"
Cohesion: 0.67
Nodes (1): FileTooLarge

### Community 310 - "Community 310"
Cohesion: 0.67
Nodes (0): 

### Community 311 - "Community 311"
Cohesion: 0.67
Nodes (1): SingleInstanceGuardTest

### Community 312 - "Community 312"
Cohesion: 0.67
Nodes (0): 

### Community 313 - "Community 313"
Cohesion: 0.67
Nodes (0): 

### Community 314 - "Community 314"
Cohesion: 0.67
Nodes (0): 

### Community 315 - "Community 315"
Cohesion: 0.67
Nodes (0): 

### Community 316 - "Community 316"
Cohesion: 1.0
Nodes (0): 

### Community 317 - "Community 317"
Cohesion: 1.0
Nodes (1): Route

### Community 318 - "Community 318"
Cohesion: 1.0
Nodes (1): ErrorResponse

### Community 319 - "Community 319"
Cohesion: 1.0
Nodes (1): FileResponse

### Community 320 - "Community 320"
Cohesion: 1.0
Nodes (1): StringKey

### Community 321 - "Community 321"
Cohesion: 1.0
Nodes (0): 

### Community 322 - "Community 322"
Cohesion: 1.0
Nodes (0): 

### Community 323 - "Community 323"
Cohesion: 1.0
Nodes (0): 

### Community 324 - "Community 324"
Cohesion: 1.0
Nodes (0): 

### Community 325 - "Community 325"
Cohesion: 1.0
Nodes (1): Sdk

### Community 326 - "Community 326"
Cohesion: 1.0
Nodes (0): 

### Community 327 - "Community 327"
Cohesion: 1.0
Nodes (0): 

### Community 328 - "Community 328"
Cohesion: 1.0
Nodes (0): 

### Community 329 - "Community 329"
Cohesion: 1.0
Nodes (0): 

### Community 330 - "Community 330"
Cohesion: 1.0
Nodes (0): 

### Community 331 - "Community 331"
Cohesion: 1.0
Nodes (0): 

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
Nodes (1): AuraRadius

### Community 336 - "Community 336"
Cohesion: 1.0
Nodes (1): AuraBorders

### Community 337 - "Community 337"
Cohesion: 1.0
Nodes (1): AuraMotion

### Community 338 - "Community 338"
Cohesion: 1.0
Nodes (1): AuraColors

### Community 339 - "Community 339"
Cohesion: 1.0
Nodes (1): AuraSpacing

### Community 340 - "Community 340"
Cohesion: 1.0
Nodes (0): 

### Community 341 - "Community 341"
Cohesion: 1.0
Nodes (1): AuraTypography

### Community 342 - "Community 342"
Cohesion: 1.0
Nodes (1): AuraGap

### Community 343 - "Community 343"
Cohesion: 1.0
Nodes (1): AuraOpacity

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
Nodes (1): LegalDocumentModel

### Community 353 - "Community 353"
Cohesion: 1.0
Nodes (1): PrivacySettingsModel

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
Nodes (1): InviteAcceptModel

### Community 360 - "Community 360"
Cohesion: 1.0
Nodes (0): 

### Community 361 - "Community 361"
Cohesion: 1.0
Nodes (0): 

### Community 362 - "Community 362"
Cohesion: 1.0
Nodes (1): LoginModel

### Community 363 - "Community 363"
Cohesion: 1.0
Nodes (1): RegisterModel

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
Nodes (1): AdminPanelModel

### Community 372 - "Community 372"
Cohesion: 1.0
Nodes (1): RegisterMemberModel

### Community 373 - "Community 373"
Cohesion: 1.0
Nodes (0): 

### Community 374 - "Community 374"
Cohesion: 1.0
Nodes (0): 

### Community 375 - "Community 375"
Cohesion: 1.0
Nodes (1): DashboardModel

### Community 376 - "Community 376"
Cohesion: 1.0
Nodes (1): DashboardRoute

### Community 377 - "Community 377"
Cohesion: 1.0
Nodes (0): 

### Community 378 - "Community 378"
Cohesion: 1.0
Nodes (0): 

### Community 379 - "Community 379"
Cohesion: 1.0
Nodes (1): ProfileRoute

### Community 380 - "Community 380"
Cohesion: 1.0
Nodes (0): 

### Community 381 - "Community 381"
Cohesion: 1.0
Nodes (1): DocumentsModel

### Community 382 - "Community 382"
Cohesion: 1.0
Nodes (0): 

### Community 383 - "Community 383"
Cohesion: 1.0
Nodes (1): DocumentsRoute

### Community 384 - "Community 384"
Cohesion: 1.0
Nodes (0): 

### Community 385 - "Community 385"
Cohesion: 1.0
Nodes (1): DocumentEmbeddingsTable

### Community 386 - "Community 386"
Cohesion: 1.0
Nodes (1): DocumentsTable

### Community 387 - "Community 387"
Cohesion: 1.0
Nodes (1): MigrationsTable

### Community 388 - "Community 388"
Cohesion: 1.0
Nodes (0): 

### Community 389 - "Community 389"
Cohesion: 1.0
Nodes (0): 

### Community 390 - "Community 390"
Cohesion: 1.0
Nodes (1): LangfuseConfig

### Community 391 - "Community 391"
Cohesion: 1.0
Nodes (1): JudgeVerdict

### Community 392 - "Community 392"
Cohesion: 1.0
Nodes (1): LegalDocumentServiceImpl

### Community 393 - "Community 393"
Cohesion: 1.0
Nodes (1): ConsentServiceImpl

### Community 394 - "Community 394"
Cohesion: 1.0
Nodes (0): 

### Community 395 - "Community 395"
Cohesion: 1.0
Nodes (0): 

### Community 396 - "Community 396"
Cohesion: 1.0
Nodes (0): 

### Community 397 - "Community 397"
Cohesion: 1.0
Nodes (0): 

### Community 398 - "Community 398"
Cohesion: 1.0
Nodes (1): LegalDocumentsTable

### Community 399 - "Community 399"
Cohesion: 1.0
Nodes (1): AccountDeletionRequestsTable

### Community 400 - "Community 400"
Cohesion: 1.0
Nodes (1): DataExportRequestsTable

### Community 401 - "Community 401"
Cohesion: 1.0
Nodes (1): ConsentRecordsTable

### Community 402 - "Community 402"
Cohesion: 1.0
Nodes (0): 

### Community 403 - "Community 403"
Cohesion: 1.0
Nodes (1): GoogleUserInfo

### Community 404 - "Community 404"
Cohesion: 1.0
Nodes (1): AuthServiceImpl

### Community 405 - "Community 405"
Cohesion: 1.0
Nodes (0): 

### Community 406 - "Community 406"
Cohesion: 1.0
Nodes (1): RolesTable

### Community 407 - "Community 407"
Cohesion: 1.0
Nodes (1): UsersTable

### Community 408 - "Community 408"
Cohesion: 1.0
Nodes (1): InvitationServiceImpl

### Community 409 - "Community 409"
Cohesion: 1.0
Nodes (1): InvitationsTable

### Community 410 - "Community 410"
Cohesion: 1.0
Nodes (1): UserGroupMembershipsTable

### Community 411 - "Community 411"
Cohesion: 1.0
Nodes (1): GroupsTable

### Community 412 - "Community 412"
Cohesion: 1.0
Nodes (1): InvitationService

### Community 413 - "Community 413"
Cohesion: 1.0
Nodes (0): 

### Community 414 - "Community 414"
Cohesion: 1.0
Nodes (0): 

### Community 415 - "Community 415"
Cohesion: 1.0
Nodes (0): 

### Community 416 - "Community 416"
Cohesion: 1.0
Nodes (1): ConversationsTable

### Community 417 - "Community 417"
Cohesion: 1.0
Nodes (0): 

### Community 418 - "Community 418"
Cohesion: 1.0
Nodes (0): 

### Community 419 - "Community 419"
Cohesion: 1.0
Nodes (0): 

### Community 420 - "Community 420"
Cohesion: 1.0
Nodes (0): 

### Community 421 - "Community 421"
Cohesion: 1.0
Nodes (0): 

### Community 422 - "Community 422"
Cohesion: 1.0
Nodes (0): 

### Community 423 - "Community 423"
Cohesion: 1.0
Nodes (0): 

### Community 424 - "Community 424"
Cohesion: 1.0
Nodes (0): 

### Community 425 - "Community 425"
Cohesion: 1.0
Nodes (0): 

### Community 426 - "Community 426"
Cohesion: 1.0
Nodes (0): 

### Community 427 - "Community 427"
Cohesion: 1.0
Nodes (0): 

### Community 428 - "Community 428"
Cohesion: 1.0
Nodes (0): 

### Community 429 - "Community 429"
Cohesion: 1.0
Nodes (0): 

### Community 430 - "Community 430"
Cohesion: 1.0
Nodes (0): 

### Community 431 - "Community 431"
Cohesion: 1.0
Nodes (0): 

### Community 432 - "Community 432"
Cohesion: 1.0
Nodes (0): 

### Community 433 - "Community 433"
Cohesion: 1.0
Nodes (0): 

### Community 434 - "Community 434"
Cohesion: 1.0
Nodes (0): 

### Community 435 - "Community 435"
Cohesion: 1.0
Nodes (0): 

### Community 436 - "Community 436"
Cohesion: 1.0
Nodes (0): 

### Community 437 - "Community 437"
Cohesion: 1.0
Nodes (0): 

### Community 438 - "Community 438"
Cohesion: 1.0
Nodes (0): 

### Community 439 - "Community 439"
Cohesion: 1.0
Nodes (0): 

### Community 440 - "Community 440"
Cohesion: 1.0
Nodes (0): 

### Community 441 - "Community 441"
Cohesion: 1.0
Nodes (0): 

### Community 442 - "Community 442"
Cohesion: 1.0
Nodes (0): 

### Community 443 - "Community 443"
Cohesion: 1.0
Nodes (0): 

### Community 444 - "Community 444"
Cohesion: 1.0
Nodes (0): 

### Community 445 - "Community 445"
Cohesion: 1.0
Nodes (0): 

### Community 446 - "Community 446"
Cohesion: 1.0
Nodes (0): 

### Community 447 - "Community 447"
Cohesion: 1.0
Nodes (0): 

### Community 448 - "Community 448"
Cohesion: 1.0
Nodes (0): 

### Community 449 - "Community 449"
Cohesion: 1.0
Nodes (0): 

### Community 450 - "Community 450"
Cohesion: 1.0
Nodes (0): 

### Community 451 - "Community 451"
Cohesion: 1.0
Nodes (0): 

### Community 452 - "Community 452"
Cohesion: 1.0
Nodes (0): 

### Community 453 - "Community 453"
Cohesion: 1.0
Nodes (0): 

### Community 454 - "Community 454"
Cohesion: 1.0
Nodes (0): 

### Community 455 - "Community 455"
Cohesion: 1.0
Nodes (0): 

### Community 456 - "Community 456"
Cohesion: 1.0
Nodes (0): 

### Community 457 - "Community 457"
Cohesion: 1.0
Nodes (0): 

### Community 458 - "Community 458"
Cohesion: 1.0
Nodes (0): 

### Community 459 - "Community 459"
Cohesion: 1.0
Nodes (0): 

### Community 460 - "Community 460"
Cohesion: 1.0
Nodes (0): 

### Community 461 - "Community 461"
Cohesion: 1.0
Nodes (0): 

### Community 462 - "Community 462"
Cohesion: 1.0
Nodes (0): 

### Community 463 - "Community 463"
Cohesion: 1.0
Nodes (0): 

### Community 464 - "Community 464"
Cohesion: 1.0
Nodes (0): 

### Community 465 - "Community 465"
Cohesion: 1.0
Nodes (0): 

### Community 466 - "Community 466"
Cohesion: 1.0
Nodes (0): 

### Community 467 - "Community 467"
Cohesion: 1.0
Nodes (0): 

### Community 468 - "Community 468"
Cohesion: 1.0
Nodes (0): 

### Community 469 - "Community 469"
Cohesion: 1.0
Nodes (0): 

### Community 470 - "Community 470"
Cohesion: 1.0
Nodes (0): 

### Community 471 - "Community 471"
Cohesion: 1.0
Nodes (0): 

### Community 472 - "Community 472"
Cohesion: 1.0
Nodes (0): 

### Community 473 - "Community 473"
Cohesion: 1.0
Nodes (0): 

## Knowledge Gaps
- **597 isolated node(s):** `Check`, `Route`, `ProcessOutcome`, `GroupRole`, `Owner` (+592 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **Thin community `Community 316`** (2 nodes): `NavigateAdd.kt`, `navigateAdd()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 317`** (2 nodes): `Route.kt`, `Route`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 318`** (2 nodes): `ErrorResponse.kt`, `ErrorResponse`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 319`** (2 nodes): `FileDtos.kt`, `FileResponse`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 320`** (2 nodes): `StringKey.kt`, `StringKey`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 321`** (2 nodes): `MviViewModelTestDsl.kt`, `test()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 322`** (2 nodes): `PlatformEngine.ios.kt`, `platformEngine()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 323`** (2 nodes): `PlatformConfig.ios.kt`, `defaultBaseUrl()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 324`** (2 nodes): `PlatformEngine.kt`, `platformEngine()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 325`** (2 nodes): `Sdk.kt`, `Sdk`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 326`** (2 nodes): `PlatformConfig.kt`, `defaultBaseUrl()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 327`** (2 nodes): `PlatformEngine.jvm.kt`, `platformEngine()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 328`** (2 nodes): `PlatformConfig.jvm.kt`, `defaultBaseUrl()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 329`** (2 nodes): `PlatformEngine.wasmJs.kt`, `platformEngine()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 330`** (2 nodes): `PlatformConfig.android.kt`, `defaultBaseUrl()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 331`** (2 nodes): `PlatformEngine.android.kt`, `platformEngine()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 332`** (2 nodes): `ReducedMotion.ios.kt`, `prefersReducedMotion()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 333`** (2 nodes): `ImageDecoder.kt`, `rememberDecodedImage()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 334`** (2 nodes): `AuraModalSheet.kt`, `AuraModalSheet()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 335`** (2 nodes): `AuraRadius.kt`, `AuraRadius`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 336`** (2 nodes): `AuraBorders.kt`, `AuraBorders`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 337`** (2 nodes): `AuraMotion.kt`, `AuraMotion`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 338`** (2 nodes): `AuraColors.kt`, `AuraColors`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 339`** (2 nodes): `AuraSpacing.kt`, `AuraSpacing`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 340`** (2 nodes): `AuraTheme.kt`, `AuraTheme()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 341`** (2 nodes): `AuraTypography.kt`, `AuraTypography`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 342`** (2 nodes): `AuraGap.kt`, `AuraGap`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 343`** (2 nodes): `AuraOpacity.kt`, `AuraOpacity`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 344`** (2 nodes): `ImageDecoder.jvm.kt`, `rememberDecodedImage()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 345`** (2 nodes): `ImagePicker.jvm.kt`, `rememberImagePickerLauncher()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 346`** (2 nodes): `ImageDecoder.wasmJs.kt`, `rememberDecodedImage()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 347`** (2 nodes): `ImagePicker.wasmJs.kt`, `rememberImagePickerLauncher()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 348`** (2 nodes): `ReducedMotion.wasmJs.kt`, `prefersReducedMotion()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 349`** (2 nodes): `ImageDecoder.android.kt`, `rememberDecodedImage()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 350`** (2 nodes): `ImagePicker.android.kt`, `rememberImagePickerLauncher()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 351`** (2 nodes): `ReducedMotion.android.kt`, `prefersReducedMotion()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 352`** (2 nodes): `LegalDocumentModel.kt`, `LegalDocumentModel`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 353`** (2 nodes): `PrivacySettingsModel.kt`, `PrivacySettingsModel`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 354`** (2 nodes): `PrivacyNavigation.kt`, `privacyEntries()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 355`** (2 nodes): `InviteLinkChecker.ios.kt`, `checkInviteLink()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 356`** (2 nodes): `OAuthCallbackChecker.ios.kt`, `checkOAuthCallback()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 357`** (2 nodes): `StringKeyResolver.kt`, `resolveStringKey()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 358`** (2 nodes): `InviteLinkChecker.kt`, `checkInviteLink()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 359`** (2 nodes): `InviteAcceptModel.kt`, `InviteAcceptModel`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 360`** (2 nodes): `OAuthCallbackChecker.kt`, `checkOAuthCallback()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 361`** (2 nodes): `OAuthCallbackHandler.kt`, `OAuthCallbackHandler()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 362`** (2 nodes): `LoginModel.kt`, `LoginModel`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 363`** (2 nodes): `RegisterModel.kt`, `RegisterModel`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 364`** (2 nodes): `OAuthCallbackChecker.jvm.kt`, `checkOAuthCallback()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 365`** (2 nodes): `InviteLinkChecker.jvm.kt`, `checkInviteLink()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 366`** (2 nodes): `OAuthCallbackChecker.wasmJs.kt`, `checkOAuthCallback()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 367`** (2 nodes): `InviteLinkChecker.wasmJs.kt`, `checkInviteLink()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 368`** (2 nodes): `InviteLinkChecker.android.kt`, `checkInviteLink()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 369`** (2 nodes): `OAuthCallbackChecker.android.kt`, `checkOAuthCallback()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 370`** (2 nodes): `AuthNavigation.kt`, `authEntries()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 371`** (2 nodes): `AdminPanelModel.kt`, `AdminPanelModel`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 372`** (2 nodes): `RegisterMemberModel.kt`, `RegisterMemberModel`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 373`** (2 nodes): `RegisterMemberScreen.kt`, `RegisterMemberScreen()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 374`** (2 nodes): `AdminNavigation.kt`, `adminEntries()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 375`** (2 nodes): `DashboardModel.kt`, `DashboardModel`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 376`** (2 nodes): `DashboardRoute.kt`, `DashboardRoute`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 377`** (2 nodes): `DashboardNavigation.kt`, `dashboardEntries()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 378`** (2 nodes): `PremiumTierContent.kt`, `PremiumTierContent()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 379`** (2 nodes): `ProfileRoute.kt`, `ProfileRoute`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 380`** (2 nodes): `ProfileNavigation.kt`, `profileEntries()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 381`** (2 nodes): `DocumentsModel.kt`, `DocumentsModel`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 382`** (2 nodes): `DocumentsScreen.kt`, `DocumentsScreen()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 383`** (2 nodes): `DocumentsRoute.kt`, `DocumentsRoute`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 384`** (2 nodes): `DocumentsNavigation.kt`, `documentsEntries()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 385`** (2 nodes): `DocumentEmbeddingsTable.kt`, `DocumentEmbeddingsTable`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 386`** (2 nodes): `DocumentsTable.kt`, `DocumentsTable`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 387`** (2 nodes): `MigrationsTable.kt`, `MigrationsTable`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 388`** (2 nodes): `Error.kt`, `preferredLanguage()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 389`** (2 nodes): `SecurityPlugin.kt`, `configureSecurity()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 390`** (2 nodes): `LangfuseConfig.kt`, `LangfuseConfig`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 391`** (2 nodes): `JudgeVerdict.kt`, `JudgeVerdict`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 392`** (2 nodes): `LegalDocumentServiceImpl.kt`, `LegalDocumentServiceImpl`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 393`** (2 nodes): `ConsentServiceImpl.kt`, `ConsentServiceImpl`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 394`** (2 nodes): `DeletionRoutes.kt`, `deletionRoutes()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 395`** (2 nodes): `LegalRoutes.kt`, `legalRoutes()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 396`** (2 nodes): `ExportRoutes.kt`, `exportRoutes()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 397`** (2 nodes): `ConsentRoutes.kt`, `consentRoutes()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 398`** (2 nodes): `LegalDocumentsTable.kt`, `LegalDocumentsTable`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 399`** (2 nodes): `AccountDeletionRequestsTable.kt`, `AccountDeletionRequestsTable`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 400`** (2 nodes): `DataExportRequestsTable.kt`, `DataExportRequestsTable`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 401`** (2 nodes): `ConsentRecordsTable.kt`, `ConsentRecordsTable`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 402`** (2 nodes): `PrivacyWireModule.kt`, `registerPrivacyWireMigrations()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 403`** (2 nodes): `OAuthService.kt`, `GoogleUserInfo`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 404`** (2 nodes): `AuthServiceImpl.kt`, `AuthServiceImpl`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 405`** (2 nodes): `UserRoutes.kt`, `userRoutes()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 406`** (2 nodes): `RolesTable.kt`, `RolesTable`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 407`** (2 nodes): `UsersTable.kt`, `UsersTable`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 408`** (2 nodes): `InvitationServiceImpl.kt`, `InvitationServiceImpl`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 409`** (2 nodes): `InvitationsTable.kt`, `InvitationsTable`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 410`** (2 nodes): `UserGroupMembershipsTable.kt`, `UserGroupMembershipsTable`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 411`** (2 nodes): `GroupsTable.kt`, `GroupsTable`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 412`** (2 nodes): `InvitationService.kt`, `InvitationService`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 413`** (2 nodes): `GroupWireModule.kt`, `registerGroupWireMigrations()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 414`** (2 nodes): `ChatAgent.kt`, `streamChat()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 415`** (2 nodes): `ChatStreamingStrategy.kt`, `chatStreamingStrategy()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 416`** (2 nodes): `ConversationsTable.kt`, `ConversationsTable`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 417`** (2 nodes): `DocumentRoutes.kt`, `documentRoutes()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 418`** (2 nodes): `AiWireModule.kt`, `registerAiWireMigrations()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 419`** (2 nodes): `FileRoutes.kt`, `fileRoutes()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 420`** (2 nodes): `Application.kt`, `main()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 421`** (2 nodes): `Server.kt`, `startServer()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 422`** (2 nodes): `AvatarRoutes.kt`, `avatarRoutes()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 423`** (2 nodes): `MainViewController.kt`, `MainViewController()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 424`** (2 nodes): `SystemDarkTheme.ios.kt`, `rememberSystemDarkTheme()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 425`** (2 nodes): `AppNavHost.kt`, `AppNavHost()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 426`** (2 nodes): `SystemDarkTheme.kt`, `rememberSystemDarkTheme()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 427`** (2 nodes): `LocaleSelector.kt`, `LocaleSelector()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 428`** (2 nodes): `main.kt`, `main()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 429`** (2 nodes): `EffectsGalleryMain.kt`, `main()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 430`** (2 nodes): `SystemDarkTheme.wasmJs.kt`, `rememberSystemDarkTheme()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 431`** (2 nodes): `SystemDarkTheme.android.kt`, `rememberSystemDarkTheme()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 432`** (2 nodes): `PlistLint.kt`, `plistLint()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 433`** (1 nodes): `settings.gradle.kts`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 434`** (1 nodes): `NavigationModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 435`** (1 nodes): `StorageModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 436`** (1 nodes): `Annotations.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 437`** (1 nodes): `SdkModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 438`** (1 nodes): `AuraPreview.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 439`** (1 nodes): `LocalTitleBarInset.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 440`** (1 nodes): `PrivacyModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 441`** (1 nodes): `AuthScreen.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 442`** (1 nodes): `AuthModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 443`** (1 nodes): `AdminModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 444`** (1 nodes): `DashboardModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 445`** (1 nodes): `ProfileModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 446`** (1 nodes): `DocumentsModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 447`** (1 nodes): `Startup.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 448`** (1 nodes): `Module.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 449`** (1 nodes): `DataExportServiceImpl.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 450`** (1 nodes): `DataExportService.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 451`** (1 nodes): `ConsentService.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 452`** (1 nodes): `LegalDocumentService.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 453`** (1 nodes): `EmailModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 454`** (1 nodes): `AuthService.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 455`** (1 nodes): `GroupModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 456`** (1 nodes): `Models.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 457`** (1 nodes): `AiModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 458`** (1 nodes): `AssistantAgent.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 459`** (1 nodes): `AiRoutes.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 460`** (1 nodes): `FileModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 461`** (1 nodes): `S3FileService.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 462`** (1 nodes): `FileService.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 463`** (1 nodes): `FileWireModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 464`** (1 nodes): `ServerModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 465`** (1 nodes): `Config.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 466`** (1 nodes): `SharedModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 467`** (1 nodes): `spa-routing.js`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 468`** (1 nodes): `AppModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 469`** (1 nodes): `LocalAppLocale.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 470`** (1 nodes): `AndroidModule.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 471`** (1 nodes): `kover-convention.gradle.kts`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 472`** (1 nodes): `server-module-convention.gradle.kts`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 473`** (1 nodes): `kmp-library-convention.gradle.kts`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What connects `Check`, `Route`, `ProcessOutcome` to the rest of the system?**
  _597 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Community 0` be split into smaller, more focused modules?**
  _Cohesion score 0.04 - nodes in this community are weakly interconnected._
- **Should `Community 1` be split into smaller, more focused modules?**
  _Cohesion score 0.04 - nodes in this community are weakly interconnected._
- **Should `Community 2` be split into smaller, more focused modules?**
  _Cohesion score 0.07 - nodes in this community are weakly interconnected._
- **Should `Community 3` be split into smaller, more focused modules?**
  _Cohesion score 0.09 - nodes in this community are weakly interconnected._
- **Should `Community 4` be split into smaller, more focused modules?**
  _Cohesion score 0.1 - nodes in this community are weakly interconnected._
- **Should `Community 5` be split into smaller, more focused modules?**
  _Cohesion score 0.1 - nodes in this community are weakly interconnected._