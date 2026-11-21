package qlbh;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

/** Cửa sổ chính, mỗi chức năng là một tab. */
public class MainFrame extends JFrame {
    public MainFrame(String userName) {
        super("Phần mềm quản lý bán hàng");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JLabel title = new JLabel("QUẢN LÝ BÁN HÀNG", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));

        JPanel userBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        userBar.add(new JLabel("Xin chào, " + userName));
        userBar.add(Ui.button("Đăng xuất", e -> {
            dispose();
            new LoginFrame().setVisible(true);
        }));

        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 6, 10));
        header.add(userBar, BorderLayout.NORTH);
        header.add(title, BorderLayout.CENTER);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Bán hàng", new BanHangPanel());
        tabs.addTab("Hóa đơn", new HoaDonPanel());
        tabs.addTab("Sản phẩm", new SanPhamPanel());
        tabs.addTab("Loại sản phẩm", new LoaiSanPhamPanel());
        tabs.addTab("Khách hàng", new KhachHangPanel());
        tabs.addTab("Nhân viên", new NhanVienPanel());
        tabs.addChangeListener(e -> {
            Component c = tabs.getSelectedComponent();
            if (c instanceof Refreshable r) {
                r.refresh();
            }
        });

        add(header, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        setSize(1100, 700);
        setLocationRelativeTo(null);
    }
}
