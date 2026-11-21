package qlbh;

import java.sql.SQLException;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/** Form quản lý sản phẩm. */
public class SanPhamPanel extends CrudPanel {
    private final JTextField txtMa = new JTextField();
    private final JTextField txtTen = new JTextField();
    private final JComboBox<Item> cboLoai = new JComboBox<>();
    private final JTextField txtGia = new JTextField();
    private final JTextField txtTon = new JTextField();
    private final JTextField txtDvt = new JTextField();

    public SanPhamPanel() {
        txtMa.setEditable(false);
        init();
    }

    @Override
    protected JPanel buildForm() {
        return Ui.form(
                new String[] {"Mã SP", "Tên sản phẩm", "Loại sản phẩm", "Đơn giá (VNĐ)", "Số lượng tồn", "Đơn vị tính"},
                new JComponent[] {txtMa, txtTen, cboLoai, txtGia, txtTon, txtDvt});
    }

    @Override
    public void refresh() {
        try {
            Ui.loadCombo(cboLoai, "SELECT MaLoai, TenLoai FROM LoaiSanPham ORDER BY TenLoai");
        } catch (SQLException ex) {
            Ui.error(this, ex);
        }
        super.refresh();
    }

    @Override
    protected DefaultTableModel loadData(String kw) throws SQLException {
        String k = "%" + kw + "%";
        return Db.query("SELECT sp.MaSP AS [Mã SP], sp.TenSP AS [Tên sản phẩm], l.TenLoai AS [Loại], "
                + "sp.DonGia AS [Đơn giá], sp.SoLuongTon AS [Tồn kho], sp.DonViTinh AS [Đơn vị tính] "
                + "FROM SanPham sp JOIN LoaiSanPham l ON sp.MaLoai = l.MaLoai "
                + "WHERE sp.TenSP LIKE ? OR l.TenLoai LIKE ? ORDER BY sp.MaSP", k, k);
    }

    private Item selectedLoai() {
        Item loai = (Item) cboLoai.getSelectedItem();
        if (loai == null) {
            throw new IllegalArgumentException("Chưa có loại sản phẩm. Hãy thêm loại sản phẩm trước.");
        }
        return loai;
    }

    @Override
    protected void doInsert() throws Exception {
        Db.update("INSERT INTO SanPham (TenSP, MaLoai, DonGia, SoLuongTon, DonViTinh) VALUES (?, ?, ?, ?, ?)",
                Ui.required(txtTen, "Tên sản phẩm"), selectedLoai().id(),
                Ui.decimal(txtGia, "Đơn giá"), Ui.integer(txtTon, "Số lượng tồn"), Ui.optional(txtDvt));
    }

    @Override
    protected void doUpdate() throws Exception {
        Db.update("UPDATE SanPham SET TenSP = ?, MaLoai = ?, DonGia = ?, SoLuongTon = ?, DonViTinh = ? WHERE MaSP = ?",
                Ui.required(txtTen, "Tên sản phẩm"), selectedLoai().id(),
                Ui.decimal(txtGia, "Đơn giá"), Ui.integer(txtTon, "Số lượng tồn"), Ui.optional(txtDvt),
                Integer.parseInt(txtMa.getText()));
    }

    @Override
    protected void doDelete() throws Exception {
        Db.update("DELETE FROM SanPham WHERE MaSP = ?", Integer.parseInt(txtMa.getText()));
    }

    @Override
    protected void fillForm(int row) {
        TableModel m = table.getModel();
        txtMa.setText(Ui.str(m.getValueAt(row, 0)));
        txtTen.setText(Ui.str(m.getValueAt(row, 1)));
        String loai = Ui.str(m.getValueAt(row, 2));
        for (int i = 0; i < cboLoai.getItemCount(); i++) {
            if (cboLoai.getItemAt(i).name().equals(loai)) {
                cboLoai.setSelectedIndex(i);
            }
        }
        txtGia.setText(Ui.str(m.getValueAt(row, 3)));
        txtTon.setText(Ui.str(m.getValueAt(row, 4)));
        txtDvt.setText(Ui.str(m.getValueAt(row, 5)));
    }

    @Override
    protected void clearForm() {
        txtMa.setText("");
        txtTen.setText("");
        txtGia.setText("");
        txtTon.setText("");
        txtDvt.setText("");
        if (cboLoai.getItemCount() > 0) {
            cboLoai.setSelectedIndex(0);
        }
    }
}
