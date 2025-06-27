import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

import { AuthProvider } from './contexts/AuthContext';
import ErrorBoundary from './components/ErrorBoundary';
import Navigation from './components/Navigation';
import LoadList from './components/LoadList';
import LoadForm from './components/LoadForm';
import LoadDetail from './components/LoadDetail';
import Profile from './components/Profile';
import Home from './components/Home';

function App() {
  return (
    <ErrorBoundary>
      <AuthProvider>
        <Router>
          <div className="App">
            <Navigation />
            <main className="container mt-4">
              <Routes>
                <Route path="/" element={<Home />} />
                <Route path="/loads" element={<LoadList />} />
                <Route path="/loads/new" element={<LoadForm />} />
                <Route path="/loads/:id" element={<LoadDetail />} />
                <Route path="/loads/:id/edit" element={<LoadForm />} />
                <Route path="/profile" element={<Profile />} />
              </Routes>
            </main>
            <ToastContainer
              position="top-right"
              autoClose={3000}
              hideProgressBar={false}
              newestOnTop={false}
              closeOnClick
              rtl={false}
              pauseOnFocusLoss
              draggable
              pauseOnHover
            />
          </div>
        </Router>
      </AuthProvider>
    </ErrorBoundary>
  );
}

export default App;
