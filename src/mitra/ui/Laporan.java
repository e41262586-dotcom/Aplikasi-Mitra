package mitra.ui;

import mitra.koneksi.Koneksi;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;

/**
 * Laporan - Panel laporan dan summary stok
 * Menampilkan summary stok masuk vs keluar dengan filter bulan/tahun
 */
public class Laporan extends JPanel {

    private final MainFrame frame;

    private static final Color BG    = MainFrame.BG_KONTEN;
    private static final Color PUTIH = MainFrame.PUTIH;
    private static final Color BIRU  = MainFrame.BIRU_MUDA;
    private static final Color TEKS  = MainFrame.TEKS_GELAP;
    private static final Color HIJAU = MainFrame.HIJAU;
    private static final Color MERAH = MainFrame.MERAH;

    // Filter
    private JComboBox<String> cbBulan;
    private JComboBox<String> cbTahun;

    // Kartu summary
    private JLabel lblTotalMasuk;
    private JLabel lblTotalKeluar;
    private JLabel lblSelisih;
    private JLabel lblNilaiMasuk;

    // Tabel laporan gabungan
    private JTable            tblLaporan;
    private DefaultTableModel modelLaporan;

    private static final String[] BULAN_NAMES = {
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    };

    public Laporan(MainFrame frame) {
        this.frame = frame;
        setBackground(BG);
        setLayout(new BorderLayout(0, 0));
        buildUI();
        loadData();
    }

    private void buildUI() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 16));
        wrapper.setBackground(BG);
        wrapper.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // ---- Filter bar ----
        JPanel filterBar = buildFilterBar();
        wrapper.add(filterBar, BorderLayout.NORTH);

        // ---- Konten ----
        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setOpaque(false);

        // Kartu summary
        JPanel cardsRow = buildSummaryCards();
        cardsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        content.add(cardsRow, BorderLayout.NORTH);

        // Tabel laporan
        JPanel tablePanel = buildTablePanel();
        content.add(tablePanel, BorderLayout.CENTER);

        wrapper.add(content, BorderLayout.CENTER);

        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(BG);
        add(scroll, BorderLayout.CENTER);
    }

    /** Bar filter bulan/tahun */
    private JPanel buildFilterBar() {
        MainFrame.RoundedPanel bar = new MainFrame.RoundedPanel(10, PUTIH);
        bar.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 10));

        JLabel lbl = new JLabel("📈 Laporan Periode:");
        lbl.setFont(new Font("Arial", Font.BOLD, 13));
        lbl.setForeground(TEKS);

        cbBulan = new JComboBox<>(BULAN_NAMES);
        cbBulan.setFont(new Font("Arial", Font.PLAIN, 12));
        cbBulan.setSelectedIndex(LocalDate.now().getMonthValue() - 1);
        cbBulan.setPreferredSize(new Dimension(130, 32));

        cbTahun = new JComboBox<>();
        cbTahun.setFont(new Font("Arial", Font.PLAIN, 12));
        cbTahun.setPreferredSize(new Dimension(90, 32));
        int currentYear = LocalDate.now().getYear();
        for (int y = currentYear; y >= currentYear - 5; y--) {
            cbTahun.addItem(String.valueOf(y));
        }

        JButton btnFilter = createSmallButton("Tampilkan", BIRU);
        btnFilter.addActionListener(e -> loadData());

        JButton btnExport = createSmallButton("⬇ Ekspor", new Color(0x757575));
        btnExport.addActionListener(e ->
            JOptionPane.showMessageDialog(frame,
                "Fitur ekspor laporan akan hadir segera!", "Info",
                JOptionPane.INFORMATION_MESSAGE));

        bar.add(lbl);
        bar.add(new JLabel("Bulan:"));
        bar.add(cbBulan);
        bar.add(new JLabel("Tahun:"));
        bar.add(cbTahun);
        bar.add(btnFilter);
        bar.add(btnExport);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(bar, BorderLayout.CENTER);
        return wrapper;
    }

    /** Panel 4 kartu summary */
    private JPanel buildSummaryCards() {
        JPanel row = new JPanel(new GridLayout(1, 4, 14, 0));
        row.setOpaque(false);

        lblTotalMasuk  = new JLabel("0");
        lblTotalKeluar = new JLabel("0");
        lblSelisih     = new JLabel("0");
        lblNilaiMasuk  = new JLabel("Rp 0");

        row.add(buildCard("📥 Total Masuk (Pcs)", lblTotalMasuk,  HIJAU,  "unit masuk periode ini"));
        row.add(buildCard("📤 Total Keluar (Pcs)", lblTotalKeluar, MERAH,  "unit keluar periode ini"));
        row.add(buildCard("📊 Selisih (Pcs)",      lblSelisih,     BIRU,   "saldo stok periode ini"));
        row.add(buildCard("💰 Nilai Masuk",        lblNilaiMasuk,  MainFrame.ORANYE, "estimasi nilai barang"));

        return row;
    }

    /** Kartu summary individual */
    private JPanel buildCard(String title, JLabel valueLabel, Color color, String subtitle) {
        MainFrame.RoundedPanel card = new MainFrame.RoundedPanel(10, PUTIH);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JPanel strip = new JPanel();
        strip.setBackground(color);
        strip.setPreferredSize(new Dimension(4, 0));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Arial", Font.PLAIN, 10));
        titleLbl.setForeground(new Color(0x757575));

        valueLabel.setFont(new Font("Arial", Font.BOLD, 24));
        valueLabel.setForeground(color);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(new Font("Arial", Font.PLAIN, 10));
        subLbl.setForeground(new Color(0xBDBDBD));

        content.add(titleLbl);
        content.add(Box.createVerticalStrut(2));
        content.add(valueLabel);
        content.add(subLbl);

        card.add(strip, BorderLayout.WEST);
        card.add(content, BorderLayout.CENTER);

        JPanel w = new JPanel(new BorderLayout());
        w.setOpaque(false);
        w.add(card, BorderLayout.CENTER);
        return w;
    }

    /** Panel tabel laporan */
    private JPanel buildTablePanel() {
        MainFrame.RoundedPanel card = new MainFrame.RoundedPanel(12, PUTIH);
        card.setLayout(new BorderLayout());

        // Header tabel
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(14, 16, 10, 16));
        JLabel title = new JLabel("Rincian Transaksi Stok");
        title.setFont(new Font("Arial", Font.BOLD, 13));
        title.setForeground(TEKS);
        header.add(title, BorderLayout.WEST);

        // Tabel
        String[] cols = {"Tanggal", "Produk", "Jenis Transaksi", "Jumlah (Pcs)",
                         "Harga Satuan", "Total Nilai", "Keterangan"};
        modelLaporan = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblLaporan = new JTable(modelLaporan);
        tblLaporan.setFont(new Font("Arial", Font.PLAIN, 12));
        tblLaporan.setRowHeight(32);
        tblLaporan.setShowGrid(false);
        tblLaporan.setBackground(PUTIH);
        tblLaporan.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
        tblLaporan.getTableHeader().setBackground(new Color(0xF5F7FA));
        tblLaporan.setSelectionBackground(new Color(0xE3F2FD));

        // Renderer jenis transaksi berwarna
        tblLaporan.getColumnModel().getColumn(2).setCellRenderer(new JenisCellRenderer());

        // Default renderer dengan striping
        tblLaporan.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                            boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                setBackground(sel ? new Color(0xE3F2FD) : (row % 2 == 0 ? PUTIH : new Color(0xFAFAFA)));
                return this;
            }
        });
        // Set kembali renderer kolom 2 setelah default
        tblLaporan.getColumnModel().getColumn(2).setCellRenderer(new JenisCellRenderer());

        // Lebar kolom
        int[] widths = {100, 180, 110, 90, 120, 130, 160};
        for (int i = 0; i < widths.length; i++) {
            tblLaporan.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        JScrollPane scroll = new JScrollPane(tblLaporan);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(PUTIH);

        card.add(header, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    /** Memuat data laporan dari DB */
    public void loadData() {
        int bulan  = cbBulan.getSelectedIndex() + 1;
        int tahun  = Integer.parseInt((String) cbTahun.getSelectedItem());

        loadSummary(bulan, tahun);
        loadRincian(bulan, tahun);
    }

    /** Memuat data summary kartu */
    private void loadSummary(int bulan, int tahun) {
        Connection conn = Koneksi.getInstance().getConnection();
        if (conn == null) return;

        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("id", "ID"));

        try {
            // Total masuk
            long totalMasuk = 0;
            double nilaiMasuk = 0;
            try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COALESCE(SUM(jumlah),0), COALESCE(SUM(jumlah*harga_satuan),0) " +
                "FROM stok_masuk WHERE MONTH(tanggal)=? AND YEAR(tanggal)=?")) {
                ps.setInt(1, bulan); ps.setInt(2, tahun);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) { totalMasuk = rs.getLong(1); nilaiMasuk = rs.getDouble(2); }
            }

            // Total keluar
            long totalKeluar = 0;
            try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COALESCE(SUM(jumlah),0) FROM stok_keluar WHERE MONTH(tanggal)=? AND YEAR(tanggal)=?")) {
                ps.setInt(1, bulan); ps.setInt(2, tahun);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) totalKeluar = rs.getLong(1);
            }

            lblTotalMasuk.setText(String.valueOf(totalMasuk));
            lblTotalKeluar.setText(String.valueOf(totalKeluar));
            lblSelisih.setText(String.valueOf(totalMasuk - totalKeluar));
            lblNilaiMasuk.setText("Rp " + nf.format(nilaiMasuk));

            // Warna selisih
            long selisih = totalMasuk - totalKeluar;
            lblSelisih.setForeground(selisih >= 0 ? HIJAU : MERAH);

        } catch (SQLException e) {
            System.err.println("[Laporan] Error loadSummary: " + e.getMessage());
        }
    }

    /** Memuat rincian transaksi ke tabel */
    private void loadRincian(int bulan, int tahun) {
        modelLaporan.setRowCount(0);
        Connection conn = Koneksi.getInstance().getConnection();
        if (conn == null) return;

        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("id", "ID"));

        // Stok masuk
        String sqlMasuk = "SELECT sm.tanggal, p.nama, sm.jumlah, sm.harga_satuan, sm.keterangan " +
                          "FROM stok_masuk sm LEFT JOIN produk p ON sm.id_produk=p.id_produk " +
                          "WHERE MONTH(sm.tanggal)=? AND YEAR(sm.tanggal)=? ORDER BY sm.tanggal";
        try (PreparedStatement ps = conn.prepareStatement(sqlMasuk)) {
            ps.setInt(1, bulan); ps.setInt(2, tahun);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                double harga = rs.getDouble("harga_satuan");
                int jml = rs.getInt("jumlah");
                modelLaporan.addRow(new Object[]{
                    rs.getString("tanggal"),
                    rs.getString("nama") != null ? rs.getString("nama") : "-",
                    "Stok Masuk",
                    jml + " pcs",
                    "Rp " + nf.format(harga),
                    "Rp " + nf.format(harga * jml),
                    rs.getString("keterangan") != null ? rs.getString("keterangan") : "-"
                });
            }
        } catch (SQLException e) {
            System.err.println("[Laporan] Error stok_masuk: " + e.getMessage());
        }

        // Stok keluar
        String sqlKeluar = "SELECT sk.tanggal, p.nama, sk.jumlah, sk.tujuan, sk.keterangan " +
                           "FROM stok_keluar sk LEFT JOIN produk p ON sk.id_produk=p.id_produk " +
                           "WHERE MONTH(sk.tanggal)=? AND YEAR(sk.tanggal)=? ORDER BY sk.tanggal";
        try (PreparedStatement ps = conn.prepareStatement(sqlKeluar)) {
            ps.setInt(1, bulan); ps.setInt(2, tahun);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                modelLaporan.addRow(new Object[]{
                    rs.getString("tanggal"),
                    rs.getString("nama") != null ? rs.getString("nama") : "-",
                    "Stok Keluar",
                    rs.getInt("jumlah") + " pcs",
                    "-",
                    "-",
                    rs.getString("tujuan") != null ?
                        "Ke: " + rs.getString("tujuan") : rs.getString("keterangan")
                });
            }
        } catch (SQLException e) {
            System.err.println("[Laporan] Error stok_keluar: " + e.getMessage());
        }
    }

    // ---- Helper ----

    private JButton createSmallButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? bg.darker() :
                            getModel().isRollover() ? bg.brighter() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Arial", Font.BOLD, 11));
        btn.setForeground(PUTIH);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(110, 32));
        return btn;
    }

    /** Renderer untuk kolom Jenis Transaksi */
    static class JenisCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                        boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            String val = v != null ? v.toString() : "";
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            switch (val) {
                case "Stok Masuk"  -> { setForeground(new Color(0x2E7D32)); setBackground(new Color(0xE8F5E9)); }
                case "Stok Keluar" -> { setForeground(new Color(0xC62828)); setBackground(new Color(0xFFEBEE)); }
                default            -> { setForeground(MainFrame.TEKS_GELAP); setBackground(Color.WHITE); }
            }
            if (sel) setBackground(new Color(0xE3F2FD));
            return this;
        }
    }
}
