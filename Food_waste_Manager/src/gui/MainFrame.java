package gui;
import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("Eco-Friendly Food Waste Reducer");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Header
        JLabel header = new JLabel("Welcome to Your Food Waste Reducer", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 35));
        header.setOpaque(true);
        header.setBackground(new Color(200, 255, 200));
        add(header, BorderLayout.NORTH);

        // Tabbed Pane
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Inventory", new InventoryPanel());
        tabs.addTab("Recipes", new RecipePanel());
        tabs.addTab("Reports", new ReportPanel());
        add(tabs, BorderLayout.CENTER);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}