package net.naoponju.liblis.config

import org.apache.ibatis.type.BaseTypeHandler
import org.apache.ibatis.type.JdbcType
import org.apache.ibatis.type.MappedTypes
import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.*

@MappedTypes(UUID::class)
class UUIDTypeHandler: BaseTypeHandler<UUID>() {
    override fun setNonNullParameter(ps: PreparedStatement, i: Int, parameter: UUID, jdbcType: JdbcType?) {
        ps.setObject(i, parameter)
    }

    override fun getNullableResult(rs: ResultSet, columnIndex: Int): UUID? {
        return rs.getObject(columnIndex) as? UUID
    }

    override fun getNullableResult(rs: ResultSet, columnName: String): UUID? {
        return rs.getObject(columnName) as? UUID
    }

    override fun getNullableResult(cs: CallableStatement, columnIndex: Int): UUID? {
        return cs.getObject(columnIndex) as? UUID
    }
}
