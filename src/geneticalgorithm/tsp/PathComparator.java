package geneticalgorithm.tsp;

import java.util.Comparator;

public class PathComparator
    implements Comparator<Path>
{

  @Override
  public int compare(Path o1, Path o2)
  {
    if (o1.getCost() > o2.getCost()) {
      return 1;
    }
    else if (o1.getCost() < o2.getCost()) {
      return -1;
    }
    else {
      return 0;
    }
  }
}
