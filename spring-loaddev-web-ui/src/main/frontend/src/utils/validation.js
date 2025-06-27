import React from 'react';

// Validation utilities for form inputs
export const validators = {
  required: (value) => {
    if (!value || (typeof value === 'string' && value.trim() === '')) {
      return 'This field is required';
    }
    return null;
  },

  minLength: (min) => (value) => {
    if (value && value.length < min) {
      return `Minimum ${min} characters required`;
    }
    return null;
  },

  maxLength: (max) => (value) => {
    if (value && value.length > max) {
      return `Maximum ${max} characters allowed`;
    }
    return null;
  },

  number: (value) => {
    if (value && isNaN(Number(value))) {
      return 'Must be a valid number';
    }
    return null;
  },

  positiveNumber: (value) => {
    if (value && (isNaN(Number(value)) || Number(value) <= 0)) {
      return 'Must be a positive number';
    }
    return null;
  },

  range: (min, max) => (value) => {
    if (value) {
      const num = Number(value);
      if (isNaN(num)) {
        return 'Must be a valid number';
      }
      if (num < min || num > max) {
        return `Must be between ${min} and ${max}`;
      }
    }
    return null;
  },

  decimal: (places = 2) => (value) => {
    if (value) {
      const regex = new RegExp(`^\\d+(\\.\\d{1,${places}})?$`);
      if (!regex.test(value)) {
        return `Must be a decimal with up to ${places} decimal places`;
      }
    }
    return null;
  },

  // Ammunition-specific validators
  powderCharge: (value) => {
    if (value) {
      const num = Number(value);
      if (isNaN(num) || num <= 0 || num > 200) {
        return 'Powder charge must be between 0.1 and 200 grains';
      }
    }
    return null;
  },

  velocity: (value) => {
    if (value) {
      const num = Number(value);
      if (isNaN(num) || num < 100 || num > 5000) {
        return 'Velocity must be between 100 and 5000 fps';
      }
    }
    return null;
  },

  overallLength: (value) => {
    if (value) {
      const num = Number(value);
      if (isNaN(num) || num < 0.5 || num > 10) {
        return 'Overall length must be between 0.5 and 10 inches';
      }
    }
    return null;
  }
};

// Sanitization utilities
export const sanitizers = {
  text: (value) => {
    if (typeof value !== 'string') return value;
    // Basic XSS prevention - remove script tags and javascript: protocols
    return value
      .replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, '')
      .replace(/javascript:/gi, '')
      .trim();
  },

  number: (value) => {
    if (!value) return '';
    return value.toString().replace(/[^0-9.-]/g, '');
  },

  decimal: (value, places = 2) => {
    if (!value) return '';
    const cleaned = value.toString().replace(/[^0-9.]/g, '');
    const parts = cleaned.split('.');
    if (parts.length > 2) {
      return parts[0] + '.' + parts.slice(1).join('');
    }
    if (parts[1] && parts[1].length > places) {
      return parts[0] + '.' + parts[1].substring(0, places);
    }
    return cleaned;
  }
};

// Validation hook
export const useFormValidation = (initialValues, validationRules) => {
  const [values, setValues] = React.useState(initialValues);
  const [errors, setErrors] = React.useState({});
  const [touched, setTouchedFields] = React.useState({});

  const validateField = (name, value) => {
    const rules = validationRules[name];
    if (!rules) return null;

    for (const rule of rules) {
      const error = rule(value);
      if (error) return error;
    }
    return null;
  };

  const validateAll = () => {
    const newErrors = {};
    Object.keys(validationRules).forEach(name => {
      const error = validateField(name, values[name]);
      if (error) {
        newErrors[name] = error;
      }
    });
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const setValue = (name, value) => {
    setValues(prev => ({ ...prev, [name]: value }));
    
    // Validate field on change if it's been touched
    if (touched[name]) {
      const error = validateField(name, value);
      setErrors(prev => ({
        ...prev,
        [name]: error
      }));
    }
  };

  const setTouched = (name) => {
    setTouchedFields(prev => ({ ...prev, [name]: true }));
    
    // Validate field when touched
    const error = validateField(name, values[name]);
    setErrors(prev => ({
      ...prev,
      [name]: error
    }));
  };

  const reset = () => {
    setValues(initialValues);
    setErrors({});
    setTouchedFields({});
  };

  return {
    values,
    errors,
    touched,
    setValue,
    setTouched,
    validateAll,
    reset,
    isValid: Object.keys(errors).length === 0
  };
};
