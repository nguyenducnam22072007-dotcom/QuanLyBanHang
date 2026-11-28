package qlbh;

import java.sql.SQLException;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/** Form quản lý khách hàng. */
public class KhachHangPanel extends CrudPanel {
    private final JTextField txtMa = new JTextField();
    private final JTextField txtTen = new JTextField();
    private final JComboBox<String> cboGt = new JComboBox<>(new String[] {"Nam", "Nữ"});
    private final JTextField txtSdt = new JTextField();
    private final JTextField txtEmail = new JTextField();
    private final JTextField txtDiaChi = new JTextField();

    public KhachHangPanel() {
        txtMa.setEditable(false);
        init("khách hàng");
    }

    @Override
    protected JPanel buildForm() {
        return Ui.form(new String[] {"Mã KH", "Họ tên", "Giới tính", "Số điện thoại", "Email", "Địa chỉ"},
                new JComponent[] {txtMa, txtTen, cboGt, txtSdt, txtEmail, txtDiaChi});
    }

    @Override
    protected DefaultTableModel loadData(String kw) throws SQLException {
        String k = "%" + kw + "%";
        return Db.query("SELECT MaKH AS [Mã KH], HoTen AS [Họ tên], GioiTinh AS [Giới tính], "
                + "SoDienThoai AS [Số điện thoại], Email, DiaChi AS [Địa chỉ] "
                + "FROM KhachHang WHERE HoTen LIKE ? OR SoDienThoai LIKE ? OR DiaChi LIKE ? ORDER BY MaKH", k, k, k);
    }

    @Override
    protected void doInsert() throws Exception {
        Db.update("INSERT INTO KhachHang (HoTen, GioiTinh, SoDienThoai, Email, DiaChi) VALUES (?, ?, ?, ?, ?)",
                Ui.required(txtTen, "Họ tên"), cboGt.getSelectedItem(), Ui.required(txtSdt, "Số điện thoại"),
                Ui.optional(txtEmail), Ui.optional(txtDiaChi));
    }

    @Override
    protected void doUpdate() throws Exception {
        Db.update("UPDATE KhachHang SET HoTen = ?, GioiTinh = ?, SoDienThoai = ?, Email = ?, DiaChi = ? WHERE MaKH = ?",
                Ui.required(txtTen, "Họ tên"), cboGt.getSelectedItem(), Ui.required(txtSdt, "Số điện thoại"),
                Ui.optional(txtEmail), Ui.optional(txtDiaChi), Integer.parseInt(txtMa.getText()));
    }

    @Override
    protected void doDelete() throws Exception {
        Db.update("DELETE FROM KhachHang WHERE MaKH = ?", Integer.parseInt(txtMa.getText()));
    }

    @Override
    protected void fillForm(int row) {
        TableModel m = table.getModel();
        txtMa.setText(Ui.str(m.getValueAt(row, 0)));
        txtTen.setText(Ui.str(m.getValueAt(row, 1)));
        cboGt.setSelectedItem(Ui.str(m.getValueAt(row, 2)));
        txtSdt.setText(Ui.str(m.getValueAt(row, 3)));
        txtEmail.setText(Ui.str(m.getValueAt(row, 4)));
        txtDiaChi.setText(Ui.str(m.getValueAt(row, 5)));
    }

    @Override
    protected void clearForm() {
        txtMa.setText("");
        txtTen.setText("");
        cboGt.setSelectedIndex(0);
        txtSdt.setText("");
        txtEmail.setText("");
        txtDiaChi.setText("");
    }
}
