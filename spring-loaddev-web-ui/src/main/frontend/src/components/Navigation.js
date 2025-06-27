import React from 'react';
import { Navbar, Nav, Container, Button } from 'react-bootstrap';
import { Link, useNavigate } from 'react-router-dom';
import { FaHome, FaList, FaPlus, FaUser, FaSignOutAlt } from 'react-icons/fa';
import { useAuth } from '../contexts/AuthContext';

const Navigation = () => {
  const { user, authenticated, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
  };

  if (!authenticated) {
    return (
      <Navbar bg="primary" variant="dark" expand="lg">
        <Container>
          <Navbar.Brand href="/">
            <FaHome className="me-2" />
            Load Development
          </Navbar.Brand>
          <Nav className="ms-auto">
            <Nav.Link href="/login">Login</Nav.Link>
          </Nav>
        </Container>
      </Navbar>
    );
  }

  return (
    <Navbar bg="primary" variant="dark" expand="lg">
      <Container>
        <Navbar.Brand as={Link} to="/">
          <FaHome className="me-2" />
          Load Development
        </Navbar.Brand>
        
        <Navbar.Toggle aria-controls="basic-navbar-nav" />
        <Navbar.Collapse id="basic-navbar-nav">
          <Nav className="me-auto">
            <Nav.Link as={Link} to="/loads">
              <FaList className="me-1" />
              My Loads
            </Nav.Link>
            <Nav.Link as={Link} to="/loads/new">
              <FaPlus className="me-1" />
              New Load
            </Nav.Link>
          </Nav>
          
          <Nav>
            <Nav.Link as={Link} to="/profile">
              <FaUser className="me-1" />
              {user?.username || 'User'}
            </Nav.Link>
            <Button variant="outline-light" onClick={handleLogout} className="ms-2">
              <FaSignOutAlt className="me-1" />
              Logout
            </Button>
          </Nav>
        </Navbar.Collapse>
      </Container>
    </Navbar>
  );
};

export default Navigation;
