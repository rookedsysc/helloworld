package data.structure.array.interfaces.pq;

import java.util.PriorityQueue;

public interface Queue<E> {
    /**
     * 요소 추가
     * @param value
     */
    void offer(E value);

    /**
     * 요소 제거
     * @return
     */
    E poll();

    /**
     * 최상위 요소 확인
     * @return
     */
    E peek();

    /**
     * 큐 사이즈
     * @return
     */
    int size();

    /**
     * 큐 비우기
     */
    void clear();
}
