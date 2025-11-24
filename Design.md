# Roam Application - Design Documentation

## Table of Contents
1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Technology Stack](#technology-stack)
4. [Package Structure](#package-structure)
5. [Core Models](#core-models)
6. [Controllers](#controllers)
7. [Repositories](#repositories)
8. [Views](#views)
9. [UI Components](#ui-components)
10. [Utilities & Services](#utilities--services)
11. [Design System](#design-system)
12. [Features](#features)

---

## Overview

**Roam** is a comprehensive project management and knowledge management application built with JavaFX. It combines operations management, task tracking, calendar scheduling, and wiki note-taking into a unified productivity platform.

### Key Capabilities
- **Operations Management**: Create and track operational projects with status, priority, and due dates
- **Task Management**: Kanban-style task boards with drag-and-drop, timeline views, and filtering
- **Calendar Integration**: Event scheduling with multiple calendar sources and color coding
- **Wiki System**: Markdown-based note-taking with tags, templates, and full-text search
- **Data Persistence**: Hibernate ORM with H2 database for robust data storage

---

## Architecture

### Application Pattern: MVC (Model-View-Controller)

```
┌─────────────────────────────────────────────────────────────┐
│                     RoamApplication                          │
│                  (JavaFX Application)                        │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                      MainLayout                              │
│            (Sidebar Navigation + Content Area)               │
└──────┬──────────┬──────────┬──────────┬─────────────────────┘
       │          │          │          │
       ▼          ▼          ▼          ▼
┌──────────┐ ┌──────────┐ ┌──────┐ ┌──────────┐
│Operations│ │ Calendar │ │Tasks │ │   Wiki   │
│   View   │ │   View   │ │ View │ │   View   │
└────┬─────┘ └────┬─────┘ └───┬──┘ └────┬─────┘
     │            │            │         │
     ▼            ▼            ▼         ▼
┌──────────┐ ┌──────────┐ ┌──────┐ ┌──────────┐
│Operations│ │ Calendar │ │Tasks │ │   Wiki   │
│Controller│ │Controller│ │Ctrl  │ │Controller│
└────┬─────┘ └────┬─────┘ └───┬──┘ └────┬─────┘
     │            │            │         │
     ▼            ▼            ▼         ▼
┌──────────┐ ┌──────────┐ ┌──────┐ ┌──────────┐
│Operation │ │CalendarE │ │Task  │ │   Note   │
│Repository│ │vtRepos.  │ │Repos.│ │Repository│
└────┬─────┘ └────┬─────┘ └───┬──┘ └────┬─────┘
     │            │            │         │
     └────────────┴────────────┴─────────┘
                  │
                  ▼
         ┌─────────────────┐
         │ HibernateUtil   │
         │  (JPA/Hibernate)│
         └────────┬─────────┘
                  │
                  ▼
         ┌─────────────────┐
         │   H2 Database   │
         └─────────────────┘
```

---

## Technology Stack

### Core Technologies
- **Java 17+**: Modern Java with records, pattern matching, and text blocks
- **JavaFX 21+**: Rich desktop UI framework
- **Hibernate 6.x**: JPA implementation for ORM
- **H2 Database**: Embedded SQL database
- **Gradle 8.x**: Build automation tool

### Key Libraries
- **Jakarta Persistence API**: ORM annotations
- **JavaFX Controls**: UI components
- **JavaFX Animation**: Smooth transitions and effects

---

## Package Structure

```
src/main/java/com/roam/
├── RoamApplication.java          # Main application entry point
├── MainLayout.java               # Root layout with navigation
│
├── model/                        # Domain models (JPA entities)
│   ├── Operation.java
│   ├── OperationStatus.java
│   ├── Priority.java
│   ├── Task.java
│   ├── TaskStatus.java
│   ├── TaskFilter.java
│   ├── Note.java
│   ├── NoteTemplate.java
│   ├── Tag.java
│   ├── CalendarEvent.java
│   ├── CalendarSource.java
│   └── CalendarSourceType.java
│
├── controller/                   # Business logic controllers
│   ├── OperationsController.java
│   ├── OperationDetailController.java
│   ├── TasksController.java
│   ├── CalendarController.java
│   └── WikiController.java
│
├── repository/                   # Data access layer
│   ├── OperationRepository.java
│   ├── TaskRepository.java
│   ├── NoteRepository.java
│   ├── NoteTemplateRepository.java
│   ├── TagRepository.java
│   ├── CalendarEventRepository.java
│   └── CalendarSourceRepository.java
│
├── view/                         # Main view components
│   ├── OperationsView.java
│   ├── OperationDetailView.java
│   ├── TasksView.java
│   ├── CalendarView.java
│   ├── WikiView.java
│   └── components/               # Reusable UI components
│       ├── OperationTableView.java
│       ├── OperationDialog.java
│       ├── OperationInfoCard.java
│       ├── TaskDialog.java
│       ├── TaskCard.java
│       ├── TasksListView.java
│       ├── TasksTimelineView.java
│       ├── TasksFilterPanel.java
│       ├── TasksStatsBar.java
│       ├── TasksToolbar.java
│       ├── KanbanBoard.java
│       ├── GlobalTasksKanban.java
│       ├── EventDialog.java
│       ├── WikiSidebar.java
│       ├── WikiToolbar.java
│       ├── WikiNoteEditor.java
│       ├── NotesEditor.java
│       ├── TemplateManagerDialog.java
│       ├── PreferencesDialog.java
│       ├── StatisticsDialog.java
│       ├── CreateTagDialog.java
│       ├── BatchOperationsBar.java
│       ├── Breadcrumb.java
│       └── cells/                # Custom table cells
│
├── service/                      # Application services
│   ├── DatabaseService.java
│   └── DataInitializer.java
│
└── util/                         # Utility classes
    ├── HibernateUtil.java
    ├── DialogUtils.java
    ├── MarkdownUtils.java
    ├── ExportUtils.java
    └── ImportUtils.java

src/main/resources/
├── styles/
│   └── application.css           # Global CSS styling
├── fonts/
│   ├── Poppins-Regular.ttf
│   ├── Poppins-Medium.ttf
│   ├── Poppins-SemiBold.ttf
│   └── Poppins-Bold.ttf
└── icons/
    └── roam-icon.png
```

---

## Core Models

### 1. Operation
**Purpose**: Represents a major operational project or initiative

**Entity**: `@Entity` JPA entity with H2 persistence

**Key Fields**:
- `id` (Long): Primary key
- `name` (String): Operation name
- `purpose` (String): Detailed description/purpose
- `status` (OperationStatus): ONGOING, IN_PROGRESS, or END
- `priority` (Priority): HIGH, MEDIUM, or LOW
- `dueDate` (LocalDate): Target completion date
- `outcome` (String): Results/achievements
- `createdAt` (LocalDateTime): Creation timestamp
- `updatedAt` (LocalDateTime): Last modification timestamp

**Relationships**:
- One-to-Many with `Task` (operation can have multiple tasks)
- One-to-Many with `Note` (operation can have multiple notes)

---

### 2. Task
**Purpose**: Actionable work items within operations

**Entity**: `@Entity` JPA entity

**Key Fields**:
- `id` (Long): Primary key
- `title` (String): Task name
- `description` (String): Detailed description
- `status` (TaskStatus): TODO, IN_PROGRESS, or DONE
- `priority` (Priority): HIGH, MEDIUM, or LOW
- `dueDate` (LocalDate): Deadline
- `operationId` (Long): Foreign key to Operation
- `createdAt` (LocalDateTime): Creation timestamp
- `completedAt` (LocalDateTime): Completion timestamp

**Features**:
- Drag-and-drop status changes
- Kanban board visualization
- Timeline view support
- Filtering by status, priority, date range

---

### 3. Note
**Purpose**: Wiki-style knowledge management entries

**Entity**: `@Entity` JPA entity

**Key Fields**:
- `id` (Long): Primary key
- `title` (String): Note title
- `content` (String, @Lob): Markdown content
- `operationId` (Long): Optional link to operation
- `tags` (Set<Tag>): Many-to-Many relationship
- `wordCount` (Integer): Calculated word count
- `createdAt` (LocalDateTime): Creation timestamp
- `updatedAt` (LocalDateTime): Last edit timestamp

**Features**:
- Markdown formatting support
- Tag-based organization
- Template system
- Search functionality
- Import/Export capabilities

---

### 4. CalendarEvent
**Purpose**: Scheduled events and appointments

**Entity**: `@Entity` JPA entity

**Key Fields**:
- `id` (Long): Primary key
- `title` (String): Event title
- `description` (String): Event details
- `startTime` (LocalDateTime): Start date/time
- `endTime` (LocalDateTime): End date/time
- `sourceId` (Long): Foreign key to CalendarSource
- `allDay` (Boolean): Full-day event flag
- `recurring` (Boolean): Recurrence flag

**Features**:
- Multiple calendar sources (Personal, Work, Operations)
- Color-coded by source
- All-day event support
- Recurring event support (future enhancement)

---

### 5. Supporting Models

#### OperationStatus (Enum)
- `ONGOING`: Active/in progress
- `IN_PROGRESS`: Currently being worked on
- `END`: Completed

#### Priority (Enum)
- `HIGH`: Critical priority (Red badge)
- `MEDIUM`: Normal priority (Orange badge)
- `LOW`: Low priority (Green badge)

#### TaskStatus (Enum)
- `TODO`: Not started
- `IN_PROGRESS`: Currently working
- `DONE`: Completed

#### CalendarSourceType (Enum)
- `PERSONAL`: Personal events
- `WORK`: Work-related events
- `OPERATIONS`: Auto-synced from operations

---

## Controllers

### 1. OperationsController
**Responsibility**: Manage operations CRUD and table interactions

**Key Methods**:
```java
public List<Operation> loadOperations()
public void createOperation()
public void editOperation(Operation operation)
public void deleteOperation(Operation operation)
public void refreshTable()
```

**Features**:
- Create/Edit/Delete operations via dialogs
- Refresh table view with latest data
- Error handling with user-friendly dialogs

---

### 2. OperationDetailController
**Responsibility**: Manage individual operation details, tasks, and notes

**Key Methods**:
```java
public void updateOperation(Operation updatedOperation)
public List<Task> loadTasks()
public void createTask(TaskStatus initialStatus)
public void editTask(Task task)
public void deleteTask(Task task)
public void updateTaskStatus(Task task, TaskStatus newStatus)
public List<Note> loadNotes()
public Note createNote()
public void saveNote(Note note)
public void deleteNote(Note note)
```

**Features**:
- Manage tasks within operation context
- Kanban board interaction
- Notes management
- Real-time updates via callbacks

---

### 3. TasksController
**Responsibility**: Global task management and filtering

**Key Methods**:
```java
public List<Task> loadAllTasks()
public List<Task> loadFilteredTasks(TaskFilter filter)
public void createTask()
public void editTask(Task task)
public void deleteTask(Task task)
public void updateTaskStatus(Task task, TaskStatus newStatus)
public Map<TaskStatus, Long> getTaskStatistics()
```

**Features**:
- Global task view across all operations
- Advanced filtering (status, priority, date range, operation)
- Task statistics dashboard
- Kanban and timeline views

---

### 4. CalendarController
**Responsibility**: Calendar event and source management

**Key Methods**:
```java
public List<CalendarSource> getCalendarSources()
public List<CalendarEvent> getEventsForDate(LocalDate date)
public void createEvent(LocalDate date)
public void editEvent(CalendarEvent event)
public void deleteEvent(CalendarEvent event)
public void toggleSourceVisibility(CalendarSource source)
```

**Features**:
- Multi-source calendar management
- Day/Week/Month view support
- Event CRUD operations
- Color-coded by source
- Default calendar creation

---

### 5. WikiController
**Responsibility**: Wiki note management and search

**Key Methods**:
```java
public List<Note> loadAllNotes()
public List<Note> searchNotes(String query)
public List<Note> filterByTag(Tag tag)
public void createNote()
public void saveNote(Note note)
public void deleteNote(Note note)
public List<Tag> loadAllTags()
public void createTag(String name, String color)
```

**Features**:
- Full-text search
- Tag-based filtering
- Template management
- Export/Import markdown
- Statistics dashboard

---

## Repositories

All repositories follow consistent patterns using Hibernate EntityManager.

### Common Operations
```java
public T save(T entity)                    // Create or update
public Optional<T> findById(Long id)       // Find by ID
public List<T> findAll()                   // Get all entities
public void delete(T entity)               // Delete entity
public void delete(Long id)                // Delete by ID
public long count()                        // Count entities
```

### Repository List

1. **OperationRepository**
   - `findByStatus(OperationStatus status)`
   - `findByPriority(Priority priority)`

2. **TaskRepository**
   - `findByOperationId(Long operationId)`
   - `findByStatus(TaskStatus status)`
   - `findByPriority(Priority priority)`
   - `findByDueDateBetween(LocalDate start, LocalDate end)`
   - `findWithFilters(TaskFilter filter)`

3. **NoteRepository**
   - `findByOperationId(Long operationId)`
   - `searchByTitleOrContent(String query)`
   - `findByTag(Tag tag)`
   - `findByTags(Set<Tag> tags)`

4. **NoteTemplateRepository**
   - Template CRUD for wiki notes

5. **TagRepository**
   - Tag management for notes
   - `findByName(String name)`

6. **CalendarEventRepository**
   - `findBySourceId(Long sourceId)`
   - `findByDateRange(LocalDate start, LocalDate end)`

7. **CalendarSourceRepository**
   - `findByType(CalendarSourceType type)`
   - `findByIsDefault(Boolean isDefault)`

---

## Views

### 1. OperationsView
**Purpose**: Main operations listing and management

**Layout Structure**:
```
┌─────────────────────────────────────────────────┐
│  Operations                   [+ New Operation] │
├─────────────────────────────────────────────────┤
│  Name     │ Status    │ Priority │ Due Date │ ⋮ │
│  Project1 │ [Ongoing] │ [High]   │ Nov 30   │ ⋮ │
│  Project2 │ [End]     │ [Low]    │ Dec 15   │ ⋮ │
└─────────────────────────────────────────────────┘
```

**Components**:
- Header with title and "New Operation" button
- `OperationTableView` with columns:
  - Name
  - Status (color-coded badge)
  - Priority (color-coded badge)
  - Due Date
  - Actions (kebab menu with Edit/Delete)
- Empty state when no operations exist

**Interactions**:
- Click row → Navigate to OperationDetailView
- Double-click → Edit operation
- Kebab menu → Edit/Delete

---

### 2. OperationDetailView
**Purpose**: Detailed view of single operation with tasks and notes

**Layout Structure**:
```
┌─────────────────────────────────────────────────┐
│  ← Back to Operations                           │
├─────────────────────────────────────────────────┤
│  ┌───────────────────────────────────────────┐  │
│  │ Operation Info Card                       │  │
│  │ Name: Project Alpha                       │  │
│  │ Status: [Ongoing]  Priority: [High]      │  │
│  │ Due Date: Nov 30, 2025                   │  │
│  │ Purpose: Strategic initiative...          │  │
│  │                              [Edit Info]  │  │
│  └───────────────────────────────────────────┘  │
├─────────────────────────────────────────────────┤
│  📋 Tasks              📝 Notes                 │
│  ┌──────────┬──────────┬──────────┐            │
│  │ TODO     │ IN PROG  │ DONE     │            │
│  │ [Task 1] │ [Task 2] │ [Task 3] │            │
│  │ [+ New]  │ [+ New]  │ [+ New]  │            │
│  └──────────┴──────────┴──────────┘            │
└─────────────────────────────────────────────────┘
```

**Components**:
- Breadcrumb navigation back to operations
- `OperationInfoCard`: Displays operation details
- Tabbed interface:
  - **Tasks Tab**: `KanbanBoard` with drag-and-drop
  - **Notes Tab**: `NotesEditor` with markdown support

**Features**:
- Edit operation info
- Create/Edit/Delete tasks
- Drag tasks between status columns
- Create/Edit/Delete notes
- Real-time updates

---

### 3. TasksView
**Purpose**: Global task management across all operations

**Layout Structure**:
```
┌─────────────────────────────────────────────────┐
│  Tasks                                          │
│  ┌──────────────────────────────────────────┐  │
│  │ Filter Panel                             │  │
│  │ Status: [All ▾]  Priority: [All ▾]      │  │
│  │ Operation: [All ▾]  Date: [Any ▾]       │  │
│  └──────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────┐  │
│  │ Stats: 15 Total │ 5 Todo │ 7 Prog │ 3 ✓  │  │
│  └──────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────┐  │
│  │ [Kanban] [Timeline] [List]               │  │
│  └──────────────────────────────────────────┘  │
│  ┌───────────┬───────────┬───────────┐         │
│  │   TODO    │ IN PROGRESS│   DONE    │         │
│  │ [Task A]  │ [Task B]   │ [Task C]  │         │
│  │ [Task D]  │            │           │         │
│  └───────────┴───────────┴───────────┘         │
└─────────────────────────────────────────────────┘
```

**Components**:
- `TasksToolbar`: View switcher and actions
- `TasksFilterPanel`: Multi-criteria filtering
- `TasksStatsBar`: Real-time statistics
- `GlobalTasksKanban`: Kanban view with all tasks
- `TasksTimelineView`: Timeline view by due date
- `TasksListView`: Traditional list view

**Features**:
- Switch between Kanban/Timeline/List views
- Filter by status, priority, operation, date range
- Drag-and-drop status changes
- Task statistics dashboard
- Create tasks from any view

---

### 4. CalendarView
**Purpose**: Calendar-based event scheduling

**Layout Structure**:
```
┌─────────────────────────────────────────────────┐
│  November 2025              [Day|Week|Month]    │
├────┬────┬────┬────┬────┬────┬────┬────────────┤
│Mon │Tue │Wed │Thu │Fri │Sat │Sun │ Calendars │
├────┼────┼────┼────┼────┼────┼────┤           │
│ 1  │ 2  │ 3  │ 4  │ 5  │ 6  │ 7  │ ☑ Personal│
│    │●Evt│    │    │    │    │    │ ☑ Work    │
├────┼────┼────┼────┼────┼────┼────┤ ☑ Ops     │
│ 8  │ 9  │ 10 │ 11 │ 12 │ 13 │ 14 │           │
│    │    │●Mtg│    │    │    │    │           │
└────┴────┴────┴────┴────┴────┴────┴───────────┘
```

**Components**:
- Month/Week/Day view selector
- Calendar grid with events
- Filter panel for calendar sources
- Event markers color-coded by source

**Features**:
- Multiple calendar sources
- Click date → Create event
- Click event → View/Edit details
- Toggle calendar source visibility
- Color-coded events
- All-day event support

---

### 5. WikiView
**Purpose**: Knowledge management with markdown notes

**Layout Structure**:
```
┌──────────┬──────────────────────────────────────┐
│ 📝 Notes │  [Search...]           [+ New Note]  │
│          ├──────────────────────────────────────┤
│ ┌─────┐  │  # Meeting Notes                     │
│ │Note1│  │                                      │
│ │Note2│  │  ## Key Points                       │
│ │Note3│  │  - Point 1                           │
│ └─────┘  │  - Point 2                           │
│          │                                      │
│ 🏷️ Tags  │                                      │
│ #work    │                          [Save] [⋮]  │
│ #project │                                      │
└──────────┴──────────────────────────────────────┘
```

**Components**:
- `WikiSidebar`: Note list and tag filter
- `WikiToolbar`: Search, actions menu
- `WikiNoteEditor`: Markdown editor with preview
- Tag management panel

**Features**:
- Markdown formatting
- Real-time search
- Tag-based organization
- Note templates
- Export/Import markdown files
- Statistics (total notes, word count)
- Auto-save functionality

---

## UI Components

### Dialog Components

1. **OperationDialog**
   - Create/Edit operation form
   - Fields: Name, Purpose, Status, Priority, Due Date

2. **TaskDialog**
   - Create/Edit task form
   - Fields: Title, Description, Status, Priority, Due Date

3. **EventDialog**
   - Create/Edit calendar event
   - Fields: Title, Description, Start/End time, Calendar source, All-day flag

4. **PreferencesDialog**
   - Wiki settings: Font family, font size, auto-save interval
   - Display settings: Show word count, confirm delete

5. **StatisticsDialog**
   - Wiki statistics: Total notes, total words, tags used
   - Charts and metrics

6. **TemplateManagerDialog**
   - Manage note templates
   - Create, edit, delete templates

7. **CreateTagDialog**
   - Create new tags with name and color

### Card Components

1. **OperationInfoCard**
   - Display operation details in detail view
   - Inline editing capabilities

2. **TaskCard**
   - Draggable task card for Kanban boards
   - Shows title, priority, due date
   - Quick actions (edit/delete)

### Table Components

1. **OperationTableView**
   - Custom table for operations listing
   - Color-coded status/priority badges
   - Kebab menu actions
   - WCAG-compliant colors

### Board Components

1. **KanbanBoard**
   - Three-column board (TODO, IN_PROGRESS, DONE)
   - Drag-and-drop task cards
   - Column-specific "Add Task" buttons

2. **GlobalTasksKanban**
   - Global version showing all tasks
   - Includes operation context

### Filter & Toolbar Components

1. **TasksFilterPanel**
   - Multi-select filtering
   - Status, Priority, Operation, Date range filters

2. **TasksToolbar**
   - View switcher (Kanban/Timeline/List)
   - Quick actions

3. **WikiToolbar**
   - Search bar
   - Menu actions (Export, Import, Statistics, Preferences)

4. **TasksStatsBar**
   - Real-time task statistics
   - Visual progress indicators

### Editor Components

1. **WikiNoteEditor**
   - Markdown text area
   - Formatting toolbar (future enhancement)
   - Auto-save indicator

2. **NotesEditor**
   - Embedded notes editor for operation detail view
   - Simplified interface

### Navigation Components

1. **Breadcrumb**
   - Navigation trail
   - Back button functionality

2. **BatchOperationsBar**
   - Bulk action toolbar
   - Select multiple items

---

## Utilities & Services

### 1. HibernateUtil
**Purpose**: Hibernate configuration and EntityManager factory

**Key Methods**:
```java
public static EntityManager getEntityManager()
public static void shutdown()
```

**Configuration**:
- Database: H2 (file-based at `./data/roam.db`)
- Dialect: H2Dialect
- DDL: `update` (auto-create tables)
- Show SQL: true (for debugging)

---

### 2. DialogUtils
**Purpose**: Standardized dialog creation

**Key Methods**:
```java
public static boolean showConfirmation(String title, String header, String content)
public static void showError(String title, String header, String content)
public static void showSuccess(String title, String header, String content)
public static void showInfo(String title, String header, String content)
```

---

### 3. MarkdownUtils
**Purpose**: Markdown processing and rendering

**Key Methods**:
```java
public static String toHtml(String markdown)
public static int countWords(String text)
public static String preview(String markdown, int maxLength)
```

---

### 4. ExportUtils
**Purpose**: Export notes to markdown files

**Key Methods**:
```java
public static void exportAllNotesToMarkdown(List<Note> notes)
public static void exportNoteToMarkdown(Note note, File file)
```

**Features**:
- Export single note or all notes
- YAML front matter with metadata
- Directory chooser dialog

---

### 5. ImportUtils
**Purpose**: Import markdown files as notes

**Key Methods**:
```java
public static List<Note> importNotesFromMarkdown(File directory)
public static Note parseMarkdownFile(File file)
```

**Features**:
- Parse YAML front matter
- Extract title and content
- Calculate word count

---

### 6. DatabaseService
**Purpose**: Database initialization

**Key Methods**:
```java
public static void initializeDatabase()
public static void testConnection()
```

**Features**:
- Initialize Hibernate
- Verify database connection
- Error handling

---

### 7. DataInitializer
**Purpose**: Seed database with sample data (development)

**Key Methods**:
```java
public static void initializeSampleData()
```

**Features**:
- Create sample operations, tasks, notes
- Useful for testing and demos

---

## Design System

### Color Palette

#### Primary Colors
- **Roam Blue**: `#4285f4` - Primary actions, active states
- **Background**: `#FAFAFA` - Main background
- **White**: `#FFFFFF` - Cards, panels
- **Border**: `#E0E0E0` - Dividers, borders

#### Text Colors
- **Primary Text**: `#212121` - Main content
- **Secondary Text**: `#616161` - Labels, headers
- **Disabled Text**: `#9E9E9E` - Placeholders

#### Status Colors (WCAG AA Compliant)
- **High Priority**: `#D32F2F` (Red) - Background: `#FFEBEE`
- **Medium Priority**: `#F57C00` (Orange) - Background: `#FFF3E0`
- **Low Priority**: `#388E3C` (Green) - Background: `#E8F5E9`

#### Status Badges
- **Ongoing**: `#1976D2` - Background: `#E3F2FD`
- **In Progress**: `#F57C00` - Background: `#FFF3E0`
- **End**: `#388E3C` - Background: `#E8F5E9`

### Typography

**Font Family**: Poppins
- **Regular**: Body text, buttons
- **Medium**: Subheadings
- **SemiBold**: Section headers
- **Bold**: Main headings, active nav

**Font Sizes**:
- **Headings**: 24px (Bold)
- **Subheadings**: 18px (SemiBold)
- **Body**: 14px (Regular)
- **Small**: 12px (Regular)

### Material Design Elevation

**Shadow System** (4 levels):
```css
--roam-shadow-1: dropshadow(gaussian, rgba(0,0,0,0.08), 6, 0, 0, 2);
--roam-shadow-2: dropshadow(gaussian, rgba(0,0,0,0.12), 12, 0, 0, 4);
--roam-shadow-3: dropshadow(gaussian, rgba(0,0,0,0.16), 18, 0, 0, 6);
--roam-shadow-4: dropshadow(gaussian, rgba(0,0,0,0.18), 25, 0, 0, 8);
```

**Usage**:
- **Level 1**: Table rows, small cards
- **Level 2**: Buttons, badges, sidebar
- **Level 3**: Cards, panels
- **Level 4**: Dialogs, modals

### Border Radius
- **Small**: 4px - Badges, tags
- **Medium**: 8px - Buttons, inputs
- **Large**: 12px - Cards, panels

### Spacing
- **Padding**: 16px (standard), 20px (large containers)
- **Margins**: 8px (compact), 12px (normal), 20px (sections)
- **Gap**: 10px (button groups), 15px (lists)

### Interactive States

**Buttons**:
- **Hover**: Background darkens, shadow increases
- **Pressed**: Scale 0.98, shadow reduces
- **Active**: Bold text, colored background

**Navigation**:
- **Default**: Gray text, no background
- **Hover**: Light gray background
- **Active**: Blue background, white text, bold font, shadow

**Table Rows**:
- **Hover**: Light gray background `#F5F5F5`
- **Selected**: Blue tint `#E3F2FD`

---

## Features

### Feature 1: Operations Management

**User Stories**:
- As a user, I can create operations to track major projects
- As a user, I can set status, priority, and due dates
- As a user, I can view all operations in a table
- As a user, I can edit or delete operations
- As a user, I can drill down into operation details

**Technical Implementation**:
- `Operation` model with JPA annotations
- `OperationRepository` for persistence
- `OperationsController` for business logic
- `OperationsView` with table display
- `OperationDialog` for create/edit forms

---

### Feature 2: Task Management

**User Stories**:
- As a user, I can create tasks within operations
- As a user, I can organize tasks in Kanban boards
- As a user, I can drag-and-drop tasks between columns
- As a user, I can filter tasks globally
- As a user, I can view task timeline and statistics

**Technical Implementation**:
- `Task` model with operation association
- `TaskRepository` with advanced filtering
- `TasksController` for global task management
- `KanbanBoard` with drag-and-drop
- `TasksFilterPanel` for multi-criteria filtering
- `TasksTimelineView` for chronological view

---

### Feature 3: Calendar Events

**User Stories**:
- As a user, I can schedule events on a calendar
- As a user, I can use multiple calendar sources
- As a user, I can view events in day/week/month views
- As a user, I can toggle calendar visibility
- As a user, I can color-code events by source

**Technical Implementation**:
- `CalendarEvent` and `CalendarSource` models
- `CalendarEventRepository` and `CalendarSourceRepository`
- `CalendarController` with multi-source support
- `CalendarView` with grid layout
- `EventDialog` for event creation

---

### Feature 4: Wiki Notes

**User Stories**:
- As a user, I can create markdown notes
- As a user, I can search notes by title/content
- As a user, I can organize notes with tags
- As a user, I can use templates for common note types
- As a user, I can export/import markdown files

**Technical Implementation**:
- `Note`, `Tag`, `NoteTemplate` models
- `NoteRepository` with full-text search
- `WikiController` for note management
- `WikiNoteEditor` with markdown support
- `ExportUtils` and `ImportUtils` for file operations
- `MarkdownUtils` for rendering

---

### Feature 5: Navigation & Layout

**User Stories**:
- As a user, I can navigate between main sections via sidebar
- As a user, I can collapse sidebar for more space
- As a user, I can see smooth transitions between views
- As a user, I can see clear visual hierarchy

**Technical Implementation**:
- `MainLayout` with sidebar and content area
- Navigation buttons with icons (📋📅✓📝)
- `FadeTransition` for view switching (300ms)
- Collapsible sidebar with animation
- Active state highlighting

---

### Feature 6: Data Persistence

**User Stories**:
- As a user, my data is automatically saved
- As a user, my data persists between sessions
- As a user, I have a local database
- As a user, I can rely on data integrity

**Technical Implementation**:
- Hibernate ORM with JPA
- H2 embedded database (file-based)
- Repository pattern for data access
- Transaction management
- Entity relationships (One-to-Many, Many-to-Many)

---

## Database Schema

### Tables

**operations**
- id (BIGINT, PK)
- name (VARCHAR)
- purpose (TEXT)
- status (VARCHAR)
- priority (VARCHAR)
- due_date (DATE)
- outcome (TEXT)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)

**tasks**
- id (BIGINT, PK)
- title (VARCHAR)
- description (TEXT)
- status (VARCHAR)
- priority (VARCHAR)
- due_date (DATE)
- operation_id (BIGINT, FK)
- created_at (TIMESTAMP)
- completed_at (TIMESTAMP)

**notes**
- id (BIGINT, PK)
- title (VARCHAR)
- content (TEXT)
- operation_id (BIGINT, FK, nullable)
- word_count (INTEGER)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)

**tags**
- id (BIGINT, PK)
- name (VARCHAR)
- color (VARCHAR)

**note_tags** (Join Table)
- note_id (BIGINT, FK)
- tag_id (BIGINT, FK)

**note_templates**
- id (BIGINT, PK)
- name (VARCHAR)
- content (TEXT)
- created_at (TIMESTAMP)

**calendar_sources**
- id (BIGINT, PK)
- name (VARCHAR)
- color (VARCHAR)
- type (VARCHAR)
- is_default (BOOLEAN)
- is_visible (BOOLEAN)

**calendar_events**
- id (BIGINT, PK)
- title (VARCHAR)
- description (TEXT)
- start_time (TIMESTAMP)
- end_time (TIMESTAMP)
- source_id (BIGINT, FK)
- all_day (BOOLEAN)
- recurring (BOOLEAN)

---

## Build Configuration

### Gradle Dependencies

```gradle
dependencies {
    // JavaFX
    implementation 'org.openjfx:javafx-controls:21.0.1'
    implementation 'org.openjfx:javafx-fxml:21.0.1'
    
    // Hibernate & JPA
    implementation 'org.hibernate:hibernate-core:6.4.1.Final'
    implementation 'jakarta.persistence:jakarta.persistence-api:3.1.0'
    
    // H2 Database
    implementation 'com.h2database:h2:2.2.224'
    
    // Logging
    implementation 'org.slf4j:slf4j-simple:2.0.9'
}
```

---

## Application Flow

### Startup Sequence

1. **RoamApplication.start()**
   - Initialize database via `DatabaseService.initializeDatabase()`
   - Load custom fonts (Poppins family)
   - Create `MainLayout` instance
   - Load CSS stylesheet
   - Show primary stage

2. **DatabaseService.initializeDatabase()**
   - Configure Hibernate
   - Create EntityManagerFactory
   - Test database connection
   - Auto-create tables (DDL update)

3. **MainLayout Constructor**
   - Initialize sidebar with navigation
   - Create content area
   - Set default view (Operations)
   - Wire up navigation handlers

4. **View Initialization**
   - Each view creates its controller
   - Controller initializes repository
   - Load initial data
   - Set up event handlers
   - Render UI components

### User Interaction Flow

**Example: Creating an Operation**

1. User clicks "**+ New Operation**" in OperationsView
2. `OperationsController.createOperation()` called
3. Opens `OperationDialog` with empty form
4. User fills form and clicks Save
5. Dialog returns `Operation` object
6. Controller calls `OperationRepository.save(operation)`
7. Repository uses Hibernate to persist to database
8. Controller calls `refreshTable()` to update view
9. View reloads data and displays new operation

**Example: Drag-and-Drop Task Status Change**

1. User drags `TaskCard` from TODO to IN_PROGRESS column
2. Drag event captured in `KanbanBoard`
3. Extract task and target status from event
4. Call `controller.updateTaskStatus(task, TaskStatus.IN_PROGRESS)`
5. Controller updates task in database
6. Fire `onDataChanged` callback
7. View refreshes Kanban board with updated task

---

## Future Enhancements

### Planned Features

1. **Advanced Calendar**
   - Recurring events implementation
   - Calendar sync (Google Calendar, Outlook)
   - Event reminders/notifications

2. **Enhanced Wiki**
   - Markdown preview pane
   - Rich text formatting toolbar
   - Note linking (backlinks)
   - Note attachments (files, images)

3. **Reporting & Analytics**
   - Operation progress charts
   - Task burndown charts
   - Time tracking integration
   - Export reports to PDF

4. **Collaboration**
   - Multi-user support
   - Shared operations/tasks
   - Comments and mentions
   - Activity feed

5. **Mobile Companion**
   - iOS/Android app
   - Cloud sync
   - Push notifications

6. **Integrations**
   - Jira/Trello import
   - Slack notifications
   - Email integration
   - API for third-party tools

7. **Customization**
   - Custom themes
   - User preferences
   - Custom fields
   - Workflow automation

---

## Development Guidelines

### Code Style

- **Package naming**: Lowercase, descriptive (`com.roam.controller`)
- **Class naming**: PascalCase, descriptive (`OperationDetailController`)
- **Method naming**: camelCase, verb-based (`loadOperations()`)
- **Constants**: UPPER_SNAKE_CASE (`SIDEBAR_WIDTH_EXPANDED`)

### Best Practices

1. **Separation of Concerns**: Keep views, controllers, and repositories separate
2. **Error Handling**: Use try-catch with user-friendly dialogs
3. **Null Safety**: Check for null before operations
4. **Resource Cleanup**: Close EntityManager in finally blocks
5. **Transaction Management**: Wrap DB writes in transactions
6. **Callbacks**: Use `Runnable` callbacks for view updates
7. **Logging**: Use System.out for info, System.err for errors

### Testing Strategy

1. **Unit Tests**: Test controllers and repositories in isolation
2. **Integration Tests**: Test database operations end-to-end
3. **UI Tests**: Manual testing of view interactions
4. **Data Validation**: Test edge cases (null, empty, invalid data)

---

## Conclusion

Roam is a comprehensive desktop application that combines project management, task tracking, scheduling, and knowledge management into a unified platform. Built with modern JavaFX and Hibernate, it provides a robust foundation for productivity and organization.

The modular MVC architecture ensures maintainability and extensibility, while the material design system delivers a polished, professional user experience. With persistent local storage and WCAG-compliant accessibility, Roam is ready for production use.

---

**Version**: 1.0  
**Last Updated**: November 21, 2025  
**Author**: Roam Development Team  
**License**: MIT
