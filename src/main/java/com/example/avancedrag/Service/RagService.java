package angaman.cedrick.rag_openai.Service;

import angaman.cedrick.rag_openai.Dto.UtilisateurDto;
import angaman.cedrick.rag_openai.Model.Utilisateur;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.stringtemplate.v4.ST;


public interface RagService {

    String askLlm(String query,UtilisateurDto utilisateur);

    void textEmbeddingPdf(Resource[] pdfResources, UtilisateurDto utilisateur);


    void textEmbeddingWord(Resource[] worldResources,UtilisateurDto utilisateur);

    void textEmbeddingExcel(Resource[] excelResources,UtilisateurDto utilisateur);

    void textEmbeddingPowerpoint(Resource[] PowerpointResources,UtilisateurDto utilisateur);

    String Image(String prompt);

}
