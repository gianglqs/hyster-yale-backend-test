/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.rollbar;

//import com.rollbar.notifier.Rollbar;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

//import static com.rollbar.notifier.config.ConfigBuilder.withAccessToken;

@Configuration
public class RollbarInitializer {

//    protected static Rollbar rollbar;
//
//    @Value("${rollbar.secret-key}")
//    private String SECRET_KEY;
//
//    @Value("${rollbar.environment}")
//    private String ENV;
//
//    @Value("${rollbar.code-version}")
//    private String CODE_VERSION;
//
//
//    @PostConstruct
//    public void init() {
//        // currently I disabled rollbar because it is running out of monthly quota
//        rollbar = Rollbar.init(withAccessToken(SECRET_KEY)
//                .environment(ENV)
//                .codeVersion(CODE_VERSION)
//                .enabled(false)
//                .build());
//    }
}
