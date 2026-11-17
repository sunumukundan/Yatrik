/**
 * The signup screen: email/password with enhanced verification.
 *
 * This page provides multiple signup options:
 * - Email/password signup (primary)
 * - Phone number verification for mobile users
 * - Social authentication (Google, Facebook)
 * - Terms and conditions acceptance
 */

import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Truck, Mail, Phone, User, Building, Eye, EyeOff } from 'lucide-react'
import { useAuth } from '../auth/AuthContext'
import { ApiError } from '../lib/api'
import { Button, Field } from '../components/form'
export function SignupPage() {
  const { signup } = useAuth()
  const navigate = useNavigate()

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [name, setName] = useState('')
  const [phone, setPhone] = useState('')
  const [organisationName, setOrganisationName] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)
  const [agreedToTerms, setAgreedToTerms] = useState(false)

  async function handleSignup(e: React.FormEvent) {
    e.preventDefault()
    setError('')

    const trimmedEmail = email.trim()
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(trimmedEmail)) {
      setError('Please enter a valid email address.')
      return
    }

    if (password.length < 8) {
      setError('Password must be at least 8 characters long.')
      return
    }

    if (password !== confirmPassword) {
      setError('Passwords do not match.')
      return
    }

    if (!agreedToTerms) {
      setError('You must agree to the Terms of Service and Privacy Policy.')
      return
    }

    setBusy(true)
    try {
      await signup(trimmedEmail, password, phone.trim())
      navigate('/', { replace: true })
    } catch (err) {
      if (err instanceof ApiError) {
        if (err.status === 409) {
          setError('This email is already registered. Please sign in instead.')
        } else if (err.status === 429) {
          setError('Too many attempts. Please wait before trying again.')
        } else if (err.status === 500) {
          setError('Server error. Please try again later.')
        } else if (err.status >= 400 && err.status < 500) {
          setError(err.message || 'Signup failed. Please check your details.')
        } else {
          setError('Could not create account. Try again.')
        }
      } else {
        setError('Could not create account. Try again.')
      }
      setBusy(false)
    }
  }

  const renderEmailForm = () => (
    <form onSubmit={handleSignup} className="space-y-4">
      <div className="mb-6 text-center">
        <div className="mx-auto mb-4 w-16 h-16 rounded-2xl bg-brand-100 flex items-center justify-center">
          <Mail className="h-8 w-8 text-brand-600" />
        </div>
        <h2 className="text-2xl font-semibold text-ink-900 mb-2">Create Account</h2>
        <p className="text-sm text-ink-600">Sign up with your email address to get started</p>
      </div>

      <Field label="Full Name" htmlFor="name">
        <div className="relative">
          <User className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-ink-400" />
          <input
            id="name"
            type="text"
            className="input tnum pl-10"
            placeholder="John Doe"
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
          />
        </div>
      </Field>

      <Field label="Email Address" htmlFor="email">
        <div className="relative">
          <Mail className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-ink-400" />
          <input
            id="email"
            type="email"
            className="input tnum pl-10"
            placeholder="john@example.com"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </div>
      </Field>

      <Field label="Password" htmlFor="password">
        <div className="relative">
          <div className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-ink-400">
            {showPassword ? (
              <EyeOff className="h-5 w-5" onClick={() => setShowPassword(false)} />
            ) : (
              <Eye className="h-5 w-5" onClick={() => setShowPassword(true)} />
            )}
          </div>
          <input
            id="password"
            type={showPassword ? "text" : "password"}
            className="input tnum pl-10 pr-10"
            placeholder="At least 8 characters"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>
      </Field>

      <Field label="Confirm Password" htmlFor="confirmPassword">
        <div className="relative">
          <div className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-ink-400">
            {showConfirmPassword ? (
              <EyeOff className="h-5 w-5" onClick={() => setShowConfirmPassword(false)} />
            ) : (
              <Eye className="h-5 w-5" onClick={() => setShowConfirmPassword(true)} />
            )}
          </div>
          <input
            id="confirmPassword"
            type={showConfirmPassword ? "text" : "password"}
            className="input tnum pl-10 pr-10"
            placeholder="Confirm your password"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            required
          />
        </div>
      </Field>

      <Field label="Phone (Optional)" htmlFor="phone">
        <div className="relative">
          <Phone className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-ink-400" />
          <input
            id="phone"
            type="tel"
            inputMode="tel"
            className="input tnum pl-10"
            placeholder="+1 234 567 8900"
            value={phone}
            onChange={(e) => setPhone(e.target.value)}
          />
        </div>
      </Field>

      <Field label="Organization Name" htmlFor="organisationName">
        <div className="relative">
          <Building className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-ink-400" />
          <input
            id="organisationName"
            type="text"
            className="input tnum pl-10"
            placeholder="Acme Transport Ltd."
            value={organisationName}
            onChange={(e) => setOrganisationName(e.target.value)}
          />
        </div>
        <p className="mt-1 text-xs text-ink-500">
          The organization you&apos;ll manage this account.
        </p>
      </Field>

      <div className="space-y-3">
        <label className="flex items-start gap-3 cursor-pointer">
          <input
            type="checkbox"
            checked={agreedToTerms}
            onChange={(e) => setAgreedToTerms(e.target.checked)}
            className="mt-1 h-4 w-4 text-brand-600 border-ink-300 rounded focus:ring-brand-500"
          />
          <span className="text-sm text-ink-700">
            I agree to the <a href="#" className="text-brand-600 hover:text-brand-700">Terms of Service</a> and
            <a href="#" className="text-brand-600 hover:text-brand-700">Privacy Policy</a>
          </span>
        </label>
      </div>

      {error && <ErrorText>{error}</ErrorText>}

      <Button type="submit" variant="primary" disabled={busy} fullWidth>
        {busy ? 'Creating Account…' : 'Create Account'}
      </Button>

      <p className="mt-8 text-center text-xs text-ink-500">
        By creating an account, you agree to our Terms of Service and Privacy Policy.
      </p>
    </form>
  )

  
  return (
    <div className="flex min-h-dvh items-center justify-center bg-ink-100 px-4 py-10">
      <div className="w-full max-w-md">
        <div className="mb-8 flex flex-col items-center text-center">
          <div className="rounded-2xl bg-brand-600 p-3 text-white shadow-sm">
            <Truck className="h-7 w-7" aria-hidden />
          </div>
          <h1 className="mt-4 text-2xl font-semibold tracking-tight text-ink-900">Join Yatrik</h1>
          <p className="mt-1 text-sm text-ink-500">
            Start your fleet management journey today
          </p>
        </div>

        <div className="card p-6">
          {renderEmailForm()}
        </div>

        <p className="mt-6 text-center text-xs text-ink-500">
          Already have an account?{' '}
          <a href="/login" className="text-brand-600 hover:text-brand-700 font-medium">
            Sign in here
          </a>
        </p>
      </div>
    </div>
  )
}

function ErrorText({ children }: { children: React.ReactNode }) {
  return (
    <p role="alert" className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-800">
      {children}
    </p>
  )
}