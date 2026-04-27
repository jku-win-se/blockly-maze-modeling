/**
 */
package blocky;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.Enumerator;

/**
 * <!-- begin-user-doc -->
 * A representation of the literals of the enumeration '<em><b>Condition Kind</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * @see blocky.BlockyPackage#getConditionKind()
 * @model
 * @generated
 */
public enum ConditionKind implements Enumerator {
	/**
	 * The '<em><b>CHECK FORWARD</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #CHECK_FORWARD_VALUE
	 * @generated
	 * @ordered
	 */
	CHECK_FORWARD(0, "CHECK_FORWARD", "CHECK_FORWARD"),

	/**
	 * The '<em><b>CHECK LEFT</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #CHECK_LEFT_VALUE
	 * @generated
	 * @ordered
	 */
	CHECK_LEFT(1, "CHECK_LEFT", "CHECK_LEFT"),

	/**
	 * The '<em><b>CHECK RIGHT</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #CHECK_RIGHT_VALUE
	 * @generated
	 * @ordered
	 */
	CHECK_RIGHT(2, "CHECK_RIGHT", "CHECK_RIGHT");

	/**
	 * The '<em><b>CHECK FORWARD</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #CHECK_FORWARD
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int CHECK_FORWARD_VALUE = 0;

	/**
	 * The '<em><b>CHECK LEFT</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #CHECK_LEFT
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int CHECK_LEFT_VALUE = 1;

	/**
	 * The '<em><b>CHECK RIGHT</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #CHECK_RIGHT
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int CHECK_RIGHT_VALUE = 2;

	/**
	 * An array of all the '<em><b>Condition Kind</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final ConditionKind[] VALUES_ARRAY = new ConditionKind[] { CHECK_FORWARD, CHECK_LEFT, CHECK_RIGHT, };

	/**
	 * A public read-only list of all the '<em><b>Condition Kind</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<ConditionKind> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>Condition Kind</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param literal the literal.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static ConditionKind get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			ConditionKind result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Condition Kind</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param name the name.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static ConditionKind getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			ConditionKind result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Condition Kind</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the integer value.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static ConditionKind get(int value) {
		switch (value) {
		case CHECK_FORWARD_VALUE:
			return CHECK_FORWARD;
		case CHECK_LEFT_VALUE:
			return CHECK_LEFT;
		case CHECK_RIGHT_VALUE:
			return CHECK_RIGHT;
		}
		return null;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private final int value;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private final String name;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private final String literal;

	/**
	 * Only this class can construct instances.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private ConditionKind(int value, String name, String literal) {
		this.value = value;
		this.name = name;
		this.literal = literal;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getValue() {
		return value;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getLiteral() {
		return literal;
	}

	/**
	 * Returns the literal value of the enumerator, which is its string representation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		return literal;
	}

} //ConditionKind
