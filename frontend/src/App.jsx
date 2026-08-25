import RecordingList from './components/RecordingList';
import './App.css';

function App() {
  return (
    <div className="app">
      <header className="app-header">
        <h1>BP Recorder</h1>
        <p>Voice Recordings by Business Partner</p>
      </header>
      <main className="app-main">
        <RecordingList />
      </main>
    </div>
  );
}

export default App;