package com.example.avancedrag.Service;


import com.example.avancedrag.Service.Dto.UtilisateurDto;

public interface UtilisateurService {

    UtilisateurDto Inscription(UtilisateurDto utilisateur, String role);

    int activation(String code);
    UtilisateurDto loadUserByUsername(String username);

}
