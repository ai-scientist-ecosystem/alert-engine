import type { Metadata } from 'next'

export const metadata: Metadata = {
  title: 'AI Scientist Ecosystem',
  description: 'Automated research & workflow platform',
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  )
}
