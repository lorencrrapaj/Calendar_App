// tailwind.config.js
module.exports = {
    content: [
        "./index.html",
        "./src/**/*.{js,ts,jsx,tsx}"
    ],
    theme: {
        extend: {
            colors: { "bg-deep": "#0b1c2c" },
        },
    },
    plugins: [ require('@tailwindcss/forms') ],
}
