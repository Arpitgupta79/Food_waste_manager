package gui;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class RecipePanel extends JPanel {

    private DefaultListModel<String> recipeListModel;
    private JList<String> recipeList;
    private JComboBox<String> filterCombo;
    private JLabel lastUpdatedLabel;

    public RecipePanel() {
        setLayout(new BorderLayout(10, 10));

        // === Title ===
        JLabel title = new JLabel("Suggested Recipes", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        add(title, BorderLayout.NORTH);

        // === Recipe List ===
        recipeListModel = new DefaultListModel<>();
        recipeList = new JList<>(recipeListModel);
        recipeList.setFont(new Font("SansSerif", Font.PLAIN, 14));
        add(new JScrollPane(recipeList), BorderLayout.CENTER);

        // === Filter Panel ===
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Filter by category:"));
        filterCombo = new JComboBox<>(new String[]{
            "All", "Shakes", "Fruits", "Vegetarian Food", "Snacks", "Non-Vegetarian Food"
        });
        filterPanel.add(filterCombo);
        JButton refreshBtn = new JButton("Refresh");
        JButton suggestFromInventoryBtn = new JButton("Suggest From Inventory");
        filterPanel.add(suggestFromInventoryBtn);
        filterPanel.add(refreshBtn);
        refreshBtn.addActionListener(e -> {
            String selectedCategory = (String) filterCombo.getSelectedItem();
            updateRecipeList(recipe ->
                selectedCategory.equals("All") ||
                recipe.getCategory().equalsIgnoreCase(selectedCategory)
            );
        });

        // === Last Updated Label ===
        lastUpdatedLabel = new JLabel("Last Updated: ");
        lastUpdatedLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // === Bottom Panel ===
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.add(filterPanel);
        bottomPanel.add(lastUpdatedLabel);
        add(bottomPanel, BorderLayout.SOUTH);

        // === Event: Filter Recipes on Selection ===
        filterCombo.addActionListener(e -> {
            String selectedCategory = (String) filterCombo.getSelectedItem();
            updateRecipeList(recipe ->
                selectedCategory.equals("All") ||
                recipe.getCategory().equalsIgnoreCase(selectedCategory)
            );
        });
        suggestFromInventoryBtn.addActionListener(e -> suggestRecipesFromInventory());

        // === Initial Load ===
        updateRecipeList(recipe -> true);
    }

    // === Functional Interface ===
    @FunctionalInterface
    public interface RecipeFilter {
        boolean matches(model.Recipe recipe);
    }
    
    // === Load Recipes and Apply Filter ===
    private void updateRecipeList(RecipeFilter filter) {
        recipeListModel.clear();

        List<model.Recipe> allRecipes = dao.RecipeDAO.getAllRecipes();

        allRecipes.stream()
                  .filter(filter::matches)
                  .forEach(r -> recipeListModel.addElement(
                      r.getName() + " → " + r.getIngredients()
                  ));

        lastUpdatedLabel.setText("Last Updated: " + new java.util.Date());
    }
    private void suggestRecipesFromInventory() {
        recipeListModel.clear();
    
        List<String> availableItems = dao.InventoryDAO.getAllItemNames(); // fetch from inventory
        List<model.Recipe> allRecipes = dao.RecipeDAO.getAllRecipes();
    
        for (model.Recipe recipe : allRecipes) {
            String[] ingredients = recipe.getIngredients().split(",\\s*");
    
            // check if all or most ingredients are present
            long matched = java.util.Arrays.stream(ingredients)
                                .filter(item -> availableItems.stream().anyMatch(i -> i.equalsIgnoreCase(item)))
                                .count();
    
            double matchPercent = (double) matched / ingredients.length;
    
            if (matchPercent >= 0.6) { // suggest recipes with at least 60% ingredient match
                recipeListModel.addElement("✅ " + recipe.getName() + " → " + recipe.getIngredients());
            }
        }
    
        lastUpdatedLabel.setText("Suggested from Inventory at: " + new java.util.Date());
    }
}