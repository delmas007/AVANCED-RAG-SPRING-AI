package com.example.avancedrag.Service.Imp;

import com.example.avancedrag.Exception.EntityNotFoundException;
import com.example.avancedrag.Exception.ErrorCodes;
import com.example.avancedrag.Model.Utilisateur;
import com.example.avancedrag.Model.Validation;
import com.example.avancedrag.Repository.ValidationRepository;
import com.example.avancedrag.Service.Dto.UtilisateurDto;
import com.example.avancedrag.Service.ValidationService;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
public class ValidationServiceImp implements ValidationService {

    public ValidationServiceImp(ValidationRepository validationRepository, NotificationMailServiceImp notificationMailServiceImp) {
        this.validationRepository = validationRepository;
        this.notificationMailServiceImp = notificationMailServiceImp;
    }

    final ValidationRepository validationRepository;
    private final NotificationMailServiceImp notificationMailServiceImp;


    @Override
        public void  enregistrer(UtilisateurDto utilisateurDto) {
        Validation validation = new Validation();
        validation.setUtilisateur(UtilisateurDto.toEntity(utilisateurDto));
        Instant creation = Instant.now();
        Instant expiration = creation.plus((Duration.ofMinutes(10)));
        validation.setCreation(creation);
        validation.setExpiration(expiration);
        Random random = new Random();
        int randomCode = random.nextInt(999999);
        String code = String.format("%06d", randomCode);

        validation.setCode(code);
        validationRepository.save(validation);
        notificationMailServiceImp.sendNotificationMail(validation);
    }
    public void  enregistrerr(UtilisateurDto utilisateurDto) {
        Validation validation = new Validation();
        suppressionParUtilisateur(UtilisateurDto.toEntity(utilisateurDto));
        validation.setUtilisateur(UtilisateurDto.toEntity(utilisateurDto));
        Instant creation = Instant.now();
        Instant expiration = creation.plus((Duration.ofMinutes(10)));
        validation.setCreation(creation);
        validation.setExpiration(expiration);

        Random random = new Random();
        int randomCode = random.nextInt(999999);
        String code = String.format("%06d", randomCode);

        validation.setCode(code);
        validationRepository.save(validation);
        notificationMailServiceImp.sendNotificationMailMot(validation);
    }

    public void suppressionParUtilisateur(Utilisateur utilisateur) {
        Optional<Validation> validation = validationRepository.findByUtilisateur(utilisateur);
        validation.ifPresent(value -> validationRepository.deleteById(value.getId()));
    }

    @Override
    public void resendMail(Utilisateur utilisateur) {
        Validation validation = validationRepository.findByUtilisateur(utilisateur).orElseThrow(() -> new EntityNotFoundException("Aucune validation en cours pour cet utilisateur", ErrorCodes.VALIDATION_NOT_FOUND));
        Random random = new Random();
        int randomCode = random.nextInt(999999);
        String code = String.format("%06d", randomCode);
        validation.setCode(code);
        validationRepository.save(validation);
        notificationMailServiceImp.sendNotificationMail(validation);
    }

    public Validation lireEnFonctionDuCode(String code) {
        return this.validationRepository.findByCode(code).orElseThrow(() -> new EntityNotFoundException("Le code est invalide",
                ErrorCodes.CODE_INVALIDE));
    }

    @Scheduled(cron = "0 */15 * * * *")
    public void deleteAllByExpirationBefore() {
        validationRepository.deleteAllByExpirationBefore(Instant.now());
    }

}
