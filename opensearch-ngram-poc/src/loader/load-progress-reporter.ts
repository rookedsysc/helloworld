const MILLISECONDS_PER_SECOND = 1000;
const BYTES_PER_MEGABYTE = 1024 * 1024;

/**
 * 적재 진행률과 실측 처리율을 보고한다.
 *
 * 10만건 본적재 전에 소규모 프로브로 처리율을 재서 소요시간을 역산하는 것이
 * 이 PoC의 게이트라, 처리율을 추정이 아니라 측정으로 남긴다.
 */
export class LoadProgressReporter {
  private readonly startedAt = Date.now();
  private loadedDocuments = 0;
  private loadedCharacters = 0;

  constructor(
    private readonly label: string,
    private readonly totalDocuments: number,
    private readonly reportInterval: number,
  ) {}

  /**
   * 배치 하나가 끝날 때마다 누적치를 갱신하고 주기적으로 진행률을 출력한다.
   *
   * @param documentCount 이번 배치의 문서 수
   * @param characterCount 이번 배치의 본문 문자 수
   */
  recordBatch(documentCount: number, characterCount: number): void {
    this.loadedDocuments += documentCount;
    this.loadedCharacters += characterCount;

    if (this.loadedDocuments % this.reportInterval === 0 || this.loadedDocuments === this.totalDocuments) {
      const elapsedSeconds = this.elapsedSeconds();
      const megabytesPerSecond = this.megabytes() / elapsedSeconds;
      const remainingSeconds = (this.totalDocuments - this.loadedDocuments) / (this.loadedDocuments / elapsedSeconds);
      console.log(
        `[${this.label}] ${this.loadedDocuments.toLocaleString()}/${this.totalDocuments.toLocaleString()} ` +
          `경과 ${elapsedSeconds.toFixed(1)}초 처리율 ${megabytesPerSecond.toFixed(2)}MB/s ` +
          `잔여 예상 ${(remainingSeconds / 60).toFixed(1)}분`,
      );
    }
  }

  /**
   * 최종 실측치를 출력한다.
   *
   * @returns 총 소요 초와 처리율(MB/s)
   */
  finish(): { elapsedSeconds: number; megabytesPerSecond: number } {
    const elapsedSeconds = this.elapsedSeconds();
    const megabytesPerSecond = this.megabytes() / elapsedSeconds;
    console.log(
      `[${this.label}] 완료 — 문서 ${this.loadedDocuments.toLocaleString()}건, ` +
        `본문 ${this.megabytes().toFixed(1)}MB, 소요 ${elapsedSeconds.toFixed(1)}초, ${megabytesPerSecond.toFixed(2)}MB/s`,
    );
    return { elapsedSeconds, megabytesPerSecond };
  }

  private elapsedSeconds(): number {
    return (Date.now() - this.startedAt) / MILLISECONDS_PER_SECOND;
  }

  private megabytes(): number {
    // 한국어 UTF-8은 음절당 3바이트라 실제 전송량에 맞춰 환산한다.
    return (this.loadedCharacters * 3) / BYTES_PER_MEGABYTE;
  }
}
