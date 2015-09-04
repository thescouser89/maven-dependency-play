/**
 * Created by dcheung on 04/09/15.
 */
public class GAV {

    private String groupId;
    private String artifactId;
    private String version;

    @Override
    public String toString() {
        return groupId + ":" + artifactId + ":" + version;
    }

    public GAV(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}
