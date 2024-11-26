package data.structure.array;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MyLinkedListGetTest {

    @Test
    @DisplayName("index로 내가 원하는 값이 잘 받아와지는지 Test")
    void getTest() {
        // given
        MyLinkedList<Integer> list = new MyLinkedList<>();
        list.add(1);
        list.add(2);
        list.add(3);

        // when
        Node<Integer> node1 = list.get(0);
        Node<Integer> node2 = list.get(1);
        Node<Integer> node3 = list.get(2);

        // then
        assertEquals(1, node1.getValue());
        assertEquals(2, node2.getValue());
        assertEquals(3, node3.getValue());
    }

    @Test
    @DisplayName("빈 배열일 때 OutOfIndexException 발생하는지 Test")
    void outOfIndexExceptionTest() {
        // given
        MyLinkedList<Integer> list = new MyLinkedList<>();

        // when
        // then
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(0));
    }

}
