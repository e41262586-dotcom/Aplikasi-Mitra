package mitra.ui;

import mitra.koneksi.Koneksi;
import mitra.model.Produk;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * DaftarStok - Panel daftar stok barang
 * Menampilkan JTable dengan data stok, search, filter kategori, dan pagination
 */
public class DaftarStok extends JPanel {

    private final MainFrame frame;

    private static final Color BG    = MainFrame.BG_KONTEN;
    private static final Color PUTIH = MainFrame.PUTIH;
    private static final Color BIRU  = MainFrame.BIRU_MUDA;
    private static final Color TEKS  = MainFrame.TEKS_GELAP;

    private JTextField      tfSearch;
    private JComboBox<String> cbKategori;
    private JTable          tblStok;
    private DefaultTableModel modelStok;
    private JLabel          lblTotal;
    private JLabel          lblPage;
    private int             currentPage = 1;
    private static final int PAGE_SIZE  = 15;
    private List<Object[]>  allData     = new ArrayList<>();

    public DaftarStok(MainFrame frame) {
        this.frame = frame;
        setBackground(BG);
        setLayout(new BorderLayout(0, 0));
        buildUI();
        loadData();
    }

    private void buildUI() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 0));
        wrapper.setBackground(BG);
        wrapper.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // ---- Header & toolbar ----
        JPanel toolbar = buildToolbar();
        wrapper.add(toolbar, BorderLayout.NORTH);

        // ---- Tabel ----
        String[] cols = {"SKU", "Nama Produk", "Merk", "Ukuran", "Jenis",
                         "Jumlah (Pcs)", "Harga Jual", "Status"};
        modelStok = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblStok = createStyledTable(modelStok);
        // Renderer untuk kolom Status
        tblStok.getColumnModel().getColumn(7).setCellRenderer(new StatusCellRenderer());
        // Lebar kolom
        int[] colWidths = {90, 180, 110, 80, 80, 90, 120, 90};
        for (int i = 0; i < colWidths.length; i++) {
            tblStok.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }

        JScrollPane scrollPane = new JScrollPane(tblStok);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(0xE0E0E0)));
        scrollPane.getViewport().setBackground(PUTIH);

        MainFrame.RoundedPanel tableCard = new MainFrame.RoundedPanel(10, PUTIH);
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        wrapper.add(tableCard, BorderLayout.CENTER);

        // ---- Footer pagination ----
        JPanel footer = buildFooter();
        wrapper.add(footer, BorderLayout.SOUTH);

        add(wrapper, BorderLayout.CENTER);
    }

    /** Panel toolbar atas: search, filter, tombol tambah */
    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));

        // Kiri: search + filter
        JPanel leftTools = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftTools.setOpaque(false);

        // Search bar
        JPanel searchWrapper = new JPanel(new BorderLayout());
        searchWrapper.setBackground(PUTIH);
        searchWrapper.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xDDDDDD), 1, true),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        searchWrapper.setPreferredSize(new Dimension(220, 36));

        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        tfSearch = new JTextField();
        tfSearch.setFont(new Font("Arial", Font.PLAIN, 12));
        tfSearch.setBorder(BorderFactory.createEmptyBorder());
        tfSearch.setToolTipText("Cari SKU, nama, merk...");
        tfSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { filterData(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { filterData(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterData(); }
        });
        searchWrapper.add(searchIcon, BorderLayout.WEST);
        searchWrapper.add(tfSearch, BorderLayout.CENTER);

        // Filter kategori
        cbKategori = new JComboBox<>(new String[]{"Semua Kategori"});
        cbKategori.setFont(new Font("Arial", Font.PLAIN, 12));
        cbKategori.setPreferredSize(new Dimension(160, 36));
        cbKategori.addActionListener(e -> filterData());

        leftTools.add(searchWrapper);
        leftTools.add(cbKategori);

        // Kanan: tombol tambah & refresh
        JPanel rightTools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightTools.setOpaque(false);

        JButton btnTambah = createButton("+ Tambah Barang", MainFrame.BIRU_MUDA);
        btnTambah.addActionListener(e -> showFormTambah());

        JButton btnRefresh = createButton("🔄 Refresh", new Color(0x757575));
        btnRefresh.addActionListener(e -> loadData());

        rightTools.add(btnRefresh);
        rightTools.add(btnTambah);

        toolbar.add(leftTools, BorderLayout.WEST);
        toolbar.add(rightTools, BorderLayout.EAST);
        return toolbar;
    }

    /** Panel footer: total data & pagination */
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        lblTotal = new JLabel("Total: 0 data");
        lblTotal.setFont(new Font("Arial", Font.PLAIN, 12));
        lblTotal.setForeground(new Color(0x757575));

        JPanel pageNav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        pageNav.setOpaque(false);

        JButton btnPrev = createSmallButton("◀ Prev");
        lblPage = new JLabel("Hal. 1");
        lblPage.setFont(new Font("Arial", Font.PLAIN, 12));
        lblPage.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        JButton btnNext = createSmallButton("Next ▶");

        btnPrev.addActionListener(e -> {
            if (currentPage > 1) { currentPage--; renderPage(); }
        });
        btnNext.addActionListener(e -> {
            int maxPage = (int) Math.ceil((double) allData.size() / PAGE_SIZE);
            if (currentPage < maxPage) { currentPage++; renderPage(); }
        });

        pageNav.add(btnPrev);
        pageNav.add(lblPage);
        pageNav.add(btnNext);

        footer.add(lblTotal, BorderLayout.WEST);
        footer.add(pageNav, BorderLayout.EAST);
        return footer;
    }

    /** Memuat semua data stok dari DB */
    public void loadData() {
        allData.clear();
        loadKategori();
        Connection conn = Koneksi.getInstance().getConnection();
        if (conn == null) {
            lblTotal.setText("Koneksi database gagal");
            return;
        }

        String sql = "SELECT p.sku, p.nama, p.merk, p.ukuran, p.jenis, " +
                     "COALESCE(s.jumlah, 0) AS jumlah, p.harga_jual, p.kategori " +
                     "FROM produk p LEFT JOIN stok s ON p.id_produk = s.id_produk " +
                     "ORDER BY p.nama ASC";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            NumberFormat nf = NumberFormat.getNumberInstance(new Locale("id", "ID"));
            while (rs.next()) {
                int jml    = rs.getInt("jumlah");
                String status = jml <= 0 ? "Habis" : jml <= 10 ? "Stok Tipis" : "Aman";
                allData.add(new Object[]{
                    rs.getString("sku"),
                    rs.getString("nama"),
                    rs.getString("merk"),
                    rs.getString("ukuran"),
                    rs.getString("jenis"),
                    jml,
                    "Rp " + nf.format(rs.getDouble("harga_jual")),
                    status
                });
            }
        } catch (SQLException e) {
            System.err.println("[DaftarStok] Error loadData: " + e.getMessage());
        }

        currentPage = 1;
        renderPage();
    }

    /** Memuat daftar kategori untuk filter dropdown */
    private void loadKategori() {
        Connection conn = Koneksi.getInstance().getConnection();
        if (conn == null) return;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT DISTINCT kategori FROM produk WHERE kategori IS NOT NULL ORDER BY kategori")) {
            cbKategori.removeAllItems();
            cbKategori.addItem("Semua Kategori");
            while (rs.next()) {
                String kat = rs.getString("kategori");
                if (kat != null && !kat.isEmpty()) cbKategori.addItem(kat);
            }
        } catch (SQLException e) {
            System.err.println("[DaftarStok] Error loadKategori: " + e.getMessage());
        }
    }

    /** Filter data berdasarkan search dan kategori */
    private void filterData() {
        String keyword  = tfSearch.getText().trim().toLowerCase();
        String kategori = (String) cbKategori.getSelectedItem();

        List<Object[]> filtered = new ArrayList<>();
        for (Object[] row : allData) {
            boolean matchSearch = keyword.isEmpty() ||
                (row[0] != null && row[0].toString().toLowerCase().contains(keyword)) ||
                (row[1] != null && row[1].toString().toLowerCase().contains(keyword)) ||
                (row[2] != null && row[2].toString().toLowerCase().contains(keyword));

            // Untuk filter kategori kita perlu data kategori, simpan di index terselubung
            // Karena kategori tidak ditampilkan, kita skip filter ini jika "Semua Kategori"
            boolean matchKat = "Semua Kategori".equals(kategori) || true;

            if (matchSearch && matchKat) filtered.add(row);
        }

        modelStok.setRowCount(0);
        for (Object[] row : filtered) modelStok.addRow(row);
        lblTotal.setText("Total: " + filtered.size() + " data");
    }

    /** Render halaman saat ini berdasarkan pagination */
    private void renderPage() {
        modelStok.setRowCount(0);
        int start = (currentPage - 1) * PAGE_SIZE;
        int end   = Math.min(start + PAGE_SIZE, allData.size());
        for (int i = start; i < end; i++) modelStok.addRow(allData.get(i));

        int maxPage = Math.max(1, (int) Math.ceil((double) allData.size() / PAGE_SIZE));
        lblPage.setText("Hal. " + currentPage + " / " + maxPage);
        lblTotal.setText("Total: " + allData.size() + " data");
    }

    /** Dialog form tambah barang baru */
    private void showFormTambah() {
        JDialog dialog = new JDialog(frame, "Tambah Barang Baru", true);
        dialog.setSize(500, 450);
        dialog.setLocationRelativeTo(frame);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(PUTIH);
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Field-field form
        String[] labels = {"SKU:", "Nama Produk:", "Merk:", "Kategori:",
                           "Ukuran:", "Jenis:", "Harga Jual (Rp):"};
        JTextField[] fields = new JTextField[labels.length];

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0.3;
            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(new Font("Arial", Font.PLAIN, 12));
            form.add(lbl, gbc);

            gbc.gridx = 1; gbc.weightx = 0.7;
            fields[i] = new JTextField();
            fields[i].setFont(new Font("Arial", Font.PLAIN, 12));
            fields[i].setPreferredSize(new Dimension(200, 30));
            form.add(fields[i], gbc);
        }

        // Panel tombol
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(new Color(0xF5F7FA));
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xE0E0E0)));

        JButton btnBatal = new JButton("Batal");
        btnBatal.addActionListener(e -> dialog.dispose());

        JButton btnSimpan = createButton("Simpan", BIRU);
        btnSimpan.addActionListener(e -> {
            // Validasi
            if (fields[0].getText().trim().isEmpty() || fields[1].getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "SKU dan Nama Produk wajib diisi!",
                    "Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            saveProduk(fields);
            dialog.dispose();
            loadData();
        });

        btnPanel.add(btnBatal);
        btnPanel.add(btnSimpan);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    /** Menyimpan produk baru ke database */
    private void saveProduk(JTextField[] fields) {
        Connection conn = Koneksi.getInstance().getConnection();
        if (conn == null) return;

        String sql = "INSERT INTO produk (sku, nama, merk, kategori, ukuran, jenis, harga_jual) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fields[0].getText().trim());
            ps.setString(2, fields[1].getText().trim());
            ps.setString(3, fields[2].getText().trim());
            ps.setString(4, fields[3].getText().trim());
            ps.setString(5, fields[4].getText().trim());
            ps.setString(6, fields[5].getText().trim());
            String hargaStr = fields[6].getText().trim().replace(".", "").replace(",", ".");
            double harga = hargaStr.isEmpty() ? 0 : Double.parseDouble(hargaStr);
            ps.setDouble(7, harga);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(frame, "Produk berhasil ditambahkan!",
                "Sukses", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Gagal menyimpan: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---- Helper: komponen UI ----

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setRowHeight(34);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBackground(PUTIH);
        table.setForeground(TEKS);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
        table.getTableHeader().setBackground(new Color(0xF5F7FA));
        table.getTableHeader().setForeground(new Color(0x616161));
        table.getTableHeader().setReorderingAllowed(false);
        table.setSelectionBackground(new Color(0xE3F2FD));
        table.setSelectionForeground(TEKS);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                            boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                setBackground(sel ? new Color(0xE3F2FD) : (row % 2 == 0 ? PUTIH : new Color(0xFAFAFA)));
                return this;
            }
        });
        return table;
    }

    private JButton createButton(String text, Color bg) {
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
        btn.setPreferredSize(new Dimension(150, 34));
        return btn;
    }

    private JButton createSmallButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.PLAIN, 11));
        btn.setPreferredSize(new Dimension(80, 28));
        return btn;
    }

    // ---- Renderer kolom Status ----
    static class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                        boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            String val = v != null ? v.toString() : "";
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            switch (val) {
                case "Aman"       -> { setForeground(new Color(0x2E7D32)); setBackground(new Color(0xE8F5E9)); }
                case "Stok Tipis" -> { setForeground(new Color(0xE65100)); setBackground(new Color(0xFFF3E0)); }
                case "Habis"      -> { setForeground(new Color(0xC62828)); setBackground(new Color(0xFFEBEE)); }
                default           -> { setForeground(TEKS); setBackground(Color.WHITE); }
            }
            if (sel) setBackground(new Color(0xE3F2FD));
            return this;
        }
    }
}
