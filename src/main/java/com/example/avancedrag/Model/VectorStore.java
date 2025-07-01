package com.example.avancedrag.Model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "vector_store")
public class VectorStore {

  @Id
  private String id;

  @Column(name = "content",columnDefinition = "TEXT")
  private String content;

  @Column(name = "metadata", columnDefinition = "jsonb")
  private String metadata;

  @Column(name = "embedding", columnDefinition = "vector(1536)")
  private float[] embedding;

}


