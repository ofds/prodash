import axios from 'axios';

const apiClient = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api', // Fallback for local dev
});

/**
 * Searches for proposals with pagination and an optional search term.
 * @param {string} searchTerm - The term to search for.
 * @param {number} page - The page number to retrieve.
 * @param {number} size - The number of items per page.
 * @returns {Promise<AxiosResponse<any>>} A promise that resolves to the API response.
 */
export const searchProposals = (searchTerm = '', page = 0, size = 10) => {
  const params = {
    searchTerm,
    page,
    size,
  };
  return apiClient.get('/proposals/search', { params });
};


// The following functions are no longer needed for the proposal search page
/*
 export const ingestTodaysProposals = () => {
 return apiClient.post('/proposals/ingest-today');
};

 export const analyzeImpactScores = (limit = 20) => {
 return apiClient.post(`/daily/analyze-impact-score?limit=${limit}`);
};

 export const getRankedProposals = (date = null) => {
 const params = date ? { date: date.toISOString().split('T')[0] } : {};
 return apiClient.get('/proposals/ranked-by-date', { params });
};
*/