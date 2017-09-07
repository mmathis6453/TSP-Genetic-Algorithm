package geneticalgorithm.tsp;

import java.io.Serializable;

public class Coordinate
    implements Serializable
{

  private static final long serialVersionUID = 1L;
  protected int x;
  protected int y;

  Coordinate(int x, int y)
  {
    this.x = x;
    this.y = y;
  }

  @Override
  public String toString()
  {
    return "(" + x + "," + y + ")";
  }

  @Override
  public boolean equals(Object o)
  {
    Coordinate coordinate = (Coordinate) o;
    if (coordinate.x == this.x && coordinate.y == this.y) {
      return true;
    }
    else {
      return false;
    }
  }

  public double distance_to(Coordinate b)
  {
    double x_diff = this.x - b.x;
    double y_diff = this.y - b.y;
    x_diff = Math.abs(x_diff);
    y_diff = Math.abs(y_diff);
    double x_sqr = x_diff * x_diff;
    double y_sqr = y_diff * y_diff;
    return Math.sqrt(x_sqr + y_sqr);
  }

  @Override
  public int hashCode()
  {
    return x * 31 + y;
  }

}
