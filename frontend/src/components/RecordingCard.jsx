function RecordingCard({ recording }) {
  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const formatFileSize = (bytes) => {
    if (!bytes) return 'Unknown';
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  };

  return (
    <div className="recording-card">
      <div className="recording-header">
        <span className="recording-filename">{recording.originalFilename}</span>
        <span className="recording-date">{formatDate(recording.createdAt)}</span>
      </div>
      <div className="recording-meta">
        <span className="recording-size">{formatFileSize(recording.fileSize)}</span>
        <span className="recording-type">{recording.contentType}</span>
      </div>
      <div className="recording-transcription">
        {recording.transcription}
      </div>
    </div>
  );
}

export default RecordingCard;