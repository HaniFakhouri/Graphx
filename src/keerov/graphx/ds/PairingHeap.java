package keerov.graphx.ds;

public class PairingHeap<T extends Comparable<? super T>> {

    private PairNode<T> root;
    private int size;

    public PairingHeap() {
        root = null;
        size = 0;
    }

    public boolean isEmpty() {
        return root == null;
    }

    public void makeEmpty() {
        root = null;
    }

    public int size() {
        return size;
    }

    public Position<T> insert(T x) {
        PairNode<T> newNode = new PairNode<T>(x);
        if (root == null) {
            root = newNode;
        } else {
            root = compareAndLink(root, newNode);
        }
        size++;
        return newNode;
    }

    public T deleteMin() {
        if (isEmpty()) {
            System.out.println("IS EMPTY!");
            return null;
        }
        T x = findMin();
        root.element = null;
        if (root.leftChild == null) {
            root = null;
        } else {
            root = combineSiblings(root.leftChild);
        }
        size--;
        return x;
    }

    public void decreaseKey(Position<T> pos, T newVal) {
        if (pos == null) {
            System.out.println("POS IS NULL");
            return;
        }

        PairNode<T> p = (PairNode<T>) pos;

        if (p.element == null || p.element.compareTo(newVal) < 0) {
            return;
        }

        p.element = newVal;
        if (p != root) {
            if (p.nextSibling != null) {
                p.nextSibling.prev = p.prev;
            }
            if (p.prev.leftChild == p) {
                p.prev.leftChild = p.nextSibling;
            } else {
                p.prev.nextSibling = p.nextSibling;
            }

            p.nextSibling = null;
            root = compareAndLink(root, p);
        }
    }

    public T findMin() {
        if (isEmpty()) {
            return null;
        }
        return root.element;
    }

    private PairNode<T> compareAndLink(PairNode<T> first, PairNode<T> second) {
        if (second == null) {
            return first;
        }

        if (second.element.compareTo(first.element) < 0) {
            second.prev = first.prev;
            first.prev = second;
            first.nextSibling = second.leftChild;
            if (first.nextSibling != null) {
                first.nextSibling.prev = first;
            }
            second.leftChild = first;
            return second;
        } else {
            second.prev = first;
            first.nextSibling = second.nextSibling;
            if (first.nextSibling != null) {
                first.nextSibling.prev = first;
            }
            second.nextSibling = first.leftChild;
            if (second.nextSibling != null) {
                second.nextSibling.prev = second;
            }
            first.leftChild = second;
            return first;
        }

    }

    @SuppressWarnings("unchecked")
    private PairNode<T>[] doubleIfFull(PairNode<T>[] array, int index) {
        if (index == array.length) {
            PairNode<T>[] oldArray = array;

            array = new PairNode[index * 2];
            for (int i = 0; i < index; i++) {
                array[i] = oldArray[i];
            }
        }
        return array;
    }

    @SuppressWarnings("rawtypes")
    private PairNode[] treeArray = new PairNode[5];

    @SuppressWarnings("unchecked")
    private PairNode<T> combineSiblings(PairNode<T> firstSibling) {
        if (firstSibling.nextSibling == null) {
            return firstSibling;
        }

        int numSiblings = 0;
        for (; firstSibling != null; numSiblings++) {
            treeArray = doubleIfFull(treeArray, numSiblings);
            treeArray[numSiblings] = firstSibling;
            firstSibling.prev.nextSibling = null;
            firstSibling = firstSibling.nextSibling;
        }
        treeArray = doubleIfFull(treeArray, numSiblings);
        treeArray[numSiblings] = null;

        int i = 0;
        for (; i + 1 < numSiblings; i += 2) {
            treeArray[i] = compareAndLink(treeArray[i], treeArray[i + 1]);
        }

        int j = i - 2;

        if (j == numSiblings - 3) {
            treeArray[j] = compareAndLink(treeArray[j], treeArray[j + 2]);
        }

        for (; j >= 2; j -= 2) {
            treeArray[j - 2] = compareAndLink(treeArray[j - 2], treeArray[j]);
        }

        return treeArray[0];

    }

    public void print() {
        PairNode<T> n = root;
        System.out.println("root: " + n.element);
        while (n != null) {
            n = printN(n);
        }
    }

    private PairNode<T> printN(PairNode<T> n) {
        if (n.leftChild != null) {
            System.out.println("L of " + n.element + ": " + n.leftChild.element);
            return n.leftChild;
        }
        if (n.nextSibling != null) {
            System.out.println("S of " + n.element + ": " + n.nextSibling.element);
            return n.nextSibling;
        }
        return n;
    }

    public interface Position<T> {

        T getValue();
    }

    private static class PairNode<T> implements Position<T> {

        public T element;
        public PairNode<T> leftChild;
        public PairNode<T> nextSibling;
        public PairNode<T> prev;

        public PairNode(T elem) {
            element = elem;
            leftChild = null;
            nextSibling = null;
            prev = null;
        }

        @Override
        public T getValue() {
            return element;
        }

    }

}
