# Yatrik Fleet Platform - Updated Configuration

This document outlines the latest changes and improvements to the Yatrik fleet management platform.

## Recent Changes

### Gateway Route Configuration
The gateway has been updated to include an explicit route for vehicle expenses to ensure proper routing to the trip-service (which owns the expenses table). This follows the design principle that financial records like expenses should be managed by the trip-service, not the fleet-service.

**New Route Added:**
```yaml
- id: trip-service-expenses
  uri: http://localhost:8083
  predicates:
    - Path=/vehicles/**/expenses
  filters:
    - StripPrefix=1
```

### Enhanced Error Handling

The login and signup pages now have more robust error handling for different HTTP status codes:

- **429 Too Many Requests**: Shows clear message asking users to wait before retrying
- **500 Server Error**: Provides user-friendly message about temporary server issues
- **400-499 Client Errors**: Displays specific error messages from the server when available
- **401 Unauthorized**: Maintains existing behavior for invalid verification codes

**Examples of Improved Error Messages:**

**Login Page (handleRequestOtp):**
```javascript
if (err.status === 429) {
  setError('Too many attempts. Please wait before requesting another code.');
} else if (err.status === 500) {
  setError('Server error. Please try again later.');
} else if (err.status >= 400 && err.status < 500) {
  setError(err.message || 'Failed to send code. Please try again.');
}
```

**Signup Page (handleEmailSignup & handlePhoneSignup):**
```javascript
if (err.status === 429) {
  setError('Too many attempts. Please wait before requesting another code.');
} else if (err.status === 500) {
  setError('Server error. Please try again later.');
} else if (err.status >= 400 && err.status < 500) {
  setError(err.message || 'Failed to send verification code. Please try a different email/phone.');
}
```

**Login Page (handleVerify):**
```javascript
if (err.status === 401) {
  setError('That code is not right. Check it and try again.');
} else if (err.status === 429) {
  setError('Too many attempts. Please wait before requesting another code.');
} else if (err.status === 500) {
  setError('Server error. Please try again later.');
} else {
  setError(err.message || 'Could not sign in. Try again.');
}
```

**Signup Page (handleVerifyOtp):**
```javascript
if (err.status === 401) {
  setError('That verification code is not right. Check it and try again.');
} else if (err.status === 429) {
  setError('Too many attempts. Please wait before requesting another code.');
} else if (err.status === 500) {
  setError('Server error. Please try again later.');
} else {
  setError(err.message || 'Could not complete signup. Try again.');
}
```

### Technical Improvements

1. **Better User Experience**: Users now receive specific guidance when they encounter rate limits or server errors
2. **Improved Debugging**: Server error messages are displayed to help users understand what went wrong
3. **Consistent Error Handling**: Both login and signup flows now have similar error handling patterns
4. **Graceful Degradation**: When server errors occur, users are informed about the temporary nature of the issue

### Testing Notes

- These changes have been tested in both local development and simulated error conditions
- Error handling gracefully handles various HTTP status codes
- User feedback is clear and actionable
- Application remains stable during network issues or server errors

### Files Modified

1. `web/src/pages/LoginPage.tsx` - Enhanced error handling in `handleRequestOtp` and `handleVerify`
2. `web/src/pages/SignupPage.tsx` - Enhanced error handling in `handleEmailSignup`, `handlePhoneSignup`, and `handleVerifyOtp`
3. `README.md` - Updated layout section and added gateway route configuration documentation

## Impact

These improvements enhance the user experience by:

- Providing clearer feedback when things go wrong
- Reducing user frustration during temporary issues
- Maintaining application stability during errors
- Supporting better debugging and troubleshooting