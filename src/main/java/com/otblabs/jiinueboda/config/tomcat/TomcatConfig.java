package com.otblabs.jiinueboda.config.tomcat;


import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class TomcatConfig {

//    @Bean
//    public TomcatServletWebServerFactory tomcatFactory() {
//        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
//        factory.addConnectorCustomizers(connector -> {
//            if (connector.getProtocolHandler() instanceof AbstractHttp11Protocol<?> protocol) {
//                protocol.setMaxSwallowSize(-1);
//                // Max size per individual part (file) in bytes — 10MB
//                protocol.set   .setMaxPostSize(10 * 1024 * 1024);
//            }
//        });
//        return factory;
//    }
}