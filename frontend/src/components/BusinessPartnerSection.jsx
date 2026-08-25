import RecordingCard from './RecordingCard';

function BusinessPartnerSection({ businessPartner, recordings }) {
  return (
    <div className="business-partner-section">
      <div className="business-partner-header">
        <h2 className="business-partner-name">{businessPartner}</h2>
        <span className="recording-count">{recordings.length} recording{recordings.length !== 1 ? 's' : ''}</span>
      </div>
      <div className="recordings-grid">
        {recordings.map((recording) => (
          <RecordingCard key={recording.id} recording={recording} />
        ))}
      </div>
    </div>
  );
}

export default BusinessPartnerSection;