import org.apache.commons.io.FileUtils;
import org.commonjava.maven.atlas.graph.rel.ProjectRelationship;
import org.commonjava.maven.atlas.graph.rel.RelationshipType;
import org.commonjava.maven.galley.maven.parse.PomPeek;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws Exception {

        // TODO: adjust so that it points to a pom.xml
        // TODO: seems like Main.java doesn't shutdown
        // TODO: doesn't work for other central? tried on dependency-analysis and it failed
        // TODO: can't find parent oss-sonatype?
        File file = new File("/home/dcheung/projects/dependency-analysis");
        Collection<File> pomFilePaths = FileUtils.listFiles(file, new String[]{"xml"}, true);

        List<String> paths = pomFilePaths.stream().filter(f -> f.getName().equals("pom.xml"))
                .map(f -> f.getAbsolutePath())
                .collect(Collectors.toList());
        Path tempDir = Files.createTempDirectory("deps");
        RelationshipReader reader = new RelationshipReader(tempDir.toFile());
        reader.setupRepositoryDirectoryFromClasspath(tempDir.toFile(), paths.toArray(new String[0]));

        System.out.println(paths);
        PomPeek peek = new PomPeek(new File(file, "pom.xml"));
        peek.getKey();

        Set<ProjectRelationship<?, ?>> relationships = reader.readRelationships(tempDir.toFile(), peek.getKey());

        if (relationships != null) {
            relationships.forEach(System.out::println);
        }

        for (ProjectRelationship<?, ?> test: relationships) {
            System.out.println(test.getDeclaring());
            System.out.println(test.getTarget());
            System.out.println(test.getType());
        }

        System.out.println("I'm done, shutting down");
    }
}
