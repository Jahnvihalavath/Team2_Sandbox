import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { ThemeProvider } from '@context/ThemeContext.jsx';
import * as Auth from '@context/AuthContext.jsx';
import Dashboard from './Dashboard.jsx';

vi.mock('@hooks/useTradeStream.js', () => ({
  useTradeStream: () => ({
    trades: [
      {
        quantity: 100,
        price: 250,
        status: 'MATCHED',
      },
      {
        quantity: 50,
        price: 251,
        status: 'UNMATCHED',
      },
    ],
    isConnected: true,
  }),
}));

vi.spyOn(Auth, 'useAuth').mockReturnValue({
  user: {
    token: 'fake-token',
    role: 'TRADER',
  },
  login: vi.fn(),
  logout: vi.fn(),
});

function renderWithProviders(ui) {
  return render(
    <ThemeProvider>
      <MemoryRouter>
        {ui}
      </MemoryRouter>
    </ThemeProvider>
  );
}

describe('<Dashboard />', () => {
  it('shows summary cards', () => {
    renderWithProviders(<Dashboard />);

    expect(
      screen.getByRole('heading', {
        name: /portfolio value/i,
      })
    ).toBeInTheDocument();

    expect(
      screen.getByRole('heading', {
        name: /trades streamed/i,
      })
    ).toBeInTheDocument();

    expect(
      screen.getByRole('heading', {
        name: /matched/i,
      })
    ).toBeInTheDocument();

    expect(
      screen.getByRole('heading', {
        name: /open breaks/i,
      })
    ).toBeInTheDocument();

    // 100 × 250 + 50 × 251 = 37,550
    expect(screen.getByText(/37,550/)).toBeInTheDocument();
  });
});