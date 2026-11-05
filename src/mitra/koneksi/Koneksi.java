package mitra.koneksi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Kelas Koneksi Database MySQL (Singleton Pattern)
 * Mengelola satu instance koneksi ke database MySQL 'mitra'
 */
public class Koneksi {

    // Konfigurasi database
    private static final String HOST     = "localhost";
    private static final String PORT     = "3306";
    private static final String DATABASE = "mitra";
    private static final String USER     = "root";
    private static final String PASSWORD = "";

    private static final String URL =
        "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE
        + "?useSSL=false&serverTimezone=Asia/Jakarta&allowPublicKeyRetrieval=true";

    // Instance tunggal (Singleton)
    private static Koneksi instance;
    private Connection connection;

    // Konstruktor privat agar tidak bisa dibuat instance dari luar
    private Koneksi() {
        connect();
    }

    /**
     * Mendapatkan instance Koneksi (Singleton)
     */
    public static synchronized Koneksi getInstance() {
        if (instance == null) {
            instance = new Koneksi();
        }
        return instance;
    }

    /**
     * Membuka koneksi ke database
     */
    private void connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("[Koneksi] Berhasil terhubung ke database: " + DATABASE);
            initTables();
        } catch (ClassNotFoundException e) {
            System.err.println("[Koneksi] Driver MySQL tidak ditemukan: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("[Koneksi] Gagal terhubung ke database: " + e.getMessage());
        }
    }

    /**
     * Mendapatkan objek Connection
     */
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            connect();
        }
        return connection;
    }

    /**
     * Menutup koneksi database
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[Koneksi] Koneksi database ditutup.");
            }
        } catch (SQLException e) {
            System.err.println("[Koneksi] Gagal menutup koneksi: " + e.getMessage());
        }
    }

    /**
     * Membuat tabel jika belum ada (inisialisasi database)
     */
    private void initTables() {
        try (Statement stmt = connection.createStatement()) {

            // Tabel produk
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS produk (" +
                "  id_produk INT AUTO_INCREMENT PRIMARY KEY," +
                "  sku VARCHAR(50) UNIQUE," +
                "  nama VARCHAR(100)," +
                "  merk VARCHAR(100)," +
                "  kategori VARCHAR(100)," +
                "  ukuran VARCHAR(50)," +
                "  jenis VARCHAR(50)," +
                "  harga_jual DECIMAL(15,2)," +
                "  deskripsi TEXT," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );

            // Tabel varian
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS varian (" +
                "  id_varian INT AUTO_INCREMENT PRIMARY KEY," +
                "  id_produk INT," +
                "  nama_varian VARCHAR(100)," +
                "  FOREIGN KEY (id_produk) REFERENCES produk(id_produk)" +
                ")"
            );

            // Tabel stok
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS stok (" +
                "  id_stok INT AUTO_INCREMENT PRIMARY KEY," +
                "  id_produk INT," +
                "  jumlah INT DEFAULT 0," +
                "  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "  FOREIGN KEY (id_produk) REFERENCES produk(id_produk)" +
                ")"
            );

            // Tabel stok_masuk
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS stok_masuk (" +
                "  id_masuk INT AUTO_INCREMENT PRIMARY KEY," +
                "  id_produk INT," +
                "  jumlah INT," +
                "  harga_satuan DECIMAL(15,2)," +
                "  tanggal DATE," +
                "  keterangan TEXT," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  FOREIGN KEY (id_produk) REFERENCES produk(id_produk)" +
                ")"
            );

            // Tabel stok_keluar
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS stok_keluar (" +
                "  id_keluar INT AUTO_INCREMENT PRIMARY KEY," +
                "  id_produk INT," +
                "  jumlah INT," +
                "  tanggal DATE," +
                "  tujuan VARCHAR(100)," +
                "  keterangan TEXT," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  FOREIGN KEY (id_produk) REFERENCES produk(id_produk)" +
                ")"
            );

            // Tabel produksi
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS produksi (" +
                "  id_produksi INT AUTO_INCREMENT PRIMARY KEY," +
                "  nama_produksi VARCHAR(100)," +
                "  jumlah INT," +
                "  tanggal DATE," +
                "  keterangan TEXT," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );

            System.out.println("[Koneksi] Inisialisasi tabel database selesai.");

        } catch (SQLException e) {
            System.err.println("[Koneksi] Gagal inisialisasi tabel: " + e.getMessage());
        }
    }
}
