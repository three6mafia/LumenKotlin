package com.falcofemoralis.lumen.data.api

import android.util.Base64
import java.util.regex.Pattern

object StreamDecoder {
    private const val TRASH_SEPARATOR = "//_//"
    private val TRASH_SYMBOLS = listOf("@", "#", "!", "^", "$")
    private val CLEAR_BEFORE_SYMBOLS = listOf('/', '=')

    private val trashPattern: Pattern by lazy {
        val combinations = mutableListOf<String>()
        combinations.addAll(cartesianProduct(TRASH_SYMBOLS, 2))
        combinations.addAll(cartesianProduct(TRASH_SYMBOLS, 3))

        val encoded = combinations.map { trash ->
            Base64.encodeToString(trash.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        }
        Pattern.compile(encoded.joinToString("|"))
    }

    fun decrypt(encrypted: String): String {
        if (!encrypted.startsWith("#")) return encrypted

        val cache = mutableMapOf<String, String>()

        fun decryptRecursive(input: String): String {
            cache[input]?.let { return it }

            val indexes = mutableListOf<Int>()
            var idx = 0
            while (true) {
                val found = input.indexOf(TRASH_SEPARATOR, idx)
                if (found == -1) break
                indexes.add(found)
                idx = found + 1
            }

            if (indexes.isEmpty()) {
                cache[input] = input
                return input
            }

            var shortest: String? = null

            for (index in indexes) {
                val remaining = input.substring(index + TRASH_SEPARATOR.length)
                val splitIdx = remaining.indexOfFirst { it in CLEAR_BEFORE_SYMBOLS }
                val (before, after) = if (splitIdx != -1) {
                    Pair(remaining.substring(0, splitIdx + 1), remaining.substring(splitIdx + 1))
                } else {
                    Pair(remaining, "")
                }

                val cleanedBefore = trashPattern.matcher(before).replaceAll("")
                val candidate = input.substring(0, index) + cleanedBefore + after
                val decryptedCandidate = decryptRecursive(candidate)

                if (shortest == null || decryptedCandidate.length < shortest.length) {
                    shortest = decryptedCandidate
                }
            }

            val result = shortest ?: input
            cache[input] = result
            return result
        }

        return try {
            val contentToDecrypt = encrypted.substring(2)
            val decryptedString = decryptRecursive(contentToDecrypt)
            val decodedBytes = Base64.decode(decryptedString, Base64.DEFAULT)
            String(decodedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            encrypted
        }
    }

    private fun cartesianProduct(symbols: List<String>, n: Int): List<String> {
        if (n <= 1) return symbols
        val sub = cartesianProduct(symbols, n - 1)
        return sub.flatMap { prefix ->
            symbols.map { symbol -> prefix + symbol }
        }
    }
}
