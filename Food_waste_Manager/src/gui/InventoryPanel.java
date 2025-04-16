package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import java.awt.*;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.function.Predicate;

public class InventoryPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JComboBox<String> filterCombo;
    private JLabel statusLabel;
    private JTextArea expiringSoonArea;

    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final long EXPIRING_THRESHOLD_DAYS = 7; // Threshold for "Expiring Soon"

    public InventoryPanel() {
        setLayout(new BorderLayout());

        // Top layout with title and filter
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Inventory", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Category:"));
        filterCombo = new JComboBox<>(new String[]{"All", "Dairy", "Fruits", "Vegetables", "Grains", "Non Veg", "Others"});
        filterPanel.add(filterCombo);

        top.add(title);
        top.add(filterPanel);
        add(top, BorderLayout.NORTH);

        // Table setup
        String[] columns = {"Name", "Category", "Quantity", "Expiration Date"};
        model = new DefaultTableModel(columns, 0);
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        resizeColumnWidth(table);

        // Expiring soon panel
        JPanel expiringPanel = new JPanel(new BorderLayout());
        expiringPanel.setBorder(BorderFactory.createTitledBorder("Expiring Soon"));
        expiringSoonArea = new JTextArea(5, 20);
        expiringSoonArea.setEditable(false);
        expiringSoonArea.setBorder(BorderFactory.createLineBorder(Color.RED, 3, true));
        expiringPanel.add(new JScrollPane(expiringSoonArea), BorderLayout.CENTER);
        add(expiringPanel, BorderLayout.EAST);

        // Buttons
        JPanel buttonPanel = new JPanel();
        JButton addBtn = new JButton("Add Item");
        JButton removeBtn = new JButton("Remove Item");
        JButton refreshBtn = new JButton("Refresh");

        buttonPanel.add(addBtn);
        buttonPanel.add(removeBtn);
        buttonPanel.add(refreshBtn);

        // Status
        statusLabel = new JLabel("Last updated: ");
        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
        southPanel.add(buttonPanel);
        southPanel.add(statusLabel);

        add(southPanel, BorderLayout.SOUTH);

        // Event handlers
        ActionListener updateAction = e -> loadItems(item -> {
            String filter = (String) filterCombo.getSelectedItem();
            return filter.equals("All") || item.category.equalsIgnoreCase(filter);
        });

        addBtn.addActionListener(e -> addItem());
        removeBtn.addActionListener(e -> removeSelectedItem());
        refreshBtn.addActionListener(updateAction);
        filterCombo.addActionListener(updateAction);

        // Initial load
        updateAction.actionPerformed(null);
    }

    private void removeSelectedItem() {
        int row = table.getSelectedRow();
        if (row != -1) {
            String name = model.getValueAt(row, 0).toString();
            executor.submit(() -> {
                try (java.sql.Connection conn = database.DBConnection.getConnection();
                     java.sql.PreparedStatement ps = conn.prepareStatement("DELETE FROM inventory WHERE name = ?")) {
                    ps.setString(1, name);
                    ps.executeUpdate();
                    SwingUtilities.invokeLater(() -> model.removeRow(row));
                } catch (Exception ex) {
                    showError(ex);
                }
            });
        }
    }

    private void resizeColumnWidth(JTable table) {
        final TableColumnModel columnModel = table.getColumnModel();
        for (int column = 0; column < table.getColumnCount(); column++) {
            int width = 15;
            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer renderer = table.getCellRenderer(row, column);
                Component comp = table.prepareRenderer(renderer, row, column);
                width = Math.max(comp.getPreferredSize().width + 1, width);
            }
            if (width > 300)
                width = 300;
            columnModel.getColumn(column).setPreferredWidth(width);
        }
    }

    private void loadItems(Predicate<Item<String, String, String, String>> filter) {
        executor.submit(() -> {
            try {
                List<model.FoodItem> foodItems = Controller.Controller.getAllItem();
                List<Item<String, String, String, String>> items = new ArrayList<>();
                for (model.FoodItem fi : foodItems) {
                    items.add(new Item<>(
                            fi.getname(),
                            fi.getcategory(),
                            fi.getquantity(),
                            fi.getexpdate().toString()
                    ));
                }

                SwingUtilities.invokeLater(() -> {
                    model.setRowCount(0);
                    expiringSoonArea.setText("");
                    for (Item<String, String, String, String> item : items) {
                        if (filter == null || filter.test(item)) {
                            model.addRow(item.toObjectArray());
                            if (isExpiringSoon(item.expiration)) {
                                expiringSoonArea.append(item.name + " - " + item.expiration + "\n");
                            }
                        }
                    }
                    statusLabel.setText("Last updated: " + new java.util.Date());
                });
            } catch (Exception e) {
                showError(e);
            }
        });
    }

    private boolean isExpiringSoon(String expirationDateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            java.util.Date expirationDate = sdf.parse(expirationDateStr);
            java.util.Date currentDate = new java.util.Date();
            long diffInMillies = expirationDate.getTime() - currentDate.getTime();
            long diffInDays = diffInMillies / (1000 * 60 * 60 * 24);
            return diffInDays <= EXPIRING_THRESHOLD_DAYS && diffInDays >= 0;
        } catch (Exception e) {
            return false;
        }
    }

    private void addItem() {
        String name = JOptionPane.showInputDialog(this, "Enter the product name:");
        String category = JOptionPane.showInputDialog(this, "Enter the product category:");
        String quantity = JOptionPane.showInputDialog(this, "Enter the quantity:");
        String expdate = JOptionPane.showInputDialog(this, "Enter the expiry date (yyyy-mm-dd):");

        if (name != null && category != null && quantity != null && expdate != null) {
            Controller.Controller.addItem(name, category, quantity, expdate);
            loadItems(item -> {
                String filter = (String) filterCombo.getSelectedItem();
                return filter.equals("All") || item.category.equalsIgnoreCase(filter);
            });
        }
    }

    private void showError(Exception e) {
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE));
    }

    // ✅ Generic Static Nested Class
    static class Item<N, C, Q, E> {
        N name;
        C category;
        Q quantity;
        E expiration;

        Item(N name, C category, Q quantity, E expiration) {
            this.name = name;
            this.category = category;
            this.quantity = quantity;
            this.expiration = expiration;
        }

        Object[] toObjectArray() {
            return new Object[]{name, category, quantity, expiration};
        }
    }
}