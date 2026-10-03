package com.aegis.agent.tools

class ExpressionEvaluator {
    fun evaluate(expression: String): Double {
        val p = Parser(expression)
        val value = p.parseExpression()
        p.skip()
        require(p.atEnd()) { "Unexpected input at position ${p.position()}" }
        require(value.isFinite()) { "Result is not finite" }
        return value
    }

    private class Parser(private val s: String) {
        private var i = 0
        fun position() = i
        fun atEnd() = i >= s.length
        fun skip() { while (i < s.length && s[i].isWhitespace()) i++ }

        fun parseExpression(): Double {
            var v = parseTerm()
            while (true) {
                skip()
                v = when {
                    consume('+') -> v + parseTerm()
                    consume('-') -> v - parseTerm()
                    else -> return v
                }
            }
        }

        private fun parseTerm(): Double {
            var v = parseFactor()
            while (true) {
                skip()
                v = when {
                    consume('*') -> v * parseFactor()
                    consume('/') -> v / parseFactor()
                    else -> return v
                }
            }
        }

        private fun parseFactor(): Double {
            skip()
            if (consume('+')) return parseFactor()
            if (consume('-')) return -parseFactor()
            if (consume('(')) {
                val v = parseExpression()
                require(consume(')')) { "Missing ')'" }
                return v
            }
            val start = i
            while (i < s.length && (s[i].isDigit() || s[i] == '.')) i++
            require(i > start) { "Expected number at position $i" }
            return s.substring(start, i).toDouble()
        }

        private fun consume(c: Char): Boolean {
            skip()
            if (i < s.length && s[i] == c) { i++; return true }
            return false
        }
    }
}
