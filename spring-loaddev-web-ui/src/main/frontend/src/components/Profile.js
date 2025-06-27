import React from 'react';
import { Container, Row, Col, Card, Button, Alert } from 'react-bootstrap';
import { FaUser, FaSignOutAlt, FaCog } from 'react-icons/fa';
import { useAuth } from '../contexts/AuthContext';

const Profile = () => {
  const { user, authenticated, logout } = useAuth();

  const handleLogout = () => {
    logout();
  };

  if (!authenticated) {
    return (
      <Container>
        <Alert variant="warning">
          Please <a href="/login">login</a> to view your profile.
        </Alert>
      </Container>
    );
  }

  return (
    <Container className="fade-in">
      <Row className="mb-4">
        <Col>
          <h2>User Profile</h2>
        </Col>
      </Row>

      <Row>
        <Col lg={8}>
          <Card className="mb-4">
            <Card.Header>
              <h5 className="mb-0">
                <FaUser className="me-2" />
                Account Information
              </h5>
            </Card.Header>
            <Card.Body>
              <Row>
                <Col md={6}>
                  <div className="mb-3">
                    <label className="form-label fw-bold">Username</label>
                    <p className="form-control-plaintext">{user?.username || 'N/A'}</p>
                  </div>
                </Col>
                <Col md={6}>
                  <div className="mb-3">
                    <label className="form-label fw-bold">Account Type</label>
                    <p className="form-control-plaintext">OAuth2 User</p>
                  </div>
                </Col>
              </Row>
            </Card.Body>
          </Card>

          <Card className="mb-4">
            <Card.Header>
              <h5 className="mb-0">
                <FaCog className="me-2" />
                Account Actions
              </h5>
            </Card.Header>
            <Card.Body>
              <div className="d-grid gap-2 d-md-flex">
                <Button variant="outline-danger" onClick={handleLogout}>
                  <FaSignOutAlt className="me-2" />
                  Sign Out
                </Button>
              </div>
            </Card.Body>
          </Card>
        </Col>

        <Col lg={4}>
          <Card>
            <Card.Header>
              <h6 className="mb-0">About Your Data</h6>
            </Card.Header>
            <Card.Body>
              <p className="mb-3">
                Your load development data is stored securely and is only accessible to you.
              </p>
              <ul className="list-unstyled">
                <li className="mb-2">✓ Private and secure</li>
                <li className="mb-2">✓ Backed up regularly</li>
                <li className="mb-2">✓ Accessible from anywhere</li>
                <li className="mb-2">✓ Export functionality (coming soon)</li>
              </ul>
            </Card.Body>
          </Card>

          <Card className="mt-4">
            <Card.Header>
              <h6 className="mb-0">Need Help?</h6>
            </Card.Header>
            <Card.Body>
              <p className="mb-3">
                Having issues or questions about using the application?
              </p>
              <div className="d-grid">
                <Button variant="outline-primary" disabled>
                  Contact Support
                </Button>
              </div>
              <small className="text-muted mt-2 d-block">
                Feature coming soon
              </small>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </Container>
  );
};

export default Profile;
