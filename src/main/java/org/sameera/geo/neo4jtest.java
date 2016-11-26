package org.sameera.geo;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;

/**
 * Created by sameera on 10/29/16.
 */
public class neo4jtest {

    //First the Neo4j DB path is specified
    private static final String Neo4j_path = "/home/sameera/installations/neo4j-community-3.0.6/data/databases/test.db";

    //Creating Nodes, RelationShip, GraphDBService
    Node first;
    Node second;
    Relationship relation;
    GraphDatabaseService graphDataService;

    //List of RelationShipts between the Nodes
    private static enum RelTypes implements RelationshipType
    {
        KNOWS
    }

    public static void main(String[] args) {

//Creating an Instance to make calls for the functions we have written below.
        neo4jtest hello = new neo4jtest();

//Function calls
        hello.createDatabase();
//        hello.removeData();
        hello.shutdown();

    }
    //Always create the Database
    void createDatabase() {

//Step : 1 == > Create GraphDatabaseService
        graphDataService = new GraphDatabaseFactory().newEmbeddedDatabase(new File(Neo4j_path));

//Step : 2 == > Begin Transaction
        Transaction transaction = graphDataService.beginTx();

        try {
//Step : 3 == > Creation of Node and Set the Properties
//createNode(), setProperty are the method

            first = graphDataService.createNode();
            first.setProperty("name","Jackson Hewitt");

            second = graphDataService.createNode();
            second.setProperty("name","H&R");

//Step : 4 ==>; Create Relationship

            relation = first.createRelationshipTo(first,RelTypes.KNOWS);
            relation.setProperty("relationship-type","knows");

//Printing out the relationship between first and second nodes
//System.out.println(first.getProperty("name").toString());
//System.out.println(relation.getProperty("relationship-type").toString());
//System.out.println(second.getProperty("name").toString());
            System.out.println(first.getProperty("name").toString()+"–>"+relation.getProperty("relationship-type").toString() + " — >" + second.getProperty("name").toString());

//Step : 5 ==> ; Success the transaction
            transaction.success();
        }
        finally {
//Step 6: ==>; Finish Transaction
            transaction.close();
        }
    }

    //Once the database is created, the data has to be removed
    void removeData() {
//Step 1 : Again create the transaction
        Transaction transaction = graphDataService.beginTx();

        try {
//Delete the Outgoing RelationShip first
            first.getSingleRelationship(RelTypes.KNOWS, Direction.OUTGOING).delete();
            System.out.println("Nodes are Removed Successfully");
            first.delete();
            second.delete();
            transaction.success();
        } finally {
//Finish the Transaction
            transaction.close();
        }
    }

    //The database instance has also be shutdown once created
    void shutdown() {
//Shutdown the graphDataService
        graphDataService.shutdown();
        System.out.println("Neo4j DB is shutdown successfully");
    }
}
enum TutorialRelationships implements RelationshipType {
    JVM_LANGIAGES,NON_JVM_LANGIAGES;
}
enum Tutorials implements Label {
    JAVA,SCALA,SQL,NEO4J;
}
