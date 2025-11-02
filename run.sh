
#!/bin/bash

# Run script for Assignment 4

if [ ! -d "bin" ]; then
    echo "Build directory not found. Running build.sh first..."
    ./build.sh
    echo ""
fi

# Default to tasks.json if no argument provided
DATASET=${1:-data/tasks.json}

echo "======================================"
echo "Running Graph Algorithms"
echo "Dataset: $DATASET"
echo "======================================"
echo ""

java -cp bin Main "$DATASET"
