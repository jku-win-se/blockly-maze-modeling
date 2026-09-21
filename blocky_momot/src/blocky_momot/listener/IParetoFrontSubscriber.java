package blocky_momot.listener;

/**
 * Subscriber interface for receiving live Pareto front updates during MOMoT optimization runs.
 */
@FunctionalInterface
public interface IParetoFrontSubscriber {
    void onParetoFrontUpdated(int nfe, Object paretoFront);
}
