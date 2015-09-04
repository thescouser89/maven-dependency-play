import org.apache.commons.io.FileUtils;
import org.commonjava.cartographer.CartoDataException;
import org.commonjava.cartographer.CartographerCore;
import org.commonjava.cartographer.CartographerCoreBuilder;
import org.commonjava.cartographer.graph.MavenModelProcessor;
import org.commonjava.cartographer.graph.discover.DiscoveryConfig;
import org.commonjava.cartographer.graph.discover.DiscoveryResult;
import org.commonjava.maven.atlas.graph.rel.ProjectRelationship;
import org.commonjava.maven.atlas.graph.spi.neo4j.FileNeo4jConnectionFactory;
import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.commonjava.maven.galley.maven.model.view.MavenPomView;
import org.commonjava.maven.galley.maven.parse.MavenPomReader;
import org.commonjava.maven.galley.maven.parse.PomPeek;
import org.commonjava.maven.galley.maven.util.ArtifactPathUtils;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.SimpleLocation;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Uses galley + cartographer to read relationships from a given POM file. If necessary, uses maven central to resolve
 * parent POMs and BOMs in order to arrive at a fully inherited Maven project model. Dependencies defined in the parent
 * will be included, if present.
 *
 * NOTE: code obtained from John Casey (@jdcasey)
 * https://github.com/Commonjava/aprox-stack-examples/blob/master/cartographer/cartographer-modelproc-example/src/main/java/org/commonjava/cartographer/ex/RelationshipReader.java
 */
public class RelationshipReader {

    private CartographerCore getCartographerCoreInstance(File cacheDir) throws IOException, CartoDataException {

        return new CartographerCoreBuilder(cacheDir,
                                           new FileNeo4jConnectionFactory(null, true))
                                             .withDefaultTransports()
                                             .build();
    }

    public List<GAVModule> readRelationships(File pomRepoDir)
            throws TransferException, GalleyMavenException, URISyntaxException, CartoDataException, IOException {

        File tempDir = Files.createTempDirectory("deps").toFile();


        try {
            List<GAVModule> gavModules = new LinkedList<>();
            CartographerCore carto = getCartographerCoreInstance(tempDir);
            List<ProjectVersionRef> projectVersionRefs = setupRepositoryDirectoryFromClasspath(tempDir, findAllPomFiles(pomRepoDir), carto);

            for(ProjectVersionRef ref: projectVersionRefs) {

                MavenPomReader pomReader = carto.getGalley().getPomReader();
                MavenPomView pomView = pomReader.read(ref, getRepoLocations(tempDir));

                Location location = new SimpleLocation("file:" + tempDir.getAbsolutePath());
                URI src = new URI(location.getUri());

                DiscoveryConfig disConf = new DiscoveryConfig(src);
                disConf.setIncludeBuildSection(false);
                disConf.setIncludeManagedDependencies(false);
                disConf.setIncludeManagedPlugins(false);

                MavenModelProcessor processor = new MavenModelProcessor();
                DiscoveryResult result = processor.readRelationships(pomView, src, disConf);
                Set<ProjectRelationship<?, ?>> relationships = result.getAcceptedRelationships();

                gavModules.add(generateGAVModule(ref, relationships));
            }
            return gavModules;
        } finally {
            FileUtils.deleteDirectory(tempDir);
        }
    }


    // TODO: get the list of locations from main pom.xml
    private List<? extends Location> getRepoLocations(File cacheDir) {
        Location location = new SimpleLocation( "file:" + cacheDir.getAbsolutePath() );
        List<? extends Location> repoLocations =
                Arrays.asList(location, new SimpleLocation("central", "http://repo.maven.apache.org/maven2/"),
                        new SimpleLocation("eap", "http://maven.repository.redhat.com/techpreview/all"),
                        new SimpleLocation("sonatype", "https://oss.sonatype.org/content/repositories"));

        return repoLocations;
    }

    private List<ProjectVersionRef> setupRepositoryDirectoryFromClasspath(File dir, List<String> poms, CartographerCore carto)
            throws TransferException, IOException {

        List<ProjectVersionRef> projectVersionRefs = new LinkedList<>();
        for (String pom: poms) {
            File pomFile = new File(pom);
            PomPeek peek = new PomPeek(pomFile);
            projectVersionRefs.add(peek.getKey());

            String path = ArtifactPathUtils.formatArtifactPath(peek.getKey().asPomArtifact(),
                                                               carto.getGalley().getTypeMapper());

            File f = new File(dir, path);
            f.getParentFile().mkdirs();
            FileUtils.copyFile(pomFile, f);
        }
        return projectVersionRefs;
    }

    private List<String> findAllPomFiles(File folderDir) {

        Collection<File> pomFilePaths = FileUtils.listFiles(folderDir, new String[]{"xml"}, true);

        List<String> paths = pomFilePaths.stream()
                                    .filter(f -> f.getName().equals("pom.xml"))
                                    .map(f -> f.getAbsolutePath())
                                    .collect(Collectors.toList());

        return paths;
    }

    private GAVModule generateGAVModule(ProjectVersionRef ref, Set<ProjectRelationship<?, ?>> relationships) {
        GAVModule module = new GAVModule(new GAV(ref.getGroupId(), ref.getArtifactId(), ref.getVersionString()));

        for (ProjectRelationship<?, ?> relationship: relationships) {
            ProjectVersionRef target = relationship.getTarget();
            GAV targetGAV = new GAV(target.getGroupId(), target.getArtifactId(), target.getVersionString());
            switch(relationship.getType()) {
                case DEPENDENCY:
                    module.addDependency(targetGAV);    break;
                case BOM:
                    module.addBom(targetGAV);           break;
                case PARENT:
                    module.setParent(targetGAV);        break;
                case EXTENSION:
                    module.addExtension(targetGAV);     break;
                case PLUGIN:
                    module.addPlugin(targetGAV);        break;
                case PLUGIN_DEP:
                    module.addPluginDeps(targetGAV);    break;
            }
        }
        return module;
    }
}
