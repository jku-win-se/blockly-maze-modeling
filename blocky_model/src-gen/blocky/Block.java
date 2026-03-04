/**
 */
package blocky;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Block</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link blocky.Block#getNext <em>Next</em>}</li>
 * </ul>
 *
 * @see blocky.BlockyPackage#getBlock()
 * @model abstract="true"
 * @generated
 */
public interface Block extends EObject {
	/**
	 * Returns the value of the '<em><b>Next</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Next</em>' containment reference.
	 * @see #setNext(Block)
	 * @see blocky.BlockyPackage#getBlock_Next()
	 * @model containment="true"
	 * @generated
	 */
	Block getNext();

	/**
	 * Sets the value of the '{@link blocky.Block#getNext <em>Next</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Next</em>' containment reference.
	 * @see #getNext()
	 * @generated
	 */
	void setNext(Block value);

} // Block
