export interface LoginPayload {
  username: string
  password: string
}

export interface LoginResult {
  token: string
  user: {
    id: number
    username: string
    nickname: string
    role: 'USER' | 'ADMIN'
  }
}
