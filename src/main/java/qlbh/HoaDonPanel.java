package qlbh;

import java.awt.BorderLayout;
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
        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        bar.add(new JLabel("Tìm theo tên khách hàng / mã hóa đơn:"));
        bar.add(txtSearch);
        bar.add(Ui.button("Tìm", e -> refresh()));
        bar.add(Ui.button("Làm mới", e -> {
            txtSearch.setText("");
            refresh();
        }));
        bar.add(Ui.button("Xóa hóa đơn", e -> deleteInvoice()));
        txtSearch.addActionListener(e -> refresh());
        add(bar, BorderLayout.NORTH);

        tblHD.setRowHeight(24);
        tblCT.setRowHeight(24);
        tblHD.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblHD.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblHD.getSelectedRow() >= 0) {
                loadDetail(selectedMaHD());
            }
        });

        JScrollPane detail = new JScrollPane(tblCT);
        detail.setBorder(BorderFactory.createTitledBorder("Chi tiết hóa đơn"));
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(tblHD), detail);
        split.setResizeWeight(0.6);
        add(split, BorderLayout.CENTER);

        refresh();
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
