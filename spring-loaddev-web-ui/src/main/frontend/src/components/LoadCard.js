import React from 'react';
import { Card, Row, Col, Button, Badge } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import { FaEye, FaEdit, FaTrash } from 'react-icons/fa';

const LoadCard = ({ load, onDelete }) => {
  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString();
  };

  return (
    <Card className="mb-3">
      <Card.Body>
        <Row>
          <Col xs={12} md={8}>
            <h6 className="mb-2">
              <Badge bg="primary" className="me-2">{load.cartridge}</Badge>
              {load.bullet}
            </h6>
            <Row className="small text-muted">
              <Col xs={6} sm={4}>
                <strong>Powder:</strong> {load.powder}
              </Col>
              <Col xs={6} sm={4}>
                <strong>Charge:</strong> {load.powderCharge ? `${load.powderCharge} gr` : 'N/A'}
              </Col>
              <Col xs={6} sm={4}>
                <strong>Velocity:</strong> {load.velocity ? `${load.velocity} fps` : 'N/A'}
              </Col>
              <Col xs={6} sm={4}>
                <strong>Created:</strong> {formatDate(load.createdAt)}
              </Col>
            </Row>
          </Col>
          <Col xs={12} md={4} className="text-md-end mt-2 mt-md-0">
            <div className="d-flex gap-1 justify-content-end">
              <Button 
                variant="outline-primary" 
                size="sm" 
                as={Link} 
                to={`/loads/${load.id}`}
                aria-label={`View load ${load.id}`}
              >
                <FaEye />
              </Button>
              <Button 
                variant="outline-warning" 
                size="sm" 
                as={Link} 
                to={`/loads/${load.id}/edit`}
                aria-label={`Edit load ${load.id}`}
              >
                <FaEdit />
              </Button>
              <Button
                variant="outline-danger"
                size="sm"
                onClick={() => onDelete(load.id)}
                aria-label={`Delete load ${load.id}`}
              >
                <FaTrash />
              </Button>
            </div>
          </Col>
        </Row>
      </Card.Body>
    </Card>
  );
};

export default LoadCard;
