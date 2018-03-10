package se.tarlinder.fileneo4;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Main entry point. Note the hard coding of the directory and database credentials.
 */
public class Main {

    public static void main(String... args) throws IOException {

        try (Driver neo4jDriver = org.neo4j.driver.v1.GraphDatabase.driver("bolt://localhost:7687",
                AuthTokens.basic("neo4j", "test"))) {
            final Path root = Paths.get("d:\\src");
            final GraphDatabase database = new GraphDatabase(neo4jDriver);
            database.deleteAllNodes();
            Files.walkFileTree(root, new VisitingNodeCreator(database));
        }
    }
}