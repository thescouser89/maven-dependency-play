import java.io.File;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {

        File file = new File("/home/dcheung/projects/pnc");
        RelationshipReader reader = new RelationshipReader();

        List<GAVModule> modules = reader.readRelationships(file);

        modules.forEach(System.out::println);

        System.out.println("I'm done, shutting down");
    }
}
