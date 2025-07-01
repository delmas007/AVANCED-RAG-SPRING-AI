package com.example.avancedrag.Service.Imp;

import com.example.avancedrag.Exception.ErrorCodes;
import com.example.avancedrag.Model.Jwt;
import com.example.avancedrag.Model.Role;
import com.example.avancedrag.Model.Utilisateur;
import com.example.avancedrag.Model.Validation;
import com.example.avancedrag.Repository.JwtRepository;
import com.example.avancedrag.Repository.UtilisateurRepository;
import com.example.avancedrag.Repository.ValidationRepository;
import com.example.avancedrag.Service.Dto.UtilisateurDto;
import com.example.avancedrag.Service.UtilisateurService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import com.example.avancedrag.Exception.EntityNotFoundException;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UtilisateurServiceImp implements UtilisateurService {

    UtilisateurRepository utilisateurRepository;

    PasswordEncoder passwordEncoder;
    AuthenticationManager authenticationManager;
    JwtEncoder jwtEncoder;
    ValidationRepository validationRepository;
    JwtRepository jwtRepository;

    public UtilisateurServiceImp(ValidationRepository validationRepository,UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtEncoder jwtEncoder, ValidationServiceImp validationServiceImp, JwtRepository jwtRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
        this.validationServiceImp = validationServiceImp;
        this.validationRepository = validationRepository;
        this.jwtRepository = jwtRepository;
    }

    ValidationServiceImp validationServiceImp;
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public UtilisateurDto Inscription(UtilisateurDto utilisateur, String role) {
        Utilisateur existingUser = utilisateurRepository.findByUsername(utilisateur.getUsername()).orElse(null);
        if (existingUser == null) {
            Utilisateur newUser = Utilisateur.builder()
                    .id(UUID.randomUUID().toString())
                    .username(utilisateur.getUsername())
                    .password(passwordEncoder.encode(utilisateur.getPassword()))
                    .nom(utilisateur.getNom())
                    .prenom(utilisateur.getPrenom())
                    .email(utilisateur.getEmail())
                    .actif(false)
                    .role(Role.builder().role(role.toUpperCase()).build())
                    .build();
            Utilisateur savedUser = utilisateurRepository.save(newUser);
            UtilisateurDto utilisateurDto = UtilisateurDto.fromEntity(savedUser);
            entityManager.close();
            validationServiceImp.enregistrer(utilisateurDto);
            return utilisateurDto;
        } else {
            throw new EntityNotFoundException("Utilisateur existe deja", ErrorCodes.UTILISATEUR_DEJA_EXIST);
        }
    }

    @Override
    public int activation(String code) {
        Validation leCodeEstInvalide = validationServiceImp.lireEnFonctionDuCode(code);
        if (Instant.now().isAfter(leCodeEstInvalide.getExpiration())) {
            throw new EntityNotFoundException("Le code a expiré", ErrorCodes.CODE_EXPIRE);
        }
        Utilisateur utilisateurActiver = utilisateurRepository.findById(leCodeEstInvalide.getUtilisateur().getId()).orElseThrow(() -> new EntityNotFoundException("Utilisateur pas trouver",
                ErrorCodes.UTILISATEUR_PAS_TROUVER));
        utilisateurActiver.setActif(true);
        utilisateurRepository.save(utilisateurActiver);
        return 1;
    }
    public int motDePasse(String username) {
        UtilisateurDto utilisateurDto = emailSearch(username);
        validationServiceImp.enregistrerr(utilisateurDto);
        return 1;
    }

    @Transactional
    public int NouveauMotDePasse(Map<String, String> donnees) {
        Validation validation = validationServiceImp.lireEnFonctionDuCode(donnees.get("code"));
        UtilisateurDto utilisateurDto = emailSearch(donnees.get("email"));
        Utilisateur entity = UtilisateurDto.toEntity(utilisateurDto);

        if(validation.getUtilisateur().getId().equals(entity.getId())){
            String mdpCrypte = passwordEncoder.encode(donnees.get("password"));
            entity.setPassword(mdpCrypte);
            entityManager.merge(entity);
            return 1;
        }
        return 1;
    }

    @Override
    public UtilisateurDto loadUserByUsername(String username) {
        Optional<Utilisateur> user = utilisateurRepository.findByUsername(username);
        return UtilisateurDto.fromEntity(user.orElseThrow(()-> new EntityNotFoundException("Utilisateur inexistant ",
                ErrorCodes.UTILISATEUR_PAS_TROUVER)));
    }

    public UtilisateurDto emailSearch(String email) {
        Optional<Utilisateur> user = utilisateurRepository.findByEmail(email);
        return UtilisateurDto.fromEntity(user.orElseThrow(()-> new EntityNotFoundException("Email inexistant ",
                ErrorCodes.EMAIL_PAS_TROUVER)));
    }



    public Jwt tokenByValue(String value) {
        return jwtRepository.findByValueAndDesactiveAndExpire(
                value, false, false
        ).orElseThrow(() -> new EntityNotFoundException("Token invalide", ErrorCodes.TOKEN_INVALIDE));
    }

    public ResponseEntity<Map<String, String>> Connexion(String username, String password) {
        String subject=null;
        String scope=null;
        UtilisateurDto utilisateur = loadUserByUsername(username);

        if (!utilisateur.getActif()) {
            throw new EntityNotFoundException("Utilisateur non actif", ErrorCodes.UTILISATEUR_NON_ACTIF);
        }
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        subject=utilisateur.getUsername();
        String nom = loadUserByUsername(username).getNom();
        String id = loadUserByUsername(username).getId();
        String prenom = loadUserByUsername(username).getPrenom();
        scope=authentication.getAuthorities()
                .stream().map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));


        Map<String, String> idToken=new HashMap<>();
        Instant instant=Instant.now();
        JwtClaimsSet jwtClaimsSet=JwtClaimsSet.builder()
                .subject(subject)
                .issuedAt(instant)
                .expiresAt(instant.plus((Duration.ofMinutes(30))))
                .issuer("security-service")
                .claim("scope",scope)
                .claim("nom",nom)
                .claim("id",id)
                .claim("prenom",prenom)
                .build();
        String jwtAccessToken=jwtEncoder.encode(JwtEncoderParameters.from(jwtClaimsSet)).getTokenValue();
        idToken.put("accessToken",jwtAccessToken);
        final Jwt jwt = Jwt.builder()
                .value(jwtAccessToken)
                .desactive(false)
                .expire(false)
                .utilisateur(UtilisateurDto.toEntity(utilisateur))
                .build();
        this.jwtRepository.save(jwt);

        return new ResponseEntity<>(idToken, HttpStatus.OK);
    }



}
