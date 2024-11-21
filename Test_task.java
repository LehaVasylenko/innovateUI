import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.*;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */
public class DocumentManager {

    //custom storage
    private Map<String, Document> storage = new HashMap<>();

    //counter for new documents
    private static int idCounter = 1;

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        //validate document
        Document validDocument = validateDocument(document);

        // Save or update document
        storage.put(validDocument.getId(), validDocument);
        return document;
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        List<Document> result = new ArrayList<>();

        for (Document document : storage.values()) {
            if (matchesSearchRequest(document, request)) {
                result.add(document);
            }
        }
        return result;
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    /**
     * Implementation of a method for generating a unique identifier for a document
     *
     * @return ID for a new document
     */
    private String generateId() {
        return "doc-" + idCounter++;
    }

    /**
     * Implementing a method to validate an incoming document
     *
     * @param document - document should be validated
     * @return document if validation successful
     * @throws IllegalArgumentException if required (from a logical point of view) fields are missing
     */
    private Document validateDocument(Document document) {
        //critical mistakes
        if (document.getTitle() == null || document.getTitle().isEmpty()) throw new IllegalArgumentException("'Title' field can't be empty!");
        if (document.getContent() == null || document.getContent().isEmpty()) throw new IllegalArgumentException("'Content' field can't be empty!");
        if (document.getAuthor() == null) throw new IllegalArgumentException("Document should have an Author!");
        if (document.getAuthor().getId() == null || document.getAuthor().getId().isEmpty()) throw new IllegalArgumentException("Document's author should have an ID!");
        if (document.getAuthor().getName() == null || document.getAuthor().getName().isEmpty()) throw new IllegalArgumentException("Document's author should have a name!");

        //if document have no ID (assuming it's a new document)
        if (document.getId() == null) {
            document.setId(generateId());
            document.setCreated(Instant.now());//as it's a new document - it has no time created yet
        }

        return document;
    }

    /**
     * IImplementation of a method that checks whether a document matches a search query
     *
     * @param document - document should be checked
     * @param request - search request which are compared to document
     * @return 'true' if document matches request or 'false' if not
     */
    private boolean matchesSearchRequest(Document document, SearchRequest request) {
        if (request.getTitlePrefixes() != null
                && request.getTitlePrefixes().stream().noneMatch(document.getTitle()::startsWith)) {
            return false;
        }
        if (request.getContainsContents() != null
                && request.getContainsContents().stream().noneMatch(document.getContent()::contains)) {
            return false;
        }
        if (request.getAuthorIds() != null
                && request.getAuthorIds().stream().noneMatch(authorId -> authorId.equals(document.getAuthor().getId()))) {
            return false;
        }
        if (request.getCreatedFrom() != null
                && document.getCreated().isAfter(request.getCreatedFrom())) {
            return false;
        }
        if (request.getCreatedTo() != null
                && document.getCreated().isBefore(request.getCreatedTo())) {
            return false;
        }
        return true;
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}