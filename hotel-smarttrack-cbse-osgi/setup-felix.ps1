# Download Felix 7.0.5
$felixUrl = "https://dlcdn.apache.org/felix/org.apache.felix.main.distribution-7.0.5.zip"
$felixZip = "felix.zip"
$felixTemp = "felix-temp"
$felixDir = "felix"

Write-Host "Downloading Apache Felix..."
Invoke-WebRequest -Uri $felixUrl -OutFile $felixZip

Write-Host "Extracting Felix..."
Expand-Archive -Path $felixZip -DestinationPath $felixTemp -Force

Write-Host "Moving to felix/ folder..."
# Find the extracted directory (handles any structure)
$extractedDir = Get-ChildItem -Path $felixTemp -Directory | Select-Object -First 1
if ($extractedDir) {
    Move-Item -Path $extractedDir.FullName -Destination $felixDir -Force
} else {
    # If no subdirectory, just rename felix-temp to felix
    Move-Item -Path $felixTemp -Destination $felixDir -Force
}
Remove-Item -Path $felixTemp -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path $felixZip -Force

Write-Host "Downloading SCR bundles to felix/bundle/..."
# Create bundle directory if it doesn't exist
New-Item -Path "$felixDir/bundle" -ItemType Directory -Force | Out-Null
cd "$felixDir/bundle"

@(
  "https://repo1.maven.org/maven2/org/apache/felix/org.apache.felix.scr/2.2.10/org.apache.felix.scr-2.2.10.jar",
  "https://repo1.maven.org/maven2/org/osgi/org.osgi.service.component/1.5.1/org.osgi.service.component-1.5.1.jar",
  "https://repo1.maven.org/maven2/org/osgi/org.osgi.util.promise/1.3.0/org.osgi.util.promise-1.3.0.jar",
  "https://repo1.maven.org/maven2/org/osgi/org.osgi.util.function/1.2.0/org.osgi.util.function-1.2.0.jar"
) | ForEach-Object { 
  Write-Host "Downloading $(Split-Path $_ -Leaf)..."
  Invoke-WebRequest $_ -OutFile (Split-Path $_ -Leaf) 
}

cd ../..
Write-Host ""
Write-Host "✅ Felix setup complete!"
Write-Host "To start Felix: cd felix && java -jar bin/felix.jar"