# Enable advanced function features, including common parameters like -Verbose
[CmdletBinding()]
param (
    [string]$RootPath = (Get-Location),
    [string[]]$ExcludedFolders = @("node_modules", ".git", "build", "target", "venv", "__pycache__",".pytest_cache",".mvn",".vscode"),
    [string]$OutputFileName = "folder_structure.txt"
)

# Initialize a script-scoped array to hold the folder structure lines
$script:folderStructure = @()

# Function to recursively get folder structure
function Get-FolderStructure {
    param (
        [string]$Path,
        [string]$Indent = ""
    )

    try {
        # Corrected Sort-Object syntax: Use 'Expression' instead of 'Name'
        $items = Get-ChildItem -Path $Path -Force -ErrorAction Stop | Sort-Object -Property @{Expression='PSIsContainer';Descending=$true}, 'Name'
    }
    catch {
        Write-Warning "Cannot access path: $Path. Skipping... Error: $_"
        # Add a note to the folder structure
        $script:folderStructure += "$Indent|-- [Cannot access: $Path]"
        return
    }

    foreach ($item in $items) {
        # Debug: show the current item being processed
        Write-Verbose "Processing item: $($item.FullName)"

        # Check if the item is a directory and if its name is in the excluded list
        if ($item.PSIsContainer) {
            if ($ExcludedFolders -contains $item.Name) {
                Write-Verbose "Excluding folder: $($item.FullName)"
                continue
            }
        }

        # Add the current item to the structure
        $script:folderStructure += "$Indent|-- $($item.Name)"
        Write-Verbose "Added to structure: $Indent|-- $($item.Name)"

        # If the item is a directory, recurse into it
        if ($item.PSIsContainer) {
            # Increase the indentation for sub-items
            $newIndent = "$Indent|   "
            Get-FolderStructure -Path $item.FullName -Indent $newIndent
        }
    }
}

# Start building the folder structure from the root path
$script:folderStructure += $RootPath
Write-Verbose "Starting folder structure generation from: $RootPath"
Get-FolderStructure -Path $RootPath

# Output the folder structure to the console
$script:folderStructure | ForEach-Object { Write-Output $_ }

# Save the folder structure to the specified file in the root directory
$outputFile = Join-Path -Path $RootPath -ChildPath $OutputFileName
try {
    $script:folderStructure | Out-File -FilePath $outputFile -Encoding UTF8 -Force
    Write-Output "`nFolder structure has been saved to $outputFile"
}
catch {
    Write-Error "Failed to write to file: $outputFile. Error: $_"
}
