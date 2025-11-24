# JavaFX UI/UX Design Specification

This document outlines the UI/UX design requirements for porting the React "Roam" application to JavaFX. It includes layout structures, component mappings, and CSS styling details to replicate the modern, clean aesthetic.

## 1. General Design System

### 1.1. Theme & Colors
The application supports Light and Dark modes. Use CSS variables (looked up via `.root`) to manage dynamic theming.

**Color Palette (Tailwind Mapping):**

| Variable | Light Mode (Tailwind) | Dark Mode (Tailwind) | JavaFX CSS Variable |
| :--- | :--- | :--- | :--- |
| Background | `#FAFAFA` (slate-50) | `#020617` (slate-950) | `-fx-bg-base` |
| Surface (Card/Nav) | `#FFFFFF` (white) | `#0F172A` (slate-900) | `-fx-bg-surface` |
| Surface Hover | `#F8FAFC` (slate-50) | `#1E293B` (slate-800) | `-fx-bg-surface-hover` |
| Border | `#E2E8F0` (slate-200) | `#334155` (slate-700) | `-fx-border-color` |
| Text Primary | `#0F172A` (slate-900) | `#FFFFFF` (white) | `-fx-text-primary` |
| Text Secondary | `#64748B` (slate-500) | `#94A3B8` (slate-400) | `-fx-text-secondary` |
| Primary (Blue) | `#2563EB` (blue-600) | `#3B82F6` (blue-500) | `-fx-color-primary` |
| Success (Emerald) | `#10B981` (emerald-500) | `#10B981` (emerald-500) | `-fx-color-success` |
| Warning (Amber) | `#F59E0B` (amber-500) | `#F59E0B` (amber-500) | `-fx-color-warning` |
| Danger (Red) | `#EF4444` (red-500) | `#EF4444` (red-500) | `-fx-color-danger` |

### 1.2. Typography
*   **Font Family:** Inter, Segoe UI, or System Default.
*   **Sizes:**
    *   H1: 24px (Bold)
    *   H2: 20px (Bold)
    *   Body: 14px (Regular)
    *   Small: 12px (Regular)

### 1.3. Icons
*   Use **Ikonli** (Feather or Material Design packs) to replicate `lucide-react` icons.
*   Icon size: Generally 16px - 20px.

---

## 2. Layout Structure

### 2.1. Main Window (`BorderPane`)
*   **Left (`Left`):** Sidebar Navigation.
*   **Center (`Center`):** Main Content Area (swappable views).
*   **Root Style:** Background color set to `-fx-bg-base`.

### 2.2. Sidebar (`VBox`)
*   **Width:** 256px (Expanded), 80px (Collapsed).
*   **Styling:**
    *   Background: `-fx-bg-surface`.
    *   Border Right: 1px solid `-fx-border-color`.
*   **Components:**
    *   **Header:** App Logo/Lock Icon (Top).
    *   **Navigation List:** `VBox` containing `Button` or `ToggleButton` items.
    *   **Footer:** Settings button (Bottom).
*   **Interaction:** Toggle button to collapse/expand width.

### 2.3. Content Area (`StackPane` or `AnchorPane`)
*   Holds the specific page views (Operations, Tasks, etc.).
*   Padding: 32px (p-8).

---

## 3. Component Specifications

### 3.1. Buttons
*   **Primary Button:**
    *   Background: `-fx-color-primary`.
    *   Text: White.
    *   Radius: 8px.
    *   Padding: 8px 16px.
*   **Ghost/Nav Button:**
    *   Background: Transparent.
    *   Text: `-fx-text-secondary`.
    *   Hover: `-fx-bg-surface-hover` + Text `-fx-text-primary`.
    *   Selected: Light Blue background + Blue text.

### 3.2. Cards
*   **Container:** `VBox` or `Pane`.
*   **Styling:**
    *   Background: `-fx-bg-surface`.
    *   Radius: 12px (rounded-xl).
    *   Border: 1px solid `-fx-border-color`.
    *   Effect: `DropShadow` (color: #00000010, radius: 5, offset Y: 2).

### 3.3. Modals (Dialogs)
*   Use `Stage` with `Modality.APPLICATION_MODAL`.
*   **Style:** Transparent stage style with a root `StackPane` having a semi-transparent black background (backdrop blur simulation).
*   **Content:** Centered Card with white/dark background, rounded corners, and shadow.

---

## 4. Screen-Specific Designs

### 4.1. Lock Screen
*   **Layout:** `StackPane` (Full Screen).
*   **Elements:**
    *   Lock Icon (Centered, Animated pulse on error).
    *   PIN Dots: `HBox` of 4 Circles. Filled state changes color.
    *   Numpad: `GridPane` (3x4) of Circular Buttons.
*   **Animation:** Shake animation on invalid PIN.

### 4.2. Operations View
*   **Header:** Title + "New Operation" Button.
*   **Filter Bar:** `HBox` with Search Field (`TextField` with icon) and Filter Button.
*   **Data Grid:** `TableView`.
    *   **Columns:** Name (Custom Cell with Title+Subtitle), Region (Icon+Text), Status (Badge), Priority (Badge), Due Date, Actions (MenuButton).
    *   **Row Styling:** Hover effect, cursor hand.

### 4.3. Operation Detail View
*   **Header Card:** Title, Status Badge, Progress Bar (`ProgressBar`), Due Date.
*   **Tabs:** `TabPane` (styled as pill buttons, not default tabs).
    *   **Tasks Tab:** List of Task Cards.
    *   **Calendar Tab:** List of Event Cards.
    *   **Timeline Tab:** Vertical `VBox` with connecting lines (custom drawing or CSS borders).
    *   **Notes Tab:** Grid of Note Cards.

### 4.4. Tasks Board
*   **View Switcher:** `HBox` of ToggleButtons (Kanban, List, Timeline, Matrix).
*   **Kanban View:** `HBox` containing 3 `VBox` columns (Todo, In Progress, Done).
    *   **Column:** Header + `ScrollPane` containing Task Cards.
    *   **Task Card:** Title, Priority Badge, Region Tag, Due Date.
*   **Matrix View:** `GridPane` (2x2). Each quadrant is a `VBox` with specific border colors.

### 4.5. Calendar
*   **Header:** Month/Week/Day Toggle, Navigation Arrows, "Today" Label.
*   **Grid:** `GridPane` (7 columns).
    *   **Month:** 5-6 rows. Cells contain `VBox` of small Event Labels.
    *   **Week/Day:** Time slots (vertical layout).
*   **Event Label:** Colored background based on Region.

### 4.6. Wiki / Journal
*   **Layout:** `SplitPane` or `HBox`.
    *   **Left (List):** Search Bar + `ListView` of entries.
    *   **Right (Editor):**
        *   **Read Mode:** `WebView` (for Markdown rendering) or `TextFlow`.
        *   **Edit Mode:** `TextArea` (Monospace font).
        *   **Toolbar:** Title Input, Save/Edit/Delete buttons.
*   **Wiki Specific:** Banner Image area at the top of the editor (ImageView).

### 4.7. Stats
*   **Layout:** `GridPane` or `FlowPane`.
*   **Charts:**
    *   **Bar Chart:** Task Status distribution.
    *   **Pie Chart:** Operation Status distribution.
*   **Summary Cards:** Simple Cards with Big Number + Label.

---

## 5. CSS Styling Snippets (Reference)

```css
.root {
    -fx-bg-base: #FAFAFA;
    -fx-bg-surface: #FFFFFF;
    -fx-border-color: #E2E8F0;
    -fx-text-primary: #0F172A;
    -fx-color-primary: #2563EB;
}

.root.dark {
    -fx-bg-base: #020617;
    -fx-bg-surface: #0F172A;
    -fx-border-color: #334155;
    -fx-text-primary: #FFFFFF;
}

.button-primary {
    -fx-background-color: -fx-color-primary;
    -fx-text-fill: white;
    -fx-background-radius: 8;
    -fx-cursor: hand;
}

.card {
    -fx-background-color: -fx-bg-surface;
    -fx-background-radius: 12;
    -fx-border-color: -fx-border-color;
    -fx-border-radius: 12;
    -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);
}

.text-field {
    -fx-background-color: -fx-bg-base;
    -fx-border-color: -fx-border-color;
    -fx-border-radius: 8;
    -fx-background-radius: 8;
}
```
