# Roam

<div align="center">

<!-- Add your logo here -->
<img src="https://raw.githubusercontent.com/joy242-15-740/roam/refs/heads/main/src/main/resources/icons/roam-icon.png" alt="Roam Logo" width="120" height="120">

### Personal Knowledge Management, Redefined

**A desktop-first PKM system that seamlessly integrates note-taking, calendar, and task management into a single, powerful application.**

[![Java Version](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21-blue.svg)](https://openjfx.io/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Status](https://img.shields.io/badge/Status-In%20Development-yellow.svg)]()
[![PRs Welcome](https://img.shields.io/badge/PRs-Welcome-brightgreen.svg)]()

[Vision](#-vision) • [Features](#-planned-features) • [Tech Stack](#-tech-stack) • [Roadmap](#-roadmap) • [Contributing](#-contributing)

</div>

---

## 🚧 Project Status

> **⚠️ This project is currently in early development.**  
> We're building Roam from the ground up with modern Java technologies. The application is not yet functional, but we're actively working on it. Star this repository to follow our progress!


**Want to help?** Check out our [Contributing](#-contributing) section below!

---

## 📖 Vision

**Roam** will be an open-source, desktop-first Personal Knowledge Management (PKM) system that brings together the best features of most of the top tier application in this niche into a unified workflow. Built with modern Java technologies, Roam will provide a seamless experience for managing your projects, tasks, events, and knowledge base.

### Why We're Building Roam

- **🎯 Unified Workspace** - Everything in one place: notes, tasks, events, and projects
- **📝 Markdown-Powered** - Beautiful notes with live preview and syntax highlighting
- **📅 World-Class Calendar** - Multi-layered time views with drag-and-drop scheduling
- **🚀 Lightning Fast** - Desktop-native performance with embedded database
- **🎨 Beautiful Design** - Clean, minimal interface that stays out of your way
- **🔒 Privacy-First** - Your data stays on your machine, no cloud required
- **🆓 Open Source** - Free forever, community-driven development

---

## ✨ Planned Features

### 🗂️ Operations 

Organize your work into **Operations** - flexible project containers that adapt to your workflow:

- **Flexible Metadata**
  - Purpose and outcome tracking
  - Due dates and priority levels (High, Medium, Low)
  - Status indicators (Ongoing, In Progress, End)
- **Integrated Workspace**
  - Each Operation will have its own dedicated page
  - Create tasks, events, and notes within each Operation
  - Notion-like database view for all Operations

### 📝 Wiki (Note-Taking)

Powerful markdown-based note-taking with real-time preview:

- **Markdown Editor**
  - Source code and live preview modes
  - Syntax highlighting for code blocks
  - Obsidian-like editing experience
  - `.md` file format support
- **Rich Text Editing**
  - High-performance text area powered by RichTextFX
  - Support for tables, lists, and formatting
  - Quick search and navigation

### 📅 Calendar

A robust, multi-layered calendar system for comprehensive time management:

- **Multiple Views**
  - Month, Week, Day, and Agenda views
  - Seamless switching between perspectives
  - Smart event collision handling (Google Calendar-style)
- **Advanced Features**
  - Multiple color-coded calendars (Personal, Work, etc.)
  - Recurring events with iCalendar RRULE standards
  - Click-to-create and drag-to-reschedule
  - Visual event collision management
- **Smart Scheduling**
  - Visual time blocking
  - Event duration editing
  - Cross-Operation event visibility
  - Full-view mode for all events

### ✅ Task Management

Streamlined task tracking integrated with your Operations:

- Create tasks within specific Operations
- Full-view mode to see all tasks across Operations
- Priority and status management
- Quick task creation and completion
- Integration with calendar events

---

## 🛠️ Tech Stack

<div align="center">

| Category | Technology | Purpose |
|----------|-----------|---------|
| **Language** | Java 21 (LTS) | Core application language |
| **UI Framework** | JavaFX 21 | Modern desktop UI |
| **UI Design** | Scene Builder | FXML visual editor |
| **Build Tool** | Gradle 8.x | Build automation |
| **Database** | H2 (Embedded) | Single-file data storage |
| **ORM** | Hibernate Core + JPA | Object-relational mapping |
| **Calendar** | CalendarFX | Professional calendar UI |
| **Text Editor** | RichTextFX | High-performance editor |
| **Markdown** | Flexmark | Markdown parsing & rendering |
| **Distribution** | jpackage | Native installers |

</div>

### Dependencies (build.gradle)

```gradle
plugins {
    id 'application'
    id 'org.openjfx.javafxplugin' version '0.1.0'
    id 'org.beryx.jlink' version '3.0.1'
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

javafx {
    version = "21"
    modules = ['javafx.controls', 'javafx.fxml', 'javafx.web']
}

dependencies {
    // JavaFX Core
    implementation 'org.openjfx:javafx-controls:21'
    implementation 'org.openjfx:javafx-fxml:21'
    implementation 'org.openjfx:javafx-web:21'
    
    // Calendar Component
    implementation 'com.calendarfx:view:11.12.7'
    
    // Rich Text Editor
    implementation 'org.fxmisc.richtext:richtextfx:0.11.2'
    
    // Database & ORM
    implementation 'org.hibernate.orm:hibernate-core:6.2.7.Final'
    implementation 'com.h2database:h2:2.2.224'
    implementation 'jakarta.persistence:jakarta.persistence-api:3.1.0'
    
    // Markdown Processing
    implementation 'com.vladsch.flexmark:flexmark-all:0.64.8'
    
    // Logging
    implementation 'org.slf4j:slf4j-api:2.0.9'
    implementation 'ch.qos.logback:logback-classic:1.4.11'
    
    // Testing
    testImplementation 'org.junit.jupiter:junit-jupiter:5.10.0'
    testImplementation 'org.mockito:mockito-core:5.5.0'
}

application {
    mainModule = 'com.roam'
    mainClass = 'com.roam.RoamApplication'
}

test {
    useJUnitPlatform()
}
```

---

## 🎨 Design System

### Color Palette

```css
/* Primary Colors */
--color-background: #FFFFFF;      /* Primary background */
--color-primary: #4285f4;         /* Google Blue - Buttons, accents */
--color-primary-hover: #1a73e8;   /* Button hover state */
--color-primary-pressed: #1765cc; /* Button pressed state */

/* Text Colors */
--color-text-primary: #000000;    /* Main text */
--color-text-secondary: #5f6368;  /* Secondary text */
--color-text-tertiary: #80868b;   /* Tertiary text */

/* UI Colors */
--color-border: #dadce0;          /* Borders and dividers */
--color-surface: #f8f9fa;         /* Cards and surfaces */
--color-hover: #f1f3f4;           /* Hover states */

/* Status Colors */
--color-success: #34a853;         /* Success states */
--color-warning: #fbbc04;         /* Warning states */
--color-error: #ea4335;           /* Error states */

/* Priority Colors */
--priority-high: #ea4335;         /* High priority */
--priority-medium: #fbbc04;       /* Medium priority */
--priority-low: #34a853;          /* Low priority */
```

### Typography

**Font**: Poppins (bundled with application)

```css
/* Headings */
--font-h1: 600 32px/40px 'Poppins', sans-serif;
--font-h2: 600 24px/32px 'Poppins', sans-serif;
--font-h3: 600 20px/28px 'Poppins', sans-serif;
--font-h4: 600 16px/24px 'Poppins', sans-serif;

/* Body */
--font-body-large: 400 16px/24px 'Poppins', sans-serif;
--font-body: 400 14px/20px 'Poppins', sans-serif;
--font-body-small: 400 12px/16px 'Poppins', sans-serif;

/* Code */
--font-code: 400 14px/20px 'JetBrains Mono', monospace;
```

### UI Components (Scene Builder)

All UI components will be designed using **Scene Builder** with FXML and styled with custom CSS following Material Design principles:

- **Buttons**: Google Blue with white text, rounded corners (4px)
- **Input Fields**: Minimal borders, bottom underline on focus
- **Cards**: Subtle shadows, white background
- **Navigation**: Clean sidebar with icon + text
- **Modals**: Center-aligned with overlay backdrop

---

## 🚀 Getting Started (For Developers)

### Prerequisites

Before you begin, ensure you have the following installed:

- **Java Development Kit (JDK) 21** - [Download here](https://adoptium.net/)
- **Gradle 8.x** - Or use the included Gradle wrapper
- **IntelliJ IDEA** - [Download here](https://www.jetbrains.com/idea/)
- **Scene Builder** - [Download here](https://gluonhq.com/products/scene-builder/)
- **Git** - [Download here](https://git-scm.com/)

### Clone the Repository

```bash
git clone https://github.com/yourusername/roam.git
cd roam
```

### Open in IntelliJ IDEA

1. Open IntelliJ IDEA
2. Click **File → Open**
3. Navigate to the `roam` directory and select it
4. Wait for Gradle to sync and download dependencies

### Configure Scene Builder in IntelliJ

1. Go to **File → Settings → Languages & Frameworks → JavaFX**
2. Set the path to your Scene Builder executable
3. Now you can right-click any `.fxml` file and select "Open in Scene Builder"

### Project Setup Checklist

- [ ] Clone repository
- [ ] Open in IntelliJ IDEA
- [ ] Configure Scene Builder
- [ ] Verify JDK 21 is selected
- [ ] Run `./gradlew build` to verify setup
- [ ] Review project structure
- [ ] Read ARCHITECTURE.md (coming soon)

### Build the Project

```bash
# Clean and build
./gradlew clean build

# Run the application (when ready)
./gradlew run

# Run tests
./gradlew test
```

---

## 🤝 Contributing

**We're actively looking for contributors!** Whether you're a developer, designer, or just passionate about PKM tools, there's a place for you in this project.

### How You Can Help

#### 🔨 For Developers
- Implement features from the roadmap
- Write unit and integration tests
- Fix bugs and improve performance
- Review pull requests

#### 🎨 For Designers
- Create UI/UX mockups
- Design icons and graphics
- Improve the design system
- Create tutorial videos

#### 📝 For Writers
- Write documentation
- Create user guides
- Improve README and wiki
- Translate to other languages

### Getting Started with Contributing

1. **Check out the [Issues](https://github.com/yourusername/roam/issues)** - Look for issues tagged with `good first issue` or `help wanted`
2. **Fork the repository** and create your feature branch
3. **Comment on the issue** you'd like to work on
4. **Submit a Pull Request** when ready

### Development Workflow

```bash
# 1. Fork and clone
git clone https://github.com/YOUR-USERNAME/roam.git
cd roam

# 2. Create a feature branch
git checkout -b feature/your-feature-name

# 3. Make your changes and commit
git add .
git commit -m "Add: brief description of changes"

# 4. Push to your fork
git push origin feature/your-feature-name

# 5. Open a Pull Request on GitHub
```

### Commit Message Convention

We follow [Conventional Commits](https://www.conventionalcommits.org/):

```
feat: add calendar month view
fix: resolve task deletion bug
docs: update README with setup instructions
style: format code according to style guide
refactor: restructure service layer
test: add unit tests for OperationService
chore: update dependencies
```

### Code Style

- Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Use meaningful variable and method names
- Add JavaDoc comments for public methods
- Keep methods small and focused
- Write tests for new features

---

## 📚 Documentation (In Progress)

We're building comprehensive documentation as we develop:

- [ ] [Architecture Overview](docs/ARCHITECTURE.md)
- [ ] [Database Schema](docs/DATABASE_SCHEMA.md)
- [ ] [API Documentation](docs/API.md)
- [ ] [UI Component Guide](docs/UI_COMPONENTS.md)
- [ ] [Contributing Guide](CONTRIBUTING.md)
- [ ] [Code of Conduct](CODE_OF_CONDUCT.md)

---

## 🐛 Report Issues

Found a bug or have a feature suggestion? Please [open an issue](https://github.com/yourusername/roam/issues/new)!

**Issue Template:**

```markdown
**Description**
A clear description of the issue or feature request

**Expected Behavior**
What you expected to happen

**Actual Behavior**
What actually happened

**Steps to Reproduce**
1. Go to '...'
2. Click on '...'
3. See error

**Environment**
- OS: [e.g., Windows 11, macOS 14, Ubuntu 22.04]
- Java Version: [e.g., OpenJDK 21.0.1]
- Roam Version: [e.g., commit hash or branch]

**Screenshots**
If applicable, add screenshots
```

---

## 💬 Community

Join our growing community:

- **Discussions**: [GitHub Discussions](https://github.com/yourusername/roam/discussions) - Ask questions, share ideas
- **Issues**: [Issue Tracker](https://github.com/yourusername/roam/issues) - Report bugs, request features
- **Discord**: [Join our server](#) _(Coming soon)_
- **Twitter**: [@RoamPKM](#) _(Coming soon)_

---

## 📄 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

```
MIT License

Copyright (c) 2024 Roam Contributors

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

## 🙏 Acknowledgments

This project will be built with incredible open-source technologies:

- [JavaFX](https://openjfx.io/) - Modern UI toolkit for Java
- [CalendarFX](https://github.com/dlsc-software-consulting-gmbh/CalendarFX) - Professional calendar component
- [RichTextFX](https://github.com/FXMisc/RichTextFX) - High-performance text editor
- [Hibernate](https://hibernate.org/) - Robust ORM framework
- [H2 Database](https://www.h2database.com/) - Fast embedded database
- [Flexmark](https://github.com/vsch/flexmark-java) - Markdown parser
- [Gradle](https://gradle.org/) - Build automation tool

Inspired by amazing tools like Notion, Obsidian, Google Calendar, and Roam Research.

---

## ⭐ Show Your Support

If you like this project idea and want to see it come to life:

- ⭐ **Star this repository** to show your support
- 👁️ **Watch** for updates on development progress
- 🍴 **Fork** and contribute to the project
- 📢 **Share** with others who might be interested

---

## 📊 Project Stats

![GitHub stars](https://img.shields.io/github/stars/yourusername/roam?style=social)
![GitHub forks](https://img.shields.io/github/forks/yourusername/roam?style=social)
![GitHub watchers](https://img.shields.io/github/watchers/yourusername/roam?style=social)
![GitHub issues](https://img.shields.io/github/issues/yourusername/roam)
![GitHub pull requests](https://img.shields.io/github/issues-pr/yourusername/roam)
![GitHub last commit](https://img.shields.io/github/last-commit/yourusername/roam)

---

<div align="center">

### 🚀 Join Us in Building the Future of PKM

**Roam** is more than just an application—it's a vision for how personal knowledge management should work. Clean, fast, and completely under your control.

**The best time to contribute was yesterday. The second best time is now.**
[![Email us](https://img.shields.io/badge/Email%20Me-1a1a1a?style=for-the-badge&logo=gmail&logoColor=white)](mailto:muntasiractive@gmail.com)
---

**Made with ❤️ by developers, for developers**

[⬆ Back to Top](#roam)

---

**© 2024 Roam. All rights reserved.**

</div>
