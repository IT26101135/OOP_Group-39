// ============================================================
// MEMBER   : IT26101135 - [Name]
// MODULE   : Shared foundation
// FILE     : tailwind.config.js
// WHAT IT DOES (simple words): Sets the shared near-black palette, local font and spacing.
// REAL-LIFE EXAMPLE: The school uniform guide keeps everyone consistent.
// OOP USED : Not applicable (CSS build settings)
// CRUD     : Not applicable (presentation settings)
// ============================================================
/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./src/main/webapp/WEB-INF/views/**/*.{jsp,jspf}', './src/main/resources/static/js/**/*.js'],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        ink: '#050505', surface: '#0A0A0A', raised: '#171717',
        primary: { DEFAULT: '#F59E0B', hover: '#FBBF24' }, accent: { DEFAULT: '#F59E0B', hover: '#FBBF24' },
        muted: '#A3A3A3', secondary: '#A3A3A3',
        success: '#22C55E', warning: '#F59E0B', danger: '#EF4444', info: '#38BDF8'
      },
      fontFamily: {
        sans: ['DriveSpark Inter', 'Inter', 'system-ui', 'sans-serif'],
        display: ['DriveSpark Inter', 'Inter', 'system-ui', 'sans-serif']
      },
      boxShadow: { glow: 'none' },
      borderRadius: { '4xl': '2rem' }
    }
  },
  plugins: []
};
