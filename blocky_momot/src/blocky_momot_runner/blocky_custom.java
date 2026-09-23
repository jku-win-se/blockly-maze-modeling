package blocky_momot_runner;

import at.ac.tuwien.big.moea.SearchExperiment;
import at.ac.tuwien.big.moea.experiment.executor.SearchExecutor;
import at.ac.tuwien.big.moea.experiment.executor.listener.AbstractProgressListener;
import at.ac.tuwien.big.moea.print.IPopulationWriter;
import at.ac.tuwien.big.moea.print.ISolutionWriter;
import at.ac.tuwien.big.moea.search.algorithm.EvolutionaryAlgorithmFactory;
import at.ac.tuwien.big.moea.search.algorithm.LocalSearchAlgorithmFactory;
import at.ac.tuwien.big.moea.search.algorithm.provider.IRegisteredAlgorithm;
import at.ac.tuwien.big.moea.search.fitness.dimension.IFitnessDimension;
import at.ac.tuwien.big.momot.TransformationResultManager;
import at.ac.tuwien.big.momot.TransformationSearchOrchestration;
import at.ac.tuwien.big.momot.problem.solution.TransformationSolution;
import at.ac.tuwien.big.momot.search.fitness.dimension.AbstractEGraphFitnessDimension;
import at.ac.tuwien.big.momot.util.MomotUtil;
import blocky.Game;
import blocky.Level;
import blocky_momot.BlockySimulator;
import blocky_momot.listener.IParetoFrontSubscriber;
import blocky_momot.listener.ParetoFrontPublisherListener;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.henshin.interpreter.EGraph;
import org.moeaframework.algorithm.NSGAII;
import org.moeaframework.analysis.collector.Accumulator;
import org.moeaframework.analysis.collector.AttachPoint;
import org.moeaframework.analysis.collector.Collector;
import org.moeaframework.core.Algorithm;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.PRNG;
import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;
import org.moeaframework.util.progress.ProgressEvent;
import org.moeaframework.util.progress.ProgressListener;

/**
 * Mediator class that extends the generated 'blocky' runner to inject dynamic parameters from
 * system properties and support interruption.
 */
public class blocky_custom extends blocky {

    private String currentInputModel;
    private ParetoFrontPublisherListener publisherListener;

    public synchronized ParetoFrontPublisherListener getPublisherListener() {
        if (publisherListener == null) {
            publisherListener = new ParetoFrontPublisherListener();
        }
        return publisherListener;
    }

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

    @Override
    protected double _createObjectiveHelper_2(final TransformationSolution solution, final EGraph graph, final EObject root) {
        try {
            if (root instanceof Game game) {
                Level level = game.getLevels().isEmpty() ? null : game.getLevels().get(0);
                if (level == null) return 1000000.0;
                return (double) BlockySimulator.simulationSteps(level);
            }
        } catch (Throwable t) {
            return 1000000.0;
        }
        return 1000000.0;
    }

    @Override
    protected IFitnessDimension<TransformationSolution> _createObjective_2(final TransformationSearchOrchestration orchestration) {
        return new AbstractEGraphFitnessDimension("Actions", at.ac.tuwien.big.moea.search.fitness.dimension.IFitnessDimension.FunctionType.Minimum) {
            @Override
            protected double internalEvaluate(TransformationSolution solution) {
                EGraph graph = solution.execute();
                EObject root = MomotUtil.getRoot(graph);
                return _createObjectiveHelper_2(solution, graph, root);
            }
        };
    }

    @Override
    protected IRegisteredAlgorithm<NSGAII> _createRegisteredAlgorithm_0(
            final TransformationSearchOrchestration orchestration,
            final EvolutionaryAlgorithmFactory<TransformationSolution> moea,
            final LocalSearchAlgorithmFactory<TransformationSolution> local) {
        final IRegisteredAlgorithm<NSGAII> delegate = super._createRegisteredAlgorithm_0(orchestration, moea, local);
        return new IRegisteredAlgorithm<NSGAII>() {
            @Override
            public NSGAII createAlgorithm() {
                NSGAII alg = delegate.createAlgorithm();
                getPublisherListener().setCurrentAlgorithm(alg);
                return alg;
            }

            @Override
            public String getRegisteredName() {
                return delegate.getRegisteredName();
            }

            @Override
            public boolean isRegistered() {
                return delegate.isRegistered();
            }

            @Override
            public String register() {
                return delegate.register();
            }
        };
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

        ParetoFrontPublisherListener pubListener = getPublisherListener();
        pubListener.setPopulationSize(getOverriddenPopulationSize());
        MomotRunContext.Config ctx = MomotRunContext.get();
        boolean stopOnFirstGoal = (ctx != null && ctx.stopOnFirstGoal) || Boolean.getBoolean("blocky.stopOnFirstGoal");
        pubListener.setStopOnFirstGoal(stopOnFirstGoal);

        Path outputDir = getOutputDirectory();
        pubListener.addSubscriber((nfe, paretoFront) -> {
            if (outputDir != null && paretoFront instanceof NondominatedPopulation pop && !pop.isEmpty()) {
                saveLiveResults(outputDir, pop);
            }
        });

        if (ctx != null && ctx.paretoFrontSubscriber != null) {
            pubListener.addSubscriber(ctx.paretoFrontSubscriber);
        }

        // Add a custom collector to capture the running algorithm on attach
        experiment.addCustomCollector(new Collector() {
            @Override
            public AttachPoint getAttachPoint() {
                return AttachPoint.isSubclass(Algorithm.class);
            }

            @Override
            public Collector attach(Object object) {
                if (object instanceof Algorithm alg) {
                    getPublisherListener().setCurrentAlgorithm(alg);
                }
                return this;
            }

            @Override
            public void collect(Accumulator accumulator) {
            }
        });

        experiment.addProgressListener(pubListener);

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

    private synchronized void saveLiveResults(Path outputDir, NondominatedPopulation paretoFront) {
        try {
            Path modelsPath = outputDir.resolve("models");
            Files.createDirectories(modelsPath);

            String effectiveBaseName = (baseName != null && !baseName.isBlank()) ? baseName : "blocky_custom";
            List<File> savedModels = TransformationResultManager.saveModels(modelsPath.toString(), effectiveBaseName, paretoFront);
            java.util.Set<String> validNames = new java.util.HashSet<>();
            if (savedModels != null) {
                for (File f : savedModels) {
                    if (f != null) validNames.add(f.getName());
                }
            }
            File[] existing = modelsPath.toFile().listFiles((dir, name) -> name.toLowerCase().endsWith(".xmi"));
            if (existing != null) {
                for (File f : existing) {
                    if (!validNames.contains(f.getName())) {
                        f.delete();
                    }
                }
            }

            File timesFile = outputDir.resolve("times.pf").toFile();
            File gensFile = outputDir.resolve("generations.pf").toFile();
            StringBuilder timesContent = new StringBuilder();
            StringBuilder gensContent = new StringBuilder();
            ParetoFrontPublisherListener pub = getPublisherListener();
            for (Solution solution : paretoFront) {
                long t = 0L;
                int g = 1;
                if (solution != null) {
                    Object attr = solution.getAttribute(ParetoFrontPublisherListener.ATTRIBUTE_TIME_TO_FORM);
                    if (attr instanceof Number n) {
                        t = n.longValue();
                    } else if (pub != null) {
                        String key = ParetoFrontPublisherListener.getSolutionKey(solution);
                        Long mapped = pub.getSolutionTimeToFormMap().get(key);
                        if (mapped != null) {
                            t = mapped;
                        }
                    }
                    Object gAttr = solution.getAttribute(ParetoFrontPublisherListener.ATTRIBUTE_GENERATION_TO_FORM);
                    if (gAttr instanceof Number gn) {
                        g = gn.intValue();
                    } else if (pub != null) {
                        String key = ParetoFrontPublisherListener.getSolutionKey(solution);
                        Integer mappedGen = pub.getSolutionGenerationToFormMap().get(key);
                        if (mappedGen != null) {
                            g = mappedGen;
                        }
                    }
                }
                timesContent.append(t).append("\n");
                gensContent.append(g).append("\n");
            }
            Files.writeString(timesFile.toPath(), timesContent.toString(), StandardCharsets.UTF_8);
            Files.writeString(gensFile.toPath(), gensContent.toString(), StandardCharsets.UTF_8);

            File firstGoalFile = outputDir.resolve("first_goal.txt").toFile();
            Long firstGoalTime = pub != null ? pub.getFirstGoalReachedTimeMs() : null;
            Integer firstGoalGen = pub != null ? pub.getFirstGoalReachedGeneration() : null;
            if (firstGoalTime != null && firstGoalGen != null) {
                String firstGoalContent = "timeToFirstGoalMs=" + firstGoalTime + "\ngenerationOfFirstGoal=" + firstGoalGen + "\n";
                Files.writeString(firstGoalFile.toPath(), firstGoalContent, StandardCharsets.UTF_8);
            }

            String objectivesFile = outputDir.resolve("objectives.pf").toString();
            TransformationResultManager.saveObjectives(objectivesFile, paretoFront);
        } catch (Throwable ignored) {
        }
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

        File timesFile = outputDir.resolve("times.pf").toFile();
        File gensFile = outputDir.resolve("generations.pf").toFile();
        StringBuilder timesContent = new StringBuilder();
        StringBuilder gensContent = new StringBuilder();
        ParetoFrontPublisherListener pub = getPublisherListener();
        for (Solution solution : population) {
            long t = 0L;
            int g = 1;
            if (solution != null) {
                Object attr = solution.getAttribute(ParetoFrontPublisherListener.ATTRIBUTE_TIME_TO_FORM);
                if (attr instanceof Number n) {
                    t = n.longValue();
                } else if (pub != null) {
                    String key = ParetoFrontPublisherListener.getSolutionKey(solution);
                    Long mapped = pub.getSolutionTimeToFormMap().get(key);
                    if (mapped != null) {
                        t = mapped;
                    }
                }
                Object gAttr = solution.getAttribute(ParetoFrontPublisherListener.ATTRIBUTE_GENERATION_TO_FORM);
                if (gAttr instanceof Number gn) {
                    g = gn.intValue();
                } else if (pub != null) {
                    String key = ParetoFrontPublisherListener.getSolutionKey(solution);
                    Integer mappedGen = pub.getSolutionGenerationToFormMap().get(key);
                    if (mappedGen != null) {
                        g = mappedGen;
                    }
                }
            }
            timesContent.append(t).append("\n");
            gensContent.append(g).append("\n");
        }
        try {
            Files.writeString(timesFile.toPath(), timesContent.toString(), StandardCharsets.UTF_8);
            Files.writeString(gensFile.toPath(), gensContent.toString(), StandardCharsets.UTF_8);
            File firstGoalFile = outputDir.resolve("first_goal.txt").toFile();
            Long firstGoalTime = pub != null ? pub.getFirstGoalReachedTimeMs() : null;
            Integer firstGoalGen = pub != null ? pub.getFirstGoalReachedGeneration() : null;
            if (firstGoalTime != null && firstGoalGen != null) {
                String firstGoalContent = "timeToFirstGoalMs=" + firstGoalTime + "\ngenerationOfFirstGoal=" + firstGoalGen + "\n";
                Files.writeString(firstGoalFile.toPath(), firstGoalContent, StandardCharsets.UTF_8);
                System.out.println("---------------------------");
                System.out.println("First Goal-Reaching Solution: " + String.format(java.util.Locale.US, "%.2fs", firstGoalTime / 1000.0) + " (" + firstGoalTime + " ms), Generation: " + firstGoalGen);
                System.out.println("---------------------------");
            }
        } catch (Exception ignored) {
        }

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
        List<File> savedModels = TransformationResultManager.saveModels(modelsDir, baseName, population);
        java.util.Set<String> validNames = new java.util.HashSet<>();
        if (savedModels != null) {
            for (File f : savedModels) {
                if (f != null) validNames.add(f.getName());
            }
        }
        File[] existing = new File(modelsDir).listFiles((dir, name) -> name.toLowerCase().endsWith(".xmi"));
        if (existing != null) {
            for (File f : existing) {
                if (!validNames.contains(f.getName())) {
                    f.delete();
                }
            }
        }

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
