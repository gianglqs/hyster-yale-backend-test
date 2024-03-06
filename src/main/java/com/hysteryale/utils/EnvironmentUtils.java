package com.hysteryale.utils;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource("classpath:application.properties")
public class EnvironmentUtils implements EnvironmentAware {
    private static Environment env;

    public static String getEnvironmentValue(String propertyKey) {
        return env.getProperty(propertyKey);
    }

    @Override
    public void setEnvironment(Environment environment) {
        env = environment;
    }


    public static Dotenv dotenv() {
        return Dotenv.configure()
                .directory("./")
                .filename(".env")
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();
    }

    static {
        System.setProperty("PORT", dotenv().get("PORT"));
        System.setProperty("DATABASE_URL", dotenv().get("DATABASE_URL"));
        System.setProperty("DATABASE_USERNAME", dotenv().get("DATABASE_USERNAME"));
        System.setProperty("DATABASE_PASSWORD", dotenv().get("DATABASE_PASSWORD"));
//        System.setProperty("ROLBAR_KEY", dotenv().get("ROLBAR_KEY"));
//        System.setProperty("ROLBAR_ENVIROMENT", dotenv().get("ROLBAR_ENVIROMENT"));
//        System.setProperty("ROLBAR_CODE_VERSION", dotenv().get("ROLBAR_CODE_VERSION"));
        System.setProperty("BASE_FOLDER_IMPORT_POSTMAN", dotenv().get("BASE_FOLDER_IMPORT_POSTMAN"));
        System.setProperty("IMPORTED_FILES", dotenv().get("IMPORTED_FILES"));
        System.setProperty("MJ_API_KEY", dotenv().get("MJ_API_KEY"));
        System.setProperty("MJ_API_SECRET", dotenv().get("MJ_API_SECRET"));
        System.setProperty("BASE_FOLDER_UPLOAD", dotenv().get("BASE_FOLDER_UPLOAD"));
        System.setProperty("PUBLIC_FOLDER", dotenv().get("PUBLIC_FOLDER"));
        System.setProperty("EXCHANGE_RATE_API_KEY", dotenv().get("EXCHANGE_RATE_API_KEY"));
    }

}
