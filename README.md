<div align="center">
   <img src="src/main/resources/icons/roam-icon.png" alt="Roam Logo" width="128" height="128">
  <h1> Roam </h1>
  <h4> Personal Productivity & Project Management </h4>
  
  [![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
  [![JavaFX](https://img.shields.io/badge/JavaFX-21-blue.svg)](https://openjfx.io/)
  [![Hibernate](https://img.shields.io/badge/Hibernate-6.4.1-green.svg)](https://hibernate.org/)
  [![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
</div>

## Overview

**Roam** is a modern, feature-rich desktop application for personal productivity and project management. Built with JavaFX and designed with a clean, intuitive interface, Roam helps you organize your operations (projects), tasks, calendar events, wikis (notes), and daily journals in one unified workspace.

## Features

### Operations (Projects)
- Create and manage operations with name, purpose, and expected outcome
- Set priority levels (Critical, High, Medium, Low)
- Track status (Planning, In Progress, On Hold, Completed, Cancelled)
- Assign regional categorization
- View operation details with associated tasks, wikis, and calendar events

### Task Management
- Kanban-style task board with drag-and-drop support
- Multiple task views: List, Kanban, or Operation-grouped
- Task filtering by status, priority, operation, and assignee
- Quick task creation with keyboard shortcuts
- Task statistics dashboard (total, completed, in-progress, overdue)
- Recurring tasks support

### Calendar
- Interactive calendar with month/week/day views
- Create and manage calendar events
- Event linking to operations, tasks, and wikis
- Calendar source management with color coding
- All-day and timed events
- Recurring events support

### Wiki (Notes)
- Rich text wiki pages with Markdown-style formatting
- Wiki templates for quick page creation
- Link wikis to operations, tasks, or calendar events
- Favorites and quick access
- Word count tracking

### Journal
- Daily journal entries with rich text editing
- Quick date navigation
- Journal archiving and search

### Search
- Global search across all content types
- Full-text search powered by Apache Lucene
- Filter by type (Operations, Tasks, Wikis, Journal, Events)

### Security
- PIN-based authentication with secure BCrypt hashing
- Rate limiting to prevent brute force attacks
- Secure local data storage

### Theming
- Light and Dark mode support
- Consistent Material Design-inspired UI
- Custom Poppins font family

## Tech Stack

| Component | Technology |
|-----------|------------|
| **Language** | Java 21 |
| **UI Framework** | JavaFX 21 |
| **Theming** | AtlantaFX 2.0.1 |
| **Database** | H2 with Hibernate ORM 6.4.1 |
| **Migrations** | Flyway |
| **Search** | Apache Lucene |
| **Icons** | Ikonli Feather |
| **Fonts** | Poppins |
| **Build** | Gradle 8.14 |

## Installation

### Prerequisites
- Java 21 or higher
- Gradle 8.x (included via wrapper)

### Build from Source

1. **Clone the repository:**
   `ash
   git clone https://github.com/your-username/roam.git
   cd roam
   `

2. **Build the application:**
   `ash
   ./gradlew build
   `

3. **Run the application:**
   `ash
   ./gradlew run
   `

### Create Distribution

`ash
./gradlew distZip
`

The distribution will be created in uild/distributions/.

## Usage

### First Launch
1. On first launch, you'll be prompted to create a PIN for security
2. Enter an 8+ character PIN and confirm it
3. You'll be taken to the main dashboard

### Navigation
Use the sidebar to navigate between modules:
- **Operations** - View and manage your projects
- **Tasks** - Manage tasks across all operations
- **Calendar** - View and create events
- **Wiki** - Create and organize notes
- **Journal** - Write daily entries
- **Settings** - Configure application preferences

### Keyboard Shortcuts
| Shortcut | Action |
|----------|--------|
| Ctrl+N | Create new item (context-sensitive) |
| Ctrl+F | Focus search |
| Ctrl+D | Toggle dark mode |
| Esc | Close dialog/Go back |

## Project Structure

`
src/main/java/com/roam/
 controller/          # MVC Controllers
 layout/              # UI Layout components
 model/               # JPA Entity models
 repository/          # Data access layer
 service/             # Business logic
 util/                # Utility classes
 validation/          # Input validation
 view/                # UI Views and components
 MainLayout.java      # Main application layout
 RoamApplication.java # Application entry point
`

## Database

Roam uses H2 Database for local data persistence. Database migrations are handled automatically by Flyway on application startup.

## Testing

Run the test suite:
`ash
./gradlew test
`

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- [AtlantaFX](https://github.com/mkpaz/atlantafx) for the JavaFX theme
- [Ikonli](https://kordamp.org/ikonli/) for the icon library
- [Poppins Font](https://fonts.google.com/specimen/Poppins) by the Indian Type Foundry

---

<div align="center">
  Made by [text](https://www.facebook.com/people/Noor/61582164040390/)
</div>
