package angaman.cedrick.rag_openai.Repository;

import angaman.cedrick.rag_openai.Model.VectorStore;
import org.springframework.ai.document.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface VectorRepository extends JpaRepository<VectorStore, String> {

    @Modifying  // Indique que la requête modifie des données
    @Transactional
    @Query("delete from VectorStore v where v.utilisateur.id = :utilisateurId")
    void supprimerParUtilisateurId(@Param("utilisateurId") String utilisateurId);


    @Transactional(readOnly = true)
    @Query("SELECT v.id FROM VectorStore v")
    List<String> findAllVectorIds();


//    @Transactional
//    @Query("SELECT v FROM VectorStore v WHERE v.id = :id")
//    Optional<VectorStore> findByIdd(@Param("id") String id);

    @Transactional
    @Query("SELECT u.id FROM VectorStore v JOIN v.utilisateur u WHERE v.id = :id")
    Optional<String> findUserIdByVectorStoreId(@Param("id") String id);

    @Transactional
    @Query("SELECT v.id, v.embedding FROM VectorStore v ")
    Optional<Document> findVectorStoreById();


//    @Transactional
//    @Query("SELECT v.id FROM VectorStore v WHERE v.id = :id")
//    Optional<String> findByIdd(@Param("id") String id);


    @Modifying
    @Transactional
    @Query(value = "UPDATE VectorStore v SET v.utilisateur.id = :userId WHERE v.id = :vectorId")
    void updateUserForVector(@Param("userId") String userId, @Param("vectorId") String vectorId);

}
