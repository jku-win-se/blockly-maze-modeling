/**
 */
package blocky;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Turn</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link blocky.Turn#getDirection <em>Direction</em>}</li>
 * </ul>
 *
 * @see blocky.BlockyPackage#getTurn()
 * @model
 * @generated
 */
public interface Turn extends Block {
	/**
	 * Returns the value of the '<em><b>Direction</b></em>' attribute.
	 * The literals are from the enumeration {@link blocky.TurnDirection}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Direction</em>' attribute.
	 * @see blocky.TurnDirection
	 * @see #setDirection(TurnDirection)
	 * @see blocky.BlockyPackage#getTurn_Direction()
	 * @model
	 * @generated
	 */
	TurnDirection getDirection();

	/**
	 * Sets the value of the '{@link blocky.Turn#getDirection <em>Direction</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Direction</em>' attribute.
	 * @see blocky.TurnDirection
	 * @see #getDirection()
	 * @generated
	 */
	void setDirection(TurnDirection value);

} // Turn
