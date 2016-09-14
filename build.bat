mkdir classes\geneticalgorithm\tsp
echo Main-Class: geneticalgorithm.tsp.TSP > classes\manifest.txt
javac -d classes src\geneticalgorithm\tsp\*.java
cd classes
jar -cvfm ..\TSP.jar manifest.txt geneticalgorithm\tsp\*.class