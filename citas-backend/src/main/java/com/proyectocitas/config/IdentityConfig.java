package com.proyectocitas.config;

import java.time.Clock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
@EnableConfigurationProperties(IdentityProperties.class)
public class IdentityConfig {
    @Bean("identityClock")
    public Clock identityClock() { return Clock.systemUTC(); }

    @Bean("identityMailExecutor")
    public ThreadPoolTaskExecutor identityMailExecutor() {
        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("identity-mail-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(5);
        return executor;
    }

    @Bean
    @ConditionalOnMissingBean(JavaMailSender.class)
    public JavaMailSender identityMailSender(Environment env) {
        var sender = new JavaMailSenderImpl();
        sender.setHost(env.getProperty("spring.mail.host", env.getProperty("SMTP_HOST", "localhost")));
        sender.setPort(env.getProperty("spring.mail.port", Integer.class,
                env.getProperty("SMTP_PORT", Integer.class, 1025)));
        sender.setUsername(env.getProperty("spring.mail.username", env.getProperty("SMTP_USERNAME", "")));
        sender.setPassword(env.getProperty("spring.mail.password", env.getProperty("SMTP_PASSWORD", "")));
        sender.setDefaultEncoding("UTF-8");
        var properties = sender.getJavaMailProperties();
        properties.setProperty("mail.smtp.auth", env.getProperty("spring.mail.properties.mail.smtp.auth",
                env.getProperty("SMTP_AUTH", String.valueOf(!sender.getUsername().isBlank()))));
        properties.setProperty("mail.smtp.starttls.enable", env.getProperty("spring.mail.properties.mail.smtp.starttls.enable",
                env.getProperty("SMTP_STARTTLS", "false")));
        properties.setProperty("mail.smtp.starttls.required", properties.getProperty("mail.smtp.starttls.enable"));
        properties.setProperty("mail.smtp.ssl.enable", env.getProperty("spring.mail.properties.mail.smtp.ssl.enable",
                env.getProperty("SMTP_SSL", "false")));
        properties.setProperty("mail.smtp.connectiontimeout", "5000");
        properties.setProperty("mail.smtp.timeout", "5000");
        properties.setProperty("mail.smtp.writetimeout", "5000");
        properties.setProperty("mail.debug", "false");
        return sender;
    }
}
