// app/layout.tsx
import type { Metadata, Viewport } from 'next'
import { Inter } from 'next/font/google'
import './globals.css'

const inter = Inter({ subsets: ['latin'] })

export const metadata: Metadata = {
  title: { default: 'Storefront', template: '%s · Storefront' },
  description: 'Data Structures demo storefront',
  icons: { icon: '/favicon.ico' },
}

export const viewport: Viewport = {
  themeColor: [
    { media: '(prefers-color-scheme: light)', color: '#ffffff' },
    { media: '(prefers-color-scheme: dark)', color: '#0b1220' },
  ],
}

export default function RootLayout({
  children,
}: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="en" className="h-full">
      <body className={`${inter.className} min-h-screen bg-gray-50 text-gray-900 antialiased`}>
        {/* Accessible skip link */}
        <a
          href="#content"
          className="sr-only focus:not-sr-only focus:fixed focus:top-4 focus:left-4 focus:z-50 rounded-lg bg-white px-3 py-2 shadow"
        >
          Skip to content
        </a>

        {/* Global container */}
        <div id="content" className="mx-auto max-w-5xl px-4">
          {children}
        </div>

        {/* Global footer */}
        <footer className="mt-16 border-t py-8 text-sm text-gray-500">
          <div className="mx-auto max-w-5xl px-4">
            © {new Date().getFullYear()} Storefront
          </div>
        </footer>

        {/*
          If you have global providers (e.g., ThemeProvider, QueryClientProvider),
          wrap {children} with a <Providers> component here.
        */}
      </body>
    </html>
  )
}
