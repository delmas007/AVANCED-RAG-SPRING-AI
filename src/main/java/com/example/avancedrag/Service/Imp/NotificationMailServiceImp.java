package com.example.avancedrag.Service.Imp;

import com.example.avancedrag.Model.Validation;
import com.example.avancedrag.Service.NotificationMailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationMailServiceImp implements NotificationMailService {

    private final JavaMailSender javaMailSender;


    @Override
    public void sendNotificationMail(Validation validation) {
        try {
            MimeMessage mail = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mail, true);

            helper.setFrom("noreply@delmas-gpt.tech");
            helper.setTo(validation.getUtilisateur().getEmail());
            helper.setSubject("Votre code d'activation");

            String content = "<html>" +
                    "<body>" +
                    "    <div style=\"font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #edf2f7; padding: 20px; text-align: center;\">" +
                    "        <div style=\"background-color: #ffffff; width: 100%; max-width: 480px; margin: auto; box-shadow: 0 8px 16px rgba(0,0,0,0.1); border-radius: 10px; overflow: hidden; border-left: 5px solid #4a90e2;\">" +
                    "            <div style=\"background-color: #4a90e2; color: white; padding: 20px; font-size: 18px; text-align: center;\">Confirmation de votre compte</div>" +
                    "            <div style=\"padding: 20px; color: #333333; line-height: 1.6; text-align: center;\">" +
                    "                Bonjour <strong>" + validation.getUtilisateur().getNom() + "</strong>,<br><br>" +
                    "                Merci de vous joindre à nous. Veuillez entrer le code de confirmation suivant pour activer votre compte.<br>" +
                    "                <div style=\"font-size: 24px; font-weight: bold; background-color: #E8F0FE; color: #4a90e2; padding: 10px 20px; border-radius: 8px; display: inline-block; margin: 20px 0; text-align: center;\">" +
                    "                    " + validation.getCode() + "" +
                    "                </div>" +
                    "                <br>" +
                    "                Si vous n'avez pas demandé ce code, veuillez ignorer cet e-mail ou nous contacter." +
                    "            </div>" +
                    "            <div style=\"background-color: #f7f7f7; color: #666666; text-align: center; padding: 12px 20px; font-size: 14px;\">© 2024 Angaman Cedrick Tous droits réservés.</div>" +
                    "        </div>" +
                    "    </div>" +
                    "</body>" +
                    "</html>";


            helper.setText(content, true);
            javaMailSender.send(mail);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void sendNotificationMailMot(Validation validation) {
        try {
            MimeMessage mail = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mail, true);

            helper.setFrom("noreply@delmas-gpt.tech");
            helper.setTo(validation.getUtilisateur().getEmail());
            helper.setSubject("Votre code de changement de mot de passe");

            String content = "<html>" +
                    "<body>" +
                    "    <div style=\"font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #edf2f7; padding: 20px; text-align: center;\">" +
                    "        <div style=\"background-color: #ffffff; width: 100%; max-width: 480px; margin: auto; box-shadow: 0 8px 16px rgba(0,0,0,0.1); border-radius: 10px; overflow: hidden; border-left: 5px solid #4a90e2;\">" +
                    "            <div style=\"background-color: #4a90e2; color: white; padding: 20px; font-size: 18px; text-align: center;\">Code de changement de mot de passe</div>" +
                    "            <div style=\"padding: 20px; color: #333333; line-height: 1.6; text-align: center;\">" +
                    "                Bonjour <strong>" + validation.getUtilisateur().getNom() + "</strong>,<br><br>" +
                    "                Vous avez demandé à changer votre mot de passe. Veuillez entrer le code de confirmation suivant pour continuer.<br>" +
                    "                <div style=\"font-size: 24px; font-weight: bold; background-color: #E8F0FE; color: #4a90e2; padding: 10px 20px; border-radius: 8px; display: inline-block; margin: 20px 0; text-align: center;\">" +
                    "                    " + validation.getCode() + "" +
                    "                </div>" +
                    "                <br>" +
                    "                Si vous n'avez pas demandé ce code, veuillez ignorer cet e-mail ou nous contacter." +
                    "            </div>" +
                    "            <div style=\"background-color: #f7f7f7; color: #666666; text-align: center; padding: 12px 20px; font-size: 14px;\">© 2024 Angaman Cedrick Tous droits réservés.</div>" +
                    "        </div>" +
                    "    </div>" +
                    "</body>" +
                    "</html>";


            helper.setText(content, true);
            javaMailSender.send(mail);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}