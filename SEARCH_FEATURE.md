# Global Search Functionality

## Overview
A comprehensive search feature has been fully implemented and integrated across all Roam modules using Apache Lucene. Users can now search across Notes, Tasks, Operations, Calendar Events, and Journal Entries from a unified search interface. **All data is automatically indexed when created or updated.**

## Features

### 1. **Search Bar in Sidebar**
- **Location**: Positioned in the left sidebar between the "Roam" title and navigation buttons
- **Always Accessible**: Available in all views for quick searching
- **Collapsed State**: Automatically hidden when sidebar is collapsed

### 2. **Full-Text Search**
- **Powered by Apache Lucene 9.9.1**: Industry-standard search engine
- **Fuzzy Matching**: Finds results even with typos (70% similarity threshold)
- **Multi-Field Search**: Searches across titles, content, descriptions, and metadata

### 3. **Search Across All Modules**
The search indexes and searches the following content types:

- **📝 Notes**: Title, content, region, and linked operation
- **✓ Tasks**: Title, description, priority, status, and due date
- **📋 Operations**: Name, purpose, outcome, priority, and status
- **📅 Calendar Events**: Title, description, location, and date/time
- **📓 Journal Entries**: Title, content, and date

### 4. **Advanced Filtering**
Search results can be filtered by:
- **Type**: Notes, Tasks, Operations, Events, Journal
- **Priority**: High, Medium, Low (for Tasks and Operations)
- **Status**: Various statuses depending on type
- **Region**: Filter Notes by region
- **Operation**: Find all items linked to a specific operation

### 5. **Results Display**
- **Grouped by Type**: Results organized into categories
- **Snippets**: Shows preview of matching content
- **Metadata**: Displays relevant information (priority, status, region, dates)
- **Color-Coded Badges**: Visual differentiation by content type
- **Click to Navigate**: Select any result to jump to that item

## Usage

### Basic Search
1. Click on the search bar in the sidebar (or press Enter in the field)
2. Type your search query
3. Press **Enter** to execute the search
4. Browse results in the dialog that appears

### Search Tips
- **Simple queries**: Just type what you're looking for (e.g., "meeting notes")
- **Fuzzy matching**: Don't worry about exact spelling
- **Multiple words**: Search for phrases or keywords
- **Filters**: Use the checkboxes to filter by content type

### Filtering Results
1. Execute a search
2. Use the checkboxes at the top of the results dialog:
   - ☑ Notes
   - ☑ Tasks
   - ☑ Operations
   - ☑ Events
   - ☑ Journal
3. Results update automatically as you toggle filters

## Technical Implementation

### Architecture
```
SearchService (Singleton)
├── Index Management
│   ├── Lucene FSDirectory (stored in data/search-index/)
│   ├── StandardAnalyzer for text processing
│   └── Auto-commit on each index operation
├── Indexing Methods
│   ├── indexNote()
│   ├── indexTask()
│   ├── indexOperation()
│   ├── indexEvent()
│   └── indexJournalEntry()
├── Search Methods
│   ├── search(query, filter)
│   └── buildQuery() with fuzzy matching
└── Maintenance
    ├── deleteDocument()
    ├── clearIndex()
    └── rebuildIndex()
```

### Dependencies Added
- **lucene-core:9.9.1** - Core search engine
- **lucene-queryparser:9.9.1** - Query parsing and syntax
- **lucene-highlighter:9.9.1** - Result highlighting (future use)
- **lucene-analyzers-common:8.11.2** - Text analysis tools

### Index Location
Search index is stored in: `data/search-index/`

## Automatic Indexing

**All content is automatically indexed when created, updated, or deleted:**

- ✅ **WikiController**: Notes are indexed on creation and save, removed from index on deletion
- ✅ **TasksController**: Tasks are indexed on creation, update, and status changes, removed from index on deletion
- ✅ **OperationsController**: Operations are indexed on creation and edit, removed from index on deletion
- ✅ **CalendarController**: Events are indexed on creation and edit, removed from index on deletion
- ✅ **JournalController**: Journal entries are indexed on creation and save, removed from index on deletion

The search index stays in sync with your data automatically - no manual action required!

## Rebuild Index Option

A **"Rebuild Search Index"** button is available in **Settings > Data Management** to manually rebuild the entire search index. This is useful:

- After importing data from JSON
- If search results seem outdated
- To optimize index performance
- After recovering from backup

The rebuild process:
1. Clears the existing index
2. Reindexes all Notes, Tasks, Operations, Events, and Journal entries
3. Shows progress dialog and completion count
4. Runs in background thread (non-blocking)

## Next Steps (Completed) ✅

All controllers now automatically index data when items are created, updated, or deleted. The search functionality is fully operational!

### Code Examples (For Reference)

**WikiController** - Indexing notes:
   ```java
   SearchService.getInstance().indexNote(
       note.getId(),
       note.getTitle(),
       note.getContent(),
       note.getRegion() != null ? note.getRegion().getName() : null,
       note.getOperation() != null ? note.getOperation().getId() : null,
       note.getUpdatedAt()
   );
   ```

2. **In TasksController** - Add indexing for tasks:
   ```java
   SearchService.getInstance().indexTask(
       task.getId(),
       task.getTitle(),
       task.getDescription(),
       task.getPriority(),
       task.getStatus(),
       task.getOperation() != null ? task.getOperation().getId() : null,
       task.getDueDate()
   );
   ```

3. **In OperationsController** - Add indexing for operations:
   ```java
   SearchService.getInstance().indexOperation(
       operation.getId(),
       operation.getName(),
       operation.getPurpose(),
       operation.getOutcome(),
       operation.getStatus(),
       operation.getPriority()
   );
   ```

4. **In CalendarController** - Add indexing for events:
   ```java
   SearchService.getInstance().indexEvent(
       event.getId(),
       event.getTitle(),
       event.getDescription(),
       event.getStartTime(),
       event.getEndTime(),
       event.getLocation()
   );
   ```

5. **In JournalView** - Add indexing for journal entries:
   ```java
   SearchService.getInstance().indexJournalEntry(
       entry.getId(),
       entry.getTitle(),
       entry.getContent(),
       entry.getDate()
   );
   ```

### Initial Index Build
Add a method to rebuild the entire index from existing data:
```java
public void rebuildFullIndex() {
    clearIndex();
    
    // Index all notes
    noteRepository.findAll().forEach(note -> indexNote(...));
    
    // Index all tasks
    taskRepository.findAll().forEach(task -> indexTask(...));
    
    // Index all operations
    operationRepository.findAll().forEach(op -> indexOperation(...));
    
    // Index all events
    eventRepository.findAll().forEach(event -> indexEvent(...));
    
    // Index all journal entries
    journalRepository.findAll().forEach(entry -> indexJournalEntry(...));
}
```

### Enhanced Features (Future)
- **Search Result Highlighting**: Highlight matching terms in snippets
- **Recent Searches**: Show dropdown of recent search queries
- **Keyboard Shortcut**: Add Ctrl+K/Cmd+K to focus search bar
- **Search Suggestions**: Auto-complete based on indexed content
- **Date Range Filters**: Filter results by date ranges
- **Advanced Query Syntax**: Support for AND, OR, NOT operators
- **Search History**: Save and recall previous searches
- **Export Results**: Export search results to CSV/JSON

## Performance Considerations
- Index updates are synchronous and commit immediately
- For bulk operations, consider batching index updates
- Recommended to rebuild index periodically for optimization
- Index size scales with content - monitor disk usage

## Troubleshooting

### Search not returning expected results
- Verify data is being indexed (check `data/search-index/` directory)
- Try rebuilding the index
- Check for typos (fuzzy matching has 70% threshold)

### Search is slow
- Consider rebuilding/optimizing the index
- Check index size and disk I/O performance
- Reduce result limit in SearchFilter

### Items not appearing in search
- Ensure auto-indexing is integrated in all controllers
- Verify the item was saved successfully
- Try rebuilding the entire index
