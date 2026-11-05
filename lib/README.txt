# Folder lib - MySQL JDBC Connector

Folder ini harus berisi file JAR MySQL Connector/J.

## Langkah Install

1. Download MySQL Connector/J dari:
   https://dev.mysql.com/downloads/connector/j/

2. Pilih versi "Platform Independent" → download ZIP atau TAR.GZ

3. Ekstrak, lalu copy file `mysql-connector-j-8.x.x.jar` ke folder ini:
   `c:\Project Netbeens\MITRA\lib\`

4. Rename (opsional) menjadi: `mysql-connector-j-8.0.33.jar`
   (atau update `nbproject/project.properties` sesuai nama file yang di-download)

5. Di NetBeans:
   - Klik kanan project MITRA → Properties
   - Libraries → Add JAR/Folder
   - Pilih file JAR dari folder lib ini

## Versi yang Direkomendasikan
- mysql-connector-j-8.0.33.jar (kompatibel dengan MySQL 5.7+ dan 8.x)

## Catatan
Tanpa JAR ini, aplikasi akan gagal terhubung ke database MySQL.
