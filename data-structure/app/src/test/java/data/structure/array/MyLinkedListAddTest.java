package data.structure.array;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MyLinkedListAddTest {

    @Test
    @DisplayName("add시 size 증가되는지 테스트")
    void addSizeIncreaseTest() {
        // given // when
        MyLinkedList<Integer> list = new MyLinkedList<>();
        list.add(1);
        list.add(2);
        list.add(3);

        // then
        assertEquals(3, list.size);
    }

    @Test
    @DisplayName("addLast addFirst Test")
    void addFirstTest() {
        // given
        MyLinkedList<Integer> list1 = new MyLinkedList<>();
        MyLinkedList<Integer> list2 = new MyLinkedList<>();

        // when
        list1.addFirst(1);
        list1.addLast(2);

        list2.addLast(2);
        list2.addFirst(1);

        // then
        assertEquals(2, list1.lastNode.getValue());
        assertEquals(1, list1.root.getValue());
    }
}
