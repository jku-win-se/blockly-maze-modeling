package blocky_momot_runner;

import java.nio.file.Path;

/**
 * Per-thread MoMoT run parameters for safe parallel execution.
 */
public final class MomotRunContext {

    public static final class Config {
        public final int populationSize;
        public final int maxEvaluations;
        public final int nrRuns;
        public final int solutionLength;
        public final Path outputDirectory;

        public Config(int populationSize, int maxEvaluations, int nrRuns, int solutionLength, Path outputDirectory) {
            this.populationSize = populationSize;
            this.maxEvaluations = maxEvaluations;
            this.nrRuns = nrRuns;
            this.solutionLength = solutionLength;
            this.outputDirectory = outputDirectory;
        }
    }

    private static final ThreadLocal<Config> CURRENT = new ThreadLocal<>();

    private MomotRunContext() {}

    public static void set(Config config) {
        CURRENT.set(config);
    }

    public static Config get() {
        return CURRENT.get();
    }

    public static void clear() {
        CURRENT.remove();
    }
}

