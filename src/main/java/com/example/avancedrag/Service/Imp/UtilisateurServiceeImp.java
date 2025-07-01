package com.example.avancedrag.Service.Imp;

import com.example.avancedrag.Exception.EntityNotFoundException;
import com.example.avancedrag.Exception.ErrorCodes;
import com.example.avancedrag.Model.Utilisateur;
import com.example.avancedrag.Repository.UtilisateurrRepository;
import com.example.avancedrag.Service.Dto.UtilisateurDto;
import com.example.avancedrag.Service.UtilisateurServicee;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UtilisateurServiceeImp implements UtilisateurServicee {

    UtilisateurrRepository utilisateurrRepository;

    public UtilisateurServiceeImp(UtilisateurrRepository utilisateurrRepository) {
        this.utilisateurrRepository = utilisateurrRepository;
    }



    @Override
    public UtilisateurDto loadUserByUsername(String username) {
        Optional<Utilisateur> user = utilisateurrRepository.findByUsername(username);
        return UtilisateurDto.fromEntity(user.orElseThrow(()-> new EntityNotFoundException("Utilisateur pas trouver ",
                ErrorCodes.UTILISATEUR_PAS_TROUVER)));
    }

}
