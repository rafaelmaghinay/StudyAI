# StudyAI Webapp

A modern, user-friendly React application for creating and taking interactive quizzes powered by AI. Built with Vite, TypeScript, and React Router, featuring a complete authentication system.

## Features

- **Authentication**: Secure login/signup system with session management
- **Dashboard**: View and manage all your quizzes
- **Quiz Creation**: Create new quizzes with AI-powered question generation
- **Interactive Quiz Taking**: Take quizzes with a smooth, intuitive interface
- **Results & Review**: Get instant feedback with detailed answer reviews
- **Responsive Design**: Works seamlessly on desktop, tablet, and mobile devices
- **Protected Routes**: Automatic redirection for unauthenticated users

## Tech Stack

- **React 18** - UI framework
- **TypeScript** - Type safety
- **Vite** - Fast build tool
- **React Router** - Client-side routing
- **Axios** - HTTP client
- **CSS3** - Responsive styling
- **Context API** - State management for authentication

## Getting Started

### Prerequisites

- Node.js (v16 or higher)
- npm or yarn
- Backend API running on `http://localhost:8080` (serving REST endpoints under `/api`, e.g. `http://localhost:8080/api`)

### Installation

1. Install dependencies:
```bash
npm install
```

2. Start the development server:
```bash
npm run dev
```

The app will open at `http://localhost:5173`

### Authentication Flow

1. **First Visit**: New users see the login/signup page
2. **Sign Up**: Create an account with name, email, and password
3. **Login**: Existing users log in with credentials
4. **Dashboard**: Access the main hub after authentication
5. **Logout**: Logout from the user menu in the header

### Backend Integration

The webapp is configured to talk to a real backend API via an environment variable:

- `VITE_API_URL` – base URL for the backend API, **including** the `/api` prefix
  - Local example: `http://localhost:8080/api`
  - Deployed example (Railway): `https://celebrated-analysis-production.up.railway.app/api`

Auth, subjects, notes, and quizzes all use this single base URL so you don't need to change code when switching between local and hosted backends.

### Building for Production

```bash
npm run build
```

This creates an optimized production build in the `dist/` directory.

## Project Structure

```
src/
├── contexts/            # Application context & state
│   └── AuthContext.tsx  # Authentication state management
├── components/          # Reusable components
│   ├── Header.tsx       # Navigation header with user info
│   ├── Header.css
│   └── ProtectedRoute.tsx # Route protection wrapper
├── pages/              # Page components
│   ├── LoginPage.tsx         # Login/signup form
│   ├── Login.css
│   ├── DashboardPage.tsx     # (Protected) Quiz list
│   ├── Dashboard.css
│   ├── CreateQuizPage.tsx    # (Protected) Quiz creation
│   ├── CreateQuiz.css
│   ├── QuizPage.tsx          # (Protected) Quiz taking
│   └── Quiz.css
├── services/           # API service layer
│   └── api.ts          # Backend API client
├── App.tsx             # Main app component with routing
├── App.css
├── main.tsx            # Entry point with AuthProvider
├── index.css           # Global styles
└── vite.config.ts      # Vite configuration
```

## Authentication

### AuthContext

The `AuthContext` provides:
- User state management
- Login/signup functions
- Logout functionality
- Authentication state (`isAuthenticated`, `isLoading`)

### Protected Routes

All quiz-related pages are protected with `ProtectedRoute` component:
- Dashboard
- Create Quiz
- Take Quiz

Unauthenticated users are automatically redirected to `/login`

## API Endpoints

The webapp integrates with the StudyAI backend API:

- `POST /api/auth/login` - User login
- `POST /api/auth/signup` - User registration
- `POST /api/subjects` - Create subject
- `POST /api/quizzes` - Create quiz
- `GET /api/quizzes` - Get all quizzes
- `GET /api/quizzes/:id` - Get specific quiz
- `POST /api/quiz-attempts` - Submit quiz attempt
- `POST /api/quiz/generate` - Generate questions with AI

## Configuration

### Backend API URL

To use a different backend URL, set the `VITE_API_URL` environment variable instead of editing code.

Examples:

- Local development:
  - `VITE_API_URL=http://localhost:8080/api`
- Production (Railway backend):
  - `VITE_API_URL=https://celebrated-analysis-production.up.railway.app/api`

### Authentication Endpoints

`AuthContext` uses the same `VITE_API_URL` base and calls:

- `POST {VITE_API_URL}/auth/login`
- `POST {VITE_API_URL}/auth/signup`

No changes are required in the code when switching environments; updating `VITE_API_URL` is sufficient.

### Vite Configuration

The Vite config includes a proxy for API requests. Modify `vite.config.ts` to adjust the proxy settings.

## Development

### Available Scripts

- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm run preview` - Preview production build
- `npm run lint` - Run ESLint

### Environment Variables

Create a `.env` file in the root directory:

```
VITE_API_URL=http://localhost:8080/api
```

## User Experience Features

- **Progress Tracking**: Visual progress bars show quiz completion
- **Question Navigation**: Jump between questions using dot indicators
- **Auto-save**: Answers are saved as you progress
- **Instant Feedback**: View correct answers and explanations immediately
- **Score Calculation**: Detailed scoring with percentage and statistics
- **Responsive Layout**: Optimized for all screen sizes
- **Post-creation Redirect**: After creating a quiz, you are taken to the quizzes list page.
- **Collapsible Sidebar**: Sidebar shows icons-only when collapsed and expands on hover to reveal labels, user email, and logout button.

## Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

## Performance

- Lazy loading of components
- Optimized re-renders with React hooks
- Fast bundle with Vite
- CSS-in-JS optimizations

## Future Enhancements

- [ ] User authentication
- [ ] Quiz categories and filtering
- [ ] Performance tracking over time
- [ ] Leaderboards
- [ ] Admin dashboard
- [ ] Quiz sharing
- [ ] Offline mode

## Troubleshooting

### Backend not connecting
- Ensure Spring Boot API is running on `http://localhost:8080`
- Check CORS settings in the backend
- Verify proxy configuration in `vite.config.ts`

### Build errors
- Clear `node_modules` and `dist` folders
- Run `npm install` again
- Check Node.js version compatibility

## License

This project is part of the StudyAI suite.

## Support

For issues or questions, please refer to the main StudyAI documentation.
