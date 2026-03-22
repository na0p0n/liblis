package net.naoponju.liblis.common.constraint

// 定数のためマジックナンバー許容
@Suppress("MagicNumber")
object PagingConstants {
    const val DEFAULT_PAGE_SIZE = 20

    val ALLOWED_PAGE_SIZES = setOf(20, 50, 100)
}
