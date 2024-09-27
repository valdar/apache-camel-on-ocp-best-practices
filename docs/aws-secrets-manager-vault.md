## AWS Secrets Manager Secrets Retrieval with Apache Camel On OCP

First of all all of the following could be supported by simply adding the camel-aws-secrets-manager component to your classpath.

To set up the secret retrieval with AWS Secrets Manager you have to first authenticate to the AWS Service. The mechanisms are:

Static credentials in application.properties:

    camel.vault.aws.accessKey = accessKey
    camel.vault.aws.secretKey = secretKey
    camel.vault.aws.region = region	

Default Credentials Provider in application.properties:

    camel.vault.aws.defaultCredentialsProvider = true
    camel.vault.aws.region = region


The static credentials could be a Kubernetes/OCP secret or you can mount a volume containing the AWS credentials file and use the default credentials provider chain. This is something up to the end user, but it’s important to stress the fact that AWS encourages the usage of default credentials provider chains instead of static credentials.

Creating a secret on AWS will require to have access to the AWS Console or to AWS CLI with create-secret and general secret manager permissions. In general the suggested way of creating a secret is by using JSON.

You can create a secret through AWS CLI with the following command:

aws secretsmanager create-secret  --name authsecdb  --secret-string file://secret.json

Where the content of secret.json could be something like:

    {
      "username": "postgresadmin",
      "password": "xxxx",
      "host": "host"
    }

The secret name will be authsecdb and the secret fields will be username, password and host.

In the Camel route it will be enough to use the following syntax and the secrets field will be retrieved.

    {{aws:authsecdb/host}}
    {{aws:authsecdb/username}}
    {{aws:authsecdb/password}}

The Spring Boot and Quarkus runtime have the starter and the extension related to AWS Secret Manager in their catalogs. The export and export Kubernetes command from camel-jbang, will automatically add the dependency in case of the above syntax usage. This should be transparent to the end user.

The same configuration could be seen on OCP by following the Camel on OCP Best Practices repository, in particular, the AWS vault section. You can follow the example for both the runtimes supported by Red Hat Build of Apache Camel:

[Camel-Quarkus - Camel on OCP Best practices - Camel Quarkus - AWS Vault](https://github.com/oscerd/camel-on-ocp-best-practices/tree/main/vault/aws/camel-quarkus/retrieval)

[Camel-Spring-Boot - Camel on OCP Best practices - Camel Spring Boot - AWS Vault](https://github.com/oscerd/camel-on-ocp-best-practices/tree/main/vault/aws/camel-spring-boot/retrieval)
