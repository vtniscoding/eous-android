package com.eous.mentor.features.chat

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

/**
 * A helper function to replace LaTeX math syntax with clean Unicode math symbols.
 */
fun formatMathEquations(input: String): String {
    var text = input

    // Replace LaTeX symbols with Unicode equivalents
    val replacements = mapOf(
        "\\pm" to "±",
        "\\times" to "×",
        "\\div" to "÷",
        "\\neq" to "≠",
        "\\ne" to "≠",
        "\\leq" to "≤",
        "\\le" to "≤",
        "\\geq" to "≥",
        "\\ge" to "≥",
        "\\alpha" to "α",
        "\\beta" to "β",
        "\\gamma" to "γ",
        "\\delta" to "δ",
        "\\Delta" to "Δ",
        "\\theta" to "θ",
        "\\pi" to "π",
        "\\lambda" to "λ",
        "\\infty" to "∞",
        "\\sqrt" to "√",
        "\\approx" to "≈",
        "\\cdot" to "·",
        "\\sum" to "∑",
        "\\int" to "∫",
        "\\partial" to "∂",
        "\\nabla" to "∇"
    )

    for ((latex, unicode) in replacements) {
        text = text.replace(latex, unicode)
    }

    // Replace exponents: ^2 -> ², ^3 -> ³, etc.
    text = text.replace("^2", "²")
        .replace("^3", "³")
        .replace("^1", "¹")
        .replace("^0", "⁰")
        .replace("^n", "ⁿ")
        .replace("^x", "ˣ")
        .replace("^{+}", "⁺")
        .replace("^{-}", "⁻")

    // Replace subscripts: _0 -> ₀, _1 -> ₁, etc.
    text = text.replace("_0", "₀")
        .replace("_1", "₁")
        .replace("_2", "₂")
        .replace("_3", "₃")
        .replace("_x", "ₓ")
        .replace("_n", "ₙ")
        .replace("_i", "ᵢ")

    // Handle \frac{a}{b} -> (a)/(b)
    val fracRegex = Regex("\\\\frac\\{([^}]+)\\}\\{([^}]+)\\}")
    text = fracRegex.replace(text) { matchResult ->
        val numerator = matchResult.groupValues[1]
        val denominator = matchResult.groupValues[2]
        "($numerator)/($denominator)"
    }

    // Clean up LaTeX delimiters: \(, \), \[, \], $$, $
    text = text.replace("\\(", "")
        .replace("\\)", "")
        .replace("\\[", "")
        .replace("\\]", "")
        .replace("$$", "")
        .replace("$", "")

    return text
}

/**
 * A simple markdown parser that converts **bold**, *italic*, and `code`
 * markup into Styled AnnotatedString while also formatting math symbols.
 */
fun parseMarkdownAndMath(text: String): AnnotatedString {
    val formattedText = formatMathEquations(text)

    return buildAnnotatedString {
        var currentIndex = 0

        // Simple regex pattern to match bold (**text**), italic (*text*), and code (`text`)
        val pattern = Regex("(\\*\\*.*?\\*\\*|\\*.*?\\*|`.*?`)", RegexOption.MULTILINE)
        val matches = pattern.findAll(formattedText).toList()

        for (match in matches) {
            val start = match.range.first
            val end = match.range.last + 1
            val value = match.value

            // Append plain text before the match
            if (start > currentIndex) {
                append(formattedText.substring(currentIndex, start))
            }

            // Style and append the matched text
            when {
                value.startsWith("**") && value.endsWith("**") -> {
                    val cleanText = value.removeSurrounding("**")
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    append(cleanText)
                    pop()
                }
                value.startsWith("*") && value.endsWith("*") -> {
                    val cleanText = value.removeSurrounding("*")
                    pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                    append(cleanText)
                    pop()
                }
                value.startsWith("`") && value.endsWith("`") -> {
                    val cleanText = value.removeSurrounding("`")
                    pushStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = Color(0xFF2C2C2C),
                            color = Color(0xFFE2E2E2)
                        )
                    )
                    append(cleanText)
                    pop()
                }
            }

            currentIndex = end
        }

        // Append any remaining text
        if (currentIndex < formattedText.length) {
            append(formattedText.substring(currentIndex))
        }
    }
}
