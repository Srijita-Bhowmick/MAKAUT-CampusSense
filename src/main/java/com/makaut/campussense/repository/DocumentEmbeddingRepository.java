package com.makaut.campussense.repository;

import com.makaut.campussense.model.DocumentEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface DocumentEmbeddingRepository extends JpaRepository<DocumentEmbedding, Long> {

    /**
     * Native pgvector cosine similarity search.
     * Uses CAST(:queryVector AS vector) for Hibernate 6 parameter compatibility.
     * Distance operator <=> is cosine distance; (1 - distance) is cosine similarity.
     */
    @Query(value = """
            SELECT de.id, de.resource_id, de.chunk_index, de.content, de.embedding, de.created_at,
                   (1 - (de.embedding <=> CAST(:queryVector AS vector))) AS similarity
            FROM document_embeddings de
            ORDER BY de.embedding <=> CAST(:queryVector AS vector) ASC
            LIMIT :topK
            """, nativeQuery = true)
    List<Object[]> searchTopKSimilarNative(@Param("queryVector") String queryVector, @Param("topK") int topK);

    /**
     * Native pgvector cosine similarity search with stream & semester filtering.
     */
    @Query(value = """
            SELECT de.id, de.resource_id, de.chunk_index, de.content, de.embedding, de.created_at,
                   (1 - (de.embedding <=> CAST(:queryVector AS vector))) AS similarity
            FROM document_embeddings de
            JOIN academic_resources ar ON de.resource_id = ar.id
            WHERE (:stream IS NULL OR ar.stream = :stream)
              AND (:semester IS NULL OR ar.semester = :semester)
            ORDER BY de.embedding <=> CAST(:queryVector AS vector) ASC
            LIMIT :topK
            """, nativeQuery = true)
    List<Object[]> searchTopKSimilarWithFilterNative(
            @Param("queryVector") String queryVector,
            @Param("stream") String stream,
            @Param("semester") Integer semester,
            @Param("topK") int topK
    );

    @Query("SELECT de FROM DocumentEmbedding de JOIN FETCH de.resource")
    List<DocumentEmbedding> findAllWithResource();

    List<DocumentEmbedding> findByResourceId(Long resourceId);

    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO document_embeddings (resource_id, chunk_index, content, embedding, created_at)
            VALUES (:resourceId, :chunkIndex, :content, CAST(:embedding AS vector), CURRENT_TIMESTAMP)
            """, nativeQuery = true)
    void insertWithNativeVector(
            @Param("resourceId") Long resourceId,
            @Param("chunkIndex") int chunkIndex,
            @Param("content") String content,
            @Param("embedding") String embedding
    );
}
