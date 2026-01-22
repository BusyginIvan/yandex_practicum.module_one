package ru.yandex.practicum.configuration;

import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ValidationConfiguration implements WebMvcConfigurer {

    @Override
    public org.springframework.validation.Validator getValidator() {
        return validator();
    }

    @Bean
    public LocalValidatorFactoryBean validator() {
        LocalValidatorFactoryBean v = new LocalValidatorFactoryBean();
        v.setMessageInterpolator(new ParameterMessageInterpolator());
        return v;
    }

    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor(
        jakarta.validation.Validator validator
    ) {
        MethodValidationPostProcessor p = new MethodValidationPostProcessor();
        p.setValidator(validator);
        return p;
    }
}
