# SAP Build Code & AnyDox Service Proof of Concept

## Disclaimer
This project is provided as-is and is not intended as an official recommendation from SAP. It serves as a proof of concept to demonstrate the integration of SAP Build Code and SAP Document Information Extraction Service.

## Context Providing Blog
This repository is part of a blog post that provides a detailed explanation of the scenario and solution. You can read the full blog post [here](https://community.sap.com/t5/technology-blogs-by-sap/transform-your-phone-s-camera-into-a-next-gen-input-tool-with-sap-build/ba-p/13770867).

## Project Changes Required

### Change Your App Configuration ID
In the following files, replace `<Your-App-ID>` with the app configuration ID you defined while creating the app configuration in SAP Mobile Services Cockpit:

1. `AndroidManifest.xml`
2. `sap_mobile_services.json`

### Update Security Configuration
From SAP Mobile Services → Your App → Security, copy the following values into the `sap_mobile_services.json` file:

1. `clientID`
2. `redirectURL`
3. `oauth2.tokenEndpoint`
4. `oauth2.authorizationEndpoint`

### Update Service URL
From SAP Mobile Services → Your App → APIs, copy the `ServiceUrl` value into the `sap_mobile_services.json` file.

### Update API References to AnyDox Service
In the `SAPServiceManager.kt` file, make the following changes:

1. Change `CONNECTION_ID_DOX_DESTINATION` if you used a different name when creating the destination for SAP Document Information Extraction Service on SAP Mobile Services.
2. Fetch the Schema ID using [this API](https://help.sap.com/docs/document-information-extraction/document-information-extraction/get-schema?locale=en-US) and paste it as the value for `val schemaID` in the `uploadToDoxService` function.
