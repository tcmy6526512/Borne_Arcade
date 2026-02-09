#!/bin/bash

set -euo pipefail

cd "$(dirname "$0")"

source ./scripts/env.sh

echo "Compilation du menu de la borne d'arcade"
echo "Veuillez patienter"
shopt -s nullglob
root_java=( *.java )
if [ ${#root_java[@]} -gt 0 ]; then
    javac -cp ".:${MG2D_HOME}" "${root_java[@]}"
fi

cd projet


#PENSER A REMETTRE COMPILATION JEUX!!!
for i in *
do
    cd $i
    echo "Compilation du jeu "$i
    echo "Veuillez patienter"
        game_java=( *.java )
        if [ ${#game_java[@]} -gt 0 ]; then
            javac -cp ".:../..:${MG2D_HOME}" "${game_java[@]}"
        else
            echo "(skip) Aucun fichier .java dans $i"
        fi
    cd ..
done

cd ..
