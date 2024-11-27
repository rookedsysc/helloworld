package data.structure.array;

import data.structure.array.interfaces.pq.Comparable;
import data.structure.array.interfaces.pq.Queue;

public class MyPriorityQueue<E extends Comparable> implements Queue<E> {

    private Object[] arr;
    private int arrSize = 0;
    private int arrLength = 0;
    private static final Object[] EMPTY_ELEMENTDATA = {};

    public MyPriorityQueue() {
        this.arr = EMPTY_ELEMENTDATA; // 이진 탐색을 위해서 ArrayList 사용
    }

    @Override
    public void offer(E value) {
        add(value);
        this.arrLength++;
    }

    private void add(E value) {
        this.arrResize();
        int index = findIndex(value);
        for (int i = index + 1; this.arrLength > i; i++) {
            this.arr[i + 1] = this.arr[i];
        }
        this.arr[index] = value;
    }

    private int findIndex(E value) {
        if(this.arrLength == 0) return 0;
        else {
            int left = 0;
            int right = this.arrLength;
            while(left < right) {
                int mid = (left + right) / 2;
                if(((E) this.arr[mid]).compareTo(value) < 0) {
                    left = mid + 1;
                } else {
                    right = mid;
                }
            }
            return left;
        }
    }

    @Override
    public E poll() {
        E value = (E) this.arr[this.arrLength - 1];
        this.arr[this.arrLength - 1] = null;
        this.arrLength --;
        return value;
    }

    @Override
    public E peek() {
        return (E) this.arr[this.arrSize - 1];
    }

    @Override
    public int size() {
        return this.arrLength;
    }

    @Override
    public void clear() {
        this.arr = EMPTY_ELEMENTDATA;
        this.arrSize = 0;
        this.arrLength = 0;
    }

    /**
     * 만약 사이즈가 50이상 넘어갔다면 20씩 증가하고 아니라면 사이즈를 2배씩 증가
     */
    private int calculateNewSize() {
        if (arrSize == 0) return 1;
        if (arrSize > 50) return this.arrSize + 50;
        return this.arrSize * 2;
    }

    private void arrResize() {
        int newSize = this.calculateNewSize();
        Object[] newArr = new Object[newSize];
        for (int i = 0; i < this.arrSize; i++) {
            newArr[i] = this.arr[i];
        }
        this.arr = newArr;
        this.arrSize = newSize;
    }
}
