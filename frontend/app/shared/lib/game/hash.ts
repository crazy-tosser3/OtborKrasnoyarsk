// Provably-fair подпись результата мини-игры: SHA-256(session_id + score + secret).
// secret приходит с /api/minigame/start и не покидает сессию до завершения.
export async function buildGameHash(
  sessionId: string,
  score: number,
  secret: string,
): Promise<string> {
  const data = sessionId + String(score) + secret
  const hashBuffer = await crypto.subtle.digest('SHA-256', new TextEncoder().encode(data))
  return Array.from(new Uint8Array(hashBuffer))
    .map((byte) => byte.toString(16).padStart(2, '0'))
    .join('')
}
