package com.otblabs.jiinueboda.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ServiceLocator {
    private static ApplicationContext context;

    @Autowired
    public ServiceLocator(ApplicationContext applicationContext) {
        context = applicationContext;
    }

    public static <T> T getService(Class<T> serviceClass) {
        return context.getBean(serviceClass);
    }
}
