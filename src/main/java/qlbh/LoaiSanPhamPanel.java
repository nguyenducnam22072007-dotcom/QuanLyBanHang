package qlbh;

import java.sql.SQLException;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/** Form quản lý loại sản phẩm. */
public class LoaiSanPhamPanel extends CrudPanel {
    private final JTextField txtMa = new JTextField();
    private final JTextField txtTen = new JTextField();
    private final JTextField txtMoTa = new JTextField();

    public LoaiSanPhamPanel() {
        txtMa.setEditable(false);
        init("loại sản phẩm");
    }

    @Override
    protected JPanel buildForm() {
        return Ui.form(new String[] {"Mã loại", "Tên loại", "Mô tả"},
                new JComponent[] {txtMa, txtTen, txtMoTa});
    }

    @Override
    protected DefaultTableModel loadData(String kw) throws SQLException {
        String k = "%" + kw + "%";
        return Db.query("SELECT MaLoai AS [Mã loại], TenLoai AS [Tên loại], MoTa AS [Mô tả] "
                + "FROM LoaiSanPham WHERE TenLoai LIKE ? OR MoTa LIKE ? ORDER BY MaLoai", k, k);
    }

    @Override
    protected void doInsert() throws Exception {
        Db.update("INSERT INTO LoaiSanPham (TenLoai, MoTa) VALUES (?, ?)",
                Ui.required(txtTen, "Tên loại"), Ui.optional(txtMoTa));
    }

    @Override
    protected void doUpdate() throws Exception {
        Db.update("UPDATE LoaiSanPham SET TenLoai = ?, MoTa = ? WHERE MaLoai = ?",
                Ui.required(txtTen, "Tên loại"), Ui.optional(txtMoTa), Integer.parseInt(txtMa.getText()));
    }

    @Override
    protected void doDelete() throws Exception {
        Db.update("DELETE FROM LoaiSanPham WHERE MaLoai = ?", Integer.parseInt(txtMa.getText()));
    }

    @Override
    protected void fillForm(int row) {
        TableModel m = table.getModel();
        txtMa.setText(Ui.str(m.getValueAt(row, 0)));
        txtTen.setText(Ui.str(m.getValueAt(row, 1)));
        txtMoTa.setText(Ui.str(m.getValueAt(row, 2)));
    }

    @Override
    protected void clearForm() {
        txtMa.setText("");
        txtTen.setText("");
        txtMoTa.setText("");
    }
}
