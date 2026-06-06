import java.io.File

/**
 * Single `.env` source-of-truth loader for Gradle tasks.
 *
 * Gradle has no native dotenv support, and JVM tasks default to inheriting the *ambient shell*
 * environment — so a globally-exported var (e.g. another project's `LANGFUSE_*`) silently shadows
 * the project's committed `.env`. To make `.env` authoritative, [load] parses the project `.env`
 * (falling back to `.env.example`) and the caller injects the result into a spawned JVM via
 * `JavaExec.environment(k, v)`, which OVERRIDES the inherited shell value for that key in the child
 * process. The child's `System.getenv(...)` then reads the `.env` value, not the stray shell one.
 *
 * Call this at EXECUTION time (`doFirst { … }` / `onlyIf { … }`) — never at configuration time —
 * so it stays compatible with the configuration cache and always reflects the current `.env`.
 */
object EnvLoader {

    /** Parsed `.env` (or `.env.example`) as key→value. Empty when neither file exists. */
    fun load(projectRoot: File): Map<String, String> {
        val file = listOf(File(projectRoot, ".env"), File(projectRoot, ".env.example"))
            .firstOrNull { it.isFile }
            ?: return emptyMap()
        return file.readLines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") && it.contains('=') }
            .associate { line ->
                val idx = line.indexOf('=')
                val key = line.substring(0, idx).trim()
                val value = line.substring(idx + 1).trim()
                    .removeSurrounding("\"")
                    .removeSurrounding("'")
                key to value
            }
    }

    /** `.env`-first resolution for [key]: the committed `.env` wins over an ambient shell var. */
    fun resolve(projectRoot: File, key: String): String? =
        load(projectRoot)[key] ?: System.getenv(key)
}
