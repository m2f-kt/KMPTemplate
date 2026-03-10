package com.m2f.template.app.documents.wire

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import com.m2f.template.app.documents.DocumentsEvent
import com.m2f.template.app.documents.DocumentsIntent
import com.m2f.template.app.documents.DocumentsScreen
import com.m2f.template.app.documents.DocumentsViewModel
import com.m2f.template.app.documents.contract.DocumentsRoute
import com.m2f.template.navigation.Route
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<Route>.documentsEntries(
    backStack: MutableList<Route>,
) {
    entry<DocumentsRoute> { route ->
        val viewModel = koinViewModel<DocumentsViewModel>()
        val state by viewModel.model.collectAsStateWithLifecycle()
        var showUploadSuccess by remember { mutableStateOf(false) }

        LaunchedEffect(route.groupId) {
            viewModel.take(DocumentsIntent.LoadDocuments(route.groupId))
        }

        LaunchedEffect(Unit) {
            viewModel.event.collect { event ->
                when (event) {
                    is DocumentsEvent.UploadSuccess -> {
                        showUploadSuccess = true
                    }
                }
            }
        }

        DocumentsScreen(
            state = state,
            onUploadClick = { },
            onDeleteDocument = { documentId ->
                viewModel.take(DocumentsIntent.DeleteDocument(documentId))
            },
            onBack = { backStack.removeLastOrNull() },
            showUploadSuccess = showUploadSuccess,
        )
    }
}
