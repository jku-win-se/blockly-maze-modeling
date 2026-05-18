import at.ac.tuwien.big.moea.SearchExperiment;
import at.ac.tuwien.big.momot.TransformationSearchOrchestration;
import at.ac.tuwien.big.momot.problem.solution.TransformationSolution;
import at.ac.tuwien.big.moea.search.algorithm.EvolutionaryAlgorithmFactory;
import at.ac.tuwien.big.moea.search.algorithm.LocalSearchAlgorithmFactory;
import org.moeaframework.util.progress.ProgressListener;
import org.moeaframework.util.progress.ProgressEvent;

/**
 * Mediator class that extends the generated 'blocky' runner to inject 
 * dynamic parameters from system properties and support interruption.
 */
public class blocky_custom extends blocky {

    private int getOverriddenPopulationSize() {
        return Integer.getInteger("blocky.populationSize", this.populationSize);
    }

    private int getOverriddenMaxEvaluations() {
        return Integer.getInteger("blocky.maxEvaluations", this.maxEvaluations);
    }

    private int getOverriddenNrRuns() {
        return Integer.getInteger("blocky.nrRuns", this.nrRuns);
    }

    @Override
    protected TransformationSearchOrchestration createOrchestration(String initialGraph, int solutionLength) {
        TransformationSearchOrchestration orchestration = super.createOrchestration(initialGraph, solutionLength);
        
        // Re-create the algorithm factory with the overridden population size
        int popSize = getOverriddenPopulationSize();
        EvolutionaryAlgorithmFactory<TransformationSolution> moea = orchestration.createEvolutionaryAlgorithmFactory(popSize);
        LocalSearchAlgorithmFactory<TransformationSolution> local = orchestration.createLocalSearchAlgorithmFactory();
        
        // Clear and re-register algorithms to use the new factory
        orchestration.getAlgorithms().clear();
        orchestration.addAlgorithm("NSGA_II", _createRegisteredAlgorithm_0(orchestration, moea, local));
        
        return orchestration;
    }

    @Override
    protected SearchExperiment<TransformationSolution> createExperiment(TransformationSearchOrchestration orchestration) {
        SearchExperiment<TransformationSolution> experiment = new SearchExperiment<>(orchestration, getOverriddenMaxEvaluations());
        experiment.setNumberOfRuns(getOverriddenNrRuns());
        experiment.addProgressListener(_createListener_0());
        
        // Force-stop the experiment if the thread is interrupted
        experiment.addProgressListener(new ProgressListener() {
            @Override
            public void progressUpdate(ProgressEvent event) {
                if (Thread.currentThread().isInterrupted()) {
                    throw new RuntimeException("MoMoT search interrupted (user stop or level change)");
                }
            }
        });
        
        return experiment;
    }

    @Override
    public void printSearchInfo(TransformationSearchOrchestration orchestration) {
        System.out.println("-------------------------------------------------------");
        System.out.println("Search (Customized via blocky_custom)");
        System.out.println("-------------------------------------------------------");
        System.out.println("InputModel:      " + INITIAL_MODEL);
        System.out.println("Objectives:      " + orchestration.getFitnessFunction().getObjectiveNames());
        System.out.println("SolutionLength:  " + orchestration.getSolutionLength());
        System.out.println("PopulationSize:  " + getOverriddenPopulationSize() + " (overridden)");
        System.out.println("MaxEvaluations:  " + getOverriddenMaxEvaluations() + " (overridden)");
        System.out.println("AlgorithmRuns:   " + getOverriddenNrRuns() + " (overridden)");
        System.out.println("Iterations:      " + getOverriddenMaxEvaluations() / getOverriddenPopulationSize());
        System.out.println("---------------------------");
    }
}
