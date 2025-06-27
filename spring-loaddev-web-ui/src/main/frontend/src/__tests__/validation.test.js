import { validators, sanitizers } from '../utils/validation';

describe('Validation Utilities', () => {
  describe('validators.required', () => {
    test('returns error for empty string', () => {
      expect(validators.required('')).toBe('This field is required');
      expect(validators.required('   ')).toBe('This field is required');
    });

    test('returns null for valid value', () => {
      expect(validators.required('valid')).toBeNull();
    });
  });

  describe('validators.powderCharge', () => {
    test('validates powder charge range', () => {
      expect(validators.powderCharge('-1')).toBe('Powder charge must be between 0.1 and 200 grains');
      expect(validators.powderCharge('201')).toBe('Powder charge must be between 0.1 and 200 grains');
      expect(validators.powderCharge('50')).toBeNull();
    });
  });

  describe('validators.velocity', () => {
    test('validates velocity range', () => {
      expect(validators.velocity('50')).toBe('Velocity must be between 100 and 5000 fps');
      expect(validators.velocity('6000')).toBe('Velocity must be between 100 and 5000 fps');
      expect(validators.velocity('2500')).toBeNull();
    });
  });
});

describe('Sanitization Utilities', () => {
  describe('sanitizers.text', () => {
    test('removes script tags', () => {
      const input = 'Hello <script>alert("xss")</script> World';
      const expected = 'Hello  World';
      expect(sanitizers.text(input)).toBe(expected);
    });

    test('removes javascript: protocols', () => {
      const input = 'javascript:alert("xss")';
      const expected = 'alert("xss")';
      expect(sanitizers.text(input)).toBe(expected);
    });
  });

  describe('sanitizers.number', () => {
    test('removes non-numeric characters', () => {
      expect(sanitizers.number('123abc')).toBe('123');
      expect(sanitizers.number('12.34')).toBe('12.34');
      expect(sanitizers.number('-12.34')).toBe('-12.34');
    });
  });
});
