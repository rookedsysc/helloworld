import { KoreanSyllableProfile } from './korean-syllable-profile';
import { SelectivityTier } from './selectivity-tier';
import { TierQueryCharacters } from './tier-query-characters';

/** 생성된 소설 한 편. */
export interface GeneratedNovel {
  title: string;
  content: string;
}

const MIN_CONTENT_LENGTH = 5000;
const MAX_CONTENT_LENGTH = 10000;
const MIN_TITLE_SYLLABLES = 6;
const MAX_TITLE_SYLLABLES = 16;

/** 마커를 주입할 때 한 문서에 넣는 최소 횟수와 변동 폭. 등장 횟수가 1이면 term frequency가 퇴화한다. */
const MIN_MARKER_OCCURRENCES = 3;
const MARKER_OCCURRENCE_SPREAD = 6;

/** 문서 인덱스를 섞어 문서마다 독립적인 난수열을 만들기 위한 황금비 상수. */
const DOCUMENT_SEED_MIXER = 0x9e3779b1;

/**
 * 실제 음절 빈도를 따르는 한국어 합성 소설 본문을 결정적으로 생성한다.
 *
 * 같은 시드와 같은 문서 인덱스는 언제나 같은 본문을 만든다. 문서마다 난수열을
 * 독립적으로 초기화하므로 앞 문서를 만들지 않고도 임의의 문서를 재현할 수 있고,
 * 배치 적재와 테스트가 같은 결과를 본다.
 */
export class NovelTextGenerator {
  private readonly injectedMarkersByTier: Map<SelectivityTier, string[]>;

  /**
   * @param profile 마커가 배제된 음절 빈도 프로파일
   * @param tierCharacters 계층별 질의 문자 정의. origin이 injected인 계층만 주입 대상이다.
   * @param seed 전체 데이터셋을 재현하는 시드
   */
  constructor(
    private readonly profile: KoreanSyllableProfile,
    tierCharacters: Record<SelectivityTier, TierQueryCharacters>,
    private readonly seed: number,
  ) {
    this.injectedMarkersByTier = new Map(
      (Object.entries(tierCharacters) as [SelectivityTier, TierQueryCharacters][])
        .filter(([, tier]) => tier.origin === 'injected')
        .map(([tierName, tier]) => [tierName, tier.characters]),
    );
  }

  /**
   * 문서 하나를 생성한다.
   *
   * @param documentIndex 0부터 시작하는 문서 순번. 마커 포함 여부가 이 값으로 결정된다.
   * @returns 제목과 본문
   */
  generate(documentIndex: number): GeneratedNovel {
    const random = createRandom(this.seed ^ Math.imul(documentIndex + 1, DOCUMENT_SEED_MIXER));
    const title = this.buildTitle(random);
    const contentLength =
      MIN_CONTENT_LENGTH + Math.floor(random() * (MAX_CONTENT_LENGTH - MIN_CONTENT_LENGTH + 1));
    const characters = this.buildBody(random, contentLength);
    this.injectMarkers(characters, documentIndex);

    return { title, content: characters.join('') };
  }

  /**
   * 주어진 문서에 주입될 마커를 알려준다.
   *
   * 선택도 계약을 검증하는 테스트와 동등성 스모크가 기대값을 계산하는 데 쓴다.
   *
   * @param documentIndex 문서 순번
   * @returns 이 문서에 주입되는 마커 문자 목록
   */
  markersFor(documentIndex: number): string[] {
    const markers: string[] = [];
    for (const [tier, tierMarkers] of this.injectedMarkersByTier) {
      const ordinal = this.includedMarkerOrdinal(tier, tierMarkers.length, documentIndex);
      if (ordinal !== null) {
        markers.push(tierMarkers[ordinal]);
      }
    }
    return markers;
  }

  /**
   * 문서가 해당 계층의 몇 번째 마커를 받는지 판정한다.
   *
   * 목표 포함률의 역수를 주기로 삼아 나머지 연산으로 배분하므로 비율이 근사가 아니라 정확하다.
   *
   * @returns 마커 순번, 받지 않으면 null
   */
  private includedMarkerOrdinal(
    tier: SelectivityTier,
    markerCount: number,
    documentIndex: number,
  ): number | null {
    const cyclePerMarker = tier === SelectivityTier.MEDIUM ? MEDIUM_CYCLE : RARE_CYCLE;
    const position = documentIndex % cyclePerMarker;
    return position < markerCount ? position : null;
  }

  private buildTitle(random: () => number): string {
    const syllableCount =
      MIN_TITLE_SYLLABLES + Math.floor(random() * (MAX_TITLE_SYLLABLES - MIN_TITLE_SYLLABLES + 1));
    const syllables: string[] = [];
    for (let index = 0; index < syllableCount; index += 1) {
      syllables.push(this.profile.sample(random()));
    }
    return syllables.join('');
  }

  /**
   * 실제 산문의 공백 밀도를 따르는 본문 문자 배열을 만든다.
   *
   * 빈도 파일의 spaceRatio는 음절 하나당 공백 수라, 전체 위치 중 공백이 차지하는
   * 비율로 환산해서 쓴다. 공백이 연달아 나오지 않도록 직전 문자를 확인한다.
   */
  private buildBody(random: () => number, contentLength: number): string[] {
    const spaceProbability = this.profile.spaceRatio / (1 + this.profile.spaceRatio);
    const characters = new Array<string>(contentLength);
    for (let position = 0; position < contentLength; position += 1) {
      const canPlaceSpace = position > 0 && position < contentLength - 1 && characters[position - 1] !== ' ';
      characters[position] = canPlaceSpace && random() < spaceProbability ? ' ' : this.profile.sample(random());
    }
    return characters;
  }

  /**
   * 마커를 본문에 고르게 흩어 넣는다.
   *
   * 한 문서가 여러 계층의 마커를 함께 받을 수 있으므로 자리를 마커 수만큼 나눠
   * 번갈아 배정한다. 계층별로 같은 자리를 계산하면 나중 마커가 앞 마커를 덮어쓴다.
   * 공백 자리에 덮어쓰면 어절 구조가 깨지므로 한 칸 밀어 음절 자리에 넣는다.
   */
  private injectMarkers(characters: string[], documentIndex: number): void {
    const markers = this.markersFor(documentIndex);
    if (markers.length === 0) {
      return;
    }

    const occurrencesPerMarker = MIN_MARKER_OCCURRENCES + (documentIndex % MARKER_OCCURRENCE_SPREAD);
    const totalSlots = occurrencesPerMarker * markers.length;
    for (let slot = 1; slot <= totalSlots; slot += 1) {
      const evenPosition = Math.floor((characters.length * slot) / (totalSlots + 1));
      const position = characters[evenPosition] === ' ' ? evenPosition + 1 : evenPosition;
      characters[position] = markers[(slot - 1) % markers.length];
    }
  }
}

/** MEDIUM 계층 목표 포함률 10%의 역수. 마커 하나가 이 주기마다 한 번 등장한다. */
const MEDIUM_CYCLE = 10;
/** RARE 계층 목표 포함률 0.1%의 역수. */
const RARE_CYCLE = 1000;

/**
 * mulberry32 의사난수 생성기.
 *
 * @param seed 32비트 시드
 * @returns 0 이상 1 미만의 난수를 반환하는 함수
 */
function createRandom(seed: number): () => number {
  let state = seed >>> 0;
  return () => {
    state = (state + 0x6d2b79f5) >>> 0;
    let mixed = state;
    mixed = Math.imul(mixed ^ (mixed >>> 15), mixed | 1);
    mixed ^= mixed + Math.imul(mixed ^ (mixed >>> 7), mixed | 61);
    return ((mixed ^ (mixed >>> 14)) >>> 0) / 4294967296;
  };
}
