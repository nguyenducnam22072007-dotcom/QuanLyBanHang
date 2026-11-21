package qlbh;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.BorderFactory;
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
        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row1.add(new JLabel("Khách hàng:"));
        row1.add(cboKH);
        row1.add(new JLabel("Nhân viên bán:"));
        row1.add(cboNV);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row2.add(new JLabel("Sản phẩm:"));
        row2.add(cboSP);
        row2.add(new JLabel("Số lượng:"));
        row2.add(spnSL);
        row2.add(Ui.button("Thêm vào giỏ", e -> addToCart()));
        row2.add(Ui.button("Xóa khỏi giỏ", e -> removeFromCart()));

        JPanel north = new JPanel(new GridLayout(2, 1));
        north.add(row1);
        north.add(row2);
        add(north, BorderLayout.NORTH);

        tblCart.setRowHeight(24);
        add(new JScrollPane(tblCart), BorderLayout.CENTER);

        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 20f));
        JPanel south = new JPanel(new BorderLayout());
        south.add(lblTotal, BorderLayout.WEST);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        btns.add(Ui.button("Hủy giỏ hàng", e -> {
            cart.setRowCount(0);
            updateTotal();
        }));
        btns.add(Ui.button("Thanh toán", e -> pay()));
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
