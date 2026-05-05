# AiCognito

AI-powered IntelliJ plugin that helps reduce cognitive load and enhance brainstorming through voice interaction.

---

## Features

- Voice input for interacting with code
- AI-powered code analysis
- Multiple modes:
  - New → suggests next steps
  - Modify → analyzes impact and risks
  - Debug → finds problems and fixes
  - Explain → explains code clearly
- Voice output (text-to-speech)
- Integrated directly into IntelliJ

---

## Idea

Modern development often involves high cognitive load.

AiCognito helps developers:
- think through problems faster
- reduce context switching
- brainstorm directly inside the IDE
- interact with code using natural language

---

## Tech Stack

- Kotlin
- IntelliJ Platform SDK
- OpenAI API (chat + speech-to-text)
- OkHttp
- Java Sound API

---

## How to Run

1. Clone the repository: 
git clone [https://github.com/UnaStaziaBo/aicognito-intellij-plugin.git](https://github.com/UnaStaziaBo/aicognito-intellij-plugin.git)


2. Add your API key (in IntelliJ Run Configuration):
```

OPENAI_API_KEY=your_api_key_here

```

3. Run the plugin:

```

./gradlew runIde

```

---

## Usage

1. Select code in the editor  
2. Use:
   - Right-click → AiCognito
   - Tool Window buttons  
   - Voice input  

3. Get:
   - structured insights  
   - suggested improvements  
   - explanations  
   - voice feedback  

---

## Status

Prototype / MVP version

---

## Author

UnaStaziaBo
```
