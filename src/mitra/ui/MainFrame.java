package mitra.ui;

import mitra.koneksi.Koneksi;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * MainFrame - JFrame utama aplikasi Nyetok
 * Berisi sidebar navigasi kiri dan area konten kanan
 * Desain blue theme modern
 */
public class MainFrame extends JFrame {

    // ---- Warna Tema ----
    public static final Color BIRU_TUA    = new Color(0x1565C0);
    public static final Color BIRU_MUDA   = new Color(0x1E88E5);
    public static final Color BG_KONTEN   = new Color(0xF5F7FA);
    public static final Color PUTIH       = Color.WHITE;
    public static final Color TEKS_GELAP  = new Color(0x212121);
    public static final Color HIJAU       = new Color(0x43A047);
    public static final Color ORANYE      = new Color(0xFB8C00);
    public static final Color MERAH       = new Color(0xE53935);
    public static final Color SIDEBAR_HOVER = new Color(0x0D47A1);
    public static final Color SIDEBAR_AKTIF = new Color(0x0D47A1);
    public static final Color TEKS_SIDEBAR  = new Color(255, 255, 255, 220);

    // ---- Komponen utama ----
    private JPanel     contentPanel;
    private JLabel     pageTitle;
    private JLabel     dateLabel;
    private JButton[]  menuButtons;
    private int        activeMenuIndex = 0;

    // ---- Menu items ----
    private static final String[][] MENU_ITEMS = {
        {"🏠", "Dashboard"},
        {"📦", "Purchase Orders"},
        {"📥", "Input Stok"},
        {"📋", "Daftar Stok"},
        {"🔄", "Stok Keluar"},
        {"💰", "Finance"},
        {"📊", "Produk"},
        {"📈", "Laporan"},
        {"⚙", "Settings"},
        {"🚪", "Log Out"}
    };

    public MainFrame() {
        super("Nyetok - Inventory Management System");
        initFrame();
        buildUI();
        startClock();
        // Tampilkan dashboard secara default
        navigateTo(0);
    }

    /** Inisialisasi properti JFrame */
    private void initFrame() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1200, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Konfirmasi keluar
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int result = JOptionPane.showConfirmDialog(
                    MainFrame.this,
                    "Apakah Anda yakin ingin keluar dari aplikasi?",
                    "Konfirmasi Keluar",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                );
                if (result == JOptionPane.YES_OPTION) {
                    Koneksi.getInstance().closeConnection();
                    System.exit(0);
                }
            }
        });
    }

    /** Membangun keseluruhan UI */
    private void buildUI() {
        // Sidebar kiri
        JPanel sidebar = buildSidebar();
        add(sidebar, BorderLayout.WEST);

        // Panel kanan (header + konten)
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(BG_KONTEN);

        JPanel header = buildHeader();
        rightPanel.add(header, BorderLayout.NORTH);

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BG_KONTEN);
        rightPanel.add(contentPanel, BorderLayout.CENTER);

        add(rightPanel, BorderLayout.CENTER);
    }

    /** Membangun sidebar navigasi kiri */
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                    0, 0, BIRU_TUA, 0, getHeight(), new Color(0x0D47A1));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // ---- Logo Area ----
        JPanel logoPanel = new JPanel();
        logoPanel.setOpaque(false);
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        logoPanel.setBorder(BorderFactory.createEmptyBorder(24, 20, 20, 20));
        logoPanel.setMaximumSize(new Dimension(220, 110));

        JLabel logoIcon = new JLabel("📦");
        logoIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        logoIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel logoText = new JLabel("NYETOK");
        logoText.setFont(new Font("Arial", Font.BOLD, 22));
        logoText.setForeground(PUTIH);
        logoText.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel logoSub = new JLabel("Inventory System");
        logoSub.setFont(new Font("Arial", Font.PLAIN, 10));
        logoSub.setForeground(new Color(255, 255, 255, 150));
        logoSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        logoPanel.add(logoIcon);
        logoPanel.add(Box.createVerticalStrut(4));
        logoPanel.add(logoText);
        logoPanel.add(logoSub);

        sidebar.add(logoPanel);

        // Garis pemisah
        sidebar.add(createSidebarDivider());

        // ---- Menu Items ----
        menuButtons = new JButton[MENU_ITEMS.length];
        for (int i = 0; i < MENU_ITEMS.length; i++) {
            final int index = i;
            JButton btn = createMenuButton(MENU_ITEMS[i][0], MENU_ITEMS[i][1], i);
            btn.addActionListener(e -> navigateTo(index));
            menuButtons[i] = btn;
            sidebar.add(btn);
        }

        sidebar.add(Box.createVerticalGlue());

        // Info versi di bawah
        JLabel verLabel = new JLabel("v1.0.0 · Nyetok");
        verLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        verLabel.setForeground(new Color(255, 255, 255, 100));
        verLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        verLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        sidebar.add(verLabel);

        return sidebar;
    }

    /** Membuat tombol menu sidebar */
    private JButton createMenuButton(String icon, String label, int index) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                if (activeMenuIndex == index) {
                    // Aktif: highlight kiri
                    g2.setColor(SIDEBAR_AKTIF);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.setColor(PUTIH);
                    g2.fillRect(0, 6, 4, getHeight() - 12);
                } else if (getModel().isRollover()) {
                    g2.setColor(SIDEBAR_HOVER);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                } else {
                    g2.setColor(new Color(0, 0, 0, 0));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
                super.paintComponent(g);
            }
        };

        btn.setText(icon + "  " + label);
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        btn.setForeground(TEKS_SIDEBAR);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(220, 44));
        btn.setMinimumSize(new Dimension(220, 44));
        btn.setPreferredSize(new Dimension(220, 44));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { btn.repaint(); }
        });

        return btn;
    }

    /** Membuat garis pemisah sidebar */
    private JPanel createSidebarDivider() {
        JPanel div = new JPanel();
        div.setOpaque(false);
        div.setMaximumSize(new Dimension(220, 1));
        div.setPreferredSize(new Dimension(220, 1));
        div.setBackground(new Color(255, 255, 255, 50));
        div.setBorder(BorderFactory.createMatteBorder(1, 20, 0, 20,
                                                       new Color(255, 255, 255, 50)));
        return div;
    }

    /** Membangun header atas */
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(PUTIH);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Shadow bawah
                g2.setColor(new Color(0, 0, 0, 20));
                g2.fillRect(0, getHeight() - 2, getWidth(), 2);
            }
        };
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(0, 60));
        header.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 24));

        // Kiri: judul halaman
        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftHeader.setOpaque(false);

        pageTitle = new JLabel("Dashboard");
        pageTitle.setFont(new Font("Arial", Font.BOLD, 18));
        pageTitle.setForeground(TEKS_GELAP);
        leftHeader.add(pageTitle);

        // Kanan: tanggal, user, sync
        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightHeader.setOpaque(false);

        dateLabel = new JLabel();
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        dateLabel.setForeground(new Color(0x757575));

        JLabel userLabel = new JLabel("👤 Admin");
        userLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        userLabel.setForeground(new Color(0x757575));

        JButton syncBtn = createHeaderButton("🔄 Sync", BIRU_MUDA);
        syncBtn.addActionListener(e -> {
            JPanel current = (JPanel) contentPanel.getComponent(0);
            if (current instanceof Dashboard) ((Dashboard) current).loadData();
            else if (current instanceof DaftarStok) ((DaftarStok) current).loadData();
            else if (current instanceof DaftarProduk) ((DaftarProduk) current).loadData();
            else if (current instanceof StokKeluar) ((StokKeluar) current).loadData();
            else if (current instanceof Laporan) ((Laporan) current).loadData();
            JOptionPane.showMessageDialog(this, "Data berhasil disinkronisasi!",
                "Sync", JOptionPane.INFORMATION_MESSAGE);
        });

        rightHeader.add(dateLabel);
        rightHeader.add(userLabel);
        rightHeader.add(syncBtn);

        header.add(leftHeader, BorderLayout.WEST);
        header.add(rightHeader, BorderLayout.EAST);

        return header;
    }

    /** Membuat tombol kecil di header */
    private JButton createHeaderButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? bg.darker() :
                            getModel().isRollover() ? bg.brighter() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 11));
        btn.setForeground(PUTIH);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(90, 32));
        return btn;
    }

    /** Navigasi ke halaman tertentu berdasarkan index menu */
    public void navigateTo(int index) {
        // Konfirmasi logout
        if (index == 9) {
            int r = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin keluar?", "Konfirmasi Log Out",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (r != JOptionPane.YES_OPTION) return;
            Koneksi.getInstance().closeConnection();
            System.exit(0);
        }

        activeMenuIndex = index;
        // Update tampilan tombol sidebar
        for (JButton btn : menuButtons) btn.repaint();

        // Update judul halaman
        pageTitle.setText(MENU_ITEMS[index][1]);

        // Ganti panel konten
        contentPanel.removeAll();
        JPanel panel = createPanelForIndex(index);
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    /** Membuat panel konten berdasarkan index menu */
    private JPanel createPanelForIndex(int index) {
        return switch (index) {
            case 0  -> new Dashboard(this);
            case 1  -> new InputStok(this);   // Purchase Orders -> ke Input Stok
            case 2  -> new InputStok(this);
            case 3  -> new DaftarStok(this);
            case 4  -> new StokKeluar(this);
            case 5  -> createPlaceholderPanel("💰 Finance", "Modul Finance dalam pengembangan...");
            case 6  -> new DaftarProduk(this);
            case 7  -> new Laporan(this);
            case 8  -> createPlaceholderPanel("⚙ Settings", "Pengaturan aplikasi akan hadir segera...");
            default -> new Dashboard(this);
        };
    }

    /** Membuat panel placeholder untuk fitur dalam pengembangan */
    private JPanel createPlaceholderPanel(String title, String msg) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_KONTEN);

        JPanel card = new JPanel();
        card.setBackground(PUTIH);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xE0E0E0), 1, true),
            BorderFactory.createEmptyBorder(40, 60, 40, 60)
        ));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel ico = new JLabel("🚧");
        ico.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        ico.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel t = new JLabel(title);
        t.setFont(new Font("Arial", Font.BOLD, 20));
        t.setForeground(TEKS_GELAP);
        t.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel s = new JLabel(msg);
        s.setFont(new Font("Arial", Font.PLAIN, 14));
        s.setForeground(new Color(0x757575));
        s.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(ico);
        card.add(Box.createVerticalStrut(12));
        card.add(t);
        card.add(Box.createVerticalStrut(6));
        card.add(s);

        panel.add(card);
        return panel;
    }

    /** Timer untuk memperbarui jam di header */
    private void startClock() {
        Timer timer = new Timer(1000, e -> {
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMMM yyyy  HH:mm:ss");
            dateLabel.setText(sdf.format(new Date()));
        });
        timer.start();
        // Trigger pertama kali
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMMM yyyy  HH:mm:ss");
        dateLabel.setText(sdf.format(new Date()));
    }

    // ---- Utility: RoundedPanel ----

    /**
     * Panel dengan sudut membulat dan shadow ringan
     */
    public static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color bg;

        public RoundedPanel(int radius, Color bg) {
            this.radius = radius;
            this.bg     = bg;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            // Shadow ringan
            g2.setColor(new Color(0, 0, 0, 15));
            g2.fillRoundRect(2, 3, getWidth() - 2, getHeight() - 2, radius, radius);
            // Background
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, radius, radius);
        }
    }
}
