package edu.cmu.tetrad.algcomparison.simulation;

import edu.cmu.tetrad.algcomparison.graph.RandomGraph;
import edu.cmu.tetrad.algcomparison.utils.HasKnowledge;
import edu.cmu.tetrad.util.Parameters;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DataType;
import edu.cmu.tetrad.data.IKnowledge;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphUtils;
import edu.cmu.tetrad.search.TimeSeriesUtils;
import edu.cmu.tetrad.sem.LargeSemSimulator;
import edu.cmu.tetrad.util.TetradMatrix;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jdramsey
 */
public class TimeSeriesSemSimulation implements Simulation, HasKnowledge {
    static final long serialVersionUID = 23L;
    private final RandomGraph randomGraph;
    private Graph graph;
    private List<DataSet> dataSets;
    private IKnowledge knowledge;

    public TimeSeriesSemSimulation(RandomGraph randomGraph) {
        if (randomGraph == null) throw new NullPointerException();
        this.randomGraph = randomGraph;
    }

    @Override
    public void createData(Parameters parameters) {
        dataSets = new ArrayList<>();

        this.graph = randomGraph.createGraph(parameters);
        this.graph = TimeSeriesUtils.GraphToLagGraph(graph);
        this.knowledge = TimeSeriesUtils.getKnowledge(graph);

        for (int i = 0; i < parameters.getInt("numRuns"); i++) {
            LargeSemSimulator sim = new LargeSemSimulator(graph);
            if(parameters.getDouble("coefHigh") > 0.80) {
                System.out.println("Coefficients have been set (perhaps by default) too " +
                        "high for stationary time series.");
                System.out.println("Setting coefficient range to [0.20,0.60].");
                sim.setCoefRange(0.20, 0.60);
            } else sim.setCoefRange(parameters.getDouble("coefLow"), parameters.getDouble("coefHigh"));
            boolean isStableTetradMatrix;
            int attempt = 1;
            int tierSize = parameters.getInt("numMeasures") + parameters.getInt("numLatents"); //params.getNumVars();
            int[] sub = new int[tierSize];
            int[] sub2 = new int[tierSize];
            for(int j = 0; j < tierSize; j++){
                sub[j] = j;
                sub2[j] = tierSize + j;
            }
            DataSet dataSet;
            do {
                dataSet = sim.simulateDataAcyclic(parameters.getInt("sampleSize")); //params.getSampleSize());

                TetradMatrix coefMat = new TetradMatrix(sim.getCoefficientMatrix());
                TetradMatrix B = coefMat.getSelection(sub, sub);
                TetradMatrix Gamma1 = coefMat.getSelection(sub2, sub);
                TetradMatrix Gamma0 = TetradMatrix.identity(tierSize).minus(B);
                TetradMatrix A1 = Gamma0.inverse().times(Gamma1);

                isStableTetradMatrix = TimeSeriesUtils.allEigenvaluesAreSmallerThanOneInModulus(A1);
                attempt++;
            } while ((!isStableTetradMatrix) && attempt<=5);
            if (!isStableTetradMatrix){
                System.out.println("%%%%%%%%%% WARNING %%%%%%%% not a stable coefficient matrix, forcing coefs to [0.15,0.3]");
                System.out.println("Made " + (attempt-1) + " attempts to get stable matrix.");
                sim.setCoefRange(0.15, 0.3);
                dataSet = sim.simulateDataAcyclic(parameters.getInt("sampleSize"));//params.getSampleSize());
            } //else System.out.println("Coefficient matrix is stable.");

            dataSet.setName("" + (i + 1));
            dataSets.add(dataSet);
        }
    }

    @Override
    public DataSet getDataSet(int index) {
        return dataSets.get(index);
    }

    @Override
    public Graph getTrueGraph() {
        return graph;
    }

    @Override
    public String getDescription() {
        return "Linear, Gaussian Dynamic SEM (1-lag SVAR) simulation";
    }

    @Override
    public List<String> getParameters() {
        List<String> parameters = new ArrayList<>();
        parameters.add("numMeasures");
        parameters.add("numLatents");
        parameters.add("avgDegree");
        parameters.add("maxDegree");
        parameters.add("maxIndegree");
        parameters.add("maxOutdegree");
        parameters.add("numRuns");
        parameters.add("sampleSize");
        return parameters;
    }

    @Override
    public int getNumDataSets() {
        return dataSets.size();
    }

    @Override
    public DataType getDataType() {
        return DataType.Continuous;
    }

    @Override
    public IKnowledge getKnowledge() {
        return knowledge;
    }

    @Override
    public void setKnowledge(IKnowledge knowledge) {
        this.knowledge = knowledge;
    }
}
