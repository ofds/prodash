import axios from 'axios';

const apiClient = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api', // Fallback for local dev
});

/**
 * Searches for proposals with pagination, an optional search term, sorting, and impact score filtering.
 * @param {string} searchTerm - The term to search for.
 * @param {number} page - The page number to retrieve.
 * @param {number} size - The number of items per page.
 * @param {string} sort - The sorting criteria (e.g., 'impactScore,desc').
 * @param {number} minImpactScore - The minimum impact score to filter by.
 * @returns {Promise<AxiosResponse<any>>} A promise that resolves to the API response.
 */
export const searchProposals = (searchTerm = '', page = 0, size = 10, sort = 'presentationDate,desc', minImpactScore) => {
  const params = {
    query: searchTerm, // The backend expects 'query' instead of 'searchTerm'
    page,
    size,
    sort,
  };

  // Only add minImpactScore to params if it has a value
  if (minImpactScore !== undefined && minImpactScore > 0) {
    params.minImpactScore = minImpactScore;
  }

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