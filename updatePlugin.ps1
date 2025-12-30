$ErrorActionPreference = 'Stop'

$targetDir = Join-Path (Get-Location) 'target'

if (-not (Test-Path -LiteralPath $targetDir)) {
    Write-Error "Target directory not found: $targetDir"
    exit 1
}

# Pick the most recently modified plugin jar (exclude sources/javadoc jars)
$jar = Get-ChildItem -Path $targetDir -Filter 'BackpackMC-*.jar' -File |
        Where-Object { $_.Name -notmatch '(-sources|-javadoc)\.jar$' } |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1

if (-not $jar) {
    Write-Error "No plugin jar found matching BackpackMC-*.jar in $targetDir"
    exit 1
}

$destinations = @(
    'D:\_Minecraft\_SurvivalLand\servers\Terralith\plugins'
    'D:\_Minecraft\_SurvivalLand\servers\Survival\plugins'
)

foreach ($d in $destinations) {
    if (-not (Test-Path -LiteralPath $d)) {
        New-Item -ItemType Directory -Path $d -Force | Out-Null
    }

    # Delete old versions first (handles different numbers at the end)
    $oldJars = Get-ChildItem -Path $d -Filter 'BackpackMC-*.jar' -File |
            Where-Object { $_.Name -notmatch '(-sources|-javadoc)\.jar$' }

    foreach ($old in $oldJars) {
        try {
            Remove-Item -LiteralPath $old.FullName -Force
            Write-Host "Deleted old plugin: $($old.Name) from $d"
        } catch {
            Write-Warning "Failed to delete $($old.FullName): $($_.Exception.Message)"
        }
    }

    Copy-Item -LiteralPath $jar.FullName -Destination $d -Force
    Write-Host "Copied $($jar.Name) to $d"
}

exit 0
