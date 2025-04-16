import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { GameEventRecordingPage } from "./pages/GameEventRecordingPage";
import "./App.css";

// Create a client for React Query
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: false,
    },
  },
});

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <GameEventRecordingPage />
    </QueryClientProvider>
  );
}

export default App;
