import java.util.LinkedList;
import java.util.List;

public class GAVModule {
    private GAV gav;

    public List<GAV> dependencies = new LinkedList<>();
    public List<GAV> boms = new LinkedList<>();
    public List<GAV> extensions = new LinkedList<>();
    public GAV parent;
    public List<GAV> plugins = new LinkedList<>();
    public List<GAV> pluginDeps = new LinkedList<>();

    @Override
    public String toString() {
        String toReturn = getGAV().toString();
        toReturn += "\n-----------------------------------------------\n";
        toReturn += "Dependencies: " + dependencies.toString();
        toReturn += "\nParent: " + parent;
        toReturn += "\nBOMs: " + boms;
        toReturn += "\n===============================================\n";
        return toReturn;
    }

    public GAV getGAV() {
        return gav;
    }
    public GAVModule(GAV gav) {
        this.gav = gav;
    }

    public void addDependency(GAV dep) {
        dependencies.add(dep);
    }

    public void addBom(GAV bom) {
        boms.add(bom);
    }

    public void addExtension(GAV extension) {
        extensions.add(extension);
    }

    public void setParent(GAV parent) {
        this.parent = parent;
    }

    public void addPlugin(GAV plugin) {
        plugins.add(plugin);
    }

    public void addPluginDeps(GAV pluginDep) {
        pluginDeps.add(pluginDep);
    }
}
