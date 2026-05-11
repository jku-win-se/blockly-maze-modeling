package blocky_momot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import blocky.AtomicStatement;
import blocky.BlockyPackage;
import blocky.Body;
import blocky.Game;
import blocky.IfStmt;
import blocky.Level;
import blocky.Loop;
import blocky.Container;
import blocky.Statement;

/**
 * "Closeness to input model" for the Blocky program subgraph (Level.solution).
 *
 * This implements a tree edit distance counted in user-visible blocks. The blocks the
 * end user sees in the editor are exactly the {@link Statement} subtypes:
 * <ul>
 *   <li>{@link AtomicStatement} (move forward / turn left / turn right),</li>
 *   <li>{@link Loop} (repeat-until-goal), and</li>
 *   <li>{@link IfStmt} (if / if-else).</li>
 * </ul>
 * {@link Container} and {@link Body} are EMF plumbing and never count towards the distance.
 *
 * Each unit of distance corresponds to exactly one block edit:
 * <ul>
 *   <li>insert one block (insert cost = subtree size of the inserted block),</li>
 *   <li>delete one block (delete cost = subtree size of the deleted block), or</li>
 *   <li>relabel one block in place: {@link AtomicStatement} kind change costs 1, {@link IfStmt}
 *       condition change costs 1; identical labels cost 0.</li>
 * </ul>
 * Same-class substitution recurses into bodies (Loop body; IfStmt then-body and else-body).
 * Different-class substitution falls back to a full delete + insert, so swapping a leaf
 * statement for a large {@code Loop}/{@code IfStmt} subtree is charged for every new block.
 * Ordered sibling sequences are aligned with a standard sequence edit distance DP, so
 * matching statements at corresponding positions cost 0 even when surrounded by inserts/deletes.
 *
 * See: https://en.wikipedia.org/wiki/Graph_edit_distance
 */
public final class BlockyProgramDistance {
    private BlockyProgramDistance() {}

    private static volatile Body BASELINE_SOLUTION;

    /**
     * Load and cache the baseline solution from the given XMI file path.
     * The path may be relative (resolved against {@code user.dir}).
     */
    public static synchronized void initializeBaseline(String gameXmiPath) {
        if (BASELINE_SOLUTION != null) {
            return;
        }
        Game game = loadGame(gameXmiPath);
        BASELINE_SOLUTION = firstLevelSolutionOrNull(game);
    }

    public static int distanceToBaseline(Game currentGame) {
        Body baseline = BASELINE_SOLUTION;
        if (baseline == null) {
            // Defensive: if initialization wasn't called or failed, treat as "far away".
            return 100000;
        }
        Body current = firstLevelSolutionOrNull(currentGame);
        return programDistance(baseline, current);
    }

    private static Body firstLevelSolutionOrNull(Game game) {
        if (game == null || game.getLevels().isEmpty()) {
            return null;
        }
        Level level = game.getLevels().get(0);
        return level != null ? level.getSolution() : null;
    }

    private static Game loadGame(String path) {
        // Ensure XMI is supported in a plain ResourceSet.
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().putIfAbsent("xmi", new XMIResourceFactoryImpl());

        // Register Blocky metamodel.
        EPackage pkg = BlockyPackage.eINSTANCE;
        EPackage.Registry.INSTANCE.put(pkg.getNsURI(), pkg);
        EPackage.Registry.INSTANCE.put(pkg.getName(), pkg);

        ResourceSet rs = new ResourceSetImpl();
        rs.getPackageRegistry().put(pkg.getNsURI(), pkg);
        rs.getPackageRegistry().put(pkg.getName(), pkg);

        URI uri;
        File f = new File(path);
        if (f.isAbsolute()) {
            uri = URI.createFileURI(f.getAbsolutePath());
        } else {
            uri = URI.createFileURI(new File(System.getProperty("user.dir"), path).getAbsolutePath());
        }

        Resource r = rs.getResource(uri, true);
        try {
            r.load(null);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load baseline game XMI from: " + uri, e);
        }
        if (r.getContents().isEmpty() || !(r.getContents().get(0) instanceof Game)) {
            throw new IllegalStateException("Baseline XMI does not contain a blocky.Game root: " + uri);
        }
        return (Game) r.getContents().get(0);
    }

    /**
     * Distance between two program bodies (may be null).
     */
    public static int programDistance(Body a, Body b) {
        List<Statement> as = toSequence(a);
        List<Statement> bs = toSequence(b);

        Map<Statement, Integer> sizeCache = new IdentityHashMap<>();
        Map<PairKey, Integer> stmtDistCache = new IdentityHashMap<>();
        return sequenceDistance(as, bs, sizeCache, stmtDistCache);
    }

    private static List<Statement> toSequence(Body body) {
        List<Statement> out = new ArrayList<>();
        if (body == null) {
            return out;
        }
        Container c = body.getFirstContainer();
        while (c != null) {
            out.add(c.getStatement());
            c = c.getNext();
        }
        return out;
    }

    private static int sequenceDistance(
            List<Statement> a,
            List<Statement> b,
            Map<Statement, Integer> sizeCache,
            Map<PairKey, Integer> stmtDistCache) {

        int m = a.size();
        int n = b.size();
        int[][] dp = new int[m + 1][n + 1];

        dp[m][n] = 0;
        for (int i = m - 1; i >= 0; i--) {
            dp[i][n] = dp[i + 1][n] + deleteCost(a.get(i), sizeCache);
        }
        for (int j = n - 1; j >= 0; j--) {
            dp[m][j] = dp[m][j + 1] + insertCost(b.get(j), sizeCache);
        }

        for (int i = m - 1; i >= 0; i--) {
            for (int j = n - 1; j >= 0; j--) {
                int del = dp[i + 1][j] + deleteCost(a.get(i), sizeCache);
                int ins = dp[i][j + 1] + insertCost(b.get(j), sizeCache);
                int sub = dp[i + 1][j + 1] + statementSubstitutionCost(a.get(i), b.get(j), sizeCache, stmtDistCache);
                dp[i][j] = Math.min(del, Math.min(ins, sub));
            }
        }

        return dp[0][0];
    }

    private static int insertCost(Statement s, Map<Statement, Integer> sizeCache) {
        return subtreeSize(s, sizeCache);
    }

    private static int deleteCost(Statement s, Map<Statement, Integer> sizeCache) {
        return subtreeSize(s, sizeCache);
    }

    private static int statementSubstitutionCost(
            Statement a,
            Statement b,
            Map<Statement, Integer> sizeCache,
            Map<PairKey, Integer> stmtDistCache) {

        if (a == null && b == null) return 0;
        if (a == null) return insertCost(b, sizeCache);
        if (b == null) return deleteCost(a, sizeCache);

        PairKey key = new PairKey(a, b);
        Integer cached = stmtDistCache.get(key);
        if (cached != null) return cached;

        int cost;
        if (a.getClass() == b.getClass()) {
            // Same kind of block: pay only the relabel cost (0 or 1) plus the recursive
            // distance between corresponding child bodies.
            cost = 0;
            if (a instanceof AtomicStatement) {
                if (((AtomicStatement) a).getKind() != ((AtomicStatement) b).getKind()) {
                    cost += 1;
                }
            } else if (a instanceof Loop) {
                cost += programDistance(((Loop) a).getBody(), ((Loop) b).getBody(), sizeCache, stmtDistCache);
            } else if (a instanceof IfStmt) {
                if (((IfStmt) a).getCondition() != ((IfStmt) b).getCondition()) {
                    cost += 1;
                }
                cost += programDistance(((IfStmt) a).getThenBody(), ((IfStmt) b).getThenBody(), sizeCache, stmtDistCache);
                cost += programDistance(((IfStmt) a).getElseBody(), ((IfStmt) b).getElseBody(), sizeCache, stmtDistCache);
            }
        } else {
            // Different kind of block: there is no natural relabel-and-recurse, so charge the
            // honest cost of deleting the old block (and its subtree) and inserting the new one.
            cost = deleteCost(a, sizeCache) + insertCost(b, sizeCache);
        }

        stmtDistCache.put(key, cost);
        return cost;
    }

    private static int programDistance(
            Body a,
            Body b,
            Map<Statement, Integer> sizeCache,
            Map<PairKey, Integer> stmtDistCache) {
        return sequenceDistance(toSequence(a), toSequence(b), sizeCache, stmtDistCache);
    }

    private static int subtreeSize(Statement s, Map<Statement, Integer> cache) {
        if (s == null) return 0;
        Integer cached = cache.get(s);
        if (cached != null) return cached;

        int size = 1; // count this statement node
        if (s instanceof Loop) {
            size += subtreeSize(((Loop) s).getBody(), cache);
        } else if (s instanceof IfStmt) {
            size += subtreeSize(((IfStmt) s).getThenBody(), cache);
            size += subtreeSize(((IfStmt) s).getElseBody(), cache);
        }

        cache.put(s, size);
        return size;
    }

    private static int subtreeSize(Body b, Map<Statement, Integer> cache) {
        int size = 0;
        for (Statement s : toSequence(b)) {
            size += subtreeSize(s, cache);
        }
        return size;
    }

    private static final class PairKey {
        private final Statement a;
        private final Statement b;

        private PairKey(Statement a, Statement b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public int hashCode() {
            // Identity semantics: statements are EObjects; we want fast caching by object identity.
            return System.identityHashCode(a) * 31 + System.identityHashCode(b);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof PairKey)) return false;
            PairKey other = (PairKey) obj;
            return a == other.a && b == other.b;
        }
    }
}

