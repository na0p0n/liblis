package net.naoponju.liblis.common.constraint

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class PagingConstantsTest {
    @Test
    @DisplayName("デフォルトページサイズ_20")
    fun defaultPageSizeIs20() {
        Assertions.assertEquals(20, PagingConstants.DEFAULT_PAGE_SIZE)
    }

    @Test
    @DisplayName("許可ページサイズ_20を含む")
    fun allowedPageSizesContains20() {
        Assertions.assertTrue(PagingConstants.ALLOWED_PAGE_SIZES.contains(20))
    }

    @Test
    @DisplayName("許可ページサイズ_50を含む")
    fun allowedPageSizesContains50() {
        Assertions.assertTrue(PagingConstants.ALLOWED_PAGE_SIZES.contains(50))
    }

    @Test
    @DisplayName("許可ページサイズ_100を含む")
    fun allowedPageSizesContains100() {
        Assertions.assertTrue(PagingConstants.ALLOWED_PAGE_SIZES.contains(100))
    }

    @Test
    @DisplayName("許可ページサイズ_不正なサイズを含まない")
    fun allowedPageSizesDoesNotContainInvalidSize() {
        Assertions.assertFalse(PagingConstants.ALLOWED_PAGE_SIZES.contains(10))
        Assertions.assertFalse(PagingConstants.ALLOWED_PAGE_SIZES.contains(200))
        Assertions.assertFalse(PagingConstants.ALLOWED_PAGE_SIZES.contains(0))
    }

    @Test
    @DisplayName("デフォルトページサイズは許可ページサイズに含まれる")
    fun defaultPageSizeIsInAllowedPageSizes() {
        Assertions.assertTrue(PagingConstants.ALLOWED_PAGE_SIZES.contains(PagingConstants.DEFAULT_PAGE_SIZE))
    }
}