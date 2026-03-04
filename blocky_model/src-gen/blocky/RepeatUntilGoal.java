/**
 */
package blocky;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Repeat Until Goal</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link blocky.RepeatUntilGoal#getBody <em>Body</em>}</li>
 * </ul>
 *
 * @see blocky.BlockyPackage#getRepeatUntilGoal()
 * @model
 * @generated
 */
public interface RepeatUntilGoal extends Block {
	/**
	 * Returns the value of the '<em><b>Body</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Body</em>' containment reference.
	 * @see #setBody(Block)
	 * @see blocky.BlockyPackage#getRepeatUntilGoal_Body()
	 * @model containment="true"
	 * @generated
	 */
	Block getBody();

	/**
	 * Sets the value of the '{@link blocky.RepeatUntilGoal#getBody <em>Body</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Body</em>' containment reference.
	 * @see #getBody()
	 * @generated
	 */
	void setBody(Block value);

} // RepeatUntilGoal
