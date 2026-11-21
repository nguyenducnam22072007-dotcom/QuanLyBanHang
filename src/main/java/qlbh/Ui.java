package qlbh;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/** Các hàm tiện ích dùng chung cho giao diện. */
public final class Ui {
    private Ui() {}

    private static final DecimalFormat MONEY = new DecimalFormat("#,##0");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static String money(Object v) {
        return v == null ? "0" : MONEY.format(v);
    }

    public static String str(Object v) {
        return v == null ? "" : v.toString();
    }

    public static void info(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirm(Component parent, String msg) {
        return JOptionPane.showConfirmDialog(parent, msg, "Xác nhận", JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION;
    }

    public static void error(Component parent, Throwable e) {
        String msg;
        if (e instanceof SQLException se) {
            msg = switch (se.getErrorCode()) {
                case 547 -> "Không thực hiện được: dữ liệu đang được dùng ở bảng khác hoặc không hợp lệ.";
                case 2627, 2601 -> "Dữ liệu bị trùng (tên / số điện thoại đã tồn tại).";
                default -> "Lỗi cơ sở dữ liệu: " + se.getMessage();
            };
        } else {
            msg = e.getMessage() == null ? e.toString() : e.getMessage();
        }
        JOptionPane.showMessageDialog(parent, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    // ---------- Đọc và kiểm tra dữ liệu nhập ----------

    public static String required(JTextField f, String label) {
        String s = f.getText().trim();
        if (s.isEmpty()) {
            f.requestFocus();
            throw new IllegalArgumentException("Vui lòng nhập " + label.toLowerCase() + ".");
        }
        return s;
    }

    public static String optional(JTextField f) {
        String s = f.getText().trim();
        return s.isEmpty() ? null : s;
    }

    public static BigDecimal decimal(JTextField f, String label) {
        String s = required(f, label).replace(",", "");
        BigDecimal v;
        try {
            v = new BigDecimal(s);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(label + " phải là số.");
        }
        if (v.signum() < 0) {
            throw new IllegalArgumentException(label + " không được âm.");
        }
        return v;
    }

    public static int integer(JTextField f, String label) {
        String s = required(f, label);
        int v;
        try {
            v = Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(label + " phải là số nguyên.");
        }
        if (v < 0) {
            throw new IllegalArgumentException(label + " không được âm.");
        }
        return v;
    }

    /** Ngày nhập dạng dd/MM/yyyy; để trống thì trả về null. */
    public static java.sql.Date optionalDate(JTextField f, String label) {
        String s = f.getText().trim();
        if (s.isEmpty()) {
            return null;
        }
        try {
            return java.sql.Date.valueOf(LocalDate.parse(s, DATE_FMT));
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(label + " không hợp lệ (định dạng dd/MM/yyyy).");
        }
    }

    // ---------- Dựng giao diện ----------

    /** Form nhãn + ô nhập, chia 2 cột. */
    public static JPanel form(String[] labels, JComponent[] fields) {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.anchor = GridBagConstraints.WEST;
        for (int i = 0; i < labels.length; i++) {
            int col = (i % 2) * 2;
            g.gridy = i / 2;
            g.gridx = col;
            g.weightx = 0;
            g.fill = GridBagConstraints.NONE;
            p.add(new JLabel(labels[i]), g);
            g.gridx = col + 1;
            g.weightx = 1;
            g.fill = GridBagConstraints.HORIZONTAL;
            p.add(fields[i], g);
        }
        return p;
    }

    public static JButton button(String text, ActionListener l) {
        JButton b = new JButton(text);
        b.addActionListener(l);
        return b;
    }

    /** Nạp ComboBox từ câu SELECT có cột 1 = mã (int), cột 2 = tên hiển thị. */
    public static void loadCombo(JComboBox<Item> combo, String sql) throws SQLException {
        DefaultTableModel m = Db.query(sql);
        combo.removeAllItems();
        for (int i = 0; i < m.getRowCount(); i++) {
            combo.addItem(new Item(((Number) m.getValueAt(i, 0)).intValue(), str(m.getValueAt(i, 1))));
        }
    }
}
