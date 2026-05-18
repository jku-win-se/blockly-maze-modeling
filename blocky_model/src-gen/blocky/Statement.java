/**
 */
package blocky;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Statement</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link blocky.Statement#isGenerated <em>Generated</em>}</li>
 * </ul>
 *
 * @see blocky.BlockyPackage#getStatement()
 * @model abstract="true"
 * @generated
 */
public interface Statement extends EObject {
	/**
	 * Returns the value of the '<em><b>Generated</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Generated</em>' attribute.
	 * @see #setGenerated(boolean)
	 * @see blocky.BlockyPackage#getStatement_Generated()
	 * @model
	 * @generated
	 */
	boolean isGenerated();

	/**
	 * Sets the value of the '{@link blocky.Statement#isGenerated <em>Generated</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Generated</em>' attribute.
	 * @see #isGenerated()
	 * @generated
	 */
	void setGenerated(boolean value);

} // Statement
