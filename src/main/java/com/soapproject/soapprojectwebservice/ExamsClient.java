package com.soapproject.soapprojectwebservice;


import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;

import com.namespacetest.GetExamRequest;
import com.namespacetest.GetExamResponse;
import com.namespacetest.GetTranscriptRequest;
import com.namespacetest.GetTranscriptResponse;
import com.namespacetest.SetNewExamRequest;
import com.namespacetest.SetNewExamResponse;

public class ExamsClient extends WebServiceGatewaySupport {
    public String username = "";
    public String password = "";


    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public GetExamResponse getOneExam(GetExamRequest request)  {
        GetExamResponse examResponse = (GetExamResponse) getWebServiceTemplate().marshalSendAndReceive(request, new SoapActionCallback("http://namespacetest.com/getExamRequest")); 

        return examResponse;
    }

    public GetTranscriptResponse getTranscript(GetTranscriptRequest request) {
        GetTranscriptResponse transcriptResponse = (GetTranscriptResponse) getWebServiceTemplate().marshalSendAndReceive(request, new SoapActionCallback("http://namespacetest.com/getTranscriptRequest"));
            
        return transcriptResponse;
    }

    public SetNewExamResponse getNewInsertedExam(SetNewExamRequest request) {
        SetNewExamResponse newExamResponse = (SetNewExamResponse) getWebServiceTemplate().marshalSendAndReceive(request, new SoapActionCallback("http://namespacetest.com/setNewExamRequest"));

        return newExamResponse;
    }
}
