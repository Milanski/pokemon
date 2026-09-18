import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { PokemonCard } from './PokemonCard';

const pikachu = { id: 25, name: 'pikachu', spriteUrl: 'http://sprite/25.png' };

describe('PokemonCard', () => {
  it('shows an "Add to collection" button when not owned', () => {
    render(<PokemonCard pokemon={pikachu} owned={false} onAdd={vi.fn()} onRemove={vi.fn()} busy={false} />);

    expect(screen.getByText('#025')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /add to collection/i })).toBeInTheDocument();
  });

  it('shows a "Remove" button when owned', () => {
    render(<PokemonCard pokemon={pikachu} owned onAdd={vi.fn()} onRemove={vi.fn()} busy={false} />);

    expect(screen.getByRole('button', { name: /remove/i })).toBeInTheDocument();
  });

  it('calls onAdd with the pokemon id when clicked', async () => {
    const onAdd = vi.fn();
    render(<PokemonCard pokemon={pikachu} owned={false} onAdd={onAdd} onRemove={vi.fn()} busy={false} />);

    await userEvent.click(screen.getByRole('button', { name: /add to collection/i }));

    expect(onAdd).toHaveBeenCalledWith(25);
  });

  it('disables the button while busy', () => {
    render(<PokemonCard pokemon={pikachu} owned={false} onAdd={vi.fn()} onRemove={vi.fn()} busy />);

    expect(screen.getByRole('button', { name: /add to collection/i })).toBeDisabled();
  });
});
