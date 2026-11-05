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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * StokKeluar - Panel input dan riwayat stok keluar
 * Form input stok keluar dan tabel riwayat dengan filter tanggal
 */
public class StokKeluar extends JPanel {

    private final MainFrame frame;

    private static final Color BG    = MainFrame.BG_KONTEN;
    private static final Color PUTIH = MainFrame.PUTIH;
    private static final Color BIRU  = MainFrame.BIRU_MUDA;
    private static final Color TEKS  = MainFrame.TEKS_GELAP;
    private static final Color MERAH = MainFrame.MERAH;

    // Form fields
    private JComboBox<String> cbProduk;
    private JTextField        tfJumlah;
    private JTextField        tfTujuan;
    private JTextField        tfKeterangan;
    private JTextField        tfTanggal;

    // Filter tanggal
    private JTextField tfFilterDari;
    private JTextField tfFilterSampai;

    // Tabel riwayat
    private JTable            tblRiwayat;
    private DefaultTableModel modelRiwayat;

    // Daftar produk
    private List<Produk> listProduk = new ArrayList<>();

    public StokKeluar(MainFrame frame) {
        this.frame = frame;
        setBackground(BG);
        setLayout(new BorderLayout(0, 0));
        buildUI();
        loadProduk();
        loadData();
    }

    private void buildUI() {
        JPanel wrapper = new JPanel(new BorderLayout(16, 0));
        wrapper.setBackground(BG);
        wrapper.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Kiri: form
        JPanel leftPanel = buildFormPanel();
        leftPanel.setPreferredSize(new Dimension(360, 0));

        // Kanan: riwayat
        JPanel rightPanel = buildRiwayatPanel();

        wrapper.add(leftPanel, BorderLayout.WEST);
        wrapper.add(rightPanel, BorderLayout.CENTER);

        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(BG);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel buildFormPanel() {
        MainFrame.RoundedPanel card = new MainFrame.RoundedPanel(12, PUTIH);
        card.setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(14, 16, 0, 16));
        JLabel title = new JLabel("📤 Form Stok Keluar");
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setForeground(TEKS);
        header.add(title);

        // Form body
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(16, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);

        // Tanggal
        addRow(body, gbc, 0, "Tanggal *",
            tfTanggal = createField(LocalDate.now().toString()));
        tfTanggal.setEditable(false);
        tfTanggal.setBackground(new Color(0xF5F7FA));

        // Produk
        addRow(body, gbc, 1, "Produk *",
            cbProduk = new JComboBox<>());
        cbProduk.setFont(new Font("Arial", Font.PLAIN, 12));
        cbProduk.setPreferredSize(new Dimension(280, 34));

        // Jumlah
        addRow(body, gbc, 2, "Jumlah (Pcs) *",
            tfJumlah = createField("Masukkan jumlah..."));

        // Tujuan
        addRow(body, gbc, 3, "Tujuan / Penerima",
            tfTujuan = createField("Nama tujuan/penerima..."));

        // Keterangan
        addRow(body, gbc, 4, "Keterangan",
            tfKeterangan = createField("Catatan tambahan (opsional)"));

        // Tombol aksi
        gbc.gridy = 5; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(16, 0, 0, 0);
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setOpaque(false);

        JButton btnSimpan = createButton("💾 Simpan Keluar", MERAH);
        btnSimpan.addActionListener(e -> simpanStokKeluar());

        JButton btnReset = new JButton("Reset");
        btnReset.setFont(new Font("Arial", Font.PLAIN, 12));
        btnReset.setPreferredSize(new Dimension(80, 36));
        btnReset.addActionListener(e -> resetForm());

        btnRow.add(btnSimpan);
        btnRow.add(btnReset);
        body.add(btnRow, gbc);

        card.add(header, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 16));
        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildRiwayatPanel() {
        MainFrame.RoundedPanel card = new MainFrame.RoundedPanel(12, PUTIH);
        card.setLayout(new BorderLayout());

        // Header + filter
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(14, 16, 10, 16));

        JLabel title = new JLabel("🔄 Riwayat Stok Keluar");
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setForeground(TEKS);

        // Filter tanggal
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        filterPanel.setOpaque(false);

        JLabel lblDari   = new JLabel("Dari:");
        lblDari.setFont(new Font("Arial", Font.PLAIN, 11));
        tfFilterDari = new JTextField(LocalDate.now().withDayOfMonth(1).toString(), 10);
        tfFilterDari.setFont(new Font("Arial", Font.PLAIN, 11));

        JLabel lblSampai  = new JLabel("s/d:");
        lblSampai.setFont(new Font("Arial", Font.PLAIN, 11));
        tfFilterSampai = new JTextField(LocalDate.now().toString(), 10);
        tfFilterSampai.setFont(new Font("Arial", Font.PLAIN, 11));

        JButton btnFilter = new JButton("Filter");
        btnFilter.setFont(new Font("Arial", Font.PLAIN, 11));
        btnFilter.setPreferredSize(new Dimension(60, 28));
        btnFilter.addActionListener(e -> loadData());

        JButton btnReset2 = new JButton("Reset");
        btnReset2.setFont(new Font("Arial", Font.PLAIN, 11));
        btnReset2.setPreferredSize(new Dimension(60, 28));
        btnReset2.addActionListener(e -> {
            tfFilterDari.setText(LocalDate.now().withDayOfMonth(1).toString());
            tfFilterSampai.setText(LocalDate.now().toString());
            loadData();
        });

        filterPanel.add(lblDari);
        filterPanel.add(tfFilterDari);
        filterPanel.add(lblSampai);
        filterPanel.add(tfFilterSampai);
        filterPanel.add(btnFilter);
        filterPanel.add(btnReset2);

        header.add(title, BorderLayout.WEST);
        header.add(filterPanel, BorderLayout.EAST);

        // Tabel
        String[] cols = {"#", "Tanggal", "Produk", "Jumlah", "Tujuan", "Keterangan"};
        modelRiwayat = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblRiwayat = new JTable(modelRiwayat);
        tblRiwayat.setFont(new Font("Arial", Font.PLAIN, 12));
        tblRiwayat.setRowHeight(32);
        tblRiwayat.setShowGrid(false);
        tblRiwayat.setBackground(PUTIH);
        tblRiwayat.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
        tblRiwayat.getTableHeader().setBackground(new Color(0xF5F7FA));
        tblRiwayat.setSelectionBackground(new Color(0xFFEBEE));
        tblRiwayat.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                            boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                setBackground(sel ? new Color(0xFFEBEE) : (row % 2 == 0 ? PUTIH : new Color(0xFAFAFA)));
                return this;
            }
        });

        // Lebar kolom
        tblRiwayat.getColumnModel().getColumn(0).setPreferredWidth(40);
        tblRiwayat.getColumnModel().getColumn(1).setPreferredWidth(100);
        tblRiwayat.getColumnModel().getColumn(2).setPreferredWidth(180);
        tblRiwayat.getColumnModel().getColumn(3).setPreferredWidth(80);
        tblRiwayat.getColumnModel().getColumn(4).setPreferredWidth(120);
        tblRiwayat.getColumnModel().getColumn(5).setPreferredWidth(150);

        JScrollPane scroll = new JScrollPane(tblRiwayat);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(PUTIH);

        card.add(header, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    /** Memuat daftar produk ke ComboBox */
    private void loadProduk() {
        listProduk.clear();
        cbProduk.removeAllItems();
        cbProduk.addItem("-- Pilih Produk --");
        Connection conn = Koneksi.getInstance().getConnection();
        if (conn == null) return;

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT p.id_produk, p.sku, p.nama, COALESCE(s.jumlah,0) AS stok " +
                 "FROM produk p LEFT JOIN stok s ON p.id_produk=s.id_produk ORDER BY p.nama")) {
            while (rs.next()) {
                Produk p = new Produk();
                p.setIdProduk(rs.getInt("id_produk"));
                p.setSku(rs.getString("sku"));
                p.setNama(rs.getString("nama"));
                listProduk.add(p);
                cbProduk.addItem("[" + rs.getString("sku") + "] " + rs.getString("nama")
                    + "  (Stok: " + rs.getInt("stok") + ")");
            }
        } catch (SQLException e) {
            System.err.println("[StokKeluar] Error loadProduk: " + e.getMessage());
        }
    }

    /** Memuat riwayat stok keluar dari DB */
    public void loadData() {
        modelRiwayat.setRowCount(0);
        Connection conn = Koneksi.getInstance().getConnection();
        if (conn == null) return;

        String dari   = tfFilterDari != null   ? tfFilterDari.getText().trim()
                                                : LocalDate.now().withDayOfMonth(1).toString();
        String sampai = tfFilterSampai != null  ? tfFilterSampai.getText().trim()
                                                : LocalDate.now().toString();

        String sql = "SELECT sk.id_keluar, sk.tanggal, p.nama, sk.jumlah, sk.tujuan, sk.keterangan " +
                     "FROM stok_keluar sk LEFT JOIN produk p ON sk.id_produk=p.id_produk " +
                     "WHERE sk.tanggal BETWEEN ? AND ? ORDER BY sk.created_at DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dari);
            ps.setString(2, sampai);
            ResultSet rs = ps.executeQuery();
            int i = 1;
            while (rs.next()) {
                modelRiwayat.addRow(new Object[]{
                    i++,
                    rs.getString("tanggal"),
                    rs.getString("nama") != null ? rs.getString("nama") : "-",
                    rs.getInt("jumlah") + " pcs",
                    rs.getString("tujuan") != null ? rs.getString("tujuan") : "-",
                    rs.getString("keterangan") != null ? rs.getString("keterangan") : "-"
                });
            }
        } catch (SQLException e) {
            System.err.println("[StokKeluar] Error loadData: " + e.getMessage());
        }
    }

    /** Menyimpan data stok keluar ke DB */
    private void simpanStokKeluar() {
        // Validasi
        if (cbProduk.getSelectedIndex() <= 0) {
            showWarning("Silakan pilih produk terlebih dahulu!"); return;
        }
        String jumlahStr = tfJumlah.getText().trim();
        if (jumlahStr.isEmpty()) { showWarning("Jumlah wajib diisi!"); return; }

        int jumlah;
        try {
            jumlah = Integer.parseInt(jumlahStr);
            if (jumlah <= 0) { showWarning("Jumlah harus lebih dari 0!"); return; }
        } catch (NumberFormatException ex) {
            showWarning("Jumlah harus berupa angka!"); return;
        }

        Produk produk = listProduk.get(cbProduk.getSelectedIndex() - 1);

        // Cek stok mencukupi
        Connection conn = Koneksi.getInstance().getConnection();
        if (conn == null) return;

        try {
            // Cek stok saat ini
            int stokSaat = 0;
            try (PreparedStatement ps = conn.prepareStatement(
                "SELECT jumlah FROM stok WHERE id_produk=?")) {
                ps.setInt(1, produk.getIdProduk());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) stokSaat = rs.getInt("jumlah");
            }

            if (stokSaat < jumlah) {
                showWarning("Stok tidak mencukupi!\nStok saat ini: " + stokSaat + " pcs\nJumlah keluar: " + jumlah + " pcs");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(frame,
                "Konfirmasi pengeluaran stok:\n" +
                "Produk : " + produk.getNama() + "\n" +
                "Jumlah : " + jumlah + " pcs\n" +
                "Tujuan : " + tfTujuan.getText().trim(),
                "Konfirmasi", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            conn.setAutoCommit(false);

            // Insert stok_keluar
            String sqlInsert = "INSERT INTO stok_keluar (id_produk, jumlah, tanggal, tujuan, keterangan) VALUES (?,?,?,?,?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
                ps.setInt(1, produk.getIdProduk());
                ps.setInt(2, jumlah);
                ps.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
                ps.setString(4, tfTujuan.getText().trim());
                ps.setString(5, tfKeterangan.getText().trim());
                ps.executeUpdate();
            }

            // Kurangi stok
            String sqlUpdate = "UPDATE stok SET jumlah=jumlah-? WHERE id_produk=?";
            try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                ps.setInt(1, jumlah);
                ps.setInt(2, produk.getIdProduk());
                ps.executeUpdate();
            }

            conn.commit();
            conn.setAutoCommit(true);

            JOptionPane.showMessageDialog(frame,
                "Stok keluar berhasil disimpan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            resetForm();
            loadProduk();
            loadData();

        } catch (SQLException e) {
            try { conn.rollback(); conn.setAutoCommit(true); } catch (SQLException ex) { /* ignore */ }
            JOptionPane.showMessageDialog(frame, "Gagal menyimpan: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetForm() {
        cbProduk.setSelectedIndex(0);
        tfJumlah.setText("");
        tfTujuan.setText("");
        tfKeterangan.setText("");
    }

    // ---- Helper ----

    private void addRow(JPanel panel, GridBagConstraints gbc, int row,
                        String label, JComponent comp) {
        gbc.gridwidth = 1;
        gbc.gridy = row; gbc.gridx = 0; gbc.weightx = 0.3;
        gbc.insets = new Insets(6, 0, 2, 0);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Arial", Font.PLAIN, 11));
        lbl.setForeground(new Color(0x616161));
        panel.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        gbc.insets = new Insets(6, 8, 2, 0);
        panel.add(comp, gbc);
    }

    private JTextField createField(String placeholder) {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Arial", Font.PLAIN, 12));
        tf.setPreferredSize(new Dimension(240, 34));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xDDDDDD), 1, true),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        tf.setToolTipText(placeholder);
        return tf;
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
        btn.setPreferredSize(new Dimension(180, 36));
        return btn;
    }

    private void showWarning(String msg) {
        JOptionPane.showMessageDialog(frame, msg, "Perhatian", JOptionPane.WARNING_MESSAGE);
    }
}
