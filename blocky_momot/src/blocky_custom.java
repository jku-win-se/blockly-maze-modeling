import at.ac.tuwien.big.moea.SearchExperiment;
import at.ac.tuwien.big.moea.experiment.executor.listener.SeedRuntimePrintListener;
import at.ac.tuwien.big.moea.print.IPopulationWriter;
import at.ac.tuwien.big.moea.print.ISolutionWriter;
import at.ac.tuwien.big.moea.search.algorithm.EvolutionaryAlgorithmFactory;
import at.ac.tuwien.big.moea.search.algorithm.LocalSearchAlgorithmFactory;
import at.ac.tuwien.big.moea.search.algorithm.provider.IRegisteredAlgorithm;
import at.ac.tuwien.big.moea.search.fitness.dimension.IFitnessDimension;
import at.ac.tuwien.big.momot.ModuleManager;
import at.ac.tuwien.big.momot.TransformationResultManager;
import at.ac.tuwien.big.momot.TransformationSearchOrchestration;
import at.ac.tuwien.big.momot.problem.solution.TransformationSolution;
import at.ac.tuwien.big.momot.problem.unit.parameter.IParameterValue;
import at.ac.tuwien.big.momot.search.algorithm.operator.mutation.TransformationParameterMutation;
import at.ac.tuwien.big.momot.search.algorithm.operator.mutation.TransformationPlaceholderMutation;
import at.ac.tuwien.big.momot.search.fitness.IEGraphMultiDimensionalFitnessFunction;
import at.ac.tuwien.big.momot.search.fitness.dimension.AbstractEGraphFitnessDimension;
import at.ac.tuwien.big.momot.search.solution.repair.ITransformationRepairer;
import at.ac.tuwien.big.momot.search.solution.repair.TransformationPlaceholderRepairer;
import at.ac.tuwien.big.momot.util.MomotUtil;
import blocky.BlockyPackage;
import blocky.Game;
import blocky.GameStatus;
import blocky.Level;
import blocky_momot.BlockyProgramDistance;
import blocky_momot.BlockyProgramMetrics;
import blocky_momot.BlockySimulator;
import blocky_momot.EnumParamPreprocessFitnessFunction;
import blocky_momot.RandomAtomicKindLiteralValue;
import blocky_momot.RandomConditionKindLiteralValue;
import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.henshin.interpreter.EGraph;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.moeaframework.algorithm.NSGAII;
import org.moeaframework.core.Population;
import org.moeaframework.core.PRNG;
import org.moeaframework.core.operator.OnePointCrossover;
import org.moeaframework.core.operator.TournamentSelection;
import org.moeaframework.util.progress.ProgressListener;

/**
 * Non-generated copy of {@code src-gen/blocky.java}.
 *
 * Key differences vs the generated runner:
 * - No static initializer that attempts to load the input file (prevents init crashes).
 * - populationSize/maxEvaluations/nrRuns are read at runtime from system properties:
 *   - blocky.populationSize (default 50)
 *   - blocky.maxEvaluations (default 2000)
 *   - blocky.nrRuns (default 10)
 *
 * This file is safe to edit; regeneration will not overwrite it.
 */
@SuppressWarnings("all")
public class blocky_custom {
  protected static String input = System.getProperty("blocky.input", "model/1.xmi");

  protected final String[] modules = new String[] { "../blocky_model/transformations/statement_insertions_henshin_text.henshin" };

  protected final String[] unitsToRemove = new String[] { "statement_insertions_henshin_text::InsertContainerIntoEmptyBody", "statement_insertions_henshin_text::InsertContainerBeforeBodyHead", "statement_insertions_henshin_text::InsertContainerBetweenNext", "statement_insertions_henshin_text::InsertContainerAfterLast", "statement_insertions_henshin_text::InsertContainerAnywhere", "statement_insertions_henshin_text::PopulateEmptyContainerWithAtomic", "statement_insertions_henshin_text::PopulateEmptyContainerWithLoop", "statement_insertions_henshin_text::PopulateEmptyContainerWithIf", "statement_insertions_henshin_text::PopulateAnyEmptyContainer", "statement_insertions_henshin_text::DeleteOnlyContainerFromBody", "statement_insertions_henshin_text::DeleteHeadContainerWithNext", "statement_insertions_henshin_text::DeleteBetweenContainerWithNext", "statement_insertions_henshin_text::DeleteLastContainer", "InsertContainerIntoEmptyBody", "InsertContainerBeforeBodyHead", "InsertContainerBetweenNext", "InsertContainerAfterLast", "InsertContainerAnywhere", "PopulateEmptyContainerWithAtomic", "PopulateEmptyContainerWithLoop", "PopulateEmptyContainerWithIf", "PopulateAnyEmptyContainer", "DeleteOnlyContainerFromBody", "DeleteHeadContainerWithNext", "DeleteBetweenContainerWithNext", "DeleteLastContainer", "statement_insertions_henshin_text.InsertContainerIntoEmptyBody", "statement_insertions_henshin_text.InsertContainerBeforeBodyHead", "statement_insertions_henshin_text.InsertContainerBetweenNext", "statement_insertions_henshin_text.InsertContainerAfterLast", "statement_insertions_henshin_text.InsertContainerAnywhere", "statement_insertions_henshin_text.PopulateEmptyContainerWithAtomic", "statement_insertions_henshin_text.PopulateEmptyContainerWithLoop", "statement_insertions_henshin_text.PopulateEmptyContainerWithIf", "statement_insertions_henshin_text.PopulateAnyEmptyContainer", "statement_insertions_henshin_text.DeleteOnlyContainerFromBody", "statement_insertions_henshin_text.DeleteHeadContainerWithNext", "statement_insertions_henshin_text.DeleteBetweenContainerWithNext", "statement_insertions_henshin_text.DeleteLastContainer", "statement_insertions_henshin_text::HenshinDSL::InsertContainerIntoEmptyBody", "statement_insertions_henshin_text::HenshinDSL::InsertContainerBeforeBodyHead", "statement_insertions_henshin_text::HenshinDSL::InsertContainerBetweenNext", "statement_insertions_henshin_text::HenshinDSL::InsertContainerAfterLast", "statement_insertions_henshin_text::HenshinDSL::InsertContainerAnywhere", "statement_insertions_henshin_text::HenshinDSL::PopulateEmptyContainerWithAtomic", "statement_insertions_henshin_text::HenshinDSL::PopulateEmptyContainerWithLoop", "statement_insertions_henshin_text::HenshinDSL::PopulateEmptyContainerWithIf", "statement_insertions_henshin_text::HenshinDSL::PopulateAnyEmptyContainer", "statement_insertions_henshin_text::HenshinDSL::DeleteOnlyContainerFromBody", "statement_insertions_henshin_text::HenshinDSL::DeleteHeadContainerWithNext", "statement_insertions_henshin_text::HenshinDSL::DeleteBetweenContainerWithNext", "statement_insertions_henshin_text::HenshinDSL::DeleteLastContainer", "HenshinDSL::InsertContainerIntoEmptyBody", "HenshinDSL::InsertContainerBeforeBodyHead", "HenshinDSL::InsertContainerBetweenNext", "HenshinDSL::InsertContainerAfterLast", "HenshinDSL::InsertContainerAnywhere", "HenshinDSL::PopulateEmptyContainerWithAtomic", "HenshinDSL::PopulateEmptyContainerWithLoop", "HenshinDSL::PopulateEmptyContainerWithIf", "HenshinDSL::PopulateAnyEmptyContainer", "HenshinDSL::DeleteOnlyContainerFromBody", "HenshinDSL::DeleteHeadContainerWithNext", "HenshinDSL::DeleteBetweenContainerWithNext", "HenshinDSL::DeleteLastContainer", "HenshinDSL.InsertContainerIntoEmptyBody", "HenshinDSL.InsertContainerBeforeBodyHead", "HenshinDSL.InsertContainerBetweenNext", "HenshinDSL.InsertContainerAfterLast", "HenshinDSL.InsertContainerAnywhere", "HenshinDSL.PopulateEmptyContainerWithAtomic", "HenshinDSL.PopulateEmptyContainerWithLoop", "HenshinDSL.PopulateEmptyContainerWithIf", "HenshinDSL.PopulateAnyEmptyContainer", "HenshinDSL.DeleteOnlyContainerFromBody", "HenshinDSL.DeleteHeadContainerWithNext", "HenshinDSL.DeleteBetweenContainerWithNext", "HenshinDSL.DeleteLastContainer" };

  protected final IEGraphMultiDimensionalFitnessFunction fitnessFunction = new EnumParamPreprocessFitnessFunction();

  protected final String _parameterValueKey_0 = "HenshinDSL.CreateThenInsertContainerThenPopulate.k";
  protected final String _parameterValueKey_1 = "HenshinDSL.CreateThenInsertContainerThenPopulate.cnd";
  protected final String _parameterValueKey_2 = "HenshinDSL::CreateThenInsertContainerThenPopulate::k";
  protected final String _parameterValueKey_3 = "HenshinDSL::CreateThenInsertContainerThenPopulate::cnd";
  protected final String _parameterValueKey_4 = "CreateThenInsertContainerThenPopulate::k";
  protected final String _parameterValueKey_5 = "CreateThenInsertContainerThenPopulate::cnd";
  protected final String _parameterValueKey_6 = "HenshinDSL.CreateThenInsertContainerThenPopulate::k";
  protected final String _parameterValueKey_7 = "HenshinDSL.CreateThenInsertContainerThenPopulate::cnd";
  protected final String _parameterValueKey_8 = "HenshinDSL.PopulateAnyEmptyContainer.k";
  protected final String _parameterValueKey_9 = "HenshinDSL.PopulateAnyEmptyContainer.cnd";
  protected final String _parameterValueKey_10 = "HenshinDSL.PopulateEmptyContainerWithAtomic.k";
  protected final String _parameterValueKey_11 = "HenshinDSL.PopulateEmptyContainerWithIf.cnd";
  protected final String _parameterValueKey_12 = "CreateThenInsertContainerThenPopulate.k";
  protected final String _parameterValueKey_13 = "CreateThenInsertContainerThenPopulate.cnd";
  protected final String _parameterValueKey_14 = "PopulateAnyEmptyContainer.k";
  protected final String _parameterValueKey_15 = "PopulateAnyEmptyContainer.cnd";
  protected final String _parameterValueKey_16 = "PopulateEmptyContainerWithAtomic.k";
  protected final String _parameterValueKey_17 = "PopulateEmptyContainerWithIf.cnd";
  protected final String _parameterValueKey_18 = "statement_insertions_henshin_text::CreateThenInsertContainerThenPopulate::k";
  protected final String _parameterValueKey_19 = "statement_insertions_henshin_text::CreateThenInsertContainerThenPopulate::cnd";
  protected final String _parameterValueKey_20 = "statement_insertions_henshin_text::HenshinDSL::CreateThenInsertContainerThenPopulate::k";
  protected final String _parameterValueKey_21 = "statement_insertions_henshin_text::HenshinDSL::CreateThenInsertContainerThenPopulate::cnd";
  protected final String _parameterValueKey_22 = "statement_insertions_henshin_text::PopulateAnyEmptyContainer::k";
  protected final String _parameterValueKey_23 = "statement_insertions_henshin_text::PopulateAnyEmptyContainer::cnd";
  protected final String _parameterValueKey_24 = "statement_insertions_henshin_text::PopulateEmptyContainerWithAtomic::k";
  protected final String _parameterValueKey_25 = "statement_insertions_henshin_text::PopulateEmptyContainerWithIf::cnd";
  protected final String _parameterValueKey_26 = "statement_insertions_henshin_text::HenshinDSL::PopulateAnyEmptyContainer::k";
  protected final String _parameterValueKey_27 = "statement_insertions_henshin_text::HenshinDSL::PopulateAnyEmptyContainer::cnd";
  protected final String _parameterValueKey_28 = "statement_insertions_henshin_text::HenshinDSL::PopulateEmptyContainerWithAtomic::k";
  protected final String _parameterValueKey_29 = "statement_insertions_henshin_text::HenshinDSL::PopulateEmptyContainerWithIf::cnd";

  protected final ITransformationRepairer solutionRepairer = new TransformationPlaceholderRepairer();

  protected String baseName;

  protected int getPopulationSize() {
    try {
      int v = Integer.parseInt(System.getProperty("blocky.populationSize", "50"));
      return v > 0 ? v : 50;
    } catch (Exception ignored) {
      return 50;
    }
  }

  protected int getMaxEvaluations() {
    try {
      int v = Integer.parseInt(System.getProperty("blocky.maxEvaluations", "2000"));
      return v > 0 ? v : 2000;
    } catch (Exception ignored) {
      return 2000;
    }
  }

  protected int getNrRuns() {
    try {
      int v = Integer.parseInt(System.getProperty("blocky.nrRuns", "10"));
      return v > 0 ? v : 10;
    } catch (Exception ignored) {
      return 10;
    }
  }

  protected IParameterValue<?> _createParameterValue_0() { return new RandomAtomicKindLiteralValue(); }
  protected IParameterValue<?> _createParameterValue_1() { return new RandomConditionKindLiteralValue(); }
  protected IParameterValue<?> _createParameterValue_2() { return new RandomAtomicKindLiteralValue(); }
  protected IParameterValue<?> _createParameterValue_3() { return new RandomConditionKindLiteralValue(); }
  protected IParameterValue<?> _createParameterValue_4() { return new RandomAtomicKindLiteralValue(); }
  protected IParameterValue<?> _createParameterValue_5() { return new RandomConditionKindLiteralValue(); }
  protected IParameterValue<?> _createParameterValue_6() { return new RandomAtomicKindLiteralValue(); }
  protected IParameterValue<?> _createParameterValue_7() { return new RandomConditionKindLiteralValue(); }
  protected IParameterValue<?> _createParameterValue_8() { return new RandomAtomicKindLiteralValue(); }
  protected IParameterValue<?> _createParameterValue_9() { return new RandomConditionKindLiteralValue(); }
  protected IParameterValue<?> _createParameterValue_10() { return new RandomAtomicKindLiteralValue(); }
  protected IParameterValue<?> _createParameterValue_11() { return new RandomConditionKindLiteralValue(); }
  protected IParameterValue<?> _createParameterValue_12() { return new RandomAtomicKindLiteralValue(); }
  protected IParameterValue<?> _createParameterValue_13() { return new RandomConditionKindLiteralValue(); }
  protected IParameterValue<?> _createParameterValue_14() { return new RandomAtomicKindLiteralValue(); }
  protected IParameterValue<?> _createParameterValue_15() { return new RandomConditionKindLiteralValue(); }
  protected IParameterValue<?> _createParameterValue_16() { return new RandomAtomicKindLiteralValue(); }
  protected IParameterValue<?> _createParameterValue_17() { return new RandomConditionKindLiteralValue(); }
  protected IParameterValue<?> _createParameterValue_18() { return new RandomAtomicKindLiteralValue(); }
  protected IParameterValue<?> _createParameterValue_19() { return new RandomConditionKindLiteralValue(); }
  protected IParameterValue<?> _createParameterValue_20() { return new RandomAtomicKindLiteralValue(); }
  protected IParameterValue<?> _createParameterValue_21() { return new RandomConditionKindLiteralValue(); }
  protected IParameterValue<?> _createParameterValue_22() { return new RandomAtomicKindLiteralValue(); }
  protected IParameterValue<?> _createParameterValue_23() { return new RandomConditionKindLiteralValue(); }
  protected IParameterValue<?> _createParameterValue_24() { return new RandomAtomicKindLiteralValue(); }
  protected IParameterValue<?> _createParameterValue_25() { return new RandomConditionKindLiteralValue(); }
  protected IParameterValue<?> _createParameterValue_26() { return new RandomAtomicKindLiteralValue(); }
  protected IParameterValue<?> _createParameterValue_27() { return new RandomConditionKindLiteralValue(); }
  protected IParameterValue<?> _createParameterValue_28() { return new RandomAtomicKindLiteralValue(); }
  protected IParameterValue<?> _createParameterValue_29() { return new RandomConditionKindLiteralValue(); }

  // Objective helpers + objective creation methods are identical to the generated runner.
  protected double _createObjectiveHelper_0(final TransformationSolution solution, final EGraph graph, final EObject root) {
    double _xtrycatchfinallyexpression = (double) 0;
    try {
      double _xblockexpression = (double) 0;
      {
        final Game game = ((Game) root);
        Level _xifexpression = null;
        boolean _isEmpty = game.getLevels().isEmpty();
        if (_isEmpty) {
          _xifexpression = null;
        } else {
          _xifexpression = game.getLevels().get(0);
        }
        final Level level = _xifexpression;
        GameStatus _xifexpression_1 = null;
        boolean _equals = Objects.equals(level, null);
        if (_equals) {
          _xifexpression_1 = GameStatus.CRASHED;
        } else {
          _xifexpression_1 = BlockySimulator.run(level);
        }
        final GameStatus status = _xifexpression_1;
        double _xifexpression_2 = (double) 0;
        boolean _equals_1 = Objects.equals(status, GameStatus.WON);
        if (_equals_1) {
          _xifexpression_2 = 1.0;
        } else {
          _xifexpression_2 = 0.0;
        }
        final double v = _xifexpression_2;
        double _xifexpression_3 = (double) 0;
        boolean _isFinite = Double.isFinite(v);
        if (_isFinite) {
          _xifexpression_3 = v;
        } else {
          _xifexpression_3 = 0.0;
        }
        _xblockexpression = _xifexpression_3;
      }
      _xtrycatchfinallyexpression = _xblockexpression;
    } catch (final Throwable _t) {
      if (_t instanceof Throwable) {
        _xtrycatchfinallyexpression = 0.0;
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
    return _xtrycatchfinallyexpression;
  }

  protected IFitnessDimension<TransformationSolution> _createObjective_0(final TransformationSearchOrchestration orchestration) {
    return new AbstractEGraphFitnessDimension("GoalReached", at.ac.tuwien.big.moea.search.fitness.dimension.IFitnessDimension.FunctionType.Maximum) {
       @Override
       protected double internalEvaluate(TransformationSolution solution) {
          EGraph graph = solution.execute();
          EObject root = MomotUtil.getRoot(graph);
          return _createObjectiveHelper_0(solution, graph, root);
       }
    };
  }

  protected double _createObjectiveHelper_1(final TransformationSolution solution, final EGraph graph, final EObject root) {
    double _xtrycatchfinallyexpression = (double) 0;
    try {
      double _xblockexpression = (double) 0;
      {
        int _distanceToBaseline = BlockyProgramDistance.distanceToBaseline(((Game) root));
        final double v = ((double) _distanceToBaseline);
        double _xifexpression = (double) 0;
        boolean _isFinite = Double.isFinite(v);
        if (_isFinite) {
          _xifexpression = v;
        } else {
          _xifexpression = 1000000000.0;
        }
        _xblockexpression = _xifexpression;
      }
      _xtrycatchfinallyexpression = _xblockexpression;
    } catch (final Throwable _t) {
      if (_t instanceof Throwable) {
        _xtrycatchfinallyexpression = 1000000000.0;
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
    return _xtrycatchfinallyexpression;
  }

  protected IFitnessDimension<TransformationSolution> _createObjective_1(final TransformationSearchOrchestration orchestration) {
    return new AbstractEGraphFitnessDimension("Edits", at.ac.tuwien.big.moea.search.fitness.dimension.IFitnessDimension.FunctionType.Minimum) {
       @Override
       protected double internalEvaluate(TransformationSolution solution) {
          EGraph graph = solution.execute();
          EObject root = MomotUtil.getRoot(graph);
          return _createObjectiveHelper_1(solution, graph, root);
       }
    };
  }

  protected double _createObjectiveHelper_2(final TransformationSolution solution, final EGraph graph, final EObject root) {
    double _xtrycatchfinallyexpression = (double) 0;
    try {
      double _xblockexpression = (double) 0;
      {
        final Game game = ((Game) root);
        Level _xifexpression = null;
        boolean _isEmpty = game.getLevels().isEmpty();
        if (_isEmpty) {
          _xifexpression = null;
        } else {
          _xifexpression = game.getLevels().get(0);
        }
        final Level level = _xifexpression;
        double _xifexpression_1 = (double) 0;
        boolean _equals = Objects.equals(level, null);
        if (_equals) {
          _xifexpression_1 = 100000.0;
        } else {
          int _stepsToGoalOrPenalty = BlockySimulator.stepsToGoalOrPenalty(level);
          _xifexpression_1 = ((double) _stepsToGoalOrPenalty);
        }
        final double v = _xifexpression_1;
        double _xifexpression_2 = (double) 0;
        boolean _isFinite = Double.isFinite(v);
        if (_isFinite) {
          _xifexpression_2 = v;
        } else {
          _xifexpression_2 = 1000000000.0;
        }
        _xblockexpression = _xifexpression_2;
      }
      _xtrycatchfinallyexpression = _xblockexpression;
    } catch (final Throwable _t) {
      if (_t instanceof Throwable) {
        _xtrycatchfinallyexpression = 1000000000.0;
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
    return _xtrycatchfinallyexpression;
  }

  protected IFitnessDimension<TransformationSolution> _createObjective_2(final TransformationSearchOrchestration orchestration) {
    return new AbstractEGraphFitnessDimension("ShortestPath", at.ac.tuwien.big.moea.search.fitness.dimension.IFitnessDimension.FunctionType.Minimum) {
       @Override
       protected double internalEvaluate(TransformationSolution solution) {
          EGraph graph = solution.execute();
          EObject root = MomotUtil.getRoot(graph);
          return _createObjectiveHelper_2(solution, graph, root);
       }
    };
  }

  protected double _createObjectiveHelper_3(final TransformationSolution solution, final EGraph graph, final EObject root) {
    double _xtrycatchfinallyexpression = (double) 0;
    try {
      double _xblockexpression = (double) 0;
      {
        final Game game = ((Game) root);
        Level _xifexpression = null;
        boolean _isEmpty = game.getLevels().isEmpty();
        if (_isEmpty) {
          _xifexpression = null;
        } else {
          _xifexpression = game.getLevels().get(0);
        }
        final Level level = _xifexpression;
        double _xifexpression_1 = (double) 0;
        boolean _equals = Objects.equals(level, null);
        if (_equals) {
          _xifexpression_1 = 100000.0;
        } else {
          int _distanceToGoalOrPenalty = BlockySimulator.distanceToGoalOrPenalty(level);
          _xifexpression_1 = ((double) _distanceToGoalOrPenalty);
        }
        final double v = _xifexpression_1;
        double _xifexpression_2 = (double) 0;
        boolean _isFinite = Double.isFinite(v);
        if (_isFinite) {
          _xifexpression_2 = v;
        } else {
          _xifexpression_2 = 1000000000.0;
        }
        _xblockexpression = _xifexpression_2;
      }
      _xtrycatchfinallyexpression = _xblockexpression;
    } catch (final Throwable _t) {
      if (_t instanceof Throwable) {
        _xtrycatchfinallyexpression = 1000000000.0;
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
    return _xtrycatchfinallyexpression;
  }

  protected IFitnessDimension<TransformationSolution> _createObjective_3(final TransformationSearchOrchestration orchestration) {
    return new AbstractEGraphFitnessDimension("DistanceToGoal", at.ac.tuwien.big.moea.search.fitness.dimension.IFitnessDimension.FunctionType.Minimum) {
       @Override
       protected double internalEvaluate(TransformationSolution solution) {
          EGraph graph = solution.execute();
          EObject root = MomotUtil.getRoot(graph);
          return _createObjectiveHelper_3(solution, graph, root);
       }
    };
  }

  protected double _createObjectiveHelper_4(final TransformationSolution solution, final EGraph graph, final EObject root) {
    double _xtrycatchfinallyexpression = (double) 0;
    try {
      double _xblockexpression = (double) 0;
      {
        final Game game = ((Game) root);
        Level _xifexpression = null;
        boolean _isEmpty = game.getLevels().isEmpty();
        if (_isEmpty) {
          _xifexpression = null;
        } else {
          _xifexpression = game.getLevels().get(0);
        }
        final Level level = _xifexpression;
        boolean _equals = Objects.equals(level, null);
        if (_equals) {
          return 1000.0;
        }
        final int loops = BlockyProgramMetrics.countLoops(game);
        final int conds = BlockyProgramMetrics.countConditionals(game);
        final boolean violates = (((!level.isAllowLoops()) && (loops > 0)) || ((!level.isAllowConditionals()) && (conds > 0)));
        double _xifexpression_1 = (double) 0;
        if (violates) {
          _xifexpression_1 = 1000.0;
        } else {
          _xifexpression_1 = 0.0;
        }
        final double v = _xifexpression_1;
        double _xifexpression_2 = (double) 0;
        boolean _isFinite = Double.isFinite(v);
        if (_isFinite) {
          _xifexpression_2 = v;
        } else {
          _xifexpression_2 = 1000.0;
        }
        _xblockexpression = _xifexpression_2;
      }
      _xtrycatchfinallyexpression = _xblockexpression;
    } catch (final Throwable _t) {
      if (_t instanceof Throwable) {
        _xtrycatchfinallyexpression = 1000.0;
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
    return _xtrycatchfinallyexpression;
  }

  protected IFitnessDimension<TransformationSolution> _createObjective_4(final TransformationSearchOrchestration orchestration) {
    return new AbstractEGraphFitnessDimension("AllowControlFlowPenalty", at.ac.tuwien.big.moea.search.fitness.dimension.IFitnessDimension.FunctionType.Minimum) {
       @Override
       protected double internalEvaluate(TransformationSolution solution) {
          EGraph graph = solution.execute();
          EObject root = MomotUtil.getRoot(graph);
          return _createObjectiveHelper_4(solution, graph, root);
       }
    };
  }

  protected double _createObjectiveHelper_5(final TransformationSolution solution, final EGraph graph, final EObject root) {
    double _xtrycatchfinallyexpression = (double) 0;
    try {
      double _xblockexpression = (double) 0;
      {
        final Game game = ((Game) root);
        Level _xifexpression = null;
        boolean _isEmpty = game.getLevels().isEmpty();
        if (_isEmpty) {
          _xifexpression = null;
        } else {
          _xifexpression = game.getLevels().get(0);
        }
        final Level level = _xifexpression;
        boolean _equals = Objects.equals(level, null);
        if (_equals) {
          return 1000.0;
        }
        final int maxBlocks = level.getMaxBlocks();
        if ((maxBlocks <= 0)) {
          return 0.0;
        }
        final int blocks = BlockyProgramMetrics.countStatements(game);
        double _xifexpression_1 = (double) 0;
        if ((blocks <= maxBlocks)) {
          _xifexpression_1 = 0.0;
        } else {
          _xifexpression_1 = ((double) (blocks - maxBlocks));
        }
        final double v = _xifexpression_1;
        double _xifexpression_2 = (double) 0;
        boolean _isFinite = Double.isFinite(v);
        if (_isFinite) {
          _xifexpression_2 = v;
        } else {
          _xifexpression_2 = 1000.0;
        }
        _xblockexpression = _xifexpression_2;
      }
      _xtrycatchfinallyexpression = _xblockexpression;
    } catch (final Throwable _t) {
      if (_t instanceof Throwable) {
        _xtrycatchfinallyexpression = 1000.0;
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
    return _xtrycatchfinallyexpression;
  }

  protected IFitnessDimension<TransformationSolution> _createObjective_5(final TransformationSearchOrchestration orchestration) {
    return new AbstractEGraphFitnessDimension("MaxBlocksPenalty", at.ac.tuwien.big.moea.search.fitness.dimension.IFitnessDimension.FunctionType.Minimum) {
       @Override
       protected double internalEvaluate(TransformationSolution solution) {
          EGraph graph = solution.execute();
          EObject root = MomotUtil.getRoot(graph);
          return _createObjectiveHelper_5(solution, graph, root);
       }
    };
  }

  protected double _createObjectiveHelper_6(final TransformationSolution solution, final EGraph graph, final EObject root) {
    double _xtrycatchfinallyexpression = (double) 0;
    try {
      double _xblockexpression = (double) 0;
      {
        final Game game = ((Game) root);
        Level _xifexpression = null;
        if (game.getLevels().isEmpty()) {
          _xifexpression = null;
        } else {
          _xifexpression = game.getLevels().get(0);
        }
        final Level level = _xifexpression;
        double v = (level == null) ? 100000.0 : (double) BlockySimulator.closestToGoalOrPenalty(level, 100000);
        if (!Double.isFinite(v)) v = 1000000000.0;
        _xblockexpression = v;
      }
      _xtrycatchfinallyexpression = _xblockexpression;
    } catch (final Throwable _t) {
      _xtrycatchfinallyexpression = 1000000000.0;
    }
    return _xtrycatchfinallyexpression;
  }

  protected IFitnessDimension<TransformationSolution> _createObjective_6(final TransformationSearchOrchestration orchestration) {
    return new AbstractEGraphFitnessDimension("closestToGoal", at.ac.tuwien.big.moea.search.fitness.dimension.IFitnessDimension.FunctionType.Minimum) {
       @Override
       protected double internalEvaluate(TransformationSolution solution) {
          EGraph graph = solution.execute();
          EObject root = MomotUtil.getRoot(graph);
          return _createObjectiveHelper_6(solution, graph, root);
       }
    };
  }

  protected ModuleManager createModuleManager() {
    ModuleManager manager = new ModuleManager();
    for(String module : modules) {
       manager.addModule(URI.createFileURI(new File(module).getPath().toString()).toString());
    }
    manager.removeUnits(unitsToRemove);
    manager.setParameterValue(_parameterValueKey_0, _createParameterValue_0());
    manager.setParameterValue(_parameterValueKey_1, _createParameterValue_1());
    manager.setParameterValue(_parameterValueKey_2, _createParameterValue_2());
    manager.setParameterValue(_parameterValueKey_3, _createParameterValue_3());
    manager.setParameterValue(_parameterValueKey_4, _createParameterValue_4());
    manager.setParameterValue(_parameterValueKey_5, _createParameterValue_5());
    manager.setParameterValue(_parameterValueKey_6, _createParameterValue_6());
    manager.setParameterValue(_parameterValueKey_7, _createParameterValue_7());
    manager.setParameterValue(_parameterValueKey_8, _createParameterValue_8());
    manager.setParameterValue(_parameterValueKey_9, _createParameterValue_9());
    manager.setParameterValue(_parameterValueKey_10, _createParameterValue_10());
    manager.setParameterValue(_parameterValueKey_11, _createParameterValue_11());
    manager.setParameterValue(_parameterValueKey_12, _createParameterValue_12());
    manager.setParameterValue(_parameterValueKey_13, _createParameterValue_13());
    manager.setParameterValue(_parameterValueKey_14, _createParameterValue_14());
    manager.setParameterValue(_parameterValueKey_15, _createParameterValue_15());
    manager.setParameterValue(_parameterValueKey_16, _createParameterValue_16());
    manager.setParameterValue(_parameterValueKey_17, _createParameterValue_17());
    manager.setParameterValue(_parameterValueKey_18, _createParameterValue_18());
    manager.setParameterValue(_parameterValueKey_19, _createParameterValue_19());
    manager.setParameterValue(_parameterValueKey_20, _createParameterValue_20());
    manager.setParameterValue(_parameterValueKey_21, _createParameterValue_21());
    manager.setParameterValue(_parameterValueKey_22, _createParameterValue_22());
    manager.setParameterValue(_parameterValueKey_23, _createParameterValue_23());
    manager.setParameterValue(_parameterValueKey_24, _createParameterValue_24());
    manager.setParameterValue(_parameterValueKey_25, _createParameterValue_25());
    manager.setParameterValue(_parameterValueKey_26, _createParameterValue_26());
    manager.setParameterValue(_parameterValueKey_27, _createParameterValue_27());
    manager.setParameterValue(_parameterValueKey_28, _createParameterValue_28());
    manager.setParameterValue(_parameterValueKey_29, _createParameterValue_29());
    return manager;
  }

  protected IEGraphMultiDimensionalFitnessFunction createFitnessFunction(final TransformationSearchOrchestration orchestration) {
    IEGraphMultiDimensionalFitnessFunction function = fitnessFunction;
    function.addObjective(_createObjective_0(orchestration));
    function.addObjective(_createObjective_1(orchestration));
    function.addObjective(_createObjective_2(orchestration));
    function.addObjective(_createObjective_6(orchestration));
    function.setSolutionRepairer(solutionRepairer);
    return function;
  }

  protected IRegisteredAlgorithm<NSGAII> _createRegisteredAlgorithm_0(final TransformationSearchOrchestration orchestration, final EvolutionaryAlgorithmFactory<TransformationSolution> moea, final LocalSearchAlgorithmFactory<TransformationSolution> local) {
    TournamentSelection _tournamentSelection = new TournamentSelection(2);
    OnePointCrossover _onePointCrossover = new OnePointCrossover(1.0);
    TransformationPlaceholderMutation _transformationPlaceholderMutation = new TransformationPlaceholderMutation(0.15);
    ModuleManager _moduleManager = orchestration.getModuleManager();
    TransformationParameterMutation _transformationParameterMutation = new TransformationParameterMutation(0.1, _moduleManager);
    IRegisteredAlgorithm<NSGAII> _createNSGAII = moea.createNSGAII(_tournamentSelection, _onePointCrossover, _transformationPlaceholderMutation, _transformationParameterMutation);
    return _createNSGAII;
  }

  protected ProgressListener _createListener_0() { return new SeedRuntimePrintListener(); }

  protected EGraph createInputGraph(final String initialGraph, final ModuleManager moduleManager) {
    EGraph graph = moduleManager.loadGraph(initialGraph);
    return adaptInputGraph(moduleManager, graph);
  }

  protected EGraph adaptInputGraph(final ModuleManager moduleManager, final EGraph initialGraph) {
    EGraph problemGraph = MomotUtil.copy(initialGraph);
    EObject root = MomotUtil.getRoot(problemGraph);
    return MomotUtil.createEGraph(adaptInputModel(root));
  }

  protected EObject adaptInputModel(final EObject root) {
    final Game game = ((Game) root);
    Resource _eResource = game.eResource();
    boolean _tripleNotEquals = (_eResource != null);
    if (_tripleNotEquals) {
      final ResourceSet rs = game.eResource().getResourceSet();
      rs.getPackageRegistry().put(BlockyPackage.eINSTANCE.getNsURI(), BlockyPackage.eINSTANCE);
      rs.getPackageRegistry().put(BlockyPackage.eINSTANCE.getName(), BlockyPackage.eINSTANCE);
    }
    return root;
  }

  protected TransformationSearchOrchestration createOrchestration(final String initialGraph, final int solutionLength) {
    TransformationSearchOrchestration orchestration = new TransformationSearchOrchestration();
    ModuleManager moduleManager = createModuleManager();
    EGraph graph = createInputGraph(initialGraph, moduleManager);
    orchestration.setModuleManager(moduleManager);
    orchestration.setProblemGraph(graph);
    orchestration.setSolutionLength(solutionLength);
    orchestration.setFitnessFunction(createFitnessFunction(orchestration));

    EvolutionaryAlgorithmFactory<TransformationSolution> moea = orchestration.createEvolutionaryAlgorithmFactory(getPopulationSize());
    LocalSearchAlgorithmFactory<TransformationSolution> local = orchestration.createLocalSearchAlgorithmFactory();
    orchestration.addAlgorithm("NSGA_II", _createRegisteredAlgorithm_0(orchestration, moea, local));

    return orchestration;
  }

  protected SearchExperiment<TransformationSolution> createExperiment(final TransformationSearchOrchestration orchestration) {
    SearchExperiment<TransformationSolution> experiment = new SearchExperiment<TransformationSolution>(orchestration, getMaxEvaluations());
    experiment.setNumberOfRuns(getNrRuns());
    experiment.addProgressListener(_createListener_0());
    return experiment;
  }

  protected void deriveBaseName(final TransformationSearchOrchestration orchestration) {
    EObject root = MomotUtil.getRoot(orchestration.getProblemGraph());
    if(root == null || root.eResource() == null || root.eResource().getURI() == null)
    	baseName = getClass().getSimpleName();
    else
    	baseName = root.eResource().getURI().trimFileExtension().lastSegment();
  }

  protected TransformationResultManager handleResults(final SearchExperiment<TransformationSolution> experiment) {
    ISolutionWriter<TransformationSolution> solutionWriter = experiment.getSearchOrchestration().createSolutionWriter();
    IPopulationWriter<TransformationSolution> populationWriter = experiment.getSearchOrchestration().createPopulationWriter();
    TransformationResultManager resultManager = new TransformationResultManager(experiment);
    Population population;
    population =
    	TransformationResultManager.createApproximationSet(experiment, (String[])null);
    System.out.println("- Save objectives of all algorithms to 'output/objectives.pf'");
    TransformationResultManager.saveObjectives(
    	"output/objectives.pf",
    	population
    );
    System.out.println("---------------------------");
    System.out.println("Objectives of all algorithms");
    System.out.println("---------------------------");
    System.out.println(TransformationResultManager.printObjectives(
    	population
    ));

    population =
    	TransformationResultManager.createApproximationSet(experiment, (String[])null);
    System.out.println("- Save solutions of all algorithms to 'output/solutions.txt'");
    TransformationResultManager.savePopulation(
    	"output/solutions.txt",
    	population,
    	populationWriter
    );
    System.out.println("- Save solutions of all algorithms to 'output/solutions.txt'");
    TransformationResultManager.saveSolutions(
    	"output/solutions/",
    	baseName,
    	MomotUtil.asIterables(
    		population,
    		TransformationSolution.class),
    	solutionWriter
    );

    population =
    	TransformationResultManager.createApproximationSet(experiment, (String[])null);
    System.out.println("- Save models of all algorithms to 'output/models/'");
    TransformationResultManager.saveModels(
    	"output/models/",
    	baseName,
    	population
    );

    return resultManager;
  }

  public void printSearchInfo(final TransformationSearchOrchestration orchestration, final String inputModelPath) {
    System.out.println("-------------------------------------------------------");
    System.out.println("Search");
    System.out.println("-------------------------------------------------------");
    System.out.println("InputModel:      " + inputModelPath);
    System.out.println("Objectives:      " + orchestration.getFitnessFunction().getObjectiveNames());
    System.out.println("NrObjectives:    " + orchestration.getNumberOfObjectives());
    System.out.println("Constraints:     " + orchestration.getFitnessFunction().getConstraintNames());
    System.out.println("NrConstraints:   " + orchestration.getNumberOfConstraints());
    System.out.println("Transformations: " + Arrays.toString(modules));
    System.out.println("Units:           " + orchestration.getModuleManager().getUnits());
    System.out.println("SolutionLength:  " + orchestration.getSolutionLength());
    int pop = getPopulationSize();
    int evals = getMaxEvaluations();
    int runs = getNrRuns();
    System.out.println("SysProps:        blocky.populationSize=" + System.getProperty("blocky.populationSize")
        + " blocky.maxEvaluations=" + System.getProperty("blocky.maxEvaluations")
        + " blocky.nrRuns=" + System.getProperty("blocky.nrRuns")
        + " blocky.seed=" + System.getProperty("blocky.seed"));
    System.out.println("PopulationSize:  " + pop);
    System.out.println("Iterations:      " + (evals / Math.max(1, pop)));
    System.out.println("MaxEvaluations:  " + evals);
    System.out.println("AlgorithmRuns:   " + runs);
    System.out.println("Seed:            " + System.getProperty("blocky.seed", "0 (auto)"));
    System.out.println("---------------------------");
  }

  public void performSearch(final String initialGraph, final int solutionLength) {
    TransformationSearchOrchestration orchestration = createOrchestration(initialGraph, solutionLength);
    deriveBaseName(orchestration);
    printSearchInfo(orchestration, initialGraph);
    SearchExperiment<TransformationSolution> experiment = createExperiment(orchestration);
    experiment.run();
    System.out.println("-------------------------------------------------------");
    System.out.println("Results");
    System.out.println("-------------------------------------------------------");
    handleResults(experiment);
  }

  public static void initialization(final String absInputModelPath) {
    System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
    final BlockyPackage pkg = BlockyPackage.eINSTANCE;
    EPackage.Registry.INSTANCE.put(pkg.getNsURI(), pkg);
    EPackage.Registry.INSTANCE.put(pkg.getName(), pkg);
    pkg.eClass();
    BlockyProgramDistance.initializeBaseline(absInputModelPath);
    BlockySimulator.initialize(absInputModelPath);

    final String seedStr = System.getProperty("blocky.seed", "0");
    try {
      final long seed = Long.parseLong(seedStr);
      if (seed != 0L) {
        PRNG.setSeed(seed);
      }
    } catch (Exception ignored) {
    }
  }

  public static void main(final String... args) {
    String in = (args != null && args.length > 0 && args[0] != null && !args[0].isBlank())
            ? args[0]
            : System.getProperty("blocky.input", input);

    String abs = new File(in).getAbsoluteFile().getPath();
    System.setProperty("blocky.input", abs);
    input = abs;

    initialization(abs);
    int solLen = Math.max(1, BlockyProgramMetrics.inferSolutionLength(abs) * 2);
    blocky_custom search = new blocky_custom();
    search.performSearch(abs, solLen);
  }
}

