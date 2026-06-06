import org.w3c.dom.Element
import org.xml.sax.EntityResolver
import org.xml.sax.InputSource
import java.io.File
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Reusable, dependency-free macOS `.plist` linter for Gradle build scripts.
 *
 * Parses a property-list XML file with a hardened [DocumentBuilderFactory] (no DTD fetch, no
 * external entities, no network) and reports which of [requiredKeys] are absent from the
 * top-level `<dict>`. The hardening matters because Apple plists declare the PLIST DTD via a
 * `<!DOCTYPE … "https://www.apple.com/DTDs/PropertyList-1.0.dtd">` — without the no-op
 * [EntityResolver] and the disabled features below, the parse would make a live network request.
 *
 * Ships the MECHANISM only: [requiredKeys] defaults to empty, so the template carries the linter
 * without asserting any particular key. Callers pass the keys they care about.
 *
 * @return one human-readable message per missing key; empty when every required key is present
 *   (and empty when [requiredKeys] is empty).
 */
fun plistLint(file: File, requiredKeys: List<String> = emptyList()): List<String> {
    if (!file.exists()) {
        return listOf("plist not found at ${file.absolutePath}")
    }
    if (requiredKeys.isEmpty()) {
        return emptyList()
    }

    val factory = DocumentBuilderFactory.newInstance().apply {
        isValidating = false
        // Allow the doctype declaration but never resolve the external DTD (no network request).
        setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
        setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        setFeature("http://xml.org/sax/features/external-general-entities", false)
        setFeature("http://xml.org/sax/features/external-parameter-entities", false)
    }
    val builder = factory.newDocumentBuilder()
    // No-op entity resolver: returns an empty InputSource for any external DTD reference so the
    // Apple PLIST DTD URL is never fetched.
    val noopResolver = EntityResolver { _, _ -> InputSource(StringReader("")) }
    builder.setEntityResolver(noopResolver)

    val doc = builder.parse(file)
    val dictNodes = doc.documentElement.getElementsByTagName("dict")
    if (dictNodes.length == 0) {
        return listOf("plist <plist> has no <dict> child in ${file.name}")
    }
    val dict = dictNodes.item(0) as Element

    val presentKeys = buildSet {
        val children = dict.childNodes
        var i = 0
        while (i < children.length) {
            val node = children.item(i)
            if (node is Element && node.tagName == "key") {
                add(node.textContent.trim())
            }
            i++
        }
    }

    return requiredKeys
        .filter { it !in presentKeys }
        .map { "plist ${file.name} missing required key: $it" }
}
