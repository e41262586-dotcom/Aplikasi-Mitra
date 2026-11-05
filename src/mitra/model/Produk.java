package mitra.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Model POJO untuk tabel produk
 * Merepresentasikan data produk dalam aplikasi Nyetok
 */
public class Produk {

    private int idProduk;
    private String sku;
    private String nama;
    private String merk;
    private String kategori;
    private String ukuran;
    private String jenis;
    private BigDecimal hargaJual;
    private String deskripsi;
    private Timestamp createdAt;

    // ---- Konstruktor ----

    public Produk() {}

    public Produk(int idProduk, String sku, String nama, String merk,
                  String kategori, String ukuran, String jenis,
                  BigDecimal hargaJual, String deskripsi) {
        this.idProduk  = idProduk;
        this.sku       = sku;
        this.nama      = nama;
        this.merk      = merk;
        this.kategori  = kategori;
        this.ukuran    = ukuran;
        this.jenis     = jenis;
        this.hargaJual = hargaJual;
        this.deskripsi = deskripsi;
    }

    // ---- Getter & Setter ----

    public int getIdProduk()          { return idProduk; }
    public void setIdProduk(int v)    { this.idProduk = v; }

    public String getSku()            { return sku; }
    public void setSku(String v)      { this.sku = v; }

    public String getNama()           { return nama; }
    public void setNama(String v)     { this.nama = v; }

    public String getMerk()           { return merk; }
    public void setMerk(String v)     { this.merk = v; }

    public String getKategori()       { return kategori; }
    public void setKategori(String v) { this.kategori = v; }

    public String getUkuran()         { return ukuran; }
    public void setUkuran(String v)   { this.ukuran = v; }

    public String getJenis()          { return jenis; }
    public void setJenis(String v)    { this.jenis = v; }

    public BigDecimal getHargaJual()       { return hargaJual; }
    public void setHargaJual(BigDecimal v) { this.hargaJual = v; }

    public String getDeskripsi()       { return deskripsi; }
    public void setDeskripsi(String v) { this.deskripsi = v; }

    public Timestamp getCreatedAt()        { return createdAt; }
    public void setCreatedAt(Timestamp v)  { this.createdAt = v; }

    @Override
    public String toString() {
        return "[" + sku + "] " + nama + " - " + merk;
    }
}
