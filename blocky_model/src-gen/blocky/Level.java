/**
 */
package blocky;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Level</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link blocky.Level#getTitle <em>Title</em>}</li>
 *   <li>{@link blocky.Level#getId <em>Id</em>}</li>
 *   <li>{@link blocky.Level#getDescription <em>Description</em>}</li>
 *   <li>{@link blocky.Level#getMap <em>Map</em>}</li>
 *   <li>{@link blocky.Level#getStartOrientation <em>Start Orientation</em>}</li>
 *   <li>{@link blocky.Level#getMaxBlocks <em>Max Blocks</em>}</li>
 *   <li>{@link blocky.Level#isAllowLoops <em>Allow Loops</em>}</li>
 *   <li>{@link blocky.Level#isAllowConditionals <em>Allow Conditionals</em>}</li>
 *   <li>{@link blocky.Level#getSolution <em>Solution</em>}</li>
 *   <li>{@link blocky.Level#getTraces <em>Traces</em>}</li>
 * </ul>
 *
 * @see blocky.BlockyPackage#getLevel()
 * @model
 * @generated
 */
public interface Level extends EObject {
	/**
	 * Returns the value of the '<em><b>Title</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Title</em>' attribute.
	 * @see #setTitle(String)
	 * @see blocky.BlockyPackage#getLevel_Title()
	 * @model
	 * @generated
	 */
	String getTitle();

	/**
	 * Sets the value of the '{@link blocky.Level#getTitle <em>Title</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Title</em>' attribute.
	 * @see #getTitle()
	 * @generated
	 */
	void setTitle(String value);

	/**
	 * Returns the value of the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Id</em>' attribute.
	 * @see #setId(int)
	 * @see blocky.BlockyPackage#getLevel_Id()
	 * @model
	 * @generated
	 */
	int getId();

	/**
	 * Sets the value of the '{@link blocky.Level#getId <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Id</em>' attribute.
	 * @see #getId()
	 * @generated
	 */
	void setId(int value);

	/**
	 * Returns the value of the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Description</em>' attribute.
	 * @see #setDescription(String)
	 * @see blocky.BlockyPackage#getLevel_Description()
	 * @model
	 * @generated
	 */
	String getDescription();

	/**
	 * Sets the value of the '{@link blocky.Level#getDescription <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Description</em>' attribute.
	 * @see #getDescription()
	 * @generated
	 */
	void setDescription(String value);

	/**
	 * Returns the value of the '<em><b>Map</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Map</em>' containment reference.
	 * @see #setMap(GridMap)
	 * @see blocky.BlockyPackage#getLevel_Map()
	 * @model containment="true"
	 * @generated
	 */
	GridMap getMap();

	/**
	 * Sets the value of the '{@link blocky.Level#getMap <em>Map</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Map</em>' containment reference.
	 * @see #getMap()
	 * @generated
	 */
	void setMap(GridMap value);

	/**
	 * Returns the value of the '<em><b>Start Orientation</b></em>' attribute.
	 * The literals are from the enumeration {@link blocky.Direction}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Start Orientation</em>' attribute.
	 * @see blocky.Direction
	 * @see #setStartOrientation(Direction)
	 * @see blocky.BlockyPackage#getLevel_StartOrientation()
	 * @model
	 * @generated
	 */
	Direction getStartOrientation();

	/**
	 * Sets the value of the '{@link blocky.Level#getStartOrientation <em>Start Orientation</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Start Orientation</em>' attribute.
	 * @see blocky.Direction
	 * @see #getStartOrientation()
	 * @generated
	 */
	void setStartOrientation(Direction value);

	/**
	 * Returns the value of the '<em><b>Max Blocks</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Max Blocks</em>' attribute.
	 * @see #setMaxBlocks(int)
	 * @see blocky.BlockyPackage#getLevel_MaxBlocks()
	 * @model
	 * @generated
	 */
	int getMaxBlocks();

	/**
	 * Sets the value of the '{@link blocky.Level#getMaxBlocks <em>Max Blocks</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Max Blocks</em>' attribute.
	 * @see #getMaxBlocks()
	 * @generated
	 */
	void setMaxBlocks(int value);

	/**
	 * Returns the value of the '<em><b>Allow Loops</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Allow Loops</em>' attribute.
	 * @see #setAllowLoops(boolean)
	 * @see blocky.BlockyPackage#getLevel_AllowLoops()
	 * @model
	 * @generated
	 */
	boolean isAllowLoops();

	/**
	 * Sets the value of the '{@link blocky.Level#isAllowLoops <em>Allow Loops</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Allow Loops</em>' attribute.
	 * @see #isAllowLoops()
	 * @generated
	 */
	void setAllowLoops(boolean value);

	/**
	 * Returns the value of the '<em><b>Allow Conditionals</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Allow Conditionals</em>' attribute.
	 * @see #setAllowConditionals(boolean)
	 * @see blocky.BlockyPackage#getLevel_AllowConditionals()
	 * @model
	 * @generated
	 */
	boolean isAllowConditionals();

	/**
	 * Sets the value of the '{@link blocky.Level#isAllowConditionals <em>Allow Conditionals</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Allow Conditionals</em>' attribute.
	 * @see #isAllowConditionals()
	 * @generated
	 */
	void setAllowConditionals(boolean value);

	/**
	 * Returns the value of the '<em><b>Solution</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Solution</em>' containment reference.
	 * @see #setSolution(Block)
	 * @see blocky.BlockyPackage#getLevel_Solution()
	 * @model containment="true"
	 * @generated
	 */
	Block getSolution();

	/**
	 * Sets the value of the '{@link blocky.Level#getSolution <em>Solution</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Solution</em>' containment reference.
	 * @see #getSolution()
	 * @generated
	 */
	void setSolution(Block value);

	/**
	 * Returns the value of the '<em><b>Traces</b></em>' containment reference list.
	 * The list contents are of type {@link blocky.ExecutionTrace}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Traces</em>' containment reference list.
	 * @see blocky.BlockyPackage#getLevel_Traces()
	 * @model containment="true"
	 * @generated
	 */
	EList<ExecutionTrace> getTraces();

} // Level
