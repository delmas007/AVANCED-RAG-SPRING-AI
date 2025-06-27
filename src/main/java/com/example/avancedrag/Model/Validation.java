package angaman.cedrick.rag_openai.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.stringtemplate.v4.ST;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "Validation")
public class Validation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private Instant creation;

    private Instant expiration;

    private String code;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.DETACH})
    private Utilisateur utilisateur;

}
