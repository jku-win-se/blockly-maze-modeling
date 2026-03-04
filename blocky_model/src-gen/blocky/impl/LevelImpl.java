/**
 */
package blocky.impl;

import blocky.Block;
import blocky.BlockyPackage;
import blocky.Direction;
import blocky.ExecutionTrace;
import blocky.GridMap;
import blocky.Level;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Level</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link blocky.impl.LevelImpl#getTitle <em>Title</em>}</li>
 *   <li>{@link blocky.impl.LevelImpl#getId <em>Id</em>}</li>
 *   <li>{@link blocky.impl.LevelImpl#getDescription <em>Description</em>}</li>
 *   <li>{@link blocky.impl.LevelImpl#getMap <em>Map</em>}</li>
 *   <li>{@link blocky.impl.LevelImpl#getStartOrientation <em>Start Orientation</em>}</li>
 *   <li>{@link blocky.impl.LevelImpl#getMaxBlocks <em>Max Blocks</em>}</li>
 *   <li>{@link blocky.impl.LevelImpl#isAllowLoops <em>Allow Loops</em>}</li>
 *   <li>{@link blocky.impl.LevelImpl#isAllowConditionals <em>Allow Conditionals</em>}</li>
 *   <li>{@link blocky.impl.LevelImpl#getSolution <em>Solution</em>}</li>
 *   <li>{@link blocky.impl.LevelImpl#getTraces <em>Traces</em>}</li>
 * </ul>
 *
 * @generated
 */
public class LevelImpl extends MinimalEObjectImpl.Container implements Level {
	/**
	 * The default value of the '{@link #getTitle() <em>Title</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTitle()
	 * @generated
	 * @ordered
	 */
	protected static final String TITLE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getTitle() <em>Title</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTitle()
	 * @generated
	 * @ordered
	 */
	protected String title = TITLE_EDEFAULT;

	/**
	 * The default value of the '{@link #getId() <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getId()
	 * @generated
	 * @ordered
	 */
	protected static final int ID_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getId() <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getId()
	 * @generated
	 * @ordered
	 */
	protected int id = ID_EDEFAULT;

	/**
	 * The default value of the '{@link #getDescription() <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDescription()
	 * @generated
	 * @ordered
	 */
	protected static final String DESCRIPTION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getDescription() <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDescription()
	 * @generated
	 * @ordered
	 */
	protected String description = DESCRIPTION_EDEFAULT;

	/**
	 * The cached value of the '{@link #getMap() <em>Map</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMap()
	 * @generated
	 * @ordered
	 */
	protected GridMap map;

	/**
	 * The default value of the '{@link #getStartOrientation() <em>Start Orientation</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStartOrientation()
	 * @generated
	 * @ordered
	 */
	protected static final Direction START_ORIENTATION_EDEFAULT = Direction.NORTH;

	/**
	 * The cached value of the '{@link #getStartOrientation() <em>Start Orientation</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStartOrientation()
	 * @generated
	 * @ordered
	 */
	protected Direction startOrientation = START_ORIENTATION_EDEFAULT;

	/**
	 * The default value of the '{@link #getMaxBlocks() <em>Max Blocks</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMaxBlocks()
	 * @generated
	 * @ordered
	 */
	protected static final int MAX_BLOCKS_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getMaxBlocks() <em>Max Blocks</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMaxBlocks()
	 * @generated
	 * @ordered
	 */
	protected int maxBlocks = MAX_BLOCKS_EDEFAULT;

	/**
	 * The default value of the '{@link #isAllowLoops() <em>Allow Loops</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isAllowLoops()
	 * @generated
	 * @ordered
	 */
	protected static final boolean ALLOW_LOOPS_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isAllowLoops() <em>Allow Loops</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isAllowLoops()
	 * @generated
	 * @ordered
	 */
	protected boolean allowLoops = ALLOW_LOOPS_EDEFAULT;

	/**
	 * The default value of the '{@link #isAllowConditionals() <em>Allow Conditionals</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isAllowConditionals()
	 * @generated
	 * @ordered
	 */
	protected static final boolean ALLOW_CONDITIONALS_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isAllowConditionals() <em>Allow Conditionals</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isAllowConditionals()
	 * @generated
	 * @ordered
	 */
	protected boolean allowConditionals = ALLOW_CONDITIONALS_EDEFAULT;

	/**
	 * The cached value of the '{@link #getSolution() <em>Solution</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSolution()
	 * @generated
	 * @ordered
	 */
	protected Block solution;

	/**
	 * The cached value of the '{@link #getTraces() <em>Traces</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTraces()
	 * @generated
	 * @ordered
	 */
	protected EList<ExecutionTrace> traces;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected LevelImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return BlockyPackage.Literals.LEVEL;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getTitle() {
		return title;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setTitle(String newTitle) {
		String oldTitle = title;
		title = newTitle;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BlockyPackage.LEVEL__TITLE, oldTitle, title));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getId() {
		return id;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setId(int newId) {
		int oldId = id;
		id = newId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BlockyPackage.LEVEL__ID, oldId, id));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDescription(String newDescription) {
		String oldDescription = description;
		description = newDescription;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BlockyPackage.LEVEL__DESCRIPTION, oldDescription,
					description));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GridMap getMap() {
		return map;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetMap(GridMap newMap, NotificationChain msgs) {
		GridMap oldMap = map;
		map = newMap;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, BlockyPackage.LEVEL__MAP,
					oldMap, newMap);
			if (msgs == null)
				msgs = notification;
			else
				msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setMap(GridMap newMap) {
		if (newMap != map) {
			NotificationChain msgs = null;
			if (map != null)
				msgs = ((InternalEObject) map).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - BlockyPackage.LEVEL__MAP,
						null, msgs);
			if (newMap != null)
				msgs = ((InternalEObject) newMap).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - BlockyPackage.LEVEL__MAP,
						null, msgs);
			msgs = basicSetMap(newMap, msgs);
			if (msgs != null)
				msgs.dispatch();
		} else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BlockyPackage.LEVEL__MAP, newMap, newMap));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Direction getStartOrientation() {
		return startOrientation;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setStartOrientation(Direction newStartOrientation) {
		Direction oldStartOrientation = startOrientation;
		startOrientation = newStartOrientation == null ? START_ORIENTATION_EDEFAULT : newStartOrientation;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BlockyPackage.LEVEL__START_ORIENTATION,
					oldStartOrientation, startOrientation));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getMaxBlocks() {
		return maxBlocks;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setMaxBlocks(int newMaxBlocks) {
		int oldMaxBlocks = maxBlocks;
		maxBlocks = newMaxBlocks;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BlockyPackage.LEVEL__MAX_BLOCKS, oldMaxBlocks,
					maxBlocks));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isAllowLoops() {
		return allowLoops;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setAllowLoops(boolean newAllowLoops) {
		boolean oldAllowLoops = allowLoops;
		allowLoops = newAllowLoops;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BlockyPackage.LEVEL__ALLOW_LOOPS, oldAllowLoops,
					allowLoops));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isAllowConditionals() {
		return allowConditionals;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setAllowConditionals(boolean newAllowConditionals) {
		boolean oldAllowConditionals = allowConditionals;
		allowConditionals = newAllowConditionals;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BlockyPackage.LEVEL__ALLOW_CONDITIONALS,
					oldAllowConditionals, allowConditionals));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Block getSolution() {
		return solution;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetSolution(Block newSolution, NotificationChain msgs) {
		Block oldSolution = solution;
		solution = newSolution;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET,
					BlockyPackage.LEVEL__SOLUTION, oldSolution, newSolution);
			if (msgs == null)
				msgs = notification;
			else
				msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSolution(Block newSolution) {
		if (newSolution != solution) {
			NotificationChain msgs = null;
			if (solution != null)
				msgs = ((InternalEObject) solution).eInverseRemove(this,
						EOPPOSITE_FEATURE_BASE - BlockyPackage.LEVEL__SOLUTION, null, msgs);
			if (newSolution != null)
				msgs = ((InternalEObject) newSolution).eInverseAdd(this,
						EOPPOSITE_FEATURE_BASE - BlockyPackage.LEVEL__SOLUTION, null, msgs);
			msgs = basicSetSolution(newSolution, msgs);
			if (msgs != null)
				msgs.dispatch();
		} else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BlockyPackage.LEVEL__SOLUTION, newSolution,
					newSolution));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<ExecutionTrace> getTraces() {
		if (traces == null) {
			traces = new EObjectContainmentEList<ExecutionTrace>(ExecutionTrace.class, this,
					BlockyPackage.LEVEL__TRACES);
		}
		return traces;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
		case BlockyPackage.LEVEL__MAP:
			return basicSetMap(null, msgs);
		case BlockyPackage.LEVEL__SOLUTION:
			return basicSetSolution(null, msgs);
		case BlockyPackage.LEVEL__TRACES:
			return ((InternalEList<?>) getTraces()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
		case BlockyPackage.LEVEL__TITLE:
			return getTitle();
		case BlockyPackage.LEVEL__ID:
			return getId();
		case BlockyPackage.LEVEL__DESCRIPTION:
			return getDescription();
		case BlockyPackage.LEVEL__MAP:
			return getMap();
		case BlockyPackage.LEVEL__START_ORIENTATION:
			return getStartOrientation();
		case BlockyPackage.LEVEL__MAX_BLOCKS:
			return getMaxBlocks();
		case BlockyPackage.LEVEL__ALLOW_LOOPS:
			return isAllowLoops();
		case BlockyPackage.LEVEL__ALLOW_CONDITIONALS:
			return isAllowConditionals();
		case BlockyPackage.LEVEL__SOLUTION:
			return getSolution();
		case BlockyPackage.LEVEL__TRACES:
			return getTraces();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
		case BlockyPackage.LEVEL__TITLE:
			setTitle((String) newValue);
			return;
		case BlockyPackage.LEVEL__ID:
			setId((Integer) newValue);
			return;
		case BlockyPackage.LEVEL__DESCRIPTION:
			setDescription((String) newValue);
			return;
		case BlockyPackage.LEVEL__MAP:
			setMap((GridMap) newValue);
			return;
		case BlockyPackage.LEVEL__START_ORIENTATION:
			setStartOrientation((Direction) newValue);
			return;
		case BlockyPackage.LEVEL__MAX_BLOCKS:
			setMaxBlocks((Integer) newValue);
			return;
		case BlockyPackage.LEVEL__ALLOW_LOOPS:
			setAllowLoops((Boolean) newValue);
			return;
		case BlockyPackage.LEVEL__ALLOW_CONDITIONALS:
			setAllowConditionals((Boolean) newValue);
			return;
		case BlockyPackage.LEVEL__SOLUTION:
			setSolution((Block) newValue);
			return;
		case BlockyPackage.LEVEL__TRACES:
			getTraces().clear();
			getTraces().addAll((Collection<? extends ExecutionTrace>) newValue);
			return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
		case BlockyPackage.LEVEL__TITLE:
			setTitle(TITLE_EDEFAULT);
			return;
		case BlockyPackage.LEVEL__ID:
			setId(ID_EDEFAULT);
			return;
		case BlockyPackage.LEVEL__DESCRIPTION:
			setDescription(DESCRIPTION_EDEFAULT);
			return;
		case BlockyPackage.LEVEL__MAP:
			setMap((GridMap) null);
			return;
		case BlockyPackage.LEVEL__START_ORIENTATION:
			setStartOrientation(START_ORIENTATION_EDEFAULT);
			return;
		case BlockyPackage.LEVEL__MAX_BLOCKS:
			setMaxBlocks(MAX_BLOCKS_EDEFAULT);
			return;
		case BlockyPackage.LEVEL__ALLOW_LOOPS:
			setAllowLoops(ALLOW_LOOPS_EDEFAULT);
			return;
		case BlockyPackage.LEVEL__ALLOW_CONDITIONALS:
			setAllowConditionals(ALLOW_CONDITIONALS_EDEFAULT);
			return;
		case BlockyPackage.LEVEL__SOLUTION:
			setSolution((Block) null);
			return;
		case BlockyPackage.LEVEL__TRACES:
			getTraces().clear();
			return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
		case BlockyPackage.LEVEL__TITLE:
			return TITLE_EDEFAULT == null ? title != null : !TITLE_EDEFAULT.equals(title);
		case BlockyPackage.LEVEL__ID:
			return id != ID_EDEFAULT;
		case BlockyPackage.LEVEL__DESCRIPTION:
			return DESCRIPTION_EDEFAULT == null ? description != null : !DESCRIPTION_EDEFAULT.equals(description);
		case BlockyPackage.LEVEL__MAP:
			return map != null;
		case BlockyPackage.LEVEL__START_ORIENTATION:
			return startOrientation != START_ORIENTATION_EDEFAULT;
		case BlockyPackage.LEVEL__MAX_BLOCKS:
			return maxBlocks != MAX_BLOCKS_EDEFAULT;
		case BlockyPackage.LEVEL__ALLOW_LOOPS:
			return allowLoops != ALLOW_LOOPS_EDEFAULT;
		case BlockyPackage.LEVEL__ALLOW_CONDITIONALS:
			return allowConditionals != ALLOW_CONDITIONALS_EDEFAULT;
		case BlockyPackage.LEVEL__SOLUTION:
			return solution != null;
		case BlockyPackage.LEVEL__TRACES:
			return traces != null && !traces.isEmpty();
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy())
			return super.toString();

		StringBuilder result = new StringBuilder(super.toString());
		result.append(" (title: ");
		result.append(title);
		result.append(", id: ");
		result.append(id);
		result.append(", description: ");
		result.append(description);
		result.append(", startOrientation: ");
		result.append(startOrientation);
		result.append(", maxBlocks: ");
		result.append(maxBlocks);
		result.append(", allowLoops: ");
		result.append(allowLoops);
		result.append(", allowConditionals: ");
		result.append(allowConditionals);
		result.append(')');
		return result.toString();
	}

} //LevelImpl
