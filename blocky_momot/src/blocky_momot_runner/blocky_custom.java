package blocky_momot_runner;

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
        
        // BRIDGE ClassLoaders by registering the local dynamic package version
        try {
            org.eclipse.emf.ecore.EObject root = at.ac.tuwien.big.momot.util.MomotUtil.getRoot(orchestration.getProblemGraph());
            if (root != null && root.eResource() != null) {
                org.eclipse.emf.ecore.resource.ResourceSet rs = root.eResource().getResourceSet();
                Class<?> pkgClass = Class.forName("blocky.BlockyPackage", true, this.getClass().getClassLoader());
                org.eclipse.emf.ecore.EPackage localPkg = (org.eclipse.emf.ecore.EPackage) pkgClass.getField("eINSTANCE").get(null);
                rs.getPackageRegistry().put(localPkg.getNsURI(), localPkg);
                rs.getPackageRegistry().put(localPkg.getNsURI() + "#", localPkg);
            }
        } catch (Throwable ignored) {}

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
    public void performSearch(String initialGraph, int solutionLength) {
        System.out.println("[MoMoT] Starting performSearch override in blocky_custom...");
        
        TransformationSearchOrchestration orchestration = createOrchestration(initialGraph, solutionLength);
        deriveBaseName(orchestration);
        printSearchInfo(orchestration);

        SearchExperiment<TransformationSolution> experiment = createExperiment(orchestration);
        experiment.run();
        
        System.out.println("[MoMoT] Search finished. Handling results...");
        handleResults(experiment);
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
        System.out.println("Transformations: " + java.util.Arrays.toString(modules));
        System.out.println("Units:           " + orchestration.getModuleManager().getUnits());
        System.out.println("Graph Size:      " + (orchestration.getProblemGraph() != null ? orchestration.getProblemGraph().size() : "null"));
        System.out.println("---------------------------");
    }
}
