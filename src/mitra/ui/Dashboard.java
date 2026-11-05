package mitra.ui;

import mitra.koneksi.Koneksi;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Dashboard - Panel halaman utama aplikasi Nyetok
 * Menampilkan statistik, tabel purchase orders & stok realtime
 */
public class Dashboard extends JPanel {

    private final MainFrame frame;

    // Warna tema (referensi dari MainFrame)
    private static final Color BIRU_TUA  = MainFrame.BIRU_TUA;
    private static final Color BIRU_MUDA = MainFrame.BIRU_MUDA;
    private static final Color BG        = MainFrame.BG_KONTEN;
    private static final Color PUTIH     = MainFrame.PUTIH;
    private static final Color HIJAU     = MainFrame.HIJAU;
    private static final Color ORANYE    = MainFrame.ORANYE;
    private static final Color MERAH     = MainFrame.MERAH;
    private static final Color TEKS      = MainFrame.TEKS_GELAP;

    // Label statistik
    private JLabel lblTotalProduk;
    private JLabel lblStokMasuk;
    private JLabel lblStokKeluar;
    private JLabel lblLowStok;

    // Tabel
    private JTable tblPurchaseOrders;
    private JTable tblStokRealtime;
    private DefaultTableModel modelPO;
    private DefaultTableModel modelStok;

    public Dashboard(MainFrame frame) {
        this.frame = frame;
        setBackground(BG);
        setLayout(new BorderLayout(0, 0));
        buildUI();
        loadData();
    }

    private void buildUI() {
        // ---- Scroll utama ----
        JPanel mainContent = new JPanel();
        mainContent.setBackground(BG);
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // ---- Baris 1: Sub-header ----
        JPanel subHeader = new JPanel(new BorderLayout());
        subHeader.setOpaque(false);
        subHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        subHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        JLabel welcomeLabel = new JLabel("Selamat datang di Nyetok! 👋");
        welcomeLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        welcomeLabel.setForeground(new Color(0x757575));

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionBtns.setOpaque(false);

        JButton btnOrder = createActionButton("+ Buat Order Baru", BIRU_MUDA);
        btnOrder.addActionListener(e -> frame.navigateTo(2));

        JButton btnExport = createActionButton("⬇ Ekspor Excel", HIJAU);
        btnExport.addActionListener(e ->
            JOptionPane.showMessageDialog(frame, "Fitur ekspor Excel akan segera hadir!",
                "Info", JOptionPane.INFORMATION_MESSAGE));

        actionBtns.add(btnOrder);
        actionBtns.add(btnExport);

        subHeader.add(welcomeLabel, BorderLayout.WEST);
        subHeader.add(actionBtns, BorderLayout.EAST);
        mainContent.add(subHeader);

        // ---- Baris 2: 4 Card statistik ----
        JPanel cardsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        cardsRow.setOpaque(false);
        cardsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        lblTotalProduk = new JLabel("0");
        lblStokMasuk   = new JLabel("0");
        lblStokKeluar  = new JLabel("0");
        lblLowStok     = new JLabel("0");

        cardsRow.add(buildStatCard("📦 Total Produk",    lblTotalProduk, BIRU_MUDA, "produk terdaftar"));
        cardsRow.add(buildStatCard("📥 Stok Masuk",      lblStokMasuk,   HIJAU,     "unit masuk hari ini"));
        cardsRow.add(buildStatCard("📤 Stok Keluar",     lblStokKeluar,  ORANYE,    "unit keluar hari ini"));
        cardsRow.add(buildStatCard("⚠ Stok Tipis/Habis", lblLowStok,    MERAH,     "produk perlu restock"));

        mainContent.add(cardsRow);
        mainContent.add(Box.createVerticalStrut(20));

        // ---- Baris 3: Dua tabel ----
        JPanel tablesRow = new JPanel(new GridLayout(1, 2, 16, 0));
        tablesRow.setOpaque(false);

        // Tabel Purchase Orders (stok_masuk terbaru)
        String[] colsPO = {"#", "Produk", "Jumlah", "Harga Satuan", "Tanggal"};
        modelPO = new DefaultTableModel(colsPO, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblPurchaseOrders = createStyledTable(modelPO);
        JPanel panelPO = buildTableCard("📦 Purchase Orders Terbaru",
            tblPurchaseOrders, BIRU_TUA, e -> frame.navigateTo(2));

        // Tabel Stok Realtime
        String[] colsStok = {"Produk", "Merk", "Jumlah", "Status"};
        modelStok = new DefaultTableModel(colsStok, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblStokRealtime = createStyledTable(modelStok);
        // Renderer status berwarna
        tblStokRealtime.getColumnModel().getColumn(3).setCellRenderer(new StatusRenderer());
        JPanel panelStok = buildTableCard("📋 Stok Realtime",
            tblStokRealtime, HIJAU, e -> frame.navigateTo(3));

        tablesRow.add(panelPO);
        tablesRow.add(panelStok);
        mainContent.add(tablesRow);

        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BG);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);
    }

    /** Memuat semua data dari database */
    public void loadData() {
        loadStatistik();
        loadPurchaseOrders();
        loadStokRealtime();
    }

    /** Memuat data statistik kartu */
    private void loadStatistik() {
        Connection conn = Koneksi.getInstance().getConnection();
        if (conn == null) return;

        try {
            // Total produk
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM produk")) {
                if (rs.next()) lblTotalProduk.setText(String.valueOf(rs.getInt(1)));
            }

            // Stok masuk hari ini (jumlah unit)
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(
                     "SELECT COALESCE(SUM(jumlah),0) FROM stok_masuk WHERE DATE(created_at)=CURDATE()")) {
                if (rs.next()) lblStokMasuk.setText(String.valueOf(rs.getInt(1)));
            }

            // Stok keluar hari ini
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(
                     "SELECT COALESCE(SUM(jumlah),0) FROM stok_keluar WHERE DATE(created_at)=CURDATE()")) {
                if (rs.next()) lblStokKeluar.setText(String.valueOf(rs.getInt(1)));
            }

            // Low stok (stok <= 10)
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM stok WHERE jumlah <= 10")) {
                if (rs.next()) lblLowStok.setText(String.valueOf(rs.getInt(1)));
            }

        } catch (SQLException e) {
            System.err.println("[Dashboard] Error loadStatistik: " + e.getMessage());
        }
    }

    /** Memuat 5 purchase orders terbaru dari stok_masuk */
    private void loadPurchaseOrders() {
        modelPO.setRowCount(0);
        Connection conn = Koneksi.getInstance().getConnection();
        if (conn == null) return;

        String sql = "SELECT sm.id_masuk, p.nama, sm.jumlah, sm.harga_satuan, sm.created_at " +
                     "FROM stok_masuk sm LEFT JOIN produk p ON sm.id_produk=p.id_produk " +
                     "ORDER BY sm.created_at DESC LIMIT 5";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            NumberFormat nf = NumberFormat.getNumberInstance(new Locale("id", "ID"));
            int row = 1;
            while (rs.next()) {
                modelPO.addRow(new Object[]{
                    row++,
                    rs.getString("nama") != null ? rs.getString("nama") : "-",
                    rs.getInt("jumlah") + " pcs",
                    "Rp " + nf.format(rs.getDouble("harga_satuan")),
                    rs.getString("created_at") != null ?
                        rs.getString("created_at").substring(0, 10) : "-"
                });
            }
        } catch (SQLException e) {
            System.err.println("[Dashboard] Error loadPurchaseOrders: " + e.getMessage());
        }
    }

    /** Memuat 5 stok realtime terbaru */
    private void loadStokRealtime() {
        modelStok.setRowCount(0);
        Connection conn = Koneksi.getInstance().getConnection();
        if (conn == null) return;

        String sql = "SELECT p.nama, p.merk, s.jumlah " +
                     "FROM stok s LEFT JOIN produk p ON s.id_produk=p.id_produk " +
                     "ORDER BY s.updated_at DESC LIMIT 5";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int jml = rs.getInt("jumlah");
                String status = jml <= 0 ? "Habis" : jml <= 10 ? "Stok Tipis" : "Aman";
                modelStok.addRow(new Object[]{
                    rs.getString("nama") != null ? rs.getString("nama") : "-",
                    rs.getString("merk") != null ? rs.getString("merk") : "-",
                    jml + " pcs",
                    status
                });
            }
        } catch (SQLException e) {
            System.err.println("[Dashboard] Error loadStokRealtime: " + e.getMessage());
        }
    }

    // ---- Helper: Komponen UI ----

    /** Membangun kartu statistik */
    private JPanel buildStatCard(String title, JLabel valueLabel, Color accentColor, String subtitle) {
        MainFrame.RoundedPanel card = new MainFrame.RoundedPanel(12, PUTIH);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // Strip warna di kiri
        JPanel strip = new JPanel();
        strip.setBackground(accentColor);
        strip.setPreferredSize(new Dimension(4, 0));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Arial", Font.PLAIN, 11));
        titleLbl.setForeground(new Color(0x757575));

        valueLabel.setFont(new Font("Arial", Font.BOLD, 32));
        valueLabel.setForeground(accentColor);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(new Font("Arial", Font.PLAIN, 10));
        subLbl.setForeground(new Color(0xBDBDBD));

        content.add(titleLbl);
        content.add(Box.createVerticalStrut(4));
        content.add(valueLabel);
        content.add(subLbl);

        card.add(strip, BorderLayout.WEST);
        card.add(content, BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    /** Membangun panel kartu tabel */
    private JPanel buildTableCard(String title, JTable table, Color headerColor,
                                   ActionListener lihatSemua) {
        MainFrame.RoundedPanel card = new MainFrame.RoundedPanel(12, PUTIH);
        card.setLayout(new BorderLayout());

        // Header kartu
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(14, 16, 10, 16));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Arial", Font.BOLD, 13));
        titleLbl.setForeground(TEKS);

        JButton btnLihat = new JButton("Lihat Semua →");
        btnLihat.setFont(new Font("Arial", Font.PLAIN, 11));
        btnLihat.setForeground(BIRU_MUDA);
        btnLihat.setBorderPainted(false);
        btnLihat.setContentAreaFilled(false);
        btnLihat.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLihat.addActionListener(lihatSemua);

        header.add(titleLbl, BorderLayout.WEST);
        header.add(btnLihat, BorderLayout.EAST);

        // Scroll tabel
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(PUTIH);

        card.add(header, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    /** Membuat JTable dengan styling */
    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setRowHeight(32);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBackground(PUTIH);
        table.setForeground(TEKS);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
        table.getTableHeader().setBackground(new Color(0xF5F7FA));
        table.getTableHeader().setForeground(new Color(0x616161));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE0E0E0)));
        table.setSelectionBackground(new Color(0xE3F2FD));
        table.setSelectionForeground(TEKS);

        // Row striping
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                            boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                if (sel) {
                    setBackground(new Color(0xE3F2FD));
                } else {
                    setBackground(row % 2 == 0 ? PUTIH : new Color(0xFAFAFA));
                }
                return this;
            }
        });
        return table;
    }

    /** Membuat tombol aksi */
    private JButton createActionButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? bg.darker() :
                            getModel().isRollover() ? bg.brighter() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setForeground(PUTIH);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(160, 36));
        return btn;
    }

    // ---- Renderer Status berwarna ----
    static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                        boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            String val = v != null ? v.toString() : "";
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            switch (val) {
                case "Aman"      -> { setForeground(new Color(0x2E7D32)); setBackground(new Color(0xE8F5E9)); }
                case "Stok Tipis"-> { setForeground(new Color(0xE65100)); setBackground(new Color(0xFFF3E0)); }
                case "Habis"     -> { setForeground(new Color(0xC62828)); setBackground(new Color(0xFFEBEE)); }
                default          -> { setForeground(MainFrame.TEKS_GELAP); setBackground(Color.WHITE); }
            }
            if (sel) setBackground(new Color(0xE3F2FD));
            return this;
        }
    }
}
