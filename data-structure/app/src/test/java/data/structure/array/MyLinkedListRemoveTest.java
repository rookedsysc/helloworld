package data.structure.array;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MyLinkedListRemoveTest {

    @Test
    @DisplayName("remove시 size 감소되는지 테스트")
    void removeSizeDecreaseTest() {
        // given
        MyLinkedList<Integer> list = new MyLinkedList<>();
        list.add(1);
        list.add(2);
        list.add(3);

        // when
        list.remove(1);

        // then
        assertEquals(2, list.size);
    }

    @Test
    @DisplayName("removeLast removeFirst Test")
    void removeFirstTest() {
        // given
        MyLinkedList<Integer> list = new MyLinkedList<>();
        list.add(1);
        list.add(2);
        list.add(3);

        // when
        Node<Integer> first = list.removeFirst();
        Node<Integer> last = list.removeLast();

        // then
        assertEquals(first.getValue(), 1);
        assertEquals(last.getValue(), 3);
        assertEquals(list.lastNode.getValue(), 2);
        assertEquals(list.root.getValue(), 2);
    }
}
