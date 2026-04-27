package com.rs.subscription.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "mail")
@Getter
@Setter
public class MailProperties {

    private boolean enabled = false;
    private String host = "localhost";
    private int port = 587;
    private String username = "";
    private String password = "";
    private From from = new From();
    private Ssl ssl = new Ssl();

    @Getter
    @Setter
    public static class From {
        private String name = "Subscription Management";
        private String address = "noreply@example.com";
    }

    @Getter
    @Setter
    public static class Ssl {
        private boolean enabled = false;
    }
}
