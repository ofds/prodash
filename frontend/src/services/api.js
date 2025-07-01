import axios from 'axios';

// This creates a special 'instance' of axios with a custom configuration
const apiClient = axios.create({
    baseURL: process.env.NEXT_PUBLIC_API_URL,
  });

/**
 * Triggers the backend to ingest today's proposals from the external API.
 */
export const ingestTodaysProposals = () => {
  return apiClient.post('/proposals/ingest-today');
};

/**
 * Triggers the backend to analyze a batch of proposals for their impact score.
 */
export const analyzeImpactScores = (limit = 20) => {
  return apiClient.post(`/daily/analyze-impact-score?limit=${limit}`);
};

/**
 * Fetches the ranked list of proposals for a given date.
 * Defaults to today if no date is provided.
 */
export const getRankedProposals = (date = null) => {
  const params = date ? { date: date.toISOString().split('T')[0] } : {};
  return apiClient.get('/proposals/ranked-by-date', { params });
};