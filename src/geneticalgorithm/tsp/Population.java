package geneticalgorithm.tsp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Population
{

  private List<Path> population = new ArrayList<Path>();

  Population(int size, List<Coordinate> nodes)
  {
    for (int i = 0; i < size; i++) {
      Path newPath = new Path(new ArrayList<Coordinate>(nodes), true);
      population.add(newPath);
    }
  }

  public List<Path> getPopList()
  {
    return population;
  }

  public void sortPopulation()
  {
    Collections.sort(population, new PathComparator());
  }

  public Path getBest()
  {
    sortPopulation();
    return this.population.get(0);
  }

  public void newGeneration(int keep_top, double chance_mutate, boolean allowTwins)
  {
    sortPopulation();
    Random random = new Random();
    int orig_size = population.size();
    List<Path> new_population = new ArrayList<Path>();
    new_population.addAll(population.subList(0, keep_top));

    for (int i = 0; i < keep_top; i++) {
      Path path = new Path(new_population.get(i));
      int j = 1;
      while (random.nextDouble() < chance_mutate / j) {
        path.mutate();
        j++;
      }
      if (allowTwins) {
        new_population.add(path);
      }
      else if (!new_population.contains(path)) {
        new_population.add(path);
      }
    }
    while (new_population.size() < orig_size) {
      int idx1 = random.nextInt(keep_top);
      int idx2 = random.nextInt(keep_top);
      Path new_path = Path.crossBreed(new Path(population.get(idx1)), new Path(population.get(idx2)));
      if (allowTwins) {
        new_population.add(new_path);
      }
      else if (!new_population.contains(new_path)) {
        new_population.add(new_path);
      }
    }
    this.population = new_population;
  }
}
