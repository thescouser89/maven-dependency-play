import org.commonjava.maven.atlas.graph.rel.ProjectRelationship;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws Exception {

        // TODO: adjust so that it points to a pom.xml
        // TODO: seems like Main.java doesn't shutdown
        // TODO: doesn't work for other central? tried on dependency-analysis and it failed
        // TODO: can't find parent oss-sonatype?
        File file = new File("/home/dcheung/projects/maven-scm-play/pom.xml");
        Path tempDir = Files.createTempDirectory("deps");
        RelationshipReader reader = new RelationshipReader(tempDir.toFile());
        Set<ProjectRelationship<?, ?>> relationships = reader.readRelationships(file);

        if (relationships != null) {
            relationships.forEach(System.out::println);
        }

        System.out.println("I'm done, shutting down");
    }
}
