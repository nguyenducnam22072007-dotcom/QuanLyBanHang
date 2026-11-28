package qlbh;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.HexFormat;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

/** Màn hình đăng nhập. Mật khẩu được mã hóa SHA-256 trước khi so sánh với bảng TaiKhoan. */
public class LoginFrame extends JFrame {
    private final JTextField txtUser = new JTextField(20);
    private final JPasswordField txtPass = new JPasswordField(20);

    public LoginFrame() {
        super("Đăng nhập");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Banner
        JLabel title = new JLabel("QUẢN LÝ BÁN HÀNG", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 26f));
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Đăng nhập để tiếp tục", SwingConstants.CENTER);
        sub.setFont(sub.getFont().deriveFont(Font.PLAIN, 15f));
        sub.setForeground(new Color(0xDCEBFA));
        JPanel header = new Theme.GradientPanel(Theme.PRIMARY_DARK, Theme.PRIMARY_LIGHT);
        header.setLayout(new BorderLayout(0, 4));
        header.setBorder(BorderFactory.createEmptyBorder(26, 20, 24, 20));
        header.add(title, BorderLayout.CENTER);
        header.add(sub, BorderLayout.SOUTH);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(24, 36, 8, 36));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 6, 8, 6);
        g.anchor = GridBagConstraints.WEST;
        g.gridx = 0;
        g.gridy = 0;
        form.add(new JLabel("Tên đăng nhập:"), g);
        g.gridx = 1;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        form.add(txtUser, g);
        g.gridx = 0;
        g.gridy = 1;
        g.weightx = 0;
        g.fill = GridBagConstraints.NONE;
        form.add(new JLabel("Mật khẩu:"), g);
        g.gridx = 1;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        form.add(txtPass, g);

        // Nút
        JButton btnLogin = Theme.button("Đăng nhập", Theme.BLUE, e -> login());
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 10));
        btns.setBackground(Color.WHITE);
        btns.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));
        btns.add(btnLogin);
        btns.add(Theme.button("Thoát", Theme.GRAY, e -> System.exit(0)));
        getRootPane().setDefaultButton(btnLogin); // nhấn Enter = Đăng nhập

        add(header, BorderLayout.NORTH);
        add(form, BorderLayout.CENTER);
        add(btns, BorderLayout.SOUTH);
        setMinimumSize(new Dimension(480, 0));
        pack();
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
