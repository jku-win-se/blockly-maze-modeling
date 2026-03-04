/**
 */
package blocky.impl;

import blocky.Block;
import blocky.BlockyPackage;
import blocky.Cell;
import blocky.Direction;
import blocky.GameState;
import blocky.GameStatus;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Game State</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link blocky.impl.GameStateImpl#getStep <em>Step</em>}</li>
 *   <li>{@link blocky.impl.GameStateImpl#getPosition <em>Position</em>}</li>
 *   <li>{@link blocky.impl.GameStateImpl#getOrientation <em>Orientation</em>}</li>
 *   <li>{@link blocky.impl.GameStateImpl#getStatus <em>Status</em>}</li>
 *   <li>{@link blocky.impl.GameStateImpl#getExecutingBlock <em>Executing Block</em>}</li>
 *   <li>{@link blocky.impl.GameStateImpl#getNext <em>Next</em>}</li>
 *   <li>{@link blocky.impl.GameStateImpl#getPrevious <em>Previous</em>}</li>
 * </ul>
 *
 * @generated
 */
public class GameStateImpl extends MinimalEObjectImpl.Container implements GameState {
	/**
	 * The default value of the '{@link #getStep() <em>Step</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStep()
	 * @generated
	 * @ordered
	 */
	protected static final int STEP_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getStep() <em>Step</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStep()
	 * @generated
	 * @ordered
	 */
	protected int step = STEP_EDEFAULT;

	/**
	 * The cached value of the '{@link #getPosition() <em>Position</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPosition()
	 * @generated
	 * @ordered
	 */
	protected Cell position;

	/**
	 * The default value of the '{@link #getOrientation() <em>Orientation</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOrientation()
	 * @generated
	 * @ordered
	 */
	protected static final Direction ORIENTATION_EDEFAULT = Direction.NORTH;

	/**
	 * The cached value of the '{@link #getOrientation() <em>Orientation</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOrientation()
	 * @generated
	 * @ordered
	 */
	protected Direction orientation = ORIENTATION_EDEFAULT;

	/**
	 * The default value of the '{@link #getStatus() <em>Status</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStatus()
	 * @generated
	 * @ordered
	 */
	protected static final GameStatus STATUS_EDEFAULT = GameStatus.RUNNING;

	/**
	 * The cached value of the '{@link #getStatus() <em>Status</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStatus()
	 * @generated
	 * @ordered
	 */
	protected GameStatus status = STATUS_EDEFAULT;

	/**
	 * The cached value of the '{@link #getExecutingBlock() <em>Executing Block</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getExecutingBlock()
	 * @generated
	 * @ordered
	 */
	protected Block executingBlock;

	/**
	 * The cached value of the '{@link #getNext() <em>Next</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNext()
	 * @generated
	 * @ordered
	 */
	protected GameState next;

	/**
	 * The cached value of the '{@link #getPrevious() <em>Previous</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPrevious()
	 * @generated
	 * @ordered
	 */
	protected GameState previous;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected GameStateImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return BlockyPackage.Literals.GAME_STATE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getStep() {
		return step;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setStep(int newStep) {
		int oldStep = step;
		step = newStep;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BlockyPackage.GAME_STATE__STEP, oldStep, step));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Cell getPosition() {
		if (position != null && position.eIsProxy()) {
			InternalEObject oldPosition = (InternalEObject) position;
			position = (Cell) eResolveProxy(oldPosition);
			if (position != oldPosition) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, BlockyPackage.GAME_STATE__POSITION,
							oldPosition, position));
			}
		}
		return position;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Cell basicGetPosition() {
		return position;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setPosition(Cell newPosition) {
		Cell oldPosition = position;
		position = newPosition;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BlockyPackage.GAME_STATE__POSITION, oldPosition,
					position));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Direction getOrientation() {
		return orientation;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setOrientation(Direction newOrientation) {
		Direction oldOrientation = orientation;
		orientation = newOrientation == null ? ORIENTATION_EDEFAULT : newOrientation;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BlockyPackage.GAME_STATE__ORIENTATION, oldOrientation,
					orientation));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GameStatus getStatus() {
		return status;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setStatus(GameStatus newStatus) {
		GameStatus oldStatus = status;
		status = newStatus == null ? STATUS_EDEFAULT : newStatus;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BlockyPackage.GAME_STATE__STATUS, oldStatus, status));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Block getExecutingBlock() {
		if (executingBlock != null && executingBlock.eIsProxy()) {
			InternalEObject oldExecutingBlock = (InternalEObject) executingBlock;
			executingBlock = (Block) eResolveProxy(oldExecutingBlock);
			if (executingBlock != oldExecutingBlock) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, BlockyPackage.GAME_STATE__EXECUTING_BLOCK,
							oldExecutingBlock, executingBlock));
			}
		}
		return executingBlock;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Block basicGetExecutingBlock() {
		return executingBlock;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setExecutingBlock(Block newExecutingBlock) {
		Block oldExecutingBlock = executingBlock;
		executingBlock = newExecutingBlock;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BlockyPackage.GAME_STATE__EXECUTING_BLOCK,
					oldExecutingBlock, executingBlock));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GameState getNext() {
		if (next != null && next.eIsProxy()) {
			InternalEObject oldNext = (InternalEObject) next;
			next = (GameState) eResolveProxy(oldNext);
			if (next != oldNext) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, BlockyPackage.GAME_STATE__NEXT, oldNext,
							next));
			}
		}
		return next;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public GameState basicGetNext() {
		return next;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetNext(GameState newNext, NotificationChain msgs) {
		GameState oldNext = next;
		next = newNext;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET,
					BlockyPackage.GAME_STATE__NEXT, oldNext, newNext);
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
	public void setNext(GameState newNext) {
		if (newNext != next) {
			NotificationChain msgs = null;
			if (next != null)
				msgs = ((InternalEObject) next).eInverseRemove(this, BlockyPackage.GAME_STATE__PREVIOUS,
						GameState.class, msgs);
			if (newNext != null)
				msgs = ((InternalEObject) newNext).eInverseAdd(this, BlockyPackage.GAME_STATE__PREVIOUS,
						GameState.class, msgs);
			msgs = basicSetNext(newNext, msgs);
			if (msgs != null)
				msgs.dispatch();
		} else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BlockyPackage.GAME_STATE__NEXT, newNext, newNext));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GameState getPrevious() {
		if (previous != null && previous.eIsProxy()) {
			InternalEObject oldPrevious = (InternalEObject) previous;
			previous = (GameState) eResolveProxy(oldPrevious);
			if (previous != oldPrevious) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, BlockyPackage.GAME_STATE__PREVIOUS,
							oldPrevious, previous));
			}
		}
		return previous;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public GameState basicGetPrevious() {
		return previous;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetPrevious(GameState newPrevious, NotificationChain msgs) {
		GameState oldPrevious = previous;
		previous = newPrevious;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET,
					BlockyPackage.GAME_STATE__PREVIOUS, oldPrevious, newPrevious);
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
	public void setPrevious(GameState newPrevious) {
		if (newPrevious != previous) {
			NotificationChain msgs = null;
			if (previous != null)
				msgs = ((InternalEObject) previous).eInverseRemove(this, BlockyPackage.GAME_STATE__NEXT,
						GameState.class, msgs);
			if (newPrevious != null)
				msgs = ((InternalEObject) newPrevious).eInverseAdd(this, BlockyPackage.GAME_STATE__NEXT,
						GameState.class, msgs);
			msgs = basicSetPrevious(newPrevious, msgs);
			if (msgs != null)
				msgs.dispatch();
		} else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BlockyPackage.GAME_STATE__PREVIOUS, newPrevious,
					newPrevious));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
		case BlockyPackage.GAME_STATE__NEXT:
			if (next != null)
				msgs = ((InternalEObject) next).eInverseRemove(this, BlockyPackage.GAME_STATE__PREVIOUS,
						GameState.class, msgs);
			return basicSetNext((GameState) otherEnd, msgs);
		case BlockyPackage.GAME_STATE__PREVIOUS:
			if (previous != null)
				msgs = ((InternalEObject) previous).eInverseRemove(this, BlockyPackage.GAME_STATE__NEXT,
						GameState.class, msgs);
			return basicSetPrevious((GameState) otherEnd, msgs);
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
		case BlockyPackage.GAME_STATE__NEXT:
			return basicSetNext(null, msgs);
		case BlockyPackage.GAME_STATE__PREVIOUS:
			return basicSetPrevious(null, msgs);
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
		case BlockyPackage.GAME_STATE__STEP:
			return getStep();
		case BlockyPackage.GAME_STATE__POSITION:
			if (resolve)
				return getPosition();
			return basicGetPosition();
		case BlockyPackage.GAME_STATE__ORIENTATION:
			return getOrientation();
		case BlockyPackage.GAME_STATE__STATUS:
			return getStatus();
		case BlockyPackage.GAME_STATE__EXECUTING_BLOCK:
			if (resolve)
				return getExecutingBlock();
			return basicGetExecutingBlock();
		case BlockyPackage.GAME_STATE__NEXT:
			if (resolve)
				return getNext();
			return basicGetNext();
		case BlockyPackage.GAME_STATE__PREVIOUS:
			if (resolve)
				return getPrevious();
			return basicGetPrevious();
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
		case BlockyPackage.GAME_STATE__STEP:
			setStep((Integer) newValue);
			return;
		case BlockyPackage.GAME_STATE__POSITION:
			setPosition((Cell) newValue);
			return;
		case BlockyPackage.GAME_STATE__ORIENTATION:
			setOrientation((Direction) newValue);
			return;
		case BlockyPackage.GAME_STATE__STATUS:
			setStatus((GameStatus) newValue);
			return;
		case BlockyPackage.GAME_STATE__EXECUTING_BLOCK:
			setExecutingBlock((Block) newValue);
			return;
		case BlockyPackage.GAME_STATE__NEXT:
			setNext((GameState) newValue);
			return;
		case BlockyPackage.GAME_STATE__PREVIOUS:
			setPrevious((GameState) newValue);
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
		case BlockyPackage.GAME_STATE__STEP:
			setStep(STEP_EDEFAULT);
			return;
		case BlockyPackage.GAME_STATE__POSITION:
			setPosition((Cell) null);
			return;
		case BlockyPackage.GAME_STATE__ORIENTATION:
			setOrientation(ORIENTATION_EDEFAULT);
			return;
		case BlockyPackage.GAME_STATE__STATUS:
			setStatus(STATUS_EDEFAULT);
			return;
		case BlockyPackage.GAME_STATE__EXECUTING_BLOCK:
			setExecutingBlock((Block) null);
			return;
		case BlockyPackage.GAME_STATE__NEXT:
			setNext((GameState) null);
			return;
		case BlockyPackage.GAME_STATE__PREVIOUS:
			setPrevious((GameState) null);
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
		case BlockyPackage.GAME_STATE__STEP:
			return step != STEP_EDEFAULT;
		case BlockyPackage.GAME_STATE__POSITION:
			return position != null;
		case BlockyPackage.GAME_STATE__ORIENTATION:
			return orientation != ORIENTATION_EDEFAULT;
		case BlockyPackage.GAME_STATE__STATUS:
			return status != STATUS_EDEFAULT;
		case BlockyPackage.GAME_STATE__EXECUTING_BLOCK:
			return executingBlock != null;
		case BlockyPackage.GAME_STATE__NEXT:
			return next != null;
		case BlockyPackage.GAME_STATE__PREVIOUS:
			return previous != null;
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
		result.append(" (step: ");
		result.append(step);
		result.append(", orientation: ");
		result.append(orientation);
		result.append(", status: ");
		result.append(status);
		result.append(')');
		return result.toString();
	}

} //GameStateImpl
