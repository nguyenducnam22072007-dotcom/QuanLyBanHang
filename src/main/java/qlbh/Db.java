package qlbh;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import javax.swing.table.DefaultTableModel;

/** Kết nối và thao tác với SQL Server. SỬA USER / PASSWORD cho đúng với máy của bạn. */
public final class Db {
    private Db() {}

    public static final String URL =
            "jdbc:sqlserver://localhost:1433;databaseName=QuanLyBanHang;encrypt=true;trustServerCertificate=true;";
    public static final String USER = "sa";
    public static final String PASSWORD = "123456";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private static void bind(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }

    /** Chạy SELECT và trả về TableModel (không cho sửa trực tiếp trên bảng). */
    public static DefaultTableModel query(String sql, Object... params) throws SQLException {
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData md = rs.getMetaData();
                int n = md.getColumnCount();
                DefaultTableModel model = new DefaultTableModel() {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };
                for (int i = 1; i <= n; i++) {
                    model.addColumn(md.getColumnLabel(i));
                }
                while (rs.next()) {
                    Object[] row = new Object[n];
                    for (int i = 0; i < n; i++) {
                        row[i] = rs.getObject(i + 1);
                    }
                    model.addRow(row);
                }
                return model;
            }
        }
    }

    /** Chạy INSERT / UPDATE / DELETE, trả về số dòng bị ảnh hưởng. */
    public static int update(String sql, Object... params) throws SQLException {
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            return ps.executeUpdate();
        }
    }
}
