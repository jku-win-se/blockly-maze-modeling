/**
 */
package blocky;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>If Stmt</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link blocky.IfStmt#getCondition <em>Condition</em>}</li>
 *   <li>{@link blocky.IfStmt#getThenBody <em>Then Body</em>}</li>
 *   <li>{@link blocky.IfStmt#getElseBody <em>Else Body</em>}</li>
 * </ul>
 *
 * @see blocky.BlockyPackage#getIfStmt()
 * @model
 * @generated
 */
public interface IfStmt extends Statement {
	/**
	 * Returns the value of the '<em><b>Condition</b></em>' attribute.
	 * The literals are from the enumeration {@link blocky.ConditionKind}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Condition</em>' attribute.
	 * @see blocky.ConditionKind
	 * @see #setCondition(ConditionKind)
	 * @see blocky.BlockyPackage#getIfStmt_Condition()
	 * @model required="true"
	 * @generated
	 */
	ConditionKind getCondition();

	/**
	 * Sets the value of the '{@link blocky.IfStmt#getCondition <em>Condition</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Condition</em>' attribute.
	 * @see blocky.ConditionKind
	 * @see #getCondition()
	 * @generated
	 */
	void setCondition(ConditionKind value);

	/**
	 * Returns the value of the '<em><b>Then Body</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Then Body</em>' containment reference.
	 * @see #setThenBody(Body)
	 * @see blocky.BlockyPackage#getIfStmt_ThenBody()
	 * @model containment="true" required="true"
	 * @generated
	 */
	Body getThenBody();

	/**
	 * Sets the value of the '{@link blocky.IfStmt#getThenBody <em>Then Body</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Then Body</em>' containment reference.
	 * @see #getThenBody()
	 * @generated
	 */
	void setThenBody(Body value);

	/**
	 * Returns the value of the '<em><b>Else Body</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Else Body</em>' containment reference.
	 * @see #setElseBody(Body)
	 * @see blocky.BlockyPackage#getIfStmt_ElseBody()
	 * @model containment="true"
	 * @generated
	 */
	Body getElseBody();

	/**
	 * Sets the value of the '{@link blocky.IfStmt#getElseBody <em>Else Body</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Else Body</em>' containment reference.
	 * @see #getElseBody()
	 * @generated
	 */
	void setElseBody(Body value);

} // IfStmt
