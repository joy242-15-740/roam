package com.roam.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.stage.StageStyle;

import java.util.Optional;

public class DialogUtils {

    /**
     * Show confirmation dialog with proper theming
     * 
     * @return true if OK/Yes clicked, false if Cancel/No clicked
     */
    public static boolean showConfirmation(String title, String header, String content) {
        Alert alert = ThemeManager.getInstance().createAlert(
                Alert.AlertType.CONFIRMATION, title, header, content);
        styleDialogPane(alert);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Show error dialog with proper theming
     */
    public static void showError(String title, String header, String content) {
        Alert alert = ThemeManager.getInstance().createAlert(
                Alert.AlertType.ERROR, title, header, content);
        styleDialogPane(alert);
        alert.showAndWait();
    }

    /**
     * Show information dialog with proper theming
     */
    public static void showInfo(String title, String header, String content) {
        Alert alert = ThemeManager.getInstance().createAlert(
                Alert.AlertType.INFORMATION, title, header, content);
        styleDialogPane(alert);
        alert.showAndWait();
    }

    /**
     * Show success message (simple info dialog) with proper theming
     */
    public static void showSuccess(String message) {
        Alert alert = ThemeManager.getInstance().createAlert(
                Alert.AlertType.INFORMATION, "Success", null, message);
        styleDialogPane(alert);
        alert.showAndWait();
    }

    /**
     * Apply consistent styling to any dialog
     */
    public static void styleDialogPane(Dialog<?> dialog) {
        if (dialog == null)
            return;

        DialogPane dialogPane = dialog.getDialogPane();

        // Apply rounded corners style
        dialogPane.setStyle(dialogPane.getStyle() +
                "-fx-background-radius: 20px; -fx-border-radius: 20px;");

        // Make the dialog window itself have rounded corners
        dialog.initStyle(StageStyle.TRANSPARENT);
    }
}
