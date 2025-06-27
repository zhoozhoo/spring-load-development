import '@testing-library/jest-dom';

// Mock window.userInfo for tests
window.userInfo = {
  authenticated: true,
  username: 'testuser'
};

// Mock environment variables
process.env.REACT_APP_API_GATEWAY_URL = 'http://localhost:8080';

// Global test setup
beforeEach(() => {
  // Clear any previous test state
  jest.clearAllMocks();
});
