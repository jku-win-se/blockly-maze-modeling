package blocky_momot_runner;

import at.ac.tuwien.big.moea.SearchExperiment;
import at.ac.tuwien.big.moea.experiment.executor.SearchExecutor;
import at.ac.tuwien.big.moea.experiment.executor.listener.AbstractProgressListener;
import at.ac.tuwien.big.moea.print.IPopulationWriter;
import at.ac.tuwien.big.moea.print.ISolutionWriter;
import at.ac.tuwien.big.moea.search.algorithm.EvolutionaryAlgorithmFactory;
import at.ac.tuwien.big.moea.search.algorithm.LocalSearchAlgorithmFactory;
import at.ac.tuwien.big.momot.TransformationResultManager;
import at.ac.tuwien.big.momot.TransformationSearchOrchestration;
import at.ac.tuwien.big.momot.problem.solution.TransformationSolution;
import at.ac.tuwien.big.momot.util.MomotUtil;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Population;
import org.moeaframework.core.PRNG;
import org.moeaframework.util.progress.ProgressEvent;
import org.moeaframework.util.progress.ProgressListener;

/**
 * Mediator class that extends the generated 'blocky' runner to inject dynamic parameters from
 * system properties and support interruption.
 */
public class blocky_custom extends blocky {

    private String currentInputModel;

    private int getOverriddenPopulationSize() {
        MomotRunContext.Config ctx = MomotRunContext.get();
        if (ctx != null && ctx.populationSize > 0) {
            return ctx.populationSize;
        }
        return Integer.getInteger("blocky.populationSize", this.populationSize);
    }

    private int getOverriddenMaxEvaluations() {
        MomotRunContext.Config ctx = MomotRunContext.get();
        if (ctx != null && ctx.maxEvaluations > 0) {
            return ctx.maxEvaluations;
        }
        return Integer.getInteger("blocky.maxEvaluations", this.maxEvaluations);
    }

    private int getOverriddenNrRuns() {
        MomotRunContext.Config ctx = MomotRunContext.get();
        if (ctx != null && ctx.nrRuns > 0) {
            return ctx.nrRuns;
        }
        return Integer.getInteger("blocky.nrRuns", this.nrRuns);
    }

    private Path getOutputDirectory() {
        MomotRunContext.Config ctx = MomotRunContext.get();
        return ctx != null ? ctx.outputDirectory : null;
    }

    @Override
    protected TransformationSearchOrchestration createOrchestration(String initialGraph, int solutionLength) {
        TransformationSearchOrchestration orchestration = super.createOrchestration(initialGraph, solutionLength);

        // BRIDGE ClassLoaders by registering the local dynamic package version
        try {
            org.eclipse.emf.ecore.EObject root = MomotUtil.getRoot(orchestration.getProblemGraph());
            if (root != null && root.eResource() != null) {
                org.eclipse.emf.ecore.resource.ResourceSet rs = root.eResource().getResourceSet();
                Class<?> pkgClass = Class.forName("blocky.BlockyPackage", true, this.getClass().getClassLoader());
                org.eclipse.emf.ecore.EPackage localPkg =
                        (org.eclipse.emf.ecore.EPackage) pkgClass.getField("eINSTANCE").get(null);
                rs.getPackageRegistry().put(localPkg.getNsURI(), localPkg);
                rs.getPackageRegistry().put(localPkg.getNsURI() + "#", localPkg);
            }
        } catch (Throwable ignored) {
        }

        // Re-create the algorithm factory with the overridden population size
        int popSize = getOverriddenPopulationSize();
        EvolutionaryAlgorithmFactory<TransformationSolution> moea = orchestration.createEvolutionaryAlgorithmFactory(popSize);
        LocalSearchAlgorithmFactory<TransformationSolution> local = orchestration.createLocalSearchAlgorithmFactory();

        // Clear and re-register algorithms to use the new factory
        orchestration.getAlgorithms().clear();
        orchestration.addAlgorithm("NSGA_II", _createRegisteredAlgorithm_0(orchestration, moea, local));

        return orchestration;
    }

    private ProgressListener createPerRunSeedListener() {
        return new AbstractProgressListener() {
            @Override
            public void update(ProgressEvent event) {
                if (isStarted(event) || isSeedStarted(event)) {
                    int seed = event.getCurrentSeed();
                    if (seed > 0) {
                        PRNG.setSeed(seed);
                    }
                }
            }
        };
    }

    @Override
    protected SearchExperiment<TransformationSolution> createExperiment(TransformationSearchOrchestration orchestration) {
        SearchExperiment<TransformationSolution> experiment =
                new SearchExperiment<>(orchestration, getOverriddenMaxEvaluations());
        experiment.setNumberOfRuns(getOverriddenNrRuns());
        experiment.addProgressListener(_createListener_0());
        experiment.addProgressListener(createPerRunSeedListener());

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
        currentInputModel = initialGraph;

        TransformationSearchOrchestration orchestration = createOrchestration(initialGraph, solutionLength);
        deriveBaseName(orchestration);
        printSearchInfo(orchestration);

        SearchExperiment<TransformationSolution> experiment = createExperiment(orchestration);
        experiment.run();

        System.out.println("[MoMoT] Search finished. Handling results...");
        handleResults(experiment);
    }

    @Override
    protected TransformationResultManager handleResults(final SearchExperiment<TransformationSolution> experiment) {
        Path outputDir = getOutputDirectory();
        if (outputDir == null) {
            return super.handleResults(experiment);
        }

        ISolutionWriter<TransformationSolution> solutionWriter = experiment.getSearchOrchestration().createSolutionWriter();
        IPopulationWriter<TransformationSolution> populationWriter =
                experiment.getSearchOrchestration().createPopulationWriter();
        TransformationResultManager resultManager = new TransformationResultManager(experiment);

        String objectivesFile = outputDir.resolve("objectives.pf").toString();
        String solutionsFile = outputDir.resolve("solutions.txt").toString();
        String solutionsDir = outputDir.resolve("solutions").toString();
        String modelsDir = outputDir.resolve("models").toString();

        Population population = TransformationResultManager.createApproximationSet(experiment, (String[]) null);
        System.out.println("- Save objectives of all algorithms to '" + objectivesFile + "'");
        TransformationResultManager.saveObjectives(objectivesFile, population);

        if (experiment.hasResults()) {
            int seed = 1;
            for (Map.Entry<SearchExecutor, List<NondominatedPopulation>> entry : experiment.getResults().entrySet()) {
                List<NondominatedPopulation> runs = entry.getValue();
                if (runs == null) {
                    continue;
                }
                for (NondominatedPopulation runPopulation : runs) {
                    String perRunFile = outputDir.resolve("objectives_seed_" + seed + ".pf").toString();
                    TransformationResultManager.saveObjectives(perRunFile, runPopulation);
                    seed++;
                }
            }
        }

        population = TransformationResultManager.createApproximationSet(experiment, (String[]) null);
        TransformationResultManager.savePopulation(solutionsFile, population, populationWriter);
        TransformationResultManager.saveSolutions(
                solutionsDir, baseName, MomotUtil.asIterables(population, TransformationSolution.class), solutionWriter);

        population = TransformationResultManager.createApproximationSet(experiment, (String[]) null);
        TransformationResultManager.saveModels(modelsDir, baseName, population);

        return resultManager;
    }

    @Override
    public void printSearchInfo(TransformationSearchOrchestration orchestration) {
        System.out.println("-------------------------------------------------------");
        System.out.println("Search (Customized via blocky_custom)");
        System.out.println("-------------------------------------------------------");
        System.out.println("InputModel:      " + (currentInputModel != null ? currentInputModel : INITIAL_MODEL));
        System.out.println("Objectives:      " + orchestration.getFitnessFunction().getObjectiveNames());
        System.out.println("SolutionLength:  " + orchestration.getSolutionLength());
        System.out.println("PopulationSize:  " + getOverriddenPopulationSize() + " (overridden)");
        System.out.println("MaxEvaluations:  " + getOverriddenMaxEvaluations() + " (overridden)");
        System.out.println("AlgorithmRuns:   " + getOverriddenNrRuns() + " (overridden)");
        System.out.println("Iterations:      " + getOverriddenMaxEvaluations() / getOverriddenPopulationSize());
        System.out.println("Transformations: " + java.util.Arrays.toString(modules));
        System.out.println("Units:           " + orchestration.getModuleManager().getUnits());
        System.out.println(
                "Graph Size:      " + (orchestration.getProblemGraph() != null ? orchestration.getProblemGraph().size() : "null"));
        System.out.println("---------------------------");
    }
}

