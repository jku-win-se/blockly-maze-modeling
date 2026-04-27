package blocky_game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Path;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.junit.jupiter.api.Test;

import blocky.AtomicStatement;
import blocky.BlockyFactory;
import blocky.BlockyPackage;
import blocky.ConditionKind;
import blocky.Game;
import blocky.GameStatus;
import blocky.IfStmt;
import blocky.Level;
import blocky_momot.BlockyProgramDistance;
import blocky_momot.BlockySimulator;

/**
 * Tests for the logic used by the MOMoT fitness objectives in {@code blocky.momot}.
 *
 * We validate:
 * - GoalReached / MustReachGoalPenalty semantics via {@link BlockySimulator#run(Level)}
 * - ShortestPath semantics via {@link BlockySimulator#stepsToGoalOrPenalty(Level)}
 * - Edits semantics via {@link BlockyProgramDistance}
 * - Enum defaults consistent with generated EMF model code
 */
public class FitnessFunctionsTest {

  private static Game loadGame(Path xmiPath) throws Exception {
    Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());

    EPackage pkg = BlockyPackage.eINSTANCE;
    EPackage.Registry.INSTANCE.put(pkg.getNsURI(), pkg);
    EPackage.Registry.INSTANCE.put(pkg.getName(), pkg);

    ResourceSet rs = new ResourceSetImpl();
    rs.getPackageRegistry().put(pkg.getNsURI(), pkg);
    rs.getPackageRegistry().put(pkg.getName(), pkg);

    File f = xmiPath.toFile();
    Resource r = rs.getResource(URI.createFileURI(f.getAbsolutePath()), true);
    r.load(null);
    Object root = r.getContents().isEmpty() ? null : r.getContents().get(0);
    assertNotNull(root, "XMI has no root: " + xmiPath);
    assertTrue(root instanceof Game, "Expected blocky.Game root but got: " + root.getClass());
    return (Game) root;
  }

  private static Level firstLevel(Game g) {
    assertNotNull(g);
    assertTrue(!g.getLevels().isEmpty(), "Game has no levels");
    return g.getLevels().get(0);
  }

  private static double goalReachedObjective(GameStatus status) {
    return status == GameStatus.WON ? 1.0 : 0.0;
  }

  private static double mustReachGoalPenaltyObjective(GameStatus status) {
    return status == GameStatus.WON ? 0.0 : 1000.0;
  }

  @Test
  void enumDefaults_matchGeneratedModel() {
    AtomicStatement a = BlockyFactory.eINSTANCE.createAtomicStatement();
    assertEquals(blocky.AtomicStatementKind.TURN_LEFT, a.getKind(), "AtomicStatement default kind should be TURN_LEFT");

    IfStmt i = BlockyFactory.eINSTANCE.createIfStmt();
    assertEquals(ConditionKind.CHECK_FORWARD, i.getCondition(), "IfStmt default condition should be CHECK_FORWARD");
  }

  @Test
  void saveXmi_reachesGoal_goalObjectivesAgree() throws Exception {
    Path saveXmi = Path.of("..", "blocky_momot", "model", "input", "save.xmi").normalize();
    Game g = loadGame(saveXmi);
    Level level = firstLevel(g);

    GameStatus status = BlockySimulator.run(level);
    assertEquals(GameStatus.WON, status, "save.xmi should reach the goal under headless simulator");

    assertEquals(1.0, goalReachedObjective(status));
    assertEquals(0.0, mustReachGoalPenaltyObjective(status));

    int steps = BlockySimulator.stepsToGoalOrPenalty(level);
    assertTrue(steps >= 0 && steps < 100000, "Expected finite step count (< penalty), got: " + steps);
  }

  @Test
  void gameOldXmi_withoutSolution_doesNotReachGoal_penaltiesApply() throws Exception {
    Path gameOldXmi = Path.of("..", "blocky_momot", "model", "input", "game_old.xmi").normalize();
    Game g = loadGame(gameOldXmi);
    Level level = firstLevel(g);

    GameStatus status = BlockySimulator.run(level);
    assertTrue(status != GameStatus.WON, "game_old.xmi should not be WON without a solution program");

    assertEquals(0.0, goalReachedObjective(status));
    assertEquals(1000.0, mustReachGoalPenaltyObjective(status));

    int steps = BlockySimulator.stepsToGoalOrPenalty(level);
    assertEquals(100000, steps, "Expected penalty steps when goal not reached");
  }

  @Test
  void editsObjective_baselineDistance_isZeroForBaseline() throws Exception {
    String baselinePath = Path.of("..", "blocky_momot", "model", "input", "save.xmi").normalize().toString();
    BlockyProgramDistance.initializeBaseline(baselinePath);

    Game baseline = loadGame(Path.of("..", "blocky_momot", "model", "input", "save.xmi").normalize());
    int dist = BlockyProgramDistance.distanceToBaseline(baseline);
    assertEquals(0, dist, "Baseline model distance to itself should be 0");
  }
}

