package blocky_momot;

import java.util.concurrent.ThreadLocalRandom;

import at.ac.tuwien.big.momot.problem.unit.parameter.IParameterValue;

/**
 * Simple bounded random integer generator for MOMoT transformation IN parameters.
 *
 * This is a local replacement for MOMoT's example RandomIntegerValue utility, which may
 * not be present on all target platforms.
 */
public final class RandomIntParameterValue implements IParameterValue<Integer> {
	private final int minInclusive;
	private final int maxInclusive;

	public RandomIntParameterValue(int minInclusive, int maxInclusive) {
		if (maxInclusive < minInclusive) {
			throw new IllegalArgumentException("maxInclusive < minInclusive");
		}
		this.minInclusive = minInclusive;
		this.maxInclusive = maxInclusive;
	}

	@Override
	public Integer getInitialValue() {
		return nextValue();
	}

	@Override
	public Integer nextValue() {
		if (minInclusive == maxInclusive) {
			return Integer.valueOf(minInclusive);
		}
		return Integer.valueOf(ThreadLocalRandom.current().nextInt(minInclusive, maxInclusive + 1));
	}
}

