/**
 */
package blocky.impl;

import blocky.BlockyPackage;
import blocky.Body;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Body</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link blocky.impl.BodyImpl#getFirstContainer <em>First Container</em>}</li>
 * </ul>
 *
 * @generated
 */
public class BodyImpl extends MinimalEObjectImpl.Container implements Body {
	/**
	 * The cached value of the '{@link #getFirstContainer() <em>First Container</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFirstContainer()
	 * @generated
	 * @ordered
	 */
	protected blocky.Container firstContainer;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected BodyImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return BlockyPackage.Literals.BODY;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public blocky.Container getFirstContainer() {
		return firstContainer;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetFirstContainer(blocky.Container newFirstContainer, NotificationChain msgs) {
		blocky.Container oldFirstContainer = firstContainer;
		firstContainer = newFirstContainer;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET,
					BlockyPackage.BODY__FIRST_CONTAINER, oldFirstContainer, newFirstContainer);
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
	public void setFirstContainer(blocky.Container newFirstContainer) {
		if (newFirstContainer != firstContainer) {
			NotificationChain msgs = null;
			if (firstContainer != null)
				msgs = ((InternalEObject) firstContainer).eInverseRemove(this,
						EOPPOSITE_FEATURE_BASE - BlockyPackage.BODY__FIRST_CONTAINER, null, msgs);
			if (newFirstContainer != null)
				msgs = ((InternalEObject) newFirstContainer).eInverseAdd(this,
						EOPPOSITE_FEATURE_BASE - BlockyPackage.BODY__FIRST_CONTAINER, null, msgs);
			msgs = basicSetFirstContainer(newFirstContainer, msgs);
			if (msgs != null)
				msgs.dispatch();
		} else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BlockyPackage.BODY__FIRST_CONTAINER,
					newFirstContainer, newFirstContainer));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
		case BlockyPackage.BODY__FIRST_CONTAINER:
			return basicSetFirstContainer(null, msgs);
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
		case BlockyPackage.BODY__FIRST_CONTAINER:
			return getFirstContainer();
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
		case BlockyPackage.BODY__FIRST_CONTAINER:
			setFirstContainer((blocky.Container) newValue);
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
		case BlockyPackage.BODY__FIRST_CONTAINER:
			setFirstContainer((blocky.Container) null);
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
		case BlockyPackage.BODY__FIRST_CONTAINER:
			return firstContainer != null;
		}
		return super.eIsSet(featureID);
	}

} //BodyImpl
