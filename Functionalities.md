# JavaFX Application Functionalities

This document details the functional requirements, data models, and interaction logic for the JavaFX port of the "Roam" application.

## 1. Core Architecture

### 1.1. Data Persistence
*   **Mechanism:** Local JSON file storage (simulating the React `DataService`).
*   **Files:** `operations.json`, `tasks.json`, `notes.json`, `events.json`, `journal.json`, `settings.json`.
*   **Service:** A singleton `DataService` class responsible for:
    *   Loading data on startup.
    *   Saving data on modification.
    *   Providing CRUD methods (get, add, update, delete).
    *   Handling Backup/Restore (Export/Import JSON).

### 1.2. Security & Authentication
*   **Context:** `SecurityContext` (Singleton).
*   **State:** `isAuthenticated` (boolean), `isLockEnabled` (boolean).
*   **Features:**
    *   **Setup:** User can set a 4-digit PIN.
    *   **Lock:** App locks on startup if enabled.
    *   **Unlock:** Validating PIN against stored hash/value.
    *   **Auto-lock:** (Optional) Lock after inactivity.

### 1.3. Navigation
*   **Router:** Centralized view switcher (replacing React Router).
*   **Logic:** Sidebar buttons trigger a change in the `Center` node of the main `BorderPane`.

---

## 2. Module Functionalities

### 2.1. Operations (Projects)
*   **Data Model:** `Operation` (id, name, purpose, status, priority, dueDate, region).
*   **Features:**
    *   **List:** View all operations with filtering (by name, region).
    *   **Create/Edit:** Modal form to set details.
    *   **Delete:** Remove operation (cascade or warn about linked tasks).
    *   **Detail View:**
        *   **Progress:** Calculate % based on linked tasks (Done / Total).
        *   **Tabs:** Filter linked Tasks, Events, and Notes by `operationId`.

### 2.2. Tasks
*   **Data Model:** `Task` (id, title, description, status, priority, dueDate, operationId, calendarEventId, noteId, region).
*   **Status Workflow:** TODO -> IN_PROGRESS -> DONE.
*   **Views:**
    *   **Kanban:** Drag and drop (or button click) to change status.
    *   **List:** Tabular view.
    *   **Timeline:** Group by Date.
    *   **Matrix:** Eisenhower Matrix logic:
        *   *Do First:* Urgent (Today/Overdue) + High Priority.
        *   *Schedule:* Not Urgent + High Priority.
        *   *Delegate:* Urgent + Low Priority.
        *   *Eliminate:* Not Urgent + Low Priority.
*   **Linking:** Can link to an Operation, a Calendar Event, and a Wiki Note.

### 2.3. Calendar
*   **Data Model:** `CalendarEvent` (id, title, date, time, description, operationId, noteId, region).
*   **Features:**
    *   **View Modes:** Month (Grid), Week (Columns), Day (List).
    *   **Interaction:** Click cell to add event. Click event to edit.
    *   **Recurrence:** (Not currently implemented, keep simple).
    *   **Linking:** Reciprocal linking with Tasks and Notes.

### 2.4. Wiki (Notes)
*   **Data Model:** `Note` (id, title, content, updatedAt, operationId, taskId, calendarEventId, bannerUrl, region).
*   **Features:**
    *   **Markdown:** Support for basic Markdown syntax (Headers, Lists, Bold/Italic).
    *   **Banner:** Upload image (store as Base64 string in JSON or file path).
    *   **Linking:** Dropdowns to link Note to Operation, Task, or Event.
    *   **Search:** Filter notes by title or content.

### 2.5. Journal
*   **Data Model:** `JournalEntry` (id, title, content, date, tags), `JournalTemplate` (id, name, content).
*   **Features:**
    *   **Daily Entry:** Quick create for "Today".
    *   **Templates:** Create/Apply templates (e.g., "Daily Reflection").
    *   **Editor:** Same Markdown editor as Wiki.

### 2.6. Statistics
*   **Logic:**
    *   Count Tasks by Status.
    *   Count Operations by Status.
    *   (Optional) Calculate completion velocity.

### 2.7. Settings
*   **Theme:** Toggle CSS stylesheet (Light/Dark).
*   **Data:**
    *   **Export:** Serialize all data to a single JSON file.
    *   **Import:** Parse JSON file and overwrite local storage.
*   **Security:** Enable/Disable PIN, Change PIN.

---

## 3. Cross-Cutting Logic

### 3.1. Regions (Tags)
*   **Concept:** "Life Areas" (Career, Finance, Health, etc.).
*   **Logic:**
    *   Pre-defined list + Custom user additions.
    *   Used for color-coding in Calendar and Badges in Lists.
    *   Stored in `settings.json` or separate `regions.json`.

### 3.2. Reciprocal Linking
*   **Logic:** When a Task is linked to a Note:
    *   Task stores `noteId`.
    *   Note stores `taskId`.
    *   Updating one side must update the other to maintain consistency.

### 3.3. Search & Filtering
*   **Global:** (Optional) Search bar in Sidebar.
*   **Local:** Each module (Ops, Wiki, Journal) has its own text-based filter.

---

## 4. Technical Requirements (JavaFX)

*   **JDK:** 17 or higher (21 recommended).
*   **Libraries:**
    *   `Jackson` or `Gson`: For JSON parsing.
    *   `Ikonli`: For Icons.
    *   `FlexBox` (optional) or standard `FlowPane`/`HBox`/`VBox` for layouts.
    *   `CommonMark` or similar: For Markdown rendering (or a simple `WebView` wrapper).
