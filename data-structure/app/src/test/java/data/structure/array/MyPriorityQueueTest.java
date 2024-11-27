package data.structure.array;

import data.structure.array.interfaces.pq.Comparable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TestNode implements Comparable {
    private String brandName;
    private int priority;

    public String getBrandName() {
        return brandName;
    }

    public int getPriority() {
        return priority;
    }

    public TestNode(String brandName, int priority) {
        this.brandName = brandName;
        this.priority = priority;
    }

    @Override
    public int compareTo(Comparable o) {
        TestNode node = (TestNode) o;
        return this.priority - node.priority;
    }
}

class MyPriorityQueueTest {

    TestNode node1 = new TestNode("삼성", 1);
    TestNode node2 = new TestNode("애플", 2);
    TestNode node3 = new TestNode("LG", 3);

    @Test
    @DisplayName("3개의 값을 Offer할 경우 가장 Priority가 높은 값이 Poll되어야 한다.")
    void testOfferPoll() {
        // given
        MyPriorityQueue<TestNode> pq = new MyPriorityQueue<>();
        pq.offer(node1);
        pq.offer(node2);
        pq.offer(node3);

        // when
        TestNode pollNode1 = pq.poll();
        TestNode pollNode2 = pq.poll();
        TestNode pollNode3 = pq.poll();

        // then
        assert pollNode1.getBrandName().equals("LG");
        assert pollNode2.getBrandName().equals("애플");
        assert pollNode3.getBrandName().equals("삼성");
        assert pq.size() == 0;
    }

    @Test
    @DisplayName("clear() 메서드 실행시 size()가 0이 되어야 한다.")
    void testClear() {
        // given
        MyPriorityQueue<TestNode> pq = new MyPriorityQueue<>();
        pq.offer(node1);
        pq.offer(node2);
        pq.offer(node3);

        // when
        pq.clear();

        // then
        assert pq.size() == 0;
    }
}
