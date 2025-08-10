#!/bin/bash

echo "🧹 Cleaning project..."
./gradlew clean

echo "📦 Building project with updated dependencies..."
./gradlew build -x test

echo "✅ Build completed!"
echo "🚀 Starting application..."
./gradlew bootRun
