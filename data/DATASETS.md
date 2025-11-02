# Dataset Summary

Generated: Sun Nov 02 17:43:11 UTC 2025

## Small Datasets (6-10 vertices)
1. **small_dag.json**: Pure DAG, 8 vertices, ~10 edges
2. **small_cycle.json**: Contains 1-2 cycles, 7 vertices, ~12 edges
3. **small_sparse.json**: Sparse structure, 10 vertices, ~9 edges

## Medium Datasets (10-20 vertices)
4. **medium_mixed.json**: Mixed structure with SCCs, 15 vertices, ~25 edges
5. **medium_multiple_sccs.json**: Multiple distinct SCCs, 12 vertices, ~20 edges
6. **medium_dense.json**: Dense connectivity, 18 vertices, ~60 edges

## Large Datasets (20-50 vertices)
7. **large_sparse.json**: Sparse DAG for performance testing, 30 vertices, ~35 edges
8. **large_complex.json**: Complex structure with multiple SCCs, 40 vertices, ~80 edges
9. **large_dense.json**: Dense graph for stress testing, 25 vertices, ~100 edges

All datasets use edge weight model with weights ranging from 1 to 10.
