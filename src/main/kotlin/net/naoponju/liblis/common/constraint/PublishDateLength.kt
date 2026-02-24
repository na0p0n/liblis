package net.naoponju.liblis.common.constraint

// 出版日データの形式判別用定数オブジェクト
@Suppress("MatchingDeclarationName")
object PublishDateLength {
    const val PUBLISH_DATE_LENGTH_FULL = 10
    const val PUBLISH_DATE_LENGTH_NON_DAY = 7
    const val PUBLISH_DATE_LENGTH_NON_MONTH_DAY = 4
}
