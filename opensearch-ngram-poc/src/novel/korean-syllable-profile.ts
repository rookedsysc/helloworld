import { readFileSync } from 'node:fs';
import { join } from 'node:path';

interface SyllableFrequencyFile {
  totalSyllables: number;
  distinctSyllables: number;
  spaceRatio: number;
  syllableFrequencies: Record<string, number>;
}

const SYLLABLE_FREQUENCY_PATH = join(__dirname, '..', '..', 'data', 'korean-syllable-frequency.json');

/**
 * 프로젝트의 실제 소설 산문에서 추출한 음절 빈도로 가중 샘플링을 제공한다.
 *
 * 균등 분포로 본문을 만들면 모든 posting list 길이가 같아져 비현실적이다.
 * 실제 한국어는 Zipf 분포라(최빈 '다' 5.31%) 이 편중이 MySQL FTS aux table과
 * OpenSearch term 통계를 좌우하므로 벤치마크 현실성에 직접 기여한다.
 */
export class KoreanSyllableProfile {
  private readonly syllables: string[];
  private readonly cumulativeWeights: Float64Array;

  private constructor(
    syllables: string[],
    cumulativeWeights: Float64Array,
    readonly spaceRatio: number,
  ) {
    this.syllables = syllables;
    this.cumulativeWeights = cumulativeWeights;
  }

  /**
   * 빈도 파일을 읽어 프로파일을 만든다.
   *
   * @param excludedSyllables 본문에 등장하면 안 되는 마커 문자. 주입으로 포함률을 통제하기 위해 풀에서 제외한다.
   * @returns 가중 샘플링이 가능한 프로파일
   * @throws 배제 후 남은 음절이 없으면 오류
   */
  static load(excludedSyllables: string[]): KoreanSyllableProfile {
    const file: SyllableFrequencyFile = JSON.parse(readFileSync(SYLLABLE_FREQUENCY_PATH, 'utf-8'));
    const excluded = new Set(excludedSyllables);

    const syllables: string[] = [];
    const weights: number[] = [];
    let runningTotal = 0;
    for (const [syllable, count] of Object.entries(file.syllableFrequencies)) {
      if (excluded.has(syllable)) {
        continue;
      }
      runningTotal += count;
      syllables.push(syllable);
      weights.push(runningTotal);
    }

    if (syllables.length === 0) {
      throw new Error('배제 후 남은 음절이 없어 본문을 만들 수 없습니다');
    }

    const normalized = Float64Array.from(weights, (weight) => weight / runningTotal);
    return new KoreanSyllableProfile(syllables, normalized, file.spaceRatio);
  }

  /** 풀에 남은 고유 음절 수. */
  get distinctSyllableCount(): number {
    return this.syllables.length;
  }

  /**
   * 누적분포에서 음절 하나를 뽑는다.
   *
   * @param uniformRandom 0 이상 1 미만의 난수
   * @returns 빈도 가중으로 선택된 음절
   */
  sample(uniformRandom: number): string {
    let low = 0;
    let high = this.cumulativeWeights.length - 1;
    while (low < high) {
      const middle = (low + high) >>> 1;
      if (this.cumulativeWeights[middle] < uniformRandom) {
        low = middle + 1;
      } else {
        high = middle;
      }
    }
    return this.syllables[low];
  }
}
