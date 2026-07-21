# QR Forge — System Design Document

## Overview

QR Forge is a premium Android QR Code Generator built with Kotlin + Jetpack Compose.
Single-activity, offline-first, dark-mode-only architecture with MVVM + Clean Architecture.

## Technology Stack

| Layer | Choice |
|-------|--------|
| Language | Kotlin 1.9+ |
| UI | Jetpack Compose (Material 3) |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt |
| Navigation | Navigation Compose |
| Database | Room |
| QR Generation | QRCode-Kotlin |
| Image Loading | Coil |
| Export | Android Graphics + custom SVG writer |
| Min SDK | 26 (Android 8) |
| Target SDK | 34 |

## Architecture Layers

```
┌─────────────────────────────────────────┐
│  UI Layer (Compose Screens/Components)  │
├─────────────────────────────────────────┤
│  ViewModel Layer (StateFlow, events)    │
├─────────────────────────────────────────┤
│  Domain Layer (UseCases, Models)        │
├─────────────────────────────────────────┤
│  Data Layer (Repository, Room, ZXing)   │
└─────────────────────────────────────────┘
```

### Data Flow
```
Screen → ViewModel → UseCase → Repository → [Room | QRGenerator | FileSystem]
                         ↑
                    StateFlow<UiState>
```

## Navigation Graph

```
MainNavGraph
├── BottomNavBar
│   ├── Home (startDestination)
│   ├── Templates
│   ├── History
│   └── Settings
├── CreateFlow
│   ├── ChooseType
│   ├── EnterData (per type)
│   ├── Customize
│   └── Result (generated QR)
└── Detail Screens
    ├── QRDetail (from history)
    └── TemplatePreview
```

## Database Schema

### qr_history
| Column | Type | Description |
|--------|------|-------------|
| id | INTEGER PK AUTO | Unique ID |
| type | TEXT NOT NULL | QR type (URL, WIFI, etc.) |
| title | TEXT NOT NULL | User-facing title |
| content | TEXT NOT NULL | Encoded content |
| raw_data | TEXT NOT NULL | Original input JSON |
| foreground_color | TEXT | HEX color |
| background_color | TEXT | HEX color |
| dot_style | TEXT | rounded/square/circular/diamond |
| eye_shape | TEXT | rounded/circle/modern |
| logo_path | TEXT | Path to logo file |
| frame_style | TEXT | Frame style name |
| is_favorite | INTEGER | 0/1 |
| created_at | INTEGER | Unix timestamp |
| updated_at | INTEGER | Unix timestamp |

### qr_templates
| Column | Type | Description |
|--------|------|-------------|
| id | INTEGER PK AUTO | Unique ID |
| name | TEXT NOT NULL | Template name |
| category | TEXT NOT NULL | Business, Social, etc. |
| qr_type | TEXT NOT NULL | Default QR type |
| default_data | TEXT | Template default data |
| foreground_color | TEXT | |
| background_color | TEXT | |
| dot_style | TEXT | |
| eye_shape | TEXT | |
| frame_style | TEXT | |
| preview_icon | TEXT | Icon name |

### app_settings
| Column | Type | Description |
|--------|------|-------------|
| key | TEXT PK | Setting key |
| value | TEXT | Setting value |

## Color System

| Token | Value | Usage |
|-------|-------|-------|
| Background0 | #090909 | Deepest background |
| Background1 | #111111 | Primary background |
| Background2 | #171717 | Card backgrounds |
| Background3 | #1E1E1E | Elevated surfaces |
| SurfaceBorder | #2A2A2A | Subtle borders |
| TextPrimary | #FFFFFF | Primary text |
| TextSecondary | #AAAAAA | Secondary text |
| TextTertiary | #666666 | Muted text |
| Accent (Lime) | #A3E635 | Primary accent |
| AccentMuted | #4D7C0F | Muted accent |
| Error | #EF4444 | Error/destructive |

## Typography

| Level | Size | Weight | Usage |
|-------|------|--------|-------|
| Display | 32sp | Bold | Hero titles |
| Headline | 24sp | SemiBold | Screen titles |
| Title | 20sp | SemiBold | Card titles |
| Body | 16sp | Regular | Body text |
| BodySmall | 14sp | Regular | Secondary text |
| Caption | 12sp | Medium | Labels, hints |
| Overline | 10sp | Medium | Section headers |

## Component Library (Reusable)

- **QrButton** — Primary/secondary/outline/ghost variants
- **QrCard** — Elevated surface container
- **QrTypeChip** — QR type selection chip
- **QrTextField** — Styled text input
- **QrColorPicker** — Color selection component
- **QrDotStylePicker** — Dot style selector
- **QrEyeShapePicker** — Eye shape selector
- **QrPreview** — Live QR preview composable
- **QrProgressIndicator** — Top progress bar
- **QrBottomSheet** — Bottom sheet wrapper
- **QrIconButton** — Icon button with ripple
- **QrCategoryChip** — Filter chip
- **QrTemplateCard** — Template grid card
- **QrHistoryItem** — History list item
- **QrEmptyState** — Empty state placeholder
- **QrSearchBar** — Search input
- **QrSectionHeader** — Section title + action

## Screen Map

### Home
- Hero "Create New QR" card with gradient
- Quick action grid (URL, Text, WiFi, Contact, Email, SMS)
- Recent history horizontal list
- Templates horizontal list

### Templates
- Category chips (All, Business, Social, WiFi, Event, Personal)
- 2-column grid of template cards
- Tap to preview → apply

### History
- Search bar
- Sort options (Recent, Name, Type)
- List with favorite/delete/rename actions
- Empty state

### Settings
- Export quality
- Default colors
- Default style
- About section
- Privacy policy

### Create Flow
1. Choose Type — grid of QR type options with icons
2. Enter Data — type-specific form
3. Customize — live preview + style controls
4. Result — final QR with download/share

## States Per Screen

Every screen supports:
- **Loading** — skeleton/spinner
- **Content** — normal state
- **Empty** — descriptive empty state
- **Error** — error with retry
