/**
 */
package blocky.impl;

import blocky.BlockyPackage;
import blocky.Cell;
import blocky.CellType;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Cell</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link blocky.impl.CellImpl#getX <em>X</em>}</li>
 *   <li>{@link blocky.impl.CellImpl#getY <em>Y</em>}</li>
 *   <li>{@link blocky.impl.CellImpl#getType <em>Type</em>}</li>
 *   <li>{@link blocky.impl.CellImpl#getTop <em>Top</em>}</li>
 *   <li>{@link blocky.impl.CellImpl#getBottom <em>Bottom</em>}</li>
 *   <li>{@link blocky.impl.CellImpl#getLeft <em>Left</em>}</li>
 *   <li>{@link blocky.impl.CellImpl#getRight <em>Right</em>}</li>
 * </ul>
 *
 * @generated
 */
public class CellImpl extends MinimalEObjectImpl.Container implements Cell {
	/**
	 * The default value of the '{@link #getX() <em>X</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getX()
	 * @generated
	 * @ordered
	 */
	protected static final int X_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getX() <em>X</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getX()
	 * @generated
	 * @ordered
	 */
	protected int x = X_EDEFAULT;

	/**
	 * The default value of the '{@link #getY() <em>Y</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getY()
	 * @generated
	 * @ordered
	 */
	protected static final int Y_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getY() <em>Y</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getY()
	 * @generated
	 * @ordered
	 */
	protected int y = Y_EDEFAULT;

	/**
	 * The default value of the '{@link #getType() <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected static final CellType TYPE_EDEFAULT = CellType.EMPTY;

	/**
	 * The cached value of the '{@link #getType() <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected CellType type = TYPE_EDEFAULT;

	/**
	 * The cached value of the '{@link #getTop() <em>Top</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTop()
	 * @generated
	 * @ordered
	 */
	protected Cell top;

	/**
	 * The cached value of the '{@link #getBottom() <em>Bottom</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBottom()
	 * @generated
	 * @ordered
	 */
	protected Cell bottom;

	/**
	 * The cached value of the '{@link #getLeft() <em>Left</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLeft()
	 * @generated
	 * @ordered
	 */
	protected Cell left;

	/**
	 * The cached value of the '{@link #getRight() <em>Right</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRight()
	 * @generated
	 * @ordered
	 */
	protected Cell right;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected CellImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return BlockyPackage.Literals.CELL;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getX() {
		return x;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setX(int newX) {
		int oldX = x;
		x = newX;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BlockyPackage.CELL__X, oldX, x));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getY() {
		return y;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setY(int newY) {
		int oldY = y;
		y = newY;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BlockyPackage.CELL__Y, oldY, y));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public CellType getType() {
		return type;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setType(CellType newType) {
		CellType oldType = type;
		type = newType == null ? TYPE_EDEFAULT : newType;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BlockyPackage.CELL__TYPE, oldType, type));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Cell getTop() {
		if (top != null && top.eIsProxy()) {
			InternalEObject oldTop = (InternalEObject) top;
			top = (Cell) eResolveProxy(oldTop);
			if (top != oldTop) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, BlockyPackage.CELL__TOP, oldTop, top));
			}
		}
		return top;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Cell basicGetTop() {
		return top;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetTop(Cell newTop, NotificationChain msgs) {
		Cell oldTop = top;
		top = newTop;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, BlockyPackage.CELL__TOP,
					oldTop, newTop);
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
	public void setTop(Cell newTop) {
		if (newTop != top) {
			NotificationChain msgs = null;
			if (top != null)
				msgs = ((InternalEObject) top).eInverseRemove(this, BlockyPackage.CELL__BOTTOM, Cell.class, msgs);
			if (newTop != null)
				msgs = ((InternalEObject) newTop).eInverseAdd(this, BlockyPackage.CELL__BOTTOM, Cell.class, msgs);
			msgs = basicSetTop(newTop, msgs);
			if (msgs != null)
				msgs.dispatch();
		} else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BlockyPackage.CELL__TOP, newTop, newTop));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Cell getBottom() {
		if (bottom != null && bottom.eIsProxy()) {
			InternalEObject oldBottom = (InternalEObject) bottom;
			bottom = (Cell) eResolveProxy(oldBottom);
			if (bottom != oldBottom) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, BlockyPackage.CELL__BOTTOM, oldBottom,
							bottom));
			}
		}
		return bottom;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Cell basicGetBottom() {
		return bottom;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetBottom(Cell newBottom, NotificationChain msgs) {
		Cell oldBottom = bottom;
		bottom = newBottom;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, BlockyPackage.CELL__BOTTOM,
					oldBottom, newBottom);
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
	public void setBottom(Cell newBottom) {
		if (newBottom != bottom) {
			NotificationChain msgs = null;
			if (bottom != null)
				msgs = ((InternalEObject) bottom).eInverseRemove(this, BlockyPackage.CELL__TOP, Cell.class, msgs);
			if (newBottom != null)
				msgs = ((InternalEObject) newBottom).eInverseAdd(this, BlockyPackage.CELL__TOP, Cell.class, msgs);
			msgs = basicSetBottom(newBottom, msgs);
			if (msgs != null)
				msgs.dispatch();
		} else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BlockyPackage.CELL__BOTTOM, newBottom, newBottom));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Cell getLeft() {
		if (left != null && left.eIsProxy()) {
			InternalEObject oldLeft = (InternalEObject) left;
			left = (Cell) eResolveProxy(oldLeft);
			if (left != oldLeft) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, BlockyPackage.CELL__LEFT, oldLeft, left));
			}
		}
		return left;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Cell basicGetLeft() {
		return left;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetLeft(Cell newLeft, NotificationChain msgs) {
		Cell oldLeft = left;
		left = newLeft;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, BlockyPackage.CELL__LEFT,
					oldLeft, newLeft);
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
	public void setLeft(Cell newLeft) {
		if (newLeft != left) {
			NotificationChain msgs = null;
			if (left != null)
				msgs = ((InternalEObject) left).eInverseRemove(this, BlockyPackage.CELL__RIGHT, Cell.class, msgs);
			if (newLeft != null)
				msgs = ((InternalEObject) newLeft).eInverseAdd(this, BlockyPackage.CELL__RIGHT, Cell.class, msgs);
			msgs = basicSetLeft(newLeft, msgs);
			if (msgs != null)
				msgs.dispatch();
		} else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BlockyPackage.CELL__LEFT, newLeft, newLeft));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Cell getRight() {
		if (right != null && right.eIsProxy()) {
			InternalEObject oldRight = (InternalEObject) right;
			right = (Cell) eResolveProxy(oldRight);
			if (right != oldRight) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, BlockyPackage.CELL__RIGHT, oldRight,
							right));
			}
		}
		return right;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Cell basicGetRight() {
		return right;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetRight(Cell newRight, NotificationChain msgs) {
		Cell oldRight = right;
		right = newRight;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, BlockyPackage.CELL__RIGHT,
					oldRight, newRight);
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
	public void setRight(Cell newRight) {
		if (newRight != right) {
			NotificationChain msgs = null;
			if (right != null)
				msgs = ((InternalEObject) right).eInverseRemove(this, BlockyPackage.CELL__LEFT, Cell.class, msgs);
			if (newRight != null)
				msgs = ((InternalEObject) newRight).eInverseAdd(this, BlockyPackage.CELL__LEFT, Cell.class, msgs);
			msgs = basicSetRight(newRight, msgs);
			if (msgs != null)
				msgs.dispatch();
		} else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BlockyPackage.CELL__RIGHT, newRight, newRight));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
		case BlockyPackage.CELL__TOP:
			if (top != null)
				msgs = ((InternalEObject) top).eInverseRemove(this, BlockyPackage.CELL__BOTTOM, Cell.class, msgs);
			return basicSetTop((Cell) otherEnd, msgs);
		case BlockyPackage.CELL__BOTTOM:
			if (bottom != null)
				msgs = ((InternalEObject) bottom).eInverseRemove(this, BlockyPackage.CELL__TOP, Cell.class, msgs);
			return basicSetBottom((Cell) otherEnd, msgs);
		case BlockyPackage.CELL__LEFT:
			if (left != null)
				msgs = ((InternalEObject) left).eInverseRemove(this, BlockyPackage.CELL__RIGHT, Cell.class, msgs);
			return basicSetLeft((Cell) otherEnd, msgs);
		case BlockyPackage.CELL__RIGHT:
			if (right != null)
				msgs = ((InternalEObject) right).eInverseRemove(this, BlockyPackage.CELL__LEFT, Cell.class, msgs);
			return basicSetRight((Cell) otherEnd, msgs);
		}
		return super.eInverseAdd(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
		case BlockyPackage.CELL__TOP:
			return basicSetTop(null, msgs);
		case BlockyPackage.CELL__BOTTOM:
			return basicSetBottom(null, msgs);
		case BlockyPackage.CELL__LEFT:
			return basicSetLeft(null, msgs);
		case BlockyPackage.CELL__RIGHT:
			return basicSetRight(null, msgs);
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
		case BlockyPackage.CELL__X:
			return getX();
		case BlockyPackage.CELL__Y:
			return getY();
		case BlockyPackage.CELL__TYPE:
			return getType();
		case BlockyPackage.CELL__TOP:
			if (resolve)
				return getTop();
			return basicGetTop();
		case BlockyPackage.CELL__BOTTOM:
			if (resolve)
				return getBottom();
			return basicGetBottom();
		case BlockyPackage.CELL__LEFT:
			if (resolve)
				return getLeft();
			return basicGetLeft();
		case BlockyPackage.CELL__RIGHT:
			if (resolve)
				return getRight();
			return basicGetRight();
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
		case BlockyPackage.CELL__X:
			setX((Integer) newValue);
			return;
		case BlockyPackage.CELL__Y:
			setY((Integer) newValue);
			return;
		case BlockyPackage.CELL__TYPE:
			setType((CellType) newValue);
			return;
		case BlockyPackage.CELL__TOP:
			setTop((Cell) newValue);
			return;
		case BlockyPackage.CELL__BOTTOM:
			setBottom((Cell) newValue);
			return;
		case BlockyPackage.CELL__LEFT:
			setLeft((Cell) newValue);
			return;
		case BlockyPackage.CELL__RIGHT:
			setRight((Cell) newValue);
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
		case BlockyPackage.CELL__X:
			setX(X_EDEFAULT);
			return;
		case BlockyPackage.CELL__Y:
			setY(Y_EDEFAULT);
			return;
		case BlockyPackage.CELL__TYPE:
			setType(TYPE_EDEFAULT);
			return;
		case BlockyPackage.CELL__TOP:
			setTop((Cell) null);
			return;
		case BlockyPackage.CELL__BOTTOM:
			setBottom((Cell) null);
			return;
		case BlockyPackage.CELL__LEFT:
			setLeft((Cell) null);
			return;
		case BlockyPackage.CELL__RIGHT:
			setRight((Cell) null);
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
		case BlockyPackage.CELL__X:
			return x != X_EDEFAULT;
		case BlockyPackage.CELL__Y:
			return y != Y_EDEFAULT;
		case BlockyPackage.CELL__TYPE:
			return type != TYPE_EDEFAULT;
		case BlockyPackage.CELL__TOP:
			return top != null;
		case BlockyPackage.CELL__BOTTOM:
			return bottom != null;
		case BlockyPackage.CELL__LEFT:
			return left != null;
		case BlockyPackage.CELL__RIGHT:
			return right != null;
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
		result.append(" (x: ");
		result.append(x);
		result.append(", y: ");
		result.append(y);
		result.append(", type: ");
		result.append(type);
		result.append(')');
		return result.toString();
	}

} //CellImpl
