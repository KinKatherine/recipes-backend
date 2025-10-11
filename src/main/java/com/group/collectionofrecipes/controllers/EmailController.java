package com.group.collectionofrecipes.controllers;

import com.group.collectionofrecipes.dto.Mail;
import com.group.collectionofrecipes.services.MailSenderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "qwerty")
@RestController
@RequiredArgsConstructor
@Slf4j
public class EmailController {

    private final MailSenderService mailSenderService;

    @PostMapping("/api/v1/simple")
    public void sendMail(@RequestBody Mail mail){
        mailSenderService.sendSimpleEmail(mail);
    }

}
