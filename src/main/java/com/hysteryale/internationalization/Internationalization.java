/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.internationalization;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

public class Internationalization extends WebMvcConfigurerAdapter {

    @Bean
    public SessionLocaleResolver sessionLocaleResolver() {
        return new SessionLocaleResolver();
    }

    /**
     * Setting default locale as US
     * @return SessionLocaleResolver
     */
    @Bean
    public LocaleResolver localeResolve() {
        SessionLocaleResolver resolver = sessionLocaleResolver();
        resolver.setDefaultLocale(Locale.US);
        return resolver;
    }
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
        localeChangeInterceptor.setParamName("lang");
        return localeChangeInterceptor;
    }
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }

    /**
     * Specify the path of the message source
     * @return MessageSource
     */
    @Bean(name = "messageSource")
    public MessageSource getMessageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();

        messageSource.setBasename("languages/message");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
}
