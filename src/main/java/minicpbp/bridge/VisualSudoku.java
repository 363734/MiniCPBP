package minicpbp.bridge;

import minicpbp.cp.Factory;
import minicpbp.engine.constraints.Oracle;
import minicpbp.engine.core.IntVar;
import minicpbp.engine.core.Solver;
import minicpbp.search.DFSearch;
import minicpbp.search.Objective;
import minicpbp.search.SearchStatistics;
import minicpbp.util.Procedure;
import minicpbp.util.exception.InconsistencyException;

import java.util.function.Supplier;

import static minicpbp.cp.BranchingScheme.maxMarginal;
import static minicpbp.cp.BranchingScheme.maxMarginalStrength;

public class VisualSudoku {

    public Solver buildSolver(int iter,boolean damping) {
        // Create solver
        Solver solver = Factory.makeSolver(false, false, iter, damping);
        return solver;
    }

    public static IntVar[][] sudoku_model(Solver solver, int n, int b) {
        IntVar[][] grid = new IntVar[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                grid[i][j] = Factory.makeIntVar(solver, 1, n);
            }
        }
        // AllDiff line
        for (int i = 0; i < n; i++) {
            solver.post(Factory.allDifferentAC(grid[i]));
        }
        // AllDiff column
        for (int j = 0; j < n; j++) {
            IntVar[] col = new IntVar[n];
            for (int i = 0; i < n; i++) {
                col[i] = grid[i][j];
            }
            solver.post(Factory.allDifferentAC(col));
        }
        // AllDiff square
        for (int i = 0; i < n; i += b) {
            for (int j = 0; j < n; j += b) {
                IntVar[] square = new IntVar[n];
                for (int di = 0; di < b; di++) {
                    for (int dj = 0; dj < b; dj++) {
                        square[di * b + dj] = grid[i + di][j + dj];
                    }
                }
                solver.post(Factory.allDifferentAC(square));
            }
        }

        return grid;
    }

    public static void assignSquare(Solver solver, IntVar var, int value) {
        solver.post(Factory.equal(var, value));
    }

    public static void removeSquare(Solver solver, IntVar var, int value) {
        solver.post(Factory.notEqual(var, value));
    }

    public static void assignOracle(Solver solver, IntVar var, double[] oracle){
        int[] v = new int[oracle.length];
        for (int i = 1; i <= oracle.length;i++){
            v[i-1] = i;
        }
        solver.post(new Oracle(var,v,oracle));
    }

    public static void removeSquares(Solver solver, IntVar var, int[] values) {
        for (int value : values) {
            solver.post(Factory.notEqual(var, value));
        }
    }

    public int[][] solve(Solver solver, IntVar[][] grid, boolean maxMarg) {
        int n = grid.length;
        IntVar[] vars = new IntVar[n * n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                vars[n * i + j] = grid[i][j];
            }
        }
        int[][] sol = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                sol[i][j] = 0;
            }
        }
        Supplier<Procedure[]> proc = (maxMarg ? maxMarginal(vars) : maxMarginalStrength(vars));
        DFSearch search = Factory.makeDfs(solver, proc);
        search.onSolution(() -> {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    sol[i][j] = vars[n * i + j].min();
                }
            }
        });
        try {
            SearchStatistics stats = search.solve(statistics -> statistics.numberOfSolutions() == 1);
        } catch (InconsistencyException e){
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    sol[i][j] = 0;
                }
            }
        }
        return sol;
    }

    public int[][] solve_init(Solver solver, IntVar[][] grid) {
        int n = grid.length;
        IntVar[] vars = new IntVar[n * n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                vars[n * i + j] = grid[i][j];
            }
        }
        solver.fixPoint();
        solver.beliefPropa();

        int[][] sol = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                sol[i][j] = vars[n * i + j].valueWithMaxMarginal();;
            }
        }
        return sol;
    }

    public void add_obj(Solver solver, IntVar[][] grid,int[][] weights) {
        Objective obj = solver.minimize(Factory.sum());

    }

    public boolean solve_is_unique_sol(Solver solver, IntVar[][] grid, boolean maxMarg) {
        int n = grid.length;
        IntVar[] vars = new IntVar[n * n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                vars[n * i + j] = grid[i][j];
            }
        }
        int[][] sol = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                sol[i][j] = 0;
            }
        }
        Supplier<Procedure[]> proc = (maxMarg ? maxMarginal(vars) : maxMarginalStrength(vars));
        DFSearch search = Factory.makeDfs(solver, proc);
        search.onSolution(() -> {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    sol[i][j] = vars[n * i + j].min();
                }
            }
        });
        SearchStatistics stats = null;
        try {
            stats = search.solve(statistics -> statistics.numberOfSolutions() == 2);
        } catch (InconsistencyException e){
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    sol[i][j] = 0;
                }
            }
        }
        return stats.numberOfSolutions() == 1;
    }

    public int[] new_array(int size) {
        return new int[size];
    }

    public double[] new_array_double(int size) {
        return new double[size];
    }
}
