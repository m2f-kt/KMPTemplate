package com.m2f.template.app.documents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.m2f.template.designsystem.components.TerminalText
import com.m2f.template.designsystem.components.button.ButtonVariant
import com.m2f.template.designsystem.components.button.TerminalButton
import com.m2f.template.designsystem.components.card.CardVariant
import com.m2f.template.designsystem.components.card.TerminalCard
import com.m2f.template.designsystem.components.data.TerminalTable
import com.m2f.template.designsystem.components.data.TerminalTableCell
import com.m2f.template.designsystem.components.data.TerminalTableRow
import com.m2f.template.designsystem.components.feedback.AlertVariant
import com.m2f.template.designsystem.components.feedback.BadgeVariant
import com.m2f.template.designsystem.components.feedback.TerminalAlert
import com.m2f.template.designsystem.components.feedback.TerminalBadge
import com.m2f.template.designsystem.theme.TerminalTheme
import org.jetbrains.compose.resources.stringResource
import template.app.documents.generated.resources.Res
import template.app.documents.generated.resources.documents_back
import template.app.documents.generated.resources.documents_delete
import template.app.documents.generated.resources.documents_empty
import template.app.documents.generated.resources.documents_empty_description
import template.app.documents.generated.resources.documents_error_description
import template.app.documents.generated.resources.documents_error_title
import template.app.documents.generated.resources.documents_loading
import template.app.documents.generated.resources.documents_table_actions
import template.app.documents.generated.resources.documents_table_chunks
import template.app.documents.generated.resources.documents_table_created
import template.app.documents.generated.resources.documents_table_name
import template.app.documents.generated.resources.documents_table_status
import template.app.documents.generated.resources.documents_table_type
import template.app.documents.generated.resources.documents_title
import template.app.documents.generated.resources.documents_upload
import template.app.documents.generated.resources.documents_upload_success
import template.app.documents.generated.resources.documents_uploading

/**
 * Stateless documents management screen for viewing and managing RAG documents.
 *
 * Uses design system components (TerminalCard, TerminalTable, TerminalBadge, TerminalButton)
 * to render a document list with upload and delete actions.
 *
 * @param state Current documents state with document list, loading, and error flags.
 * @param onUploadClick Callback to trigger file picker for document upload.
 * @param onDeleteDocument Callback to delete a document by its ID.
 * @param onBack Callback to navigate back.
 * @param showUploadSuccess Whether to show the upload success alert.
 * @param modifier Modifier for the screen root.
 */
@Composable
fun DocumentsScreen(
    state: DocumentsModel,
    onUploadClick: () -> Unit,
    onDeleteDocument: (String) -> Unit,
    onBack: () -> Unit,
    showUploadSuccess: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.bg)
                .verticalScroll(rememberScrollState())
                .padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // Top bar: back button + title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                TerminalButton(
                    text = stringResource(Res.string.documents_back),
                    onClick = onBack,
                )
                TerminalText(
                    text = stringResource(Res.string.documents_title),
                    style = typography.xxl.copy(fontWeight = FontWeight.Bold),
                    color = colors.text,
                )
            }

            // Loading state (initial load)
            if (state.isLoading && state.documents.isEmpty()) {
                TerminalText(
                    text = stringResource(Res.string.documents_loading),
                    style = typography.md,
                    color = colors.textMuted,
                )
                return@Column
            }

            // Error state (no data loaded)
            if (state.error != null && state.documents.isEmpty()) {
                TerminalCard(
                    title = stringResource(Res.string.documents_error_title),
                    description = stringResource(Res.string.documents_error_description),
                    variant = CardVariant.Default,
                ) {
                    TerminalBadge(
                        text = "error: ${resolveStringKey(state.error)}",
                        variant = BadgeVariant.Error,
                    )
                }
                return@Column
            }

            // Upload success message
            if (showUploadSuccess) {
                TerminalAlert(
                    message = stringResource(Res.string.documents_upload_success),
                    variant = AlertVariant.Success,
                )
            }

            // Upload button
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TerminalButton(
                    text = if (state.isUploading) {
                        stringResource(Res.string.documents_uploading)
                    } else {
                        stringResource(Res.string.documents_upload)
                    },
                    onClick = onUploadClick,
                    variant = ButtonVariant.Secondary,
                    enabled = !state.isUploading,
                )
            }

            // Empty state
            if (state.documents.isEmpty()) {
                TerminalCard(
                    title = stringResource(Res.string.documents_empty),
                    description = stringResource(Res.string.documents_empty_description),
                    variant = CardVariant.Default,
                ) {}
                return@Column
            }

            // Documents table
            TerminalTable(
                headers = listOf(
                    stringResource(Res.string.documents_table_name),
                    stringResource(Res.string.documents_table_type),
                    stringResource(Res.string.documents_table_status),
                    stringResource(Res.string.documents_table_chunks),
                    stringResource(Res.string.documents_table_created),
                    stringResource(Res.string.documents_table_actions),
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                state.documents.forEachIndexed { index, document ->
                    TerminalTableRow(
                        showBottomBorder = index < state.documents.lastIndex,
                    ) {
                        TerminalTableCell(text = document.name)
                        TerminalTableCell(text = document.contentType, secondary = true)
                        Box(modifier = Modifier.weight(1f)) {
                            TerminalBadge(
                                text = document.status,
                                variant = when (document.status.lowercase()) {
                                    "ingested" -> BadgeVariant.Success
                                    "pending" -> BadgeVariant.Accent
                                    "failed" -> BadgeVariant.Error
                                    else -> BadgeVariant.Default
                                },
                            )
                        }
                        TerminalTableCell(
                            text = document.chunkCount.toString(),
                            secondary = true,
                        )
                        TerminalTableCell(
                            text = document.createdAt.take(10),
                            secondary = true,
                        )
                        Box(modifier = Modifier.weight(1f)) {
                            TerminalButton(
                                text = stringResource(Res.string.documents_delete),
                                onClick = { onDeleteDocument(document.id) },
                                variant = ButtonVariant.Ghost,
                            )
                        }
                    }
                }
            }

            // Inline error (when documents are already loaded but a new error occurs)
            if (state.error != null && state.documents.isNotEmpty()) {
                TerminalAlert(
                    message = resolveStringKey(state.error),
                    variant = AlertVariant.Error,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
