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
    @DisplayName("3개의 값을 Offer할 경우 가장 Priority가 높은 값이 Peek되어야 한다.")
    void testOfferPeek() {
        // given
        MyPriorityQueue<TestNode> pq = new MyPriorityQueue<>();
        pq.offer(node1);
        pq.offer(node2);
        pq.offer(node3);

        // when
        TestNode peekNode = pq.peek();

        // then
        assert peekNode.getBrandName().equals("LG");
        assert pq.size() == 3;
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

    @Test
    @DisplayName("clear() 했다가 offer() 하면 size()가 1이 되어야 한다.")
    void testClearOffer() {
        // given
        MyPriorityQueue<TestNode> pq = new MyPriorityQueue<>();
        pq.offer(node1);
        pq.offer(node2);
        pq.offer(node3);
        pq.clear();

        // when
        pq.offer(node1);

        // then
        assert pq.size() == 1;
    }


    @Test
    @DisplayName("다수의 데이터를 넣어도 순서가 보장되는지 확인")
    void testOfferPollMany() {
        // given
        MyPriorityQueue<TestNode> pq = new MyPriorityQueue<>();
        pq.offer(new TestNode("테스트1", 1));
        pq.offer(new TestNode("테스트2", 4));
        pq.offer(new TestNode("테스트3", 7));
        pq.offer(new TestNode("테스트4", 2));
        pq.offer(new TestNode("테스트5", 3));
        pq.offer(new TestNode("테스트6", 5));
        pq.offer(new TestNode("테스트7", 1));
        pq.offer(new TestNode("테스트8", 2));
        pq.offer(new TestNode("테스트9", 3));
        pq.offer(new TestNode("테스트10", 6));

        // when
        TestNode pollNode1 = pq.poll();
        TestNode pollNode2 = pq.poll();
        TestNode pollNode3 = pq.poll();
        TestNode pollNode4 = pq.poll();
        TestNode pollNode5 = pq.poll();
        TestNode pollNode6 = pq.poll();
        TestNode pollNode7 = pq.poll();
        TestNode pollNode8 = pq.poll();
        TestNode pollNode9 = pq.poll();
        TestNode pollNode10 = pq.poll();

        // then
        assert pollNode1.getPriority() == 7;
        assert pollNode2.getPriority() == 6;
        assert pollNode3.getPriority() == 5;
        assert pollNode4.getPriority() == 4;
        assert pollNode5.getPriority() == 3;
        assert pollNode6.getPriority() == 3;
        assert pollNode7.getPriority() == 2;
        assert pollNode8.getPriority() == 2;
        assert pollNode9.getPriority() == 1;
        assert pollNode10.getPriority() == 1;
        assert pq.size() == 0;
    }
}
