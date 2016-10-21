# TSP-Genetic-Algorithm
For those unfamiliar with genetic algorithms, I suggest you read the [Wikipedia page](https://en.wikipedia.org/wiki/Genetic_algorithm) on the subject.

This is a swing app that uses a genetic algorithm approach to the traveling salesman problem.
To create the runnable jar that launches the app, sumply run the build.bat script if on windows.

Once the app is launched, you are presented with a canvas where you can either place your own nodes, or generate 10 random nodes. 
You are also presented with the ability to change the evolution parameters before running.

* **Population size:** This is how many random paths are initially created for your set of nodes. Every new generation of your path will contain this many nodes.
* **Survivors:** This is the quantity of top performers you want to continue onto the next generation, and mutate/breed to fill up your population to the initial size.
* **Mutation Rate:** This is the chance of your paths mutating to create new ones. The paths can mutate more than once.
* **# Populations:** This is the number of independent populations evolving at the same time. If more than one independent population is evolving, the canvas will show the path of the best performing. Note that large numbers of independent populations can cause some performance issues.
*  **Allow Twins:** If allow twins is turned off, each population will contain all unique paths. You will also need to make sure that you have enough nodes to fill a population with enough unique paths. Not allowing twins has significant overhead, but the benefit from it is that the algorithm is less likely to stall at a non-optimal solution. 

Once your canvas is populated with nodes and variables are passed in, you can run the program and the canvas will update in real time with the lowest cost path. 

The program will also output a csv file with the path cost and timestamp for analysis of the algorithm.
