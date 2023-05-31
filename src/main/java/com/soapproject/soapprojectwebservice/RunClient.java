package com.soapproject.soapprojectwebservice;


import org.apache.commons.codec.digest.DigestUtils;

import com.namespacetest.ExamRegistration;
import com.namespacetest.GetExamRequest;
import com.namespacetest.GetExamResponse;
import com.namespacetest.GetTranscriptRequest;
import com.namespacetest.GetTranscriptResponse;
import com.namespacetest.SetNewExamRequest;
import com.namespacetest.SetNewExamResponse;

public class RunClient {
    public static void main(String[] args) throws Exception {
        String action;
        String username;
        String password;
        try {
            action = args[0];
            username = args[1]; 
            password = args[2];
        } catch (java.lang.ArrayIndexOutOfBoundsException exception) {
            System.out.println("\n\nPlease specify an action to execute as first argument: you can choose between 'AddExam', 'RequireAnExam', 'RequireTranscript'.");
            System.out.println("Please insert your username as second argument");
            System.out.println("Please specify your password as third argument\n\n");
            return ;
        }
        
        // Configure the configurations for the client (example the security interceptor on the client side)
        ExamsClientConfig clientConfig = new ExamsClientConfig();
        password = DigestUtils.sha3_512Hex(password);
        clientConfig.setPassword(password);
        clientConfig.setUsername(username);
        if(action.compareTo("RequireAnExam") == 0) {
            clientConfig.setActionToExecute("getExamRequest");
        }
        else if(action.compareTo("RequireTranscript") == 0) {
            clientConfig.setActionToExecute("getTranscriptRequest");
        }
        else if(action.compareTo("AddExam") == 0){
            clientConfig.setActionToExecute("setNewExamRequest");
        }

        ExamsClient examsClient = clientConfig.getExamsClient();

        if (action.compareTo("RequireAnExam") == 0) {
            String examName;
            try {
                examName = args[3];
            } catch (java.lang.ArrayIndexOutOfBoundsException exception) {
                System.out.println("\n\nPlease, if you wanna require an exam insert the one's name as the fourth argument.\n\n");
                return ;
            }
            GetExamRequest request = new GetExamRequest();
            request.setExamName(examName);
            request.setStudentID(username);

            GetExamResponse response = examsClient.getOneExam(request);

            ExamRegistration obtainedExam = response.getResponseResult();
            System.out.println("\n\nResponse, exam name is: " +  obtainedExam.getExamName());
            System.out.println("Response, mark value is: " +  obtainedExam.getValue());
            System.out.println("Response, candidate name is: " +  obtainedExam.getCandidateName());
            System.out.println("Response, registration date is: " +  obtainedExam.getRegistrationDate());
        }
        else if (action.compareTo("RequireTranscript") == 0) {
            GetTranscriptRequest request = new GetTranscriptRequest();
            request.setStudentID(username);

            GetTranscriptResponse response = examsClient.getTranscript(request);

            String obtainedTranscript = response.getResponseResult();
            System.out.println("\n\nResponse, This is the transcript: ");
            System.out.println(obtainedTranscript);
        }
        else if(action.compareTo("AddExam") == 0) {
            String newExamName;
            String newMarkValue;
            String studentIDWhomAssignInsertedNewExam;

            try {
                newExamName = args[3];
                newMarkValue = args[4];
                studentIDWhomAssignInsertedNewExam = args[5];
            } catch (java.lang.ArrayIndexOutOfBoundsException exception) {
                System.out.println("\n\nPlease, if you wanna insert a new exam, specify the new exam name as fourth argument, a mark value for the new exam as fifth argument and the studentID of the student who will receive the new exam.\n\n");
                return ;
            }
            // Only a professor can add an exam to a transcript so check that who wanna insert a new exam is a "professore"
            if(!username.toLowerCase().contains("professore")) {
                System.out.println("\n\nOnly professors can add exams to a transcript of a student.\n\n");
                return ;
            }
            else {

                if (Integer.parseInt(args[4]) < 18 || Integer.parseInt(args[4]) > 31) {
                    System.out.println("\n\nIs not legal to insert a new mark with a value minor than 18 and major than 31.\n\n");
                    return ;
                }
                else {
                    SetNewExamRequest request =  new SetNewExamRequest();
                    request.setExamName(newExamName);
                    request.setMarkValue(Integer.parseInt(newMarkValue));
                    request.setStudentID(studentIDWhomAssignInsertedNewExam);
                    
                    SetNewExamResponse response = examsClient.getNewInsertedExam(request);
                    
                    String resultOfInsertion = response.getResponseResult();
                    System.out.println("\n\nResponse: " + resultOfInsertion + "\n\n");
                }
            }
        }
        else {
            System.out.println("\n\nCheck the action you'd insert.\n\n");
        }
    }

}
