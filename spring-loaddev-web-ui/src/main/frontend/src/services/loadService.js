import api from './api';

const LOADS_ENDPOINT = '/loads';

export const loadService = {
  // Get all loads with pagination and search
  getLoads: async (page = 0, size = 10, sortBy = 'createdAt', sortDir = 'desc', search = '') => {
    const params = {
      page,
      size,
      sortBy,
      sortDir,
    };
    
    if (search && search.trim()) {
      params.search = search.trim();
    }
    
    const response = await api.get(LOADS_ENDPOINT, { params });
    return response.data;
  },

  // Get load by ID
  getLoadById: async (id) => {
    const response = await api.get(`${LOADS_ENDPOINT}/${id}`);
    return response.data;
  },

  // Create new load
  createLoad: async (loadData) => {
    const response = await api.post(LOADS_ENDPOINT, loadData);
    return response.data;
  },

  // Update existing load
  updateLoad: async (id, loadData) => {
    const response = await api.put(`${LOADS_ENDPOINT}/${id}`, loadData);
    return response.data;
  },

  // Delete load
  deleteLoad: async (id) => {
    await api.delete(`${LOADS_ENDPOINT}/${id}`);
  },

  // Get loads by cartridge
  getLoadsByCartridge: async (cartridge) => {
    const response = await api.get(`${LOADS_ENDPOINT}/by-cartridge/${encodeURIComponent(cartridge)}`);
    return response.data;
  },

  // Get loads by bullet
  getLoadsByBullet: async (bullet) => {
    const response = await api.get(`${LOADS_ENDPOINT}/by-bullet/${encodeURIComponent(bullet)}`);
    return response.data;
  },

  // Get loads by powder
  getLoadsByPowder: async (powder) => {
    const response = await api.get(`${LOADS_ENDPOINT}/by-powder/${encodeURIComponent(powder)}`);
    return response.data;
  },
};
