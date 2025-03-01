// Copyright 2023 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.aws.toolkits.jetbrains.services.codewhisperer.service

import software.aws.toolkits.jetbrains.services.codewhisperer.language.CodeWhispererProgrammingLanguage
import software.aws.toolkits.jetbrains.services.codewhisperer.language.languages.CodeWhispererCpp
import software.aws.toolkits.jetbrains.services.codewhisperer.language.languages.CodeWhispererCsharp
import software.aws.toolkits.jetbrains.services.codewhisperer.language.languages.CodeWhispererGo
import software.aws.toolkits.jetbrains.services.codewhisperer.language.languages.CodeWhispererJava
import software.aws.toolkits.jetbrains.services.codewhisperer.language.languages.CodeWhispererJavaScript
import software.aws.toolkits.jetbrains.services.codewhisperer.language.languages.CodeWhispererJsx
import software.aws.toolkits.jetbrains.services.codewhisperer.language.languages.CodeWhispererKotlin
import software.aws.toolkits.jetbrains.services.codewhisperer.language.languages.CodeWhispererPhp
import software.aws.toolkits.jetbrains.services.codewhisperer.language.languages.CodeWhispererPlainText
import software.aws.toolkits.jetbrains.services.codewhisperer.language.languages.CodeWhispererPython
import software.aws.toolkits.jetbrains.services.codewhisperer.language.languages.CodeWhispererRuby
import software.aws.toolkits.jetbrains.services.codewhisperer.language.languages.CodeWhispererRust
import software.aws.toolkits.jetbrains.services.codewhisperer.language.languages.CodeWhispererScala
import software.aws.toolkits.jetbrains.services.codewhisperer.language.languages.CodeWhispererShell
import software.aws.toolkits.jetbrains.services.codewhisperer.language.languages.CodeWhispererSql
import software.aws.toolkits.jetbrains.services.codewhisperer.language.languages.CodeWhispererTsx
import software.aws.toolkits.jetbrains.services.codewhisperer.language.languages.CodeWhispererTypeScript
import software.aws.toolkits.telemetry.CodewhispererAutomatedTriggerType

object CodeWhispererClassifierConstants {
    val osMap: Map<String, Double> = mapOf(
        "Mac OS X" to -0.1552,
        "Windows 10" to -0.0238,
        "Windows" to 0.0412,
        "win32" to -0.0559,
    )

    // these are used for 100% classifier driven auto trigger
    val triggerTypeCoefficientMap: Map<CodewhispererAutomatedTriggerType, Double> = mapOf(
        CodewhispererAutomatedTriggerType.SpecialCharacters to 0.0209,
        CodewhispererAutomatedTriggerType.Enter to 0.2853
    )

    val languageMap: Map<CodeWhispererProgrammingLanguage, Double> = mapOf(
        CodeWhispererPython.INSTANCE to -0.3052,
        CodeWhispererJava.INSTANCE to -0.4622,
        CodeWhispererJavaScript.INSTANCE to -0.4688,
        CodeWhispererCsharp.INSTANCE to -0.3475,
        CodeWhispererPlainText.INSTANCE to 0.0,
        CodeWhispererTypeScript.INSTANCE to -0.6084,
        CodeWhispererTsx.INSTANCE to -0.6084,
        CodeWhispererJsx.INSTANCE to -0.4688,
        CodeWhispererShell.INSTANCE to -0.4718,
        CodeWhispererRuby.INSTANCE to -0.7356,
        CodeWhispererSql.INSTANCE to -0.4937,
        CodeWhispererRust.INSTANCE to -0.4309,
        CodeWhispererKotlin.INSTANCE to -0.4739,
        CodeWhispererPhp.INSTANCE to -0.3917,
        CodeWhispererGo.INSTANCE to -0.3504,
        CodeWhispererScala.INSTANCE to -0.534,
        CodeWhispererCpp.INSTANCE to -0.1734
    )

    // other metadata coefficient
    const val lineNumCoefficient = -0.0416

    // length of the current line of left_context
    const val lengthOfLeftCurrentCoefficient = -1.1747

    // length of the previous line of left context
    const val lengthOfLeftPrevCoefficient = 0.4033

    // lenght of right_context
    const val lengthofRightCoefficient = -0.3321

    const val prevDecisionAcceptCoefficient = 0.5397

    const val prevDecisionRejectCoefficient = -0.1656

    const val prevDecisionOtherCoefficient = 0.0

    // intercept of logistic regression classifier
    const val intercept = 0.3738713

    // length of left context
    const val lengthLeft0To5 = -0.8756
    const val lengthLeft5To10 = -0.5463
    const val lengthLeft10To20 = -0.4081
    const val lengthLeft20To30 = -0.3272
    const val lengthLeft30To40 = -0.2442
    const val lengthLeft40To50 = -0.1471

    val coefficientsMap = mapOf<String, Double>(
        "throw" to 1.5868,
        ";" to -1.268,
        "any" to -1.1565,
        "7" to -1.1347,
        "false" to -1.1307,
        "nil" to -1.0653,
        "elif" to 1.0122,
        "9" to -1.0098,
        "pass" to -1.0058,
        "True" to -1.0002,
        "False" to -0.9434,
        "6" to -0.9222,
        "true" to -0.9142,
        "None" to -0.9027,
        "8" to -0.9013,
        "break" to -0.8475,
        "}" to -0.847,
        "5" to -0.8414,
        "4" to -0.8197,
        "1" to -0.8085,
        "\\" to -0.8019,
        "static" to -0.7748,
        "0" to -0.77,
        "end" to -0.7617,
        "(" to 0.7239,
        "/" to -0.7104,
        "where" to -0.6981,
        "readonly" to -0.6741,
        "async" to -0.6723,
        "3" to -0.654,
        "continue" to -0.6413,
        "struct" to -0.64,
        "try" to -0.6369,
        "float" to -0.6341,
        "using" to 0.6079,
        "@" to 0.6016,
        "|" to 0.5993,
        "impl" to 0.5808,
        "private" to -0.5746,
        "for" to 0.5741,
        "2" to -0.5634,
        "let" to -0.5187,
        "foreach" to 0.5186,
        "select" to -0.5148,
        "export" to -0.5,
        "mut" to -0.4921,
        ")" to -0.463,
        "]" to -0.4611,
        "when" to 0.4602,
        "virtual" to -0.4583,
        "extern" to -0.4465,
        "catch" to 0.4446,
        "new" to 0.4394,
        "val" to -0.4339,
        "map" to 0.4284,
        "case" to 0.4271,
        "throws" to 0.4221,
        "null" to -0.4197,
        "protected" to -0.4133,
        "q" to 0.4125,
        "except" to 0.4115,
        ": " to 0.4072,
        "^" to -0.407,
        " " to 0.4066,
        "$" to 0.3981,
        "this" to 0.3962,
        "switch" to 0.3947,
        "*" to -0.3931,
        "module" to 0.3912,
        "array" to 0.385,
        "=" to 0.3828,
        "p" to 0.3728,
        "ON" to 0.3708,
        "`" to 0.3693,
        "u" to 0.3658,
        "a" to 0.3654,
        "require" to 0.3646,
        ">" to -0.3644,
        "const" to -0.3476,
        "o" to 0.3423,
        "sizeof" to 0.3416,
        "object" to 0.3362,
        "w" to 0.3345,
        "print" to 0.3344,
        "range" to 0.3336,
        "if" to 0.3324,
        "abstract" to -0.3293,
        "var" to -0.3239,
        "i" to 0.321,
        "while" to 0.3138,
        "J" to 0.3137,
        "c" to 0.3118,
        "await" to -0.3072,
        "from" to 0.3057,
        "f" to 0.302,
        "echo" to 0.2995,
        "#" to 0.2984,
        "e" to 0.2962,
        "r" to 0.2925,
        "mod" to 0.2893,
        "loop" to 0.2874,
        "t" to 0.2832,
        "~" to 0.282,
        "final" to -0.2816,
        "del" to 0.2785,
        "override" to -0.2746,
        "ref" to -0.2737,
        "h" to 0.2693,
        "m" to 0.2681,
        "{" to 0.2674,
        "implements" to 0.2672,
        "inline" to -0.2642,
        "match" to 0.2613,
        "with" to -0.261,
        "x" to 0.2597,
        "namespace" to -0.2596,
        "operator" to 0.2573,
        "double" to -0.2563,
        "source" to -0.2482,
        "import" to -0.2419,
        "NULL" to -0.2399,
        "l" to 0.239,
        "or" to 0.2378,
        "s" to 0.2366,
        "then" to 0.2354,
        "W" to 0.2354,
        "y" to 0.2333,
        "local" to 0.2288,
        "is" to 0.2282,
        "n" to 0.2254,
        "+" to -0.2251,
        "G" to 0.223,
        "public" to -0.2229,
        "WHERE" to 0.2224,
        "list" to 0.2204,
        "Q" to 0.2204,
        "[" to 0.2136,
        "VALUES" to 0.2134,
        "H" to 0.2105,
        "g" to 0.2094,
        "else" to -0.208,
        "bool" to -0.2066,
        "long" to -0.2059,
        "R" to 0.2025,
        "S" to 0.2021,
        "d" to 0.2003,
        "V" to 0.1974,
        "K" to -0.1961,
        "<" to 0.1958,
        "debugger" to -0.1929,
        "NOT" to -0.1911,
        "b" to 0.1907,
        "boolean" to -0.1891,
        "z" to -0.1866,
        "LIKE" to -0.1793,
        "raise" to 0.1782,
        "L" to 0.1768,
        "fn" to 0.176,
        "delete" to 0.1714,
        "unsigned" to -0.1675,
        "auto" to -0.1648,
        "finally" to 0.1616,
        "k" to 0.1599,
        "as" to 0.156,
        "instanceof" to 0.1558,
        "&" to 0.1554,
        "E" to 0.1551,
        "M" to 0.1542,
        "I" to 0.1503,
        "Y" to 0.1493,
        "typeof" to 0.1475,
        "j" to 0.1445,
        "INTO" to 0.1442,
        "IF" to 0.1437,
        "next" to 0.1433,
        "undef" to -0.1427,
        "THEN" to -0.1416,
        "v" to 0.1415,
        "C" to 0.1383,
        "P" to 0.1353,
        "AND" to -0.1345,
        "constructor" to 0.1337,
        "void" to -0.1336,
        "class" to -0.1328,
        "defer" to 0.1316,
        "begin" to 0.1306,
        "FROM" to -0.1304,
        "SET" to 0.1291,
        "decimal" to -0.1278,
        "friend" to 0.1277,
        "SELECT" to -0.1265,
        "event" to 0.1259,
        "lambda" to 0.1253,
        "enum" to 0.1215,
        "A" to 0.121,
        "lock" to 0.1187,
        "ensure" to 0.1184,
        "%" to 0.1177,
        "isset" to 0.1175,
        "O" to 0.1174,
        "." to 0.1146,
        "UNION" to -0.1145,
        "alias" to -0.1129,
        "template" to -0.1102,
        "WHEN" to 0.1093,
        "rescue" to 0.1083,
        "DISTINCT" to -0.1074,
        "trait" to -0.1073,
        "D" to 0.1062,
        "in" to 0.1045,
        "internal" to -0.1029,
        "," to 0.1027,
        "static_cast" to 0.1016,
        "do" to -0.1005,
        "OR" to 0.1003,
        "AS" to -0.1001,
        "interface" to 0.0996,
        "super" to 0.0989,
        "B" to 0.0963,
        "U" to 0.0962,
        "T" to 0.0943,
        "CALL" to -0.0918,
        "BETWEEN" to -0.0915,
        "N" to 0.0897,
        "yield" to 0.0867,
        "done" to -0.0857,
        "string" to -0.0837,
        "out" to -0.0831,
        "volatile" to -0.0819,
        "retry" to 0.0816,
        "?" to -0.0796,
        "number" to -0.0791,
        "short" to 0.0787,
        "sealed" to -0.0776,
        "package" to 0.0765,
        "OPEN" to -0.0756,
        "base" to 0.0735,
        "and" to 0.0729,
        "exit" to 0.0726,
        "_" to 0.0721,
        "keyof" to -0.072,
        "def" to 0.0713,
        "crate" to -0.0706,
        "-" to -0.07,
        "FUNCTION" to 0.0692,
        "declare" to -0.0678,
        "include" to 0.0671,
        "COUNT" to -0.0669,
        "INDEX" to -0.0666,
        "CLOSE" to -0.0651,
        "fi" to -0.0644,
        "uint" to 0.0624,
        "params" to 0.0575,
        "HAVING" to 0.0575,
        "byte" to -0.0575,
        "clone" to -0.0552,
        "char" to -0.054,
        "func" to 0.0538,
        "never" to -0.053,
        "unset" to -0.0524,
        "unless" to -0.051,
        "esac" to -0.0509,
        "shift" to -0.0507,
        "require_once" to 0.0486,
        "ELSE" to -0.0477,
        "extends" to 0.0461,
        "elseif" to 0.0452,
        "mutable" to -0.0451,
        "asm" to 0.0449,
        "!" to 0.0446,
        "LIMIT" to 0.0444,
        "ushort" to -0.0438,
        "\"" to -0.0433,
        "Z" to 0.0431,
        "exec" to -0.0431,
        "IS" to -0.0429,
        "DECLARE" to -0.0425,
        "__LINE__" to -0.0424,
        "BEGIN" to -0.0418,
        "typedef" to 0.0414,
        "EXIT" to -0.0412,
        "'" to 0.041,
        "function" to -0.0393,
        "dyn" to -0.039,
        "wchar_t" to -0.0388,
        "unique" to -0.0383,
        "include_once" to 0.0367,
        "stackalloc" to 0.0359,
        "RETURN" to -0.0356,
        "const_cast" to 0.035,
        "MAX" to 0.0341,
        "assert" to -0.0331,
        "JOIN" to -0.0328,
        "use" to 0.0318,
        "GET" to 0.0317,
        "VIEW" to 0.0314,
        "move" to 0.0308,
        "typename" to 0.0308,
        "die" to 0.0305,
        "asserts" to -0.0304,
        "reinterpret_cast" to -0.0302,
        "USING" to -0.0289,
        "elsif" to -0.0285,
        "FIRST" to -0.028,
        "self" to -0.0278,
        "RETURNING" to -0.0278,
        "symbol" to -0.0273,
        "OFFSET" to 0.0263,
        "bigint" to 0.0253,
        "register" to -0.0237,
        "union" to -0.0227,
        "return" to -0.0227,
        "until" to -0.0224,
        "endfor" to -0.0213,
        "implicit" to -0.021,
        "LOOP" to 0.0195,
        "pub" to 0.0182,
        "global" to 0.0179,
        "EXCEPTION" to 0.0175,
        "delegate" to 0.0173,
        "signed" to -0.0163,
        "FOR" to 0.0156,
        "unsafe" to 0.014,
        "NEXT" to -0.0133,
        "IN" to 0.0129,
        "MIN" to -0.0123,
        "go" to -0.0112,
        "type" to -0.0109,
        "explicit" to -0.0107,
        "eval" to -0.0104,
        "int" to -0.0099,
        "CASE" to -0.0096,
        "END" to 0.0084,
        "UPDATE" to 0.0074,
        "default" to 0.0072,
        "chan" to 0.0068,
        "fixed" to 0.0066,
        "not" to -0.0052,
        "X" to -0.0047,
        "endforeach" to 0.0031,
        "goto" to 0.0028,
        "empty" to 0.0022,
        "checked" to 0.0012,
        "F" to -0.001
    )
}
