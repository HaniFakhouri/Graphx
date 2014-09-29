package keerov.graphx.ds;

public class Stack<T> {

    private static final int DEFAULT_CAPACITY = 5;
    private T[] theArray;
    private int topOfStack;

    @SuppressWarnings("unchecked")
    public Stack() {
        theArray = (T[]) new Object[DEFAULT_CAPACITY];
        topOfStack = -1;
    }

    public void push(T x) {
        if (topOfStack + 1 == theArray.length) {
            doubleStack();
        }
        theArray[++topOfStack] = x;
    }

    public void pop() {
        if (isEmpty()) {
            return;
        }
        topOfStack--;
    }

    public T top() {
        if (isEmpty()) {
            return null;
        }
        return theArray[topOfStack];
    }

    public T topAndPop() {
        if (isEmpty()) {
            return null;
        }
        return theArray[topOfStack--];
    }

    public boolean isEmpty() {
        return topOfStack == -1;
    }

    public void makeEmpty() {
        topOfStack = -1;
    }

    public void print() {
        System.out.println(theArray[topOfStack] + " <-- top of stack");
        for (int i = topOfStack - 1; i >= 0; i--) {
            System.out.println(theArray[i]);
        }
        System.out.println("Capacity: " + theArray.length);
        System.out.println();
    }

    private void doubleStack() {
        int newSize = 2 * theArray.length;
        @SuppressWarnings("unchecked")
        T[] newArray = (T[]) new Object[newSize];
        for (int i = 0; i < theArray.length; i++) {
            newArray[i] = theArray[i];
        }
        theArray = newArray;
    }
}
