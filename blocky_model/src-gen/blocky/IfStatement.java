/**
 */
package blocky;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>If Statement</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link blocky.IfStatement#getCondition <em>Condition</em>}</li>
 *   <li>{@link blocky.IfStatement#getThenBranch <em>Then Branch</em>}</li>
 *   <li>{@link blocky.IfStatement#getElseBranch <em>Else Branch</em>}</li>
 * </ul>
 *
 * @see blocky.BlockyPackage#getIfStatement()
 * @model
 * @generated
 */
public interface IfStatement extends Block {
	/**
	 * Returns the value of the '<em><b>Condition</b></em>' attribute.
	 * The literals are from the enumeration {@link blocky.SensorDirection}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Condition</em>' attribute.
	 * @see blocky.SensorDirection
	 * @see #setCondition(SensorDirection)
	 * @see blocky.BlockyPackage#getIfStatement_Condition()
	 * @model
	 * @generated
	 */
	SensorDirection getCondition();

	/**
	 * Sets the value of the '{@link blocky.IfStatement#getCondition <em>Condition</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Condition</em>' attribute.
	 * @see blocky.SensorDirection
	 * @see #getCondition()
	 * @generated
	 */
	void setCondition(SensorDirection value);

	/**
	 * Returns the value of the '<em><b>Then Branch</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Then Branch</em>' containment reference.
	 * @see #setThenBranch(Block)
	 * @see blocky.BlockyPackage#getIfStatement_ThenBranch()
	 * @model containment="true"
	 * @generated
	 */
	Block getThenBranch();

	/**
	 * Sets the value of the '{@link blocky.IfStatement#getThenBranch <em>Then Branch</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Then Branch</em>' containment reference.
	 * @see #getThenBranch()
	 * @generated
	 */
	void setThenBranch(Block value);

	/**
	 * Returns the value of the '<em><b>Else Branch</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Else Branch</em>' containment reference.
	 * @see #setElseBranch(Block)
	 * @see blocky.BlockyPackage#getIfStatement_ElseBranch()
	 * @model containment="true"
	 * @generated
	 */
	Block getElseBranch();

	/**
	 * Sets the value of the '{@link blocky.IfStatement#getElseBranch <em>Else Branch</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Else Branch</em>' containment reference.
	 * @see #getElseBranch()
	 * @generated
	 */
	void setElseBranch(Block value);

} // IfStatement
