# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Kotlin Multiplatform Compose application called "LM Client" - a cross-platform chat interface for language models. The app supports Android, iOS, and Desktop (JVM) platforms with a shared UI layer built on Compose Multiplatform.

The application provides a unified interface for interacting with multiple AI providers including OpenAI, Google, Anthropic, OpenRouter, Ollama, DeepSeek, and Nexos AI, with support for both streaming and non-streaming conversations.

## Project Structure

```
LM-Client/
├── shared/                    # Main multiplatform module (shared code)
│   └── src/
│       ├── commonMain/        # Shared Kotlin code for all platforms
│       ├── androidMain/       # Android-specific implementations
│       ├── jvmMain/          # Desktop-specific implementations
│       └── iosMain/          # iOS-specific implementations
├── androidApp/               # Standalone Android application module
│   └── src/main/
│       ├── java/             # Android Application & Activity
│       └── res/              # Android resources
├── iosApp/                   # iOS native Xcode project
└── gradle/                   # Gradle configuration
```

## Development Commands

### Build and Run
- **Desktop**: `./gradlew :shared:run`
- **Desktop (Hot Reload)**: `./gradlew :shared:runHot`
- **Android Debug APK**: `./gradlew :androidApp:assembleDebug`
- **Android App Bundle**: Find APK in `androidApp/build/outputs/apk/debug/androidApp-debug.apk`

### Testing
- **Desktop Tests**: `./gradlew :shared:jvmTest`
- **Android UI Tests**: `./gradlew :androidApp:connectedDebugAndroidTest`
- **iOS Simulator Tests**: `./gradlew :shared:iosSimulatorArm64Test`

### Code Quality
- **Detekt (Static Analysis)**: `./gradlew detektAll`
- **Export Library Licenses**: `./gradlew :shared:exportLibraryDefinitions`

### Prerequisites
- JDK 21 or higher (configured in gradle/libs.versions.toml)
- Add `local.properties` file with Android SDK path
- For iOS: Open `iosApp/iosApp.xcodeproj` in Xcode

## Architecture

### Core Structure
- **Dependency Injection**: Koin with KSP-generated modules for compile-time safety
- **Navigation**: Decompose library handles navigation with a stack-based approach
- **Database**: SQLDelight for local persistence with platform-specific drivers and async generation
- **HTTP Client**: Ktor for API communication with different engines per platform
- **UI**: Compose Multiplatform with Material3 theming
- **Rich Text**: Multiplatform Markdown Renderer for enhanced message display
- **Image Loading**: Coil with Ktor network support
- **Settings**: Multiplatform Settings with observable and coroutine support
- **Error Handling**: Arrow-kt for functional error handling
- **Logging**: Kermit for multiplatform logging
- **Clipboard**: Platform-specific clipboard implementations for copy functionality

### Key Components
- **Main Navigation**: Decompose-based navigation system with AppComponent managing chat, settings, and configuration screens
- **Repository Pattern**: Interfaces in `data/repositories/` with implementations for:
  - `ChatMessagesRepository` - Message persistence and retrieval
  - `ModelProviderRepository` - Provider configuration management
  - `SettingsRepository` - Application settings
  - `ChatRoomRepository` - Chat room management
- **Model Provider System**: Supports multiple AI providers defined in `LMClientModelProvider` sealed class:
  - OpenAI (`openai`)
  - Google (`google`)
  - Claude (`claude`)
  - OpenRouter (`openrouter`)
  - Ollama (`ollama`)
  - DeepSeek (`deepseek`)
  - Nexos AI (`nexos_ai`)
- **Database Stores**: SQLDelight tables for Messages, ChatRooms, ModelProviders, Models, ModelProviderConfigs, and MessageAttachments
- **Message Features**: Support for streaming responses, message copying, rich markdown rendering, and file attachments

### Package Structure

All shared code is in `shared/src/commonMain/kotlin/dev/olek/lmclient/`:

- `app/`: Application initialization and Koin module setup
  - `AppModule.kt` - Main Koin configuration
  - `KoinApp.kt` - Koin application setup
  - `AppInitializer.kt` - App initialization
- `data/`: Data layer with repositories, database, remote services, and models
  - `DataModule.kt` - Koin DI module for data layer
  - `models/` - Core data models (`LMClientError`, `Message`, `Model`, `ChatRoom`, `ModelProvider`)
  - `remote/` - API implementations for different providers:
    - `LMClientApi.kt` - Main API interface
    - `LMClientApiProvider.kt` - Factory with caching
    - `messages/` - Prompt execution APIs:
      - `PromptApi.kt` - Interface for prompt execution
      - `koog/KoogPromptApi.kt` - Koog framework implementation
      - `anthropic/CustomKoogAnthropicClient.kt` - Custom Anthropic client
    - `models/` - Model fetching per provider:
      - `ModelsApi.kt` - Interface
      - `koog/KoogModelsApi.kt` - Koog-based providers (Google, OpenAI, OpenRouter, DeepSeek, Ollama)
      - `anthropic/ClaudeModelsApi.kt` - Claude custom implementation
      - `nexosai/NexosAiModelsApi.kt` - NexosAI custom implementation
    - `mappers/` - Data transformation (`ErrorMapper`, `ModelMapper`, `KoogMessageMapper`, `CapabilityMapper`)
    - `utils/` - HTTP clients and provider utilities
  - `repositories/` - Repository implementations:
    - `ChatMessagesRepository.kt` - Message CRUD + streaming
    - `ChatRoomRepository.kt` - Chat session management
    - `ModelProviderRepository.kt` - Provider configuration
    - `SettingsRepository.kt` - Application settings
  - `database/` - SQLDelight stores (`MessagesStore`, `ChatRoomStore`, `ModelProviderStore`)
  - `util/LMClientModelProvider.kt` - Provider sealed class enum
- `presentation/`: UI layer with components, screens, and themes
  - `components/` - Decompose components for navigation and business logic:
    - `AppComponent.kt` - Root navigation with stack-based routing
    - `main/` - Chat screen components (`MainScreenComponent`, `ChatScreenComponent`, `ModelSelectorComponent`)
    - `settings/` - Settings components (`SettingsScreenComponent`, `ModelProviderListComponent`)
  - `ui/` - Compose UI screens:
    - `App.kt` - Root composable
    - `mobile/main/` - Main chat interface with markdown rendering
    - `mobile/main/chat/messages/` - Chat message items (`AssistantChatItem`, `UserChatItem`)
    - `mobile/main/chat/modelselector/` - Model selection popup
    - `mobile/settings/` - Settings screens and configuration
    - `mobile/common/` - Shared UI components (popups, list items, buttons)
    - `extensions/` - UI extension functions
  - `theme/` - Material3 theming (`Theme.kt`, `Color.kt`, `Typography.kt`)
  - `util/` - Platform utilities (`Clipboard.kt`, `CollectAsState.kt` - expect/actual)

Platform-specific implementations in `shared/src/{androidMain,iosMain,jvmMain}/`:
- Database drivers (`DriverFactory`)
- Clipboard implementations
- Theme adaptations
- Entry points (`main.kt` for desktop/iOS)

### Data Flow
1. **UI Components** interact with **Decompose Components**
2. **Components** call **Repository Interfaces**
3. **Repositories** coordinate between **Database Stores** and **Remote Services**
4. **Model Provider System** manages different AI service integrations
5. **Error Handling** uses Arrow-kt's `Either` type for functional error management
6. **Settings** are managed through observable multiplatform settings

### Important Notes
- When adding new model providers, update `LMClientModelProvider` sealed class and add database migration
- Platform-specific implementations use `expect/actual` declarations
- The app uses Material3 with custom theming including dynamic color support
- Database schema changes require migrations defined in `.sq` files (located in `shared/src/commonMain/sqldelight/`)
- Error handling follows the `LMClientError` sealed interface pattern for consistent error representation
- All remote API calls return `Either<LMClientError, Success>` for functional error handling
- Koin dependency injection uses KSP for compile-time module generation with auto-generated source in `build/generated/ksp/`
- Compose functions should follow naming conventions defined in `detekt.yml`
- The project includes AboutLibraries plugin for license management and compliance
- Android app is a separate module (`androidApp/`) that depends on `shared` module

### Koog Framework Integration
- **Koog Framework**: AI agent framework integrated for enhanced LLM provider management
- **Hybrid Architecture**: Koog used for supported providers (Google, OpenAI, OpenRouter, Ollama, DeepSeek), custom implementations for others
- **Bridge Pattern**: `KoogPromptApi` and `CustomKoogAnthropicClient` bridge Koog prompt executor to existing `PromptApi` interface
- **Error Mapping**: Koog exceptions are mapped to `LMClientError` types for consistency
- **Model Mapping**: Provider-specific model ID conversion for Koog framework integration
- **Enhanced Features**: Foundation for agent memory, structured data processing, and advanced AI capabilities
- **Custom Providers**: Support for non-standard providers like NexosAI through dedicated implementations
- **Streaming Support**: Integration with Koog's streaming capabilities using `Flow<MessageStreamResult>`

### Recent Features
- **Copy Messages**: Platform-specific clipboard integration for copying assistant messages
- **Markdown Rendering**: Rich text display using multiplatform-markdown-renderer with async parsing
- **Message Attachments**: Support for images, videos, audio, and file attachments with SQLDelight persistence
- **Message Actions**: Enhanced chat interface with retry, copy, and share functionality
- **Custom Provider Icons**: Unique visual identity for all providers with platform-specific implementations

### Build Configuration
- **Kotlin Multiplatform** with Compose Multiplatform UI framework
- **Android**: Min SDK 27, Target/Compile SDK 36
- **Android Gradle Plugin**: Uses `com.android.kotlin.multiplatform.library` plugin
- **JVM Target**: Java 21
- **Hot Reload**: Enabled for faster development iteration
- **BuildKonfig**: Version information injection at compile time

All library versions are managed in `gradle/libs.versions.toml`.
