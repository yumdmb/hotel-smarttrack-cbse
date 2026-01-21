# Build all bundles
mvn clean install

# Copy to Felix auto-deploy folder
Copy-Item common-bundle/target/*.jar felix/bundle/
Copy-Item guest-management-bundle/target/*.jar felix/bundle/
Copy-Item room-management-bundle/target/*.jar felix/bundle/
Copy-Item reservation-management-bundle/target/*.jar felix/bundle/
Copy-Item stay-management-bundle/target/*.jar felix/bundle/
Copy-Item billing-payment-bundle/target/*.jar felix/bundle/
Copy-Item application-bundle/target/*.jar felix/bundle/

Write-Host "✅ Bundles deployed to felix/bundle/"
Write-Host "Restart Felix: cd felix && java -jar bin/felix.jar"