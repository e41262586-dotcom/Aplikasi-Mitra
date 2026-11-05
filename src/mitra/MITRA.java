package mitra;

import mitra.koneksi.Koneksi;
import mitra.ui.MainFrame;

import javax.swing.*;
import java.awt.*;

/**
 * Kelas utama (Entry Point) aplikasi Nyetok
 * Menampilkan splash screen kemudian membuka MainFrame
 */
public class MITRA {

    public static void main(String[] args) {
        // Atur Look and Feel sistem agar tampilan lebih native
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Gagal mengatur Look and Feel: " + e.getMessage());
        }

        // Jalankan di Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            // Tampilkan splash screen
            SplashScreen splash = new SplashScreen();
            splash.setVisible(true);

            // Inisialisasi koneksi database di background thread
            new Thread(() -> {
                try {
                    // Simulasi loading dengan progress
                    for (int i = 0; i <= 100; i += 5) {
                        final int progress = i;
                        SwingUtilities.invokeLater(() -> splash.setProgress(progress));
                        Thread.sleep(60);
                    }

                    // Inisialisasi koneksi database
                    Koneksi.getInstance();

                    Thread.sleep(500); // Jeda sebentar agar splash terlihat

                    // Tutup splash dan buka MainFrame
                    SwingUtilities.invokeLater(() -> {
                        splash.dispose();
                        MainFrame mainFrame = new MainFrame();
                        mainFrame.setVisible(true);
                    });

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        });
    }

    // =========================================================
    // Inner class: SplashScreen
    // =========================================================
    static class SplashScreen extends JWindow {

        private JProgressBar progressBar;
        private JLabel       statusLabel;

        // Warna tema biru
        private static final Color BIRU_TUA    = new Color(0x1565C0);
        private static final Color BIRU_MUDA   = new Color(0x1E88E5);
        private static final Color PUTIH       = Color.WHITE;

        public SplashScreen() {
            setSize(500, 300);
            setLocationRelativeTo(null);
            setLayout(new BorderLayout());

            // Panel utama splash
            JPanel mainPanel = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                         RenderingHints.VALUE_ANTIALIAS_ON);
                    // Gradien biru
                    GradientPaint gp = new GradientPaint(
                        0, 0, BIRU_TUA, getWidth(), getHeight(), BIRU_MUDA);
                    g2d.setPaint(gp);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
            };
            mainPanel.setOpaque(false);
            mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 30, 40));

            // Panel tengah: logo & teks
            JPanel centerPanel = new JPanel();
            centerPanel.setOpaque(false);
            centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

            // Icon/logo teks
            JLabel iconLabel = new JLabel("📦");
            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 56));
            iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel titleLabel = new JLabel("NYETOK");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
            titleLabel.setForeground(PUTIH);
            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel subtitleLabel = new JLabel("Inventory Management System");
            subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            subtitleLabel.setForeground(new Color(255, 255, 255, 180));
            subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            centerPanel.add(iconLabel);
            centerPanel.add(Box.createVerticalStrut(8));
            centerPanel.add(titleLabel);
            centerPanel.add(Box.createVerticalStrut(4));
            centerPanel.add(subtitleLabel);

            // Panel bawah: progress bar
            JPanel bottomPanel = new JPanel(new BorderLayout(0, 6));
            bottomPanel.setOpaque(false);

            statusLabel = new JLabel("Memuat aplikasi...");
            statusLabel.setForeground(new Color(255, 255, 255, 200));
            statusLabel.setFont(new Font("Arial", Font.PLAIN, 11));

            progressBar = new JProgressBar(0, 100);
            progressBar.setStringPainted(false);
            progressBar.setForeground(PUTIH);
            progressBar.setBackground(new Color(255, 255, 255, 60));
            progressBar.setBorderPainted(false);
            progressBar.setPreferredSize(new Dimension(400, 6));

            bottomPanel.add(statusLabel, BorderLayout.NORTH);
            bottomPanel.add(progressBar, BorderLayout.SOUTH);

            mainPanel.add(centerPanel, BorderLayout.CENTER);
            mainPanel.add(bottomPanel, BorderLayout.SOUTH);

            add(mainPanel);

            // Border tipis putih
            getRootPane().setBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 1));
        }

        /** Memperbarui nilai progress bar dan label status */
        public void setProgress(int value) {
            progressBar.setValue(value);
            if (value < 30) {
                statusLabel.setText("Memuat komponen UI...");
            } else if (value < 60) {
                statusLabel.setText("Menghubungkan ke database...");
            } else if (value < 90) {
                statusLabel.setText("Menyiapkan data...");
            } else {
                statusLabel.setText("Siap!");
            }
        }
    }
}
