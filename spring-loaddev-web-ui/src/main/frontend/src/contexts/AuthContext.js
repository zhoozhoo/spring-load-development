import React, { createContext, useContext, useState, useEffect } from 'react';
import api from '../services/api';

const AuthContext = createContext();

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [authenticated, setAuthenticated] = useState(false);

  useEffect(() => {
    checkAuthStatus();
  }, []);

  const checkAuthStatus = async () => {
    try {
      setLoading(true);
      // Try to get user info from backend
      const response = await api.get('/api/user');
      const userInfo = response.data;
      
      if (userInfo && userInfo.authenticated) {
        setUser(userInfo);
        setAuthenticated(true);
      } else {
        // Fallback to window.userInfo for backward compatibility
        const fallbackUser = window.userInfo;
        if (fallbackUser && fallbackUser.authenticated) {
          setUser(fallbackUser);
          setAuthenticated(true);
        }
      }
    } catch (error) {
      console.warn('Auth check failed, checking fallback:', error);
      // Fallback to window.userInfo
      const fallbackUser = window.userInfo;
      if (fallbackUser && fallbackUser.authenticated) {
        setUser(fallbackUser);
        setAuthenticated(true);
      } else {
        setAuthenticated(false);
        setUser(null);
      }
    } finally {
      setLoading(false);
    }
  };

  const login = () => {
    window.location.href = '/login';
  };

  const logout = () => {
    setUser(null);
    setAuthenticated(false);
    window.location.href = '/logout';
  };

  const value = {
    user,
    authenticated,
    loading,
    login,
    logout,
    refreshAuth: checkAuthStatus
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
