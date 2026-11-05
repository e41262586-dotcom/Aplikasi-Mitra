package mitra.model;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Model POJO untuk tabel stok_keluar
 * Merepresentasikan data stok barang keluar
 */
public class StokKeluar {

    private int       idKeluar;
    private int       idProduk;
    private int       jumlah;
    private Date      tanggal;
    private String    tujuan;
    private String    keterangan;
    private Timestamp createdAt;

    // Field tambahan dari JOIN produk
    private String namaProduk;
    private String sku;
    private String merk;

    // ---- Konstruktor ----

    public StokKeluar() {}

    public StokKeluar(int idProduk, int jumlah, Date tanggal,
                      String tujuan, String keterangan) {
        this.idProduk   = idProduk;
        this.jumlah     = jumlah;
        this.tanggal    = tanggal;
        this.tujuan     = tujuan;
        this.keterangan = keterangan;
    }

    // ---- Getter & Setter ----

    public int getIdKeluar()        { return idKeluar; }
    public void setIdKeluar(int v)  { this.idKeluar = v; }

    public int getIdProduk()        { return idProduk; }
    public void setIdProduk(int v)  { this.idProduk = v; }

    public int getJumlah()          { return jumlah; }
    public void setJumlah(int v)    { this.jumlah = v; }

    public Date getTanggal()         { return tanggal; }
    public void setTanggal(Date v)   { this.tanggal = v; }

    public String getTujuan()        { return tujuan; }
    public void setTujuan(String v)  { this.tujuan = v; }

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
}
