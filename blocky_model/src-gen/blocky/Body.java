/**
 */
package blocky;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Body</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link blocky.Body#getFirstContainer <em>First Container</em>}</li>
 * </ul>
 *
 * @see blocky.BlockyPackage#getBody()
 * @model
 * @generated
 */
public interface Body extends EObject {
	/**
	 * Returns the value of the '<em><b>First Container</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>First Container</em>' containment reference.
	 * @see #setFirstContainer(Container)
	 * @see blocky.BlockyPackage#getBody_FirstContainer()
	 * @model containment="true"
	 * @generated
	 */
	Container getFirstContainer();

	/**
	 * Sets the value of the '{@link blocky.Body#getFirstContainer <em>First Container</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>First Container</em>' containment reference.
	 * @see #getFirstContainer()
	 * @generated
	 */
	void setFirstContainer(Container value);

} // Body
