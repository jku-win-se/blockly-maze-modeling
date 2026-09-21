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

    private final List<IParetoFrontSubscriber> subscribers = new CopyOnWriteArrayList<>();
    private final Map<String, Long> solutionTimeToFormMap = new ConcurrentHashMap<>();
    private volatile long startTimeMs = 0;
    private volatile long lastNotifyTimeMs = 0;
    private volatile Algorithm currentAlgorithm;

    public ParetoFrontPublisherListener() {}

    public ParetoFrontPublisherListener(IParetoFrontSubscriber initialSubscriber) {
        if (initialSubscriber != null) {
            subscribers.add(initialSubscriber);
        }
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

    public void resetTimer() {
        this.startTimeMs = System.currentTimeMillis();
        this.lastNotifyTimeMs = 0;
        this.solutionTimeToFormMap.clear();
    }

    public long getStartTimeMs() {
        return startTimeMs;
    }

    public Map<String, Long> getSolutionTimeToFormMap() {
        return solutionTimeToFormMap;
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

                boolean newSolutionFound = false;
                for (Solution s : result) {
                    if (s != null) {
                        String key = getSolutionKey(s);
                        Long formed = solutionTimeToFormMap.putIfAbsent(key, elapsed);
                        if (formed == null) {
                            newSolutionFound = true;
                        }
                        long time = (formed != null) ? formed : elapsed;
                        s.setAttribute(ATTRIBUTE_TIME_TO_FORM, time);
                    }
                }

                boolean shouldNotify = newSolutionFound
                        || (now - lastNotifyTimeMs >= 400)
                        || isSeedFinished(event)
                        || isFinished(event);

                if (shouldNotify) {
                    lastNotifyTimeMs = now;
                    for (IParetoFrontSubscriber subscriber : subscribers) {
                        try {
                            subscriber.onParetoFrontUpdated(event.getCurrentNFE(), result);
                        } catch (Throwable ignored) {
                        }
                    }
                }
            }
        }
    }
}
