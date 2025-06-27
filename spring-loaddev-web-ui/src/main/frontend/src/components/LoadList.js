import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Table, Button, Form, InputGroup, Pagination, Spinner, Alert } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import { FaPlus, FaSearch, FaEdit, FaTrash, FaEye } from 'react-icons/fa';
import { toast } from 'react-toastify';
import { loadService } from '../services/loadService';
import { useAuth } from '../contexts/AuthContext';
import LoadCard from './LoadCard';

const LoadList = () => {
  const { authenticated } = useAuth();
  const [loads, setLoads] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [pageSize] = useState(10);
  const [sortBy, setSortBy] = useState('createdAt');
  const [sortDir, setSortDir] = useState('desc');
  const [isMobile, setIsMobile] = useState(window.innerWidth < 768);

  useEffect(() => {
    fetchLoads();
  }, [currentPage, sortBy, sortDir, searchTerm]);

  useEffect(() => {
    const handleResize = () => {
      setIsMobile(window.innerWidth < 768);
    };
    
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  const fetchLoads = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await loadService.getLoads(currentPage, pageSize, sortBy, sortDir, searchTerm);
      setLoads(response.content || []);
      setTotalPages(response.totalPages || 0);
      setTotalElements(response.totalElements || 0);
    } catch (err) {
      setError('Failed to fetch loads. Please try again.');
      console.error('Error fetching loads:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    setCurrentPage(0);
    fetchLoads();
  };

  const handleSort = (field) => {
    if (sortBy === field) {
      setSortDir(sortDir === 'asc' ? 'desc' : 'asc');
    } else {
      setSortBy(field);
      setSortDir('asc');
    }
    setCurrentPage(0);
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this load?')) {
      try {
        await loadService.deleteLoad(id);
        toast.success('Load deleted successfully');
        fetchLoads();
      } catch (err) {
        toast.error('Failed to delete load');
        console.error('Error deleting load:', err);
      }
    }
  };

  const getSortIcon = (field) => {
    if (sortBy !== field) return '';
    return sortDir === 'asc' ? ' ↑' : ' ↓';
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString();
  };

  const renderPagination = () => {
    if (totalPages <= 1) return null;

    const items = [];
    const maxVisiblePages = 5;
    const startPage = Math.max(0, currentPage - Math.floor(maxVisiblePages / 2));
    const endPage = Math.min(totalPages - 1, startPage + maxVisiblePages - 1);

    if (currentPage > 0) {
      items.push(
        <Pagination.First key="first" onClick={() => setCurrentPage(0)} />,
        <Pagination.Prev key="prev" onClick={() => setCurrentPage(currentPage - 1)} />
      );
    }

    for (let page = startPage; page <= endPage; page++) {
      items.push(
        <Pagination.Item
          key={page}
          active={page === currentPage}
          onClick={() => setCurrentPage(page)}
        >
          {page + 1}
        </Pagination.Item>
      );
    }

    if (currentPage < totalPages - 1) {
      items.push(
        <Pagination.Next key="next" onClick={() => setCurrentPage(currentPage + 1)} />,
        <Pagination.Last key="last" onClick={() => setCurrentPage(totalPages - 1)} />
      );
    }

    return <Pagination className="justify-content-center">{items}</Pagination>;
  };

  if (!authenticated) {
    return (
      <Container>
        <Alert variant="warning">
          Please <a href="/login">login</a> to view your loads.
        </Alert>
      </Container>
    );
  }

  return (
    <Container className="fade-in">
      <Row className="mb-4">
        <Col>
          <div className="d-flex justify-content-between align-items-center">
            <h2>My Loads ({totalElements})</h2>
            <Button variant="primary" as={Link} to="/loads/new">
              <FaPlus className="me-2" />
              New Load
            </Button>
          </div>
        </Col>
      </Row>

      <Row className="mb-4">
        <Col md={6}>
          <Form onSubmit={handleSearch}>
            <InputGroup>
              <Form.Control
                type="text"
                placeholder="Search loads..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="search-box"
              />
              <Button variant="outline-secondary" type="submit">
                <FaSearch />
              </Button>
            </InputGroup>
          </Form>
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
        <Col>
          <Card>
            <Card.Body>
              {loading ? (
                <div className="loading-spinner">
                  <Spinner animation="border" role="status">
                    <span className="visually-hidden">Loading...</span>
                  </Spinner>
                </div>
              ) : loads.length === 0 ? (
                <div className="empty-state">
                  <div className="empty-state-icon">📊</div>
                  <h4>No loads found</h4>
                  <p>
                    {searchTerm 
                      ? 'No loads match your search criteria.' 
                      : 'You haven\'t created any loads yet.'}
                  </p>
                  <Button variant="primary" as={Link} to="/loads/new">
                    <FaPlus className="me-2" />
                    Create Your First Load
                  </Button>
                </div>
              ) : (
                <>
                  {isMobile ? (
                    // Mobile card view
                    <div>
                      {loads.map((load) => (
                        <LoadCard key={load.id} load={load} onDelete={handleDelete} />
                      ))}
                    </div>
                  ) : (
                    // Desktop table view
                    <div className="table-responsive">
                      <Table hover>
                        <thead>
                          <tr>
                            <th 
                              style={{ cursor: 'pointer' }}
                              onClick={() => handleSort('cartridge')}
                              role="button"
                              tabIndex={0}
                              onKeyDown={(e) => e.key === 'Enter' && handleSort('cartridge')}
                              aria-label="Sort by cartridge"
                            >
                              Cartridge{getSortIcon('cartridge')}
                            </th>
                            <th 
                              style={{ cursor: 'pointer' }}
                              onClick={() => handleSort('bullet')}
                              role="button"
                              tabIndex={0}
                              onKeyDown={(e) => e.key === 'Enter' && handleSort('bullet')}
                              aria-label="Sort by bullet"
                            >
                              Bullet{getSortIcon('bullet')}
                            </th>
                            <th 
                              style={{ cursor: 'pointer' }}
                              onClick={() => handleSort('powder')}
                              role="button"
                              tabIndex={0}
                              onKeyDown={(e) => e.key === 'Enter' && handleSort('powder')}
                              aria-label="Sort by powder"
                            >
                              Powder{getSortIcon('powder')}
                            </th>
                            <th 
                              style={{ cursor: 'pointer' }}
                              onClick={() => handleSort('powderCharge')}
                              role="button"
                              tabIndex={0}
                              onKeyDown={(e) => e.key === 'Enter' && handleSort('powderCharge')}
                              aria-label="Sort by powder charge"
                            >
                              Charge{getSortIcon('powderCharge')}
                            </th>
                            <th 
                              style={{ cursor: 'pointer' }}
                              onClick={() => handleSort('velocity')}
                              role="button"
                              tabIndex={0}
                              onKeyDown={(e) => e.key === 'Enter' && handleSort('velocity')}
                              aria-label="Sort by velocity"
                            >
                              Velocity{getSortIcon('velocity')}
                            </th>
                            <th 
                              style={{ cursor: 'pointer' }}
                              onClick={() => handleSort('createdAt')}
                              role="button"
                              tabIndex={0}
                              onKeyDown={(e) => e.key === 'Enter' && handleSort('createdAt')}
                              aria-label="Sort by creation date"
                            >
                              Created{getSortIcon('createdAt')}
                            </th>
                            <th width="150">Actions</th>
                          </tr>
                        </thead>
                        <tbody>
                          {loads.map((load) => (
                            <tr key={load.id}>
                              <td>{load.cartridge}</td>
                              <td>{load.bullet}</td>
                              <td>{load.powder}</td>
                              <td>{load.powderCharge ? `${load.powderCharge} gr` : 'N/A'}</td>
                              <td>{load.velocity ? `${load.velocity} fps` : 'N/A'}</td>
                              <td>{formatDate(load.createdAt)}</td>
                              <td>
                                <div className="d-flex gap-1">
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
                                    onClick={() => handleDelete(load.id)}
                                    aria-label={`Delete load ${load.id}`}
                                  >
                                    <FaTrash />
                                  </Button>
                                </div>
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </Table>
                    </div>
                  )}
                  
                  {renderPagination()}
                </>
              )}
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </Container>
  );
};

export default LoadList;
