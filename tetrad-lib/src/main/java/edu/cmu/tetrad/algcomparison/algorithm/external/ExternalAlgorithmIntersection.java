package edu.cmu.tetrad.algcomparison.algorithm.external;

import edu.cmu.tetrad.algcomparison.algorithm.ExternalAlgorithm;
import edu.cmu.tetrad.algcomparison.simulation.Simulation;
import edu.cmu.tetrad.data.DataModel;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DataType;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.util.Parameters;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * An API to allow results from external algorithms to be included in a report through the algrorithm
 * comparison tool. This one is for matrix generated by PC in pcalg. See below. This script can generate
 * the files in R.
 * <p>
 * library("MASS");
 * library("pcalg");
 * <p>
 * path<-"/Users/user/tetrad/comparison-final";
 * simulation<-1;
 * <p>
 * subdir<-"pc.solve.confl.TRUE";
 * dir.create(paste(path, "/save/", simulation, "/", subdir, sep=""));
 * <p>
 * for (i in 1:10) {
 * data<-read.table(paste(path, "/save/", simulation, "/data/data.", i, ".txt", sep=""), header=TRUE)
 * n<-nrow(data)
 * C<-cor(data)
 * v<-names(data)
 * suffStat<-list(C = C, n=n)
 * pc.fit<-pc(suffStat=suffStat, indepTest=gaussCItest, alpha=0.001, labels=v,
 * solve.conf=TRUE)
 * A<-as(pc.fit, "amat")
 * name<-paste(path, "/save/", simulation, "/", subdir, "/graph.", i, ".txt", sep="")
 * print(name)
 * write.matrix(A, file=name, sep="\t")
 * }
 *
 * @author jdramsey
 */
public class ExternalAlgorithmIntersection implements ExternalAlgorithm {
    static final long serialVersionUID = 23L;
    private final ExternalAlgorithm[] algorithms;
    private String shortDescription = null;
    private List<String> usedParameters = new ArrayList<>();
    private Simulation simulation;
    private String path;
    private int simIndex;
    private long elapsed = -99;

    public ExternalAlgorithmIntersection(String shortDescription, ExternalAlgorithm... algorithms) {
        this.algorithms = algorithms;
        this.shortDescription = shortDescription;
    }

    @Override
    /**
     * Reads in the relevant graph from the file (see above) and returns it.
     */
    public Graph search(DataModel dataSet, Parameters parameters) {
        this.elapsed = 0;

        for (ExternalAlgorithm algorithm : algorithms) {
            algorithm.setPath(this.path);
            algorithm.setSimIndex(this.simIndex);
            algorithm.setSimulation(this.simulation);
            elapsed += algorithm.getElapsedTime((DataSet) dataSet, parameters);
        }

        Graph graph0 = algorithms[0].search(dataSet, parameters);
        Set<Edge> edges = graph0.getEdges();

        for (int i = 1; i < algorithms.length; i++) {
            edges.retainAll(algorithms[i].search(dataSet, parameters).getEdges());
        }

        EdgeListGraph intersection = new EdgeListGraph(graph0.getNodes());

        for (Edge edge : edges) {
            intersection.addEdge(edge);
        }

        return intersection;
    }

    @Override
    /**
     * Returns the pattern of the supplied DAG.
     */
    public Graph getComparisonGraph(Graph graph) {
        return algorithms[0].getComparisonGraph(graph);
    }

    public String getDescription() {
        return shortDescription;
    }

    @Override
    public List<String> getParameters() {
        return usedParameters;
    }

    public int getNumDataModels() {
        return simulation.getNumDataModels();
    }

    @Override
    public DataType getDataType() {
        return DataType.Continuous;
    }

    public void setSimulation(Simulation simulation) {
        this.simulation = simulation;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setSimIndex(int simIndex) {
        this.simIndex = simIndex;
    }

    @Override
    public Simulation getSimulation() {
        return simulation;
    }

    @Override
    public long getElapsedTime(DataModel dataSet, Parameters parameters) {
        return this.elapsed;
    }

}
