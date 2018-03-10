package se.tarlinder.fileneo4;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * A file visitor that creates nodes and links in a graph database.
 */
public class VisitingNodeCreator extends SimpleFileVisitor<Path> {

    private final GraphDatabase database;
    private Stack<Long> nodeIds = new Stack<>();

    public VisitingNodeCreator(GraphDatabase database) {
        this.database = database;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        Map<String, String> properties = new HashMap<>();
        properties.put("name", dir.toFile().getName());
        final long newNodeId = database.createNode("DIRECTORY", properties);

        if (!nodeIds.empty()) {
            database.linkNodes(nodeIds.peek(), newNodeId, "SUBDIR_OF");
        }
        nodeIds.push(newNodeId);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
        if (attrs.isRegularFile()) {
            Map<String, Object> properties = new HashMap<>();
            properties.put("name", path.toFile().getName());
            properties.put("size", attrs.size());
            final long newNodeId = database.createNode("FILE", properties);
            database.linkNodes(nodeIds.peek(), newNodeId, "IN");
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        nodeIds.pop();
        return FileVisitResult.CONTINUE;
    }
}
