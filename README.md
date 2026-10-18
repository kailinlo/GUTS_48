# GUTS_48
"Do you have the guts?" challenge 2026 entry for Morgan Stanley
GUTS challenge 2026 - team 48

An interactive walk-through demonstration game for Dijkstra's algorithm

Implemented using Java and Java Swing

INSTRUCTIONS -------------------------------------------------------------------
initial set up:
mkdir -p out
javac -d out $(find src -name "*.java")

to run game:
java -cp out Main          # random roads each run
java -cp out Main 12345    # fixed seed, so the live demo always gets a good graph
