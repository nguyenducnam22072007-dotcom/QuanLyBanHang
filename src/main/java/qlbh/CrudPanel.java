package qlbh;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/**
 * Khung chung cho các form quản lý (Thêm - Sửa - Xóa - Tìm kiếm - Làm mới).
 * Lớp con chỉ cần dựng form nhập và viết các câu lệnh SQL tương ứng.
 */
public abstract class CrudPanel extends JPanel implements Refreshable {
    protected final JTable table = new JTable();
    protected final JTextField txtSearch = new JTextField(22);

    @FunctionalInterface
    private interface Task {
        void run() throws Exception;
    }

    protected abstract JPanel buildForm();

    protected abstract DefaultTableModel loadData(String keyword) throws SQLException;

    protected abstract void doInsert() throws Exception;

    protected abstract void doUpdate() throws Exception;

    protected abstract void doDelete() throws Exception;

    /** Đổ dòng đang chọn trong bảng lên form (modelRow = chỉ số dòng trong model). */
    protected abstract void fillForm(int modelRow);

    protected abstract void clearForm();

    /** Lớp con gọi hàm này ở cuối constructor. */
    protected final void init() {
        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        bar.add(Ui.button("Thêm", e -> run(this::doInsert, "Đã thêm thành công.", false)));
        bar.add(Ui.button("Sửa", e -> run(this::doUpdate, "Đã cập nhật thành công.", true)));
        bar.add(Ui.button("Xóa", e -> {
            if (hasSelection() && Ui.confirm(this, "Bạn có chắc muốn xóa dòng đã chọn?")) {
                run(this::doDelete, "Đã xóa thành công.", true);
            }
        }));
        bar.add(Ui.button("Làm mới", e -> {
            txtSearch.setText("");
            table.clearSelection();
            clearForm();
            refresh();
        }));
        bar.add(Box.createHorizontalStrut(24));
        bar.add(new JLabel("Tìm kiếm:"));
        bar.add(txtSearch);
        bar.add(Ui.button("Tìm", e -> refresh()));
        txtSearch.addActionListener(e -> refresh());

        JPanel north = new JPanel(new BorderLayout());
        north.add(buildForm(), BorderLayout.CENTER);
        north.add(bar, BorderLayout.SOUTH);
        add(north, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        table.setRowHeight(24);
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            int r = table.getSelectedRow();
            if (!e.getValueIsAdjusting() && r >= 0) {
                fillForm(table.convertRowIndexToModel(r));
            }
        });
        refresh();
    }

    private boolean hasSelection() {
        if (table.getSelectedRow() < 0) {
            Ui.info(this, "Hãy chọn một dòng trong bảng trước.");
            return false;
        }
        return true;
    }

    private void run(Task task, String okMessage, boolean needSelection) {
        if (needSelection && !hasSelection()) {
            return;
        }
        try {
            task.run();
            table.clearSelection();
            clearForm();
            refresh();
            Ui.info(this, okMessage);
        } catch (Exception ex) {
            Ui.error(this, ex);
        }
    }

    @Override
    public void refresh() {
        try {
            table.setModel(loadData(txtSearch.getText().trim()));
        } catch (SQLException ex) {
            Ui.error(this, ex);
        }
    }
}
