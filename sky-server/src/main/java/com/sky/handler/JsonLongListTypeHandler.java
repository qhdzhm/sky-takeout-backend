package com.sky.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON Long列表类型处理器
 * 专门处理Long类型的列表，解决Integer到Long的转换问题
 */
public class JsonLongListTypeHandler extends BaseTypeHandler<List<Long>> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<Long> parameter, JdbcType jdbcType) throws SQLException {
        try {
            ps.setString(i, objectMapper.writeValueAsString(parameter));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON序列化失败", e);
        }
    }

    @Override
    public List<Long> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String jsonString = rs.getString(columnName);
        return parseJson(jsonString);
    }

    @Override
    public List<Long> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String jsonString = rs.getString(columnIndex);
        return parseJson(jsonString);
    }

    @Override
    public List<Long> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String jsonString = cs.getString(columnIndex);
        return parseJson(jsonString);
    }

    private List<Long> parseJson(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return null;
        }
        try {
            // 先解析为Object列表，然后转换为Long列表
            List<Object> objectList = objectMapper.readValue(jsonString, new TypeReference<List<Object>>() {});
            List<Long> longList = new ArrayList<>();
            
            for (Object obj : objectList) {
                if (obj != null) {
                    if (obj instanceof Number) {
                        longList.add(((Number) obj).longValue());
                    } else if (obj instanceof String) {
                        longList.add(Long.parseLong((String) obj));
                    } else {
                        longList.add(Long.valueOf(obj.toString()));
                    }
                } else {
                    longList.add(null);
                }
            }
            
            return longList;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON反序列化失败: " + jsonString, e);
        } catch (NumberFormatException e) {
            throw new RuntimeException("数字格式转换失败: " + jsonString, e);
        }
    }
} 