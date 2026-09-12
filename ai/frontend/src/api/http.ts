import type { ApiResult } from '../types/chat'

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? ''

export class ApiError extends Error {
  readonly code?: number

  constructor(message: string, code?: number) {
    super(message)
    this.name = 'ApiError'
    this.code = code
  }
}

/**
 * 统一请求封装：后端复用公寓系统 common 的 Result，业务失败时 HTTP 仍是 200。
 */
export async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: { 'Content-Type': 'application/json', ...(init.headers ?? {}) },
    ...init,
  })
  const result = (await response.json()) as ApiResult<T>
  if (!result || result.code !== 200) {
    throw new ApiError(result?.message || `请求失败（HTTP ${response.status}）`, result?.code)
  }
  return result.data
}
