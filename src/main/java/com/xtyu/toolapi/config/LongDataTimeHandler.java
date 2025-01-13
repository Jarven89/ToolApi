//package com.xtyu.toolapi.config;
//
//import com.xtyu.toolapi.utils.GDateUtil;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.ibatis.type.BaseTypeHandler;
//import org.apache.ibatis.type.JdbcType;
//import org.apache.ibatis.type.MappedTypes;
//
//import java.sql.CallableStatement;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.time.LocalDateTime;
//
///**
// * @author dylan
// */
//@MappedTypes(LocalDateTime.class)
//@Slf4j
//public class LongDataTimeHandler extends BaseTypeHandler<LocalDateTime> {
//    @Override
//
//    public void setNonNullParameter(PreparedStatement preparedStatement, int i, LocalDateTime localDateTime, JdbcType jdbcType) throws SQLException {
//
//        preparedStatement.setLong(i, GDateUtil.toSeconds(localDateTime));
//    }
//
//    @Override
//    public LocalDateTime getNullableResult(ResultSet resultSet, String s) throws SQLException {
//
//        long milliseconds = resultSet.getLong(s);
//        if (milliseconds == 0) {
//            return null;
//        }
//        return GDateUtil.fromSeconds(milliseconds);
//    }
//
//    @Override
//    public LocalDateTime getNullableResult(ResultSet resultSet, int i) throws SQLException {
//        return null;
//    }
//
//    @Override
//    public LocalDateTime getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
//        return null;
//    }
//}
