package com.cacaoscan.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendRecoveryEmail(String toEmail, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("soporte@cacaoscan.com");
            message.setTo(toEmail);
            message.setSubject("Recuperación de Contraseña - CacaoScan");
            
            // Simular enlace. En producción esto apuntaría al deep link o URL frontend
            String recoveryLink = "cacaoscan://reset-password?token=" + token;
            
            String body = "Hola,\n\n" +
                    "Hemos recibido una solicitud para restablecer la contraseña de tu cuenta de CacaoScan.\n" +
                    "Por favor, utiliza el siguiente token de recuperación en la aplicación o haz clic en el enlace:\n\n" +
                    "Token: " + token + "\n" +
                    "Enlace: " + recoveryLink + "\n\n" +
                    "Este token expirará en 30 minutos.\n" +
                    "Si no realizaste esta solicitud, puedes ignorar este correo.\n\n" +
                    "Atentamente,\n" +
                    "El equipo de CacaoScan (Finca La Variante)";
            
            message.setText(body);
            mailSender.send(message);
            logger.info("Correo de recuperación enviado con éxito a {}", toEmail);
        } catch (Exception e) {
            logger.error("Error al enviar el correo de recuperación a {}: {}", toEmail, e.getMessage());
            // No propagamos el error para no revelar información o causar fallas graves en la API
        }
    }
}
