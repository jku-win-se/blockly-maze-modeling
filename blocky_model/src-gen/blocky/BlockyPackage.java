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
	 * The meta object id for the '{@link blocky.impl.GameImpl <em>Game</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see blocky.impl.GameImpl
	 * @see blocky.impl.BlockyPackageImpl#getGame()
	 * @generated
	 */
	int GAME = 0;

	/**
	 * The feature id for the '<em><b>Levels</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GAME__LEVELS = 0;

	/**
	 * The number of structural features of the '<em>Game</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GAME_FEATURE_COUNT = 1;

	/**
	 * The number of operations of the '<em>Game</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GAME_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link blocky.impl.LevelImpl <em>Level</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see blocky.impl.LevelImpl
	 * @see blocky.impl.BlockyPackageImpl#getLevel()
	 * @generated
	 */
	int LEVEL = 1;

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
	int GRID_MAP = 2;

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
	int CELL = 3;

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
	int EXECUTION_TRACE = 4;

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
	int GAME_STATE = 5;

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
	 * The feature id for the '<em><b>Executing Statement</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GAME_STATE__EXECUTING_STATEMENT = 4;

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
	 * The meta object id for the '{@link blocky.impl.BodyImpl <em>Body</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see blocky.impl.BodyImpl
	 * @see blocky.impl.BlockyPackageImpl#getBody()
	 * @generated
	 */
	int BODY = 6;

	/**
	 * The feature id for the '<em><b>First Container</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BODY__FIRST_CONTAINER = 0;

	/**
	 * The number of structural features of the '<em>Body</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BODY_FEATURE_COUNT = 1;

	/**
	 * The number of operations of the '<em>Body</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BODY_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link blocky.impl.ContainerImpl <em>Container</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see blocky.impl.ContainerImpl
	 * @see blocky.impl.BlockyPackageImpl#getContainer()
	 * @generated
	 */
	int CONTAINER = 7;

	/**
	 * The feature id for the '<em><b>Statement</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONTAINER__STATEMENT = 0;

	/**
	 * The feature id for the '<em><b>Next</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONTAINER__NEXT = 1;

	/**
	 * The feature id for the '<em><b>Generated</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONTAINER__GENERATED = 2;

	/**
	 * The number of structural features of the '<em>Container</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONTAINER_FEATURE_COUNT = 3;

	/**
	 * The number of operations of the '<em>Container</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONTAINER_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link blocky.impl.StatementImpl <em>Statement</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see blocky.impl.StatementImpl
	 * @see blocky.impl.BlockyPackageImpl#getStatement()
	 * @generated
	 */
	int STATEMENT = 8;

	/**
	 * The feature id for the '<em><b>Generated</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STATEMENT__GENERATED = 0;

	/**
	 * The number of structural features of the '<em>Statement</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STATEMENT_FEATURE_COUNT = 1;

	/**
	 * The number of operations of the '<em>Statement</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STATEMENT_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link blocky.impl.AtomicStatementImpl <em>Atomic Statement</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see blocky.impl.AtomicStatementImpl
	 * @see blocky.impl.BlockyPackageImpl#getAtomicStatement()
	 * @generated
	 */
	int ATOMIC_STATEMENT = 9;

	/**
	 * The feature id for the '<em><b>Generated</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ATOMIC_STATEMENT__GENERATED = STATEMENT__GENERATED;

	/**
	 * The feature id for the '<em><b>Kind</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ATOMIC_STATEMENT__KIND = STATEMENT_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Atomic Statement</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ATOMIC_STATEMENT_FEATURE_COUNT = STATEMENT_FEATURE_COUNT + 1;

	/**
	 * The number of operations of the '<em>Atomic Statement</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ATOMIC_STATEMENT_OPERATION_COUNT = STATEMENT_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link blocky.impl.LoopImpl <em>Loop</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see blocky.impl.LoopImpl
	 * @see blocky.impl.BlockyPackageImpl#getLoop()
	 * @generated
	 */
	int LOOP = 10;

	/**
	 * The feature id for the '<em><b>Generated</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LOOP__GENERATED = STATEMENT__GENERATED;

	/**
	 * The feature id for the '<em><b>Body</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LOOP__BODY = STATEMENT_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Loop</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LOOP_FEATURE_COUNT = STATEMENT_FEATURE_COUNT + 1;

	/**
	 * The number of operations of the '<em>Loop</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LOOP_OPERATION_COUNT = STATEMENT_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link blocky.impl.IfStmtImpl <em>If Stmt</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see blocky.impl.IfStmtImpl
	 * @see blocky.impl.BlockyPackageImpl#getIfStmt()
	 * @generated
	 */
	int IF_STMT = 11;

	/**
	 * The feature id for the '<em><b>Generated</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IF_STMT__GENERATED = STATEMENT__GENERATED;

	/**
	 * The feature id for the '<em><b>Condition</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IF_STMT__CONDITION = STATEMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Then Body</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IF_STMT__THEN_BODY = STATEMENT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Else Body</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IF_STMT__ELSE_BODY = STATEMENT_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>If Stmt</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IF_STMT_FEATURE_COUNT = STATEMENT_FEATURE_COUNT + 3;

	/**
	 * The number of operations of the '<em>If Stmt</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IF_STMT_OPERATION_COUNT = STATEMENT_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link blocky.Direction <em>Direction</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see blocky.Direction
	 * @see blocky.impl.BlockyPackageImpl#getDirection()
	 * @generated
	 */
	int DIRECTION = 12;

	/**
	 * The meta object id for the '{@link blocky.CellType <em>Cell Type</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see blocky.CellType
	 * @see blocky.impl.BlockyPackageImpl#getCellType()
	 * @generated
	 */
	int CELL_TYPE = 13;

	/**
	 * The meta object id for the '{@link blocky.SensorDirection <em>Sensor Direction</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see blocky.SensorDirection
	 * @see blocky.impl.BlockyPackageImpl#getSensorDirection()
	 * @generated
	 */
	int SENSOR_DIRECTION = 14;

	/**
	 * The meta object id for the '{@link blocky.GameStatus <em>Game Status</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see blocky.GameStatus
	 * @see blocky.impl.BlockyPackageImpl#getGameStatus()
	 * @generated
	 */
	int GAME_STATUS = 15;

	/**
	 * The meta object id for the '{@link blocky.ConditionKind <em>Condition Kind</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see blocky.ConditionKind
	 * @see blocky.impl.BlockyPackageImpl#getConditionKind()
	 * @generated
	 */
	int CONDITION_KIND = 16;

	/**
	 * The meta object id for the '{@link blocky.AtomicStatementKind <em>Atomic Statement Kind</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see blocky.AtomicStatementKind
	 * @see blocky.impl.BlockyPackageImpl#getAtomicStatementKind()
	 * @generated
	 */
	int ATOMIC_STATEMENT_KIND = 17;

	/**
	 * Returns the meta object for class '{@link blocky.Game <em>Game</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Game</em>'.
	 * @see blocky.Game
	 * @generated
	 */
	EClass getGame();

	/**
	 * Returns the meta object for the containment reference list '{@link blocky.Game#getLevels <em>Levels</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Levels</em>'.
	 * @see blocky.Game#getLevels()
	 * @see #getGame()
	 * @generated
	 */
	EReference getGame_Levels();

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
	 * Returns the meta object for the reference '{@link blocky.GameState#getExecutingStatement <em>Executing Statement</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Executing Statement</em>'.
	 * @see blocky.GameState#getExecutingStatement()
	 * @see #getGameState()
	 * @generated
	 */
	EReference getGameState_ExecutingStatement();

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
	 * Returns the meta object for class '{@link blocky.Body <em>Body</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Body</em>'.
	 * @see blocky.Body
	 * @generated
	 */
	EClass getBody();

	/**
	 * Returns the meta object for the containment reference '{@link blocky.Body#getFirstContainer <em>First Container</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>First Container</em>'.
	 * @see blocky.Body#getFirstContainer()
	 * @see #getBody()
	 * @generated
	 */
	EReference getBody_FirstContainer();

	/**
	 * Returns the meta object for class '{@link blocky.Container <em>Container</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Container</em>'.
	 * @see blocky.Container
	 * @generated
	 */
	EClass getContainer();

	/**
	 * Returns the meta object for the containment reference '{@link blocky.Container#getStatement <em>Statement</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Statement</em>'.
	 * @see blocky.Container#getStatement()
	 * @see #getContainer()
	 * @generated
	 */
	EReference getContainer_Statement();

	/**
	 * Returns the meta object for the containment reference '{@link blocky.Container#getNext <em>Next</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Next</em>'.
	 * @see blocky.Container#getNext()
	 * @see #getContainer()
	 * @generated
	 */
	EReference getContainer_Next();

	/**
	 * Returns the meta object for the attribute '{@link blocky.Container#isGenerated <em>Generated</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Generated</em>'.
	 * @see blocky.Container#isGenerated()
	 * @see #getContainer()
	 * @generated
	 */
	EAttribute getContainer_Generated();

	/**
	 * Returns the meta object for class '{@link blocky.Statement <em>Statement</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Statement</em>'.
	 * @see blocky.Statement
	 * @generated
	 */
	EClass getStatement();

	/**
	 * Returns the meta object for the attribute '{@link blocky.Statement#isGenerated <em>Generated</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Generated</em>'.
	 * @see blocky.Statement#isGenerated()
	 * @see #getStatement()
	 * @generated
	 */
	EAttribute getStatement_Generated();

	/**
	 * Returns the meta object for class '{@link blocky.AtomicStatement <em>Atomic Statement</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Atomic Statement</em>'.
	 * @see blocky.AtomicStatement
	 * @generated
	 */
	EClass getAtomicStatement();

	/**
	 * Returns the meta object for the attribute '{@link blocky.AtomicStatement#getKind <em>Kind</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Kind</em>'.
	 * @see blocky.AtomicStatement#getKind()
	 * @see #getAtomicStatement()
	 * @generated
	 */
	EAttribute getAtomicStatement_Kind();

	/**
	 * Returns the meta object for class '{@link blocky.Loop <em>Loop</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Loop</em>'.
	 * @see blocky.Loop
	 * @generated
	 */
	EClass getLoop();

	/**
	 * Returns the meta object for the containment reference '{@link blocky.Loop#getBody <em>Body</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Body</em>'.
	 * @see blocky.Loop#getBody()
	 * @see #getLoop()
	 * @generated
	 */
	EReference getLoop_Body();

	/**
	 * Returns the meta object for class '{@link blocky.IfStmt <em>If Stmt</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>If Stmt</em>'.
	 * @see blocky.IfStmt
	 * @generated
	 */
	EClass getIfStmt();

	/**
	 * Returns the meta object for the attribute '{@link blocky.IfStmt#getCondition <em>Condition</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Condition</em>'.
	 * @see blocky.IfStmt#getCondition()
	 * @see #getIfStmt()
	 * @generated
	 */
	EAttribute getIfStmt_Condition();

	/**
	 * Returns the meta object for the containment reference '{@link blocky.IfStmt#getThenBody <em>Then Body</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Then Body</em>'.
	 * @see blocky.IfStmt#getThenBody()
	 * @see #getIfStmt()
	 * @generated
	 */
	EReference getIfStmt_ThenBody();

	/**
	 * Returns the meta object for the containment reference '{@link blocky.IfStmt#getElseBody <em>Else Body</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Else Body</em>'.
	 * @see blocky.IfStmt#getElseBody()
	 * @see #getIfStmt()
	 * @generated
	 */
	EReference getIfStmt_ElseBody();

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
	 * Returns the meta object for enum '{@link blocky.ConditionKind <em>Condition Kind</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Condition Kind</em>'.
	 * @see blocky.ConditionKind
	 * @generated
	 */
	EEnum getConditionKind();

	/**
	 * Returns the meta object for enum '{@link blocky.AtomicStatementKind <em>Atomic Statement Kind</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Atomic Statement Kind</em>'.
	 * @see blocky.AtomicStatementKind
	 * @generated
	 */
	EEnum getAtomicStatementKind();

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
		 * The meta object literal for the '{@link blocky.impl.GameImpl <em>Game</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see blocky.impl.GameImpl
		 * @see blocky.impl.BlockyPackageImpl#getGame()
		 * @generated
		 */
		EClass GAME = eINSTANCE.getGame();

		/**
		 * The meta object literal for the '<em><b>Levels</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference GAME__LEVELS = eINSTANCE.getGame_Levels();

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
		 * The meta object literal for the '<em><b>Executing Statement</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference GAME_STATE__EXECUTING_STATEMENT = eINSTANCE.getGameState_ExecutingStatement();

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
		 * The meta object literal for the '{@link blocky.impl.BodyImpl <em>Body</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see blocky.impl.BodyImpl
		 * @see blocky.impl.BlockyPackageImpl#getBody()
		 * @generated
		 */
		EClass BODY = eINSTANCE.getBody();

		/**
		 * The meta object literal for the '<em><b>First Container</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BODY__FIRST_CONTAINER = eINSTANCE.getBody_FirstContainer();

		/**
		 * The meta object literal for the '{@link blocky.impl.ContainerImpl <em>Container</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see blocky.impl.ContainerImpl
		 * @see blocky.impl.BlockyPackageImpl#getContainer()
		 * @generated
		 */
		EClass CONTAINER = eINSTANCE.getContainer();

		/**
		 * The meta object literal for the '<em><b>Statement</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference CONTAINER__STATEMENT = eINSTANCE.getContainer_Statement();

		/**
		 * The meta object literal for the '<em><b>Next</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference CONTAINER__NEXT = eINSTANCE.getContainer_Next();

		/**
		 * The meta object literal for the '<em><b>Generated</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CONTAINER__GENERATED = eINSTANCE.getContainer_Generated();

		/**
		 * The meta object literal for the '{@link blocky.impl.StatementImpl <em>Statement</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see blocky.impl.StatementImpl
		 * @see blocky.impl.BlockyPackageImpl#getStatement()
		 * @generated
		 */
		EClass STATEMENT = eINSTANCE.getStatement();

		/**
		 * The meta object literal for the '<em><b>Generated</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute STATEMENT__GENERATED = eINSTANCE.getStatement_Generated();

		/**
		 * The meta object literal for the '{@link blocky.impl.AtomicStatementImpl <em>Atomic Statement</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see blocky.impl.AtomicStatementImpl
		 * @see blocky.impl.BlockyPackageImpl#getAtomicStatement()
		 * @generated
		 */
		EClass ATOMIC_STATEMENT = eINSTANCE.getAtomicStatement();

		/**
		 * The meta object literal for the '<em><b>Kind</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ATOMIC_STATEMENT__KIND = eINSTANCE.getAtomicStatement_Kind();

		/**
		 * The meta object literal for the '{@link blocky.impl.LoopImpl <em>Loop</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see blocky.impl.LoopImpl
		 * @see blocky.impl.BlockyPackageImpl#getLoop()
		 * @generated
		 */
		EClass LOOP = eINSTANCE.getLoop();

		/**
		 * The meta object literal for the '<em><b>Body</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference LOOP__BODY = eINSTANCE.getLoop_Body();

		/**
		 * The meta object literal for the '{@link blocky.impl.IfStmtImpl <em>If Stmt</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see blocky.impl.IfStmtImpl
		 * @see blocky.impl.BlockyPackageImpl#getIfStmt()
		 * @generated
		 */
		EClass IF_STMT = eINSTANCE.getIfStmt();

		/**
		 * The meta object literal for the '<em><b>Condition</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IF_STMT__CONDITION = eINSTANCE.getIfStmt_Condition();

		/**
		 * The meta object literal for the '<em><b>Then Body</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IF_STMT__THEN_BODY = eINSTANCE.getIfStmt_ThenBody();

		/**
		 * The meta object literal for the '<em><b>Else Body</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IF_STMT__ELSE_BODY = eINSTANCE.getIfStmt_ElseBody();

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

		/**
		 * The meta object literal for the '{@link blocky.ConditionKind <em>Condition Kind</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see blocky.ConditionKind
		 * @see blocky.impl.BlockyPackageImpl#getConditionKind()
		 * @generated
		 */
		EEnum CONDITION_KIND = eINSTANCE.getConditionKind();

		/**
		 * The meta object literal for the '{@link blocky.AtomicStatementKind <em>Atomic Statement Kind</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see blocky.AtomicStatementKind
		 * @see blocky.impl.BlockyPackageImpl#getAtomicStatementKind()
		 * @generated
		 */
		EEnum ATOMIC_STATEMENT_KIND = eINSTANCE.getAtomicStatementKind();

	}

} //BlockyPackage
