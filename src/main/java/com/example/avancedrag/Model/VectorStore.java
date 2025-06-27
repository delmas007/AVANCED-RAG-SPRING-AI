package angaman.cedrick.rag_openai.Model;


import com.fasterxml.jackson.annotation.JsonIgnore;
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

//  @Column(name = "embedding", columnDefinition = "vector(3)")  // 3 est la dimension du vecteur
  @Column(name = "embedding", columnDefinition = "vector(1536)")  // 1536 est la dimension du vecteur
  private float[] embedding;  // Utiliser float[] pour représenter le vecteur en Java

//  @ManyToOne
//  @JsonIgnore
//  @JoinColumn(name = "utilisateur_id")
//  private Utilisateur utilisateur;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "utilisateur_id")
  private Utilisateur utilisateur;

}


