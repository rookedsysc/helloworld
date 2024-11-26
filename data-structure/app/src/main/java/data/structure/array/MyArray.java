package data.structure.array;

import java.util.List;

public class MyArray<T> {

  private Object[] arr;
  private int elementCnt;
  private int size; 

  public MyArray(List<T> data) {
    this.initializeArr(data);
  }

  public MyArray() {
    this.initializeArr(List.of());
  }

  /**
   * Array의 기본 사이즈를 지정함
   */
  private void initializeArr(List<T> data) {
    if(data.isEmpty()) {
      initialArr();
    } else {
      allocateData(data);
    }
  }

  /**
   * 데이터가 없는 경우 arr 초기화
   */
  private void initialArr() {
    int arrSize = 2;
    this.arr = new Object[arrSize];
    this.size = arrSize;
    this.elementCnt = 0;
  }

  /**
   * 데이터가 있는 경우 arr에 데이터 할당
   */
  private void allocateData(List<T> data) {
    int dataSize = data.size();
    this.arr = data.toArray();
    this.elementCnt = dataSize;
    this.size = dataSize;
  }


  /**
   * list에 할당하는 메서드 
   */
  public void add(T data) {
    if(checkFull()) {
      this.arrResize();
    }
    arr[elementCnt] = data;
    this.elementCnt += 1;
  }

  /**
   * arr이 꽉 찼는지 확인
   */
  private boolean checkFull() {
    return this.size == this.elementCnt ? true : false;
  }

  /**
   * arr 사이즈를 재조정 후 새로 할당하고
   * 기존의 내용을 복사 후 size만 재기록
   */
  private void arrResize() {
    int newSize = this.calculateNewSize();
    Object[] newArr = new Object[newSize];
    for(int i = 0; i < this.size; i++) {
      newArr[i] = this.arr[i];
    }
    this.arr = newArr;
    this.size = newSize;
  }

  /**
   * 만약 사이즈가 50이상 넘어갔다면 20씩 증가하고 아니라면 사이즈를 2배씩 증가
   */
  private int calculateNewSize() {
    if(size > 50) return this.size + 50;
    return this.size * 2;
  }

  /**
   * N 번째 인덱스에 있는 데이터를 삭제
   * 삭제한 index 데이터
   */
  public T remove(int idx) {
    @SuppressWarnings("unchecked")
    T result = (T) this.arr[idx];
    for(int i = idx; i < this.size; i++) {
      if(i == size - 1)  {
        this.arr[i] = null;
        break;
      }
      this.elementCnt --;
      this.arr[idx] = this.arr[idx + 1];
    }
    return result;
  }

  /**
   * index의 데이터를 반환
   */
  @SuppressWarnings("unchecked")
  public T get(int idx) {
    return (T) this.arr[idx];
  }
}