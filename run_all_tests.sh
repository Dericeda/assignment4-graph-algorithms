#!/bin/bash

# Run all tests script

echo "=========================================="
echo "Running All Datasets Tests"
echo "=========================================="
echo ""

if [ ! -d "bin" ]; then
    echo "Building project first..."
    ./build.sh
    echo ""
fi

if [ ! -f "data/small_dag.json" ]; then
    echo "Generating datasets first..."
    ./generate_datasets.sh
    echo ""
fi

# Array of all datasets
DATASETS=(
    "data/tasks.json"
    "data/small_dag.json"
    "data/small_cycle.json"
    "data/small_sparse.json"
    "data/medium_mixed.json"
    "data/medium_multiple_sccs.json"
    "data/medium_dense.json"
    "data/large_sparse.json"
    "data/large_complex.json"
    "data/large_dense.json"
)

# Run each dataset
for dataset in "${DATASETS[@]}"; do
    echo ""
    echo "=========================================="
    echo "Testing: $dataset"
    echo "=========================================="
    java -cp bin Main "$dataset" | grep -A 20 "SUMMARY"
    echo ""
    echo "Press Enter to continue to next dataset..."
    read
done

echo ""
echo "=========================================="
echo "All tests completed!"
echo "=========================================="
