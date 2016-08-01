package edu.cmu.tetradapp.model;

import edu.cmu.tetrad.data.DataModel;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.search.ScoredGraph;
import edu.cmu.tetrad.algcomparison.utils.Parameters;

import java.util.List;

/**
 * Created by jdramsey on 2/22/16.
 */
public interface IFgsRunner {
    FgsRunner.Type getType();

    List<ScoredGraph> getTopGraphs();

    Parameters getParams();

    Graph getSourceGraph();

    DataModel getDataModel();
}
