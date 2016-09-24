package com.spring.german.registration;

import com.spring.german.entity.User;
import com.spring.german.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.UUID;

/**
 * Handles the OnRegistrationCompleteEvent
 */
@Component
public class RegistrationListener implements ApplicationListener<OnRegistrationCompleteEvent> {

    Logger log = LoggerFactory.getLogger(RegistrationListener.class);

    private static final String LOGO_NAME = "germany-logo.jpg";

    /**
     * The instance of the TemplateEngine class is provided by Spring Boot
     * Thymeleaf auto configuration. All we need to do is to call the process()
     * method which expects the name of the template that we want to use and
     * the context object that acts as a container for our model.
     */
    @Autowired
    private TemplateEngine templateEngine;
    @Autowired
    private UserService service;
    @Autowired
    private MessageSource messages;
    @Autowired
    private JavaMailSender mailSender;

    @Inject
    private ApplicationContext context;

    @Override
    public void onApplicationEvent(OnRegistrationCompleteEvent event) {
        try {
            this.confirmRegistration(event);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    /**
     * TODO: c-p#1
     * confirmRegistration method will receive the OnRegistrationCompleteEvent,
     * extract all the necessary User information from it, create the
     * verification token, persist it, and then send it as a parameter in the
     * “Confirm Registration” link.
     *
     * As was mentioned above, any javax.mail.AuthenticationFailedException
     * thrown by JavaMailSender will be handled by the controller.
     * @param event
     */
    private void confirmRegistration(OnRegistrationCompleteEvent event) throws IOException, MessagingException {
        User user = event.getUser();
        log.info("RegistrationListener accepts the following user: {}", event.getUser());
        /**
         * UUID - Universally Unique Identifier.
         * UUID.randomUUID() - Static factory to retrieve a type 4 (pseudo
         * randomly generated) UUID. The UUID is generated using a
         * cryptographically strong pseudo random number generator.
         */

        String token = UUID.randomUUID().toString();
        service.createVerificationToken(user, token);

        String recipientAddress = user.getEmail();
        log.info("Destination email: {}", user.getEmail());
        log.info("You will prepend the following url to your link: {}", event.getAppUrl());
        String subject = "Registration Confirmation";
        String confirmationUrl
                = event.getAppUrl() + "/registrationConfirm?token=" + token;
        String message = messages.getMessage("message.registration.success", null, event.getLocale());
        log.info("Based on user's locale, the following message, was fetched " +
                "from the properties file: {{}, locale: {}}", message, event.getLocale());

        // Prepare the evaluation context
        final Context ctx = new Context(event.getLocale());
        ctx.setVariable("name", user.getFirstName());
        ctx.setVariable("body", message + " \n" + "http://localhost:8080" + confirmationUrl);
        ctx.setVariable("imageResourceName", LOGO_NAME); // so that we can reference it from HTML


        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        final String htmlContent = this.templateEngine.process("email-inlineimage", ctx);
        mimeMessageHelper.setText(htmlContent, true); // true = isHtml
        mimeMessageHelper.setTo(recipientAddress);
        mimeMessageHelper.setSubject(subject);

//        mimeMessageHelper.setText(message + " \n" + "http://localhost:8080" + confirmationUrl);

        mimeMessageHelper.addInline(LOGO_NAME, new ClassPathResource("static/img/germany-logo.jpg"));

        log.info("JavaMailSender performs an email dispatch");
        mailSender.send(mimeMessage);
    }
}
