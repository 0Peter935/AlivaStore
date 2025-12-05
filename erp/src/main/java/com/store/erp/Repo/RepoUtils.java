package com.store.erp.Repo;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public class RepoUtils {

    public static boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int columns = rsmd.getColumnCount();
        for (int i = 1; i <= columns; i++) {
            if (columnName.equalsIgnoreCase(rsmd.getColumnName(i))) {
                return true;
            }
        }
        return false;
    }

    public static boolean safeBool(ResultSet rs, String col) throws SQLException {
        if (!hasColumn(rs, col)) return false;
        Object val = rs.getObject(col);
        if (val == null) return false;
        if (val instanceof Boolean) return (Boolean) val;
        if (val instanceof Number) return ((Number) val).intValue() == 1;
        return "true".equalsIgnoreCase(val.toString()) || "1".equals(val.toString());
    }

    public static int safeInt(ResultSet rs, String col) throws SQLException {
        if (!hasColumn(rs, col)) return 0;
        Object val = rs.getObject(col);
        if (val == null) return 0;
        if (val instanceof Number) return ((Number) val).intValue();
        try { return Integer.parseInt(val.toString()); }
        catch (Exception e) { return 0; }
    }

    public static long safeLong(ResultSet rs, String col) throws SQLException {
        if (!hasColumn(rs, col)) return 0L;
        Object val = rs.getObject(col);
        if (val == null) return 0L;
        if (val instanceof Number) return ((Number) val).longValue();
        try { return Long.parseLong(val.toString()); }
        catch (Exception e) { return 0L; }
    }

    public static double safeDouble(ResultSet rs, String col) throws SQLException {
        if (!hasColumn(rs, col)) return 0.0;
        Object val = rs.getObject(col);
        if (val == null) return 0.0;
        if (val instanceof Number) return ((Number) val).doubleValue();
        try { return Double.parseDouble(val.toString()); }
        catch (Exception e) { return 0.0; }
    }

    public static String safeString(ResultSet rs, String col) throws SQLException {
        if (!hasColumn(rs, col)) return null;
        Object val = rs.getObject(col);
        return val != null ? val.toString() : null;
    }

    public static LocalDate safeDate(ResultSet rs, String col) throws SQLException {
        if (!hasColumn(rs, col)) return null;
        java.sql.Date date = rs.getDate(col);
        return date != null ? date.toLocalDate() : null;
    }

    public static LocalDateTime safeLocalDateTime(ResultSet rs, String col) throws SQLException {
        if (!hasColumn(rs, col)) return null;
        java.sql.Timestamp timestamp = rs.getTimestamp(col);
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }

    public static OffsetDateTime safeOffsetDateTime(ResultSet rs, String col) throws SQLException {
        if (!hasColumn(rs, col)) return null;
        java.sql.Timestamp timestamp = rs.getTimestamp(col);
        return timestamp != null ? timestamp.toInstant().atOffset(java.time.ZoneOffset.UTC) : null;
    }
}

