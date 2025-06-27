package angaman.cedrick.rag_openai.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "utilisateur")
public class Utilisateur {

    @Id
    private String id ;

    @Column(name = "username",unique = true)
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "nom")
    private String nom;

    @Column(name = "prenom")
    private String prenom;

    @Column(name = "email",unique = true)
    private String email;

    @Column(name = "actif")
    private Boolean actif;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "roleId")
    private Role role;

//    @OneToMany(mappedBy = "utilisateur")
//    @JsonIgnore
    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VectorStore> vectorStores;

    @OneToMany(mappedBy = "utilisateur")
    private List<Jwt> jwts;

    @OneToMany(mappedBy = "utilisateur")
    private List<Validation> validations;





}
