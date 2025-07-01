package com.example.avancedrag.Exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCodes {
    EMAIL_PAS_TROUVER(1200),
    UTILISATEUR_PAS_TROUVER(1100),
    UTILISATEUR_DEJA_EXIST(1000),
    CODE_INVALIDE(900),
    CODE_EXPIRE(800),
    UTILISATEUR_NON_ACTIF(700),
    VALIDATION_NOT_FOUND(600),
    TOKEN_INVALIDE(650);




    private final int code;
}
