package com.movieflix.movieapi.service;

import com.movieflix.movieapi.dto.MailBody;

public interface EmailService {

    void sendSimpleMessage(MailBody mailBody);
}
