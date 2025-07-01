package com.example.avancedrag.Service;


import com.example.avancedrag.Model.Utilisateur;
import com.example.avancedrag.Service.Dto.UtilisateurDto;

public interface ValidationService {

    void enregistrer(UtilisateurDto utilisateurDto);

    void resendMail(Utilisateur utilisateur);


}
