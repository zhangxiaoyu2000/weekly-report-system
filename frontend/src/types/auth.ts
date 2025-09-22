export interface User {
  id: number
  username: string
  email: string
  fullName: string
  firstName?: string
  lastName?: string
  avatar?: string
  role: 'SUPER_ADMIN' | 'ADMIN' | 'MANAGER'
  department?: string
  employeeId?: string
  phone?: string
  position?: string
  createdAt: string
  updatedAt: string
}

export interface LoginRequest {
  username: string          // Frontend form field
  password: string
  rememberMe?: boolean
}

export interface BackendLoginRequest {
  usernameOrEmail: string   // Backend expected field  
  password: string
  rememberMe?: boolean
}

export interface RegisterRequest {
  username: string
  email: string
  password: string
  confirmPassword: string
  fullName: string
  firstName?: string
  lastName?: string
  department?: string
  role?: 'SUPER_ADMIN' | 'ADMIN' | 'MANAGER'
  employeeId?: string
  phone?: string
  position?: string
}

export interface AuthResponse {
  success: boolean
  message: string
  data: {
    user: User
    accessToken?: string    // Backend uses accessToken
    token?: string          // Fallback for compatibility
    refreshToken: string
    tokenType?: string      // Backend includes tokenType
    expiresIn?: number      // Backend uses expiresIn instead of expiresAt
    expiresAt?: number      // Fallback for compatibility
  }
}

export interface RefreshTokenRequest {
  refreshToken: string
}

export interface ChangePasswordRequest {
  currentPassword: string
  newPassword: string
  confirmPassword: string
}