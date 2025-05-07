# 🥗 Green & Tasty
 
**Green & Tasty** is a modern restaurant web application built using **React**, **TypeScript**, and **Vite**. The app allows users to manage thier account,browse menus and different locations of the restaurant, make reservations, and manage their reservations. It also includes features for waiters to handle bookings.
 
Styled using [**shadcn/ui**](https://ui.shadcn.com/), it follows modular structure and best practices for scalability and maintainability.
 
---
 
## ⚙️ Tech Stack
 
- 📖 React 18
- 📦 Vite
- 🧾 TypeScript
- 🎨 ShadCN UI (Radix + Tailwind)
- 🧪 Vitest (Unit Testing)
- 🛠️ ESLint + Prettier
- 📁 Feature-Based File Structure
 
---
 
## 📁 Project Structure
 
```
src/
│
├── __tests__/            # Unit and integration tests      
├── assets/               # Images and static assets
├── components/           # Reusable UI components
├── constants/            # App-wide constants
├── hooks/                # Reusable custom hooks
├── lib/                  # Utility and helper functions
├── pages/                # Page-level components
├── services/             # API call handlers
├── routes/               # App routes configuration
├── types/                # TypeScript type definitions
├── App.tsx               # Root React component
├── main.tsx              # Entry point
├── vite-env.d.ts         # Vite type declarations
└── setupTests.ts         # Vitest setup file
                    
public/                   # Public assets
README.md                 # This file
```
 
---

## External Resources

- Code for the app is present in Gitlab at develop_frontend branch, link for the code base is 
"https://git.epam.com/epm-edai/project-runs/run-8/team-7/serverless/restaurant-app/-/tree/develop_frontend"
- Information about the application is at knowledge base (https://kb.epam.com/display/EPMEDAI/Student+Hub)

---

## Git strategy

- Git strategy used here is GitFlow.
Git Flow is a branching strategy for Git developed by Vincent Driessen. It defines a strict model of how branches should be structured to manage features, releases, and hotfixes effectively in larger or more structured software projects.

🌳 Main Branches in Git Flow
- main / master: 
Always contains production-ready code.
Only updated via completed releases or hotfixes.

- develop:
Contains the latest development code.
New features are merged here and tested before going to main.

🔀 Supporting Branches
- feature/*
For new features or tasks.
Branches from develop, merges back to develop.
Naming: feature/login-page, feature/api-integration, etc.

- release/*
Prepares a new version for production.
Branches from develop, merges to both main and develop.
Used for last-minute polishing like documentation or bug fixes.

- hotfix/*
For urgent fixes to production.
Branches from main, merges to both main and develop.

---
 
## 🚀 Getting Started
 
### 1. **Clone the Repository**
 
```bash
- git clone https://git.epam.com/epm-edai/project-runs/run-8/team-7/serverless/restaurant-app.git (using 'HTTPS')
'OR'
- git clone git@git.epam.com:epm-edai/project-runs/run-8/team-7/serverless/restaurant-app.git (using 'SSH')
cd frontend
cd restaurant-app
```
 
### 2. **Install Dependencies**
 
```bash
npm install
```
 
### 3. **Set Environment Variables**
 
Create a `.env` file in the root directory and add:
 
```env
VITE_API_URL=https://your-api-url.com
```
 
### 4. **Run the Development Server**
 
```bash
npm run dev
```
 
Your app will be available at `http://localhost:5173`.
 
---
 
## 🧪 Running Tests
 
This project uses [**Vitest**](https://vitest.dev/) for unit testing.
 
```bash
npm run test
```
 
Vitest config is located in `vitest.config.ts`. Setup and globals are handled in `setupTests.ts`.
 
---
 
## 🧼 Code Quality
 
### ✅ Linting
 
Run ESLint to check code quality:
 
```bash
npm run lint
```
 
### 🧹 Formatting
 
To auto-format with Prettier:
 
```bash
npm run format
```
 
---
 
## 🧩 ShadCN UI Integration
 
This project uses [ShadCN](https://ui.shadcn.com/) components built on:
 
- Tailwind CSS
- Radix UI
- Lucide Icons
 
To create new components:
 
```bash
npx shadcn-ui@latest add component-name
```
 
Component styles are customizable through the `tailwind.config.ts`.
 
---
 
## 📦 Build for Production
 
```bash
npm run build
```
 
The production-ready output will be in the `dist/` folder.
 
To preview the build locally:
 
```bash
npm run preview
```
 
---
 
## 🌐 Deployment
 
This app isdeployed in:

- [AWS S3]
The app link after deployment in s3 bucket is "http://team-7-frontend-bucket.s3-website-ap-southeast-2.amazonaws.com"
 
> Note: Make sure your environment variables are configured in your hosting platform.

---
 
## 🧾 License
 
This project is licensed under the MIT License.
 
---
 
## 📌 Author
 
**Green & Tasty** – built with ❤️ by your team.
Build your component library - shadcn/ui
A set of beautifully-designed, accessible components and a code distribution platform. Works with your favorite frameworks. Open Source. Open Code.
 