package net.naoponju.liblis.common.config

import org.apache.ibatis.type.BaseTypeHandler
import org.apache.ibatis.type.JdbcType
import org.apache.ibatis.type.MappedTypes
import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.ResultSet

@MappedTypes(List::class)
class StringListTypeHandler : BaseTypeHandler<List<String>>() {
    override fun setNonNullParameter(
        ps: PreparedStatement,
        i: Int,
        parameter: List<String>,
        jdbcType: JdbcType?,
    ) {
        // ListをJava Arrayに変換して、JDBCのConnectionからSQL Arrayを作成
        val array = ps.connection.createArrayOf("text", parameter.toTypedArray())
        ps.setArray(i, array)
    }

    override fun getNullableResult(
        rs: ResultSet,
        columnName: String,
    ): List<String>? {
        return extractList(rs.getArray(columnName))
    }

    override fun getNullableResult(
        rs: ResultSet,
        columnIndex: Int,
    ): List<String>? {
        return extractList(rs.getArray(columnIndex))
    }

    override fun getNullableResult(
        cs: CallableStatement,
        columnIndex: Int,
    ): List<String>? {
        return extractList(cs.getArray(columnIndex))
    }

    private fun extractList(sqlArray: java.sql.Array?): List<String>? {
        return (sqlArray?.array as? Array<*>)?.mapNotNull { it?.toString() }
    }
}
