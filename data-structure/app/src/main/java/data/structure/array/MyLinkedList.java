package data.structure.array;

public class MyLinkedList<T> {

    public Node<T> root;
    public Node<T> lastNode;
    public int size = 0;

    /**
     * 뒤에 value를 추가하는 메서드
     */
    @SuppressWarnings("unchecked")
    public void add(T value) {
        if (!initialize(value)) {
            Node<T> newNode = new Node<T>(value);
            this.lastNode.setNext(newNode);
            newNode.setPrev(this.lastNode);
            this.lastNode = newNode;
        }
        this.size++;
    }

    /**
     * 특정 인덱스에 Node 삽입
     */
    @SuppressWarnings("unchecked")
    public void add(T value, int index) {
        checkIdxOrThrow(index);
        if (!initialize(value)) {
            Node<T> newNode = new Node<T>(value);
            Node<T> originNode = this.get(index);
            Node<T> prevNode = originNode.getPrev();
            prevNode.setNext(newNode);
            originNode.setPrev(newNode);
        }
        size++;
    }

    /**
     * 제일 앞에 value를 추가하는 메서드
     */
    @SuppressWarnings("unchecked")
    public void addFirst(T value) {
        if (!initialize(value)) {
            Node<T> addedNode = new Node<T>(value);
            this.root.setPrev(addedNode);
            addedNode.setNext(root);
            this.root = addedNode;
        }
        this.size++;
    }

    /**
     * 제일 앞에 value를 추가하는 메서드
     */
    @SuppressWarnings("unchecked")
    public void addLast(T value) {
        if (!initialize(value)) {
            Node<T> addNode = new Node<T>(value);
            this.lastNode.setNext(addNode);
            addNode.setPrev(root);
            this.lastNode = addNode;
        }
        this.size++;
    }


    /**
     * 뒤에서부터 찾을 수 있도록 좀 더 빠르게 최적화 가능할듯
     */
    @SuppressWarnings("unchecked")
    public Node<T> get(int index) {
        checkIdxOrThrow(index);
        Node<T> result = root;
        for (int i = 0; i < index; i++) {
            result = result.getNext();
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public Node<T> remove(int index) {
        Node<T> originNode = this.get(index);
        Node<T> prevNode = originNode.getPrev();
        Node<T> nextNode = originNode.getNext();

        if (prevNode != null) prevNode.setNext(prevNode);
        if (nextNode != null) nextNode.setPrev(nextNode);

        originNode.setNext(null);
        originNode.setPrev(null);

        this.size--;

        return originNode;
    }

    @SuppressWarnings("unchecked")
    public Node<T> removeFirst() {
        Node<T> prevRootNode = this.root;
        this.root = this.root.getNext();
        this.root.setPrev(null);
        this.size--;
        return prevRootNode;
    }

    public Node<T> removeLast() {
        Node<T> prevLastNode = this.lastNode;
        this.lastNode = this.lastNode.getPrev();
        this.lastNode.setNext(null);
        this.size--;
        return prevLastNode;
    }

    /**
     * 초기화 메서드
     *
     * @param value
     * @return 초기화 되지 않으면 false 반환
     */
    private boolean initialize(T value) {
        if (this.root == null) {
            this.root = new Node<T>(value);
            lastNode = root;
            return true;
        }
        return false;
    }

    /**
     * 요청한 index와 size를 비교해서
     * index가 size 이상이면 IndexOutOfBoundsException를 발생
     */
    private void checkIdxOrThrow(int index) {
        if (index >= this.size) {
            throw new IndexOutOfBoundsException();
        }
    }
}


class Node<T> {
    private Node<T> prevNode;
    private Node<T> nextNode;
    private T value;

    public Node(T value) {
        this.value = value;
    }

    public Node getNext() {
        return this.nextNode;
    }

    public void setNext(Node<T> node) {
        this.nextNode = node;
    }

    public Node getPrev() {
        return this.prevNode;
    }

    public void setPrev(Node<T> node) {
        this.prevNode = node;
    }

    public T getValue() {
        return this.value;
    }
}
