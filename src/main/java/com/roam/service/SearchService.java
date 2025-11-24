package com.roam.service;

import com.roam.util.InputSanitizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for searching across all modules using Apache Lucene
 */
public class SearchService {

    private static SearchService instance;
    private final Directory indexDirectory;
    private final StandardAnalyzer analyzer;
    private IndexWriter indexWriter;

    private static final String INDEX_PATH = System.getProperty("user.home") + "/.roam/index";

    private SearchService() throws IOException {
        // Ensure index directory exists
        java.nio.file.Path indexPath = Paths.get(INDEX_PATH);
        java.nio.file.Files.createDirectories(indexPath);

        this.indexDirectory = FSDirectory.open(indexPath);
        this.analyzer = new StandardAnalyzer();

        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        this.indexWriter = new IndexWriter(indexDirectory, config);
    }

    public static synchronized SearchService getInstance() {
        if (instance == null) {
            try {
                instance = new SearchService();
            } catch (IOException e) {
                throw new RuntimeException("Failed to initialize SearchService", e);
            }
        }
        return instance;
    }

    /**
     * Index a wiki
     */
    public void indexWiki(Long id, String title, String content, String region,
            Long operationId, LocalDateTime updatedAt) throws IOException {
        Document doc = new Document();

        doc.add(new StringField("id", id.toString(), Field.Store.YES));
        doc.add(new StringField("type", "wiki", Field.Store.YES));
        doc.add(new TextField("title", title != null ? title : "", Field.Store.YES));
        doc.add(new TextField("content", content != null ? content : "", Field.Store.YES));
        doc.add(new StringField("region", region != null ? region : "", Field.Store.YES));
        doc.add(new StringField("operationId", operationId != null ? operationId.toString() : "", Field.Store.YES));
        doc.add(new StringField("updatedAt",
                updatedAt != null ? updatedAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "",
                Field.Store.YES));

        // For fuzzy matching and better search
        doc.add(new TextField("allText",
                (title != null ? title + " " : "") + (content != null ? content : ""),
                Field.Store.NO));

        indexWriter.updateDocument(new Term("id", id.toString()), doc);
        indexWriter.commit();
    }

    /**
     * Index a task
     */
    public void indexTask(Long id, String title, String description, String priority,
            String status, Long operationId, LocalDateTime dueDate) throws IOException {
        Document doc = new Document();

        doc.add(new StringField("id", id.toString(), Field.Store.YES));
        doc.add(new StringField("type", "task", Field.Store.YES));
        doc.add(new TextField("title", title != null ? title : "", Field.Store.YES));
        doc.add(new TextField("description", description != null ? description : "", Field.Store.YES));
        doc.add(new StringField("priority", priority != null ? priority : "", Field.Store.YES));
        doc.add(new StringField("status", status != null ? status : "", Field.Store.YES));
        doc.add(new StringField("operationId", operationId != null ? operationId.toString() : "", Field.Store.YES));
        doc.add(new StringField("dueDate",
                dueDate != null ? dueDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "",
                Field.Store.YES));

        doc.add(new TextField("allText",
                (title != null ? title + " " : "") + (description != null ? description : ""),
                Field.Store.NO));

        indexWriter.updateDocument(new Term("id", id.toString()), doc);
        indexWriter.commit();
    }

    /**
     * Index a calendar event
     */
    public void indexEvent(Long id, String title, String description, LocalDateTime startTime,
            LocalDateTime endTime, String location) throws IOException {
        Document doc = new Document();

        doc.add(new StringField("id", id.toString(), Field.Store.YES));
        doc.add(new StringField("type", "event", Field.Store.YES));
        doc.add(new TextField("title", title != null ? title : "", Field.Store.YES));
        doc.add(new TextField("description", description != null ? description : "", Field.Store.YES));
        doc.add(new TextField("location", location != null ? location : "", Field.Store.YES));
        doc.add(new StringField("startTime",
                startTime != null ? startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "",
                Field.Store.YES));
        doc.add(new StringField("endTime",
                endTime != null ? endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "",
                Field.Store.YES));

        doc.add(new TextField("allText",
                (title != null ? title + " " : "") +
                        (description != null ? description + " " : "") +
                        (location != null ? location : ""),
                Field.Store.NO));

        indexWriter.updateDocument(new Term("id", id.toString()), doc);
        indexWriter.commit();
    }

    /**
     * Index a journal entry
     */
    public void indexJournalEntry(Long id, String title, String content, String date) throws IOException {
        Document doc = new Document();

        doc.add(new StringField("id", id.toString(), Field.Store.YES));
        doc.add(new StringField("type", "journal", Field.Store.YES));
        doc.add(new TextField("title", title != null ? title : "", Field.Store.YES));
        doc.add(new TextField("content", content != null ? content : "", Field.Store.YES));
        doc.add(new StringField("date", date != null ? date : "", Field.Store.YES));

        doc.add(new TextField("allText",
                (title != null ? title + " " : "") + (content != null ? content : ""),
                Field.Store.NO));

        indexWriter.updateDocument(new Term("id", id.toString()), doc);
        indexWriter.commit();
    }

    /**
     * Index an operation
     */
    public void indexOperation(Long id, String name, String purpose, String outcome,
            String status, String priority) throws IOException {
        Document doc = new Document();

        doc.add(new StringField("id", id.toString(), Field.Store.YES));
        doc.add(new StringField("type", "operation", Field.Store.YES));
        doc.add(new TextField("name", name != null ? name : "", Field.Store.YES));
        doc.add(new TextField("purpose", purpose != null ? purpose : "", Field.Store.YES));
        doc.add(new TextField("outcome", outcome != null ? outcome : "", Field.Store.YES));
        doc.add(new StringField("status", status != null ? status : "", Field.Store.YES));
        doc.add(new StringField("priority", priority != null ? priority : "", Field.Store.YES));

        doc.add(new TextField("allText",
                (name != null ? name + " " : "") +
                        (purpose != null ? purpose + " " : "") +
                        (outcome != null ? outcome : ""),
                Field.Store.NO));

        indexWriter.updateDocument(new Term("id", id.toString()), doc);
        indexWriter.commit();
    }

    /**
     * Search across all indexed content
     */
    public List<SearchResult> search(String queryString, SearchFilter filter) throws Exception {
        List<SearchResult> results = new ArrayList<>();

        if (queryString == null || queryString.trim().isEmpty()) {
            return results;
        }

        // Sanitize search query to prevent Lucene injection attacks
        String sanitizedQuery = InputSanitizer.sanitizeSearchQuery(queryString);

        IndexReader reader = DirectoryReader.open(indexDirectory);
        IndexSearcher searcher = new IndexSearcher(reader); // Build query
        Query query = buildQuery(sanitizedQuery, filter);

        // Execute search with limit
        TopDocs topDocs = searcher.search(query, filter.maxResults); // Process results
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            Document doc = searcher.storedFields().document(scoreDoc.doc);
            SearchResult result = new SearchResult();
            result.id = Long.parseLong(doc.get("id"));
            result.type = doc.get("type"); // Get title based on type (operations use "name" field)
            if ("operation".equals(result.type)) {
                result.title = doc.get("name");
            } else {
                result.title = doc.get("title");
            }

            result.snippet = getSnippet(doc);
            result.score = scoreDoc.score;

            // Type-specific fields
            switch (result.type) {
                case "wiki":
                    result.region = doc.get("region");
                    result.operationId = doc.get("operationId");
                    result.updatedAt = doc.get("updatedAt");
                    break;
                case "task":
                    result.priority = doc.get("priority");
                    result.status = doc.get("status");
                    result.operationId = doc.get("operationId");
                    result.dueDate = doc.get("dueDate");
                    break;
                case "event":
                    result.location = doc.get("location");
                    result.startTime = doc.get("startTime");
                    result.endTime = doc.get("endTime");
                    break;
                case "journal":
                    result.date = doc.get("date");
                    break;
                case "operation":
                    result.status = doc.get("status");
                    result.priority = doc.get("priority");
                    break;
            }

            results.add(result);
        }

        reader.close();
        return results;
    }

    private Query buildQuery(String queryString, SearchFilter filter) throws Exception {
        // Parse basic query
        String[] fields = { "title", "content", "description", "name", "purpose", "outcome", "location", "allText" };
        MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, analyzer);
        parser.setDefaultOperator(QueryParser.Operator.OR);
        parser.setFuzzyMinSim(0.7f); // Enable fuzzy matching

        Query baseQuery = parser.parse(queryString);

        // Add filters
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        builder.add(baseQuery, BooleanClause.Occur.MUST);

        // Filter by type
        if (filter.types != null && !filter.types.isEmpty()) {
            BooleanQuery.Builder typeBuilder = new BooleanQuery.Builder();
            for (String type : filter.types) {
                typeBuilder.add(new TermQuery(new Term("type", type)), BooleanClause.Occur.SHOULD);
            }
            builder.add(typeBuilder.build(), BooleanClause.Occur.MUST);
        }

        // Filter by region
        if (filter.region != null && !filter.region.isEmpty()) {
            builder.add(new TermQuery(new Term("region", filter.region)), BooleanClause.Occur.MUST);
        }

        // Filter by operation
        if (filter.operationId != null) {
            builder.add(new TermQuery(new Term("operationId", filter.operationId.toString())),
                    BooleanClause.Occur.MUST);
        }

        // Filter by priority
        if (filter.priority != null && !filter.priority.isEmpty()) {
            builder.add(new TermQuery(new Term("priority", filter.priority)), BooleanClause.Occur.MUST);
        }

        // Filter by status
        if (filter.status != null && !filter.status.isEmpty()) {
            builder.add(new TermQuery(new Term("status", filter.status)), BooleanClause.Occur.MUST);
        }

        return builder.build();
    }

    private String getSnippet(Document doc) {
        String content = doc.get("content");
        if (content == null)
            content = doc.get("description");
        if (content == null)
            content = doc.get("purpose");
        if (content == null)
            content = "";

        // Return first 150 characters as snippet
        if (content.length() > 150) {
            return content.substring(0, 150) + "...";
        }
        return content;
    }

    /**
     * Delete document from index
     */
    public void deleteDocument(Long id) throws IOException {
        indexWriter.deleteDocuments(new Term("id", id.toString()));
        indexWriter.commit();
    }

    /**
     * Clear entire index
     */
    public void clearIndex() throws IOException {
        indexWriter.deleteAll();
        indexWriter.commit();
    }

    /**
     * Rebuild entire index from database
     */
    public void rebuildIndex() {
        // This will be called to reindex all content
        // Implementation will fetch all data and reindex
        try {
            clearIndex();
            // Repositories will be used to fetch and reindex all data
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() throws IOException {
        if (indexWriter != null) {
            indexWriter.close();
        }
        if (indexDirectory != null) {
            indexDirectory.close();
        }
    }

    /**
     * Search result class
     */
    public static class SearchResult {
        public Long id;
        public String type;
        public String title;
        public String snippet;
        public float score;

        // Optional fields based on type
        public String region;
        public String operationId;
        public String updatedAt;
        public String priority;
        public String status;
        public String dueDate;
        public String location;
        public String startTime;
        public String endTime;
        public String date;
    }

    /**
     * Search filter class
     */
    public static class SearchFilter {
        public List<String> types = new ArrayList<>(); // wiki, task, event, journal, operation
        public String region;
        public Long operationId;
        public String priority;
        public String status;
        public int maxResults = 50;

        public SearchFilter() {
            // By default, search all types
            types.add("wiki");
            types.add("task");
            types.add("event");
            types.add("journal");
            types.add("operation");
        }
    }
}
