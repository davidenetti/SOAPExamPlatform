package com.soapproject.soapprojectwebservice;

import com.namespacetest.ExamRegistration;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertOneResult;

import org.bson.Document;
import org.bson.conversions.Bson;

import static com.mongodb.client.model.Filters.*;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class ExamsRepository {
    private Map<String, ExamRegistration> exams = new HashMap<>();

	private MongoCollection<Document> findInDB() {

        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("ExamsDB");
        MongoCollection<Document> collection = database.getCollection("exams");

        return collection;
    }


    public ExamRegistration findExamRegistration (String examName, String studentID) {
        Assert.notNull(examName, "The examName musn't be null");
        Assert.notNull(studentID, "The studentID musn't be null");

        String keyBuilding = examName.toLowerCase() + studentID.toLowerCase();
        System.out.println("This is the builded keyBuilding: " + keyBuilding);

        MongoCollection<Document> collection = findInDB();
        Bson regexComparison = regex("_id", ".*" + keyBuilding + ".*");
        FindIterable<Document> cursor = collection.find(regexComparison);
        Document examFromDB = cursor.first();
        
        ExamRegistration retrievedExam = new ExamRegistration();
        retrievedExam.setCandidateName(examFromDB.get("candidateName").toString());
        retrievedExam.setExamName(examFromDB.get("examName").toString());
        retrievedExam.setRegistrationDate(examFromDB.get("registrationDate").toString());
        retrievedExam.setValue(Integer.parseInt(examFromDB.get("value").toString()));

        return retrievedExam;
    }

    public String findAllexams (String studentID) {
        Assert.notNull(studentID, "The studentID musn't be null");

        String resultingTranscript = "";
        String keyBuilding = studentID.toLowerCase();

        MongoCollection<Document> collection = findInDB();
        Bson regexComparison = regex("_id", ".*" + keyBuilding + ".*");
        FindIterable<Document> cursor = collection.find(regexComparison);
        Document nextElement = null;
        try (final MongoCursor<Document> cursorIterator = cursor.cursor()) { 
            while(cursorIterator.hasNext()){
                nextElement = cursorIterator.next();
                String value = (String) nextElement.get("value");
                String examName = (String) nextElement.get("examName");
                String registrationDate = (String) nextElement.get("registrationDate");
                resultingTranscript +="\nExam name: " + examName + ", Mark: " + value + ", Registration date: " + registrationDate;
            }
        }

        return resultingTranscript; //The value is returned as a string to match the WSDL and the type string in XML
    }

    public String insertNewExam(String studentID, String newExam, Integer value) {
        Assert.notNull(studentID, "The studentID musn't be null");
        Assert.notNull(newExam, "You must specify an exam to insert");
        Assert.notNull(value, "You must specify a value for the exam to insert");

        MongoCollection<Document> collection = findInDB();
        String keyBuilding = newExam.toLowerCase() + studentID.toLowerCase();
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
        
        String valueToString = String.valueOf(value);

        try {
            InsertOneResult result = collection.insertOne(
                new Document()
                    .append("_id", keyBuilding)
                    .append("value", valueToString)
                    .append("candidateName", studentID)
                    .append("examName", newExam)
                    .append("registrationDate", timeStamp)
            );
        } catch (MongoException me) {
            System.err.println("Unable to insert due to an error: " + me);
        }
        return "Exam registration successfully registered.";
    }
}