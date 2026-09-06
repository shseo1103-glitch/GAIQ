/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        // Material Design 3 dark-theme inspired surface scale.
        // Light theme keeps neutral gray/blue tones (High-Performance HMI: calm background).
        surface: {
          light: '#f5f6f8',
          DEFAULT: '#ffffff',
          dark: '#121212', // md3 base dark surface (not pure black)
        },
        elevation: {
          0: '#121212',
          1: '#1e1e1e',
          2: '#222222',
          3: '#242424',
          4: '#272727',
          5: '#2c2c2c',
        },
        brand: {
          cyan: '#22d3ee',
          cyanDark: '#0891b2',
        },
        status: {
          pass: '#16a34a',
          passDark: '#4ade8a',
          fail: '#dc2626',
          failDark: '#f87171',
          warn: '#d97706',
          warnDark: '#fbbf74',
          info: '#2563eb',
          infoDark: '#60a5fa',
        },
      },
    },
  },
  plugins: [],
}
