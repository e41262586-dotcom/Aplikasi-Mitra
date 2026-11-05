package mitra.model;

import java.sql.Timestamp;

/**
 * Model POJO untuk tabel stok
 * Merepresentasikan data stok barang
 */
public class Stok {

    private int    idStok;
    private int    idProduk;
    private int    jumlah;
    private Timestamp updatedAt;

    // Data join dari tabel produk (optional, diisi saat query JOIN)
    private String sku;
    private String namaProduk;
    private String merk;
    private String ukuran;
    private String jenis;
    private String kategori;
    private double hargaJual;

    // ---- Konstruktor ----

    public Stok() {}

    public Stok(int idStok, int idProduk, int jumlah) {
        this.idStok   = idStok;
        this.idProduk = idProduk;
        this.jumlah   = jumlah;
    }

    // ---- Getter & Setter ----

    public int getIdStok()         { return idStok; }
    public void setIdStok(int v)   { this.idStok = v; }

    public int getIdProduk()       { return idProduk; }
    public void setIdProduk(int v) { this.idProduk = v; }

    public int getJumlah()         { return jumlah; }
    public void setJumlah(int v)   { this.jumlah = v; }

    public Timestamp getUpdatedAt()       { return updatedAt; }
    public void setUpdatedAt(Timestamp v) { this.updatedAt = v; }

    public String getSku()            { return sku; }
    public void setSku(String v)      { this.sku = v; }

    public String getNamaProduk()          { return namaProduk; }
    public void setNamaProduk(String v)    { this.namaProduk = v; }

    public String getMerk()           { return merk; }
    public void setMerk(String v)     { this.merk = v; }

    public String getUkuran()         { return ukuran; }
    public void setUkuran(String v)   { this.ukuran = v; }

    public String getJenis()          { return jenis; }
    public void setJenis(String v)    { this.jenis = v; }

    public String getKategori()       { return kategori; }
    public void setKategori(String v) { this.kategori = v; }

    public double getHargaJual()       { return hargaJual; }
    public void setHargaJual(double v) { this.hargaJual = v; }

    /**
     * Mengembalikan status stok: Aman / Stok Tipis / Habis
     */
    public String getStatus() {
        if (jumlah <= 0)   return "Habis";
        if (jumlah <= 10)  return "Stok Tipis";
        return "Aman";
    }
}
