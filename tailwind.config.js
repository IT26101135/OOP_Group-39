// Module owner: IT26101135
/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./src/main/webapp/WEB-INF/views/**/*.{jsp,jspf}', './src/main/resources/static/js/**/*.js'],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        ink: '#0B1120', surface: '#111827', raised: '#1F2937',
        primary: { DEFAULT: '#6366F1', hover: '#818CF8' }, accent: { DEFAULT: '#F59E0B', hover: '#FBBF24' },
        muted: '#64748B', secondary: '#94A3B8',
        success: '#22C55E', warning: '#F59E0B', danger: '#EF4444', info: '#38BDF8'
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', '-apple-system', 'Segoe UI', 'sans-serif'],
        display: ['Poppins', 'Plus Jakarta Sans', 'Inter', 'system-ui', 'sans-serif']
      },
      boxShadow: { glow: '0 16px 45px rgba(99,102,241,.2)' },
      borderRadius: { '4xl': '2rem' }
    }
  },
  plugins: []
};
