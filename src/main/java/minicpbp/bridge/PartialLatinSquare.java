package minicpbp.bridge;

import minicpbp.cp.Factory;
import minicpbp.engine.core.IntVar;
import minicpbp.engine.core.Solver;
import minicpbp.search.DFSearch;
import minicpbp.search.SearchStatistics;

import static minicpbp.cp.BranchingScheme.firstFail;

public class PartialLatinSquare {

    public Solver buildSolver(){
        // Create solver
        Solver solver = Factory.makeSolver(false);
        return solver;
    }
    public IntVar[] buildModel(Solver solver, int board_size, int[] square, boolean remove_columns_domains) {
        // Creates the variables.
        int nVar = board_size * board_size;
        IntVar[] assigned = new IntVar[nVar];
        for (int i = 0; i < nVar; i++) {
            assigned[i] = Factory.makeIntVar(solver, 1, board_size);
            for (int j = 0; j < board_size;j++){
                if (square[i*board_size+j] == 1) {
                    assigned[i].assign(j + 1);
                }
            }
        }

        // Creates the constraints.
        // All numbers in the same row must be different.
        for (int i = 0; i < nVar; i += board_size) {
            IntVar[] array = new IntVar[board_size];
            System.arraycopy(assigned, i, array, 0, board_size);
            solver.post(Factory.allDifferentAC(array));
        }
        if (!remove_columns_domains) {
            // All numbers in the same column must be different
            for (int i = 0; i < board_size; i++) {
                IntVar[] array = new IntVar[board_size];
                for (int j = 0; j < board_size; j++) {
                    array[j] = assigned[i + j * board_size];
                }
                solver.post(Factory.allDifferentAC(array));
            }
        }

        return assigned;
    }
    public void remove_from_domain(IntVar x, int v){
        x.remove(v);
    }

    public double[] propagate_believes(Solver solver, IntVar[] assigned, int n){
        solver.beliefPropa();
        double[] believes = new double[assigned.length * n];
        for (int i = 0; i < assigned.length; i++){
            for (int j = 0; j < n; j++) {
                if (assigned[i].contains(j+1)) {
                    believes[i * n + j] = assigned[i].marginal(j + 1);
                } else {
                    believes[i * n + j] = 0;
                }
            }
        }
        return believes;
    }

    public boolean test_is_feasible(Solver solver, IntVar[] assigned, long max_time_in_millis){
        DFSearch search = Factory.makeDfs(solver, firstFail(assigned));
        SearchStatistics stats = search.solve(statistics -> statistics.numberOfSolutions() == 1 || statistics.timeElapsed() >= max_time_in_millis);
        return (stats.numberOfSolutions() == 1) || (stats.timeElapsed() >= max_time_in_millis);
    }

    public int[] new_array(int size){
        return new int[size];
    }
}
