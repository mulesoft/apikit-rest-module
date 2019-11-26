# APIKit 

APIKit Rest is a toolkit that facilitates the REST APIs development through MULE ESB. 

## Documentation link
* **APIkit Overview:** [link](https://docs.mulesoft.com/apikit/4.x/overview-4)
* **What's new in APIkit:** [link](https://mule4-docs.mulesoft.com/apikit/apikit-whats-new)
* **Release Notes:** [link](https://docs.mulesoft.com/release-notes/apikit/apikit-release-notes)
* **APIkit XML Reference:** [link](https://docs.mulesoft.com/apikit/4.x/apikit-4-xml-reference)
* **APIkit Error Handling Reference:** [link](https://docs.mulesoft.com/apikit/4.x/apikit-error-handling-reference)

## Team
* **Engineering Manager:** Pablo Angelani: [pablo.angelani@mulesoft.com](mailto:pablo.angelani@mulesoft.com)
* **Developers / Juanes:**  
  * Damián Courtil: [dcourtil@mulesoft.com](mailto:dcourtil@mulesoft.com)
  * Juan Brasca: [juan.brasca@mulesoft.com](mailto:juan.brasca@mulesoft.com)
  * Juan Aller: [juan.aller@mulesoft.com](mailto:juan.aller@mulesoft.com)

* **Team mailing account:** [apikit@mulesoft.com](mailto:apikit@mulesoft.com)

## Slack Channels
* APIkit : [#apikit](https://mulesoft.slack.com/archives/apikit)
* APIkit Support : [#apikit-support](https://mulesoft.slack.com/archives/apikit-support)
* RAML : [#raml](https://mulesoft.slack.com/archives/raml)

### Jira Project
* APIKIT [Open issues](https://www.mulesoft.org/jira/issues/?jql=project%20%3D%20APIKIT%20AND%20resolution%20%3D%20Unresolved)
* RAML Parser [Open issues](https://www.mulesoft.org/jira/issues/?jql=project%20%3D%20RP%20AND%20resolution%20%3D%20Unresolved)
* Support Escalations [Link](https://www.mulesoft.org/jira/issues/?filter=19636)

## Troubleshooting
### Using Studio
When running a Mule project, in the Console tab you will see these lines related to APIkit: 
~~~
[WrapperListener_start_runner] org.mule.runtime.module.extension.internal.manager.DefaultExtensionManager: Registering extension APIKit (version: 1.0.0-SNAPSHOT vendor: Mulesoft )
...
...
org.mule.module.apikit.Console: 
********************************************************************************
* APIKit ConsoleURL : http://localhost:8081/console/                           *
********************************************************************************
...
...
org.mule.runtime.module.deployment.impl.internal.application.DefaultMuleApplication: 
**********************************************************************
* Started app 'xxxxx'                                                *
* Application plugins:                                               *
*  - HTTP                                                            *
*  - APIKit                                                          *
*  - Sockets                                                         *
* Application libraries:                                             *
*  - xxxxx                                                           *
**********************************************************************
~~~

### Using Mule Runtime
When using APIkit with a Mule runtime, in the logs you may find these lines: 
~~~
[WrapperListener_start_runner] org.mule.runtime.module.deployment.impl.internal.application.DefaultMuleApplication:
**********************************************************************
* Started app 'xxxx'                                                 *
* Application plugins:                                               *
*  - HTTP                                                            *
*  - APIKit                                                          *
*  - File                                                            *
*  - Sockets                                                         *
*  - File Common Plugin                                              *
* Application libraries:                                             *
*  - xxxx                                                            *
**********************************************************************
~~~

Post requests using form parameters are supported, but not validated.
