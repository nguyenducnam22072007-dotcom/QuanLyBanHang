package qlbh;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

/** Form bán hàng: chọn khách hàng, nhân viên, thêm sản phẩm vào giỏ và thanh toán (lập hóa đơn). */
public class BanHangPanel extends JPanel implements Refreshable {

    private record SP(int id, String name, BigDecimal price, int stock) {
        @Override
        public String toString() {
            return name + " - " + Ui.money(price) + " đ (tồn: " + stock + ")";
        }
    }

    private final JComboBox<Item> cboKH = new JComboBox<>();
    private final JComboBox<Item> cboNV = new JComboBox<>();
    private final JComboBox<SP> cboSP = new JComboBox<>();
    private final JSpinner spnSL = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
    private final DefaultTableModel cart = new DefaultTableModel(
            new Object[] {"Mã SP", "Tên sản phẩm", "Số lượng", "Đơn giá", "Thành tiền"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tblCart = new JTable(cart);
    private final JLabel lblTotal = new JLabel("Tổng tiền: 0 đ");

    public BanHangPanel() {
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        cboKH.setPreferredSize(new Dimension(280, 32));
        cboNV.setPreferredSize(new Dimension(220, 32));
        cboSP.setPreferredSize(new Dimension(380, 32));
        spnSL.setPreferredSize(new Dimension(80, 32));

        JPanel info = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        info.setBackground(Theme.CARD);
        info.setBorder(Theme.section("1. Thông tin bán hàng"));
        info.add(new JLabel("Khách hàng:"));
        info.add(cboKH);
        info.add(new JLabel("Nhân viên bán:"));
        info.add(cboNV);

        JPanel pick = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        pick.setBackground(Theme.CARD);
        pick.setBorder(Theme.section("2. Chọn sản phẩm"));
        pick.add(new JLabel("Sản phẩm:"));
        pick.add(cboSP);
        pick.add(new JLabel("Số lượng:"));
        pick.add(spnSL);
        pick.add(Ui.button("Thêm vào giỏ", Theme.GREEN, e -> addToCart()));
        pick.add(Ui.button("Xóa khỏi giỏ", Theme.ORANGE, e -> removeFromCart()));

        JPanel north = new JPanel();
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.add(info);
        north.add(Box.createVerticalStrut(10));
        north.add(pick);
        add(north, BorderLayout.NORTH);

        Theme.styleTable(tblCart);
        JScrollPane scroll = new JScrollPane(tblCart);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        scroll.getViewport().setBackground(Color.WHITE);
        JPanel cartPanel = new JPanel(new BorderLayout());
        cartPanel.setBackground(Theme.CARD);
        cartPanel.setBorder(Theme.section("3. Giỏ hàng"));
        cartPanel.add(scroll, BorderLayout.CENTER);
        add(cartPanel, BorderLayout.CENTER);

        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 24f));
        lblTotal.setForeground(new Color(0xD32F2F));
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 2));
        btns.setOpaque(false);
        btns.add(Ui.button("Hủy giỏ hàng", Theme.RED, e -> {
            cart.setRowCount(0);
            updateTotal();
        }));
        btns.add(Theme.bigButton("THANH TOÁN", Theme.TEAL, e -> pay()));
        JPanel south = new JPanel(new BorderLayout());
        south.setBackground(Theme.CARD);
        south.setBorder(Theme.section("4. Thanh toán"));
        south.add(lblTotal, BorderLayout.WEST);
        south.add(btns, BorderLayout.EAST);
        add(south, BorderLayout.SOUTH);

        refresh();
    }

    @Override
    public void refresh() {
        try {
            Ui.loadCombo(cboKH, "SELECT MaKH, HoTen + N' - ' + ISNULL(SoDienThoai, '') AS Ten FROM KhachHang ORDER BY HoTen");
            Ui.loadCombo(cboNV, "SELECT MaNV, HoTen FROM NhanVien ORDER BY HoTen");
            DefaultTableModel m = Db.query(
                    "SELECT MaSP, TenSP, DonGia, SoLuongTon FROM SanPham WHERE SoLuongTon > 0 ORDER BY TenSP");
            cboSP.removeAllItems();
            for (int i = 0; i < m.getRowCount(); i++) {
                cboSP.addItem(new SP(((Number) m.getValueAt(i, 0)).intValue(), Ui.str(m.getValueAt(i, 1)),
                        new BigDecimal(Ui.str(m.getValueAt(i, 2))), ((Number) m.getValueAt(i, 3)).intValue()));
            }
        } catch (SQLException ex) {
            Ui.error(this, ex);
        }
    }

    private void addToCart() {
        SP sp = (SP) cboSP.getSelectedItem();
        if (sp == null) {
            Ui.info(this, "Không có sản phẩm nào còn hàng để bán.");
            return;
        }
        int qty = (Integer) spnSL.getValue();
        for (int i = 0; i < cart.getRowCount(); i++) {
            if ((Integer) cart.getValueAt(i, 0) == sp.id()) {
                int newQty = (Integer) cart.getValueAt(i, 2) + qty;
                if (newQty > sp.stock()) {
                    Ui.info(this, "Vượt quá số lượng tồn kho (" + sp.stock() + ").");
                    return;
                }
                cart.setValueAt(newQty, i, 2);
                cart.setValueAt(sp.price().multiply(BigDecimal.valueOf(newQty)), i, 4);
                updateTotal();
                return;
            }
        }
        if (qty > sp.stock()) {
            Ui.info(this, "Vượt quá số lượng tồn kho (" + sp.stock() + ").");
            return;
        }
        cart.addRow(new Object[] {sp.id(), sp.name(), qty, sp.price(), sp.price().multiply(BigDecimal.valueOf(qty))});
        updateTotal();
    }

    private void removeFromCart() {
        int r = tblCart.getSelectedRow();
        if (r < 0) {
            Ui.info(this, "Hãy chọn một dòng trong giỏ hàng.");
            return;
        }
        cart.removeRow(r);
        updateTotal();
    }

    private BigDecimal total() {
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 0; i < cart.getRowCount(); i++) {
            sum = sum.add((BigDecimal) cart.getValueAt(i, 4));
        }
        return sum;
    }

    private void updateTotal() {
        lblTotal.setText("Tổng tiền: " + Ui.money(total()) + " đ");
    }

    /** Lưu hóa đơn + chi tiết và trừ tồn kho trong cùng một transaction. */
    private void pay() {
        if (cart.getRowCount() == 0) {
            Ui.info(this, "Giỏ hàng đang trống.");
            return;
        }
        Item kh = (Item) cboKH.getSelectedItem();
        Item nv = (Item) cboNV.getSelectedItem();
        if (kh == null || nv == null) {
            Ui.info(this, "Hãy chọn khách hàng và nhân viên bán hàng (thêm ở các tab tương ứng nếu chưa có).");
            return;
        }
        BigDecimal sum = total();
        try (Connection c = Db.getConnection()) {
            c.setAutoCommit(false);
            int maHD = -1;
            try {
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO HoaDon (MaKH, MaNV) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, kh.id());
                    ps.setInt(2, nv.id());
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        rs.next();
                        maHD = rs.getInt(1);
                    }
                }
                try (PreparedStatement ins = c.prepareStatement(
                        "INSERT INTO ChiTietHoaDon (MaHD, MaSP, SoLuong, DonGiaBan) VALUES (?, ?, ?, ?)");
                     PreparedStatement upd = c.prepareStatement(
                        "UPDATE SanPham SET SoLuongTon = SoLuongTon - ? WHERE MaSP = ? AND SoLuongTon >= ?")) {
                    for (int i = 0; i < cart.getRowCount(); i++) {
                        int maSP = (Integer) cart.getValueAt(i, 0);
                        int sl = (Integer) cart.getValueAt(i, 2);
                        ins.setInt(1, maHD);
                        ins.setInt(2, maSP);
                        ins.setInt(3, sl);
                        ins.setBigDecimal(4, (BigDecimal) cart.getValueAt(i, 3));
                        ins.executeUpdate();
                        upd.setInt(1, sl);
                        upd.setInt(2, maSP);
                        upd.setInt(3, sl);
                        if (upd.executeUpdate() == 0) {
                            throw new IllegalStateException(
                                    "Sản phẩm \"" + cart.getValueAt(i, 1) + "\" không đủ hàng trong kho.");
                        }
                    }
                }
                c.commit();
            } catch (Exception ex) {
                c.rollback();
                throw ex;
            }
            Ui.info(this, "Thanh toán thành công!\nMã hóa đơn: " + maHD + "\nTổng tiền: " + Ui.money(sum) + " đ");
            cart.setRowCount(0);
            updateTotal();
            refresh();
        } catch (Exception ex) {
            Ui.error(this, ex);
        }
    }
}
