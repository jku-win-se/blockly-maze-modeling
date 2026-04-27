/**
 */
package blocky;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Atomic Statement</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link blocky.AtomicStatement#getKind <em>Kind</em>}</li>
 * </ul>
 *
 * @see blocky.BlockyPackage#getAtomicStatement()
 * @model
 * @generated
 */
public interface AtomicStatement extends Statement {
	/**
	 * Returns the value of the '<em><b>Kind</b></em>' attribute.
	 * The literals are from the enumeration {@link blocky.AtomicStatementKind}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Kind</em>' attribute.
	 * @see blocky.AtomicStatementKind
	 * @see #setKind(AtomicStatementKind)
	 * @see blocky.BlockyPackage#getAtomicStatement_Kind()
	 * @model required="true"
	 * @generated
	 */
	AtomicStatementKind getKind();

	/**
	 * Sets the value of the '{@link blocky.AtomicStatement#getKind <em>Kind</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Kind</em>' attribute.
	 * @see blocky.AtomicStatementKind
	 * @see #getKind()
	 * @generated
	 */
	void setKind(AtomicStatementKind value);

} // AtomicStatement
