package com.soapproject.soapprojectwebservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import org.springframework.ws.soap.server.endpoint.annotation.SoapAction;

import com.namespacetest.GetExamRequest;
import com.namespacetest.GetExamResponse;
import com.namespacetest.GetTranscriptRequest;
import com.namespacetest.GetTranscriptResponse;
import com.namespacetest.SetNewExamRequest;
import com.namespacetest.SetNewExamResponse;

@Endpoint
public class ExamsEndpoint {
	public static final String NAMESPACE_URI="http://namespacetest.com";
    private ExamsRepository examsRepo;

    @Autowired
    public ExamsEndpoint (ExamsRepository examsRepo) {
        this.examsRepo = examsRepo;
    }

	@SoapAction(value = "http://namespacetest.com/getExamRequest")
	@ResponsePayload
	public GetExamResponse getASingleExam(@RequestPayload GetExamRequest request) {
		GetExamResponse response = new GetExamResponse();

		response.setResponseResult(examsRepo.findExamRegistration(request.getExamName(), request.getStudentID()));
	
		return response;
	}

	@SoapAction(value = "http://namespacetest.com/getTranscriptRequest")
	@ResponsePayload
	public GetTranscriptResponse getFullTranscript(@RequestPayload GetTranscriptRequest request) {
		GetTranscriptResponse response = new GetTranscriptResponse();

		response.setResponseResult(examsRepo.findAllexams(request.getStudentID()));

		return response;
	}

	@SoapAction(value = "http://namespacetest.com/setNewExamRequest")
	@ResponsePayload
	public SetNewExamResponse setNewExam(@RequestPayload SetNewExamRequest request) {
		SetNewExamResponse response = new SetNewExamResponse();
		response.setResponseResult(examsRepo.insertNewExam(request.getStudentID(), request.getExamName(), request.getMarkValue()));

		return response;
	}
}
