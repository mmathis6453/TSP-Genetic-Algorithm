package geneticalgorithm.tsp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Path
{

  private List<Coordinate> path = new ArrayList<Coordinate>();
  private int size;

  Path(Path old)
  {
    this.path = new ArrayList<Coordinate>(old.getPath());
    this.size = path.size();
    this.getCost();
  }

  /**
   * We only want to randomize the nodes in the path during the first generation of the path. This ensures that the
   * "evolution" of the path is truely random
   * 
   * @param nodes
   * @param randomize
   */
  Path(List<Coordinate> nodes, boolean randomize)
  {
    if (randomize) {
      Collections.shuffle(nodes);
    }
    this.path = nodes;
    size = this.path.size();
  }

  /**
   * This returns the cost of fully tranversing the path
   */
  public double getCost()
  {
    double cost = 0;
    for (int i = 0; i < this.path.size(); i++) {
      Coordinate from = this.path.get(i);
      Coordinate to = (i + 1 != this.path.size()) ? this.path.get(i + 1) : this.path.get(0);
      double dist_add = from.distance_to(to);
      cost += dist_add;
    }
    return cost;
  }

  public List<Coordinate> getPath()
  {
    return this.path;
  }

  @Override
  public String toString()
  {
    String retVal = "";
    for (Coordinate coord : path) {
      retVal += "(" + coord.x + "," + coord.y + ")";
    }
    return retVal;
  }

  public int getSize()
  {
    return this.size;
  }

  @Override
  public boolean equals(Object o)
  {
    Path pathB = (Path) o;
    List<Coordinate> listB = pathB.getPath();
    List<Coordinate> listA = this.getPath();

    Coordinate cA = listA.get(0);
    int idxB = listB.indexOf(cA);
    idxB++;
    for (int idxA = 1; idxA < listA.size(); idxA++, idxB++) {
      if (idxB == listB.size()) idxB = 0;
      if (!listB.get(idxB).equals(listA.get(idxA))) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    return Arrays.deepHashCode(path.toArray());
  }

  /**
   *  There are two types of mutation. 
   *  1. A sequence of nodes are reversed
   *  2. A sequence of nodes are moved somewhere else in the path
   *  
   *  There is a 25% chance of either one of these types of mutation happening, 
   *  and a 50% chance of both of them happening
   *  
   *  Flip example:
   *               ab|cdefg|hij -> abgfedchij
   *  
   *  Move example:
   *               ab|cdefg|hi j -> abhicdefgj
   *                          ^
   */
  public void mutate()
  {
    Random random = new Random();
    boolean flip = (random.nextInt(2) == 0) ? true : false;
    boolean move = (random.nextInt(2) == 1) ? true : false;
    if (!flip && !move) {
      flip = true;
      move = true;
    }

    List<Coordinate> new_path = new ArrayList<Coordinate>();
    List<Coordinate> mut_path;
    List<Coordinate> orig_path = new ArrayList<Coordinate>(this.path);

    // These values define the end points of the sequence to be mutated
    // We don't want them to have the same value
    int randInt1 = Integer.MAX_VALUE;
    int randInt2 = Integer.MAX_VALUE;
    while (randInt1 == randInt2) {
      randInt1 = random.nextInt((this.size));
      randInt2 = random.nextInt((this.size));
    }

    if (randInt1 > randInt2) {
      int tmp = randInt1;
      randInt1 = randInt2;
      randInt2 = tmp;
    }

    mut_path = new ArrayList<Coordinate>(orig_path.subList(randInt1, randInt2));

    if (flip) {
      Collections.reverse(mut_path);
    }

    if (move) {
      int seedInt = orig_path.size() - Math.abs(randInt1 - randInt2);
      int randInt3 = random.nextInt((seedInt));
      orig_path.removeAll(mut_path);

      if (randInt3 != 0) {
        new_path.addAll(orig_path.subList(0, randInt3));
      }
      new_path.addAll(mut_path);
      if (randInt3 != orig_path.size()) {
        new_path.addAll(orig_path.subList(randInt3, orig_path.size()));
      }
    }
    else {
      if (randInt1 != 0) {
        new_path.addAll(orig_path.subList(0, randInt1));
      }
      new_path.addAll(mut_path);
      if (randInt2 != orig_path.size()) {
        new_path.addAll(orig_path.subList(randInt2, orig_path.size()));
      }
    }
    this.path = new_path;
  }

  /**
   * This method takes in two paths as arguments, and returns a newly formed path that has parts of both. 
   * This works by randomly selecting a sequence from path1, removing the intersection of nodes from 
   * path 2, and combining the final result back together.
   * 
   * Ex: abcdefghij x jigbacfhde
   * 
   * Suppose we want to cross these two paths, and select "bcdef" as the sequence
   * 
   *     a|bcdef|ghij x jigbacfhde = bcdefjigah
   *       ^^^^^        ^^^ ^  ^     
   * We cut out bcdef from path1, remove those nodes from path2, and concatenate the paths together 
   * 
   */
  public static Path crossBreed(Path path1, Path path2)
  {
    List<Coordinate> path_list_1 = new ArrayList<Coordinate>(path1.getPath());
    List<Coordinate> path_list_2 = new ArrayList<Coordinate>(path2.getPath());

    List<Coordinate> new_path;
    int path_size = path1.getSize();
    Random random = new Random();
    int randomNum1 = random.nextInt((path_size) + 1);
    int randomNum2 = random.nextInt((path_size) + 1);
    if (randomNum1 < randomNum2) {
      new_path = path_list_1.subList(randomNum1, randomNum2);
    }
    else {
      new_path = path_list_1.subList(randomNum1, path1.size);
      new_path.addAll(path_list_1.subList(0, randomNum2));
    }
    path_list_2.removeAll(new_path);
    new_path.addAll(path_list_2);
    return new Path(new_path, false);

  }

}
