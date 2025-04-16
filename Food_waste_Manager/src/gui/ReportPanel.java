package gui;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

public class ReportPanel extends JPanel {

    private JLabel wasteLabel;
    private JLabel categoryLabel;
    private JLabel footer;
    private JTextArea reportArea;

    public ReportPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // === Title ===
        JLabel titleLabel = new JLabel("Summary");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(titleLabel, BorderLayout.NORTH);

        // === Summary Labels ===
        wasteLabel = new JLabel("Waste Reduced: 0 kg");
        wasteLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        wasteLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        categoryLabel = new JLabel("Top Category: Unknown");
        categoryLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        categoryLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setBackground(Color.WHITE);
        summaryPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        summaryPanel.add(wasteLabel);
        summaryPanel.add(categoryLabel);

        // === Report Text Area ===
        reportArea = new JTextArea(10, 40);
        reportArea.setEditable(false);
        reportArea.setBorder(BorderFactory.createTitledBorder("Report Details"));
        JScrollPane scrollPane = new JScrollPane(reportArea);

        // === Buttons ===
        JButton generateBtn = new JButton("Generate Report");
        generateBtn.setFont(new Font("SansSerif", Font.PLAIN, 14));
        generateBtn.addActionListener(e -> generateReport());

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(new Font("SansSerif", Font.PLAIN, 14));
        refreshBtn.addActionListener(e -> refreshReport());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(generateBtn);

        // === Center Layout ===
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(summaryPanel);
        centerPanel.add(Box.createVerticalStrut(15));
        centerPanel.add(scrollPane);
        centerPanel.add(Box.createVerticalStrut(15));
        centerPanel.add(buttonPanel);

        add(centerPanel, BorderLayout.CENTER);

        // === Footer ===
        footer = new JLabel("Last Updated: " + getCurrentDateTime());
        footer.setFont(new Font("SansSerif", Font.PLAIN, 12));
        footer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(footer, BorderLayout.SOUTH);
    }

    private void generateReport() {
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM inventory");
             ResultSet rs = ps.executeQuery()) {

            LocalDate today = LocalDate.now();
            List<String> wasted = new ArrayList<>();
            List<String> fresh = new ArrayList<>();

            while (rs.next()) {
                String name = rs.getString("name");
                String expiration = rs.getString("expiration");
                LocalDate expDate = LocalDate.parse(expiration);

                if (expDate.isBefore(today)) {
                    wasted.add(name + " (Expired on " + expiration + ")");
                } else {
                    fresh.add(name + " (Good till " + expiration + ")");
                }
            }

            reportArea.setText(""); // clear previous
            reportArea.append("🍂 Wasted Items:\n");
            if (wasted.isEmpty()) {
                reportArea.append("  None 🎉\n");
            } else {
                wasted.forEach(item -> reportArea.append("  - " + item + "\n"));
            }

            reportArea.append("\n🥦 Fresh Items:\n");
            if (fresh.isEmpty()) {
                reportArea.append("  None 😢\n");
            } else {
                fresh.forEach(item -> reportArea.append("  - " + item + "\n"));
            }

            wasteLabel.setText("Waste Reduced: " + wasted.size() + " items");
            categoryLabel.setText("Top Category: (based on available data)");
            footer.setText("Last Updated: " + getCurrentDateTime());

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void refreshReport() {
        wasteLabel.setText("Waste Reduced: 0 kg");
        categoryLabel.setText("Top Category: Unknown");
        reportArea.setText("");
        footer.setText("Last Updated: " + getCurrentDateTime());
    }

    private String getCurrentDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy, h:mm a");
        return now.format(formatter);
    }
}