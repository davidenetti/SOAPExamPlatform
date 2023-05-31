package com.soapproject.soapprojectwebservice;

import org.springframework.context.annotation.Configuration;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.XsdSchema;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.security.auth.callback.CallbackHandler;

import org.bson.Document;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor;
import org.springframework.ws.soap.security.wss4j2.callback.KeyStoreCallbackHandler;
import org.springframework.ws.soap.security.wss4j2.callback.SimplePasswordValidationCallbackHandler;
import org.springframework.ws.soap.security.wss4j2.support.CryptoFactoryBean;

@EnableWs
@Configuration
public class WebServiceConfig extends WsConfigurerAdapter {

    private Map<String, String> retrieveCredentialsFromDB() {
        Map<String, String> credentialsList= new HashMap<>();
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("UsersDB");
        MongoCollection<Document> collection = database.getCollection("users");
        FindIterable<Document> cursor = collection.find();
        Document nextElement = null;
        try (final MongoCursor<Document> cursorIterator = cursor.cursor()) { 
            while(cursorIterator.hasNext()){
                nextElement = cursorIterator.next();
                String username = (String) nextElement.get("username");
                String password = (String) nextElement.get("password");
                credentialsList.put(username, password);
            }
        }

        return credentialsList;
    }

    @Bean
    public SimplePasswordValidationCallbackHandler securityCallbackHandler(){
        SimplePasswordValidationCallbackHandler callbackHandler = new SimplePasswordValidationCallbackHandler();
        Properties users = new Properties();
        Map<String, String> credentials = retrieveCredentialsFromDB();
        for (Map.Entry<String, String> entry : credentials.entrySet()){
            users.setProperty(entry.getKey(), entry.getValue());
        }

        callbackHandler.setUsers(users);
        System.out.println(users);

        return callbackHandler;
    }

    @Bean
    public KeyStoreCallbackHandler keyStoreCallbackHandler() throws Exception {
        KeyStoreCallbackHandler callbackhandler = new KeyStoreCallbackHandler();
        callbackhandler.setPrivateKeyPassword("serverkeystore");

        return callbackhandler;
    }

    @Bean
    public CryptoFactoryBean getCryptoFactoryBean() throws Exception {
        CryptoFactoryBean cryptoFactoryBean = new CryptoFactoryBean();
       
        ClassPathResource pathToFile = new ClassPathResource("server.jks");
        cryptoFactoryBean.setKeyStoreLocation(pathToFile);
        cryptoFactoryBean.setKeyStoreType("jks");
        cryptoFactoryBean.setKeyStorePassword("serverkeystore");
        cryptoFactoryBean.afterPropertiesSet();

        return cryptoFactoryBean;
    }

    @Bean
    public Wss4jSecurityInterceptor securityInterceptor() throws IOException, Exception {
        
        Wss4jSecurityInterceptor securityInterceptor = new Wss4jSecurityInterceptor();

        // Validate incoming request
        securityInterceptor.setValidationActions("UsernameToken Timestamp Signature Encrypt");
        securityInterceptor.setValidationTimeToLive(10); //10 second time windows to consider the request valid
        securityInterceptor.setEnableSignatureConfirmation(true);
        securityInterceptor.setValidationSignatureCrypto(getCryptoFactoryBean().getObject());
        securityInterceptor.setValidationDecryptionCrypto(getCryptoFactoryBean().getObject());
        CallbackHandler[] arrayCallbackHandlers = new CallbackHandler[2];
        arrayCallbackHandlers[0] = securityCallbackHandler();
        arrayCallbackHandlers[1] = keyStoreCallbackHandler();
        securityInterceptor.setValidationCallbackHandlers(arrayCallbackHandlers);
        
        //Encrypt the response
        securityInterceptor.setSecurementEncryptionUser("clientpublic");
        securityInterceptor.setSecurementEncryptionParts("{Content}{http://namespacetest.com}responseResult");
        securityInterceptor.setSecurementEncryptionCrypto(getCryptoFactoryBean().getObject());
        
        //Sign the response
        securityInterceptor.setSecurementActions("Timestamp Signature Encrypt");
        securityInterceptor.setSecurementUsername("server");
        securityInterceptor.setSecurementPassword("serverkeystore");
        securityInterceptor.setSecurementSignatureCrypto(getCryptoFactoryBean().getObject());

        securityInterceptor.afterPropertiesSet();

        return securityInterceptor;
    }


    @Override
    public void addInterceptors(List<EndpointInterceptor> interceptors) {
        try {
            interceptors.add(securityInterceptor());

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext applicationContext) {
		MessageDispatcherServlet servlet = new MessageDispatcherServlet();
		servlet.setApplicationContext(applicationContext);
		servlet.setTransformWsdlLocations(true);
		return new ServletRegistrationBean<>(servlet, "/ws/*");
    }

    @Bean(name = "exams")
    public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema examsSchema) {
        DefaultWsdl11Definition wsdl11Definition = new DefaultWsdl11Definition();
        wsdl11Definition.setPortTypeName("ExamsPort");
        wsdl11Definition.setLocationUri("http://localhost:8080/ws/exams.wsdl");
        wsdl11Definition.setTargetNamespace(ExamsEndpoint.NAMESPACE_URI);
        wsdl11Definition.setSchema(examsSchema);
        
        return wsdl11Definition;
    }

    @Bean
    public XsdSchema examsSchema() {
        return new SimpleXsdSchema(new ClassPathResource("exams.xsd"));
    }
}

