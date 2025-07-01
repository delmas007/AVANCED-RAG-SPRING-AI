package com.example.avancedrag.Service.Imp;

import com.example.avancedrag.Model.Utilisateur;
import com.example.avancedrag.Repository.UtilisateurRepository;
import com.example.avancedrag.Service.ResendMail;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class ResendMailImp implements ResendMail {

    UtilisateurRepository utilisateurRepository;
    ValidationServiceImp validationServiceImp;

    @Override
    public int resendMail(String email) {
        Optional<Utilisateur> byEmail = utilisateurRepository.findByEmail(email);
        if (byEmail.isPresent()) {
            Utilisateur user = byEmail.get();
            validationServiceImp.resendMail(user);
            return 1;
        }
        return 0;
    }
}
