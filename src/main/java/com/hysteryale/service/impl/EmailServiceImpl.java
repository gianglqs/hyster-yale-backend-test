package com.hysteryale.service.impl;

import com.hysteryale.service.EmailService;
import com.hysteryale.utils.EnvironmentUtils;
import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import com.mailjet.client.resource.Emailv31;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {
    //for testing only, should put into the configuration
    private final String emailFrom = "bao.khuu@edge-works.net";

    @Override
    public void sendRegistrationEmail(String userName, String password, String emailTo) throws MailjetSocketTimeoutException, MailjetException {

        String subject = "Welcome to Hyster-Yale";
        String textPart = "Your account is ready to use";
        String htmlPart = "<h1> Welcome " +  userName + ",</h1>";
        htmlPart += "<h3> Your account is ready to use, please log-in with your email and password: " + password + ".</h3>";

        //create email body
        MailjetRequest request = createEmailRequest(emailTo, subject, textPart, htmlPart);
        sendEmail(request);
    }

    @Override
    public void sendResetPasswordEmail(String userName, String newPassword, String emailTo) throws MailjetSocketTimeoutException, MailjetException {

        String subject = "Welcome to Hyster-Yale";
        String textPart = "Your account's password has been updated";
        String htmlPart = "<h1> Hello " +  userName + ",</h1>";
        htmlPart += "<h3> Your account's password has been updated, please log-in with your new password: " + newPassword + "</h3>";

        //create email body
        MailjetRequest request = createEmailRequest(emailTo, subject, textPart, htmlPart);
        sendEmail(request);
    }

    private MailjetRequest createEmailRequest(String emailTo, String subject, String textPart, String htmlPart){
        return new MailjetRequest(Emailv31.resource)
                .property(Emailv31.MESSAGES, new JSONArray()
                        .put(new JSONObject()
                                .put(Emailv31.Message.FROM, new JSONObject()
                                        .put("Email", emailFrom)
                                )
                                .put(Emailv31.Message.TO, new JSONArray()
                                        .put(new JSONObject()
                                                .put("Email", emailTo)
                                        )
                                )
                                .put(Emailv31.Message.SUBJECT, subject)
                                .put(Emailv31.Message.TEXTPART, textPart)
                                .put(Emailv31.Message.HTMLPART, htmlPart)
                        )
                );
    }

    private void sendEmail(MailjetRequest request) throws MailjetSocketTimeoutException, MailjetException {
        MailjetResponse response;

        MailjetClient client = new MailjetClient(
                EnvironmentUtils.getEnvironmentValue("mailjet.apiKey"),
                EnvironmentUtils.getEnvironmentValue("mailjet.apiSecret"),
                new ClientOptions("v3.1")
        );

        response = client.post(request);
        log.info(response.getStatus() + " " + response.getData());
    }
}
