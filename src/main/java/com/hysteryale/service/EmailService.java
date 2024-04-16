/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.service;

import com.mailjet.client.MailjetRequest;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;

public interface EmailService {


    /**
     * To send confirmation email to users when admin creates a new account
     * Email contains password
     */
    void sendRegistrationEmail(String userName, String password, String emailTo) throws MailjetSocketTimeoutException, MailjetException;
    void sendResetPasswordEmail(String userName, String newPassword, String emailTo) throws MailjetSocketTimeoutException, MailjetException;
}
