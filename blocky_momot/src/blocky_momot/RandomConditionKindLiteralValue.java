package blocky_momot;

import java.util.concurrent.ThreadLocalRandom;

import at.ac.tuwien.big.momot.problem.unit.parameter.IParameterValue;
import blocky.ConditionKind;

/**
 * Supplies a random ConditionKind literal name (CHECK_FORWARD / CHECK_LEFT / CHECK_RIGHT).
 *
 * We use String literals because Henshin casts enum-typed attributes from strings
 * (via EFactory#createFromString), and integer values are not valid for EEnums.
 */
public final class RandomConditionKindLiteralValue implements IParameterValue<ConditionKind> {
	@Override
	public ConditionKind getInitialValue() {
		return nextValue();
	}

	@Override
	public ConditionKind nextValue() {
		int v = ThreadLocalRandom.current().nextInt(0, 3);
		if (v == 0) return ConditionKind.CHECK_FORWARD;
		if (v == 1) return ConditionKind.CHECK_LEFT;
		return ConditionKind.CHECK_RIGHT;
	}
}

