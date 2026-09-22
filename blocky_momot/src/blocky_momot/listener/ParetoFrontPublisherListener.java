package blocky_momot.listener;

import at.ac.tuwien.big.moea.experiment.executor.SearchExecutor;
import at.ac.tuwien.big.moea.experiment.executor.listener.AbstractProgressListener;
import at.ac.tuwien.big.momot.problem.solution.TransformationSolution;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.moeaframework.core.Algorithm;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.util.progress.ProgressEvent;

/**
 * MOEA progress listener that records solution formation times and notifies subscribers
 * whenever the Pareto front is updated during search execution.
 */
public class ParetoFrontPublisherListener extends AbstractProgressListener {

    public static final String ATTRIBUTE_TIME_TO_FORM = "timeToFormMs";
    public static final String ATTRIBUTE_GENERATION_TO_FORM = "generationToForm";

    private final List<IParetoFrontSubscriber> subscribers = new CopyOnWriteArrayList<>();
    private final Map<String, Long> solutionTimeToFormMap = new ConcurrentHashMap<>();
    private final Map<String, Integer> solutionGenerationToFormMap = new ConcurrentHashMap<>();
    private final NondominatedPopulation globalParetoFront = new NondominatedPopulation();
    private final java.util.concurrent.atomic.AtomicLong firstGoalReachedTimeMs = new java.util.concurrent.atomic.AtomicLong(-1);
    private final java.util.concurrent.atomic.AtomicInteger firstGoalReachedGeneration = new java.util.concurrent.atomic.AtomicInteger(-1);
    private volatile long startTimeMs = 0;
    private volatile long lastNotifyTimeMs = 0;
    private volatile int populationSize = 50;
    private volatile Algorithm currentAlgorithm;

    public ParetoFrontPublisherListener() {}

    public ParetoFrontPublisherListener(IParetoFrontSubscriber initialSubscriber) {
        if (initialSubscriber != null) {
            subscribers.add(initialSubscriber);
        }
    }

    public void setPopulationSize(int populationSize) {
        if (populationSize > 0) {
            this.populationSize = populationSize;
        }
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public void setCurrentAlgorithm(Algorithm algorithm) {
        this.currentAlgorithm = algorithm;
    }

    public Algorithm getCurrentAlgorithm() {
        return currentAlgorithm;
    }

    public void addSubscriber(IParetoFrontSubscriber subscriber) {
        if (subscriber != null && !subscribers.contains(subscriber)) {
            subscribers.add(subscriber);
        }
    }

    public void removeSubscriber(IParetoFrontSubscriber subscriber) {
        if (subscriber != null) {
            subscribers.remove(subscriber);
        }
    }

    public synchronized void resetTimer() {
        this.startTimeMs = System.currentTimeMillis();
        this.lastNotifyTimeMs = 0;
        this.solutionTimeToFormMap.clear();
        this.solutionGenerationToFormMap.clear();
        this.firstGoalReachedTimeMs.set(-1);
        this.firstGoalReachedGeneration.set(-1);
        synchronized (globalParetoFront) {
            this.globalParetoFront.clear();
        }
    }

    public Long getFirstGoalReachedTimeMs() {
        long t = firstGoalReachedTimeMs.get();
        return t >= 0 ? t : null;
    }

    public Integer getFirstGoalReachedGeneration() {
        int g = firstGoalReachedGeneration.get();
        return g >= 0 ? g : null;
    }

    public static boolean isGoalSolution(Solution s) {
        if (s == null) return false;
        double[] objs = s.getObjectives();
        return objs != null && objs.length > 0 && objs[0] <= -0.5;
    }

    public NondominatedPopulation getGlobalParetoFrontSnapshot() {
        synchronized (globalParetoFront) {
            return new NondominatedPopulation(globalParetoFront);
        }
    }

    public long getStartTimeMs() {
        return startTimeMs;
    }

    public Map<String, Long> getSolutionTimeToFormMap() {
        return solutionTimeToFormMap;
    }

    public Map<String, Integer> getSolutionGenerationToFormMap() {
        return solutionGenerationToFormMap;
    }

    public static String getSolutionKey(Solution s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        if (s.getObjectives() != null) {
            sb.append(Arrays.toString(s.getObjectives()));
        }
        if (s instanceof TransformationSolution ts) {
            sb.append("_len").append(ts.getSolutionLength());
        }
        return sb.toString();
    }

    public static boolean haveSameObjectives(Solution s1, Solution s2) {
        if (s1 == null || s2 == null) return false;
        double[] o1 = s1.getObjectives();
        double[] o2 = s2.getObjectives();
        if (o1 == null || o2 == null || o1.length != o2.length) return false;
        for (int i = 0; i < o1.length; i++) {
            if (Math.abs(o1[i] - o2[i]) > 1e-4) return false;
        }
        return true;
    }

    @Override
    public void update(ProgressEvent event) {
        if (event == null) {
            return;
        }

        if (isStarted(event)) {
            resetTimer();
        }

        Algorithm algorithm = this.currentAlgorithm;
        if (algorithm == null && event.getExecutor() instanceof SearchExecutor executor) {
            algorithm = executor.getAlgorithm();
        }

        if (algorithm != null) {
            NondominatedPopulation result = algorithm.getResult();
            if (result != null && !result.isEmpty()) {
                long now = System.currentTimeMillis();
                if (startTimeMs <= 0) {
                    startTimeMs = now;
                }
                long elapsed = Math.max(1, now - startTimeMs);

                int nfe = algorithm.getNumberOfEvaluations();
                if (nfe <= 0) {
                    nfe = event.getCurrentNFE();
                }
                int pop = populationSize > 0 ? populationSize : 50;
                int currentGen = nfe > 0 ? Math.max(1, (nfe + pop - 1) / pop) : 1;

                boolean newSolutionFound = false;
                synchronized (globalParetoFront) {
                    for (Solution s : result) {
                        if (s != null) {
                            String key = getSolutionKey(s);
                            Long formed = solutionTimeToFormMap.putIfAbsent(key, elapsed);
                            Integer formedGen = solutionGenerationToFormMap.putIfAbsent(key, currentGen);
                            if (formed == null || formedGen == null) {
                                newSolutionFound = true;
                            }
                            long time = (formed != null) ? formed : elapsed;
                            int gen = (formedGen != null) ? formedGen : currentGen;
                            s.setAttribute(ATTRIBUTE_TIME_TO_FORM, time);
                            s.setAttribute(ATTRIBUTE_GENERATION_TO_FORM, gen);

                            if (isGoalSolution(s)) {
                                firstGoalReachedTimeMs.accumulateAndGet(time, (curr, val) -> curr < 0 ? val : Math.min(curr, val));
                                firstGoalReachedGeneration.accumulateAndGet(gen, (curr, val) -> curr < 0 ? val : Math.min(curr, val));
                            }

                            // Check if a solution with identical objectives already exists in globalParetoFront
                            boolean hasIdentical = false;
                            for (Solution existing : globalParetoFront) {
                                if (haveSameObjectives(existing, s)) {
                                    hasIdentical = true;
                                    break;
                                }
                            }
                            if (!hasIdentical) {
                                if (globalParetoFront.add(s)) {
                                    newSolutionFound = true;
                                }
                            }
                        }
                    }
                }

                boolean shouldNotify = newSolutionFound
                        || (now - lastNotifyTimeMs >= 400)
                        || isSeedFinished(event)
                        || isFinished(event);

                if (shouldNotify) {
                    lastNotifyTimeMs = now;
                    NondominatedPopulation snapshot;
                    synchronized (globalParetoFront) {
                        snapshot = new NondominatedPopulation(globalParetoFront);
                    }
                    if (!snapshot.isEmpty()) {
                        for (IParetoFrontSubscriber subscriber : subscribers) {
                            try {
                                subscriber.onParetoFrontUpdated(event.getCurrentNFE(), snapshot);
                            } catch (Throwable ignored) {
                            }
                        }
                    }
                }
            }
        }
    }
}
