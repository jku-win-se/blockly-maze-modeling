/**
 */
package blocky.impl;

import blocky.Block;
import blocky.BlockyPackage;
import blocky.IfStatement;
import blocky.SensorDirection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>If Statement</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link blocky.impl.IfStatementImpl#getCondition <em>Condition</em>}</li>
 *   <li>{@link blocky.impl.IfStatementImpl#getThenBranch <em>Then Branch</em>}</li>
 *   <li>{@link blocky.impl.IfStatementImpl#getElseBranch <em>Else Branch</em>}</li>
 * </ul>
 *
 * @generated
 */
public class IfStatementImpl extends BlockImpl implements IfStatement {
	/**
	 * The default value of the '{@link #getCondition() <em>Condition</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCondition()
	 * @generated
	 * @ordered
	 */
	protected static final SensorDirection CONDITION_EDEFAULT = SensorDirection.AHEAD;

	/**
	 * The cached value of the '{@link #getCondition() <em>Condition</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCondition()
	 * @generated
	 * @ordered
	 */
	protected SensorDirection condition = CONDITION_EDEFAULT;

	/**
	 * The cached value of the '{@link #getThenBranch() <em>Then Branch</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getThenBranch()
	 * @generated
	 * @ordered
	 */
	protected Block thenBranch;

	/**
	 * The cached value of the '{@link #getElseBranch() <em>Else Branch</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getElseBranch()
	 * @generated
	 * @ordered
	 */
	protected Block elseBranch;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected IfStatementImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return BlockyPackage.Literals.IF_STATEMENT;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public SensorDirection getCondition() {
		return condition;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setCondition(SensorDirection newCondition) {
		SensorDirection oldCondition = condition;
		condition = newCondition == null ? CONDITION_EDEFAULT : newCondition;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BlockyPackage.IF_STATEMENT__CONDITION, oldCondition,
					condition));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Block getThenBranch() {
		return thenBranch;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetThenBranch(Block newThenBranch, NotificationChain msgs) {
		Block oldThenBranch = thenBranch;
		thenBranch = newThenBranch;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET,
					BlockyPackage.IF_STATEMENT__THEN_BRANCH, oldThenBranch, newThenBranch);
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
	public void setThenBranch(Block newThenBranch) {
		if (newThenBranch != thenBranch) {
			NotificationChain msgs = null;
			if (thenBranch != null)
				msgs = ((InternalEObject) thenBranch).eInverseRemove(this,
						EOPPOSITE_FEATURE_BASE - BlockyPackage.IF_STATEMENT__THEN_BRANCH, null, msgs);
			if (newThenBranch != null)
				msgs = ((InternalEObject) newThenBranch).eInverseAdd(this,
						EOPPOSITE_FEATURE_BASE - BlockyPackage.IF_STATEMENT__THEN_BRANCH, null, msgs);
			msgs = basicSetThenBranch(newThenBranch, msgs);
			if (msgs != null)
				msgs.dispatch();
		} else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BlockyPackage.IF_STATEMENT__THEN_BRANCH,
					newThenBranch, newThenBranch));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Block getElseBranch() {
		return elseBranch;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetElseBranch(Block newElseBranch, NotificationChain msgs) {
		Block oldElseBranch = elseBranch;
		elseBranch = newElseBranch;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET,
					BlockyPackage.IF_STATEMENT__ELSE_BRANCH, oldElseBranch, newElseBranch);
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
	public void setElseBranch(Block newElseBranch) {
		if (newElseBranch != elseBranch) {
			NotificationChain msgs = null;
			if (elseBranch != null)
				msgs = ((InternalEObject) elseBranch).eInverseRemove(this,
						EOPPOSITE_FEATURE_BASE - BlockyPackage.IF_STATEMENT__ELSE_BRANCH, null, msgs);
			if (newElseBranch != null)
				msgs = ((InternalEObject) newElseBranch).eInverseAdd(this,
						EOPPOSITE_FEATURE_BASE - BlockyPackage.IF_STATEMENT__ELSE_BRANCH, null, msgs);
			msgs = basicSetElseBranch(newElseBranch, msgs);
			if (msgs != null)
				msgs.dispatch();
		} else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BlockyPackage.IF_STATEMENT__ELSE_BRANCH,
					newElseBranch, newElseBranch));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
		case BlockyPackage.IF_STATEMENT__THEN_BRANCH:
			return basicSetThenBranch(null, msgs);
		case BlockyPackage.IF_STATEMENT__ELSE_BRANCH:
			return basicSetElseBranch(null, msgs);
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
		case BlockyPackage.IF_STATEMENT__CONDITION:
			return getCondition();
		case BlockyPackage.IF_STATEMENT__THEN_BRANCH:
			return getThenBranch();
		case BlockyPackage.IF_STATEMENT__ELSE_BRANCH:
			return getElseBranch();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
		case BlockyPackage.IF_STATEMENT__CONDITION:
			setCondition((SensorDirection) newValue);
			return;
		case BlockyPackage.IF_STATEMENT__THEN_BRANCH:
			setThenBranch((Block) newValue);
			return;
		case BlockyPackage.IF_STATEMENT__ELSE_BRANCH:
			setElseBranch((Block) newValue);
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
		case BlockyPackage.IF_STATEMENT__CONDITION:
			setCondition(CONDITION_EDEFAULT);
			return;
		case BlockyPackage.IF_STATEMENT__THEN_BRANCH:
			setThenBranch((Block) null);
			return;
		case BlockyPackage.IF_STATEMENT__ELSE_BRANCH:
			setElseBranch((Block) null);
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
		case BlockyPackage.IF_STATEMENT__CONDITION:
			return condition != CONDITION_EDEFAULT;
		case BlockyPackage.IF_STATEMENT__THEN_BRANCH:
			return thenBranch != null;
		case BlockyPackage.IF_STATEMENT__ELSE_BRANCH:
			return elseBranch != null;
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
		result.append(" (condition: ");
		result.append(condition);
		result.append(')');
		return result.toString();
	}

} //IfStatementImpl
