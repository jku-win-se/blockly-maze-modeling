/**
 */
package blocky;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each operation of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see blocky.BlockyFactory
 * @model kind="package"
 * @generated
 */
public interface BlockyPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "blocky";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://www.example.org/blocky";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "blocky";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	BlockyPackage eINSTANCE = blocky.impl.BlockyPackageImpl.init();

	/**
	 * The meta object id for the '{@link blocky.impl.LevelImpl <em>Level</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see blocky.impl.LevelImpl
	 * @see blocky.impl.BlockyPackageImpl#getLevel()
	 * @generated
	 */
	int LEVEL = 0;

	/**
	 * The feature id for the '<em><b>Title</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LEVEL__TITLE = 0;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LEVEL__ID = 1;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LEVEL__DESCRIPTION = 2;

	/**
	 * The feature id for the '<em><b>Map</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LEVEL__MAP = 3;

	/**
	 * The feature id for the '<em><b>Start Orientation</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LEVEL__START_ORIENTATION = 4;

	/**
	 * The feature id for the '<em><b>Max Blocks</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LEVEL__MAX_BLOCKS = 5;

	/**
	 * The feature id for the '<em><b>Allow Loops</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LEVEL__ALLOW_LOOPS = 6;

	/**
	 * The feature id for the '<em><b>Allow Conditionals</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LEVEL__ALLOW_CONDITIONALS = 7;

	/**
	 * The feature id for the '<em><b>Solution</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LEVEL__SOLUTION = 8;

	/**
	 * The feature id for the '<em><b>Traces</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LEVEL__TRACES = 9;

	/**
	 * The number of structural features of the '<em>Level</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LEVEL_FEATURE_COUNT = 10;

	/**
	 * The number of operations of the '<em>Level</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LEVEL_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link blocky.impl.GridMapImpl <em>Grid Map</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see blocky.impl.GridMapImpl
	 * @see blocky.impl.BlockyPackageImpl#getGridMap()
	 * @generated
	 */
	int GRID_MAP = 1;

	/**
	 * The feature id for the '<em><b>Width</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GRID_MAP__WIDTH = 0;

	/**
	 * The feature id for the '<em><b>Height</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GRID_MAP__HEIGHT = 1;

	/**
	 * The feature id for the '<em><b>Cells</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GRID_MAP__CELLS = 2;

	/**
	 * The number of structural features of the '<em>Grid Map</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GRID_MAP_FEATURE_COUNT = 3;

	/**
	 * The number of operations of the '<em>Grid Map</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GRID_MAP_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link blocky.impl.CellImpl <em>Cell</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see blocky.impl.CellImpl
	 * @see blocky.impl.BlockyPackageImpl#getCell()
	 * @generated
	 */
	int CELL = 2;

	/**
	 * The feature id for the '<em><b>X</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CELL__X = 0;

	/**
	 * The feature id for the '<em><b>Y</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CELL__Y = 1;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CELL__TYPE = 2;

	/**
	 * The feature id for the '<em><b>Top</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CELL__TOP = 3;

	/**
	 * The feature id for the '<em><b>Bottom</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CELL__BOTTOM = 4;

	/**
	 * The feature id for the '<em><b>Left</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CELL__LEFT = 5;

	/**
	 * The feature id for the '<em><b>Right</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CELL__RIGHT = 6;

	/**
	 * The number of structural features of the '<em>Cell</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CELL_FEATURE_COUNT = 7;

	/**
	 * The number of operations of the '<em>Cell</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CELL_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link blocky.impl.ExecutionTraceImpl <em>Execution Trace</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see blocky.impl.ExecutionTraceImpl
	 * @see blocky.impl.BlockyPackageImpl#getExecutionTrace()
	 * @generated
	 */
	int EXECUTION_TRACE = 3;

	/**
	 * The feature id for the '<em><b>States</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXECUTION_TRACE__STATES = 0;

	/**
	 * The number of structural features of the '<em>Execution Trace</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXECUTION_TRACE_FEATURE_COUNT = 1;

	/**
	 * The number of operations of the '<em>Execution Trace</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXECUTION_TRACE_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link blocky.impl.GameStateImpl <em>Game State</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see blocky.impl.GameStateImpl
	 * @see blocky.impl.BlockyPackageImpl#getGameState()
	 * @generated
	 */
	int GAME_STATE = 4;

	/**
	 * The feature id for the '<em><b>Step</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GAME_STATE__STEP = 0;

	/**
	 * The feature id for the '<em><b>Position</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GAME_STATE__POSITION = 1;

	/**
	 * The feature id for the '<em><b>Orientation</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GAME_STATE__ORIENTATION = 2;

	/**
	 * The feature id for the '<em><b>Status</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GAME_STATE__STATUS = 3;

	/**
	 * The feature id for the '<em><b>Executing Block</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GAME_STATE__EXECUTING_BLOCK = 4;

	/**
	 * The feature id for the '<em><b>Next</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GAME_STATE__NEXT = 5;

	/**
	 * The feature id for the '<em><b>Previous</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GAME_STATE__PREVIOUS = 6;

	/**
	 * The number of structural features of the '<em>Game State</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GAME_STATE_FEATURE_COUNT = 7;

	/**
	 * The number of operations of the '<em>Game State</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GAME_STATE_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link blocky.impl.BlockImpl <em>Block</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see blocky.impl.BlockImpl
	 * @see blocky.impl.BlockyPackageImpl#getBlock()
	 * @generated
	 */
	int BLOCK = 5;

	/**
	 * The feature id for the '<em><b>Next</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BLOCK__NEXT = 0;

	/**
	 * The number of structural features of the '<em>Block</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BLOCK_FEATURE_COUNT = 1;

	/**
	 * The number of operations of the '<em>Block</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BLOCK_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link blocky.impl.MoveForwardImpl <em>Move Forward</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see blocky.impl.MoveForwardImpl
	 * @see blocky.impl.BlockyPackageImpl#getMoveForward()
	 * @generated
	 */
	int MOVE_FORWARD = 6;

	/**
	 * The feature id for the '<em><b>Next</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MOVE_FORWARD__NEXT = BLOCK__NEXT;

	/**
	 * The number of structural features of the '<em>Move Forward</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MOVE_FORWARD_FEATURE_COUNT = BLOCK_FEATURE_COUNT + 0;

	/**
	 * The number of operations of the '<em>Move Forward</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MOVE_FORWARD_OPERATION_COUNT = BLOCK_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link blocky.impl.TurnImpl <em>Turn</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see blocky.impl.TurnImpl
	 * @see blocky.impl.BlockyPackageImpl#getTurn()
	 * @generated
	 */
	int TURN = 7;

	/**
	 * The feature id for the '<em><b>Next</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TURN__NEXT = BLOCK__NEXT;

	/**
	 * The feature id for the '<em><b>Direction</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TURN__DIRECTION = BLOCK_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Turn</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TURN_FEATURE_COUNT = BLOCK_FEATURE_COUNT + 1;

	/**
	 * The number of operations of the '<em>Turn</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TURN_OPERATION_COUNT = BLOCK_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link blocky.impl.RepeatUntilGoalImpl <em>Repeat Until Goal</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see blocky.impl.RepeatUntilGoalImpl
	 * @see blocky.impl.BlockyPackageImpl#getRepeatUntilGoal()
	 * @generated
	 */
	int REPEAT_UNTIL_GOAL = 8;

	/**
	 * The feature id for the '<em><b>Next</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REPEAT_UNTIL_GOAL__NEXT = BLOCK__NEXT;

	/**
	 * The feature id for the '<em><b>Body</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REPEAT_UNTIL_GOAL__BODY = BLOCK_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Repeat Until Goal</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REPEAT_UNTIL_GOAL_FEATURE_COUNT = BLOCK_FEATURE_COUNT + 1;

	/**
	 * The number of operations of the '<em>Repeat Until Goal</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REPEAT_UNTIL_GOAL_OPERATION_COUNT = BLOCK_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link blocky.impl.IfStatementImpl <em>If Statement</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see blocky.impl.IfStatementImpl
	 * @see blocky.impl.BlockyPackageImpl#getIfStatement()
	 * @generated
	 */
	int IF_STATEMENT = 9;

	/**
	 * The feature id for the '<em><b>Next</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IF_STATEMENT__NEXT = BLOCK__NEXT;

	/**
	 * The feature id for the '<em><b>Condition</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IF_STATEMENT__CONDITION = BLOCK_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Then Branch</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IF_STATEMENT__THEN_BRANCH = BLOCK_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Else Branch</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IF_STATEMENT__ELSE_BRANCH = BLOCK_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>If Statement</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IF_STATEMENT_FEATURE_COUNT = BLOCK_FEATURE_COUNT + 3;

	/**
	 * The number of operations of the '<em>If Statement</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IF_STATEMENT_OPERATION_COUNT = BLOCK_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link blocky.Direction <em>Direction</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see blocky.Direction
	 * @see blocky.impl.BlockyPackageImpl#getDirection()
	 * @generated
	 */
	int DIRECTION = 10;

	/**
	 * The meta object id for the '{@link blocky.TurnDirection <em>Turn Direction</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see blocky.TurnDirection
	 * @see blocky.impl.BlockyPackageImpl#getTurnDirection()
	 * @generated
	 */
	int TURN_DIRECTION = 11;

	/**
	 * The meta object id for the '{@link blocky.CellType <em>Cell Type</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see blocky.CellType
	 * @see blocky.impl.BlockyPackageImpl#getCellType()
	 * @generated
	 */
	int CELL_TYPE = 12;

	/**
	 * The meta object id for the '{@link blocky.SensorDirection <em>Sensor Direction</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see blocky.SensorDirection
	 * @see blocky.impl.BlockyPackageImpl#getSensorDirection()
	 * @generated
	 */
	int SENSOR_DIRECTION = 13;

	/**
	 * The meta object id for the '{@link blocky.GameStatus <em>Game Status</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see blocky.GameStatus
	 * @see blocky.impl.BlockyPackageImpl#getGameStatus()
	 * @generated
	 */
	int GAME_STATUS = 14;

	/**
	 * Returns the meta object for class '{@link blocky.Level <em>Level</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Level</em>'.
	 * @see blocky.Level
	 * @generated
	 */
	EClass getLevel();

	/**
	 * Returns the meta object for the attribute '{@link blocky.Level#getTitle <em>Title</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Title</em>'.
	 * @see blocky.Level#getTitle()
	 * @see #getLevel()
	 * @generated
	 */
	EAttribute getLevel_Title();

	/**
	 * Returns the meta object for the attribute '{@link blocky.Level#getId <em>Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Id</em>'.
	 * @see blocky.Level#getId()
	 * @see #getLevel()
	 * @generated
	 */
	EAttribute getLevel_Id();

	/**
	 * Returns the meta object for the attribute '{@link blocky.Level#getDescription <em>Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Description</em>'.
	 * @see blocky.Level#getDescription()
	 * @see #getLevel()
	 * @generated
	 */
	EAttribute getLevel_Description();

	/**
	 * Returns the meta object for the containment reference '{@link blocky.Level#getMap <em>Map</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Map</em>'.
	 * @see blocky.Level#getMap()
	 * @see #getLevel()
	 * @generated
	 */
	EReference getLevel_Map();

	/**
	 * Returns the meta object for the attribute '{@link blocky.Level#getStartOrientation <em>Start Orientation</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Start Orientation</em>'.
	 * @see blocky.Level#getStartOrientation()
	 * @see #getLevel()
	 * @generated
	 */
	EAttribute getLevel_StartOrientation();

	/**
	 * Returns the meta object for the attribute '{@link blocky.Level#getMaxBlocks <em>Max Blocks</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Max Blocks</em>'.
	 * @see blocky.Level#getMaxBlocks()
	 * @see #getLevel()
	 * @generated
	 */
	EAttribute getLevel_MaxBlocks();

	/**
	 * Returns the meta object for the attribute '{@link blocky.Level#isAllowLoops <em>Allow Loops</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Allow Loops</em>'.
	 * @see blocky.Level#isAllowLoops()
	 * @see #getLevel()
	 * @generated
	 */
	EAttribute getLevel_AllowLoops();

	/**
	 * Returns the meta object for the attribute '{@link blocky.Level#isAllowConditionals <em>Allow Conditionals</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Allow Conditionals</em>'.
	 * @see blocky.Level#isAllowConditionals()
	 * @see #getLevel()
	 * @generated
	 */
	EAttribute getLevel_AllowConditionals();

	/**
	 * Returns the meta object for the containment reference '{@link blocky.Level#getSolution <em>Solution</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Solution</em>'.
	 * @see blocky.Level#getSolution()
	 * @see #getLevel()
	 * @generated
	 */
	EReference getLevel_Solution();

	/**
	 * Returns the meta object for the containment reference list '{@link blocky.Level#getTraces <em>Traces</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Traces</em>'.
	 * @see blocky.Level#getTraces()
	 * @see #getLevel()
	 * @generated
	 */
	EReference getLevel_Traces();

	/**
	 * Returns the meta object for class '{@link blocky.GridMap <em>Grid Map</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Grid Map</em>'.
	 * @see blocky.GridMap
	 * @generated
	 */
	EClass getGridMap();

	/**
	 * Returns the meta object for the attribute '{@link blocky.GridMap#getWidth <em>Width</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Width</em>'.
	 * @see blocky.GridMap#getWidth()
	 * @see #getGridMap()
	 * @generated
	 */
	EAttribute getGridMap_Width();

	/**
	 * Returns the meta object for the attribute '{@link blocky.GridMap#getHeight <em>Height</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Height</em>'.
	 * @see blocky.GridMap#getHeight()
	 * @see #getGridMap()
	 * @generated
	 */
	EAttribute getGridMap_Height();

	/**
	 * Returns the meta object for the containment reference list '{@link blocky.GridMap#getCells <em>Cells</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Cells</em>'.
	 * @see blocky.GridMap#getCells()
	 * @see #getGridMap()
	 * @generated
	 */
	EReference getGridMap_Cells();

	/**
	 * Returns the meta object for class '{@link blocky.Cell <em>Cell</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Cell</em>'.
	 * @see blocky.Cell
	 * @generated
	 */
	EClass getCell();

	/**
	 * Returns the meta object for the attribute '{@link blocky.Cell#getX <em>X</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>X</em>'.
	 * @see blocky.Cell#getX()
	 * @see #getCell()
	 * @generated
	 */
	EAttribute getCell_X();

	/**
	 * Returns the meta object for the attribute '{@link blocky.Cell#getY <em>Y</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Y</em>'.
	 * @see blocky.Cell#getY()
	 * @see #getCell()
	 * @generated
	 */
	EAttribute getCell_Y();

	/**
	 * Returns the meta object for the attribute '{@link blocky.Cell#getType <em>Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Type</em>'.
	 * @see blocky.Cell#getType()
	 * @see #getCell()
	 * @generated
	 */
	EAttribute getCell_Type();

	/**
	 * Returns the meta object for the reference '{@link blocky.Cell#getTop <em>Top</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Top</em>'.
	 * @see blocky.Cell#getTop()
	 * @see #getCell()
	 * @generated
	 */
	EReference getCell_Top();

	/**
	 * Returns the meta object for the reference '{@link blocky.Cell#getBottom <em>Bottom</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Bottom</em>'.
	 * @see blocky.Cell#getBottom()
	 * @see #getCell()
	 * @generated
	 */
	EReference getCell_Bottom();

	/**
	 * Returns the meta object for the reference '{@link blocky.Cell#getLeft <em>Left</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Left</em>'.
	 * @see blocky.Cell#getLeft()
	 * @see #getCell()
	 * @generated
	 */
	EReference getCell_Left();

	/**
	 * Returns the meta object for the reference '{@link blocky.Cell#getRight <em>Right</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Right</em>'.
	 * @see blocky.Cell#getRight()
	 * @see #getCell()
	 * @generated
	 */
	EReference getCell_Right();

	/**
	 * Returns the meta object for class '{@link blocky.ExecutionTrace <em>Execution Trace</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Execution Trace</em>'.
	 * @see blocky.ExecutionTrace
	 * @generated
	 */
	EClass getExecutionTrace();

	/**
	 * Returns the meta object for the containment reference list '{@link blocky.ExecutionTrace#getStates <em>States</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>States</em>'.
	 * @see blocky.ExecutionTrace#getStates()
	 * @see #getExecutionTrace()
	 * @generated
	 */
	EReference getExecutionTrace_States();

	/**
	 * Returns the meta object for class '{@link blocky.GameState <em>Game State</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Game State</em>'.
	 * @see blocky.GameState
	 * @generated
	 */
	EClass getGameState();

	/**
	 * Returns the meta object for the attribute '{@link blocky.GameState#getStep <em>Step</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Step</em>'.
	 * @see blocky.GameState#getStep()
	 * @see #getGameState()
	 * @generated
	 */
	EAttribute getGameState_Step();

	/**
	 * Returns the meta object for the reference '{@link blocky.GameState#getPosition <em>Position</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Position</em>'.
	 * @see blocky.GameState#getPosition()
	 * @see #getGameState()
	 * @generated
	 */
	EReference getGameState_Position();

	/**
	 * Returns the meta object for the attribute '{@link blocky.GameState#getOrientation <em>Orientation</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Orientation</em>'.
	 * @see blocky.GameState#getOrientation()
	 * @see #getGameState()
	 * @generated
	 */
	EAttribute getGameState_Orientation();

	/**
	 * Returns the meta object for the attribute '{@link blocky.GameState#getStatus <em>Status</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Status</em>'.
	 * @see blocky.GameState#getStatus()
	 * @see #getGameState()
	 * @generated
	 */
	EAttribute getGameState_Status();

	/**
	 * Returns the meta object for the reference '{@link blocky.GameState#getExecutingBlock <em>Executing Block</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Executing Block</em>'.
	 * @see blocky.GameState#getExecutingBlock()
	 * @see #getGameState()
	 * @generated
	 */
	EReference getGameState_ExecutingBlock();

	/**
	 * Returns the meta object for the reference '{@link blocky.GameState#getNext <em>Next</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Next</em>'.
	 * @see blocky.GameState#getNext()
	 * @see #getGameState()
	 * @generated
	 */
	EReference getGameState_Next();

	/**
	 * Returns the meta object for the reference '{@link blocky.GameState#getPrevious <em>Previous</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Previous</em>'.
	 * @see blocky.GameState#getPrevious()
	 * @see #getGameState()
	 * @generated
	 */
	EReference getGameState_Previous();

	/**
	 * Returns the meta object for class '{@link blocky.Block <em>Block</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Block</em>'.
	 * @see blocky.Block
	 * @generated
	 */
	EClass getBlock();

	/**
	 * Returns the meta object for the containment reference '{@link blocky.Block#getNext <em>Next</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Next</em>'.
	 * @see blocky.Block#getNext()
	 * @see #getBlock()
	 * @generated
	 */
	EReference getBlock_Next();

	/**
	 * Returns the meta object for class '{@link blocky.MoveForward <em>Move Forward</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Move Forward</em>'.
	 * @see blocky.MoveForward
	 * @generated
	 */
	EClass getMoveForward();

	/**
	 * Returns the meta object for class '{@link blocky.Turn <em>Turn</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Turn</em>'.
	 * @see blocky.Turn
	 * @generated
	 */
	EClass getTurn();

	/**
	 * Returns the meta object for the attribute '{@link blocky.Turn#getDirection <em>Direction</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Direction</em>'.
	 * @see blocky.Turn#getDirection()
	 * @see #getTurn()
	 * @generated
	 */
	EAttribute getTurn_Direction();

	/**
	 * Returns the meta object for class '{@link blocky.RepeatUntilGoal <em>Repeat Until Goal</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Repeat Until Goal</em>'.
	 * @see blocky.RepeatUntilGoal
	 * @generated
	 */
	EClass getRepeatUntilGoal();

	/**
	 * Returns the meta object for the containment reference '{@link blocky.RepeatUntilGoal#getBody <em>Body</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Body</em>'.
	 * @see blocky.RepeatUntilGoal#getBody()
	 * @see #getRepeatUntilGoal()
	 * @generated
	 */
	EReference getRepeatUntilGoal_Body();

	/**
	 * Returns the meta object for class '{@link blocky.IfStatement <em>If Statement</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>If Statement</em>'.
	 * @see blocky.IfStatement
	 * @generated
	 */
	EClass getIfStatement();

	/**
	 * Returns the meta object for the attribute '{@link blocky.IfStatement#getCondition <em>Condition</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Condition</em>'.
	 * @see blocky.IfStatement#getCondition()
	 * @see #getIfStatement()
	 * @generated
	 */
	EAttribute getIfStatement_Condition();

	/**
	 * Returns the meta object for the containment reference '{@link blocky.IfStatement#getThenBranch <em>Then Branch</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Then Branch</em>'.
	 * @see blocky.IfStatement#getThenBranch()
	 * @see #getIfStatement()
	 * @generated
	 */
	EReference getIfStatement_ThenBranch();

	/**
	 * Returns the meta object for the containment reference '{@link blocky.IfStatement#getElseBranch <em>Else Branch</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Else Branch</em>'.
	 * @see blocky.IfStatement#getElseBranch()
	 * @see #getIfStatement()
	 * @generated
	 */
	EReference getIfStatement_ElseBranch();

	/**
	 * Returns the meta object for enum '{@link blocky.Direction <em>Direction</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Direction</em>'.
	 * @see blocky.Direction
	 * @generated
	 */
	EEnum getDirection();

	/**
	 * Returns the meta object for enum '{@link blocky.TurnDirection <em>Turn Direction</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Turn Direction</em>'.
	 * @see blocky.TurnDirection
	 * @generated
	 */
	EEnum getTurnDirection();

	/**
	 * Returns the meta object for enum '{@link blocky.CellType <em>Cell Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Cell Type</em>'.
	 * @see blocky.CellType
	 * @generated
	 */
	EEnum getCellType();

	/**
	 * Returns the meta object for enum '{@link blocky.SensorDirection <em>Sensor Direction</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Sensor Direction</em>'.
	 * @see blocky.SensorDirection
	 * @generated
	 */
	EEnum getSensorDirection();

	/**
	 * Returns the meta object for enum '{@link blocky.GameStatus <em>Game Status</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Game Status</em>'.
	 * @see blocky.GameStatus
	 * @generated
	 */
	EEnum getGameStatus();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	BlockyFactory getBlockyFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each operation of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link blocky.impl.LevelImpl <em>Level</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see blocky.impl.LevelImpl
		 * @see blocky.impl.BlockyPackageImpl#getLevel()
		 * @generated
		 */
		EClass LEVEL = eINSTANCE.getLevel();

		/**
		 * The meta object literal for the '<em><b>Title</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute LEVEL__TITLE = eINSTANCE.getLevel_Title();

		/**
		 * The meta object literal for the '<em><b>Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute LEVEL__ID = eINSTANCE.getLevel_Id();

		/**
		 * The meta object literal for the '<em><b>Description</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute LEVEL__DESCRIPTION = eINSTANCE.getLevel_Description();

		/**
		 * The meta object literal for the '<em><b>Map</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference LEVEL__MAP = eINSTANCE.getLevel_Map();

		/**
		 * The meta object literal for the '<em><b>Start Orientation</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute LEVEL__START_ORIENTATION = eINSTANCE.getLevel_StartOrientation();

		/**
		 * The meta object literal for the '<em><b>Max Blocks</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute LEVEL__MAX_BLOCKS = eINSTANCE.getLevel_MaxBlocks();

		/**
		 * The meta object literal for the '<em><b>Allow Loops</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute LEVEL__ALLOW_LOOPS = eINSTANCE.getLevel_AllowLoops();

		/**
		 * The meta object literal for the '<em><b>Allow Conditionals</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute LEVEL__ALLOW_CONDITIONALS = eINSTANCE.getLevel_AllowConditionals();

		/**
		 * The meta object literal for the '<em><b>Solution</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference LEVEL__SOLUTION = eINSTANCE.getLevel_Solution();

		/**
		 * The meta object literal for the '<em><b>Traces</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference LEVEL__TRACES = eINSTANCE.getLevel_Traces();

		/**
		 * The meta object literal for the '{@link blocky.impl.GridMapImpl <em>Grid Map</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see blocky.impl.GridMapImpl
		 * @see blocky.impl.BlockyPackageImpl#getGridMap()
		 * @generated
		 */
		EClass GRID_MAP = eINSTANCE.getGridMap();

		/**
		 * The meta object literal for the '<em><b>Width</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GRID_MAP__WIDTH = eINSTANCE.getGridMap_Width();

		/**
		 * The meta object literal for the '<em><b>Height</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GRID_MAP__HEIGHT = eINSTANCE.getGridMap_Height();

		/**
		 * The meta object literal for the '<em><b>Cells</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference GRID_MAP__CELLS = eINSTANCE.getGridMap_Cells();

		/**
		 * The meta object literal for the '{@link blocky.impl.CellImpl <em>Cell</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see blocky.impl.CellImpl
		 * @see blocky.impl.BlockyPackageImpl#getCell()
		 * @generated
		 */
		EClass CELL = eINSTANCE.getCell();

		/**
		 * The meta object literal for the '<em><b>X</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CELL__X = eINSTANCE.getCell_X();

		/**
		 * The meta object literal for the '<em><b>Y</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CELL__Y = eINSTANCE.getCell_Y();

		/**
		 * The meta object literal for the '<em><b>Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CELL__TYPE = eINSTANCE.getCell_Type();

		/**
		 * The meta object literal for the '<em><b>Top</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference CELL__TOP = eINSTANCE.getCell_Top();

		/**
		 * The meta object literal for the '<em><b>Bottom</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference CELL__BOTTOM = eINSTANCE.getCell_Bottom();

		/**
		 * The meta object literal for the '<em><b>Left</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference CELL__LEFT = eINSTANCE.getCell_Left();

		/**
		 * The meta object literal for the '<em><b>Right</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference CELL__RIGHT = eINSTANCE.getCell_Right();

		/**
		 * The meta object literal for the '{@link blocky.impl.ExecutionTraceImpl <em>Execution Trace</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see blocky.impl.ExecutionTraceImpl
		 * @see blocky.impl.BlockyPackageImpl#getExecutionTrace()
		 * @generated
		 */
		EClass EXECUTION_TRACE = eINSTANCE.getExecutionTrace();

		/**
		 * The meta object literal for the '<em><b>States</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference EXECUTION_TRACE__STATES = eINSTANCE.getExecutionTrace_States();

		/**
		 * The meta object literal for the '{@link blocky.impl.GameStateImpl <em>Game State</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see blocky.impl.GameStateImpl
		 * @see blocky.impl.BlockyPackageImpl#getGameState()
		 * @generated
		 */
		EClass GAME_STATE = eINSTANCE.getGameState();

		/**
		 * The meta object literal for the '<em><b>Step</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GAME_STATE__STEP = eINSTANCE.getGameState_Step();

		/**
		 * The meta object literal for the '<em><b>Position</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference GAME_STATE__POSITION = eINSTANCE.getGameState_Position();

		/**
		 * The meta object literal for the '<em><b>Orientation</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GAME_STATE__ORIENTATION = eINSTANCE.getGameState_Orientation();

		/**
		 * The meta object literal for the '<em><b>Status</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GAME_STATE__STATUS = eINSTANCE.getGameState_Status();

		/**
		 * The meta object literal for the '<em><b>Executing Block</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference GAME_STATE__EXECUTING_BLOCK = eINSTANCE.getGameState_ExecutingBlock();

		/**
		 * The meta object literal for the '<em><b>Next</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference GAME_STATE__NEXT = eINSTANCE.getGameState_Next();

		/**
		 * The meta object literal for the '<em><b>Previous</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference GAME_STATE__PREVIOUS = eINSTANCE.getGameState_Previous();

		/**
		 * The meta object literal for the '{@link blocky.impl.BlockImpl <em>Block</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see blocky.impl.BlockImpl
		 * @see blocky.impl.BlockyPackageImpl#getBlock()
		 * @generated
		 */
		EClass BLOCK = eINSTANCE.getBlock();

		/**
		 * The meta object literal for the '<em><b>Next</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BLOCK__NEXT = eINSTANCE.getBlock_Next();

		/**
		 * The meta object literal for the '{@link blocky.impl.MoveForwardImpl <em>Move Forward</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see blocky.impl.MoveForwardImpl
		 * @see blocky.impl.BlockyPackageImpl#getMoveForward()
		 * @generated
		 */
		EClass MOVE_FORWARD = eINSTANCE.getMoveForward();

		/**
		 * The meta object literal for the '{@link blocky.impl.TurnImpl <em>Turn</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see blocky.impl.TurnImpl
		 * @see blocky.impl.BlockyPackageImpl#getTurn()
		 * @generated
		 */
		EClass TURN = eINSTANCE.getTurn();

		/**
		 * The meta object literal for the '<em><b>Direction</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TURN__DIRECTION = eINSTANCE.getTurn_Direction();

		/**
		 * The meta object literal for the '{@link blocky.impl.RepeatUntilGoalImpl <em>Repeat Until Goal</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see blocky.impl.RepeatUntilGoalImpl
		 * @see blocky.impl.BlockyPackageImpl#getRepeatUntilGoal()
		 * @generated
		 */
		EClass REPEAT_UNTIL_GOAL = eINSTANCE.getRepeatUntilGoal();

		/**
		 * The meta object literal for the '<em><b>Body</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference REPEAT_UNTIL_GOAL__BODY = eINSTANCE.getRepeatUntilGoal_Body();

		/**
		 * The meta object literal for the '{@link blocky.impl.IfStatementImpl <em>If Statement</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see blocky.impl.IfStatementImpl
		 * @see blocky.impl.BlockyPackageImpl#getIfStatement()
		 * @generated
		 */
		EClass IF_STATEMENT = eINSTANCE.getIfStatement();

		/**
		 * The meta object literal for the '<em><b>Condition</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IF_STATEMENT__CONDITION = eINSTANCE.getIfStatement_Condition();

		/**
		 * The meta object literal for the '<em><b>Then Branch</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IF_STATEMENT__THEN_BRANCH = eINSTANCE.getIfStatement_ThenBranch();

		/**
		 * The meta object literal for the '<em><b>Else Branch</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IF_STATEMENT__ELSE_BRANCH = eINSTANCE.getIfStatement_ElseBranch();

		/**
		 * The meta object literal for the '{@link blocky.Direction <em>Direction</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see blocky.Direction
		 * @see blocky.impl.BlockyPackageImpl#getDirection()
		 * @generated
		 */
		EEnum DIRECTION = eINSTANCE.getDirection();

		/**
		 * The meta object literal for the '{@link blocky.TurnDirection <em>Turn Direction</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see blocky.TurnDirection
		 * @see blocky.impl.BlockyPackageImpl#getTurnDirection()
		 * @generated
		 */
		EEnum TURN_DIRECTION = eINSTANCE.getTurnDirection();

		/**
		 * The meta object literal for the '{@link blocky.CellType <em>Cell Type</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see blocky.CellType
		 * @see blocky.impl.BlockyPackageImpl#getCellType()
		 * @generated
		 */
		EEnum CELL_TYPE = eINSTANCE.getCellType();

		/**
		 * The meta object literal for the '{@link blocky.SensorDirection <em>Sensor Direction</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see blocky.SensorDirection
		 * @see blocky.impl.BlockyPackageImpl#getSensorDirection()
		 * @generated
		 */
		EEnum SENSOR_DIRECTION = eINSTANCE.getSensorDirection();

		/**
		 * The meta object literal for the '{@link blocky.GameStatus <em>Game Status</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see blocky.GameStatus
		 * @see blocky.impl.BlockyPackageImpl#getGameStatus()
		 * @generated
		 */
		EEnum GAME_STATUS = eINSTANCE.getGameStatus();

	}

} //BlockyPackage
