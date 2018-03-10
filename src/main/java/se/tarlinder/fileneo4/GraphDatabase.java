package se.tarlinder.fileneo4;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.neo4j.driver.v1.Values.parameters;

/**
 * Abstraction over the graph database that allows emptying it and creating simple nodes and directed links.
 * Not efficient at all with session management.
 */
public class GraphDatabase {
    private final Driver neo4jDriver;

    public GraphDatabase(Driver neo4jDriver) {
        this.neo4jDriver = neo4jDriver;
    }

    public void deleteAllNodes() {
        try (Session session = neo4jDriver.session()) {
            session.writeTransaction(tx -> {
                tx.run("MATCH (n) DETACH DELETE n");
                return null;
            });
        }
    }

    public <K, V> long createNode(String label, Map<K, V> properties) {
        try (Session session = neo4jDriver.session()) {
            return session.writeTransaction(tx -> {
                StatementResult result = tx.run("CREATE (n:" + label + " " + map2props(properties) + ") RETURN id(n)");
                return result.single().get(0).asLong();
            });
        }
    }

    public void linkNodes(long parentNodeId, long childNodeId, String relationship) {
        try (Session session = neo4jDriver.session()) {
            session.writeTransaction(tx -> {
                tx.run("MATCH (p), (c) WHERE id(p) = $parentId and id(c) = $childId CREATE (c)-[r:" + relationship + "]->(p)",
                        parameters("parentId", parentNodeId, "childId", childNodeId));
                return null;
            });
        }
    }


    private <K, V> String map2props(Map<K, V> map) {
        List<String> properties = new ArrayList<>();
        map.forEach((k, v) -> properties.add(k + ":'" + v + "'"));
        return "{" + String.join(",", properties) + "}";
    }

}
