# UI Theme System

This directory contains the complete theme system for the OpenAPI Developer Example App, implementing the design language specified in `DESIGN_LANGUAGE.md`.

## Structure

```
ui/
├── theme/
│   ├── Color.kt      # Color palette definitions
│   ├── Type.kt       # Typography system
│   ├── Shape.kt      # Shape definitions
│   └── Theme.kt      # Main theme composable
└── components/
    └── Components.kt # Reusable UI components
```

## Usage

### Applying the Theme

Wrap your composable content with `AppTheme`:

```kotlin
setContent {
    AppTheme {
        // Your content here
    }
}
```

### Using Components

#### Primary Button
```kotlin
PrimaryButton(
    text = "Click Me",
    onClick = { /* action */ },
    modifier = Modifier.fillMaxWidth(0.8f),
    enabled = true
)
```

#### Section Header
```kotlin
SectionHeader(text = "My Screen Title")
```

#### Body Text
```kotlin
BodyText(
    text = "This is some explanatory text that provides context.",
    textAlign = TextAlign.Center
)
```

#### App Screen Container
```kotlin
AppScreen {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Your screen content
    }
}
```

#### Card Component
```kotlin
AppCard {
    Text("Card content")
    Spacer(modifier = Modifier.height(8.dp))
    Text("More content")
}
```

## Accessing Theme Values

### Colors
```kotlin
// Access via MaterialTheme
val primary = MaterialTheme.colorScheme.primary
val background = MaterialTheme.colorScheme.background
val onSurface = MaterialTheme.colorScheme.onSurface
```

### Typography
```kotlin
Text(
    text = "Heading",
    style = MaterialTheme.typography.displaySmall
)

Text(
    text = "Body text",
    style = MaterialTheme.typography.bodyLarge
)
```

### Shapes
```kotlin
Box(
    modifier = Modifier
        .clip(MaterialTheme.shapes.large)
        .background(MaterialTheme.colorScheme.surface)
)
```

## Design Tokens

### Colors
- **Primary**: #4A90E2 (Cool blue)
- **Background**: #0F0F0F (Deep black-gray)
- **Surface**: #222222 (Dark gray)
- **Text Primary**: #FFFFFF (White)
- **Text Secondary**: #E5E5E5 (Light gray)
- **Text Muted**: #888888 (Mid gray)

### Typography Scale
- **Display Large**: 48sp, Medium (500)
- **Display Small**: 32sp, Medium (500)
- **Body Large**: 18sp, Regular (400)
- **Body Medium**: 16sp, Regular (400)
- **Label Large**: 16sp, Medium (500)

### Spacing Scale
- **Extra Small**: 4dp
- **Small**: 8dp
- **Medium**: 16dp
- **Large**: 24dp
- **Extra Large**: 32dp

### Corner Radius
- **Small**: 8dp
- **Medium**: 12dp
- **Large**: 16dp
- **Extra Large**: 24dp
- **Full**: 50% (pill-shaped)

## Animation Specifications

### Timing
- **Fast**: 200ms (button presses, micro-interactions)
- **Medium**: 350ms (screen transitions, modal appearances)
- **Slow**: 500ms (large content changes)

### Easing
- Use `tween()` for most animations
- All animations are smooth and consistent

## Best Practices

### Do's ✅
- Always use AppTheme as the root wrapper
- Use provided components for consistency
- Stick to the defined color palette
- Follow the spacing scale
- Maintain proper visual hierarchy
- Test animations on real devices

### Don'ts ❌
- Don't hardcode colors - use theme values
- Don't create custom font sizes outside the scale
- Don't use inconsistent corner radius values
- Don't skip animations for user interactions
- Don't use MaterialTheme directly (use AppTheme)

## Customization

### Adding New Colors
1. Add color definition in `Color.kt`
2. Map it to the color scheme in `Theme.kt`
3. Document the usage in this README

### Adding New Components
1. Create the component in `Components.kt`
2. Follow existing patterns for consistency
3. Include animation where appropriate
4. Document usage with examples

### Modifying Typography
1. Update values in `Type.kt`
2. Ensure hierarchy is maintained
3. Test across all screens

## Accessibility

- Minimum contrast ratio: 4.5:1 for body text
- Minimum touch target: 48dp
- All interactive elements have visual feedback
- Text is readable at default system font sizes

## Resources

- [Material Design 3](https://m3.material.io/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Design Language Documentation](../DESIGN_LANGUAGE.md)
- [Implementation Summary](../DESIGN_IMPLEMENTATION.md)

