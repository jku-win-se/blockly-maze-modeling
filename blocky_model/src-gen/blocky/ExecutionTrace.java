/**
 */
package blocky;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Execution Trace</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link blocky.ExecutionTrace#getStates <em>States</em>}</li>
 * </ul>
 *
 * @see blocky.BlockyPackage#getExecutionTrace()
 * @model
 * @generated
 */
public interface ExecutionTrace extends EObject {
	/**
	 * Returns the value of the '<em><b>States</b></em>' containment reference list.
	 * The list contents are of type {@link blocky.GameState}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>States</em>' containment reference list.
	 * @see blocky.BlockyPackage#getExecutionTrace_States()
	 * @model containment="true"
	 * @generated
	 */
	EList<GameState> getStates();

} // ExecutionTrace
