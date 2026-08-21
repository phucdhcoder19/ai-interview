/**
 * Lớp gọi API dùng chung. Backend luôn trả về hình dạng {code, message, data}
 * (class Result<T> phía Java) — code khác 0 nghĩa là lỗi, ném ra kèm message
 * để component hiển thị thẳng cho người dùng.
 */
export interface Result<T> {
  code: number
  message: string
  data: T
}

export class ApiError extends Error {
  readonly code: number

  constructor(message: string, code: number) {
    super(message)
    this.code = code
  }
}

async function parse<T>(response: Response): Promise<T> {
  const body = (await response.json().catch(() => null)) as Result<T> | null
  if (body === null) {
    throw new ApiError(`Máy chủ trả về dữ liệu không đọc được (HTTP ${response.status})`, response.status)
  }
  if (body.code !== 0) {
    throw new ApiError(body.message, body.code)
  }
  return body.data
}

export function get<T>(url: string): Promise<T> {
  return fetch(url).then((r) => parse<T>(r))
}

export function post<T>(url: string, body?: unknown): Promise<T> {
  return fetch(url, {
    method: 'POST',
    headers: body === undefined ? undefined : { 'Content-Type': 'application/json' },
    body: body === undefined ? undefined : JSON.stringify(body),
  }).then((r) => parse<T>(r))
}
