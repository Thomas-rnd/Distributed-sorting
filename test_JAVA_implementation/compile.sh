#!/bin/bash

# Vérifie si le répertoire de compilation existe, s'il n'existe pas, le crée
if [ ! -d "bin" ]; then
  mkdir bin
fi

# Compile tous les fichiers Java dans le répertoire courant et les place dans le répertoire "bin"
find . -name "*.java" | xargs javac -d bin
