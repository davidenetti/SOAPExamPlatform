package com.soapproject.soapprojectwebservice;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor;
import org.springframework.ws.soap.security.wss4j2.callback.KeyStoreCallbackHandler;
import org.springframework.ws.soap.security.wss4j2.support.CryptoFactoryBean;

@Configuration
public class ExamsClientConfig {
    private String username = "";
    private String password = "";
    private String actionToExecute = "";


    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setActionToExecute(String actionToExecute) {
        this.actionToExecute = actionToExecute;
    }

    @Bean
    public CryptoFactoryBean getCryptoFactoryBeanClient() throws Exception {
        org.apache.xml.security.Init.init();

        CryptoFactoryBean cryptoFactoryBean = new CryptoFactoryBean();

        cryptoFactoryBean.setKeyStoreType("jks");
        ClassPathResource pathToFile = new ClassPathResource("client.jks");
        cryptoFactoryBean.setKeyStoreLocation(pathToFile);
        cryptoFactoryBean.setKeyStorePassword("clientkeystore");
        cryptoFactoryBean.afterPropertiesSet();

        return cryptoFactoryBean;
    }


    @Bean
    public KeyStoreCallbackHandler keyStoreCallbackHandlerClient() throws Exception {
        KeyStoreCallbackHandler callbackhandler = new KeyStoreCallbackHandler();
        callbackhandler.setPrivateKeyPassword("clientkeystore");

        return callbackhandler;
    }

    @Bean
    public Wss4jSecurityInterceptor securityInterceptorClient() throws IOException, Exception {
        Wss4jSecurityInterceptor securityInterceptor = new Wss4jSecurityInterceptor();
        securityInterceptor.setSecurementActions("UsernameToken Timestamp Signature Encrypt");
        securityInterceptor.setSecurementUsername(this.username);
        securityInterceptor.setSecurementPassword(this.password);
        securityInterceptor.setSecurementSignatureCrypto(getCryptoFactoryBeanClient().getObject());

        securityInterceptor.setSecurementEncryptionUser("serverpublic");
        securityInterceptor.setSecurementEncryptionCrypto(getCryptoFactoryBeanClient().getObject());
        securityInterceptor.setSecurementEncryptionParts("{Content}{http://namespacetest.com}" + this.actionToExecute);

        // Validate incoming response
        securityInterceptor.setValidationActions("Timestamp Signature Encrypt");
        securityInterceptor.setValidationSignatureCrypto(getCryptoFactoryBeanClient().getObject());
        securityInterceptor.setValidationDecryptionCrypto(getCryptoFactoryBeanClient().getObject());
        securityInterceptor.setValidationCallbackHandler(keyStoreCallbackHandlerClient());
        securityInterceptor.setValidationTimeToLive(10); //10 second time windows to consider the request valid

        
        securityInterceptor.afterPropertiesSet();

        return securityInterceptor;
    }

    @Bean
    public Jaxb2Marshaller getMarshaller(){
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("com.namespacetest");

        return marshaller;
    }

    @Bean
    public ExamsClient getExamsClient() throws IOException, Exception{
        ExamsClient examsClient = new ExamsClient();
        examsClient.setMarshaller(getMarshaller());
        examsClient.setUnmarshaller(getMarshaller());
        examsClient.setDefaultUri("http://localhost:8080/ws/");
        ClientInterceptor[] interceptors = new ClientInterceptor[] {securityInterceptorClient()};
        examsClient.setInterceptors(interceptors);
        examsClient.afterPropertiesSet();

        return examsClient;
    }
}