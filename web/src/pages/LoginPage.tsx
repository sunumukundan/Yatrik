/**
 * The login screen: supports phone OTP, email OTP, and email/password login.
 *
 * Three login methods:
 * - Phone OTP: SMS-based authentication
 * - Email OTP: Email-based authentication
 * - Email/Password: Traditional credentials
 *
 * Two details worth knowing:
 *
 * - The OTP is delivered over SMS/email by the configured sender in identity-service (the
 *   log-based one while app.sms.provider=log, the Twilio one when it is set to twilio).
 *   For local development the code lands in the backend logs; the screen just asks for
 *   the code and never pretends a real message was on its way.
 *
 * - The OTP request endpoint answers 202 whatever you send it, deliberately, so it
 *   cannot be used to find out whether an identifier is registered. The screen
 *   therefore never says "no account found" — and for an identifier that is new, it asks
 *   for the organisation's name on the verify step.
 */

import { useState } from 'react'
import { ArrowLeft, Truck, Mail, Phone, Lock, Eye, EyeOff } from 'lucide-react'
import { useAuth } from '../auth/AuthContext'
import { ApiError } from '../lib/api'
import { Button, Field } from '../components/form'
import { Link, useNavigate } from 'react-router-dom'

export function LoginPage() {
  const { requestOtp, verifyOtp, login } = useAuth()
  const navigate = useNavigate()

  const [method, setMethod] = useState<'phone' | 'email' | 'password'>('phone')
  const [step, setStep] = useState<'input' | 'otp'>('input')
  const [phone, setPhone] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [otp, setOtp] = useState('')
  const [name, setName] = useState('')
  const [organisationName, setOrganisationName] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const [showPassword, setShowPassword] = useState(false)

  async function handleRequestOtp(e: React.FormEvent) {
    e.preventDefault()
    setError('')

    const identifier = method === 'phone' ? phone.trim() : email.trim()

    if (method === 'phone') {
      if (!/^\+?[0-9]{10,15}$/.test(identifier)) {
        setError('Enter a 10-15 digit phone number.')
        return
      }
    } else {
      if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(identifier)) {
        setError('Please enter a valid email address.')
        return
      }
    }

    setBusy(true)
    try {
      await requestOtp(identifier)
      setStep('otp')
    } catch (err) {
      if (err instanceof ApiError) {
        if (err.status === 429) {
          setError('Too many attempts. Please wait before requesting another code.')
        } else if (err.status === 500) {
          setError('Server error. Please try again later.')
        } else if (err.status >= 400 && err.status < 500) {
          setError(err.message || 'Failed to send code. Please try again.')
        } else {
          setError('Could not request the code. Try again.')
        }
      } else {
        setError('Could not request the code. Try again.')
      }
    } finally {
      setBusy(false)
    }
  }

  async function handlePasswordLogin(e: React.FormEvent) {
    e.preventDefault()
    setError('')

    const trimmedEmail = email.trim()
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(trimmedEmail)) {
      setError('Please enter a valid email address.')
      return
    }

    if (!password) {
      setError('Please enter your password.')
      return
    }

    setBusy(true)
    try {
      await login(trimmedEmail, password)
      navigate('/')
    } catch (err) {
      if (err instanceof ApiError) {
        if (err.status === 401) {
          setError('Invalid email or password.')
        } else if (err.status === 429) {
          setError('Too many attempts. Please wait before trying again.')
        } else if (err.status === 500) {
          setError('Server error. Please try again later.')
        } else {
          setError(err.message || 'Could not sign in. Try again.')
        }
      } else {
        setError('Could not sign in. Try again.')
      }
      setBusy(false)
    }
  }

  async function handleVerify(e: React.FormEvent) {
    e.preventDefault()
    setError('')

    const code = otp.trim()
    if (code.length < 4 || code.length > 8) {
      setError('Enter the code from the message.')
      return
    }

    const identifier = method === 'email' ? email.trim() : phone.trim()

    setBusy(true)
    try {
      await verifyOtp(
        identifier,
        code,
        name.trim() || undefined,
        organisationName.trim() || undefined,
      )
      navigate('/')
    } catch (err) {
      if (err instanceof ApiError) {
        if (err.status === 401) {
          setError('That code is not right. Check it and try again.')
        } else if (err.status === 429) {
          setError('Too many attempts. Please wait before requesting another code.')
        } else if (err.status === 500) {
          setError('Server error. Please try again later.')
        } else {
          setError(err.message || 'Could not sign in. Try again.')
        }
      } else {
        setError('Could not sign in. Try again.')
      }
      setBusy(false)
    }
  }

  function renderMethodTabs() {
    return (
      <div className="mb-6 grid grid-cols-3 gap-2" role="tablist" aria-label="Login method">
        <button
          role="tab"
          aria-selected={method === 'phone'}
          onClick={() => { setMethod('phone'); setStep('input'); setError(''); setOtp(''); }}
          className={`px-3 py-2 rounded-lg text-sm font-medium transition-all ${
            method === 'phone'
              ? 'bg-brand-600 text-white shadow-sm'
              : 'text-ink-600 hover:bg-ink-100'
          }`}
        >
          <div className="flex items-center justify-center gap-2">
            <Phone className="h-4 w-4" aria-hidden />
            <span>Phone</span>
          </div>
        </button>
        <button
          role="tab"
          aria-selected={method === 'email'}
          onClick={() => { setMethod('email'); setStep('input'); setError(''); setOtp(''); }}
          className={`px-3 py-2 rounded-lg text-sm font-medium transition-all ${
            method === 'email'
              ? 'bg-brand-600 text-white shadow-sm'
              : 'text-ink-600 hover:bg-ink-100'
          }`}
        >
          <div className="flex items-center justify-center gap-2">
            <Mail className="h-4 w-4" aria-hidden />
            <span>Email OTP</span>
          </div>
        </button>
        <button
          role="tab"
          aria-selected={method === 'password'}
          onClick={() => { setMethod('password'); setStep('input'); setError(''); }}
          className={`px-3 py-2 rounded-lg text-sm font-medium transition-all ${
            method === 'password'
              ? 'bg-brand-600 text-white shadow-sm'
              : 'text-ink-600 hover:bg-ink-100'
          }`}
        >
          <div className="flex items-center justify-center gap-2">
            <Lock className="h-4 w-4" aria-hidden />
            <span>Password</span>
          </div>
        </button>
      </div>
    )
  }

  function renderPhoneInput() {
    return (
      <form onSubmit={handleRequestOtp} className="space-y-4">
        <Field label="Phone number" htmlFor="phone">
          <input
            id="phone"
            type="tel"
            inputMode="tel"
            autoComplete="tel"
            autoFocus
            className="input tnum"
            placeholder="9876543210"
            value={phone}
            onChange={(e) => setPhone(e.target.value)}
          />
        </Field>

        {error && <ErrorText>{error}</ErrorText>}

        <Button type="submit" variant="primary" disabled={busy} fullWidth>
          {busy ? 'Sending…' : 'Send code'}
        </Button>
      </form>
    )
  }

  function renderEmailInput() {
    return (
      <form onSubmit={handleRequestOtp} className="space-y-4">
        <Field label="Email address" htmlFor="email">
          <input
            id="email"
            type="email"
            inputMode="email"
            autoComplete="email"
            autoFocus
            className="input tnum"
            placeholder="john@example.com"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
        </Field>

        {error && <ErrorText>{error}</ErrorText>}

        <Button type="submit" variant="primary" disabled={busy} fullWidth>
          {busy ? 'Sending…' : 'Send code'}
        </Button>
      </form>
    )
  }

  function renderPasswordForm() {
    return (
      <form onSubmit={handlePasswordLogin} className="space-y-4">
        <Field label="Email address" htmlFor="email">
          <div className="relative">
            <Mail className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-ink-400" />
            <input
              id="email"
              type="email"
              className="input tnum pl-10"
              placeholder="john@example.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
          </div>
        </Field>

        <Field label="Password" htmlFor="password">
          <div className="relative">
            <Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-ink-400" />
            <input
              id="password"
              type={showPassword ? 'text' : 'password'}
              className="input tnum pl-10 pr-10"
              placeholder="Your password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
            <button
              type="button"
              onClick={() => setShowPassword(!showPassword)}
              className="absolute right-3 top-1/2 transform -translate-y-1/2 text-ink-400 hover:text-ink-600"
              aria-label={showPassword ? 'Hide password' : 'Show password'}
            >
              {showPassword ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}
            </button>
          </div>
        </Field>

        {error && <ErrorText>{error}</ErrorText>}

        <Button type="submit" variant="primary" disabled={busy} fullWidth>
          {busy ? 'Signing in…' : 'Sign in'}
        </Button>

        <div className="text-center text-sm">
          <a href="#" className="text-brand-600 hover:text-brand-700">Forgot password?</a>
        </div>
      </form>
    )
  }

  function renderOtpForm() {
    return (
      <form onSubmit={handleVerify} className="space-y-4">
        <Field
          label="Verification code"
          htmlFor="otp"
          hint={method === 'email'
            ? 'We sent you a 6-digit code over email. It expires in 5 minutes.'
            : 'We sent you a 6-digit code over SMS. It expires in 5 minutes.'
          }
        >
          <input
            id="otp"
            type="text"
            inputMode="numeric"
            autoComplete="one-time-code"
            autoFocus
            className="input tnum text-center text-lg tracking-[0.3em]"
            placeholder="••••••"
            maxLength={8}
            value={otp}
            onChange={(e) => setOtp(e.target.value)}
          />
        </Field>

        {error && <ErrorText>{error}</ErrorText>}

        <details className="rounded-lg border border-ink-200 bg-ink-50 px-3 py-2">
          <summary className="cursor-pointer text-sm font-medium text-ink-700">
            First time here?
          </summary>
          <div className="mt-3 space-y-3">
            <Field label="Your name" htmlFor="name">
              <input
                id="name"
                type="text"
                className="input"
                placeholder="Owner"
                value={name}
                onChange={(e) => setName(e.target.value)}
              />
            </Field>
            <Field label="Organisation name" htmlFor="org">
              <input
                id="org"
                type="text"
                className="input"
                placeholder="Sharma Transport"
                value={organisationName}
                onChange={(e) => setOrganisationName(e.target.value)}
              />
            </Field>
            <p className="text-xs text-ink-500">
              Only needed if this email/phone number is not registered yet.
            </p>
          </div>
        </details>

        <Button type="submit" variant="primary" disabled={busy} fullWidth>
          {busy ? 'Signing in…' : 'Sign in'}
        </Button>

        <button
          type="button"
          onClick={() => {
            setStep('input')
            setOtp('')
            setError('')
          }}
          className="btn-ghost w-full"
        >
          <ArrowLeft className="h-4 w-4" aria-hidden />
          Change {method === 'email' ? 'email' : 'number'}
        </button>
      </form>
    )
  }

  return (
    <div className="flex min-h-dvh items-center justify-center bg-ink-100 px-4 py-10">
      <div className="w-full max-w-sm">
        <div className="mb-8 flex flex-col items-center text-center">
          <div className="rounded-2xl bg-brand-600 p-3 text-white shadow-sm">
            <Truck className="h-7 w-7" aria-hidden />
          </div>
          <h1 className="mt-4 text-2xl font-semibold tracking-tight text-ink-900">Yatrik</h1>
          <p className="mt-1 text-sm text-ink-500">
            Fleet management for transport operators
          </p>
        </div>

        <div className="card p-6">
          {renderMethodTabs()}

          {step === 'input' ? (
            method === 'phone' ? renderPhoneInput() :
            method === 'email' ? renderEmailInput() :
            renderPasswordForm()
          ) : (
            renderOtpForm()
          )}

          <p className="mt-6 text-center text-sm text-ink-600">
            Don&apos;t have an account?{' '}
            <Link to="/signup" className="text-brand-600 hover:text-brand-700 font-medium">
              Sign up
            </Link>
          </p>
        </div>

        <p className="mt-6 text-center text-xs text-ink-500">
          One organisation, one ledger. Your data stays in your organisation.
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