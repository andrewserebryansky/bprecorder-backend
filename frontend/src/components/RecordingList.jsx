import { useState, useEffect } from 'react';
import { recordingsApi } from '../services/api';
import BusinessPartnerSection from './BusinessPartnerSection';

function RecordingList() {
  const [recordings, setRecordings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [groupedRecordings, setGroupedRecordings] = useState({});

  const fetchRecordings = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await recordingsApi.getAll();
      const data = response.data;
      setRecordings(data);
      
      const grouped = data.reduce((acc, recording) => {
        const partner = recording.businessPartner || 'Unknown';
        if (!acc[partner]) {
          acc[partner] = [];
        }
        acc[partner].push(recording);
        return acc;
      }, {});
      
      Object.keys(grouped).forEach(partner => {
        grouped[partner].sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
      });
      
      setGroupedRecordings(grouped);
    } catch (err) {
      setError('Failed to load recordings. Make sure the backend is running on port 8080.');
      console.error('Error fetching recordings:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleInitSampleData = async () => {
    try {
      await recordingsApi.initSampleData();
      await fetchRecordings();
    } catch (err) {
      setError('Failed to initialize sample data.');
      console.error('Error initializing sample data:', err);
    }
  };

  useEffect(() => {
    fetchRecordings();
  }, []);

  if (loading) {
    return (
      <div className="loading">
        <div className="spinner"></div>
        <p>Loading recordings...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="error">
        <p>{error}</p>
        <button onClick={fetchRecordings} className="retry-button">Retry</button>
        <button onClick={handleInitSampleData} className="init-button">Initialize Sample Data</button>
      </div>
    );
  }

  const partners = Object.keys(groupedRecordings).sort();

  if (partners.length === 0) {
    return (
      <div className="empty-state">
        <p>No recordings found.</p>
        <button onClick={handleInitSampleData} className="init-button">Initialize Sample Data</button>
      </div>
    );
  }

  return (
    <div className="recording-list">
      <div className="list-header">
        <h1>Business Partner Recordings</h1>
        <div className="header-actions">
          <span className="total-recordings">{recordings.length} total recordings</span>
          <button onClick={fetchRecordings} className="refresh-button">Refresh</button>
        </div>
      </div>
      <div className="partners-container">
        {partners.map((partner) => (
          <BusinessPartnerSection
            key={partner}
            businessPartner={partner}
            recordings={groupedRecordings[partner]}
          />
        ))}
      </div>
    </div>
  );
}

export default RecordingList;