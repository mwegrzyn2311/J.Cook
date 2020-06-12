package jcook.controllers;

import com.mongodb.client.model.Filters;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jcook.filters.CategoryFilter;
import jcook.filters.CombinedFilter;
import jcook.filters.Filter;
import jcook.filters.NameFilter;
import jcook.models.Category;
import jcook.models.Recipe;
import jcook.providers.RecipeProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RecipeListController {

    private final CombinedFilter currentFilter = new CombinedFilter(Filters::and);
    private final RecipeProvider recipeProvider = RecipeProvider.getInstance();
    private final int fixedCellSize = 50;
    private final int fixedFilterCellSize = 30;

    @FXML
    TableView<Recipe> recipeTable;
    @FXML
    TableColumn<Recipe, Image> iconColumn;
    @FXML
    TableColumn<Recipe, String> nameColumn;

    // TODO: change to display stars
    @FXML
    TableColumn<Recipe, Void> ratingColumn;
    @FXML
    ListView<Filter> filtersList;
    @FXML
    VBox filterAddingList;

    private final List<VBox> filterForms = new ArrayList<>();

    @FXML
    ComboBox<Button> userButtons;

    @FXML
    Pane header;
    @FXML
    Button recipeFormButton;
    @FXML
    Button recipeListButton;
    @FXML
    GridPane recipeListContentPane;
    private StackPane mainPane;

    // TODO: Would be nice to have returned types as Observables

    @FXML
    public void initialize() {
        currentFilter.addFilter(new NameFilter(""));

        initRecipeTable();
        initFilterList();
        initFilterAddingList();
        initHeader();
    }

    private void initRecipeTable() {
        // TODO: Consider changing return types from Recipe to StringProperty, etc.
        this.recipeTable.setItems(FXCollections.observableList(recipeProvider.getObjects(currentFilter)));

        // On-click handler
        this.recipeTable.setRowFactory( param -> {
            TableRow<Recipe> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if(!row.isEmpty() && event.getClickCount() == 2) {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RecipeView.fxml"));
                        GridPane recipeViewPane = loader.load();
                        RecipeViewController recipeViewController = loader.getController();
                        recipeViewController.setRecipe(row.getItem());
                        final Stage recipeView = new Stage();
                        recipeView.initModality(Modality.APPLICATION_MODAL);
                        Scene recipeViewScene = new Scene(recipeViewPane, 600, 700);
                        recipeView.setScene(recipeViewScene);
                        recipeView.show();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });
            return row;
        });

        // Columns data factories
        this.iconColumn.setCellFactory(param -> {
            final ImageView imageView = new ImageView();
            imageView.setFitWidth(fixedCellSize);
            imageView.setFitHeight(fixedCellSize);
            TableCell<Recipe, Image> cell = new TableCell<>() {
                @Override
                public void updateItem(Image image, boolean empty) {
                    if (!empty) {
                        imageView.setImage(image);
                    }
                }
            };
            cell.setGraphic(imageView);
            return cell;
        });
        this.iconColumn.setCellValueFactory(new PropertyValueFactory<>("image"));
        this.nameColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getName()));

        this.ratingColumn.setCellFactory(param -> {
            final ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("/images/star.png")));
            imageView.setFitHeight(fixedCellSize*4/5.0);
            imageView.setFitWidth(fixedCellSize*4/5.0);
            return new TableCell<>() {
                @Override
                public void updateItem(Void item, boolean empty) {
                    if (empty) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(getTableRow().getItem().avgRating().toString());
                        setGraphic(imageView);
                    }
                }
            };
        });
    }

    private void initFilterList() {
        filtersList.setFixedCellSize(fixedFilterCellSize);
        filtersList.setItems(FXCollections.observableList(currentFilter.getFilters()));
        filtersList.setCellFactory(param -> {
            final HBox hbox = new HBox();
            hbox.setPrefWidth(filtersList.getPrefWidth());
            final Button removeFilterButton = new Button("X");
            final Label label = new Label("Empty");
            label.setPrefWidth(filtersList.getWidth()-removeFilterButton.getWidth());
            label.setTextAlignment(TextAlignment.LEFT);
            final Filter[] f = new Filter[1];

            removeFilterButton.addEventHandler(ActionEvent.ACTION, e -> {
                currentFilter.removeFilter(f[0]);
                filtersList.setItems(FXCollections.observableList(currentFilter.getFilters()));
                recipeTable.setItems(FXCollections.observableList(recipeProvider.getObjects(currentFilter)));
            });

            hbox.getChildren().addAll(label, removeFilterButton);
            return new ListCell<>() {
                @Override
                public void updateItem(Filter filter, boolean empty) {
                    super.updateItem(filter, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        label.setText(filter.toString());
                        f[0] = filter;
                        setGraphic(hbox);
                    }
                }
            };
        });
    }

    private void initHeader() {
        recipeFormButton.addEventHandler(ActionEvent.ACTION, e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RecipeForm.fxml"));
                GridPane recipeFormPane = loader.load();
                final Stage recipeForm = new Stage();
                recipeForm.initModality(Modality.APPLICATION_MODAL);
                Scene recipeFormScene = new Scene(recipeFormPane, 600, 700);
                recipeForm.setScene(recipeFormScene);
                recipeForm.show();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        // TODO: Fix it
        List<Button> buttons = new ArrayList<>();
        Button openProfileButton = new Button("Open profile");
        Button logOutButton = new Button("Log out");
        buttons.add(openProfileButton);
        buttons.add(logOutButton);
        ObservableList<Button> obsButtons = FXCollections.observableList(buttons);
        userButtons.setItems(obsButtons);
    }

    private void initFilterAddingList() {
        /* Name filter */
        VBox nameFilter = new VBox();
        nameFilter.getStyleClass().add("filter");
        TextField nameField = new TextField();
        Button addNameFilterButton = new Button("Add filter");
        addNameFilterButton.addEventHandler(ActionEvent.ACTION, e -> {
            currentFilter.addFilter(new NameFilter(nameField.getText()));
            recipeTable.setItems(FXCollections.observableList(recipeProvider.getObjects(currentFilter)));
            filtersList.setItems(FXCollections.observableList(currentFilter.getFilters()));
        });
        nameFilter.getChildren().addAll(new Label("name"), nameField, addNameFilterButton);
        filterForms.add(nameFilter);

        VBox categoryFilter = new VBox();
        categoryFilter.getStyleClass().add("filter");
        ComboBox<Category> categoryBox = new ComboBox<>();
        categoryBox.getItems().setAll(Category.values());
        Button addCategoryFilterButton = new Button("Add filter");
        addCategoryFilterButton.addEventHandler(ActionEvent.ACTION, e -> {
            currentFilter.addFilter(new CategoryFilter(categoryBox.getSelectionModel().getSelectedItem()));
            recipeTable.setItems(FXCollections.observableList(recipeProvider.getObjects(currentFilter)));
            filtersList.setItems(FXCollections.observableList(currentFilter.getFilters()));
        });
        nameFilter.getChildren().addAll(new Label("category"), categoryBox, addCategoryFilterButton);
        filterForms.add(categoryFilter);

        filterAddingList.getChildren().addAll(filterForms);
    }

    public void setMainPane(StackPane mainPane) {
        this.mainPane = mainPane;
    }
}
