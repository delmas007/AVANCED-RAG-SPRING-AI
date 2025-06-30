package com.example.avancedrag.Service.Dto;

import com.example.avancedrag.Model.Role;
import com.example.avancedrag.Model.Utilisateur;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UtilisateurDto {
    private String id;
    private String username;
    private String password;
    private String nom;
    private String prenom;
    private String email;
    private Boolean actif;
    private Role role;

    public static UtilisateurDto fromEntity(Utilisateur utilisateur){
        if (utilisateur == null){
            return null;
        }
        return UtilisateurDto.builder()
                .id(utilisateur.getId())
                .username(utilisateur.getUsername())
                .password(utilisateur.getPassword())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .email(utilisateur.getEmail())
                .actif(utilisateur.getActif())
                .role(utilisateur.getRole())
                .build();
    }

    public static Utilisateur toEntity(UtilisateurDto utilisateurDto){
        if (utilisateurDto == null){
            return null;
        }
        return Utilisateur.builder()
                .id(utilisateurDto.getId())
                .username(utilisateurDto.getUsername())
                .password(utilisateurDto.getPassword())
                .nom(utilisateurDto.getNom())
                .prenom(utilisateurDto.getPrenom())
                .email(utilisateurDto.getEmail())
                .actif(utilisateurDto.getActif())
                .role(utilisateurDto.getRole())
                .build();
    }
}
