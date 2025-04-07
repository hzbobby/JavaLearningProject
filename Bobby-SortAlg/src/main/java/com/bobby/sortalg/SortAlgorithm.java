package com.bobby.sortalg;

import java.util.Comparator;
import java.util.List;

public interface SortAlgorithm {
    <T> void sort(List<T> data, Comparator<T> comparator);
}
