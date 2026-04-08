// Helper funkcije za sanitizaciju inputa na frontendu

/**
 * Sanitizuje string tako da uklanja potencijalno opasne HTML karaktere
 * @param {string} str - String za sanitizaciju
 * @returns {string} - Sanitizovani string
 */
export const sanitizeString = (str) => {
  if (typeof str !== 'string') return str;
  
  // Ukloni HTML tagove i escape-uj specijalne karaktere
  return str
    .replace(/[<>]/g, '') // Ukloni < i >
    .replace(/&/g, '&amp;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#x27;')
    .replace(/\//g, '&#x2F;')
    .trim();
};

/**
 * Sanitizuje input polje - uklanja leading/trailing whitespace i ograničava dužinu
 * @param {string} value - Vrijednost za sanitizaciju
 * @param {number} maxLength - Maksimalna dužina (opciono)
 * @returns {string} - Sanitizovana vrijednost
 */
export const sanitizeInput = (value, maxLength = null) => {
  if (typeof value !== 'string') return value;
  
  let sanitized = value.trim();
  
  if (maxLength && sanitized.length > maxLength) {
    sanitized = sanitized.substring(0, maxLength);
  }
  
  return sanitized;
};

/**
 * Validira email format
 * @param {string} email - Email za validaciju
 * @returns {boolean} - True ako je email validan
 */
export const validateEmail = (email) => {
  if (typeof email !== 'string') return false;
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email.trim());
};

/**
 * Validira da li je vrijednost pozitivan cijeli broj
 * @param {string|number} value - Vrijednost za validaciju
 * @returns {boolean} - True ako je pozitivan cijeli broj
 */
export const validatePositiveInteger = (value) => {
  const num = typeof value === 'string' ? parseInt(value, 10) : value;
  return Number.isInteger(num) && num > 0;
};

