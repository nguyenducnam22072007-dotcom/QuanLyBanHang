package qlbh;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/** Bảng màu và các thành phần giao diện dùng chung cho toàn phần mềm. */
public final class Theme {
    private Theme() {}

    // ----- Bảng màu -----
    public static final Color PRIMARY_DARK = new Color(0x0D47A1);
    public static final Color PRIMARY = new Color(0x1565C0);
    public static final Color PRIMARY_LIGHT = new Color(0x42A5F5);
    public static final Color BG = new Color(0xEEF3F9);
    public static final Color CARD = Color.WHITE;
    public static final Color TEXT = new Color(0x263238);
    public static final Color BORDER = new Color(0xC5D0DC);
    public static final Color ZEBRA = new Color(0xF0F6FD);
    public static final Color SELECT = new Color(0xBBDEFB);

    public static final Color GREEN = new Color(0x2E9E4F);
    public static final Color ORANGE = new Color(0xF28C00);
    public static final Color RED = new Color(0xE03C31);
    public static final Color GRAY = new Color(0x78909C);
    public static final Color BLUE = new Color(0x1E88E5);
    public static final Color TEAL = new Color(0x00897B);

    /** Gọi một lần ở đầu chương trình, trước khi tạo giao diện. */
    public static void install() {
        UIManager.put("Panel.background", BG);
        UIManager.put("Label.foreground", TEXT);
        Border field = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(5, 8, 5, 8));
        UIManager.put("TextField.border", field);
        UIManager.put("PasswordField.border", field);
    }

    static Color mix(Color a, Color b, double t) {
        return new Color((int) (a.getRed() + (b.getRed() - a.getRed()) * t),
                (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }

    // ----- Nút bấm màu, bo góc -----
    public static JButton button(String text, Color color, ActionListener l) {
        return new StyledButton(text, color, 14f, 8, 18, l);
    }

    public static JButton bigButton(String text, Color color, ActionListener l) {
        return new StyledButton(text, color, 17f, 12, 30, l);
    }

    private static class StyledButton extends JButton {
        private final Color base;
        private boolean hover;
        private boolean pressed;

        StyledButton(String text, Color base, float size, int padV, int padH, ActionListener l) {
            super(text);
            this.base = base;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setForeground(Color.WHITE);
            setFont(getFont().deriveFont(Font.BOLD, size));
            setBorder(BorderFactory.createEmptyBorder(padV, padH, padV, padH));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addActionListener(l);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    pressed = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    pressed = true;
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    pressed = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color c = !isEnabled() ? mix(base, Color.WHITE, 0.6)
                    : pressed ? mix(base, Color.BLACK, 0.2)
                    : hover ? mix(base, Color.WHITE, 0.15) : base;
            g2.setColor(c);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ----- Khung có tiêu đề -----
    public static Border section(String title) {
        Font f = UIManager.getFont("Label.font");
        f = (f == null ? new Font("SansSerif", Font.PLAIN, 14) : f).deriveFont(Font.BOLD, 14f);
        TitledBorder tb = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER, 1, true), " " + title + " ",
                TitledBorder.LEFT, TitledBorder.TOP, f, PRIMARY);
        return BorderFactory.createCompoundBorder(tb, BorderFactory.createEmptyBorder(4, 8, 8, 8));
    }

    /** Panel nền gradient (dùng cho banner). */
    public static class GradientPanel extends JPanel {
        private final Color from;
        private final Color to;

        public GradientPanel(Color from, Color to) {
            this.from = from;
            this.to = to;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setPaint(new GradientPaint(0, 0, from, getWidth(), 0, to));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }

    // ----- Bảng dữ liệu -----
    public static void styleTable(JTable t) {
        t.setRowHeight(30);
        t.setShowGrid(false);
        t.setShowHorizontalLines(true);
        t.setGridColor(new Color(0xE3E8EE));
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setSelectionBackground(SELECT);
        t.setSelectionForeground(TEXT);
        t.setFillsViewportHeight(true);
        t.setDefaultRenderer(Object.class, new ZebraRenderer());
        JTableHeader h = t.getTableHeader();
        h.setDefaultRenderer(new HeaderRenderer());
        h.setBackground(PRIMARY);
        h.setOpaque(true);
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(0, 36));
    }

    private static class ZebraRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int r, int c) {
            boolean money = v instanceof BigDecimal;
            super.getTableCellRendererComponent(t, money ? Ui.money(v) : Ui.str(v), sel, false, r, c);
            setHorizontalAlignment(money ? RIGHT : LEFT);
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            if (!sel) {
                setBackground(r % 2 == 0 ? Color.WHITE : ZEBRA);
                setForeground(TEXT);
            }
            return this;
        }
    }

    private static class HeaderRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int r, int c) {
            String text = Ui.str(v);
            RowSorter<?> rs = t.getRowSorter();
            if (rs != null && !rs.getSortKeys().isEmpty()) {
                RowSorter.SortKey k = rs.getSortKeys().get(0);
                if (k.getColumn() == t.convertColumnIndexToModel(c) && k.getSortOrder() != SortOrder.UNSORTED) {
                    text += k.getSortOrder() == SortOrder.ASCENDING ? "  ▲" : "  ▼";
                }
            }
            super.getTableCellRendererComponent(t, text, false, false, r, c);
            setOpaque(true);
            setBackground(PRIMARY);
            setForeground(Color.WHITE);
            setFont(getFont().deriveFont(Font.BOLD));
            setHorizontalAlignment(LEFT);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 0, 1, mix(PRIMARY, Color.WHITE, 0.25)),
                    BorderFactory.createEmptyBorder(0, 10, 0, 10)));
            return this;
        }
    }

    // ----- Tab màu -----
    public static void styleTabs(JTabbedPane tabs) {
        tabs.setUI(new TabsUI());
        tabs.setFont(tabs.getFont().deriveFont(Font.BOLD, 15f));
        tabs.setBackground(BG);
        tabs.setOpaque(true);
    }

    private static class TabsUI extends BasicTabbedPaneUI {
        @Override
        protected void installDefaults() {
            super.installDefaults();
            tabInsets = new Insets(10, 22, 10, 22);
            selectedTabPadInsets = new Insets(0, 0, 0, 0);
            tabAreaInsets = new Insets(6, 10, 0, 10);
            contentBorderInsets = new Insets(0, 0, 0, 0);
        }

        @Override
        protected void paintTabBackground(Graphics g, int tp, int index, int x, int y, int w, int h, boolean sel) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(sel ? PRIMARY : new Color(0xD6E6F7));
            g2.fillRoundRect(x + 2, y, w - 4, h + 8, 14, 14);
            g2.dispose();
        }

        @Override
        protected void paintTabBorder(Graphics g, int tp, int index, int x, int y, int w, int h, boolean sel) {
            // không vẽ viền
        }

        @Override
        protected void paintFocusIndicator(Graphics g, int tp, Rectangle[] rects, int index,
                Rectangle iconRect, Rectangle textRect, boolean sel) {
            // không vẽ khung focus
        }

        @Override
        protected void paintContentBorder(Graphics g, int tp, int selectedIndex) {
            int h = calculateTabAreaHeight(tp, runCount, maxTabHeight);
            g.setColor(PRIMARY);
            g.fillRect(0, h - 3, tabPane.getWidth(), 3);
        }

        @Override
        protected void paintText(Graphics g, int tp, Font font, FontMetrics metrics, int index,
                String title, Rectangle textRect, boolean sel) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(font);
            g2.setColor(sel ? Color.WHITE : PRIMARY_DARK);
            g2.drawString(title, textRect.x, textRect.y + metrics.getAscent());
            g2.dispose();
        }

        @Override
        protected int getTabLabelShiftX(int tp, int index, boolean sel) {
            return 0;
        }

        @Override
        protected int getTabLabelShiftY(int tp, int index, boolean sel) {
            return 0;
        }
    }
}
