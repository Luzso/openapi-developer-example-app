# Design Language

## Vision
A minimalist, futuristic interface that feels intuitive and sophisticated—like technology from a slightly advanced human civilization. Clean, purposeful, and effortlessly modern.

## Core Principles

### 1. Minimalism
- Remove all unnecessary elements
- Every component serves a clear purpose
- Embrace whitespace and breathing room
- Information hierarchy through scale and weight, not decoration

### 2. Clarity
- High contrast for critical information
- Subtle, muted tones for secondary elements
- Clear visual feedback for all interactions
- Typography prioritizes readability

### 3. Sophistication
- Smooth, purposeful animations (200-400ms)
- Subtle depth through elevation, not heavy shadows
- Refined spacing using 8px grid system
- Consistent rounded corners (8-16px)

## Visual System

### Color Palette
- **Background**: Deep neutral grays (#0F0F0F, #1A1A1A)
- **Surface**: Elevated panels (#222222, #2A2A2A)
- **Primary**: Cool blue accent (#4A90E2, #5BA3FF)
- **Text**: High contrast white (#FFFFFF, #E5E5E5)
- **Muted**: Low contrast gray (#888888, #666666)
- **Success**: Soft cyan (#00D9FF)
- **Warning**: Amber glow (#FFB84D)

### Typography
- **Headings**: 32-48sp, medium weight (500)
- **Body**: 16-18sp, regular weight (400)
- **Buttons**: 14-16sp, medium weight (500)
- **Captions**: 12-14sp, regular weight (400)
- **Font**: System default (Roboto/SF Pro equivalent)

### Spacing
- Use multiples of 8: 8, 16, 24, 32, 48, 64
- Consistent padding: 16-24dp for containers
- Generous margins between sections: 32-48dp

### Components

#### Buttons
- Pill-shaped (fully rounded ends)
- Filled primary buttons with subtle glow on hover
- Minimal borders, focus on solid fills
- Disabled state: 40% opacity

#### Cards/Surfaces
- Subtle elevation (4-8dp)
- Rounded corners (12-16dp)
- Thin border (0.5-1dp) in darker shade for definition
- No heavy drop shadows

#### Icons
- Outline style preferred
- Consistent stroke width
- 24dp standard size
- Optical alignment over mathematical centering

## Animation
- **Micro-interactions**: 200ms ease-out
- **Transitions**: 300ms ease-in-out
- **Loading states**: Subtle pulse or shimmer
- **Page transitions**: Smooth fade with slight scale (350ms)

## Interaction Patterns
- Immediate visual feedback on touch
- Haptic feedback for significant actions
- Progressive disclosure of complexity
- Clear loading and success states
- Graceful error handling with helpful guidance

## Philosophy
Less is more. Every pixel should earn its place. Technology should feel invisible yet powerful—present when needed, absent when not.

