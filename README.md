# SAP Build Code & AnyDox Service Proof of Concept

## Disclaimer
This project is provided as-is and is not intended as an official recommendation from SAP. It serves as a proof of concept to demonstrate the integration of SAP Build Code and SAP Document Information Extraction Service.

## Context Providing Blog
This repository is part of a blog post that provides a detailed explanation of the scenario and solution. You can read the full blog post [here](link-to-blog).

## Project Changes Required

### Change Your App Configuration ID
In the following files, change <Your-App-ID> to app configuration ID you defined while creating an app config on SAP Mobile Services Cockpit.

1. AndroidManifest.xml
2. sap_mobile_services.json

### Update Security Configuration

From SAP Mobile Services &rarr; Your App &rarr; Security copy the following values into the sap_mobile_services.json file

1. clientID
2. redirectURL
3. oauth2.tokenEndpoint
4. oauth2.authorizationEndpoint 

### Update Service URL

From SAP Mobile Services &rarr; Your App &rarr; APIs copy the following values into the sap_mobile_services.json file

1. ServiceUrl