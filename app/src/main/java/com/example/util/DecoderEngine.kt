package com.example.util

import android.util.Base64
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.json.JSONObject

enum class FormatType(val displayName: String, val description: String) {
    AUTO("Auto Detect", "Detects format automatically"),
    AS_IT_IS("Make It As It Is", "Recursively decodes nested encodings"),
    BASE64("Base64", "Standard binary-to-text encoding"),
    HEX("Hexadecimal", "Hex byte representation"),
    URL("URL / URI", "Percent-encoded web format"),
    BINARY("Binary (8-bit)", "0s and 1s byte sequence"),
    MORSE("Morse Code", "Dots and dashes representation"),
    JWT("JWT Token", "JSON Web Token Header & Payload"),
    ROT13("Caesar / ROT13", "Alphabetical rotation shift"),
    HTML_ENTITIES("HTML Entities", "HTML escaped characters"),
    ASCII("ASCII Numbers", "Numeric byte codes"),
    UNICODE("Unicode Escapes", "\\uXXXX unicode sequences"),
    BASE32("Base32", "Base32 alphabet encoding")
}

data class DecodeResult(
    val output: String,
    val formatUsed: FormatType,
    val isSuccess: Boolean,
    val steps: List<String> = emptyList(),
    val errorMessage: String? = null,
    val metadata: Map<String, String> = emptyMap()
)

data class JwtParsed(
    val isValid: Boolean,
    val headerJson: String = "",
    val payloadJson: String = "",
    val signature: String = "",
    val algorithm: String = "",
    val tokenType: String = "",
    val subject: String = "",
    val issuer: String = "",
    val issuedAt: String = "",
    val expiresAt: String = "",
    val isExpired: Boolean = false,
    val remainingTimeText: String = "",
    val rawClaims: Map<String, String> = emptyMap()
)

object DecoderEngine {

    private val MORSE_MAP = mapOf(
        'A' to ".-", 'B' to "-...", 'C' to "-.-.", 'D' to "-..", 'E' to ".",
        'F' to "..-.", 'G' to "--.", 'H' to "....", 'I' to "..", 'J' to ".---",
        'K' to "-.-", 'L' to ".-..", 'M' to "--", 'N' to "-.", 'O' to "---",
        'P' to ".--.", 'Q' to "--.-", 'R' to ".-.", 'S' to "...", 'T' to "-",
        'U' to "..-", 'V' to "...-", 'W' to ".--", 'X' to "-..-", 'Y' to "-.--",
        'Z' to "--..",
        '0' to "-----", '1' to ".----", '2' to "..---", '3' to "...--", '4' to "....-",
        '5' to ".....", '6' to "-....", '7' to "--...", '8' to "---..", '9' to "----.",
        '.' to ".-.-.-", ',' to "--..--", '?' to "..--..", '\'' to ".----.", '!' to "-.-.--",
        '/' to "-..-.", '(' to "-.--.", ')' to "-.--.-", '&' to ".-...", ':' to "---...",
        ';' to "-.-.-.", '=' to "-...-", '+' to ".-.-.", '-' to "-....-", '_' to "..--.-",
        '"' to ".-..-.", '$' to "...-..-", '@' to ".--.-.", ' ' to "/"
    )

    private val REVERSE_MORSE_MAP = MORSE_MAP.entries.associate { (k, v) -> v to k }

    // --- MAIN DECODE FUNCTION ---
    fun decode(input: String, format: FormatType, caesarShift: Int = 13): DecodeResult {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            return DecodeResult("", format, false, errorMessage = "Input is empty")
        }

        return when (format) {
            FormatType.AUTO -> autoDecode(trimmed)
            FormatType.AS_IT_IS -> restoreAsItIs(trimmed)
            FormatType.BASE64 -> decodeBase64(trimmed)
            FormatType.HEX -> decodeHex(trimmed)
            FormatType.URL -> decodeUrl(trimmed)
            FormatType.BINARY -> decodeBinary(trimmed)
            FormatType.MORSE -> decodeMorse(trimmed)
            FormatType.JWT -> decodeJwt(trimmed)
            FormatType.ROT13 -> decodeCaesar(trimmed, caesarShift)
            FormatType.HTML_ENTITIES -> decodeHtmlEntities(trimmed)
            FormatType.ASCII -> decodeAscii(trimmed)
            FormatType.UNICODE -> decodeUnicode(trimmed)
            FormatType.BASE32 -> decodeBase32(trimmed)
        }
    }

    // --- MAIN ENCODE FUNCTION ---
    fun encode(input: String, format: FormatType, caesarShift: Int = 13): String {
        if (input.isEmpty()) return ""
        return when (format) {
            FormatType.BASE64 -> encodeBase64(input)
            FormatType.HEX -> encodeHex(input)
            FormatType.URL -> encodeUrl(input)
            FormatType.BINARY -> encodeBinary(input)
            FormatType.MORSE -> encodeMorse(input)
            FormatType.ROT13 -> encodeCaesar(input, caesarShift)
            FormatType.HTML_ENTITIES -> encodeHtmlEntities(input)
            FormatType.ASCII -> encodeAscii(input)
            FormatType.UNICODE -> encodeUnicode(input)
            FormatType.BASE32 -> encodeBase32(input)
            else -> encodeBase64(input)
        }
    }

    // --- "MAKE IT AS IT IS" RECURSIVE RESTORATION ---
    fun restoreAsItIs(input: String): DecodeResult {
        var current = input.trim()
        val steps = mutableListOf<String>()
        steps.add("Original Encoded Input (${current.length} chars)")

        var pass = 1
        val maxPasses = 10
        var formatDetectedInPass = FormatType.AUTO

        while (pass <= maxPasses) {
            val autoRes = autoDecode(current)
            if (!autoRes.isSuccess || autoRes.output == current || autoRes.output.isBlank()) {
                break
            }
            // Protect against non-printable binary garbage
            if (isUnreadableGarbage(autoRes.output)) {
                break
            }

            formatDetectedInPass = autoRes.formatUsed
            current = autoRes.output
            steps.add("Pass $pass: Decoded as ${autoRes.formatUsed.displayName} -> Result (${current.length} chars)")
            pass++
        }

        if (steps.size == 1) {
            return DecodeResult(
                output = input,
                formatUsed = FormatType.AS_IT_IS,
                isSuccess = true,
                steps = listOf("No additional encodings detected. Data is already as it is."),
                errorMessage = null
            )
        }

        return DecodeResult(
            output = current,
            formatUsed = formatDetectedInPass,
            isSuccess = true,
            steps = steps
        )
    }

    // --- AUTO DETECT DECODE ---
    fun autoDecode(input: String): DecodeResult {
        val trimmed = input.trim()

        // 1. Try JWT
        if (isJwtFormat(trimmed)) {
            val jwtRes = decodeJwt(trimmed)
            if (jwtRes.isSuccess) return jwtRes
        }

        // 2. Try URL
        if (trimmed.contains("%") && (trimmed.contains("%20") || trimmed.contains("%3A") || trimmed.contains("%2F") || trimmed.contains("%22"))) {
            val urlRes = decodeUrl(trimmed)
            if (urlRes.isSuccess && urlRes.output != trimmed) return urlRes
        }

        // 3. Try Binary
        if (isBinaryFormat(trimmed)) {
            val binRes = decodeBinary(trimmed)
            if (binRes.isSuccess && !isUnreadableGarbage(binRes.output)) return binRes
        }

        // 4. Try Morse
        if (isMorseFormat(trimmed)) {
            val morseRes = decodeMorse(trimmed)
            if (morseRes.isSuccess && morseRes.output.isNotBlank()) return morseRes
        }

        // 5. Try HTML Entities
        if (trimmed.contains("&") && (trimmed.contains("&lt;") || trimmed.contains("&gt;") || trimmed.contains("&amp;") || trimmed.contains("&#"))) {
            val htmlRes = decodeHtmlEntities(trimmed)
            if (htmlRes.isSuccess) return htmlRes
        }

        // 6. Try Unicode Escapes
        if (trimmed.contains("\\u") && Regex("""\\u[0-9a-fA-F]{4}""").containsMatchIn(trimmed)) {
            val uniRes = decodeUnicode(trimmed)
            if (uniRes.isSuccess) return uniRes
        }

        // 7. Try ASCII numbers
        if (isAsciiFormat(trimmed)) {
            val asciiRes = decodeAscii(trimmed)
            if (asciiRes.isSuccess && !isUnreadableGarbage(asciiRes.output)) return asciiRes
        }

        // 8. Try Hex
        if (isHexFormat(trimmed)) {
            val hexRes = decodeHex(trimmed)
            if (hexRes.isSuccess && !isUnreadableGarbage(hexRes.output)) return hexRes
        }

        // 9. Try Base64
        if (isBase64Format(trimmed)) {
            val b64Res = decodeBase64(trimmed)
            if (b64Res.isSuccess && !isUnreadableGarbage(b64Res.output)) return b64Res
        }

        // Fallback: If URL decode changes anything, return URL decode
        val urlFallback = decodeUrl(trimmed)
        if (urlFallback.isSuccess && urlFallback.output != trimmed) {
            return urlFallback
        }

        return DecodeResult(
            output = trimmed,
            formatUsed = FormatType.AUTO,
            isSuccess = true,
            steps = listOf("PlainText (No standard encoding detected)"),
            errorMessage = "Data appears to be plain text already."
        )
    }

    // --- BASE64 DECODING / ENCODING ---
    fun decodeBase64(input: String): DecodeResult {
        return try {
            val cleaned = input.replace("\n", "").replace("\r", "").trim()
            val bytes = Base64.decode(cleaned, Base64.DEFAULT or Base64.NO_PADDING or Base64.URL_SAFE)
            val decodedStr = String(bytes, StandardCharsets.UTF_8)
            DecodeResult(decodedStr, FormatType.BASE64, true)
        } catch (e: Exception) {
            DecodeResult(input, FormatType.BASE64, false, errorMessage = "Invalid Base64 string: ${e.message}")
        }
    }

    fun encodeBase64(input: String): String {
        return try {
            Base64.encodeToString(input.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
        } catch (e: Exception) {
            ""
        }
    }

    // --- HEX DECODING / ENCODING ---
    fun decodeHex(input: String): DecodeResult {
        return try {
            val cleanHex = input.replace(" ", "").replace("0x", "").replace(":", "")
            if (cleanHex.length % 2 != 0) {
                return DecodeResult(input, FormatType.HEX, false, errorMessage = "Hex string length must be even")
            }
            val bytes = ByteArray(cleanHex.length / 2)
            for (i in cleanHex.indices step 2) {
                val byteVal = cleanHex.substring(i, i + 2).toInt(16)
                bytes[i / 2] = byteVal.toByte()
            }
            val decodedStr = String(bytes, StandardCharsets.UTF_8)
            DecodeResult(decodedStr, FormatType.HEX, true)
        } catch (e: Exception) {
            DecodeResult(input, FormatType.HEX, false, errorMessage = "Invalid Hex string: ${e.message}")
        }
    }

    fun encodeHex(input: String, separator: String = " "): String {
        return try {
            input.toByteArray(StandardCharsets.UTF_8).joinToString(separator) { String.format("%02X", it) }
        } catch (e: Exception) {
            ""
        }
    }

    // --- URL DECODING / ENCODING ---
    fun decodeUrl(input: String): DecodeResult {
        return try {
            val decoded = URLDecoder.decode(input, "UTF-8")
            DecodeResult(decoded, FormatType.URL, true)
        } catch (e: Exception) {
            DecodeResult(input, FormatType.URL, false, errorMessage = "URL decoding failed: ${e.message}")
        }
    }

    fun encodeUrl(input: String): String {
        return try {
            URLEncoder.encode(input, "UTF-8")
        } catch (e: Exception) {
            ""
        }
    }

    // --- BINARY DECODING / ENCODING ---
    fun decodeBinary(input: String): DecodeResult {
        return try {
            val cleanBin = input.replace(Regex("[^01]"), "")
            if (cleanBin.isEmpty() || cleanBin.length % 8 != 0) {
                return DecodeResult(input, FormatType.BINARY, false, errorMessage = "Binary sequence length must be multiple of 8 bits")
            }
            val sb = StringBuilder()
            for (i in cleanBin.indices step 8) {
                val byteStr = cleanBin.substring(i, i + 8)
                val charVal = byteStr.toInt(2).toChar()
                sb.append(charVal)
            }
            DecodeResult(sb.toString(), FormatType.BINARY, true)
        } catch (e: Exception) {
            DecodeResult(input, FormatType.BINARY, false, errorMessage = "Binary decode failed: ${e.message}")
        }
    }

    fun encodeBinary(input: String): String {
        val sb = StringBuilder()
        for (char in input) {
            val binStr = Integer.toBinaryString(char.code).padStart(8, '0')
            sb.append(binStr).append(" ")
        }
        return sb.toString().trim()
    }

    // --- MORSE CODE DECODING / ENCODING ---
    fun decodeMorse(input: String): DecodeResult {
        return try {
            val words = input.trim().split(" / ", "   ")
            val sb = StringBuilder()
            for (word in words) {
                val letters = word.trim().split(" ")
                for (letter in letters) {
                    if (letter.isBlank()) continue
                    val char = REVERSE_MORSE_MAP[letter] ?: '?'
                    sb.append(char)
                }
                sb.append(" ")
            }
            DecodeResult(sb.toString().trim(), FormatType.MORSE, true)
        } catch (e: Exception) {
            DecodeResult(input, FormatType.MORSE, false, errorMessage = "Morse decoding failed: ${e.message}")
        }
    }

    fun encodeMorse(input: String): String {
        val sb = StringBuilder()
        for (char in input.uppercase(Locale.ROOT)) {
            val morse = MORSE_MAP[char] ?: ""
            if (morse.isNotEmpty()) {
                sb.append(morse).append(" ")
            }
        }
        return sb.toString().trim()
    }

    // --- JWT DECODING ---
    fun decodeJwt(input: String): DecodeResult {
        val parts = input.split(".")
        if (parts.size != 3) {
            return DecodeResult(input, FormatType.JWT, false, errorMessage = "JWT must consist of 3 dot-separated parts")
        }

        return try {
            val headerDecoded = decodeBase64(parts[0]).output
            val payloadDecoded = decodeBase64(parts[1]).output
            val signature = parts[2]

            val formattedHeader = try { JSONObject(headerDecoded).toString(2) } catch (_: Exception) { headerDecoded }
            val formattedPayload = try { JSONObject(payloadDecoded).toString(2) } catch (_: Exception) { payloadDecoded }

            val parsedJwt = parseJwtDetails(formattedHeader, formattedPayload, signature)

            val fullOutput = "--- HEADER ---\n$formattedHeader\n\n--- PAYLOAD ---\n$formattedPayload\n\n--- SIGNATURE ---\n$signature"
            DecodeResult(
                output = fullOutput,
                formatUsed = FormatType.JWT,
                isSuccess = true,
                metadata = mapOf(
                    "header" to formattedHeader,
                    "payload" to formattedPayload,
                    "signature" to signature,
                    "exp" to parsedJwt.expiresAt,
                    "sub" to parsedJwt.subject,
                    "iss" to parsedJwt.issuer,
                    "alg" to parsedJwt.algorithm
                )
            )
        } catch (e: Exception) {
            DecodeResult(input, FormatType.JWT, false, errorMessage = "JWT decoding failed: ${e.message}")
        }
    }

    fun parseJwtDetails(headerJsonStr: String, payloadJsonStr: String, signature: String): JwtParsed {
        return try {
            val headerObj = try { JSONObject(headerJsonStr) } catch (_: Exception) { JSONObject() }
            val payloadObj = try { JSONObject(payloadJsonStr) } catch (_: Exception) { JSONObject() }

            val alg = headerObj.optString("alg", "Unknown")
            val typ = headerObj.optString("typ", "JWT")
            val sub = payloadObj.optString("sub", "")
            val iss = payloadObj.optString("iss", "")
            val iatEpoch = payloadObj.optLong("iat", -1L)
            val expEpoch = payloadObj.optLong("exp", -1L)

            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.getDefault())
            val iatStr = if (iatEpoch > 0) sdf.format(Date(iatEpoch * 1000)) else ""
            
            var expStr = ""
            var isExpired = false
            var remaining = ""

            if (expEpoch > 0) {
                val expDate = Date(expEpoch * 1000)
                expStr = sdf.format(expDate)
                val now = System.currentTimeMillis()
                val diffMs = (expEpoch * 1000) - now
                if (diffMs < 0) {
                    isExpired = true
                    remaining = "Expired"
                } else {
                    val days = diffMs / (1000 * 60 * 60 * 24)
                    val hours = (diffMs / (1000 * 60 * 60)) % 24
                    val mins = (diffMs / (1000 * 60)) % 60
                    remaining = "${days}d ${hours}h ${mins}m valid"
                }
            }

            val claims = mutableMapOf<String, String>()
            val keys = payloadObj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                claims[key] = payloadObj.opt(key)?.toString() ?: ""
            }

            JwtParsed(
                isValid = true,
                headerJson = headerJsonStr,
                payloadJson = payloadJsonStr,
                signature = signature,
                algorithm = alg,
                tokenType = typ,
                subject = sub,
                issuer = iss,
                issuedAt = iatStr,
                expiresAt = expStr,
                isExpired = isExpired,
                remainingTimeText = remaining,
                rawClaims = claims
            )
        } catch (e: Exception) {
            JwtParsed(isValid = false)
        }
    }

    // --- CAESAR / ROT13 DECODING / ENCODING ---
    fun decodeCaesar(input: String, shift: Int = 13): DecodeResult {
        val decoded = shiftText(input, -shift)
        return DecodeResult(decoded, FormatType.ROT13, true, metadata = mapOf("shift" to shift.toString()))
    }

    fun encodeCaesar(input: String, shift: Int = 13): String {
        return shiftText(input, shift)
    }

    private fun shiftText(input: String, shift: Int): String {
        val normalizedShift = (shift % 26 + 26) % 26
        val sb = StringBuilder()
        for (char in input) {
            when {
                char in 'a'..'z' -> {
                    val newChar = ((char - 'a' + normalizedShift) % 26 + 'a'.code).toChar()
                    sb.append(newChar)
                }
                char in 'A'..'Z' -> {
                    val newChar = ((char - 'A' + normalizedShift) % 26 + 'A'.code).toChar()
                    sb.append(newChar)
                }
                else -> sb.append(char)
            }
        }
        return sb.toString()
    }

    // --- HTML ENTITIES DECODING / ENCODING ---
    fun decodeHtmlEntities(input: String): DecodeResult {
        var res = input
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&apos;", "'")
            .replace("&nbsp;", " ")
            .replace("&copy;", "©")
            .replace("&reg;", "®")

        // Handle numeric entities &#8217; or &#x2019;
        val numericRegex = Regex("&#([0-9]+);")
        res = numericRegex.replace(res) { match ->
            val code = match.groupValues[1].toIntOrNull()
            if (code != null) code.toChar().toString() else match.value
        }

        val hexRegex = Regex("&#x([0-9a-fA-F]+);")
        res = hexRegex.replace(res) { match ->
            val code = match.groupValues[1].toIntOrNull(16)
            if (code != null) code.toChar().toString() else match.value
        }

        return DecodeResult(res, FormatType.HTML_ENTITIES, true)
    }

    fun encodeHtmlEntities(input: String): String {
        return input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }

    // --- ASCII NUMBERS DECODING / ENCODING ---
    fun decodeAscii(input: String): DecodeResult {
        return try {
            val parts = input.trim().split(Regex("[,\\s]+"))
            val sb = StringBuilder()
            for (p in parts) {
                if (p.isBlank()) continue
                val code = p.toInt()
                sb.append(code.toChar())
            }
            DecodeResult(sb.toString(), FormatType.ASCII, true)
        } catch (e: Exception) {
            DecodeResult(input, FormatType.ASCII, false, errorMessage = "ASCII numbers decoding failed: ${e.message}")
        }
    }

    fun encodeAscii(input: String): String {
        return input.map { it.code.toString() }.joinToString(" ")
    }

    // --- UNICODE ESCAPES DECODING / ENCODING ---
    fun decodeUnicode(input: String): DecodeResult {
        return try {
            val regex = Regex("""\\u([0-9a-fA-F]{4})""")
            val decoded = regex.replace(input) { match ->
                val codeStr = match.groupValues[1]
                val charVal = codeStr.toInt(16).toChar()
                charVal.toString()
            }
            DecodeResult(decoded, FormatType.UNICODE, true)
        } catch (e: Exception) {
            DecodeResult(input, FormatType.UNICODE, false, errorMessage = "Unicode escape decoding failed: ${e.message}")
        }
    }

    fun encodeUnicode(input: String): String {
        val sb = StringBuilder()
        for (c in input) {
            val code = c.code
            if (code in 32..126) {
                sb.append(c)
            } else {
                sb.append(String.format("\\u%04x", code))
            }
        }
        return sb.toString()
    }

    // --- BASE32 DECODING / ENCODING ---
    fun decodeBase32(input: String): DecodeResult {
        val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
        val clean = input.uppercase(Locale.ROOT).replace("=", "").trim()
        val bits = StringBuilder()
        for (char in clean) {
            val index = alphabet.indexOf(char)
            if (index < 0) return DecodeResult(input, FormatType.BASE32, false, errorMessage = "Invalid Base32 character: $char")
            bits.append(Integer.toBinaryString(index).padStart(5, '0'))
        }
        val bytes = mutableListOf<Byte>()
        for (i in 0 until (bits.length - bits.length % 8) step 8) {
            val byteStr = bits.substring(i, i + 8)
            bytes.add(byteStr.toInt(2).toByte())
        }
        val res = String(bytes.toByteArray(), StandardCharsets.UTF_8)
        return DecodeResult(res, FormatType.BASE32, true)
    }

    fun encodeBase32(input: String): String {
        val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
        val bytes = input.toByteArray(StandardCharsets.UTF_8)
        val bits = StringBuilder()
        for (b in bytes) {
            bits.append(Integer.toBinaryString(b.toInt() and 0xFF).padStart(8, '0'))
        }
        val sb = StringBuilder()
        for (i in bits.indices step 5) {
            val end = (i + 5).coerceAtMost(bits.length)
            var chunk = bits.substring(i, end)
            if (chunk.length < 5) chunk = chunk.padEnd(5, '0')
            val index = chunk.toInt(2)
            sb.append(alphabet[index])
        }
        while (sb.length % 8 != 0) sb.append("=")
        return sb.toString()
    }

    // --- HELPERS FOR FORMAT MATCHING ---
    private fun isJwtFormat(s: String): Boolean {
        val parts = s.split(".")
        return parts.size == 3 && parts[0].length >= 10 && parts[1].length >= 10
    }

    private fun isBinaryFormat(s: String): Boolean {
        val clean = s.replace(Regex("[^01]"), "")
        return clean.length >= 8 && clean.length % 8 == 0 && s.all { it == '0' || it == '1' || it.isWhitespace() }
    }

    private fun isMorseFormat(s: String): Boolean {
        return s.isNotEmpty() && s.all { it == '.' || it == '-' || it == '/' || it.isWhitespace() }
    }

    private fun isHexFormat(s: String): Boolean {
        val clean = s.replace(" ", "").replace("0x", "").replace(":", "")
        return clean.length >= 2 && clean.length % 2 == 0 && clean.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }
    }

    private fun isBase64Format(s: String): Boolean {
        if (s.length < 4) return false
        val regex = Regex("^[A-Za-z0-9+/=_-]+$")
        return regex.matches(s)
    }

    private fun isAsciiFormat(s: String): Boolean {
        val parts = s.trim().split(Regex("[,\\s]+"))
        if (parts.size < 2) return false
        return parts.all { it.toIntOrNull() in 0..255 }
    }

    private fun isUnreadableGarbage(s: String): Boolean {
        if (s.isEmpty()) return true
        var nonPrintableCount = 0
        for (c in s) {
            val code = c.code
            if (code < 9 || (code in 14..31) || code == 127 || code > 65533) {
                nonPrintableCount++
            }
        }
        return (nonPrintableCount.toDouble() / s.length.toDouble()) > 0.15
    }
}
