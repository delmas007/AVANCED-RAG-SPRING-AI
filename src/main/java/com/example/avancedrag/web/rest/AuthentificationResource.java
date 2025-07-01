package com.example.avancedrag.web.rest;

import com.example.avancedrag.Service.Dto.UtilisateurDto;
import com.example.avancedrag.Service.Imp.ResendMailImp;
import com.example.avancedrag.Service.Imp.UtilisateurServiceImp;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthentificationResource {

    private final UtilisateurServiceImp utilisateurServiceImp;
    private final ResendMailImp resendMailImp;


    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/inscription")
    public UtilisateurDto save(@RequestBody UtilisateurDto dto) {
        String role = "USER";
        return utilisateurServiceImp.Inscription(dto,role);
    }

    @PostMapping("/connexion")
    public ResponseEntity<Map<String, String>> Connexion(@RequestBody Map<String, String> authentification) {
        String username = authentification.get("username");
        String password = authentification.get("password");

        return utilisateurServiceImp.Connexion(username, password);
    }

    @PostMapping("/resendMail")
    public int resend(@RequestBody String email) {
        return resendMailImp.resendMail(email);
    }

    @PostMapping("/activation")
    public int activation(@RequestBody Map<String, String> codes) {
        String code = codes.get("code");
        return utilisateurServiceImp.activation(code);
    }

    @PostMapping("/modifierMotDePasse")
    public int motDePasse(@RequestBody Map<String, String> username) {
        String email = username.get("email");
        return utilisateurServiceImp.motDePasse(email);
    }

    @PostMapping("/NouveauMotDePasse")
    public int NouveauMotDePasse(@RequestBody Map<String, String> donnees) {
        return utilisateurServiceImp.NouveauMotDePasse(donnees);
    }
}
