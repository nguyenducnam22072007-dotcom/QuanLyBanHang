package qlbh;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.HexFormat;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

/** Màn hình đăng nhập. Mật khẩu được mã hóa SHA-256 trước khi so sánh với bảng TaiKhoan. */
public class LoginFrame extends JFrame {
    private final JTextField txtUser = new JTextField(18);
    private final JPasswordField txtPass = new JPasswordField(18);

    public LoginFrame() {
        super("Đăng nhập");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JLabel title = new JLabel("ĐĂNG NHẬP HỆ THỐNG", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        title.setBorder(BorderFactory.createEmptyBorder(16, 0, 8, 0));

        JPanel form = new JPanel(new GridLayout(2, 2, 10, 12));
        form.setBorder(BorderFactory.createEmptyBorder(8, 30, 8, 30));
        form.add(new JLabel("Tên đăng nhập:"));
        form.add(txtUser);
        form.add(new JLabel("Mật khẩu:"));
        form.add(txtPass);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        btns.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        btns.add(Ui.button("Đăng nhập", e -> login()));
        btns.add(Ui.button("Thoát", e -> System.exit(0)));

        add(title, BorderLayout.NORTH);
        add(form, BorderLayout.CENTER);
        add(btns, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(
                (javax.swing.JButton) btns.getComponent(0)); // nhấn Enter = Đăng nhập

        pack();
        setSize(Math.max(getWidth(), 420), getHeight());
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private void login() {
        String user = txtUser.getText().trim();
        String pass = new String(txtPass.getPassword());
        if (user.isEmpty() || pass.isEmpty()) {
            Ui.info(this, "Vui lòng nhập tên đăng nhập và mật khẩu.");
            return;
        }
        try {
            DefaultTableModel m = Db.query(
                    "SELECT HoTen FROM TaiKhoan WHERE TenDangNhap = ? AND MatKhau = ?", user, sha256(pass));
            if (m.getRowCount() == 0) {
                Ui.info(this, "Sai tên đăng nhập hoặc mật khẩu.");
                txtPass.setText("");
                txtPass.requestFocus();
                return;
            }
            String name = Ui.str(m.getValueAt(0, 0));
            dispose();
            new MainFrame(name.isEmpty() ? user : name).setVisible(true);
        } catch (SQLException ex) {
            Ui.error(this, ex);
        }
    }

    /** Mã hóa SHA-256, trả về chuỗi hex 64 ký tự viết thường. */
    static String sha256(String s) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(d);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
