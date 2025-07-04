# Define the base project path
$basePath = "src/main/java/com/prodash"

# Define all the file paths to create
$filePaths = @(
    # Domain Models
    "$basePath/domain/model/AnalysisJob.java",
    "$basePath/domain/model/AnalysisResult.java",

    # Persistence Layer
    "$basePath/infrastructure/adapter/out/persistence/AnalysisJobDocument.java",
    "$basePath/infrastructure/adapter/out/persistence/AnalysisResultDocument.java",
    "$basePath/infrastructure/adapter/out/persistence/AnalysisJobMongoRepository.java",
    "$basePath/infrastructure/adapter/out/persistence/AnalysisResultMongoRepository.java",
    "$basePath/infrastructure/adapter/out/persistence/AnalysisJobRepositoryAdapter.java",
    "$basePath/infrastructure/adapter/out/persistence/AnalysisResultRepositoryAdapter.java",

    # Application Ports
    "$basePath/application/port/in/StartAnalysisUseCase.java",
    "$basePath/application/port/in/GetAnalysisJobUseCase.java",
    "$basePath/application/port/out/AnalysisJobRepositoryPort.java",
    "$basePath/application/port/out/AnalysisResultRepositoryPort.java",

    # Application Services
    "$basePath/application/service/OnDemandAnalysisService.java",

    # Web Adapter
    "$basePath/infrastructure/adapter/in/web/AnalysisController.java"
)

# Loop through each file path
foreach ($filePath in $filePaths) {
    # Extract directory and ensure it exists
    $directory = Split-Path $filePath
    if (!(Test-Path $directory)) {
        New-Item -ItemType Directory -Path $directory -Force | Out-Null
        Write-Output "Created directory: $directory"
    }

    # Create the file if it doesn't exist
    if (!(Test-Path $filePath)) {
        New-Item -ItemType File -Path $filePath | Out-Null
        Write-Output "Created file: $filePath"
    } else {
        Write-Output "File already exists: $filePath"
    }
}

Write-Output "✅ All files and directories created."
