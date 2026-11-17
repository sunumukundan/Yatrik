/**
 * The session.
 *
 * The access token and the refresh token live in localStorage so a page reload does
 * not sign the operator out mid-shift. That is the right trade-off for a dashboard
 * on a shared office computer — the alternative (a cookie with HttpOnly) needs a
 * server to set it, and this dashboard talks to the gateway directly.
 *
 * The organisation and the role are kept in memory alongside the tokens, because
 * every screen wants them and re-decoding a JWT for the nav bar is wasteful.
 */

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { api, onUnauthorized, tokens } from '../lib/api'
import type { AuthResponse, UserRole } from '../lib/types'

interface Session {
  userId: string
  organisationId: string
  role: UserRole
}

interface AuthState {
  session: Session | null
  isAuthenticated: boolean
  isLoading: boolean
  /** Sends an OTP to a phone number or email. Always resolves — the API never says whether an identifier is known. */
  requestOtp: (identifier: string) => Promise<void>
  /** Exchanges the OTP for tokens. Throws ApiError on a wrong code. */
  verifyOtp: (identifier: string, otp: string, name?: string, organisationName?: string) => Promise<void>
  /** Email/password login */
  login: (email: string, password: string) => Promise<void>
  /** Email/password signup */
  signup: (email: string, password: string, phone: string) => Promise<void>
  logout: () => Promise<void>
}

const AuthContext = createContext<AuthState | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<Session | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  /**
   * Restores a session from the stored tokens on first load.
   *
   * The role and the organisation id are read from the token's claims rather than
   * from a separate call — the JWT is already in hand and it carries both, and the
   * access token is only 30 minutes old so a stale role is corrected on the next
   * refresh anyway.
   */
  useEffect(() => {
    // In dev, clear stale tokens to avoid blank page from invalid auth state
    if (import.meta.env.DEV) {
      tokens.clear()
    }

    const stored = tokens.access
    if (!stored) {
      setIsLoading(false)
      return
    }

    const claims = decodeJwt(stored)
    if (!claims) {
      tokens.clear()
      setIsLoading(false)
      return
    }

    setSession({
      userId: claims.sub ?? '',
      organisationId: claims.org ?? '',
      role: (claims.role as UserRole) ?? 'OWNER',
    })
    setIsLoading(false)
  }, [])

  /** The session is over — drop it and let the router send the operator to login. */
  useEffect(() => {
    return onUnauthorized(() => setSession(null))
  }, [])

  const requestOtp = useCallback(async (identifier: string) => {
    const body: Record<string, string> = {}
    if (identifier.includes('@')) {
      body.email = identifier
    } else {
      body.phone = identifier
    }
    await api.post('/auth/otp/request', body, { anonymous: true })
  }, [])

  const verifyOtp = useCallback(
    async (identifier: string, otp: string, name?: string, organisationName?: string) => {
      const body: Record<string, string> = { otp }
      // Send identifier in the correct field based on format
      if (identifier.includes('@')) {
        body.email = identifier
      } else {
        body.phone = identifier
      }
      // A new identifier gets a new organisation and an OWNER account
      if (name) body.name = name
      if (organisationName) body.organisationName = organisationName

      // Add timeout to prevent hanging
      const res = await Promise.race([
        api.post<AuthResponse>('/auth/otp/verify', body, { anonymous: true }),
        new Promise<never>((_, reject) => setTimeout(() => reject(new Error('Request timed out')), 30000))
      ])
      tokens.set(res.accessToken, res.refreshToken)
      setSession({
        userId: res.userId,
        organisationId: res.organisationId,
        role: res.role,
      })
    },
    [],
  )

  const logout = useCallback(async () => {
    const refreshToken = tokens.refresh
    if (refreshToken) {
      try {
        // Always 204 server-side, whether or not the token was known.
        await api.post('/auth/logout', { refreshToken }, { anonymous: true })
      } catch {
        // A failed logout must not trap the operator in the app.
      }
    }
    tokens.clear()
    setSession(null)
  }, [])

  const login = useCallback(async (email: string, password: string) => {
    const res = await api.post<AuthResponse>('/auth/login', { email, password }, { anonymous: true })
    tokens.set(res.accessToken, res.refreshToken)
    setSession({
      userId: res.userId,
      organisationId: res.organisationId,
      role: res.role,
    })
  }, [])

  const signup = useCallback(async (email: string, password: string, phone: string) => {
    const res = await api.post<AuthResponse>('/auth/signup', { email, password, phone }, { anonymous: true })
    tokens.set(res.accessToken, res.refreshToken)
    setSession({
      userId: res.userId,
      organisationId: res.organisationId,
      role: res.role,
    })
  }, [])

  const value = useMemo<AuthState>(
    () => ({
      session,
      isAuthenticated: session !== null,
      isLoading,
      requestOtp,
      verifyOtp,
      login,
      signup,
      logout,
    }),
    [session, isLoading, requestOtp, verifyOtp, login, signup, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthState {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used inside an AuthProvider')
  return ctx
}

interface Claims {
  sub?: string
  org?: string
  role?: string
  exp?: number
}

/**
 * Reads the claims out of a JWT.
 *
 * This is a decode, not a verification — the signature is checked by every service
 * that matters. The dashboard only needs the org id and the role to draw the nav
 * bar, and it must not treat the result as authoritative for anything else.
 */
function decodeJwt(token: string): Claims | null {
  const parts = token.split('.')
  if (parts.length !== 3) return null
  try {
    const payload = parts[1].replace(/-/g, '+').replace(/_/g, '/')
    const json = decodeURIComponent(
      atob(payload)
        .split('')
        .map((c) => `%${`00${c.charCodeAt(0).toString(16)}`.slice(-2)}`)
        .join(''),
    )
    return JSON.parse(json) as Claims
  } catch {
    return null
  }
}
