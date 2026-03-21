package net.naoponju.liblis.common.constraint

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class ChangePasswordResultTest {
    @Test
    @DisplayName("ChangePasswordResult_列挙値_SUCCESSが存在する")
    fun successEnumValueExists() {
        Assertions.assertDoesNotThrow {
            ChangePasswordResult.SUCCESS
        }
    }

    @Test
    @DisplayName("ChangePasswordResult_列挙値_WRONG_CURRENTが存在する")
    fun wrongCurrentEnumValueExists() {
        Assertions.assertDoesNotThrow {
            ChangePasswordResult.WRONG_CURRENT
        }
    }

    @Test
    @DisplayName("ChangePasswordResult_列挙値_VALIDATION_ERRORが存在する")
    fun validationErrorEnumValueExists() {
        Assertions.assertDoesNotThrow {
            ChangePasswordResult.VALIDATION_ERROR
        }
    }

    @Test
    @DisplayName("ChangePasswordResult_列挙値_NOT_SUPPORTEDが存在する")
    fun notSupportedEnumValueExists() {
        Assertions.assertDoesNotThrow {
            ChangePasswordResult.NOT_SUPPORTED
        }
    }

    @Test
    @DisplayName("ChangePasswordResult_列挙値は4種類のみ")
    fun enumHasExactlyFourValues() {
        Assertions.assertEquals(4, ChangePasswordResult.entries.size)
    }

    @Test
    @DisplayName("ChangePasswordResult_各列挙値は異なる")
    fun allEnumValuesAreDistinct() {
        val values = ChangePasswordResult.entries
        Assertions.assertEquals(values.size, values.toSet().size)
    }

    @Test
    @DisplayName("ChangePasswordResult_文字列からSUCCESSを取得できる")
    fun valueOfSuccessFromString() {
        val result = ChangePasswordResult.valueOf("SUCCESS")
        Assertions.assertEquals(ChangePasswordResult.SUCCESS, result)
    }

    @Test
    @DisplayName("ChangePasswordResult_文字列からWRONG_CURRENTを取得できる")
    fun valueOfWrongCurrentFromString() {
        val result = ChangePasswordResult.valueOf("WRONG_CURRENT")
        Assertions.assertEquals(ChangePasswordResult.WRONG_CURRENT, result)
    }

    @Test
    @DisplayName("ChangePasswordResult_文字列からVALIDATION_ERRORを取得できる")
    fun valueOfValidationErrorFromString() {
        val result = ChangePasswordResult.valueOf("VALIDATION_ERROR")
        Assertions.assertEquals(ChangePasswordResult.VALIDATION_ERROR, result)
    }

    @Test
    @DisplayName("ChangePasswordResult_文字列からNOT_SUPPORTEDを取得できる")
    fun valueOfNotSupportedFromString() {
        val result = ChangePasswordResult.valueOf("NOT_SUPPORTED")
        Assertions.assertEquals(ChangePasswordResult.NOT_SUPPORTED, result)
    }

    @Test
    @DisplayName("ChangePasswordResult_when式で全列挙値を網羅できる")
    fun exhaustiveWhenExpression() {
        val allResults = ChangePasswordResult.entries
        allResults.forEach { result ->
            val handled =
                when (result) {
                    ChangePasswordResult.SUCCESS -> true
                    ChangePasswordResult.WRONG_CURRENT -> true
                    ChangePasswordResult.VALIDATION_ERROR -> true
                    ChangePasswordResult.NOT_SUPPORTED -> true
                }
            Assertions.assertTrue(handled)
        }
    }
}
