# ProposalAI: Legislative Analysis Engine

ProposalAI is a powerful backend service designed to autonomously fetch, analyze, and score legislative proposals from Brazil's Chamber of Deputies. It now captures not only the proposals themselves but also the entire voting record, including individual deputy votes, parties, and thematic areas.

Leveraging a modern tech stack and a robust Clean Architecture, this project provides a streamlined way to process and understand complex legislative data using the power of Large Language Models (LLMs).

### Table of Contents

  - [Highlights](https://www.google.com/search?q=%23highlights)
  - [Architectural Vision](https://www.google.com/search?q=%23architectural-vision)
  - [How It Works](https://www.google.com/search?q=%23how-it-works)
  - [Tech Stack](https://www.google.com/search?q=%23tech-stack)
  - [Installation & Setup](https://www.google.com/search?q=%23installation--setup)
  - [Usage](https://www.google.com/search?q=%23usage)
  - [Contributing](https://www.google.com/search?q=%23contributing)
  - [License](https://www.google.com/search?q=%23license)
  - [Acknowledgments](https://www.google.com/search?q=%23acknowledgments)

-----

## Highlights

  - **Comprehensive Data Ingestion**: A built-in scheduler automatically fetches the latest legislative proposals, **votings**, **individual votes**, **deputies**, **parties**, and **thematic areas** from Brazil's official Câmara dos Deputados API.
  - **AI-Powered Analysis**: Utilizes a Large Language Model (via OpenRouter) to perform two key tasks in efficient batches:
      - **Summarization**: Generates concise, neutral summaries for complex proposals.
      - **Impact Scoring**: Assigns a quantitative `impact_score` (0.0-10.0) and a qualitative `justification` to each proposal.
  - **Clean Architecture**: Refactored for maintainability, testability, and scalability by separating concerns into distinct Domain, Application, and Infrastructure layers.
  - **Efficient & Scalable**: Employs an intelligent data fetching strategy that only requests new information, avoiding full database scans and minimizing API calls.
  - **User-Friendly Console**: Displays a dynamic progress bar for long-running tasks, providing clear visual feedback on the data synchronization process.
  - **RESTful API**: Exposes a simple REST endpoint to retrieve all processed proposals, making it easy to integrate with a frontend or other services.

-----

## Architectural Vision

This project was recently refactored to follow the principles of **Clean Architecture**. This strategic decision ensures that the core business logic (the "Domain") is completely independent of external frameworks and technologies like the database or web server.

The key benefits of this architecture include:

  - **High Testability**: Each layer can be tested in isolation.
  - **Framework Independence**: The core logic isn't tied to Spring, allowing for easier upgrades or migrations in the future.
  - **Maintainability**: A clear separation of concerns makes the codebase easier to understand, modify, and extend.

The application is structured into three primary layers:

  - `domain`: Contains the core business models and rules.
  - `application`: Orchestrates the use cases (e.g., fetching and scoring proposals).
  - `infrastructure`: Handles all external communication, such as database connections, API calls, and scheduling.

-----

## How It Works

The data ingestion and processing pipeline is fully automated and runs in a specific, efficient sequence.

1.  **Scheduler Trigger**: The `ProposalSyncScheduler` triggers the `runFullDataSync` method based on a single cron expression.
2.  **Fetch New Proposals**: The `ProposalFetchingService` is called.
      - It fetches the latest proposal IDs from the Câmara API.
      - It efficiently queries the database **once** to get all existing IDs.
      - It filters in-memory to find only the new, unique proposal IDs.
      - For these new IDs, it fetches the full details, including their associated **themes**.
3.  **Fetch Votings and Votes**: The list of new proposal IDs is passed to the `VotingFetchingService`.
      - For each new proposal, it queries the API for any associated **voting sessions**.
      - If new voting sessions are found, it queries the API for every **individual vote** within that session.
      - It ensures the corresponding **Deputy** and **Party** for each vote are saved to the database.
4.  **LLM Enrichment**: The scheduler then triggers the AI-powered services.
      - The `SummarizeProposalsUseCase` finds proposals without a summary and sends them to the LLM.
      - The `ScoreProposalsUseCase` finds summarized but unscored proposals and sends them to the LLM for an impact score and justification.
5.  **Process Repeats**: The entire cycle repeats on the schedule defined by `prodash.sync.cron`.

-----

## Tech Stack

  - **Backend**: Spring Boot 3
  - **Language**: Java 17
  - **Database**: MongoDB
  - **Build Tool**: Apache Maven
  - **Core Libraries**:
      - Spring Data MongoDB
      - Spring Web
      - Spring Scheduler
  - **Console UI**: `me.tongfei:progressbar`
  - **LLM Integration**: OpenRouter (configurable for any OpenAI-compatible API)

-----

## Installation & Setup

Follow these steps to get the project running on your local machine.

### Prerequisites

  - **Java 17 SDK**
  - **Apache Maven 3.8+**
  - **MongoDB** (running on `mongodb://localhost:27017`)
  - **OpenRouter API Key** (or another compatible LLM provider)

### Step-by-Step Installation

1.  **Clone the repository:**

    ```bash
    git clone https://github.com/your-username/proposal-ai.git
    cd proposal-ai
    ```

2.  **Configure your API Key and Scheduler:**

      - Open the `src/main/resources/application.properties` file.
      - Find the `openrouter.api.key` property and replace `YOUR_OPENROUTER_API_KEY` with your actual key.
      - Adjust the `prodash.sync.cron` expression if needed. The default is set to run every 6 hours.

    <!-- end list -->

    ```properties
    # OpenRouter LLM Configuration
    openrouter.api.key=YOUR_OPENROUTER_API_KEY
    openrouter.api.url=https://openrouter.ai/api/v1/chat/completions
    llm.model.name=openai/gpt-3.5-turbo

    # Cron for the full data sync cycle (e.g., every 6 hours)
    prodash.sync.cron=0 0 */6 * * *
    ```

3.  **Build and run the application using Maven:**

    ```bash
    mvn clean install
    mvn spring-boot:run
    ```

The application will start, and after a short delay, it will begin the first full data synchronization process.

-----

## Usage

Once the application is running, you can retrieve the processed proposals by calling the API endpoint.

### Retrieve All Proposals

  - **URL**: `http://localhost:8080/api/proposals`
  - **Method**: `GET`

**Example using `curl`:**

```bash
curl http://localhost:8080/api/proposals
```

**Example JSON Response:**

```json
[
  {
    "id": "2421344",
    "title": "PL 1933/2024",
    "summary": "This is a concise, AI-generated summary of the legislative proposal's main objectives.",
    "fullTextUrl": "https://www.camara.leg.br/proposicoesWeb/fichadetramitacao?idProposicao=2421344",
    "status": "Aguardando Parecer do Relator na Comissão de Saúde (CSAUDE)",
    "presentationDate": "2024-05-21",
    "impactScore": 8.2,
    "justification": "This proposal has a high potential for economic impact due to its proposed changes to tax regulations.",
    "themes": [
        {
            "cod": 135,
            "nome": "Saúde"
        },
        {
            "cod": 142,
            "nome": "Finanças Públicas e Orçamento"
        }
    ]
  }
]
```

-----

## Contributing

Contributions are welcome\! If you have suggestions for improvements or find a bug, please follow these steps:

1.  **Fork** the repository.
2.  Create a new branch (`git checkout -b feature/your-feature-name`).
3.  Make your changes and commit them (`git commit -m 'Add some amazing feature'`).
4.  Push to the branch (`git push origin feature/your-feature-name`).
5.  Open a **Pull Request**.

Please ensure your code adheres to the existing style.

-----

## License

This project is licensed under the MIT License. See the LICENSE file for details.

-----

## Acknowledgments

  - The **Clean Architecture** principles as described by Robert C. Martin.
  - The **Spring Boot** team for creating a powerful and easy-to-use framework.
  - The **OpenRouter** team for providing a flexible gateway to various LLMs.