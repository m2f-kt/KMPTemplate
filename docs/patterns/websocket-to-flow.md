# Pattern: WebSocket → Flow (streaming SDK endpoints)

How the client SDK turns a Ktor WebSocket into a cold, cancellable `Flow` of typed
domain updates — the streaming counterpart to the `apiCall { }` wrapper used for
request/response endpoints.

This is a *shape*, not a feature. The template ships no streaming endpoint; this
documents the recipe so a new one is built consistently. All errors still flow
through `AppError` (see `core:models`) and the SDK's `mapException`
(`core/sdk/.../sdk/ErrorMapper.kt`).

## Why a Flow

A streaming endpoint emits 0..N partial updates then terminates (normally or with
an error). That maps cleanly to a `Flow<Update>` where `Update` is a sealed type
carrying either a partial result or a terminal `Error(AppError)`. The collector
just `collect`s; cancelling the collecting coroutine tears down the socket.

## The five moving parts

1. **`channelFlow { }`** — not a plain `flow { }`. The socket has a *concurrent
   sender* (uploading outbound frames) running alongside the *receiver* loop, so
   emissions come from more than one coroutine. `channelFlow` is the only builder
   that allows `send` from a launched child.

2. **A launched sender** — `launch { outbound.collect { send(Frame.Binary(...)) } }`
   inside the `webSocket { }` block pumps the caller's outbound `Flow` into the
   socket. When the outbound flow *completes*, send any in-band "end of input"
   control frame the protocol defines, then let the receiver keep reading so the
   server's final message isn't lost to a premature client close. `cancel()` the
   sender in a `finally`.

3. **`closeReason` → typed error** — Ktor consumes the peer's `Frame.Close`
   internally and closes the `incoming` channel; the terminal reason is exposed via
   `closeReason.await()`. After the receive loop ends, inspect it: any code other
   than `1000` (normal) should be surfaced as a terminal `Update.Error(...)` (e.g.
   `AppError.AI.*`) so the collector observes the failure instead of a silent
   completion.

4. **`CancellationException` rethrow** — wrap the body in `try/catch`. Catch
   `CancellationException` FIRST and rethrow it unchanged — never swallow it, or
   structured-concurrency cancellation breaks. Only the second, broad
   `catch (t: Throwable)` converts an unexpected failure into a terminal
   `Update.Error`.

5. **`mapException(t)`** — that broad catch feeds the throwable through the SDK's
   shared `mapException` so transport/serialization failures become the same
   `AppError` taxonomy the rest of the SDK uses. No raw exceptions escape the Flow.

## Skeleton

```kotlin
override fun openStream(
    sessionId: String,
    outbound: Flow<ByteArray>,
): Flow<Update> = channelFlow {
    try {
        client.webSocket(path = STREAM_PATH, request = { parameter("sessionId", sessionId) }) {
            // Auth header is added by AuthInterceptor on the shared HttpClient — do NOT
            // set Authorization manually here (Ktor would append a 2nd value and the
            // handshake would fail).
            val sender = launch {
                outbound.collect { send(Frame.Binary(fin = true, data = it)) }
                runCatching { send(Frame.Text(CONTROL_END_OF_INPUT)) } // in-band, NOT a WS close
            }
            try {
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        send(json.decodeFromString<Update>(frame.readText()))
                    }
                }
                val reason = closeReason.await()
                if (reason != null && reason.code != WS_CLOSE_NORMAL) {
                    send(Update.Error(/* AppError carrying reason.code / reason.message */))
                }
            } finally {
                sender.cancel()
            }
        }
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (t: Throwable) {
        send(Update.Error(mapException(t)))
    }
}

private companion object {
    const val STREAM_PATH = "/api/<feature>/stream"
    const val WS_CLOSE_NORMAL: Short = 1000
}
```

## Pitfalls

- **Don't client-close to signal end-of-input.** A client WS close completes the
  client session and the server's final message can race the teardown and be lost.
  Use an in-band control frame and keep reading.
- **Don't set `Authorization` manually** when an `AuthInterceptor` is installed on
  the shared `HttpClient` — duplicate headers break the handshake.
- **Don't use `flow { }`** — concurrent `send` from the launched sender requires
  `channelFlow`.
- **Always rethrow `CancellationException`** before the broad catch.

## Testing

JVM-only: drive it with `ktor-server-test-host` + `ktor-server-websockets`
(see `core/sdk` `jvmTest` deps). Stand up a test WebSocket route, point the SDK's
client at it, and assert the emitted `Flow` with Turbine. Server-side close codes
are exercised by closing the session with a non-`1000` reason and asserting a
terminal `Update.Error`.

> Reference shape (not copied here): NoType's `SttApiImpl.openStream` is the
> production embodiment of this pattern.
