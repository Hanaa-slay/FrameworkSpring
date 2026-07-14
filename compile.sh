#!/bin/bash

set -e

echo "Nettoyage..."
rm -rf bin
mkdir -p bin

echo "Recherche des fichiers Java..."
find src/main/java -name "*.java" > sources.txt

echo "Compilation..."
javac -cp "lib/*:." -d bin @sources.txt

rm sources.txt

echo ""
echo "================================="
echo " Compilation réussie !"
echo " Les .class sont dans : bin/"
echo "================================="