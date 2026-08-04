import { KoreanSyllableProfile } from './korean-syllable-profile';
import { NovelTextGenerator } from './novel-text-generator';
import { SelectivityTier } from './selectivity-tier';
import { collectInjectedMarkers, loadTierQueryCharacters } from './tier-query-characters';

const SEED = 42;
const CONTENT_SAMPLE_SIZE = 200;
const RATIO_SAMPLE_SIZE = 10000;
const RATIO_TOLERANCE = 0.005;

describe('NovelTextGenerator', () => {
  const tierCharacters = loadTierQueryCharacters();
  const profile = KoreanSyllableProfile.load(collectInjectedMarkers(tierCharacters));
  const generator = new NovelTextGenerator(profile, tierCharacters, SEED);

  it('같은 시드와 같은 문서 인덱스는 언제나 같은 본문을 만든다', () => {
    const other = new NovelTextGenerator(profile, tierCharacters, SEED);

    for (const documentIndex of [0, 1, 7, 999, 12345]) {
      expect(generator.generate(documentIndex)).toEqual(other.generate(documentIndex));
    }
  });

  it('본문 길이가 모두 5000자 이상 10000자 이하다', () => {
    for (let documentIndex = 0; documentIndex < CONTENT_SAMPLE_SIZE; documentIndex += 1) {
      const { content, title } = generator.generate(documentIndex);

      expect(content.length).toBeGreaterThanOrEqual(5000);
      expect(content.length).toBeLessThanOrEqual(10000);
      expect(title.length).toBeLessThanOrEqual(200);
    }
  });

  it('주입 마커의 문서 포함 비율이 목표치와 일치한다', () => {
    const containmentCounts = new Map<string, number>();
    for (let documentIndex = 0; documentIndex < RATIO_SAMPLE_SIZE; documentIndex += 1) {
      for (const marker of generator.markersFor(documentIndex)) {
        containmentCounts.set(marker, (containmentCounts.get(marker) ?? 0) + 1);
      }
    }

    for (const tier of [SelectivityTier.MEDIUM, SelectivityTier.RARE]) {
      const { characters, targetDocumentRatio } = tierCharacters[tier];
      for (const marker of characters) {
        const actualRatio = (containmentCounts.get(marker) ?? 0) / RATIO_SAMPLE_SIZE;

        expect(Math.abs(actualRatio - targetDocumentRatio)).toBeLessThanOrEqual(RATIO_TOLERANCE);
      }
    }
  });

  it('주입 마커는 예측된 문서에만 등장하고 자연 최빈 음절은 모든 문서에 등장한다', () => {
    const allInjectedMarkers = collectInjectedMarkers(tierCharacters);

    for (let documentIndex = 0; documentIndex < CONTENT_SAMPLE_SIZE; documentIndex += 1) {
      const { content } = generator.generate(documentIndex);
      const expectedMarkers = new Set(generator.markersFor(documentIndex));

      for (const marker of allInjectedMarkers) {
        expect(content.includes(marker)).toBe(expectedMarkers.has(marker));
      }
      for (const commonCharacter of tierCharacters[SelectivityTier.COMMON].characters) {
        expect(content).toContain(commonCharacter);
      }
    }
  });
});
