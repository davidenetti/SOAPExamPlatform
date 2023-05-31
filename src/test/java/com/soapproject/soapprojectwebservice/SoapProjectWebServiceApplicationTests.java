package com.soapproject.soapprojectwebservice;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.namespacetest.ExamRegistration;

class SoapProjectWebServiceApplicationTests {

	@Test
	void getExam() {
		ExamsRepository repoTest= new ExamsRepository();
		
		String test1ExamName = "Programmazione1";
		String test1StudentID = "Utente1";
		String date = "2021-12-12";

		ExamRegistration result = repoTest.findExamRegistration(test1ExamName, test1StudentID);
		
		System.out.println("Questo è il candidate name: " + result.getCandidateName());
		assertTrue(result.getCandidateName().equals(test1StudentID));
		assertTrue(result.getExamName().equals(test1ExamName));
		assertTrue(result.getValue() == 27);
		assertTrue(result.getRegistrationDate().equals(date));
	}

	@Test
	void getTranscript() {
		ExamsRepository repoTest = new ExamsRepository();
		String test1StudentID = "utente2";

		String result = repoTest.findAllexams(test1StudentID);
		
		assertTrue(result.contains("Programmazione2, Mark: 28, Registration date:"));
	}

	@Test
	void insertNewExam() {
		ExamsRepository repoTest = new ExamsRepository();

		String result = repoTest.insertNewExam("Utente2", "Architettura1", 27);
		
		assertTrue(result.equals("Exam registration successfully registered."));

		ExamRegistration result2 = repoTest.findExamRegistration("architettura1", "utente2");
		assertTrue(result2.getCandidateName().equals("Utente2"));
		assertTrue(result2.getExamName().equals("Architettura1"));
		assertTrue(result2.getValue() == 27);
	}
}