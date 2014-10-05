package keerov.graphx;

import java.util.Comparator;

public class QuickSort {

    private static final int CUTOFF = 5;
    private static Comparator cmp;

    public void useComparator(Comparator cmp) {
        this.cmp = cmp;
    }

    private static <T extends Comparable<? super T>> int compare(T e1, T e2) {
        if (cmp != null) {
            return cmp.compare(e1, e2);
        }
        return e1.compareTo(e2);
    }

    public static <T extends Comparable<? super T>> void sort(T[] a) {
        quickSort(a, 0, a.length - 1);
    }

    @SuppressWarnings("empty-statement")
    private static <T extends Comparable<? super T>> void quickSort(T[] a, int low, int high) {

        if (low + CUTOFF >= high) {
            insertionSort(a, low, high);
        } else {
            int middle = (low + high) / 2;

            // Sort low, middle, high
            if (compare(a[middle], a[low]) < 0) {
                swapReferences(a, low, middle);
            }
            if (compare(a[high], a[low]) < 0) {
                swapReferences(a, low, high);
            }
            if (compare(a[high], a[middle]) < 0) {
                swapReferences(a, middle, high);
            }

            swapReferences(a, middle, high - 1);
            T pivot = a[high - 1];

            // Begin partitioning
            int i, j;
            for (i = low, j = high - 1;;) {
                // Start at position i+1 since low has already been sorted correctly
                // i.e. a[low] < pivot
                //while ( a[++i].compareTo(pivot) < 0 )
                while (compare(a[++i], pivot) < 0)
					;
                // Start at position j-1 since high has already been sorted correctly
                // i.e. a[high-1] = pivot
                //while ( a[--j].compareTo(pivot) > 0 )
                while (compare(a[--j], pivot) > 0)
					;
                if (i >= j) {
                    break;
                }
                swapReferences(a, i, j);
            }

            // Restore the pivot
            swapReferences(a, i, high - 1);

            quickSort(a, low, i - 1);   // Sort small elements 
            quickSort(a, i + 1, high);  // Sort large elements
        }

    }

    private static <T> void swapReferences(T[] a, int p1, int p2) {
        T tmp = a[p1];
        a[p1] = a[p2];
        a[p2] = tmp;
    }

    private static <T extends Comparable<? super T>> void insertionSort(T[] a, int low, int high) {
        for (int i = low + 1; i <= high; i++) {
            T temp = a[i];
            int j = i;
            while (j > 0 && compare(temp, a[j - 1]) < 0) {
                a[j] = a[j - 1];
                j--;
            }
            a[j] = temp;
        }
    }
}
