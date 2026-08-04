/**
 * 1글자 검색 질의의 선택도 계층.
 *
 * 한국어 장문 코퍼스에서는 자연 빈도만으로 중저 선택도를 만들 수 없다.
 * 고유 음절 1,482종에서 문서당 7,500자를 뽑으면 빈도 0.1% 이상 음절은
 * 99.9%의 문서에 등장하기 때문이다. 그래서 COMMON만 자연 빈도를 쓰고
 * MEDIUM과 RARE는 본문 풀에서 배제한 마커를 결정적으로 주입해 만든다.
 */
export enum SelectivityTier {
  COMMON = 'common',
  MEDIUM = 'medium',
  RARE = 'rare',
}
