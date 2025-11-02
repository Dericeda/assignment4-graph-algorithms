#!/bin/bash

echo "=========================================="
echo "Assignment 4 Submission Checker"
echo "=========================================="
echo ""

ERRORS=0

# Check directory structure
echo "Checking project structure..."
REQUIRED_DIRS=(
    "src/main/java/graph/scc"
    "src/main/java/graph/topo"
    "src/main/java/graph/dagsp"
    "src/main/java/graph/utils"
    "src/test/java/graph/scc"
    "src/test/java/graph/topo"
    "src/test/java/graph/dagsp"
    "data"
)

for dir in "${REQUIRED_DIRS[@]}"; do
    if [ -d "$dir" ]; then
        echo "  ✓ $dir"
    else
        echo "  ✗ $dir (MISSING)"
        ERRORS=$((ERRORS + 1))
    fi
done
echo ""

# Check required files
echo "Checking required files..."
REQUIRED_FILES=(
    "README.md"
    "pom.xml"
    ".gitignore"
    "src/main/java/Main.java"
    "src/main/java/DatasetGenerator.java"
    "src/main/java/graph/scc/TarjanSCC.java"
    "src/main/java/graph/scc/KosarajuSCC.java"
    "src/main/java/graph/topo/TopologicalSort.java"
    "src/main/java/graph/dagsp/DAGShortestPath.java"
    "src/main/java/graph/utils/Graph.java"
    "src/main/java/graph/utils/Edge.java"
    "src/main/java/graph/utils/Metrics.java"
    "src/main/java/graph/utils/MetricsImpl.java"
    "src/main/java/graph/utils/GraphLoader.java"
)

for file in "${REQUIRED_FILES[@]}"; do
    if [ -f "$file" ]; then
        echo "  ✓ $file"
    else
        echo "  ✗ $file (MISSING)"
        ERRORS=$((ERRORS + 1))
    fi
done
echo ""

# Check datasets
echo "Checking datasets..."
REQUIRED_DATASETS=(
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

DATASET_COUNT=0
for dataset in "${REQUIRED_DATASETS[@]}"; do
    if [ -f "$dataset" ]; then
        echo "  ✓ $dataset"
        DATASET_COUNT=$((DATASET_COUNT + 1))
    else
        echo "  ⚠ $dataset (Will be generated)"
    fi
done
echo ""

if [ $DATASET_COUNT -lt 9 ]; then
    echo "Generating missing datasets..."
    ./generate_datasets.sh
    echo ""
fi

# Check compilation
echo "Checking compilation..."
if [ -d "bin" ]; then
    echo "  ✓ Build directory exists"
else
    echo "  ⚠ Build directory missing, compiling..."
    ./build.sh
    echo ""
fi

# Test compilation
echo "Testing main program..."
if java -cp bin Main data/tasks.json > /dev/null 2>&1; then
    echo "  ✓ Main program runs successfully"
else
    echo "  ✗ Main program failed"
    ERRORS=$((ERRORS + 1))
fi
echo ""

# Check documentation
echo "Checking documentation..."
if grep -q "SCC" README.md && grep -q "Topological" README.md && grep -q "DAG" README.md; then
    echo "  ✓ README.md has algorithm descriptions"
else
    echo "  ✗ README.md missing algorithm descriptions"
    ERRORS=$((ERRORS + 1))
fi

if grep -q "Dataset Summary" README.md; then
    echo "  ✓ README.md has dataset documentation"
else
    echo "  ⚠ README.md missing detailed dataset documentation"
fi

if grep -q "Build and Run" README.md; then
    echo "  ✓ README.md has build instructions"
else
    echo "  ✗ README.md missing build instructions"
    ERRORS=$((ERRORS + 1))
fi
echo ""

# Summary
echo "=========================================="
echo "SUBMISSION CHECK SUMMARY"
echo "=========================================="
echo ""

if [ $ERRORS -eq 0 ]; then
    echo "✓ All checks passed!"
    echo ""
    echo "Your submission is ready. To submit:"
    echo "  1. git init (if not already a git repository)"
    echo "  2. git add ."
    echo "  3. git commit -m \"Assignment 4: Graph Algorithms Implementation\""
    echo "  4. git remote add origin <your-github-repo-url>"
    echo "  5. git push -u origin main"
    echo ""
    echo "Make sure to:"
    echo "  - Update README.md with your name"
    echo "  - Verify the GitHub repository link in Moodle"
    echo "  - Check that all files are pushed to GitHub"
    echo ""
else
    echo "✗ Found $ERRORS error(s)"
    echo ""
    echo "Please fix the errors before submitting."
    exit 1
fi
