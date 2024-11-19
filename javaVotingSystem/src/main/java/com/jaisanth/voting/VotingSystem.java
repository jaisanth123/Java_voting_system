package com.jaisanth.voting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import org.bson.Document;

import com.jaisanth.database.DatabaseConnection;
import com.jaisanth.models.Candidate;
import com.mongodb.client.MongoCollection;

public class VotingSystem {

    private static List<Candidate> candidates = new ArrayList<>();
    private static final String PASSWORD = "Reveal_Password";   //! :  to set a password for vote reveal process 
    private static MongoCollection<Document> collection;
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String collectionName = "Votes_" + System.currentTimeMillis();  //todo : mongodb collection creation
        collection = DatabaseConnection.getDatabase().getCollection(collectionName);


        printBoxed("Welcome to the Online Voting System!");
        printBoxed("Enter the number of candidates: ");
        int numCandidates = Integer.parseInt(scanner.nextLine());
        for (int i = 1; i <= numCandidates; i++) {
            printBoxed("Enter name of candidate " + i + ": ");
            String name = scanner.nextLine();                      //! candidates name
            candidates.add(new Candidate(name));  }
         printBoxed("Voting system is ready for voting!");
        while (true) {                                                                                        //! main loop 
            printBoxed("Enter your vote (candidate number) or type 'exit' to finish voting: ");
            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("exit")) {                                //! exit check 
                break;  }
            if (isNumeric(input)) {
                int vote = Integer.parseInt(input);
                if (vote > 0 && vote <= candidates.size()) {
                    Candidate selectedCandidate = candidates.get(vote - 1);
                    selectedCandidate.incrementVotes();
                    printBoxed("You have voted for " + selectedCandidate.getName() + "!");
                                                                                             //todo class calling              //! db storing 
                    storeVoteInDatabase(selectedCandidate.getName(), "Voter_" + UUID.randomUUID()); }       //! uuid voter uniq id 
                 else {
                    printBoxed("Invalid candidate number. Try again."); }
            } else {
                printBoxed("Invalid input. Please enter a number or 'exit'.");}}
        publishResults(scanner);        }                                                                //! publish results class calling



        
    //hack  <----------------------  storing vote in database  --------------------->

    private static void storeVoteInDatabase(String candidateName, String voterId) {
        Document voteDocument = new Document("candidate", candidateName).append("voterId", voterId);           //! voter id ==> who has voted       
        collection.insertOne(voteDocument);} //todo inserting vote in database
 



 //hack  <----------------------  str consist num checker  --------------------->
    public static boolean isNumeric(String str) {
        return str.matches("\\d+");   }                                           //! check str cointains num only
    

//hack  <----------------------  puslish result and password checker method  --------------------->
    private static void publishResults(Scanner scanner) {                         //! publish results class 
        printBoxed("Do you want to publish the results? (yes/no): ");
        String response = scanner.nextLine();
        if (response.equalsIgnoreCase("yes")) {
            while (true) {
                printBoxed("Enter password to reveal voting results: ");
                String passwordInput = scanner.nextLine();
                if (passwordInput.equals(PASSWORD)) {
                    displayVotingResults();                 //! display result class calling
                    break;  } 
                   else {
                    printBoxed("Wrong password! Choose an option:");
                     printBoxed("1. Retry");
                     printBoxed("2. Restart session");
                    printBoxed("3. Exit application");
                    String choice = scanner.nextLine();
                    switch (choice) {
                        case "1": 
                            continue;   //! retry 
                        case "2":
                            main(new String[0]);  
                            return;              //! restart voting 
                        case "3":
                            System.exit(0);                  //! exit
                            return;  
                        default:
                            printBoxed("Invalid choice. Exiting application.");
                            System.exit(0);
                    }}}}
           else {
            printBoxed("Results will not be published.");
             askForNewSession(scanner);
        }}






    //hack  <----------------------  display results method   --------------------->
    //todo reterival 
    public static void displayVotingResults() { //! display results class 
        System.out.println("\n-----------------");
         System.out.println("| Voting Results: |");
         System.out.println("-----------------");      //todo java aggregation pipeline 
        List<Document> results = collection.aggregate(Arrays.asList(new Document("$group", new Document("_id", "$candidate").append("totalVotes", new Document("$sum", 1))))).into(new ArrayList<>());
        if (results.isEmpty()) {
            System.out.println("\nNo votes were cast.");}
            else {
            String winner = null;
            int maxVotes = 0;
            boolean isDraw = false;
            for (Document result : results) {
                String candidateName = result.getString("_id");
                int totalVotes = result.getInteger("totalVotes");
                System.out.println("| " + candidateName + ": " + totalVotes + " votes |");
                if (totalVotes > maxVotes) {
                     maxVotes = totalVotes;
                     winner = candidateName;
                     isDraw = false;} //!Reset draw status if a new leader is found
                 else if (totalVotes == maxVotes) {
                        isDraw = true; }}//!Set draw status if there’s a tie with the current max
            if (isDraw) {
                  System.out.println("\nIt's a draw between the top candidates!");}
             else {
                  System.out.println("\nWinner: " + winner + " with " + maxVotes + " votes!");}}
        System.out.println("-----------------");
        askForNewSession(new Scanner(System.in));} //! another session caller

    

    //hack  <----------------------  ask for new session method  --------------------->
    private static void askForNewSession(Scanner scanner) {
        printBoxed("\nDo you want to start a new voting session? (yes/no): ");
        String newSessionResponse = scanner.nextLine();
        if (newSessionResponse.equalsIgnoreCase("yes")) {
            candidates.clear();                                              //! clear previos data and restart
             main(new String[0]);  
        } else {
            printBoxed("Thank you for participating! Exiting application.");
            System.exit(0);   }}                                        //! exit application


            
//hack  <----------------------  printboxed method  --------------------->
    private static void printBoxed(String message) {
          int length = message.length();
        System.out.println("-------------------------------");
          System.out.println("| " + message + " ".repeat(Math.max(0, length - message.length() - 2)) + "|");
        System.out.println("-------------------------------");
    }
}