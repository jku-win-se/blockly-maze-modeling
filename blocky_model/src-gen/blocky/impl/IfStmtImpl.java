/**
 */
package blocky.impl;

import blocky.BlockyPackage;
import blocky.Body;
import blocky.ConditionKind;
import blocky.IfStmt;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>If Stmt</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link blocky.impl.IfStmtImpl#getCondition <em>Condition</em>}</li>
 *   <li>{@link blocky.impl.IfStmtImpl#getThenBody <em>Then Body</em>}</li>
 *   <li>{@link blocky.impl.IfStmtImpl#getElseBody <em>Else Body</em>}</li>
 * </ul>
 *
 * @generated
 */
public class IfStmtImpl extends StatementImpl implements IfStmt {
	/**
	 * The default value of the '{@link #getCondition() <em>Condition</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCondition()
	 * @generated
	 * @ordered
	 */
	protected static final ConditionKind CONDITION_EDEFAULT = ConditionKind.CHECK_FORWARD;

	/**
	 * The cached value of the '{@link #getCondition() <em>Condition</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCondition()
	 * @generated
	 * @ordered
	 */
	protected ConditionKind condition = CONDITION_EDEFAULT;

	/**
	 * The cached value of the '{@link #getThenBody() <em>Then Body</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getThenBody()
	 * @generated
	 * @ordered
	 */
	protected Body thenBody;

	/**
	 * The cached value of the '{@link #getElseBody() <em>Else Body</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getElseBody()
	 * @generated
	 * @ordered
	 */
	protected Body elseBody;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected IfStmtImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return BlockyPackage.Literals.IF_STMT;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ConditionKind getCondition() {
		return condition;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setCondition(ConditionKind newCondition) {
		ConditionKind oldCondition = condition;
		condition = newCondition == null ? CONDITION_EDEFAULT : newCondition;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BlockyPackage.IF_STMT__CONDITION, oldCondition,
					condition));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Body getThenBody() {
		return thenBody;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetThenBody(Body newThenBody, NotificationChain msgs) {
		Body oldThenBody = thenBody;
		thenBody = newThenBody;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET,
					BlockyPackage.IF_STMT__THEN_BODY, oldThenBody, newThenBody);
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
	public void setThenBody(Body newThenBody) {
		if (newThenBody != thenBody) {
			NotificationChain msgs = null;
			if (thenBody != null)
				msgs = ((InternalEObject) thenBody).eInverseRemove(this,
						EOPPOSITE_FEATURE_BASE - BlockyPackage.IF_STMT__THEN_BODY, null, msgs);
			if (newThenBody != null)
				msgs = ((InternalEObject) newThenBody).eInverseAdd(this,
						EOPPOSITE_FEATURE_BASE - BlockyPackage.IF_STMT__THEN_BODY, null, msgs);
			msgs = basicSetThenBody(newThenBody, msgs);
			if (msgs != null)
				msgs.dispatch();
		} else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BlockyPackage.IF_STMT__THEN_BODY, newThenBody,
					newThenBody));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Body getElseBody() {
		return elseBody;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetElseBody(Body newElseBody, NotificationChain msgs) {
		Body oldElseBody = elseBody;
		elseBody = newElseBody;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET,
					BlockyPackage.IF_STMT__ELSE_BODY, oldElseBody, newElseBody);
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
	public void setElseBody(Body newElseBody) {
		if (newElseBody != elseBody) {
			NotificationChain msgs = null;
			if (elseBody != null)
				msgs = ((InternalEObject) elseBody).eInverseRemove(this,
						EOPPOSITE_FEATURE_BASE - BlockyPackage.IF_STMT__ELSE_BODY, null, msgs);
			if (newElseBody != null)
				msgs = ((InternalEObject) newElseBody).eInverseAdd(this,
						EOPPOSITE_FEATURE_BASE - BlockyPackage.IF_STMT__ELSE_BODY, null, msgs);
			msgs = basicSetElseBody(newElseBody, msgs);
			if (msgs != null)
				msgs.dispatch();
		} else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BlockyPackage.IF_STMT__ELSE_BODY, newElseBody,
					newElseBody));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
		case BlockyPackage.IF_STMT__THEN_BODY:
			return basicSetThenBody(null, msgs);
		case BlockyPackage.IF_STMT__ELSE_BODY:
			return basicSetElseBody(null, msgs);
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
		case BlockyPackage.IF_STMT__CONDITION:
			return getCondition();
		case BlockyPackage.IF_STMT__THEN_BODY:
			return getThenBody();
		case BlockyPackage.IF_STMT__ELSE_BODY:
			return getElseBody();
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
		case BlockyPackage.IF_STMT__CONDITION:
			setCondition((ConditionKind) newValue);
			return;
		case BlockyPackage.IF_STMT__THEN_BODY:
			setThenBody((Body) newValue);
			return;
		case BlockyPackage.IF_STMT__ELSE_BODY:
			setElseBody((Body) newValue);
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
		case BlockyPackage.IF_STMT__CONDITION:
			setCondition(CONDITION_EDEFAULT);
			return;
		case BlockyPackage.IF_STMT__THEN_BODY:
			setThenBody((Body) null);
			return;
		case BlockyPackage.IF_STMT__ELSE_BODY:
			setElseBody((Body) null);
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
		case BlockyPackage.IF_STMT__CONDITION:
			return condition != CONDITION_EDEFAULT;
		case BlockyPackage.IF_STMT__THEN_BODY:
			return thenBody != null;
		case BlockyPackage.IF_STMT__ELSE_BODY:
			return elseBody != null;
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

} //IfStmtImpl
