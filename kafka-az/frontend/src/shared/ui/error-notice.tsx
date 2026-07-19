type ErrorNoticeProps = {
  message: string;
  onRetry?: () => void;
};

export function ErrorNotice({ message, onRetry }: ErrorNoticeProps) {
  return (
    <div className="error-banner" role="alert">
      <span>{message}</span>
      {onRetry && (
        <button className="secondary-button" type="button" onClick={onRetry}>
          다시 불러오기
        </button>
      )}
    </div>
  );
}
