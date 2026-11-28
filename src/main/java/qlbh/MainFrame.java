package qlbh;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/** Cửa sổ chính: banner màu ở trên, mỗi chức năng là một tab. */
public class MainFrame extends JFrame {
    public MainFrame(String userName) {
        super("Phần mềm quản lý bán hàng");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Bán hàng", new BanHangPanel());
        tabs.addTab("Hóa đơn", new HoaDonPanel());
        tabs.addTab("Sản phẩm", new SanPhamPanel());
        tabs.addTab("Loại sản phẩm", new LoaiSanPhamPanel());
        tabs.addTab("Khách hàng", new KhachHangPanel());
        tabs.addTab("Nhân viên", new NhanVienPanel());
        Theme.styleTabs(tabs);
        tabs.addChangeListener(e -> {
            Component c = tabs.getSelectedComponent();
            if (c instanceof Refreshable r) {
                r.refresh();
            }
        });

        add(buildHeader(userName, e -> {
            dispose();
            new LoginFrame().setVisible(true);
        }), BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        setSize(1150, 760);
        setLocationRelativeTo(null);
    }

    /** Banner gradient: tên phần mềm, lời chào và nút đăng xuất. */
    static JPanel buildHeader(String userName, ActionListener onLogout) {
        JLabel title = new JLabel("QUẢN LÝ BÁN HÀNG");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Quản lý sản phẩm  •  Khách hàng  •  Hóa đơn");
        sub.setFont(sub.getFont().deriveFont(Font.PLAIN, 14f));
        sub.setForeground(new Color(0xDCEBFA));

        JPanel left = new JPanel(new BorderLayout());
        left.setOpaque(false);
        left.add(title, BorderLayout.CENTER);
        left.add(sub, BorderLayout.SOUTH);

        JLabel hello = new JLabel("Xin chào, " + userName);
        hello.setFont(hello.getFont().deriveFont(Font.BOLD, 15f));
        hello.setForeground(Color.WHITE);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        right.setOpaque(false);
        right.add(hello);
        right.add(Ui.button("Đăng xuất", Theme.RED, onLogout));

        JPanel header = new Theme.GradientPanel(Theme.PRIMARY_DARK, Theme.PRIMARY_LIGHT);
        header.setLayout(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(14, 22, 14, 14));
        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }
}
