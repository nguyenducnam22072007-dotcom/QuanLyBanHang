package qlbh;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.sql.SQLException;
import javax.swing.BorderFactory;
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

    /** Lớp con gọi hàm này ở cuối constructor, truyền tên đối tượng (vd: "sản phẩm"). */
    protected final void init(String entityName) {
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Khung 1: form nhập thông tin
        JPanel form = buildForm();
        form.setBorder(Theme.section("Thông tin " + entityName));

        // Khung 2: nút chức năng + tìm kiếm
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        actions.setOpaque(false);
        actions.add(Ui.button("Thêm", Theme.GREEN, e -> run(this::doInsert, "Đã thêm thành công.", false)));
        actions.add(Ui.button("Sửa", Theme.ORANGE, e -> run(this::doUpdate, "Đã cập nhật thành công.", true)));
        actions.add(Ui.button("Xóa", Theme.RED, e -> {
            if (hasSelection() && Ui.confirm(this, "Bạn có chắc muốn xóa dòng đã chọn?")) {
                run(this::doDelete, "Đã xóa thành công.", true);
            }
        }));
        actions.add(Ui.button("Làm mới", Theme.GRAY, e -> {
            txtSearch.setText("");
            table.clearSelection();
            clearForm();
            refresh();
        }));

        JPanel search = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
        search.setOpaque(false);
        search.add(new JLabel("Tìm kiếm:"));
        search.add(txtSearch);
        search.add(Ui.button("Tìm", Theme.BLUE, e -> refresh()));
        txtSearch.addActionListener(e -> refresh());

        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Theme.CARD);
        bar.setBorder(Theme.section("Chức năng"));
        bar.add(actions, BorderLayout.WEST);
        bar.add(search, BorderLayout.EAST);

        JPanel north = new JPanel(new BorderLayout(0, 10));
        north.add(form, BorderLayout.CENTER);
        north.add(bar, BorderLayout.SOUTH);
        add(north, BorderLayout.NORTH);

        // Khung 3: danh sách
        Theme.styleTable(table);
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            int r = table.getSelectedRow();
            if (!e.getValueIsAdjusting() && r >= 0) {
                fillForm(table.convertRowIndexToModel(r));
            }
        });
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        scroll.getViewport().setBackground(Color.WHITE);
        JPanel list = new JPanel(new BorderLayout());
        list.setBackground(Theme.CARD);
        list.setBorder(Theme.section("Danh sách " + entityName + " (bấm vào một dòng để sửa / xóa)"));
        list.add(scroll, BorderLayout.CENTER);
        add(list, BorderLayout.CENTER);

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
