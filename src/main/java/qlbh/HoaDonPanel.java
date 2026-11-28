package qlbh;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/** Form xem, tìm kiếm và xóa hóa đơn (xóa sẽ hoàn lại số lượng tồn kho). */
public class HoaDonPanel extends JPanel implements Refreshable {
    private final JTable tblHD = new JTable();
    private final JTable tblCT = new JTable();
    private final JTextField txtSearch = new JTextField(22);

    public HoaDonPanel() {
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        left.setOpaque(false);
        left.add(new JLabel("Tên khách hàng / mã hóa đơn:"));
        left.add(txtSearch);
        left.add(Ui.button("Tìm", Theme.BLUE, e -> refresh()));
        left.add(Ui.button("Làm mới", Theme.GRAY, e -> {
            txtSearch.setText("");
            refresh();
        }));
        txtSearch.addActionListener(e -> refresh());
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
        right.setOpaque(false);
        right.add(Ui.button("Xóa hóa đơn", Theme.RED, e -> deleteInvoice()));

        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Theme.CARD);
        bar.setBorder(Theme.section("Tìm kiếm & thao tác"));
        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        add(bar, BorderLayout.NORTH);

        Theme.styleTable(tblHD);
        Theme.styleTable(tblCT);
        tblHD.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblHD.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblHD.getSelectedRow() >= 0) {
                loadDetail(selectedMaHD());
            }
        });

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                card("Danh sách hóa đơn (bấm vào một hóa đơn để xem chi tiết)", tblHD),
                card("Chi tiết hóa đơn đang chọn", tblCT));
        split.setResizeWeight(0.55);
        split.setBorder(null);
        split.setDividerSize(10);
        add(split, BorderLayout.CENTER);

        refresh();
    }

    private static JPanel card(String title, JTable t) {
        JScrollPane scroll = new JScrollPane(t);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        scroll.getViewport().setBackground(Color.WHITE);
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.CARD);
        p.setBorder(Theme.section(title));
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    private int selectedMaHD() {
        return ((Number) tblHD.getValueAt(tblHD.getSelectedRow(), 0)).intValue();
    }

    @Override
    public void refresh() {
        String kw = txtSearch.getText().trim();
        try {
            tblHD.setModel(Db.query(
                    "SELECT hd.MaHD AS [Mã HD], CONVERT(varchar(16), hd.NgayLap, 120) AS [Ngày lập], "
                    + "kh.HoTen AS [Khách hàng], nv.HoTen AS [Nhân viên], hd.TrangThai AS [Trạng thái], "
                    + "SUM(ct.SoLuong * ct.DonGiaBan) AS [Tổng tiền] "
                    + "FROM HoaDon hd JOIN KhachHang kh ON hd.MaKH = kh.MaKH "
                    + "JOIN NhanVien nv ON hd.MaNV = nv.MaNV "
                    + "LEFT JOIN ChiTietHoaDon ct ON hd.MaHD = ct.MaHD "
                    + "WHERE kh.HoTen LIKE ? OR CAST(hd.MaHD AS NVARCHAR(20)) = ? "
                    + "GROUP BY hd.MaHD, hd.NgayLap, kh.HoTen, nv.HoTen, hd.TrangThai ORDER BY hd.MaHD DESC",
                    "%" + kw + "%", kw));
            tblCT.setModel(new DefaultTableModel());
        } catch (SQLException ex) {
            Ui.error(this, ex);
        }
    }

    private void loadDetail(int maHD) {
        try {
            tblCT.setModel(Db.query(
                    "SELECT sp.MaSP AS [Mã SP], sp.TenSP AS [Tên sản phẩm], ct.SoLuong AS [Số lượng], "
                    + "ct.DonGiaBan AS [Đơn giá bán], ct.SoLuong * ct.DonGiaBan AS [Thành tiền] "
                    + "FROM ChiTietHoaDon ct JOIN SanPham sp ON ct.MaSP = sp.MaSP WHERE ct.MaHD = ?", maHD));
        } catch (SQLException ex) {
            Ui.error(this, ex);
        }
    }

    private void deleteInvoice() {
        if (tblHD.getSelectedRow() < 0) {
            Ui.info(this, "Hãy chọn một hóa đơn trong bảng trước.");
            return;
        }
        int maHD = selectedMaHD();
        if (!Ui.confirm(this, "Xóa hóa đơn " + maHD + "? Số lượng hàng sẽ được hoàn lại vào kho.")) {
            return;
        }
        try (Connection c = Db.getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE sp SET sp.SoLuongTon = sp.SoLuongTon + ct.SoLuong "
                        + "FROM SanPham sp JOIN ChiTietHoaDon ct ON sp.MaSP = ct.MaSP WHERE ct.MaHD = ?")) {
                    ps.setInt(1, maHD);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM HoaDon WHERE MaHD = ?")) {
                    ps.setInt(1, maHD);
                    ps.executeUpdate();
                }
                c.commit();
            } catch (Exception ex) {
                c.rollback();
                throw ex;
            }
            refresh();
            Ui.info(this, "Đã xóa hóa đơn " + maHD + ".");
        } catch (Exception ex) {
            Ui.error(this, ex);
        }
    }
}
