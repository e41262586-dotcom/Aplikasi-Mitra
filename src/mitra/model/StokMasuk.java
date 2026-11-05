package mitra.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

/**
 * Model POJO untuk tabel stok_masuk
 * Merepresentasikan data stok barang masuk
 */
public class StokMasuk {

    private int        idMasuk;
    private int        idProduk;
    private int        jumlah;
    private BigDecimal hargaSatuan;
    private Date       tanggal;
    private String     keterangan;
    private Timestamp  createdAt;

    // Field tambahan dari JOIN produk
    private String namaProduk;
    private String sku;
    private String merk;

    // ---- Konstruktor ----

    public StokMasuk() {}

    public StokMasuk(int idProduk, int jumlah, BigDecimal hargaSatuan,
                     Date tanggal, String keterangan) {
        this.idProduk    = idProduk;
        this.jumlah      = jumlah;
        this.hargaSatuan = hargaSatuan;
        this.tanggal     = tanggal;
        this.keterangan  = keterangan;
    }

    // ---- Getter & Setter ----

    public int getIdMasuk()         { return idMasuk; }
    public void setIdMasuk(int v)   { this.idMasuk = v; }

    public int getIdProduk()        { return idProduk; }
    public void setIdProduk(int v)  { this.idProduk = v; }

    public int getJumlah()          { return jumlah; }
    public void setJumlah(int v)    { this.jumlah = v; }

    public BigDecimal getHargaSatuan()        { return hargaSatuan; }
    public void setHargaSatuan(BigDecimal v)  { this.hargaSatuan = v; }

    public Date getTanggal()         { return tanggal; }
    public void setTanggal(Date v)   { this.tanggal = v; }

    public String getKeterangan()       { return keterangan; }
    public void setKeterangan(String v) { this.keterangan = v; }

    public Timestamp getCreatedAt()        { return createdAt; }
    public void setCreatedAt(Timestamp v)  { this.createdAt = v; }

    public String getNamaProduk()          { return namaProduk; }
    public void setNamaProduk(String v)    { this.namaProduk = v; }

    public String getSku()            { return sku; }
    public void setSku(String v)      { this.sku = v; }

    public String getMerk()           { return merk; }
    public void setMerk(String v)     { this.merk = v; }

    /**
     * Menghitung total nilai stok masuk (jumlah x harga satuan)
     */
    public BigDecimal getTotalNilai() {
        if (hargaSatuan == null) return BigDecimal.ZERO;
        return hargaSatuan.multiply(BigDecimal.valueOf(jumlah));
    }
}
