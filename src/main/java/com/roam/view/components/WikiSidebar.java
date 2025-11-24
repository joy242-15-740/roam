package com.roam.view.components;

import com.roam.controller.WikiController;
import com.roam.model.Wiki;
import com.roam.model.Operation;
import com.roam.util.ExportUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class WikiSidebar extends VBox {

    private final WikiController controller;
    private final Font poppinsRegular;
    private final Font poppinsBold;

    private VBox favoritesContent;
    private VBox recentContent;
    private VBox operationWikisContent;
    private VBox operationWikisSection;

    public WikiSidebar(WikiController controller, Font poppinsRegular, Font poppinsBold) {
        this.controller = controller;
        this.poppinsRegular = poppinsRegular;
        this.poppinsBold = poppinsBold;

        configureSidebar();
        buildSections();
    }

    private void configureSidebar() {
        setMinWidth(280);
        setPrefWidth(280);
        setMaxWidth(280);
        setStyle("-fx-background-color: -roam-gray-bg; -fx-border-color: -roam-border; -fx-border-width: 0 1 0 0;");
        setPadding(new Insets(15));
        setSpacing(20);
    }

    private void buildSections() {
        VBox favoritesSection = createFavoritesSection();
        VBox recentSection = createRecentSection();
        operationWikisSection = createOperationWikisSection();

        // Initially hide operation wikis section
        operationWikisSection.setVisible(false);
        operationWikisSection.setManaged(false);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox quickActions = createQuickActions();

        getChildren().addAll(
                favoritesSection,
                recentSection,
                operationWikisSection,
                spacer,
                quickActions);
    }

    private VBox createOperationWikisSection() {
        VBox section = new VBox(10);

        // Header
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        FontIcon icon = new FontIcon(Feather.LIST);
        icon.setIconSize(14);

        Label title = new Label("Operation Wikis");
        title.setFont(Font.font(poppinsBold.getFamily(), 15));

        header.getChildren().addAll(icon, title);

        // Content container
        operationWikisContent = new VBox(5);
        operationWikisContent.setPadding(new Insets(10, 0, 0, 0));

        section.getChildren().addAll(header, operationWikisContent);
        return section;
    }

    private VBox createFavoritesSection() {
        VBox section = new VBox(10);

        // Header
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        FontIcon icon = new FontIcon(Feather.STAR);
        icon.setIconSize(14);

        Label title = new Label("Favorites");
        title.setFont(Font.font(poppinsBold.getFamily(), 15));

        header.getChildren().addAll(icon, title);

        // Content container
        favoritesContent = new VBox(5);
        favoritesContent.setPadding(new Insets(10, 0, 0, 0));

        section.getChildren().addAll(header, favoritesContent);
        return section;
    }

    private VBox createRecentSection() {
        VBox section = new VBox(10);

        // Header
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        FontIcon icon = new FontIcon(Feather.CLOCK);
        icon.setIconSize(14);

        Label title = new Label("Recent");
        title.setFont(Font.font(poppinsBold.getFamily(), 15));

        header.getChildren().addAll(icon, title);

        // Content container
        recentContent = new VBox(5);
        recentContent.setPadding(new Insets(10, 0, 0, 0));

        section.getChildren().addAll(header, recentContent);
        return section;
    }

    private VBox createQuickActions() {
        VBox actions = new VBox(10);

        Button exportBtn = new Button("Export");
        exportBtn.setGraphic(new FontIcon(Feather.UPLOAD));
        exportBtn.setPrefWidth(250);
        exportBtn.setPrefHeight(40);
        exportBtn.setAlignment(Pos.CENTER_LEFT);
        exportBtn.setPadding(new Insets(10));
        exportBtn.setFont(Font.font(poppinsRegular.getFamily(), 13));
        exportBtn.setStyle(
                "-fx-background-color: -roam-bg-primary; " +
                        "-fx-border-color: -roam-border; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 6; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;");
        exportBtn.setOnAction(e -> {
            if (controller.getCurrentNote() != null) {
                ExportUtils.exportNoteToMarkdown(controller.getCurrentNote(), getScene().getWindow());
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export");
                alert.setHeaderText("No Wiki selected");
                alert.setContentText("Please select a Wiki to export.");
                alert.showAndWait();
            }
        });

        actions.getChildren().add(exportBtn);
        return actions;
    }

    private HBox createNoteItem(Wiki Wiki) {
        HBox item = new HBox();
        item.setPrefHeight(60);
        item.setPadding(new Insets(8));
        item.setStyle("-fx-background-color: transparent; -fx-background-radius: 6; -fx-cursor: hand;");

        item.setOnMouseEntered(e -> item.setStyle(
                "-fx-background-color: -roam-bg-primary; -fx-background-radius: 6; -fx-cursor: hand;"));
        item.setOnMouseExited(e -> item.setStyle(
                "-fx-background-color: transparent; -fx-background-radius: 6; -fx-cursor: hand;"));

        VBox content = new VBox(4);
        content.setAlignment(Pos.TOP_LEFT);

        // Title
        Label titleLabel = new Label(Wiki.getTitle());
        titleLabel.setFont(Font.font(poppinsRegular.getFamily(), 13));
        titleLabel.setMaxWidth(250);
        titleLabel.setStyle("-fx-text-fill: -roam-text-primary;");

        // Meta info
        HBox meta = new HBox(8);
        meta.setAlignment(Pos.CENTER_LEFT);

        Label timeLabel = new Label(formatRelativeTime(Wiki.getUpdatedAt()));
        timeLabel.setFont(Font.font(poppinsRegular.getFamily(), 11));
        timeLabel.setStyle("-fx-text-fill: -roam-text-hint;");

        // Add region if exists (replaces tags)
        if (Wiki.getRegion() != null && !Wiki.getRegion().isEmpty()) {
            Label regionLabel = new Label(Wiki.getRegion());
            regionLabel.setFont(Font.font(poppinsRegular.getFamily(), 10));
            regionLabel.setPadding(new Insets(2, 6, 2, 6));
            regionLabel.setStyle(
                    "-fx-background-color: -roam-blue-light; " +
                            "-fx-background-radius: 8; " +
                            "-fx-text-fill: -roam-blue;");
            meta.getChildren().add(regionLabel);
        }

        meta.getChildren().add(timeLabel);

        content.getChildren().addAll(titleLabel, meta);
        item.getChildren().add(content);

        // Click action
        item.setOnMouseClicked(e -> {
            controller.setCurrentNote(Wiki);
        });

        return item;
    }

    private String formatRelativeTime(LocalDateTime dateTime) {
        if (dateTime == null)
            return "";

        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(dateTime, now);

        if (minutes < 60) {
            return minutes + "m ago";
        }

        long hours = ChronoUnit.HOURS.between(dateTime, now);
        if (hours < 24) {
            return hours + "h ago";
        }

        long days = ChronoUnit.DAYS.between(dateTime, now);
        if (days < 7) {
            return days + "d ago";
        }

        return dateTime.format(DateTimeFormatter.ofPattern("MMM d"));
    }

    // Public refresh methods
    public void refreshFavorites() {
        favoritesContent.getChildren().clear();
        List<Wiki> favorites = controller.loadFavoriteNotes();

        if (favorites.isEmpty()) {
            Label emptyLabel = new Label("No favorites yet");
            emptyLabel.setFont(Font.font(poppinsRegular.getFamily(), 12));
            emptyLabel.setStyle("-fx-text-fill: -roam-text-hint;");
            favoritesContent.getChildren().add(emptyLabel);
        } else {
            int limit = Math.min(favorites.size(), 5);
            for (int i = 0; i < limit; i++) {
                favoritesContent.getChildren().add(createNoteItem(favorites.get(i)));
            }
        }
    }

    public void refreshRecent() {
        recentContent.getChildren().clear();
        List<Wiki> recent = controller.loadRecentNotes(10);

        if (recent.isEmpty()) {
            Label emptyLabel = new Label("No wikis yet");
            emptyLabel.setFont(Font.font(poppinsRegular.getFamily(), 12));
            emptyLabel.setStyle("-fx-text-fill: -roam-text-hint;");
            recentContent.getChildren().add(emptyLabel);
        } else {
            for (Wiki Wiki : recent) {
                recentContent.getChildren().add(createNoteItem(Wiki));
            }
        }
    }

    public void refreshAll() {
        refreshFavorites();
        refreshRecent();
    }

    public void switchToOperationMode(Operation operation) {
        // Hide standard sections
        favoritesContent.getParent().setVisible(false);
        favoritesContent.getParent().setManaged(false);
        recentContent.getParent().setVisible(false);
        recentContent.getParent().setManaged(false);

        // Show operation wikis section
        operationWikisSection.setVisible(true);
        operationWikisSection.setManaged(true);

        // Update title
        if (!operationWikisSection.getChildren().isEmpty()
                && operationWikisSection.getChildren().get(0) instanceof HBox) {
            HBox header = (HBox) operationWikisSection.getChildren().get(0);
            if (header.getChildren().size() > 1 && header.getChildren().get(1) instanceof Label) {
                ((Label) header.getChildren().get(1)).setText(operation.getName());
            }
        }

        refreshOperationWikis(operation);
    }

    public void refreshOperationWikis(Operation operation) {
        operationWikisContent.getChildren().clear();
        List<Wiki> wikis = controller.loadNotesForOperation(operation);

        if (wikis.isEmpty()) {
            Label emptyLabel = new Label("No wikis yet");
            emptyLabel.setFont(Font.font(poppinsRegular.getFamily(), 12));
            emptyLabel.setStyle("-fx-text-fill: -roam-text-hint;");
            operationWikisContent.getChildren().add(emptyLabel);
        } else {
            for (Wiki wiki : wikis) {
                operationWikisContent.getChildren().add(createNoteItem(wiki));
            }
        }
    }
}
