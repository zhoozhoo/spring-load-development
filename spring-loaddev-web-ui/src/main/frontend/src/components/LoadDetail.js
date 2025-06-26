import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Button, Alert, Spinner, Badge } from 'react-bootstrap';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { toast } from 'react-toastify';
import { FaArrowLeft, FaEdit, FaTrash, FaCopy } from 'react-icons/fa';
import { loadService } from '../services/loadService';

const LoadDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [load, setLoad] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchLoad();
  }, [id]);

  const fetchLoad = async () => {
    try {
      setLoading(true);
      setError(null);
      const loadData = await loadService.getLoadById(id);
      setLoad(loadData);
    } catch (err) {
      setError('Failed to fetch load details. Please try again.');
      console.error('Error fetching load:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async () => {
    if (window.confirm('Are you sure you want to delete this load?')) {
      try {
        await loadService.deleteLoad(id);
        toast.success('Load deleted successfully');
        navigate('/loads');
      } catch (err) {
        toast.error('Failed to delete load');
        console.error('Error deleting load:', err);
      }
    }
  };

  const handleDuplicate = () => {
    if (load) {
      // Navigate to new load form with current load data pre-filled
      const queryParams = new URLSearchParams({
        cartridge: load.cartridge || '',
        bullet: load.bullet || '',
        powder: load.powder || '',
        powderCharge: load.powderCharge || '',
        primer: load.primer || '',
        caseName: load.caseName || '',
        overallLength: load.overallLength || '',
        velocity: load.velocity || '',
        notes: `Duplicated from Load #${load.id}: ${load.notes || ''}`
      });
      navigate(`/loads/new?${queryParams.toString()}`);
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleString();
  };

  const formatNumber = (value, unit = '') => {
    if (value === null || value === undefined || value === '') return 'N/A';
    return `${value}${unit ? ' ' + unit : ''}`;
  };

  if (!window.userInfo?.authenticated) {
    return (
      <Container>
        <Alert variant="warning">
          Please <a href="/login">login</a> to view load details.
        </Alert>
      </Container>
    );
  }

  if (loading) {
    return (
      <Container>
        <div className="loading-spinner">
          <Spinner animation="border" role="status">
            <span className="visually-hidden">Loading...</span>
          </Spinner>
        </div>
      </Container>
    );
  }

  if (error) {
    return (
      <Container>
        <Alert variant="danger">{error}</Alert>
        <Button variant="outline-secondary" onClick={() => navigate('/loads')}>
          <FaArrowLeft className="me-2" />
          Back to Loads
        </Button>
      </Container>
    );
  }

  if (!load) {
    return (
      <Container>
        <Alert variant="warning">Load not found.</Alert>
        <Button variant="outline-secondary" onClick={() => navigate('/loads')}>
          <FaArrowLeft className="me-2" />
          Back to Loads
        </Button>
      </Container>
    );
  }

  return (
    <Container className="fade-in">
      <Row className="mb-4">
        <Col>
          <div className="d-flex align-items-center justify-content-between">
            <div className="d-flex align-items-center">
              <Button 
                variant="outline-secondary" 
                onClick={() => navigate('/loads')} 
                className="me-3"
              >
                <FaArrowLeft />
              </Button>
              <div>
                <h2>Load Details</h2>
                <small className="text-muted">ID: {load.id}</small>
              </div>
            </div>
            <div className="d-flex gap-2">
              <Button variant="outline-info" onClick={handleDuplicate}>
                <FaCopy className="me-2" />
                Duplicate
              </Button>
              <Button variant="outline-warning" as={Link} to={`/loads/${load.id}/edit`}>
                <FaEdit className="me-2" />
                Edit
              </Button>
              <Button variant="outline-danger" onClick={handleDelete}>
                <FaTrash className="me-2" />
                Delete
              </Button>
            </div>
          </div>
        </Col>
      </Row>

      <Row>
        <Col lg={8}>
          <Card className="mb-4">
            <Card.Header>
              <h5 className="mb-0">Cartridge Information</h5>
            </Card.Header>
            <Card.Body>
              <Row>
                <Col md={6}>
                  <div className="mb-3">
                    <label className="form-label fw-bold">Cartridge</label>
                    <p className="form-control-plaintext">{load.cartridge}</p>
                  </div>
                </Col>
                <Col md={6}>
                  <div className="mb-3">
                    <label className="form-label fw-bold">Case</label>
                    <p className="form-control-plaintext">{load.caseName || 'N/A'}</p>
                  </div>
                </Col>
              </Row>
            </Card.Body>
          </Card>

          <Card className="mb-4">
            <Card.Header>
              <h5 className="mb-0">Components</h5>
            </Card.Header>
            <Card.Body>
              <Row>
                <Col md={6}>
                  <div className="mb-3">
                    <label className="form-label fw-bold">Bullet</label>
                    <p className="form-control-plaintext">{load.bullet}</p>
                  </div>
                </Col>
                <Col md={6}>
                  <div className="mb-3">
                    <label className="form-label fw-bold">Primer</label>
                    <p className="form-control-plaintext">{load.primer || 'N/A'}</p>
                  </div>
                </Col>
              </Row>
              <Row>
                <Col md={6}>
                  <div className="mb-3">
                    <label className="form-label fw-bold">Powder</label>
                    <p className="form-control-plaintext">{load.powder}</p>
                  </div>
                </Col>
                <Col md={6}>
                  <div className="mb-3">
                    <label className="form-label fw-bold">Powder Charge</label>
                    <p className="form-control-plaintext">{formatNumber(load.powderCharge, 'grains')}</p>
                  </div>
                </Col>
              </Row>
            </Card.Body>
          </Card>

          <Card className="mb-4">
            <Card.Header>
              <h5 className="mb-0">Performance Data</h5>
            </Card.Header>
            <Card.Body>
              <Row>
                <Col md={6}>
                  <div className="mb-3">
                    <label className="form-label fw-bold">Overall Length</label>
                    <p className="form-control-plaintext">{formatNumber(load.overallLength, 'inches')}</p>
                  </div>
                </Col>
                <Col md={6}>
                  <div className="mb-3">
                    <label className="form-label fw-bold">Velocity</label>
                    <p className="form-control-plaintext">{formatNumber(load.velocity, 'fps')}</p>
                  </div>
                </Col>
              </Row>
            </Card.Body>
          </Card>

          {load.notes && (
            <Card className="mb-4">
              <Card.Header>
                <h5 className="mb-0">Notes</h5>
              </Card.Header>
              <Card.Body>
                <p className="mb-0" style={{ whiteSpace: 'pre-wrap' }}>{load.notes}</p>
              </Card.Body>
            </Card>
          )}
        </Col>

        <Col lg={4}>
          <Card className="mb-4">
            <Card.Header>
              <h5 className="mb-0">Load Summary</h5>
            </Card.Header>
            <Card.Body>
              <div className="mb-3">
                <strong>{load.cartridge}</strong>
                <br />
                <small className="text-muted">Cartridge</small>
              </div>
              
              <div className="mb-3">
                <strong>{load.bullet}</strong>
                <br />
                <small className="text-muted">Bullet</small>
              </div>
              
              <div className="mb-3">
                <strong>{load.powder}</strong>
                {load.powderCharge && (
                  <>
                    <br />
                    <Badge bg="secondary">{load.powderCharge} grains</Badge>
                  </>
                )}
                <br />
                <small className="text-muted">Powder</small>
              </div>

              {load.velocity && (
                <div className="mb-3">
                  <Badge bg="primary" className="fs-6">{load.velocity} fps</Badge>
                  <br />
                  <small className="text-muted">Velocity</small>
                </div>
              )}
            </Card.Body>
          </Card>

          <Card>
            <Card.Header>
              <h6 className="mb-0">Metadata</h6>
            </Card.Header>
            <Card.Body>
              <div className="mb-2">
                <small className="text-muted">Created:</small>
                <br />
                <span>{formatDate(load.createdAt)}</span>
              </div>
              
              {load.updatedAt && load.updatedAt !== load.createdAt && (
                <div className="mb-2">
                  <small className="text-muted">Updated:</small>
                  <br />
                  <span>{formatDate(load.updatedAt)}</span>
                </div>
              )}
              
              <div>
                <small className="text-muted">Created by:</small>
                <br />
                <span>{load.createdBy}</span>
              </div>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </Container>
  );
};

export default LoadDetail;
