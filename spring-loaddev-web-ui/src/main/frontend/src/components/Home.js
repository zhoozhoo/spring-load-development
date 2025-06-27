import React from 'react';
import { Container, Row, Col, Card, Button } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import { FaList, FaPlus, FaChartLine, FaCrosshairs } from 'react-icons/fa';
import { useAuth } from '../contexts/AuthContext';

const Home = () => {
  const { user, authenticated } = useAuth();

  if (!authenticated) {
    return (
      <Container className="mt-5">
        <Row className="justify-content-center">
          <Col md={8} lg={6}>
            <div className="text-center">
              <h1 className="display-4 mb-4">Load Development</h1>
              <p className="lead mb-4">
                Manage and track your ammunition load development data with precision and ease.
              </p>
              <Button variant="primary" size="lg" href="/login">
                Get Started
              </Button>
            </div>
          </Col>
        </Row>
      </Container>
    );
  }

  return (
    <Container className="fade-in">
      <Row className="mb-4">
        <Col>
          <h1 className="display-5">Welcome back, {user?.username || 'User'}!</h1>
          <p className="lead text-muted">
            Manage your ammunition load development data and track your precision shooting progress.
          </p>
        </Col>
      </Row>

      <Row>
        <Col md={6} lg={3} className="mb-4">
          <Card className="h-100 text-center">
            <Card.Body className="d-flex flex-column">
              <div className="mb-3">
                <FaList size={40} className="text-primary" />
              </div>
              <Card.Title>View Loads</Card.Title>
              <Card.Text className="flex-grow-1">
                Browse and search through your existing load data with advanced filtering options.
              </Card.Text>
              <Button variant="primary" as={Link} to="/loads">View All Loads</Button>
            </Card.Body>
          </Card>
        </Col>

        <Col md={6} lg={3} className="mb-4">
          <Card className="h-100 text-center">
            <Card.Body className="d-flex flex-column">
              <div className="mb-3">
                <FaPlus size={40} className="text-success" />
              </div>
              <Card.Title>New Load</Card.Title>
              <Card.Text className="flex-grow-1">
                Create a new load entry with detailed specifications and performance data.
              </Card.Text>
              <Button variant="success" as={Link} to="/loads/new">Create Load</Button>
            </Card.Body>
          </Card>
        </Col>

        <Col md={6} lg={3} className="mb-4">
          <Card className="h-100 text-center">
            <Card.Body className="d-flex flex-column">
              <div className="mb-3">
                <FaChartLine size={40} className="text-info" />
              </div>
              <Card.Title>Analytics</Card.Title>
              <Card.Text className="flex-grow-1">
                Analyze your load performance data and identify trends for optimization.
              </Card.Text>
              <Button variant="info" disabled>
                Coming Soon
              </Button>
            </Card.Body>
          </Card>
        </Col>

        <Col md={6} lg={3} className="mb-4">
          <Card className="h-100 text-center">
            <Card.Body className="d-flex flex-column">
              <div className="mb-3">
                <FaCrosshairs size={40} className="text-warning" />
              </div>
              <Card.Title>Precision Tracking</Card.Title>
              <Card.Text className="flex-grow-1">
                Track accuracy metrics and shooting performance across different loads.
              </Card.Text>
              <Button variant="warning" disabled>
                Coming Soon
              </Button>
            </Card.Body>
          </Card>
        </Col>
      </Row>

      <Row className="mt-5">
        <Col>
          <Card>
            <Card.Header>
              <h5 className="mb-0">Getting Started</h5>
            </Card.Header>
            <Card.Body>
              <Row>
                <Col md={4}>
                  <h6>1. Create Your First Load</h6>
                  <p className="text-muted">
                    Start by adding your ammunition load data including cartridge, bullet, powder, and performance metrics.
                  </p>
                </Col>
                <Col md={4}>
                  <h6>2. Track Performance</h6>
                  <p className="text-muted">
                    Record velocity, accuracy, and other performance characteristics for each load variation.
                  </p>
                </Col>
                <Col md={4}>
                  <h6>3. Analyze & Optimize</h6>
                  <p className="text-muted">
                    Use the data to identify your best performing loads and optimize for your specific needs.
                  </p>
                </Col>
              </Row>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </Container>
  );
};

export default Home;
