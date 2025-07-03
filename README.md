# ProposalAI: Legislative Analysis Engine

ProposalAI is a powerful backend service designed to autonomously fetch, analyze, and score legislative proposals. Leveraging a modern tech stack and a robust Clean Architecture, this project provides a streamlined way to process and understand complex legislative data using the power of Large Language Models (LLMs).

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

\<hr\>

## Highlights

  * **Automated Data Ingestion**: A built-in scheduler automatically fetches the latest legislative proposals from Brazil's official Câmara dos Deputados API.
  * **AI-Powered Analysis**: Utilizes a Large Language Model (via OpenRouter) to perform two key tasks in efficient batches:
      * **Summarization**: Generates concise, neutral summaries for complex proposals.
      * **Impact Scoring**: Assigns a quantitative `impact_score` (0.0-10.0) and a qualitative `justification` to each proposal.
  * **Clean Architecture**: Refactored for maintainability, testability, and scalability by separating concerns into distinct Domain, Application, and Infrastructure layers.
  * **RESTful API**: Exposes a simple REST endpoint to retrieve all processed proposals, making it easy to integrate with a frontend or other services.
  * **Resilient and Scalable**: Built on Spring Boot and MongoDB, ensuring a robust foundation for handling large volumes of data.

\<hr\>

## Architectural Vision

This project was recently refactored to follow the principles of **Clean Architecture**. This strategic decision ensures that the core business logic (the "Domain") is completely independent of external frameworks and technologies like the database or web server.

The key benefits of this architecture include:

  * **High Testability**: Each layer can be tested in isolation.
  * **Framework Independence**: The core logic isn't tied to Spring, allowing for easier upgrades or migrations in the future.
  * **Maintainability**: A clear separation of concerns makes the codebase easier to understand, modify, and extend.

The application is structured into three primary layers:

  * `domain`: Contains the core business models and rules.
  * `application`: Orchestrates the use cases (e.g., ingesting and scoring proposals).
  * `infrastructure`: Handles all external communication, such as database connections, API calls, and scheduling.

\<hr\>

## How It Works

1.  The `ProposalSyncScheduler` automatically triggers the ingestion process on a schedule.
2.  The `ProposalIngestionService` calls the `CamaraApiAdapter` to fetch a list of the latest proposals.
3.  The service filters out any proposals that already exist in the MongoDB database.
4.  The remaining new proposals are sent in a single batch to the `LlmAdapter`.
5.  The `LlmAdapter` uses the `summarize_proposals_prompt.txt` to ask the configured LLM to generate a summary for each proposal.
6.  The service saves the newly summarized proposals to the database.
7.  Separately, the scheduler triggers the `ProposalScoringService`.
8.  This service finds all unscored proposals in the database and sends them in a batch to the `LlmAdapter`, this time using the `impact_score_prompt.txt`.
9.  The LLM returns an impact score and justification for each, which are then saved to the database.

\<hr\>

## Tech Stack

  * **Backend**: Spring Boot 3
  * **Language**: Java 17
  * **Database**: MongoDB
  * **Build Tool**: Apache Maven
  * **Core Libraries**:
      * Spring Data MongoDB
      * Spring Web
      * Spring Scheduler
  * **LLM Integration**: OpenRouter (configurable for any OpenAI-compatible API)
  * **Code Quality**: Lombok

\<hr\>

## Installation & Setup

Follow these steps to get the project running on your local machine.

### Prerequisites

  * **Java 17 SDK**
  * **Apache Maven 3.8+**
  * **MongoDB** (running on `mongodb://localhost:27017`)
  * **OpenRouter API Key** (or another compatible LLM provider)

### Step-by-Step Installation

1.  **Clone the repository:**

    ```bash
    git clone https://github.com/your-username/proposal-ai.git
    cd proposal-ai
    ```

2.  **Configure your API Key:**

      * Open the `src/main/resources/application.properties` file.
      * Find the `openrouter.api.key` property and replace `YOUR_OPENROUTER_API_KEY` with your actual key.

    <!-- end list -->

    ```properties
    # OpenRouter LLM Configuration
    openrouter.api.key=YOUR_OPENROUTER_API_KEY
    openrouter.api.url=https://openrouter.ai/api/v1/chat/completions
    llm.model.name=openai/gpt-3.5-turbo
    ```

3.  **Build and run the application using Maven:**

    ```bash
    mvn spring-boot:run
    ```

The application will start, and after a short delay (configured in `ProposalSyncScheduler`), it will begin fetching and processing proposals.

\<hr\>

## Usage

Once the application is running, you can retrieve the processed proposals by calling the API endpoint.

### Retrieve All Proposals

  * **URL**: `http://localhost:8080/api/proposals`
  * **Method**: `GET`

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
    "status": null,
    "presentationDate": null,
    "impactScore": 8.2,
    "justification": "This proposal has a high potential for economic impact due to its proposed changes to tax regulations."
  }
]
```

\<hr\>

## Contributing

Contributions are welcome\! If you have suggestions for improvements or find a bug, please follow these steps:

1.  **Fork** the repository.
2.  Create a new branch (`git checkout -b feature/your-feature-name`).
3.  Make your changes and commit them (`git commit -m 'Add some amazing feature'`).
4.  Push to the branch (`git push origin feature/your-feature-name`).
5.  Open a **Pull Request**.

Please ensure your code adheres to the existing style and that all tests pass.

\<hr\>

## License

This project is licensed under the MIT License. See the LICENSE file for details.

\<hr\>

## Acknowledgments

  * The **Clean Architecture** principles as described by Robert C. Martin.
  * The **Spring Boot** team for creating a powerful and easy-to-use framework.
  * The **OpenRouter** team for providing a flexible gateway to various LLMs.
