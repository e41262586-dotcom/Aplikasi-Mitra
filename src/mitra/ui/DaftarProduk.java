package mitra.ui;

import mitra.koneksi.Koneksi;

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
 * DaftarProduk - Panel manajemen data produk
 * Menampilkan daftar produk dengan fitur tambah, edit, dan hapus
 */
public class DaftarProduk extends JPanel {

    private final MainFrame frame;

    private static final Color BG    = MainFrame.BG_KONTEN;
    private static final Color PUTIH = MainFrame.PUTIH;
    private static final Color BIRU  = MainFrame.BIRU_MUDA;
    private static final Color TEKS  = MainFrame.TEKS_GELAP;

    private JTable            tblProduk;
    private DefaultTableModel modelProduk;
    private JTextField        tfSearch;
    private JLabel            lblTotal;
    private List<Object[]>    allData = new ArrayList<>();

    public DaftarProduk(MainFrame frame) {
        this.frame = frame;
        setBackground(BG);
        setLayout(new BorderLayout(0, 0));
        buildUI();
        loadData();
    }

    private void buildUI() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 10));
        wrapper.setBackground(BG);
        wrapper.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // ---- Toolbar ----
        JPanel toolbar = buildToolbar();
        wrapper.add(toolbar, BorderLayout.NORTH);

        // ---- Tabel ----
        String[] cols = {"ID", "SKU", "Nama Produk", "Merk", "Kategori",
                         "Ukuran", "Jenis", "Harga Jual", "Stok"};
        modelProduk = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblProduk = createStyledTable(modelProduk);

        // Lebar kolom
        int[] widths = {50, 90, 180, 110, 100, 80, 80, 120, 70};
        for (int i = 0; i < widths.length; i++) {
            tblProduk.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // Double-click untuk edit
        tblProduk.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tblProduk.getSelectedRow() >= 0) {
                    editProduk();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tblProduk);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(0xE0E0E0)));
        scrollPane.getViewport().setBackground(PUTIH);

        MainFrame.RoundedPanel tableCard = new MainFrame.RoundedPanel(10, PUTIH);
        tableCard.setLayout(new BorderLayout());
        tableCard.add(scrollPane, BorderLayout.CENTER);
        wrapper.add(tableCard, BorderLayout.CENTER);

        // ---- Footer ----
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        lblTotal = new JLabel("Total: 0 produk");
        lblTotal.setFont(new Font("Arial", Font.PLAIN, 12));
        lblTotal.setForeground(new Color(0x757575));

        JLabel hint = new JLabel("💡 Double-click baris untuk edit produk");
        hint.setFont(new Font("Arial", Font.ITALIC, 11));
        hint.setForeground(new Color(0xAAAAAA));

        footer.add(lblTotal, BorderLayout.WEST);
        footer.add(hint, BorderLayout.EAST);
        wrapper.add(footer, BorderLayout.SOUTH);

        add(wrapper, BorderLayout.CENTER);
    }

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Search
        JPanel leftTools = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftTools.setOpaque(false);

        JPanel searchWrap = new JPanel(new BorderLayout());
        searchWrap.setBackground(PUTIH);
        searchWrap.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xDDDDDD), 1, true),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        searchWrap.setPreferredSize(new Dimension(240, 36));

        JLabel ico = new JLabel("🔍 ");
        ico.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        tfSearch = new JTextField();
        tfSearch.setFont(new Font("Arial", Font.PLAIN, 12));
        tfSearch.setBorder(BorderFactory.createEmptyBorder());
        tfSearch.setToolTipText("Cari SKU, nama, merk...");
        tfSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { filterData(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { filterData(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterData(); }
        });
        searchWrap.add(ico, BorderLayout.WEST);
        searchWrap.add(tfSearch, BorderLayout.CENTER);
        leftTools.add(searchWrap);

        // Tombol aksi
        JPanel rightTools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightTools.setOpaque(false);

        JButton btnTambah = createButton("+ Tambah Produk", BIRU);
        btnTambah.addActionListener(e -> showFormProduk(null));

        JButton btnEdit = createButton("✏ Edit", MainFrame.ORANYE);
        btnEdit.setPreferredSize(new Dimension(90, 34));
        btnEdit.addActionListener(e -> editProduk());

        JButton btnHapus = createButton("🗑 Hapus", MainFrame.MERAH);
        btnHapus.setPreferredSize(new Dimension(90, 34));
        btnHapus.addActionListener(e -> hapusProduk());

        JButton btnRefresh = createButton("🔄", new Color(0x757575));
        btnRefresh.setPreferredSize(new Dimension(50, 34));
        btnRefresh.setToolTipText("Refresh");
        btnRefresh.addActionListener(e -> loadData());

        rightTools.add(btnRefresh);
        rightTools.add(btnEdit);
        rightTools.add(btnHapus);
        rightTools.add(btnTambah);

        toolbar.add(leftTools, BorderLayout.WEST);
        toolbar.add(rightTools, BorderLayout.EAST);
        return toolbar;
    }

    /** Memuat data produk dari DB */
    public void loadData() {
        allData.clear();
        Connection conn = Koneksi.getInstance().getConnection();
        if (conn == null) return;

        String sql = "SELECT p.id_produk, p.sku, p.nama, p.merk, p.kategori, " +
                     "p.ukuran, p.jenis, p.harga_jual, COALESCE(s.jumlah,0) AS stok " +
                     "FROM produk p LEFT JOIN stok s ON p.id_produk=s.id_produk " +
                     "ORDER BY p.nama ASC";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            NumberFormat nf = NumberFormat.getNumberInstance(new Locale("id", "ID"));
            while (rs.next()) {
                allData.add(new Object[]{
                    rs.getInt("id_produk"),
                    rs.getString("sku"),
                    rs.getString("nama"),
                    rs.getString("merk"),
                    rs.getString("kategori"),
                    rs.getString("ukuran"),
                    rs.getString("jenis"),
                    "Rp " + nf.format(rs.getDouble("harga_jual")),
                    rs.getInt("stok")
                });
            }
        } catch (SQLException e) {
            System.err.println("[DaftarProduk] Error loadData: " + e.getMessage());
        }

        filterData();
    }

    /** Filter data berdasarkan keyword search */
    private void filterData() {
        String kw = tfSearch != null ? tfSearch.getText().trim().toLowerCase() : "";
        modelProduk.setRowCount(0);
        int count = 0;
        for (Object[] row : allData) {
            boolean match = kw.isEmpty() ||
                (row[1] != null && row[1].toString().toLowerCase().contains(kw)) ||
                (row[2] != null && row[2].toString().toLowerCase().contains(kw)) ||
                (row[3] != null && row[3].toString().toLowerCase().contains(kw)) ||
                (row[4] != null && row[4].toString().toLowerCase().contains(kw));
            if (match) {
                modelProduk.addRow(row);
                count++;
            }
        }
        if (lblTotal != null) lblTotal.setText("Total: " + count + " produk");
    }

    /** Edit produk terpilih */
    private void editProduk() {
        int row = tblProduk.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(frame, "Pilih produk yang ingin diedit!",
                "Perhatian", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Ambil data dari model
        Object[] data = new Object[9];
        for (int i = 0; i < 9; i++) data[i] = modelProduk.getValueAt(row, i);
        showFormProduk(data);
    }

    /** Hapus produk terpilih */
    private void hapusProduk() {
        int row = tblProduk.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(frame, "Pilih produk yang ingin dihapus!",
                "Perhatian", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int idProduk = (int) modelProduk.getValueAt(row, 0);
        String nama   = (String) modelProduk.getValueAt(row, 2);

        int confirm = JOptionPane.showConfirmDialog(frame,
            "Hapus produk \"" + nama + "\"?\nData stok terkait juga akan terhapus!",
            "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        Connection conn = Koneksi.getInstance().getConnection();
        if (conn == null) return;

        try {
            conn.setAutoCommit(false);
            // Hapus stok, stok_masuk, stok_keluar, varian, lalu produk
            for (String tbl : new String[]{"stok_keluar", "stok_masuk", "stok", "varian"}) {
                try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM " + tbl + " WHERE id_produk=?")) {
                    ps.setInt(1, idProduk);
                    ps.executeUpdate();
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM produk WHERE id_produk=?")) {
                ps.setInt(1, idProduk);
                ps.executeUpdate();
            }
            conn.commit();
            conn.setAutoCommit(true);
            JOptionPane.showMessageDialog(frame, "Produk berhasil dihapus!",
                "Sukses", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        } catch (SQLException e) {
            try { conn.rollback(); conn.setAutoCommit(true); } catch (Exception ex) { /* ignore */ }
            JOptionPane.showMessageDialog(frame, "Gagal menghapus: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Dialog form tambah/edit produk */
    private void showFormProduk(Object[] existingData) {
        boolean isEdit = existingData != null;
        JDialog dialog = new JDialog(frame,
            isEdit ? "Edit Produk" : "Tambah Produk Baru", true);
        dialog.setSize(500, 520);
        dialog.setLocationRelativeTo(frame);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(PUTIH);
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);

        String[] labels = {"SKU:", "Nama Produk:", "Merk:", "Kategori:",
                           "Ukuran:", "Jenis:", "Harga Jual (Rp):", "Deskripsi:"};
        JTextField[] fields = new JTextField[labels.length];

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0.35;
            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(new Font("Arial", Font.PLAIN, 12));
            form.add(lbl, gbc);

            gbc.gridx = 1; gbc.weightx = 0.65;
            fields[i] = new JTextField();
            fields[i].setFont(new Font("Arial", Font.PLAIN, 12));
            fields[i].setPreferredSize(new Dimension(220, 30));
            fields[i].setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDDDDDD), 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
            ));
            form.add(fields[i], gbc);
        }

        // Isi data jika edit (kolom: 0=ID, 1=SKU, 2=Nama, 3=Merk, 4=Kategori, 5=Ukuran, 6=Jenis, 7=Harga)
        if (isEdit) {
            fields[0].setText(existingData[1] != null ? existingData[1].toString() : "");
            fields[1].setText(existingData[2] != null ? existingData[2].toString() : "");
            fields[2].setText(existingData[3] != null ? existingData[3].toString() : "");
            fields[3].setText(existingData[4] != null ? existingData[4].toString() : "");
            fields[4].setText(existingData[5] != null ? existingData[5].toString() : "");
            fields[5].setText(existingData[6] != null ? existingData[6].toString() : "");
            String harga = existingData[7] != null ? existingData[7].toString().replace("Rp ", "").replace(".", "") : "";
            fields[6].setText(harga);
            fields[0].setEditable(false); // SKU tidak boleh diubah saat edit
            fields[0].setBackground(new Color(0xF5F7FA));
        }

        // Panel tombol
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(new Color(0xF5F7FA));
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xE0E0E0)));

        JButton btnBatal = new JButton("Batal");
        btnBatal.addActionListener(e -> dialog.dispose());

        JButton btnSimpan = createButton(isEdit ? "💾 Update" : "💾 Simpan", BIRU);
        btnSimpan.setPreferredSize(new Dimension(120, 36));
        btnSimpan.addActionListener(e -> {
            if (fields[0].getText().trim().isEmpty() || fields[1].getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "SKU dan Nama wajib diisi!",
                    "Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Connection conn = Koneksi.getInstance().getConnection();
            if (conn == null) return;

            try {
                String hargaStr = fields[6].getText().trim()
                    .replace(".", "").replace(",", ".");
                double harga = hargaStr.isEmpty() ? 0 : Double.parseDouble(hargaStr);

                if (isEdit) {
                    int idProduk = (int) existingData[0];
                    String sql = "UPDATE produk SET nama=?, merk=?, kategori=?, ukuran=?, jenis=?, harga_jual=?, deskripsi=? WHERE id_produk=?";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, fields[1].getText().trim());
                        ps.setString(2, fields[2].getText().trim());
                        ps.setString(3, fields[3].getText().trim());
                        ps.setString(4, fields[4].getText().trim());
                        ps.setString(5, fields[5].getText().trim());
                        ps.setDouble(6, harga);
                        ps.setString(7, fields[7].getText().trim());
                        ps.setInt(8, idProduk);
                        ps.executeUpdate();
                    }
                    JOptionPane.showMessageDialog(dialog, "Produk berhasil diupdate!",
                        "Sukses", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    String sql = "INSERT INTO produk (sku, nama, merk, kategori, ukuran, jenis, harga_jual, deskripsi) VALUES (?,?,?,?,?,?,?,?)";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, fields[0].getText().trim());
                        ps.setString(2, fields[1].getText().trim());
                        ps.setString(3, fields[2].getText().trim());
                        ps.setString(4, fields[3].getText().trim());
                        ps.setString(5, fields[4].getText().trim());
                        ps.setString(6, fields[5].getText().trim());
                        ps.setDouble(7, harga);
                        ps.setString(8, fields[7].getText().trim());
                        ps.executeUpdate();
                    }
                    JOptionPane.showMessageDialog(dialog, "Produk berhasil ditambahkan!",
                        "Sukses", JOptionPane.INFORMATION_MESSAGE);
                }
                dialog.dispose();
                loadData();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Gagal menyimpan: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Format harga tidak valid!",
                    "Validasi", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnPanel.add(btnBatal);
        btnPanel.add(btnSimpan);

        dialog.add(new JScrollPane(form), BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // ---- Helper ----

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
}
