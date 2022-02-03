# Hefesto DEVOPS

O Hefesto Devops fornece um conjunto de arquétipos e configurações para aplicações e pipelines. 

--- 

## O que vai encontrar aqui?

Os arquivos desta pasta (".azuredevops/")  são responsáveis por todas as configurações de construção 
e implantação de sua aplicação. 

1. **pipeline-azure.yml** - Pipeline atente o arquétipo de seu projeto

2. **Dockerfile** - Dockerfile utilizado para construção do container contendo sua aplicação

3. **secrets.yaml** - **Referênciadas** a todas as chaves armazenadas no cofre (Azure Vault)   
e disponibilizadas para aplicação.

4. **./helm/[enviroment]/environments_variables.yml** - Arquivo para definição de variáveis de ambiente.

5. **./helm/[enviroment]/values.yml** - Valores do Helm Chart responsável pela implantação.


---

## Antes de qualquer coisa, vamos aos 3 TENHA!

- **Tenha *CAUTELA* ao fazer alterações nos arquivos deste diretório.**

- **Tenha *CERTEZA* em garantir o entendimento da real necessidade do ajuste que vai realizar**

- **Tenha *CIÊNCIA* que alterações equivocadas podem indisponibilizar sua aplicação e demais sistemas que dependam dela.**

---

## Agora um pouco mais sobre os arquivos de configuração e definição.
 
### Pipeline-azure.yml - Definição do processo de CI/CD
Este arquivo **define qual pipeline será utilizada para a construção e implantação** de sua aplicação, para isso ele **utiliza**
a **variável** chamada **archetype**, *no exemplo abaixo configurada para utilizar o archetype api-helm-k8s.*

**Quando** um **pull request** é solicitado, ou os ramos (branch) **dev_integração**, **release** ou **master** recebem uma modificação a esteira
é **automaticamente acionada** para **verificar:** **"qualidade"**  através do código utilizando o *SonarQube* e
 **valida "construção"** da aplicação. No caso de **sucesso* nas validações das branches de integração, ela **adiciona uma tag** [ambiente]/[versao_aplicacao] (develop/0.0.1-SNAPSHOT).
 Assim que a **tag é adicionada** a **esteira realiza** a **implantação** no ambiente referente a tag fornecida.

```yaml
variables:
- template: /modules/variables/load-variables.yml@Pipelines
- name: archetype
  value: java/api-helm-k8s
trigger:
  batch: true
  branches:
    include:
    - dev_integracao
    - release
    - master
    - refs/tags/develop/*
    - refs/tags/release/*
    - refs/tags/production/*
resources:
  repositories:
  - repository: Pipelines
    name: Hefesto/Hefesto.Core.Pipelines
    type: git
    ref: refs/tags/v1-latest
    endpoint: HefestoPipelines
stages:
- template: /archetypes/$'{{'variables.archetype'}}'.yml@Pipelines
```
**Obs:**
- *Esta pipeline utiliza templates (archetypes) que fazem parte de uma Pipeline Core armazenada em outro repositório.*
- *A Definição de todos os passos podem ser encontrados no repositório Hefesto.Core.Pipeline.*
- *Por padrão a referência a Pipeline Core é definida pela tag da versão no formato Major-lastest (v1-latest)*
- *Para criação de novos arquétipos abra uma nova branch em Hefesto.Core.Pipeline, aponte sua pipeline para sua nova branch ```ref: refs/heads/minha-branch-de-implementacao```, 
realize a implementação, teste muito, solicite um pull request. Antes mesmo de ser aprovado você pode ir utilizando seu novo
archetype apontado para a branch de sua implementação.*   

---

### Dockerfile - Instruções para conteinerização da aplicação
É recomendado que o **Dockerfile** utilize uma imagem base versionada no registry interno da companhia. 
Ex FROM registry-docker-k8s.riachuelo.net:5000/corporativo/openjdk:14

Durante a construção da imagem a esteira passa dois argumentos de construção o primeiro é o **APPLICATION_PATH**
que é o diretório que contém a aplicação contruída e o segundo arqumento é o **APPLICATION_NAME** que contém o nome do arquivo referente a aplicação contruída.

**Exemplo: Container Java**
```bash
docker build ...
    --build-arg APPLICATION_PATH="target/"
    --build-arg APPLICATION_NAME=application.jar
...
```

**Exemplo: Java14**
```Dockerfile
FROM registry-docker-k8s.riachuelo.net:5000/corporativo/openjdk:14
MAINTAINER Riachuelo Developer Team
ARG APPLICATION_NAME
ENV APPLICATION_NAME=$APPLICATION_NAME
ARG APPLICATION_PATH
ENV APPLICATION_PATH=$APPLICATION_PATH
ADD $APPLICATION_PATH/$APPLICATION_NAME $APPLICATION_NAME
VOLUME /config
ENV JAVA_OPTS_MEMORY_MIN="128m"
ENV JAVA_OPTS_MEMORY_MAX="256m"
ENV JAVA_OPTS_METASPACE_MIN="48m"
ENV JAVA_OPTS_METASPACE_MAX="48m"
ENV JAVA_OPTS_MEMORY='-Xms$JAVA_OPTS_MEMORY_MIN -Xmx$JAVA_OPTS_MEMORY_MAX -XX:MetaspaceSize=$JAVA_OPTS_METASPACE_MIN -XX:MaxMetaspaceSize=$JAVA_OPTS_METASPACE_MAX'
ENV JAVA_OPTS_CONFIG='-Dspring.config.location=file:/config/application.properties '
ENV JAVA_OPTS="$JAVA_OPTS_MEMORY  -XX:+UseG1GC -XX:+UnlockExperimentalVMOptions -XX:+ShowCodeDetailsInExceptionMessages $JAVA_OPTS_CONFIG -Djava.security.egd=file:/dev/./urandom"
RUN echo $'#!/bin/sh \n\
java '$JAVA_OPTS' --enable-preview -jar '$APPLICATION_NAME'' > ./entrypoint.sh && \
    chmod +x ./entrypoint.sh
EXPOSE 8082
ENTRYPOINT ["./entrypoint.sh"]
```
---

### Secrets.yaml - Valores secretos e arquivos sigilosos

Este arquivo é o local correto para você referências os valores secretos que sua aplicação necessita.

Qualquer valor que represente uma senha, chave de api, tokens e segredos devem ser declaradas neste arquivos, 
sua declaração é feita através de um mapeamento entre a variável de ambiente que será disponibilizada para a aplicação 
em tempo de implantação e a secret criada no Vault da Azure.  

Por padrão existem 3 cofres para cada aplicação, um para cada ambiente. Quando um mapeamento é definido é necessário 
declarar essa secret nos 3 cofres, sempre com o mesmo nome de secret declarado no mapeamento. 

Ex:
```yaml
secrets: 
- name: ENV_VAR_MINHA_SENHA
  value: "$(MINHA-SENHA)"
- name: ENV_VAR_MEU_TOKEN
  value: "$(MEU-TOKEN)"
```
Aqui você também consegue declarar arquivos secretos, como um certificado, chave de serviço, etc. 

Vamos dizer que sua aplicação consome um recurso da cloud do google como um PubSub, para sua aplicação utilizar esse
recurso, ela necessita se authenticar e para isso é necessário fornecer um arquivo .json contendo a chave de serviço. 

Para cada ambiente temos nossas respectivas service_account.json. O primeiro passo seria armazenar 
o conteúdo do nosso service_account.json serializado em base64 em uma secret ex: GCP-SERVICE-ACCOUNT. 

O segundo passo declarar essa relação no secrets.yaml

**Ex.:**
```yaml
secretFiles:
- name: service_account.json
  value: "$(GCP-SERVICE-ACCOUNT)"
```
**Durante a implantação será montado um volume /config e dentro deste diretório será criado um arquivo service_account.json
contendo o arquivo armazenado no vault da aplicação.**


**Ex. com mais de um arquivo:**
```yaml
secretFiles:
- name: service_account.json
  value: "$(GCP-SERVICE-ACCOUNT)"
- name: ssh.pem
  value: "$(SSH-PEM)"
```
**Durante a implantação será montado um volume /config e dentro deste diretório será criado o arquivo service_account.json
e o ssh.pem, ambos contendo o conteúdo armazenado no vault da aplicação.**

Os **Vaults** da Azure **possuem limitações** na definição de uma chave, são no **máximo 126 caracteres** e **somente letras**, **números** e **-** (traço) são permitidos.
*Atenção caracteres especiais, ponto e underline são proibidos.*

---

### Helm / [enviroment] / environments_variables.yml - Variáveis de ambiente

Neste aqruivo você deve declarar as variáveis de ambiente que sua aplicação necessita. 
Todas as variaveis declaradas serão disponibilizadas para a aplicação no momento da implantação do container.

*Obs: nunca declare aqui informações sigilosas como senhas, tokens e chaves*

**Exemplo: helm/production/environments_variables.yml**
```yaml
env: 
    - name: "ENV_VAR_ENVIRONMENT"
      value: "prod"
    - name: "TZ"
      value: "America/Sao_Paulo"
```

**Exemplo: helm/homologation/environments_variables.yml**
```yaml
env: 
    - name: "ENV_VAR_ENVIRONMENT"
      value: "uat"
    - name: "TZ"
      value: "America/Sao_Paulo"
```

**Exemplo: helm/development/environments_variables.yml**
```yaml
env: 
    - name: "ENV_VAR_ENVIRONMENT"
      value: "dev"
    - name: "TZ"
      value: "America/Sao_Paulo"
```

---

### Helm / [enviroment] / values.yml - Configurações da implantação

Cada **ambiente possui** seu **próprio arquivo** de **configuração** da implantação,
assim possibilitando **configurações customizadas** para regras de autoscale como número inicial e máximo
de replicas **distintas** para **desenvolvimento**, **homologação** e **produção**.

No **exemplo** abaixo o arquivo **define** a **porta de exposição do container**,
**dominio base** para exposição pelo Ingress,
nome da **secret** que contém o **tls** para o dominio base (costuma ser o mesmo nome do cluster), **replica inicial**,
 **regras de prontidão**, **limite para utilização de recursos** e **regra de autoscale**.

*Fique atento com as regras de prontidão, tanto o liveness quando o readiness servem para monitorar a prontidão
de sua aplicação a grande diferença é o que fazem no caso de sucesso e falha. Quando falha o liveness reinicia o POD,
já o readness quando falha corta o fluxo de requisições para o POD. E quando obtem sucesso tornam o POD disponível.*

**Exemplo:**
```yaml
containerPort: '8082'
httpHostname: app-microservice.tribe.rchlo.k8s-dev.riachuelo.net.br
httpsHostname: app-microservice-tribe.rchlo.k8s-dev.riachuelo.net.br
tlsSecretName: on-premise-dev-rchlo-corp
initialReplicas: 1
livenessProbe:
  httpGet:
    path: /actuator/health
    port: '8082'
  initialDelaySeconds: 240
  periodSeconds: 10
  timeoutSeconds: 1
  successThreshold: 1
  failureThreshold: 5
readinessProbe:
  httpGet:
    path: /actuator/health
    port: '8082'
  initialDelaySeconds: 60
  periodSeconds: 5
  timeoutSeconds: 1
  successThreshold: 1
  failureThreshold: 3
resources:
  requests:
    memory: 512M
    cpu: 150m
  limits:
    memory: 1024M
hpa:
  maxReplicas: 2
  minReplicas: 1
  targetCPUUtilizationPercentage: 75

```

---

**TODA ALTERAÇÃO REALZIADAS NESTES ARQUIVOS SERÃO PROPAGADAS SOMENTE APÓS A RE-ENTREGUA**

---

# FAQ

### Como resolvo quando minha aplicação precisa de mais memória ou processamento?

Para fornecer mais recurso a sua aplicação, edite o arquivo ```./helm/[enviroment]/values.yml```.
 
Procure pelas configurações de ```resource:```, lá são definidos quantro sua aplicação necessita e o limite máximo de utilização.

**Memória:**
1. ```resources -> requests -> memory```  
2. ```resources -> limits -> memory``` 

*Idealmente, no caso de memória, ambos devem ser iguais.*

**CPU (Processamento)**
1. ```resources -> requests -> cpu```
2. ```resources -> limits -> cpu```

Exemplo:
```
resources:
  requests:
    memory: 512M
    cpu: 150m
  limits:
    memory: 1024M
```
***Omitir um valor em limites torna a utilização do container limitada aos recursos do node do cluster.***

Para mais detalhes acesse por favor o link: melhores práticas de resource requests e limites (https://cloud.google.com/blog/products/gcp/kubernetes-best-practices-resource-requests-and-limits).

*obs: O auto-scale com base em processamento, utiliza o que foi definido em ```resources.requests.cpu```*   

---

### Como declaro a porta que a aplicação responde?


Para mudar a porta da sua aplicação, sao necessários 3 passos:

1.Ajuste a instrução ```EXPOSE``` no ```Dockerfile``` (exemplo ```EXPOSE 80```);
```Dockerfile
EXPOSE 8082
```
2.Configure o ```containerPort``` no arquivo ```./helm/[enviroment]/values.yml```;
```yaml
containerPort: 8082
```
3.Ajuste a variável ```port``` do ```livenesProbe``` e do ```readinessProbe``` no arquivo ```./helm/[enviroment]/values.yml```. Exemplo:
```
...
livenessProbe:
  httpGet:
    path: /actuator/health
    port: 8082
readinessProbe:
  httpGet:
    path: /actuator/health
    port: 8082
...
```
---

### Como configurar o endpoint de health-check da minha aplicação (livenessProbe e readinessProbe)?

O **livenessProbe** e o **readinessProbe** são monitores de prontidão, no exemplo abaixo 
devem retornar um HTTP status 200 para o Kubernetes saber que a aplicação está disponivel. 
Nós configuramos no Helm Chart (```./helm/[enviroment]/values.yml```). Veja por favor o trecho abaixo:
```
livenessProbe:
  httpGet:
    path: /actuator/health
    port: 8082
  initialDelaySeconds: 240
  periodSeconds: 10
  timeoutSeconds: 1
  successThreshold: 1
  failureThreshold: 5

readinessProbe:
  httpGet:
    path: /actuator/health
    port: 8082
  initialDelaySeconds: 60
  periodSeconds: 5
  timeoutSeconds: 1
  successThreshold: 1
  failureThreshold: 3
```


Para mais detalhes por favor acesse o link https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-startup-probes/#define-a-liveness-http-request .

---

### Como configuro as variáveis de ambiente da minha aplicação?
Você deverá ajustar a configuração do Helm Chart (```./helm/[enviroment]/environments_variables.yml```). Veja o trecho abaixo:
```
env:
- name: MINHA_NOVA_VARIAVEL_DE_AMBIENTE
  value: VALOR_DA_VARIAVEL

- name: JAVA_OPTS_MEMORY_MIN
  value: 256m

- name: JAVA_OPTS_MEMORY_MAX
  value: 512m

- name: JAVA_OPTS_METASPACE_MIN
  value: 64m

- name: JAVA_OPTS_METASPACE_MAX
  value: 128m
```

*Atenção **NUNCA** declarar variavés de ambiente contendo informações sigilosas como credenciais de banco de dados*

---

### Como eu configuro o autoscale da minha aplicação?
Você deverá ajustar a configuração do Helm Chart (```./helm/[enviroment]/values.yml```). Veja o trecho abaixo:
```
hpa:
  maxReplicas: 2
  minReplicas: 1
  targetCPUUtilizationPercentage: 75
```
OBS: 100% é o tamanho definido em ```resources -> requests -> cpu``` (veja a questão 1).

---

### Como posso alterar o as senhas ou conteúdo dos arquivos de senhas da minha aplicação? 

A alteração pode ser feita em 2 partes: 

- A primeira é a criação ou atualização do segredo no vault da azure.

- A segunda é o ajuste do arquivos de secrets.yaml.


#### Primeira parte - Azure Vault

Os segredos literais ou contido no seu arquivo de configuração são é armazenado em um cofre por ambiente no Azure Vault.     
Acesse o link [Azure Vault](https://portal.azure.com/#blade/HubsExtension/BrowseResource/resourceType/Microsoft.KeyVault%2Fvaults), 
no filtro de busca, informe o nome da sua aplicação e acesse o cofre referente ao ambiente:

O nome do vault é composto por 4 caracteres no prefixo indicando o ambiente (dev-, uat-, prd-) e 
18 caracteres com o início do UUID referênte ao repositório (082c35cf-ab35-426b) que armazena a aplicação. 

 - **Desenvolvimento**: dev-cf02c835-4ab5-6b42
 - **Homologacao**: uat-cf02c835-4ab5-6b42 
 - **Produção**: prd-cf02c835-4ab5-6b42

Para facilitar a identificação são utilizadas TAGs.

- **org_uri**: https://dev.azure.com/DEVOPS-RCHLO/
- **project_id**: 87f82020-cc41-4ba4-a5ed-88c167e9de56
- **project_name**: tribe-nome
- **repository_id**: 516ac5bf-5d57-4571-a978-02ee3c04c343
- **repository_name**: repositorio-nome

Após acessar o vault da aplicação para o ambiente que deseja, clique em ```Settings -> Secrets``` no menu lateral esquerdo.
Localize a chave da secrets referenciadas no arquivo secrets.yaml, edite, salve e realize uma nova entrega.

#### Segunda parte - Secrets.yaml

O arquivo secrets.yaml é separado em 2 partes, a primeira são os segredos literais (texto) são declarados em ```secrets:``` 
e a segunda que são arquivos contendo segredos declarados em  ```secretFiles:```.
```yaml
secrets: 
  - name: ENV_VAR_MINHA_SENHA
    value: "$(MINHA-SENHA)"
  - name: ENV_VAR_MEU_TOKEN
    value: "$(MEU-TOKEN)"
secretFiles:
  - name: service_account.json
    value: "$(GCP-SERVICE-ACCOUNT)"
  - name: ssh.pem
    value: "$(SSH-PEM)"
```

**Quando a secret é um arquivo:** conteúdo desta secret está armazenado em base 64, quando for definir um novo conteúdo para o seu 
arquivo de configuração será necessário armazena-lo em base 64. 

### **Para essa tarefa nunca utilize conversores on-line!** 

***Linux Shell Base64:*** 

- **Codificar** ``` cat application.properties | base64 -w 0 ```
- **Decodificar** ```cat application-properties | base64 -d > application.properties```

***Windows/Linux Openssl:***

- **Codificar** ```openssl base64 -in .env -output dot-env``` (on OSX use `-output`)
- **Decodificar** ```openssl base64 -d -in dot-env -output .env ```
 

## Como alterar onde minha aplicação está sendo entregue?

Esta ação requer 2 passos:
 
**1.Configuração do Context** 

O context pode ser configurado em dois pontos, no values.yml ou no Vault. O valor definido no values.yml sobrescreve o definido no Vault. 

**1.a) Para definir o valor no values.yml:**

Na raiz do projeto acesse o arquivo ```.azuredevops/helm/[enviroment]/values.yml``` e altere a propriedade ```k8sContext```.


**1.b) Para definir o valor no Vault:**

Acesse o link [Azure Vault](https://portal.azure.com/#blade/HubsExtension/BrowseResource/resourceType/Microsoft.KeyVault%2Fvaults), 
no filtro de busca, informe o nome da sua aplicação, acesse o cofre referente ao ambiente clique em secrets e informe um novo valor para a  
a chave **K8S-CONTEXT** 


**2.Configuração nos arquivos de implantação**

Acesse o arquivo de configuração de implantaçao (```./helm/[enviroment]/values.yml```) referente ao ambiente que deseja realizar a movimentação e ajuste as variáveis:

- **httpsHostname**: Hostname para protocolo **https**
- **tlsSecretName**: Nome da secret previamente provisionada contendo o certificado para o TLS.
- **httpHostname**: Hostname para protocolo **http** (retrocampatibilidade com o legado)

*Após alterar o conteúdo de seu arquivo de configuração é necessário realizar a re-implantação de sua aplicação. 
Para isso acesse a pipeline da aplicação, clique em rodar pipeline e selecione a tag referente ao ambiente e versão que
deseja re-implantar.*

---

## Como customizar a url de exposição da minha aplicação?

A url de exposição pe definida no arquivo de configuração de implantaçao (```./helm/[enviroment]/values.yml```) pelas variáveis: 

- **httpsHostname**: Hostname para protocolo **https**
- **httpHostname**: Hostname para protocolo **http** (retrocampatibilidade com o legado)

As urls de exposição são criadas apartir de 3 partes no seguinte formato:

**[nome-aplicacao]-[nome-tribo].[dominio-do-cluster]**

- **[nome-aplicacao]**: aut-centralizacao-precificacaoeletronicos-microservice
- **[nome-tribo]**: automation
- **[dominio-do-cluster]**: rchlo.k8s-dev.riachuelo.net.br
 
Url: ```https://aut-centralizacao-precificacaoeletronicos-microservice-automation.rchlo.k8s-dev.riachuelo.net.br``` 
 
Em alguns casos o nome das aplicações ficam muito grande e precisam ser encurtadas para isso podemos fazer como o exemplo a seguir: 

Url padrão:```aut-centralizacao-precificacaoeletronicos-microservice-automation.rchlo.k8s-dev.riachuelo.net.br```

Url encurtada: ```precificacao-eletronicos-automation.rchlo.k8s-dev.riachuelo.net.br```

Url encurtada (alternativo): ```prcf-eletr-aut.rchlo.k8s-dev.riachuelo.net.br```

**Atenção:** Somente pode ser abreviado (alterado) os dois primeiros segmentos da url **[nome-aplicacao]-[nome-tribo]**. 
O terceiro segmento (*rchlo.k8s-dev.riachuelo.net.br*) **[dominio-do-cluster]** não pode ser abreviado! 

---

## Mais informações

Para mais informações acesse a página do confluence pelo link: [Hefesto Overview](https://r-confluence.riachuelo.net/display/ADT/HEFESTO). 

---


Outros Links:

- Docker [https://www.docker.com/](https://www.docker.com/)
- Kubernetes: [https://kubernetes.io/pt/docs/home/](https://kubernetes.io/pt/docs/home/)
- Helm: [https://helm.sh/](https://helm.sh/)
- Rancher [https://rancher.com/](https://rancher.com/)
- Azure Devops Pipeline YAML [https://docs.microsoft.com/en-us/azure/devops/pipelines/yaml-schema?view=azure-devops&tabs=schema%2Cparameter-schema](https://docs.microsoft.com/en-us/azure/devops/pipelines/yaml-schema)



