///////////////////////////////////////////////////////////////////////////////
// For information as to what this class does, see the Javadoc, below.       //
// Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003, 2004, 2005, 2006,       //
// 2007, 2008, 2009, 2010, 2014, 2015 by Peter Spirtes, Richard Scheines, Joseph   //
// Ramsey, and Clark Glymour.                                                //
//                                                                           //
// This program is free software; you can redistribute it and/or modify      //
// it under the terms of the GNU General Public License as published by      //
// the Free Software Foundation; either version 2 of the License, or         //
// (at your option) any later version.                                       //
//                                                                           //
// This program is distributed in the hope that it will be useful,           //
// but WITHOUT ANY WARRANTY; without even the implied warranty of            //
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the             //
// GNU General Public License for more details.                              //
//                                                                           //
// You should have received a copy of the GNU General Public License         //
// along with this program; if not, write to the Free Software               //
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA //
///////////////////////////////////////////////////////////////////////////////

package edu.cmu.tetradapp.model;

import edu.cmu.tetrad.data.IKnowledge;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.search.*;
import edu.cmu.tetrad.algcomparison.utils.Parameters;
import edu.cmu.tetrad.util.TetradSerializableUtils;

import java.util.*;

/**
 * Extends AbstractAlgorithmRunner to produce a wrapper for the PC algorithm.
 *
 * @author Joseph Ramsey
 */
public class CpcRunner extends AbstractAlgorithmRunner
        implements IndTestProducer, GraphSource {
    static final long serialVersionUID = 23L;
    private Graph trueGraph;
//    private CPC cpc = null;

    Set<Edge> pcAdjacent;
    Set<Edge> pcNonadjacent;
    List<Node> pcNodes;

    //============================CONSTRUCTORS============================//

    /**
     * Constructs a wrapper for the given DataWrapper. The DataWrapper must
     * contain a DataSet that is either a DataSet or a DataSet or a DataList
     * containing either a DataSet or a DataSet as its selected model.
     */
    public CpcRunner(DataWrapper dataWrapper, Parameters params) {
        super(dataWrapper, params, null);
    }

    public CpcRunner(DataWrapper dataWrapper, Parameters params, KnowledgeBoxModel knowledgeBoxModel) {
        super(dataWrapper, params, knowledgeBoxModel);
    }
    
    /**
     * Constucts a wrapper for the given EdgeListGraph.
     */
    public CpcRunner(Graph graph, Parameters params) {
        super(graph, params);
    }
    
    /**
     * Constucts a wrapper for the given EdgeListGraph.
     */
    public CpcRunner(Graph graph, Parameters params, KnowledgeBoxModel knowledgeBoxModel) {
        super(graph, params, knowledgeBoxModel);
    }

    /**
     * Constucts a wrapper for the given EdgeListGraph.
     */
    public CpcRunner(GraphWrapper graphWrapper, Parameters params) {
        super(graphWrapper.getGraph(), params);
    }
    
    /**
     * Constucts a wrapper for the given EdgeListGraph.
     */
    public CpcRunner(GraphWrapper graphWrapper, Parameters params, KnowledgeBoxModel knowledgeBoxModel) {
        super(graphWrapper.getGraph(), params, knowledgeBoxModel);
    }
    
    /**
     * Constucts a wrapper for the given EdgeListGraph.
     */
    public CpcRunner(GraphSource graphWrapper, Parameters params, KnowledgeBoxModel knowledgeBoxModel) {
        super(graphWrapper.getGraph(), params, knowledgeBoxModel);
    }

    
    /**
     * Constucts a wrapper for the given EdgeListGraph.
     */
    public CpcRunner(GraphSource graphWrapper, Parameters params) {
        super(graphWrapper.getGraph(), params);
    }
    
    public CpcRunner(DagWrapper dagWrapper, Parameters params) {
        super(dagWrapper.getDag(), params);
    }

    public CpcRunner(DagWrapper dagWrapper, Parameters params, KnowledgeBoxModel knowledgeBoxModel) {
        super(dagWrapper.getDag(), params, knowledgeBoxModel);
    }
    
    public CpcRunner(SemGraphWrapper dagWrapper, Parameters params) {
        super(dagWrapper.getGraph(), params);
    }

    public CpcRunner(SemGraphWrapper dagWrapper, Parameters params, KnowledgeBoxModel knowledgeBoxModel) {
        super(dagWrapper.getGraph(), params, knowledgeBoxModel);
    }
    
    public CpcRunner(DataWrapper dataWrapper, GraphWrapper graphWrapper, Parameters params) {
        super(dataWrapper, params, null);
        this.trueGraph = graphWrapper.getGraph();
    }
    
    public CpcRunner(DataWrapper dataWrapper, GraphWrapper graphWrapper, Parameters params, KnowledgeBoxModel knowledgeBoxModel) {
        super(dataWrapper, params, knowledgeBoxModel);
        this.trueGraph = graphWrapper.getGraph();
    }

    public CpcRunner(IndependenceFactsModel model, Parameters params) {
        super(model, params, null);
    }

    public CpcRunner(IndependenceFactsModel model, Parameters params, KnowledgeBoxModel knowledgeBoxModel) {
        super(model, params, knowledgeBoxModel);
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see TetradSerializableUtils
     */
    public static CpcRunner serializableInstance() {
        return new CpcRunner(Dag.serializableInstance(), new Parameters());
    }

    //===================PUBLIC METHODS OVERRIDING ABSTRACT================//

    public void execute() {
        IKnowledge knowledge = getParams().getKnowledge();

        if (trueGraph != null) {
            CpcOrienter orienter = new CpcOrienter(getIndependenceTest(), knowledge);

            Graph graph = GraphUtils.undirectedGraph(trueGraph);
            orienter.orient(graph);

            if (getSourceGraph() != null) {
                GraphUtils.arrangeBySourceGraph(graph, getSourceGraph());
            }
            else if (knowledge.isDefaultToKnowledgeLayout()) {
                SearchGraphUtils.arrangeByKnowledgeTiers(graph, knowledge);
            }
            else {
                GraphUtils.circleLayout(graph, 200, 200, 150);
            }

            setResultGraph(graph);

        }
        else {
            Cpc cpc = new Cpc(getIndependenceTest());
            cpc.setKnowledge(knowledge);
            cpc.setAggressivelyPreventCycles(this.isAggressivelyPreventCycles());
            cpc.setDepth(getParams().getDepth());
            Graph graph = cpc.search();

            if (getSourceGraph() != null) {
                GraphUtils.arrangeBySourceGraph(graph, getSourceGraph());
            }
            else if (knowledge.isDefaultToKnowledgeLayout()) {
                SearchGraphUtils.arrangeByKnowledgeTiers(graph, knowledge);
            }
            else {
                GraphUtils.circleLayout(graph, 200, 200, 150);
            }

            setResultGraph(graph);
            setCpcFields(cpc);
        }


    }

    public IndependenceTest getIndependenceTest() {
        Object dataModel = getDataModel();

        if (dataModel == null) {
            dataModel = getSourceGraph();
        }

        IndTestType testType = (getParams()).getIndTestType();
        return new IndTestChooser().getTest(dataModel, getParams(), testType);
    }

    public Graph getGraph() {
        return getResultGraph();
    }

    /**
     * @return the names of the triple classifications. Coordinates with
     */
    public List<String> getTriplesClassificationTypes() {
        List<String> names = new ArrayList<>();
        names.add("Ambiguous Triples");
        return names;
    }

    /**
     * @return the list of triples corresponding to <code>getTripleClassificationNames</code>.
     */
    public List<List<Triple>> getTriplesLists(Node node) {
        List<List<Triple>> triplesList = new ArrayList<>();
        Graph graph = getGraph();
        triplesList.add(GraphUtils.getAmbiguousTriplesFromGraph(node, graph));
        return triplesList;
    }

    public Set<Edge> getAdj() {
        return new HashSet<>(pcAdjacent);
    }

    public Set<Edge> getNonAdj() {
        return new HashSet<>(pcNonadjacent);
    }

    public boolean supportsKnowledge() {
        return true;
    }


    public ImpliedOrientation getMeekRules() {
        MeekRules meekRules = new MeekRules();
        meekRules.setAggressivelyPreventCycles(this.isAggressivelyPreventCycles());
        meekRules.setKnowledge(getParams().getKnowledge());
        return meekRules;
    }

    @Override
    public String getAlgorithmName() {
        return "CPC";
    }

    @Override
    public Map<String, String> getParamSettings() {
        super.getParamSettings();
        paramSettings.put("Test", getIndependenceTest().toString());
        return paramSettings;
    }

    //========================== Private Methods ===============================//

    private boolean isAggressivelyPreventCycles() {
        return getParams().isAggressivelyPreventCycles();
    }

    private void setCpcFields(Cpc cpc) {
        pcAdjacent = cpc.getAdjacencies();
        pcNonadjacent = cpc.getNonadjacencies();
        pcNodes = getGraph().getNodes();
    }
}



