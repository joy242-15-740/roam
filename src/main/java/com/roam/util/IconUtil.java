package com.roam.util;

import javafx.scene.Node;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Utility class for creating consistent icons throughout the application
 * using AtlantaFX's Feather icon pack
 */
public class IconUtil {

    /**
     * Create a FontIcon with default size (16)
     */
    public static FontIcon createIcon(Feather icon) {
        return createIcon(icon, 16);
    }

    /**
     * Create a FontIcon with custom size
     */
    public static FontIcon createIcon(Feather icon, int size) {
        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconSize(size);
        return fontIcon;
    }

    /**
     * Create an icon with custom style
     */
    public static FontIcon createIcon(Feather icon, int size, String style) {
        FontIcon fontIcon = createIcon(icon, size);
        fontIcon.setStyle(style);
        return fontIcon;
    }

    // Common icon shortcuts
    public static Node star() {
        return createIcon(Feather.STAR);
    }

    public static Node starFilled() {
        FontIcon icon = createIcon(Feather.STAR);
        icon.setStyle("-fx-fill: #FFD700;");
        return icon;
    }

    public static Node calendar() {
        return createIcon(Feather.CALENDAR);
    }

    public static Node clipboard() {
        return createIcon(Feather.CLIPBOARD);
    }

    public static Node checkSquare() {
        return createIcon(Feather.CHECK_SQUARE);
    }

    public static Node fileText() {
        return createIcon(Feather.FILE_TEXT);
    }

    public static Node edit() {
        return createIcon(Feather.EDIT);
    }

    public static Node search() {
        return createIcon(Feather.SEARCH);
    }

    public static Node settings() {
        return createIcon(Feather.SETTINGS);
    }

    public static Node trash() {
        return createIcon(Feather.TRASH_2);
    }

    public static Node image() {
        return createIcon(Feather.IMAGE);
    }

    public static Node file() {
        return createIcon(Feather.FILE);
    }

    public static Node book() {
        return createIcon(Feather.BOOK);
    }

    public static Node moreVertical() {
        return createIcon(Feather.MORE_VERTICAL);
    }

    public static Node refreshCw() {
        return createIcon(Feather.REFRESH_CW);
    }

    public static Node copy() {
        return createIcon(Feather.COPY);
    }

    public static Node download() {
        return createIcon(Feather.DOWNLOAD);
    }

    public static Node clock() {
        return createIcon(Feather.CLOCK);
    }

    public static Node upload() {
        return createIcon(Feather.UPLOAD);
    }

    public static Node check() {
        return createIcon(Feather.CHECK);
    }
}
