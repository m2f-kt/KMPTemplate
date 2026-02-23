# Phase 19: Structured AI & RAG Pipeline - Context

**Gathered:** 2026-02-23
**Status:** Ready for planning

<domain>
## Phase Boundary

AI can return typed structured responses via Koog, and chat is augmented with group-scoped document context via Koog-exclusive RAG. Users upload documents through a dedicated UI, documents are chunked, embedded (Google text-embedding-004 via Koog LLMEmbedder), and stored in pgvector. The existing AI chat endpoint is enhanced with automatic relevance-based RAG retrieval. Group isolation ensures documents are scoped correctly.

</domain>

<decisions>
## Implementation Decisions

### Structured output design
- No dedicated structured output API endpoint — Koog structured LLM calls are used inline in server code wherever needed
- No example schemas needed — the Koog integration itself is the pattern for template users
- Stick with Gemini for structured output calls (consistent with existing chat model)
- Structured output used wherever helpful — within RAG pipeline, multi-agent (Phase 20), or any server logic

### Document ingestion flow
- Claude's discretion on whether ingestion is automatic on upload or a separate explicit action
- Claude's discretion on supported file types (balance simplicity vs coverage)
- Embedding via Koog LLMEmbedder with Google text-embedding-004 (matches 768-dim pgvector column from Phase 17)
- Dedicated Documents UI for uploading and managing documents (not API-only)

### RAG retrieval behavior
- Auto-detect relevance — RAG triggers only when the query seems to need document context (not always-on, not manual toggle)
- Hidden context — retrieved chunks are injected silently into the prompt, no citations/sources shown to user
- Enhance existing AI chat streaming endpoint — RAG is built into current chat, not a separate endpoint
- Documents can be deleted with full cleanup — embeddings removed from vector store when document is deleted

### Group scoping & access
- Two document levels: personal documents and group documents
- Any member can upload personal documents for themselves
- Only admins can upload group-level documents
- Admins can upload personal documents on behalf of specific group members (e.g., trainer assigns materials to a client)
- Admin-assigned personal documents are visible to the member but only deletable by an admin
- RAG search scope is role-based:
  - Members: RAG searches their personal documents only (including admin-assigned ones)
  - Admins: RAG searches personal documents AND/OR group documents
- Documents UI shows a single list with labels indicating personal vs group (no tabs)
- Admin document management lives in the admin panel's group section — admins manage group docs and member-assigned docs from there

### Claude's Discretion
- Auto-ingest vs explicit indexing action on upload
- Supported file types (text, PDF, etc.)
- Chunking strategy and chunk size
- Number of chunks retrieved per query
- Relevance detection heuristic for auto-RAG
- Loading states and empty states in Documents UI
- Exact layout of the Documents UI

</decisions>

<specifics>
## Specific Ideas

- Trainer/client use case: admin (trainer) uploads personalized documents for specific members (clients) — those docs augment the member's AI chat experience
- Documents UI is part of the admin panel group section for admins, while members see their personal documents in a separate view

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope

</deferred>

---

*Phase: 19-structured-ai-rag-pipeline*
*Context gathered: 2026-02-23*
