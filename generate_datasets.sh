#!/bin/bash

# Dataset generation script for Assignment 4

if [ ! -d "bin" ]; then
    echo "Build directory not found. Running build.sh first..."
    ./build.sh
    echo ""
fi

echo "======================================"
echo "Generating Datasets"
echo "======================================"
echo ""

java -cp bin DatasetGenerator

if [ $? -eq 0 ]; then
    echo ""
    echo "✓ All datasets generated successfully!"
    echo ""
    echo "Generated datasets:"
    ls -lh data/*.json
fi
