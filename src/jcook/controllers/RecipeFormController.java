package jcook.controllers;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import jcook.models.Category;
import jcook.models.Ingredient;
import jcook.models.Recipe;
import jcook.providers.RecipeProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static javafx.scene.layout.GridPane.getRowIndex;

public class RecipeFormController {
    @FXML
    GridPane categoryPane;
    private final List<Category> categoryList = new LinkedList<>();
    private int categoryIndex = 0;
    @FXML
    GridPane ingredientPane;
    private final List<Ingredient> ingredientList = new LinkedList<>();
    private int ingredientIndex = 0;
    @FXML
    GridPane tagPane;
    private final List<String> tagList = new LinkedList<>();
    private int tagIndex = 0;
    @FXML
    Button save;
    @FXML
    Button image;
    private byte[] imageBytes;
    @FXML
    TextField name;
    @FXML
    TextArea description;

    private BiConsumer<GridPane, Button> nextCategory(int index) {
        return (grid, add) -> {
            ComboBox<Category> categories = new ComboBox<>(FXCollections.observableArrayList(Category.values()));
            categories.setPromptText("category");
            categories.setPrefWidth(500.0);
            categories.valueProperty().addListener((a, oldValue, newValue) -> {
                add.setDisable(categories.getValue() == null);
                categoryList.set(getRowIndex(add), newValue);
            });
            grid.addRow(index, categories, add);
        };
    }

    private int getCategoryIndex(int d) {
        categoryIndex+=d;
        return categoryIndex-d;
    }

    private BiConsumer<GridPane, Button> nextIngredient(int index) {
        return (grid, add) -> {
            TextField ingredient = new TextField();
            ingredient.setPromptText("ingredient");
            ingredient.setPrefWidth(400.0);
            TextField quantity = new TextField();
            quantity.setPrefWidth(100.0);
            quantity.setPromptText("quantity");
            TextField unit = new TextField();
            unit.setPrefWidth(50.0);
            unit.setPromptText("unit");
            ChangeListener<? super String> listener = (a, oldValue, newValue) -> {
                add.setDisable(ingredient.getText().isBlank() || quantity.getText().isBlank());
                if (!quantity.getText().isBlank()) ingredientList.set(
                        getRowIndex(add),
                        new Ingredient(
                                ingredient.getText(),
                                Double.parseDouble(quantity.getText()),
                                unit.getText()
                        )
                );
            };
            ingredient.textProperty().addListener(listener);
            quantity.textProperty().addListener(listener);
            unit.textProperty().addListener(listener);
            grid.addRow(index, ingredient, quantity, unit, add);
        };
    }

    private int getIngredientIndex(int d) {
        ingredientIndex+=d;
        return ingredientIndex-d;
    }

    private BiConsumer<GridPane, Button> nextTag(int index) {
        return (grid, add) -> {
            TextField tag = new TextField();
            tag.setPromptText("tag");
            tag.setPrefWidth(500.0);
            tag.textProperty().addListener((a, oldValue, newValue) -> {
                add.setDisable(newValue.isEmpty());
                tagList.set(getRowIndex(add), newValue);
            });
            grid.addRow(index, add, tag);
        };
    }

    private int getTagIndex(int d) {
        tagIndex+=d;
        return tagIndex-d;
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    private <T> void insertRow(GridPane grid, List<T> values, Function<Integer, Integer> index,
                               Function<Integer, BiConsumer<GridPane, Button>> nextRow) {
        int rowIndex = index.apply(1);
        grid.getChildren().forEach(node -> {
            int row = getRowIndex(node);
            if (row >= rowIndex) {
                GridPane.setRowIndex(node, row + 1);
            }
        });

        Button add = new Button("+");
        add.setDisable(true);
        add.setOnAction(evt -> {
            insertRow(grid, values, index, nextRow);
            add.textProperty().setValue("--");
            add.setOnAction(ev -> {
                index.apply(-1);
                grid.getChildren().removeIf(node -> getRowIndex(node).equals(getRowIndex(add)));
                grid.getChildren().forEach(node -> {
                    int row = getRowIndex(node);
                    if (row >= getRowIndex(add)) {
                        GridPane.setRowIndex(node, row - 1);
                    }
                });
                values.remove(getRowIndex(add));
            });
        });

        values.add(rowIndex, null);
        nextRow.apply(rowIndex).accept(grid, add);
    }

    @FXML
    public void initialize() {
        insertRow(categoryPane, categoryList, this::getCategoryIndex, this::nextCategory);
        insertRow(ingredientPane, ingredientList, this::getIngredientIndex, this::nextIngredient);
        insertRow(tagPane, tagList, this::getTagIndex, this::nextTag);
        image.setOnAction(actionEvent -> {
                    FileChooser fileChooser = new FileChooser();
                    FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("images (*.jpg,*.jpeg, *.png)",
                            "*.jpg", "*.jpeg", "*.png");
                    fileChooser.getExtensionFilters().add(extFilter);
                    File file = fileChooser.showOpenDialog(null);
                    if (file != null) {
                        imageBytes = new byte[(int) file.length()];
                        try {
                            FileInputStream input = new FileInputStream(file);
                            input.read(imageBytes);
                            input.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
        save.setOnAction(event -> {
            RecipeProvider.getInstance().addObject(new Recipe(
                    name.getText(),
                    description.getText(),
                    imageBytes,
                    ingredientList,
                    tagList,
                    categoryList
            ));
            ((Stage) save.getScene().getWindow()).close();
        });
    }

    public void setRecipe(Recipe recipe) {
        name.textProperty().setValue(recipe.getName());
        description.textProperty().setValue(recipe.getDescription());
        recipe.getCategories().forEach(category -> {

            insertRow(categoryPane, categoryList, this::getCategoryIndex, this::nextCategory);
        });
    }
}
