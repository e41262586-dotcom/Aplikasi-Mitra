package mitra.ui;

import mitra.koneksi.Koneksi;
import mitra.model.Produk;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.sql.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * InputStok - Panel input stok masuk
 * Menyediakan form input stok masuk dengan sistem draft (antrian)
 */
public class InputStok extends JPanel {

    private final MainFrame frame;

    private static final Color BG    = MainFrame.BG_KONTEN;
    private static final Color PUTIH = MainFrame.PUTIH;
    private static final Color BIRU  = MainFrame.BIRU_MUDA;
    private static final Color TEKS  = MainFrame.TEKS_GELAP;
    private static final Color HIJAU = MainFrame.HIJAU;

    // Form fields
    private JComboBox<String> cbProduk;
    private JTextField tfJumlah;
    private JTextField tfHarga;
    private JTextField tfKeterangan;
    private JLabel     lblTotalUnit;
    private JLabel     lblEstimasiNilai;

    // List produk dari DB
    private List<Produk> listProduk = new ArrayList<>();

    // Model tabel draft
    private DefaultTableModel modelDraft;
    private JTable            tblDraft;

    // Data draft (sementara sebelum simpan)
    private List<Object[]> draftList = new ArrayList<>();

    public InputStok(MainFrame frame) {
        this.frame = frame;
        setBackground(BG);
        setLayout(new BorderLayout(0, 0));
        buildUI();
        loadProduk();
    }

    private void buildUI() {
        JPanel wrapper = new JPanel(new BorderLayout(16, 0));
        wrapper.setBackground(BG);
        wrapper.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // ---- Panel kiri: Form Input ----
        JPanel leftPanel = buildFormPanel();
        leftPanel.setPreferredSize(new Dimension(380, 0));

        // ---- Panel kanan: Daftar Antrian Draft ----
        JPanel rightPanel = buildDraftPanel();

        wrapper.add(leftPanel, BorderLayout.WEST);
        wrapper.add(rightPanel, BorderLayout.CENTER);

        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(BG);
        add(scroll, BorderLayout.CENTER);
    }

    /** Panel form input kiri */
    private JPanel buildFormPanel() {
        MainFrame.RoundedPanel card = new MainFrame.RoundedPanel(12, PUTIH);
        card.setLayout(new BorderLayout());

        // Header kartu
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(14, 16, 0, 16));
        JLabel title = new JLabel("📥 Form Input Stok Masuk");
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setForeground(TEKS);
        header.add(title);

        // Form body
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(6, 0, 6, 0);

        // Produk
        addFormRow(body, gbc, 0, "Pilih Produk *",
            cbProduk = new JComboBox<>());
        cbProduk.setFont(new Font("Arial", Font.PLAIN, 12));
        cbProduk.setPreferredSize(new Dimension(300, 34));
        cbProduk.addActionListener(e -> updateEstimasi());

        // Jumlah
        addFormRow(body, gbc, 1, "Jumlah (Pcs) *",
            tfJumlah = createTextField("Masukkan jumlah unit..."));
        tfJumlah.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { updateEstimasi(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { updateEstimasi(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateEstimasi(); }
        });

        // Harga Satuan
        addFormRow(body, gbc, 2, "Harga Jual Satuan (Rp) *",
            tfHarga = createTextField("Harga per pcs..."));
        tfHarga.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { updateEstimasi(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { updateEstimasi(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateEstimasi(); }
        });

        // Keterangan
        addFormRow(body, gbc, 3, "Keterangan",
            tfKeterangan = createTextField("Keterangan / catatan (opsional)"));

        // Separator
        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 0, 10, 0);
        body.add(new JSeparator(), gbc);

        // Estimasi
        gbc.insets = new Insets(2, 0, 2, 0);
        gbc.gridy = 5;
        JPanel estimasiPanel = buildEstimasiPanel();
        body.add(estimasiPanel, gbc);

        gbc.gridy = 6; gbc.insets = new Insets(14, 0, 4, 0);
        // Tombol aksi
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setOpaque(false);

        JButton btnDraft = createButton("+ Tambah ke Draft", BIRU);
        btnDraft.addActionListener(e -> tambahKeDraft());

        JButton btnSimpan = createButton("💾 Simpan Semua", HIJAU);
        btnSimpan.addActionListener(e -> simpanSemua());

        JButton btnReset = new JButton("Reset Form");
        btnReset.setFont(new Font("Arial", Font.PLAIN, 12));
        btnReset.setPreferredSize(new Dimension(110, 34));
        btnReset.addActionListener(e -> resetForm());

        btnRow.add(btnDraft);
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

    /** Panel estimasi nilai */
    private JPanel buildEstimasiPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 4));
        panel.setOpaque(false);

        JPanel row1 = new JPanel(new BorderLayout());
        row1.setOpaque(false);
        JLabel lbl1 = new JLabel("Total Unit Masuk:");
        lbl1.setFont(new Font("Arial", Font.PLAIN, 12));
        lbl1.setForeground(new Color(0x757575));
        lblTotalUnit = new JLabel("0 pcs");
        lblTotalUnit.setFont(new Font("Arial", Font.BOLD, 13));
        lblTotalUnit.setForeground(BIRU);
        row1.add(lbl1, BorderLayout.WEST);
        row1.add(lblTotalUnit, BorderLayout.EAST);

        JPanel row2 = new JPanel(new BorderLayout());
        row2.setOpaque(false);
        JLabel lbl2 = new JLabel("Estimasi Nilai Barang:");
        lbl2.setFont(new Font("Arial", Font.PLAIN, 12));
        lbl2.setForeground(new Color(0x757575));
        lblEstimasiNilai = new JLabel("Rp 0");
        lblEstimasiNilai.setFont(new Font("Arial", Font.BOLD, 13));
        lblEstimasiNilai.setForeground(HIJAU);
        row2.add(lbl2, BorderLayout.WEST);
        row2.add(lblEstimasiNilai, BorderLayout.EAST);

        panel.add(row1);
        panel.add(row2);
        return panel;
    }

    /** Panel daftar antrian draft kanan */
    private JPanel buildDraftPanel() {
        MainFrame.RoundedPanel card = new MainFrame.RoundedPanel(12, PUTIH);
        card.setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(14, 16, 10, 16));
        JLabel title = new JLabel("📋 Daftar Antrian Stok (Draft)");
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setForeground(TEKS);

        JButton btnHapus = new JButton("🗑 Hapus Terpilih");
        btnHapus.setFont(new Font("Arial", Font.PLAIN, 11));
        btnHapus.setForeground(MainFrame.MERAH);
        btnHapus.setBorderPainted(false);
        btnHapus.setContentAreaFilled(false);
        btnHapus.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnHapus.addActionListener(e -> hapusDraftTerpilih());

        header.add(title, BorderLayout.WEST);
        header.add(btnHapus, BorderLayout.EAST);

        // Tabel draft
        String[] cols = {"#", "Produk", "Jumlah", "Harga Satuan", "Total Nilai", "Keterangan"};
        modelDraft = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblDraft = new JTable(modelDraft);
        tblDraft.setFont(new Font("Arial", Font.PLAIN, 12));
        tblDraft.setRowHeight(32);
        tblDraft.setShowGrid(false);
        tblDraft.setBackground(PUTIH);
        tblDraft.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
        tblDraft.getTableHeader().setBackground(new Color(0xF5F7FA));
        tblDraft.setSelectionBackground(new Color(0xE3F2FD));

        JScrollPane scroll = new JScrollPane(tblDraft);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(PUTIH);

        // Footer draft: total keseluruhan
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xE0E0E0)));
        JLabel lblInfo = new JLabel("Tekan '💾 Simpan Semua' untuk menyimpan semua draft ke database");
        lblInfo.setFont(new Font("Arial", Font.ITALIC, 11));
        lblInfo.setForeground(new Color(0xAAAAAA));
        footer.add(lblInfo);

        card.add(header, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);

        return card;
    }

    /** Memuat daftar produk dari DB ke combo box */
    public void loadProduk() {
        listProduk.clear();
        cbProduk.removeAllItems();
        cbProduk.addItem("-- Pilih Produk --");

        Connection conn = Koneksi.getInstance().getConnection();
        if (conn == null) return;

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT id_produk, sku, nama, merk, harga_jual FROM produk ORDER BY nama")) {
            while (rs.next()) {
                Produk p = new Produk();
                p.setIdProduk(rs.getInt("id_produk"));
                p.setSku(rs.getString("sku"));
                p.setNama(rs.getString("nama"));
                p.setMerk(rs.getString("merk"));
                p.setHargaJual(rs.getBigDecimal("harga_jual"));
                listProduk.add(p);
                cbProduk.addItem("[" + rs.getString("sku") + "] " + rs.getString("nama"));
            }
        } catch (SQLException e) {
            System.err.println("[InputStok] Error loadProduk: " + e.getMessage());
        }
    }

    /** Memperbarui label estimasi nilai barang */
    private void updateEstimasi() {
        try {
            int jumlah = Integer.parseInt(tfJumlah.getText().trim());
            double harga = Double.parseDouble(
                tfHarga.getText().trim().replace(".", "").replace(",", "."));
            double total = jumlah * harga;
            NumberFormat nf = NumberFormat.getNumberInstance(new Locale("id", "ID"));
            lblTotalUnit.setText(jumlah + " pcs");
            lblEstimasiNilai.setText("Rp " + nf.format(total));
        } catch (NumberFormatException ex) {
            lblTotalUnit.setText("0 pcs");
            lblEstimasiNilai.setText("Rp 0");
        }
    }

    /** Menambahkan item ke daftar draft */
    private void tambahKeDraft() {
        // Validasi
        if (cbProduk.getSelectedIndex() <= 0) {
            showWarning("Silakan pilih produk terlebih dahulu!");
            return;
        }
        String jumlahStr = tfJumlah.getText().trim();
        String hargaStr  = tfHarga.getText().trim().replace(".", "").replace(",", ".");
        if (jumlahStr.isEmpty()) { showWarning("Jumlah wajib diisi!"); return; }
        if (hargaStr.isEmpty())  { showWarning("Harga satuan wajib diisi!"); return; }

        try {
            int    jumlah = Integer.parseInt(jumlahStr);
            double harga  = Double.parseDouble(hargaStr);
            if (jumlah <= 0) { showWarning("Jumlah harus lebih dari 0!"); return; }

            int produkIdx       = cbProduk.getSelectedIndex() - 1;
            Produk produk       = listProduk.get(produkIdx);
            double totalNilai   = jumlah * harga;
            String keterangan   = tfKeterangan.getText().trim();

            NumberFormat nf = NumberFormat.getNumberInstance(new Locale("id", "ID"));

            // Simpan ke daftarDraft
            draftList.add(new Object[]{
                produk.getIdProduk(), produk.getNama(),
                jumlah, harga, totalNilai, keterangan
            });

            // Tambah ke model tabel
            modelDraft.addRow(new Object[]{
                modelDraft.getRowCount() + 1,
                "[" + produk.getSku() + "] " + produk.getNama(),
                jumlah + " pcs",
                "Rp " + nf.format(harga),
                "Rp " + nf.format(totalNilai),
                keterangan
            });

            // Reset field input (kecuali produk)
            tfJumlah.setText("");
            tfHarga.setText("");
            tfKeterangan.setText("");
            updateEstimasi();

        } catch (NumberFormatException ex) {
            showWarning("Jumlah dan harga harus berupa angka!");
        }
    }

    /** Menyimpan semua draft ke database */
    private void simpanSemua() {
        if (draftList.isEmpty()) {
            showWarning("Tidak ada data draft untuk disimpan!\nTambahkan item ke draft terlebih dahulu.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(frame,
            "Simpan " + draftList.size() + " item stok masuk ke database?",
            "Konfirmasi Simpan", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        Connection conn = Koneksi.getInstance().getConnection();
        if (conn == null) return;

        String sqlMasuk = "INSERT INTO stok_masuk (id_produk, jumlah, harga_satuan, tanggal, keterangan) " +
                          "VALUES (?, ?, ?, ?, ?)";
        String sqlCekStok   = "SELECT id_stok FROM stok WHERE id_produk=?";
        String sqlUpdateStok = "UPDATE stok SET jumlah=jumlah+? WHERE id_produk=?";
        String sqlInsertStok = "INSERT INTO stok (id_produk, jumlah) VALUES (?, ?)";

        int sukses = 0;
        try {
            conn.setAutoCommit(false);
            for (Object[] draft : draftList) {
                int    idProduk   = (int) draft[0];
                int    jumlah     = (int) draft[2];
                double harga      = (double) draft[3];
                String ket        = (String) draft[5];

                // Insert stok_masuk
                try (PreparedStatement ps = conn.prepareStatement(sqlMasuk)) {
                    ps.setInt(1, idProduk);
                    ps.setInt(2, jumlah);
                    ps.setDouble(3, harga);
                    ps.setDate(4, java.sql.Date.valueOf(LocalDate.now()));
                    ps.setString(5, ket);
                    ps.executeUpdate();
                }

                // Update atau insert stok
                try (PreparedStatement ps = conn.prepareStatement(sqlCekStok)) {
                    ps.setInt(1, idProduk);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        // Update
                        try (PreparedStatement psu = conn.prepareStatement(sqlUpdateStok)) {
                            psu.setInt(1, jumlah);
                            psu.setInt(2, idProduk);
                            psu.executeUpdate();
                        }
                    } else {
                        // Insert baru
                        try (PreparedStatement psi = conn.prepareStatement(sqlInsertStok)) {
                            psi.setInt(1, idProduk);
                            psi.setInt(2, jumlah);
                            psi.executeUpdate();
                        }
                    }
                }
                sukses++;
            }
            conn.commit();
            conn.setAutoCommit(true);

            JOptionPane.showMessageDialog(frame,
                sukses + " item stok masuk berhasil disimpan!",
                "Sukses", JOptionPane.INFORMATION_MESSAGE);

            // Bersihkan draft
            draftList.clear();
            modelDraft.setRowCount(0);
            resetForm();

        } catch (SQLException e) {
            try { conn.rollback(); conn.setAutoCommit(true); } catch (SQLException ex) { /* ignore */ }
            JOptionPane.showMessageDialog(frame,
                "Gagal menyimpan: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Menghapus baris draft yang dipilih */
    private void hapusDraftTerpilih() {
        int row = tblDraft.getSelectedRow();
        if (row < 0) {
            showWarning("Pilih item draft yang ingin dihapus terlebih dahulu!");
            return;
        }
        draftList.remove(row);
        modelDraft.removeRow(row);
        // Update nomor urut
        for (int i = 0; i < modelDraft.getRowCount(); i++) {
            modelDraft.setValueAt(i + 1, i, 0);
        }
    }

    /** Reset semua field form */
    private void resetForm() {
        cbProduk.setSelectedIndex(0);
        tfJumlah.setText("");
        tfHarga.setText("");
        tfKeterangan.setText("");
        lblTotalUnit.setText("0 pcs");
        lblEstimasiNilai.setText("Rp 0");
    }

    // ---- Helper ----

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridwidth = 1;
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0.3;
        gbc.insets = new Insets(6, 0, 2, 0);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Arial", Font.PLAIN, 11));
        lbl.setForeground(new Color(0x616161));
        panel.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7; gbc.gridwidth = 1;
        gbc.insets = new Insets(6, 8, 2, 0);
        panel.add(field, gbc);
    }

    private JTextField createTextField(String placeholder) {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Arial", Font.PLAIN, 12));
        tf.setPreferredSize(new Dimension(220, 34));
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
        btn.setPreferredSize(new Dimension(170, 36));
        return btn;
    }

    private void showWarning(String msg) {
        JOptionPane.showMessageDialog(frame, msg, "Perhatian", JOptionPane.WARNING_MESSAGE);
    }
}
