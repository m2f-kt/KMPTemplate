package com.m2f.template.securestorage

import com.sun.jna.platform.win32.Crypt32Util
import java.io.File

/**
 * Production [DpapiBackend] for Windows — seals/unseals arbitrary secret payloads with DPAPI
 * (`CryptProtectData` / `CryptUnprotectData`) via JNA-Platform.
 *
 * Each `(service, account)` pair is persisted as its own sealed envelope file under [dir]
 * (filename derived deterministically from service/account). POSIX permissions are tightened
 * defensively in case the directory is on a network share that respects them.
 */
internal class WindowsDpapiBackend : DpapiBackend {

    override fun load(dir: File, service: String, account: String): ByteArray? {
        val sealedFile = envelopeFile(dir, service, account)
        if (!sealedFile.exists()) return null
        val sealed = sealedFile.readBytes()
        return Crypt32Util.cryptUnprotectData(sealed)
    }

    override fun store(dir: File, service: String, account: String, bytes: ByteArray) {
        dir.mkdirs()
        val sealedFile = envelopeFile(dir, service, account)
        val sealed = Crypt32Util.cryptProtectData(bytes)
        sealedFile.writeBytes(sealed)
        sealedFile.setReadable(false, false)
        sealedFile.setReadable(true, true)
        sealedFile.setWritable(false, false)
        sealedFile.setWritable(true, true)
    }

    override fun remove(dir: File, service: String, account: String) {
        envelopeFile(dir, service, account).delete()
    }

    private companion object {
        /**
         * Deterministic, filesystem-safe envelope filename for a `(service, account)` pair.
         * Non-alphanumeric chars are replaced so arbitrary service/account strings cannot
         * escape [dir] or collide with path separators.
         */
        fun envelopeFile(dir: File, service: String, account: String): File {
            val safe = "${service}__${account}".map { c ->
                if (c.isLetterOrDigit() || c == '_' || c == '-' || c == '.') c else '_'
            }.joinToString("")
            return File(dir, "$safe.dpapi")
        }
    }
}
