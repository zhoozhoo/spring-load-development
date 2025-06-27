import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Form, Button, Alert, Spinner } from 'react-bootstrap';
import { useNavigate, useParams } from 'react-router-dom';
import { toast } from 'react-toastify';
import { FaSave, FaArrowLeft } from 'react-icons/fa';
import { loadService } from '../services/loadService';
import { useAuth } from '../contexts/AuthContext';

const LoadForm = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const { authenticated } = useAuth();
  const isEditing = !!id;

  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [validated, setValidated] = useState(false);

  const [formData, setFormData] = useState({
    cartridge: '',
    bullet: '',
    powder: '',
    powderCharge: '',
    primer: '',
    caseName: '',
    overallLength: '',
    velocity: '',
    notes: ''
  });

  useEffect(() => {
    if (isEditing) {
      fetchLoad();
    }
  }, [id, isEditing]);

  const fetchLoad = async () => {
    try {
      setLoading(true);
      setError(null);
      const load = await loadService.getLoadById(id);
      setFormData({
        cartridge: load.cartridge || '',
        bullet: load.bullet || '',
        powder: load.powder || '',
        powderCharge: load.powderCharge ? load.powderCharge.toString() : '',
        primer: load.primer || '',
        caseName: load.caseName || '',
        overallLength: load.overallLength ? load.overallLength.toString() : '',
        velocity: load.velocity ? load.velocity.toString() : '',
        notes: load.notes || ''
      });
    } catch (err) {
      setError('Failed to fetch load data. Please try again.');
      console.error('Error fetching load:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const form = e.currentTarget;
    
    if (form.checkValidity() === false) {
      e.stopPropagation();
      setValidated(true);
      return;
    }

    try {
      setSubmitting(true);
      setError(null);

      const submitData = {
        ...formData,
        powderCharge: formData.powderCharge ? parseFloat(formData.powderCharge) : null,
        overallLength: formData.overallLength ? parseFloat(formData.overallLength) : null,
        velocity: formData.velocity ? parseInt(formData.velocity) : null
      };

      if (isEditing) {
        await loadService.updateLoad(id, submitData);
        toast.success('Load updated successfully');
      } else {
        await loadService.createLoad(submitData);
        toast.success('Load created successfully');
      }

      navigate('/loads');
    } catch (err) {
      setError(`Failed to ${isEditing ? 'update' : 'create'} load. Please try again.`);
      console.error(`Error ${isEditing ? 'updating' : 'creating'} load:`, err);
    } finally {
      setSubmitting(false);
    }
  };

  const handleCancel = () => {
    navigate('/loads');
  };

  if (!authenticated) {
    return (
      <Container>
        <Alert variant="warning">
          Please <a href="/login">login</a> to create or edit loads.
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

  return (
    <Container className="fade-in">
      <Row className="mb-4">
        <Col>
          <div className="d-flex align-items-center">
            <Button variant="outline-secondary" onClick={handleCancel} className="me-3">
              <FaArrowLeft />
            </Button>
            <h2>{isEditing ? 'Edit Load' : 'Create New Load'}</h2>
          </div>
        </Col>
      </Row>

      {error && (
        <Row className="mb-4">
          <Col>
            <Alert variant="danger">{error}</Alert>
          </Col>
        </Row>
      )}

      <Row>
        <Col lg={8}>
          <Card>
            <Card.Header>
              <h5 className="mb-0">Load Details</h5>
            </Card.Header>
            <Card.Body>
              <Form noValidate validated={validated} onSubmit={handleSubmit}>
                <Row>
                  <Col md={6}>
                    <Form.Group className="mb-3">
                      <Form.Label>Cartridge *</Form.Label>
                      <Form.Control
                        type="text"
                        name="cartridge"
                        value={formData.cartridge}
                        onChange={handleInputChange}
                        required
                        placeholder="e.g., 223 Remington"
                      />
                      <Form.Control.Feedback type="invalid">
                        Please provide a cartridge name.
                      </Form.Control.Feedback>
                    </Form.Group>
                  </Col>
                  <Col md={6}>
                    <Form.Group className="mb-3">
                      <Form.Label>Bullet *</Form.Label>
                      <Form.Control
                        type="text"
                        name="bullet"
                        value={formData.bullet}
                        onChange={handleInputChange}
                        required
                        placeholder="e.g., Sierra MatchKing 77gr"
                      />
                      <Form.Control.Feedback type="invalid">
                        Please provide a bullet description.
                      </Form.Control.Feedback>
                    </Form.Group>
                  </Col>
                </Row>

                <Row>
                  <Col md={6}>
                    <Form.Group className="mb-3">
                      <Form.Label>Powder *</Form.Label>
                      <Form.Control
                        type="text"
                        name="powder"
                        value={formData.powder}
                        onChange={handleInputChange}
                        required
                        placeholder="e.g., IMR 8208 XBR"
                      />
                      <Form.Control.Feedback type="invalid">
                        Please provide a powder name.
                      </Form.Control.Feedback>
                    </Form.Group>
                  </Col>
                  <Col md={6}>
                    <Form.Group className="mb-3">
                      <Form.Label>Powder Charge (grains)</Form.Label>
                      <Form.Control
                        type="number"
                        step="0.1"
                        name="powderCharge"
                        value={formData.powderCharge}
                        onChange={handleInputChange}
                        placeholder="e.g., 23.5"
                      />
                    </Form.Group>
                  </Col>
                </Row>

                <Row>
                  <Col md={6}>
                    <Form.Group className="mb-3">
                      <Form.Label>Primer</Form.Label>
                      <Form.Control
                        type="text"
                        name="primer"
                        value={formData.primer}
                        onChange={handleInputChange}
                        placeholder="e.g., CCI 400"
                      />
                    </Form.Group>
                  </Col>
                  <Col md={6}>
                    <Form.Group className="mb-3">
                      <Form.Label>Case</Form.Label>
                      <Form.Control
                        type="text"
                        name="caseName"
                        value={formData.caseName}
                        onChange={handleInputChange}
                        placeholder="e.g., Lake City"
                      />
                    </Form.Group>
                  </Col>
                </Row>

                <Row>
                  <Col md={6}>
                    <Form.Group className="mb-3">
                      <Form.Label>Overall Length (inches)</Form.Label>
                      <Form.Control
                        type="number"
                        step="0.001"
                        name="overallLength"
                        value={formData.overallLength}
                        onChange={handleInputChange}
                        placeholder="e.g., 2.260"
                      />
                    </Form.Group>
                  </Col>
                  <Col md={6}>
                    <Form.Group className="mb-3">
                      <Form.Label>Velocity (fps)</Form.Label>
                      <Form.Control
                        type="number"
                        name="velocity"
                        value={formData.velocity}
                        onChange={handleInputChange}
                        placeholder="e.g., 2650"
                      />
                    </Form.Group>
                  </Col>
                </Row>

                <Form.Group className="mb-4">
                  <Form.Label>Notes</Form.Label>
                  <Form.Control
                    as="textarea"
                    rows={4}
                    name="notes"
                    value={formData.notes}
                    onChange={handleInputChange}
                    placeholder="Additional notes about this load..."
                  />
                </Form.Group>

                <div className="d-flex gap-2">
                  <Button
                    type="submit"
                    variant="primary"
                    disabled={submitting}
                  >
                    {submitting ? (
                      <>
                        <Spinner
                          as="span"
                          animation="border"
                          size="sm"
                          role="status"
                          aria-hidden="true"
                          className="me-2"
                        />
                        {isEditing ? 'Updating...' : 'Creating...'}
                      </>
                    ) : (
                      <>
                        <FaSave className="me-2" />
                        {isEditing ? 'Update Load' : 'Create Load'}
                      </>
                    )}
                  </Button>
                  <Button
                    type="button"
                    variant="outline-secondary"
                    onClick={handleCancel}
                    disabled={submitting}
                  >
                    Cancel
                  </Button>
                </div>
              </Form>
            </Card.Body>
          </Card>
        </Col>

        <Col lg={4}>
          <Card>
            <Card.Header>
              <h6 className="mb-0">Tips</h6>
            </Card.Header>
            <Card.Body>
              <ul className="list-unstyled">
                <li className="mb-2">
                  <strong>Cartridge:</strong> Include caliber and type (e.g., "223 Remington")
                </li>
                <li className="mb-2">
                  <strong>Bullet:</strong> Include weight and type (e.g., "77gr BTHP")
                </li>
                <li className="mb-2">
                  <strong>Powder:</strong> Use exact powder name and manufacturer
                </li>
                <li className="mb-2">
                  <strong>Safety:</strong> Always follow safe loading practices and start with minimum charges
                </li>
              </ul>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </Container>
  );
};

export default LoadForm;
