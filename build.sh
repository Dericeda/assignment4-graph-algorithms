#!/bin/bash

# Build script for Assignment 4

echo "======================================"
echo "Building Assignment 4: Graph Algorithms"
echo "======================================"
echo ""

# Clean previous build
echo "Cleaning previous build..."
rm -rf bin/
mkdir -p bin/

# Compile all Java files
echo "Compiling Java source files..."
javac -d bin -sourcepath src/main/java $(find src/main/java -name "*.java")

if [ $? -eq 0 ]; then
    echo "✓ Compilation successful!"
    echo ""
    echo "Build complete. You can now run:"
    echo "  ./run.sh data/tasks.json"
    echo "  ./generate_datasets.sh"
else
    echo "✗ Compilation failed!"
    exit 1
fi
