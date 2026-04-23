/** @type {import('tailwindcss').Config} */
export default {
  // Tell Tailwind which files to scan for class names.
  // It removes any class you don't actually use from the final CSS bundle (tree-shaking).
  content: [
    "./index.html",
    "./src/**/*.{js,jsx}",  // scan all JS and JSX files inside src/
  ],
  theme: {
    extend: {},
  },
  plugins: [],
}
