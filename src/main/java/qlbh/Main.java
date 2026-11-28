package qlbh;

import java.awt.Font;
import java.util.Collections;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // dùng giao diện mặc định
        }
        FontUIResource font = new FontUIResource("Segoe UI", Font.PLAIN, 14);
        for (Object key : Collections.list(UIManager.getDefaults().keys())) {
            if (UIManager.get(key) instanceof FontUIResource) {
                UIManager.put(key, font);
            }
        }

        Theme.install();

        SwingUtilities.invokeLater(() -> {
            try {
                Db.getConnection().close();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "Không kết nối được SQL Server:\n" + e.getMessage()
                        + "\n\nKiểm tra lại URL / USER / PASSWORD trong file Db.java,"
                        + "\nSQL Server đã bật TCP/IP (cổng 1433) và đã chạy file QuanLyBanHang.sql chưa.",
                        "Lỗi kết nối", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
            new LoginFrame().setVisible(true);
        });
    }
}
