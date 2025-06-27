import React from 'react';
import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from '../contexts/AuthContext';
import Navigation from '../components/Navigation';

// Mock API module
jest.mock('../services/api', () => ({
  get: jest.fn(),
}));

const renderWithProviders = (component) => {
  return render(
    <BrowserRouter>
      <AuthProvider>
        {component}
      </AuthProvider>
    </BrowserRouter>
  );
};

describe('Navigation Component', () => {
  test('renders navigation brand', () => {
    renderWithProviders(<Navigation />);
    expect(screen.getByText('Load Development')).toBeInTheDocument();
  });

  test('shows login link when not authenticated', () => {
    // Override the mock for this test
    window.userInfo = { authenticated: false };
    
    renderWithProviders(<Navigation />);
    expect(screen.getByText('Login')).toBeInTheDocument();
  });

  test('shows user menu when authenticated', async () => {
    // Override the mock for this test
    window.userInfo = { authenticated: true, username: 'testuser' };
    
    renderWithProviders(<Navigation />);
    
    // Wait for auth context to load
    await screen.findByText('My Loads');
    expect(screen.getByText('My Loads')).toBeInTheDocument();
    expect(screen.getByText('New Load')).toBeInTheDocument();
  });
});
