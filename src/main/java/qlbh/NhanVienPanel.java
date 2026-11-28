package qlbh;

import java.sql.SQLException;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/** Form quản lý nhân viên. Ngày nhập theo dạng dd/MM/yyyy. */
public class NhanVienPanel extends CrudPanel {
    private final JTextField txtMa = new JTextField();
    private final JTextField txtTen = new JTextField();
    private final JTextField txtNgaySinh = new JTextField();
    private final JTextField txtSdt = new JTextField();
    private final JTextField txtChucVu = new JTextField();

    public NhanVienPanel() {
        txtMa.setEditable(false);
        init("nhân viên");
    }

    @Override
    protected JPanel buildForm() {
        return Ui.form(new String[] {"Mã NV", "Họ tên", "Ngày sinh (dd/MM/yyyy)", "Số điện thoại", "Chức vụ"},
                new JComponent[] {txtMa, txtTen, txtNgaySinh, txtSdt, txtChucVu});
    }

    @Override
    protected DefaultTableModel loadData(String kw) throws SQLException {
        String k = "%" + kw + "%";
        return Db.query("SELECT MaNV AS [Mã NV], HoTen AS [Họ tên], CONVERT(varchar(10), NgaySinh, 103) AS [Ngày sinh], "
                + "SoDienThoai AS [Số điện thoại], ChucVu AS [Chức vụ], CONVERT(varchar(10), NgayVaoLam, 103) AS [Ngày vào làm] "
                + "FROM NhanVien WHERE HoTen LIKE ? OR SoDienThoai LIKE ? OR ChucVu LIKE ? ORDER BY MaNV", k, k, k);
    }

    @Override
    protected void doInsert() throws Exception {
        Db.update("INSERT INTO NhanVien (HoTen, NgaySinh, SoDienThoai, ChucVu) VALUES (?, ?, ?, ?)",
                Ui.required(txtTen, "Họ tên"), Ui.optionalDate(txtNgaySinh, "Ngày sinh"),
                Ui.required(txtSdt, "Số điện thoại"), Ui.optional(txtChucVu));
    }

    @Override
    protected void doUpdate() throws Exception {
        Db.update("UPDATE NhanVien SET HoTen = ?, NgaySinh = ?, SoDienThoai = ?, ChucVu = ? WHERE MaNV = ?",
                Ui.required(txtTen, "Họ tên"), Ui.optionalDate(txtNgaySinh, "Ngày sinh"),
                Ui.required(txtSdt, "Số điện thoại"), Ui.optional(txtChucVu), Integer.parseInt(txtMa.getText()));
    }

    @Override
    protected void doDelete() throws Exception {
        Db.update("DELETE FROM NhanVien WHERE MaNV = ?", Integer.parseInt(txtMa.getText()));
    }

    @Override
    protected void fillForm(int row) {
        TableModel m = table.getModel();
        txtMa.setText(Ui.str(m.getValueAt(row, 0)));
        txtTen.setText(Ui.str(m.getValueAt(row, 1)));
        txtNgaySinh.setText(Ui.str(m.getValueAt(row, 2)));
        txtSdt.setText(Ui.str(m.getValueAt(row, 3)));
        txtChucVu.setText(Ui.str(m.getValueAt(row, 4)));
    }

    @Override
    protected void clearForm() {
        txtMa.setText("");
        txtTen.setText("");
        txtNgaySinh.setText("");
        txtSdt.setText("");
        txtChucVu.setText("");
    }
}
