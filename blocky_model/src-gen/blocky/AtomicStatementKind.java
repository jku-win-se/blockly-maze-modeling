/**
 */
package blocky;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.Enumerator;

/**
 * <!-- begin-user-doc -->
 * A representation of the literals of the enumeration '<em><b>Atomic Statement Kind</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * @see blocky.BlockyPackage#getAtomicStatementKind()
 * @model
 * @generated
 */
public enum AtomicStatementKind implements Enumerator {
	/**
	 * The '<em><b>TURN LEFT</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #TURN_LEFT_VALUE
	 * @generated
	 * @ordered
	 */
	TURN_LEFT(0, "TURN_LEFT", "TURN_LEFT"),

	/**
	 * The '<em><b>TURN RIGHT</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #TURN_RIGHT_VALUE
	 * @generated
	 * @ordered
	 */
	TURN_RIGHT(1, "TURN_RIGHT", "TURN_RIGHT"),

	/**
	 * The '<em><b>MOVE FORWARD</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #MOVE_FORWARD_VALUE
	 * @generated
	 * @ordered
	 */
	MOVE_FORWARD(2, "MOVE_FORWARD", "MOVE_FORWARD");

	/**
	 * The '<em><b>TURN LEFT</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #TURN_LEFT
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int TURN_LEFT_VALUE = 0;

	/**
	 * The '<em><b>TURN RIGHT</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #TURN_RIGHT
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int TURN_RIGHT_VALUE = 1;

	/**
	 * The '<em><b>MOVE FORWARD</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #MOVE_FORWARD
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int MOVE_FORWARD_VALUE = 2;

	/**
	 * An array of all the '<em><b>Atomic Statement Kind</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final AtomicStatementKind[] VALUES_ARRAY = new AtomicStatementKind[] { TURN_LEFT, TURN_RIGHT,
			MOVE_FORWARD, };

	/**
	 * A public read-only list of all the '<em><b>Atomic Statement Kind</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<AtomicStatementKind> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>Atomic Statement Kind</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param literal the literal.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static AtomicStatementKind get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			AtomicStatementKind result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Atomic Statement Kind</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param name the name.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static AtomicStatementKind getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			AtomicStatementKind result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Atomic Statement Kind</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the integer value.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static AtomicStatementKind get(int value) {
		switch (value) {
		case TURN_LEFT_VALUE:
			return TURN_LEFT;
		case TURN_RIGHT_VALUE:
			return TURN_RIGHT;
		case MOVE_FORWARD_VALUE:
			return MOVE_FORWARD;
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
	private AtomicStatementKind(int value, String name, String literal) {
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

} //AtomicStatementKind
