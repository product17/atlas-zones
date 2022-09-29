package io.sandbox.atlas.zone.data_types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DimensionStartPoints {
  public List<Integer> points = new ArrayList<>();

  public Integer getNextPoint() {
    int result = 0;
    int size = points.size();
    if (points.size() > 0) {
      Collections.sort(points);
      result = DimensionStartPoints.findFirstMissing(points, 0, size - 1);
    }

    points.add(result);
    
    return result;
  }

  private static int findFirstMissing(List<Integer> array, int start, int end) {
    if (start > end) {
      return end + 1;
    }

    if (start != array.get(start)) {
      return start;
    }

    int mid = (start + end) / 2;

    // Left half has all elements from 0 to mid
    if (array.get(mid) == mid) {
      return findFirstMissing(array, mid + 1, end);
    }

    return findFirstMissing(array, start, mid);
  }
}
