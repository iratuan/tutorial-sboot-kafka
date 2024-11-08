![Arquitetura Limpa](/images/f01_capa.png "Capa")

# Tutorial de integração entre spring boot e kafka

- Autor: Iratuã Júnior
- Data: 08/11/2024
- Versão do springboot: 3.3.5
- Versão jdk: 17
- [Respositorio no github](https://github.com/iratuan/tutorial-sboot-kafka)

## Sobre o tutorial
Neste tutorial, exploraremos a construção de uma API em Spring Boot que simula uma transação em um e-commerce, com um fluxo completo de processamento de pedidos e envio de notificações por e-mail. Nossa API será capaz de receber um pedido, persistir suas informações em um banco de dados PostgreSQL, e enviar o pedido para uma fila Kafka, representando o fluxo do status de cada pedido.

Ao ouvir essa fila, um consumidor Kafka atualizará o status do pedido e enviará as informações detalhadas do pedido e do cliente para outra fila, dedicada ao envio de e-mails. Em seguida, um segundo consumidor Kafka escutará essa fila de envio de e-mails e simulará o envio da notificação para o cliente, armazenando a data e hora exatas em que o e-mail foi enviado.

Este projeto proporciona uma visão prática sobre a integração de tecnologias modernas para a criação de sistemas escaláveis e robustos, combinando persistência de dados, mensageria com Kafka e serviços independentes para comunicação assíncrona. Ao final, você terá um exemplo funcional de uma API e-commerce com capacidade de processamento de pedidos e envio de notificações de forma automatizada e eficiente.

## Passo 1 - Criação do projeto e configuração inicial

Para iniciar o desenvolvimento deste projeto, começaremos configurando o projeto no Spring Initializr, selecionando as dependências essenciais para implementar a API com persistência em banco de dados, comunicação assíncrona via Kafka, e suporte para testes e desenvolvimento eficiente. As dependências escolhidas têm funções específicas que facilitarão o desenvolvimento das funcionalidades desejadas.

### Passos Iniciais
No Spring Initializr, configuramos o projeto selecionando as seguintes dependências:

1. **spring-boot-starter-data-jpa**: Permite interações com o banco de dados de forma simplificada, utilizando o Java Persistence API (JPA). É especialmente útil para operações de CRUD (criação, leitura, atualização e exclusão) e facilita o mapeamento objeto-relacional.

2. **spring-boot-devtools**: Inclui ferramentas que otimizam o desenvolvimento, como a recarga automática da aplicação a cada alteração de código, reduzindo o tempo de atualização do servidor durante o desenvolvimento.

3. **spring-boot-docker-compose**: Facilita a integração do Spring Boot com o Docker Compose, permitindo gerenciar e inicializar dependências externas, como bancos de dados e servidores Kafka, diretamente a partir do ambiente Spring Boot.

4. **lombok**: Elimina a necessidade de código boilerplate, gerando automaticamente métodos como `getters`, `setters`, `equals`, `hashCode`, e `toString`. Isso simplifica o código e aumenta sua legibilidade.

5. **spring-boot-starter-test**: Fornece suporte completo para testes unitários e de integração com ferramentas populares como JUnit e Mockito, facilitando a criação de testes para as funcionalidades da API.

6. **spring-kafka-test**: Inclui ferramentas específicas para testar a integração com o Apache Kafka, o que é essencial para garantir que o envio e consumo de mensagens na fila Kafka estejam funcionando conforme esperado.

7. **postgresql**: Inclui o driver necessário para conectar a aplicação Spring Boot ao banco de dados PostgreSQL, que será utilizado para persistir as informações dos pedidos e dos clientes.

8. **spring-boot-starter-web**: Adiciona suporte para construir APIs RESTful, permitindo expor endpoints HTTP para que os clientes possam interagir com a aplicação. Inclui o Spring MVC, que facilita a criação de controladores REST.

Essas dependências foram cuidadosamente selecionadas para suportar o desenvolvimento de uma aplicação robusta, com comunicação assíncrona, integração com banco de dados e funcionalidades de teste, criando uma base sólida para construir uma API de e-commerce escalável e eficiente.

**Exemplo de como ficariam as dependências no arquivo `pom.xml` do seu projeto.**
```xml
<dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.kafka</groupId>
            <artifactId>spring-kafka</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-docker-compose</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.kafka</groupId>
            <artifactId>spring-kafka-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
    </dependencies>
```
### Criação do arquivo docker-compose.yml
Para este projeto, o uso de um arquivo `docker-compose.yml` é fundamental para instanciar e configurar rapidamente serviços externos essenciais, como o banco de dados PostgreSQL e o sistema de mensageria Kafka. O `docker-compose.yml` permite criar e gerenciar esses contêineres de forma simplificada, automatizando o processo de inicialização e configurando a infraestrutura para que o projeto funcione adequadamente em qualquer ambiente. Abaixo está uma explicação detalhada de cada serviço configurado no arquivo.

```yaml
version: '3.8'

services:
  # Serviço de banco de dados PostgreSQL
  postgres:
    image: postgres:15
    container_name: apirest-commerce
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
      POSTGRES_DB: commerce
    ports:
      - "5432:5432"
    volumes:
      - data:/var/lib/postgresql/data

  # Serviço de Zookeeper, necessário para o Kafka
  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    container_name: zookeeper
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000

  # Serviço de Kafka
  kafka:
    image: confluentinc/cp-kafka:latest
    container_name: kafka_broker
    depends_on:
      - zookeeper
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: INSIDE://kafka:9092,OUTSIDE://localhost:29092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: INSIDE:PLAINTEXT,OUTSIDE:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: INSIDE
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    ports:
      - "9092:9092"
      - "29092:29092"


volumes:
  data:

```
### Explicação do arquivo `docker-compose.yml`

1. **version**: Define a versão do Docker Compose. No caso, a versão `3.8` é utilizada, que oferece suporte a recursos modernos e é amplamente compatível com serviços de containers Docker.

2. **services**: Define os serviços que serão iniciados. Neste caso, temos o `postgres`, `zookeeper` e `kafka`.

   #### Serviço de Banco de Dados PostgreSQL (`postgres`)
    - **image**: Utiliza a imagem oficial do PostgreSQL versão `15`, garantindo uma versão estável e atualizada do banco de dados.
    - **container_name**: Nomeia o contêiner como `apirest-commerce` para facilitar a identificação.
    - **environment**: Configura as variáveis de ambiente necessárias para o PostgreSQL, como `POSTGRES_USER`, `POSTGRES_PASSWORD` e `POSTGRES_DB`. Esses valores definem o usuário, a senha e o nome do banco de dados que será criado na inicialização.
    - **ports**: Mapeia a porta `5432` do contêiner para a porta `5432` do host, permitindo o acesso ao banco de dados localmente.
    - **volumes**: Define um volume `data` para persistir os dados do PostgreSQL fora do contêiner, garantindo que os dados sejam mantidos mesmo que o contêiner seja recriado.

   #### Serviço Zookeeper (`zookeeper`)
    - **image**: Utiliza a imagem `confluentinc/cp-zookeeper:latest`, necessária para que o Kafka funcione, pois ele depende do Zookeeper para gerenciar o estado dos clusters.
    - **container_name**: Nomeia o contêiner como `zookeeper`.
    - **environment**: Define variáveis de ambiente para o Zookeeper, como `ZOOKEEPER_CLIENT_PORT` (porta de conexão do cliente) e `ZOOKEEPER_TICK_TIME` (intervalo de sincronização entre os nós do Zookeeper).

   #### Serviço Kafka (`kafka`)
    - **image**: Utiliza a imagem `confluentinc/cp-kafka:latest`, uma das imagens mais estáveis para Kafka, garantindo confiabilidade.
    - **container_name**: Nomeia o contêiner como `kafka_broker`.
    - **depends_on**: Define uma dependência para o contêiner `zookeeper`, garantindo que o Kafka só seja iniciado após o Zookeeper estar pronto.
    - **environment**: Define várias variáveis de ambiente:
        - `KAFKA_BROKER_ID`: Identificador único do broker Kafka.
        - `KAFKA_ZOOKEEPER_CONNECT`: Conecta o Kafka ao Zookeeper.
        - `KAFKA_ADVERTISED_LISTENERS`: Define os listeners internos e externos do Kafka. O listener `INSIDE` é usado para comunicação interna entre contêineres e `OUTSIDE` para comunicação externa (localhost).
        - `KAFKA_LISTENER_SECURITY_PROTOCOL_MAP`: Mapeia os protocolos de segurança para cada listener.
        - `KAFKA_INTER_BROKER_LISTENER_NAME`: Define `INSIDE` como o listener para comunicação entre brokers.
        - `KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR`: Define o fator de replicação do tópico de offsets como `1` (adequado para ambientes de desenvolvimento).
    - **ports**: Mapeia as portas `9092` e `29092` para acesso externo. A porta `9092` é usada para comunicação interna entre contêineres e a `29092` para conexão externa (localhost).

3. **volumes**: Define um volume chamado `data` para persistir os dados do PostgreSQL, evitando a perda de dados ao reiniciar ou recriar o contêiner do banco de dados.

Este arquivo `docker-compose.yml` permite que todos os serviços necessários para o projeto sejam facilmente inicializados com um único comando, simplificando o processo de configuração e garantindo consistência entre ambientes de desenvolvimento e produção.

> Crie o arquivo docker-compose.yml na raiz do seu projeto.

> Crie também um diretório `data` que irá guardar os arquivos do seu banco de dados

Configure também o seu aquivo `application.properties` com as seguintes propriedades:

```properties
spring.application.name=apirest


# Configuração do PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/commerce
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

# Essa configuração permite que o Spring execute o script SQL
spring.jpa.hibernate.ddl-auto=none
spring.sql.init.mode=always

# Configuração do Kafka
spring.kafka.bootstrap-servers=localhost:29092
spring.kafka.consumer.group-id=purchase_order_group
spring.kafka.consumer.auto-offset-reset=earliest

# Serialização do Producer
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer

# Deserialização do Consumer
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer
spring.kafka.consumer.properties.spring.json.trusted.packages=*

```
Vamos detalhar as configurações do projeto Spring Boot. Cada uma dessas configurações é essencial para garantir que o projeto funcione corretamente, especialmente em relação ao banco de dados PostgreSQL e ao Kafka.

### Configuração Geral da Aplicação
```properties
spring.application.name=apirest
```
Essa propriedade define o nome da aplicação no contexto do Spring Boot. Ele pode ser útil para identificá-la em logs e em configurações relacionadas ao Kafka, caso desejemos rastrear a aplicação nos registros.

### Configuração do PostgreSQL
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/commerce
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```
Essas propriedades configuram a conexão com o banco de dados PostgreSQL. Cada uma delas tem uma função específica:
- `spring.datasource.url`: define o caminho de conexão com o banco de dados, que neste caso é o banco `commerce` rodando localmente na porta `5432`.
- `spring.datasource.username` e `spring.datasource.password`: definem o usuário e a senha para acesso ao banco.
- `spring.datasource.driver-class-name`: especifica o driver JDBC necessário para o PostgreSQL.
- `spring.jpa.database-platform`: define o dialeto do Hibernate para o PostgreSQL, o que ajuda o JPA a adaptar as consultas SQL específicas para este banco de dados.

### Script SQL e Inicialização do Banco de Dados
```properties
spring.jpa.hibernate.ddl-auto=none
spring.sql.init.mode=always
```
Essas configurações controlam a execução do script SQL ao iniciar a aplicação:
- `spring.jpa.hibernate.ddl-auto=none`: evita que o Hibernate tente alterar o esquema do banco de dados automaticamente. Com isso, o Spring usará o script `schema.sql` para definir o banco de dados.
- `spring.sql.init.mode=always`: faz com que o Spring execute o script SQL definido em `schema.sql` sempre que a aplicação iniciar. Isso garante que a estrutura do banco seja configurada automaticamente a cada execução.

### Configuração do Kafka
```properties
spring.kafka.bootstrap-servers=localhost:29092
spring.kafka.consumer.group-id=purchase_order_group
spring.kafka.consumer.auto-offset-reset=earliest
```
Essas configurações configuram a conexão e o comportamento dos consumidores e produtores no Kafka:
- `spring.kafka.bootstrap-servers`: especifica o endereço do servidor Kafka. No caso, ele está configurado para o host local (`localhost`) na porta `29092`.
- `spring.kafka.consumer.group-id`: define o grupo de consumidores Kafka. Consumidores no mesmo grupo compartilham as mensagens de uma fila.
- `spring.kafka.consumer.auto-offset-reset=earliest`: define que, se não houver um offset (posição de leitura) armazenado para um consumidor, ele deve começar a consumir desde o início da fila.

### Configurações de Serialização e Desserialização do Kafka
Essas propriedades configuram a forma como as mensagens são serializadas e desserializadas no Kafka:
```properties
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
```
Para o **Producer**:
- `spring.kafka.producer.key-serializer`: define a serialização da chave da mensagem como `String`.
- `spring.kafka.producer.value-serializer`: define a serialização do valor da mensagem como JSON, o que facilita o envio de objetos Java para o Kafka.

```properties
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer
spring.kafka.consumer.properties.spring.json.trusted.packages=*
```
Para o **Consumer**:
- `spring.kafka.consumer.key-deserializer`: define a desserialização da chave da mensagem como `String`.
- `spring.kafka.consumer.value-deserializer`: define a desserialização do valor da mensagem como JSON.
- `spring.kafka.consumer.properties.spring.json.trusted.packages=*`: permite que qualquer pacote seja desserializado, o que ajuda no caso de estarmos enviando objetos de diferentes pacotes Java.

Essas configurações são fundamentais para garantir que nossa aplicação se conecte corretamente ao PostgreSQL e ao Kafka, possibilitando a persistência dos dados e o uso da mensageria.

## Passo 2 - Criação dos pacotes

No próximo passo, vamos organizar o projeto em uma estrutura de pacotes seguindo o padrão arquitetural `layers`, que facilita a organização do código e torna a aplicação mais modular e escalável. Abaixo está a estrutura de pacotes que utilizaremos:

| **Pacote**    | **Descrição**                                                                                                                                                                                                                           |
|---------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **controller** | Contém as classes responsáveis por gerenciar as requisições HTTP recebidas pela API. Essas classes expõem os endpoints e encaminham as requisições para a camada de `service`, retornando as respostas adequadas aos clientes da API. |
| **dto**        | Abriga os objetos de transferência de dados (DTOs), que são utilizados para transportar dados entre as camadas do sistema, garantindo que apenas as informações necessárias sejam expostas e transferidas entre o cliente e a API.      |
| **exception**  | Define as classes de exceção personalizadas da aplicação, que serão utilizadas para lidar com erros e cenários inesperados, oferecendo feedback apropriado ao usuário e facilitando o tratamento de erros na aplicação.                  |
| **model**      | Contém as classes de entidade que representam os objetos de domínio, como `Pedido`, `Cliente` e `Produto`. Essas classes são mapeadas para tabelas no banco de dados e refletem as estruturas de dados essenciais do sistema.           |
| **repository** | Armazena as interfaces de repositório que fazem a comunicação direta com o banco de dados. Essas interfaces são usadas para executar operações de persistência e consulta, utilizando o Spring Data JPA para simplificar o acesso a dados. |
| **service**    | Contém as classes de serviço que encapsulam a lógica de negócios e coordenam as operações entre as diferentes camadas da aplicação. A camada de `service` é responsável por implementar as regras de negócio e orquestrar as funcionalidades da API. |

Esses pacotes estruturam o projeto de forma modular, facilitando a manutenção e o desenvolvimento de novas funcionalidades. Essa divisão em camadas é essencial para uma aplicação limpa e organizada, onde cada camada tem responsabilidades bem definidas e limitações de acesso entre si.

## Passo 3 - Criando as entidades e repositorios

Agora, vamos criar as entidades e os repositórios do projeto. As entidades representam os objetos de domínio do nosso sistema, como `Pedido`, `Cliente` e `Produto`. Elas são mapeadas para tabelas no banco de dados e refletem as estruturas de dados que precisamos persistir. A criação das entidades é fundamental, pois permite que o Spring Data JPA mapeie automaticamente essas classes para as tabelas correspondentes no PostgreSQL, facilitando as operações de armazenamento e consulta.

Em paralelo, criaremos os repositórios para cada uma dessas entidades. Os repositórios são interfaces que herdam do `JpaRepository`, e são responsáveis pela interação direta com o banco de dados, fornecendo métodos prontos para operações de CRUD (Create, Read, Update, Delete). Essa abordagem elimina a necessidade de implementar manualmente a maioria das operações de banco de dados, tornando o código mais limpo e fácil de manter.

Com essa etapa concluída, teremos a base de dados pronta para armazenar e manipular as informações necessárias para o funcionamento da nossa API.

Crie as seguintes classes no pacote model.

**Cliente.java**

```java
package br.com.aygean.apirest.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "clientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    String nome;
    String cpf;
    String email;
    String telefone;
}
```

**Produto.java**
```java
package br.com.aygean.apirest.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "produtos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Produto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private BigDecimal preco;

}
```

**RegistroEnvioEmail.java**
```java
package br.com.aygean.apirest.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "registro_envio_email")
@Data
@NoArgsConstructor
public class RegistroEnvioEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long clienteId;

    private Long pedidoId;

    private LocalDateTime dataEnvio;
}
```

**Pedido.java**
````java
package br.com.aygean.apirest.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "pedidos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String status;

    private LocalDateTime dataCriacao;

    private LocalDateTime dataAtualizacao;

    @ManyToMany
    @JoinTable(
            name = "pedidos_produtos",
            joinColumns = @JoinColumn(name = "pedido_id"),
            inverseJoinColumns = @JoinColumn(name = "produto_id")
    )
    private Set<Produto> produtos = new LinkedHashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;
}
````
A classe `Pedido` merece uma atenção especial, por se tratar da classe central do nosso tutorial.

A classe `Pedido` representa um pedido no nosso sistema e reflete uma tabela do banco de dados chamada `pedidos`. Aqui está uma visão simplificada de cada parte:

1. **Campos Básicos do Pedido**:
    - `id`: Identificador único do pedido, gerado automaticamente pelo banco de dados.
    - `status`: Estado atual do pedido, como "em processamento" ou "aguardando pagamento".
    - `dataCriacao` e `dataAtualizacao`: Registram quando o pedido foi criado e a última vez que foi atualizado.

2. **Relacionamento com Produtos**:
    - Um pedido pode ter vários produtos, e cada produto pode fazer parte de vários pedidos. Para representar isso, usamos uma estrutura chamada `Set<Produto>` para armazenar os produtos relacionados.
    - Esse relacionamento é implementado como muitos-para-muitos, o que significa que a mesma tabela de junção (`pedidos_produtos`) conecta os IDs de pedidos e produtos.

3. **Relacionamento com Cliente**:
    - Cada pedido está ligado a um cliente específico (muitos pedidos para um cliente), e isso é definido com um relacionamento de muitos-para-um.
    - O campo `cliente_id` atua como chave estrangeira, ligando cada pedido a um cliente.

Em resumo, essa classe `Pedido` ajuda a:
- Armazenar informações do pedido, como status e datas.
- Associar o pedido a produtos e clientes no banco de dados.
  Isso permite que o sistema gerencie as informações de cada pedido de forma organizada e eficiente, mantendo o relacionamento entre pedidos, produtos e clientes.

### Criando os repositorios
Agora iremos criar as classes de repositório do projeto. Os repositórios são responsáveis por interagir diretamente com o banco de dados, permitindo a criação, leitura, atualização e exclusão (CRUD) das entidades que definimos, como `Pedido`, `Produto` e `Cliente`.

Ao utilizarmos o Spring Data JPA, podemos definir essas interfaces de repositório para cada entidade e o framework cuidará automaticamente de muitas operações básicas de banco de dados, reduzindo o código necessário. Dessa forma, os repositórios atuarão como uma camada intermediária entre a lógica de negócios e o banco de dados, simplificando o acesso aos dados e mantendo o código mais organizado e desacoplado.

```java
package br.com.aygean.apirest.repository;

import br.com.aygean.apirest.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
}
```

```java
package br.com.aygean.apirest.repository;

import br.com.aygean.apirest.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
}
```

```java
package br.com.aygean.apirest.repository;

import br.com.aygean.apirest.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {
}
```

```java
package br.com.aygean.apirest.repository;

import br.com.aygean.apirest.model.RegistroEnvioEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegistroEnvioEmailRepository extends JpaRepository<RegistroEnvioEmail, Long> {
}
```

Essas classes garantem a integração com o banco de dados.

## Passo 4 - Criando os serviços
Agora que temos nossos repositórios, o próximo passo é criar as classes de serviço. Essas classes de serviço são responsáveis por encapsular a lógica de negócios do projeto, fornecendo métodos que realizam operações e manipulações específicas em nossas entidades, como `Pedido`, `Produto` e `Cliente`.

A camada de serviço é onde aplicamos as regras de negócio, validamos dados e coordenamos a interação entre os repositórios e o restante da aplicação. Em nosso projeto, a camada de serviço irá:

- Gerenciar o fluxo de criação e atualização de pedidos.
- Enviar mensagens para o Kafka, notificando sobre alterações no status do pedido.
- Integrar-se à lógica de envio de e-mails e atualizar o status dos pedidos e registros de envio.

Ao separar essa lógica na camada de serviço, conseguimos manter o código mais organizado e fácil de manter, além de garantir que a lógica de negócio fique desacoplada da camada de apresentação (controllers) e da camada de persistência (repositórios).

```java
package br.com.aygean.apirest.service;


import br.com.aygean.apirest.dto.EmailRequest;
import br.com.aygean.apirest.dto.ListaPedidoRequest;
import br.com.aygean.apirest.exception.ClienteNotFoundException;
import br.com.aygean.apirest.exception.PedidoNotFoundException;
import br.com.aygean.apirest.model.Pedido;
import br.com.aygean.apirest.model.Produto;
import br.com.aygean.apirest.repository.ClienteRepository;
import br.com.aygean.apirest.repository.PedidoRepository;
import br.com.aygean.apirest.repository.ProdutoRepository;
import br.com.aygean.apirest.service.producer.EmailProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PedidoService {

    private static final String TOPICO_PEDIDOS = "pedidos";


    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final ClienteRepository clienteRepository;
    private final EmailProducer emailProducer;

    @Autowired
    private KafkaTemplate<String, Pedido> kafkaTemplate;

    public PedidoService(PedidoRepository pedidoRepository, ProdutoRepository produtoRepository, ClienteRepository clienteRepository, EmailProducer emailProducer) {
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
        this.clienteRepository = clienteRepository;
        this.emailProducer = emailProducer;
    }

    @Transactional
    public Pedido criarPedido(ListaPedidoRequest pedidoRequest) {
        var cliente = clienteRepository.findById(pedidoRequest.clienteId()).orElseThrow(() -> new ClienteNotFoundException());
        Map<Produto, Integer> produtos = pedidoRequest.pedidos().stream()
                .collect(Collectors.toMap(
                        p -> produtoRepository.findById(p.codigoProduto()).get(), // chave: Produto
                        p -> p.quantidade()     // valor: quantidade
                ));
        var pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setProdutos(produtos.keySet());
        pedido.setStatus("EM_PROCESSAMENTO");
        pedido.setDataCriacao(LocalDateTime.now());
        pedido.setDataAtualizacao(LocalDateTime.now());
        pedido = pedidoRepository.save(pedido);
        kafkaTemplate.send(TOPICO_PEDIDOS, pedido);

        // Enviando para a fila de envio de email
        emailProducer.enviarParaFila(new EmailRequest(pedido.getCliente().getId(), pedido.getId()));

        return pedido;
    }

    @Transactional
    public Pedido atualizarStatusPedido(Long id, String novoStatus) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new PedidoNotFoundException("Pedido não encontrado"));
        pedido.setStatus(novoStatus);
        return pedidoRepository.save(pedido);
    }

    public Pedido consultarPedido(Long id) {
        return pedidoRepository.findById(id).orElseThrow(() -> new PedidoNotFoundException("Pedido não encontrado"));
    }
}
```

```java
package br.com.aygean.apirest.service;


import br.com.aygean.apirest.model.Produto;
import br.com.aygean.apirest.repository.ProdutoRepository;
import org.springframework.stereotype.Service;

@Service
public class ProdutoService {
    private static final String TOPICO_PEDIDOS = "pedidos";

    private final ProdutoRepository prepository;


    public ProdutoService(ProdutoRepository prepository) {
        this.prepository = prepository;
    }

    public Produto buscarProdutoPorId(Long id) {
        return prepository.findById(id).get();
    }
}

```


## Passo 5 - Criando os consumers e producers

Agora precisaremos criar os `producers` e `consumers`, que irão se integrar ao `kafka`.

No contexto de sistemas distribuídos e mensageria, como o Kafka, temos três conceitos fundamentais: **consumers**, **producers** e o próprio **Kafka**.

### Kafka
O **Apache Kafka** é uma plataforma de mensageria distribuída, usada para processamento de fluxo de dados em tempo real e troca de mensagens entre sistemas e serviços. Ele foi desenvolvido para lidar com grandes volumes de dados, com alta disponibilidade e tolerância a falhas. No Kafka, as mensagens são organizadas em **tópicos**, que funcionam como “canais” para onde os dados são enviados e de onde podem ser consumidos. O Kafka permite comunicação assíncrona entre sistemas, sendo útil para cenários de processamento em tempo real, registro de logs e coleta de métricas.

### Producers
Os **producers** são responsáveis por **enviar mensagens** para o Kafka. Eles produzem e enviam dados para um tópico específico no Kafka, onde esses dados ficam disponíveis para serem lidos por outros sistemas. No nosso projeto, por exemplo, o producer pode ser configurado para enviar um pedido para o Kafka assim que ele for criado. Isso permite que o status do pedido seja atualizado em outra parte do sistema, de forma assíncrona, sem bloquear a criação do pedido.

### Consumers
Os **consumers** são responsáveis por **ler mensagens** dos tópicos do Kafka. Eles "consomem" as mensagens enviadas pelo producer e processam essas informações conforme necessário. No exemplo do nosso e-commerce, um consumer pode monitorar o tópico de pedidos e, ao receber uma mensagem de novo pedido, atualizar o status para “aguardando pagamento” ou então disparar uma notificação de envio de e-mail. Os consumers permitem que várias ações sejam tomadas com base nos dados produzidos, tudo de maneira independente.

### Exemplo Prático no Projeto
No cenário do nosso projeto:
- O **producer** envia uma mensagem para um tópico no Kafka quando um novo pedido é criado.
- Um **consumer** escuta esse tópico e, ao detectar uma nova mensagem, processa-a para atualizar o status do pedido e gerar uma nova mensagem para o envio de e-mails.
- Outro **consumer**, conectado a um tópico de envio de e-mails, simula o envio de e-mail ao cliente e registra a data e hora de envio.

Essas interações tornam o sistema escalável, desacoplado e eficiente, garantindo que as tarefas sejam realizadas em tempo real e de maneira assíncrona.

**PedidoConsumer.java**
```java
package br.com.aygean.apirest.service.consumer;

import br.com.aygean.apirest.dto.EmailRequest;
import br.com.aygean.apirest.model.Pedido;
import br.com.aygean.apirest.repository.PedidoRepository;
import br.com.aygean.apirest.service.producer.EmailProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PedidoConsumer {

    private static final Logger logger = LoggerFactory.getLogger(PedidoConsumer.class);

    private final PedidoRepository pedidoRepository;

    public PedidoConsumer(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    @Transactional
    @KafkaListener(topics = "pedidos", groupId = "purchase_order_group")
    public void processarPedido(Pedido pedido) {

        Optional<Pedido> pedidoOptional = pedidoRepository.findById(pedido.getId());
        if (pedidoOptional.isPresent()) {
            logger.info("Recebido pedido para processamento: {}", pedido);
            Pedido pedidoExistente = pedidoOptional.get();
            logger.info("Pedido encontrado: {}. Atualizando status para AGUARDANDO_PAGAMENTO", pedidoExistente);

            // Atualiza o status para "AGUARDANDO_PAGAMENTO"
            pedidoExistente.setStatus("AGUARDANDO_PAGAMENTO");
            pedidoRepository.save(pedidoExistente);


            logger.info("Pedido atualizado e salvo: {}", pedidoExistente);
        }
    }
}
```
A classe `PedidoConsumer` é responsável por **consumir mensagens do tópico Kafka `"pedidos"`** e processar o pedido encontrado. Ela busca o pedido no banco de dados e, se o pedido existir, atualiza o status para `"AGUARDANDO_PAGAMENTO"`.

### Pontos principais:

1. **Escutar o Tópico no Kafka**:
    - O método `processarPedido` possui a anotação `@KafkaListener`, configurando-o para escutar o tópico `"pedidos"` no grupo `"purchase_order_group"`.
    - Cada vez que uma mensagem é enviada para o tópico `"pedidos"`, o método é chamado automaticamente, recebendo o objeto `Pedido`.

2. **Busca do Pedido no Banco de Dados**:
    - A classe utiliza o `pedidoRepository` para buscar o pedido no banco de dados com base no `id` fornecido na mensagem.
    - É utilizada a classe `Optional` para verificar a presença do pedido. Se o pedido for encontrado, ele é processado; caso contrário, não ocorre nenhuma atualização.

3. **Atualização do Status do Pedido**:
    - Se o pedido existe, o método atualiza o status do pedido para `"AGUARDANDO_PAGAMENTO"`, indicando que o pedido foi registrado e aguarda o próximo passo.
    - O pedido atualizado é salvo novamente no banco de dados com o `pedidoRepository.save`.

4. **Logs para Acompanhamento**:
    - O `Logger` fornece informações importantes sobre o processamento. A cada etapa, o logger registra o status e as ações realizadas no pedido, como a atualização do status, para facilitar o monitoramento e depuração.

Essa classe, portanto, coordena o processo de atualização dos pedidos ao receber mensagens, mantendo o status atualizado e registrando cada etapa do processamento no banco de dados.


**EmailConsumer.java**
```java
package br.com.aygean.apirest.service.consumer;

import br.com.aygean.apirest.dto.EmailRequest;
import br.com.aygean.apirest.model.RegistroEnvioEmail;
import br.com.aygean.apirest.repository.RegistroEnvioEmailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class EmailConsumer {

    private static final Logger logger = LoggerFactory.getLogger(EmailConsumer.class);
    private final RegistroEnvioEmailRepository registroEnvioEmailRepository;

    public EmailConsumer(RegistroEnvioEmailRepository registroEnvioEmailRepository) {
        this.registroEnvioEmailRepository = registroEnvioEmailRepository;
    }

    @Transactional
    @KafkaListener(topics = "envio_email", groupId = "email_group")
    public void processarEnvioEmail(EmailRequest emailRequest) {
        logger.info("Processando envio de e-mail para Cliente ID: {}, Produto ID: {}", emailRequest.clienteId(), emailRequest.pedidoId());

        RegistroEnvioEmail registro = new RegistroEnvioEmail();
        registro.setClienteId(emailRequest.clienteId());
        registro.setPedidoId(emailRequest.pedidoId());
        registro.setDataEnvio(LocalDateTime.now());

        registroEnvioEmailRepository.save(registro);

        logger.info("Registro de envio de e-mail salvo: {}", registro);
    }
}
```

A classe `EmailConsumer` é responsável por **consumir mensagens** da fila Kafka chamada `"envio_email"`. Sua função principal é registrar o envio de e-mails para os pedidos, armazenando essas informações em um banco de dados para controle e histórico.

### Pontos mais importantes:

1. **Escutar o Tópico no Kafka**:
    - O método `processarEnvioEmail` possui a anotação `@KafkaListener`, que configura a classe para ouvir mensagens enviadas ao tópico `"envio_email"`, dentro do grupo `"email_group"`.
    - Sempre que uma mensagem é postada no tópico `"envio_email"`, o método `processarEnvioEmail` é acionado automaticamente.

2. **Log de Acompanhamento**:
    - O logger registra cada etapa do processo. Primeiramente, ele informa que está processando o envio de e-mail para um pedido específico. Isso ajuda a acompanhar e depurar o fluxo de processamento das mensagens.

3. **Registro do Envio de E-mail**:
    - A classe recebe um `EmailRequest`, que contém dados do cliente e do pedido.
    - Cria-se um objeto `RegistroEnvioEmail` e define-se a data/hora do envio (`dataEnvio`) para capturar o momento do processamento.
    - Este registro é então salvo no banco de dados usando o `registroEnvioEmailRepository`.

4. **Persistência no Banco de Dados**:
    - O uso do `@Transactional` assegura que o processo de gravação do registro de envio de e-mail no banco seja feito de forma atômica. Se ocorrer algum erro durante o salvamento, a transação será revertida automaticamente.
    - Salvar o registro do envio de e-mail no banco permite rastrear quais pedidos receberam notificação, com um registro confiável de data e hora.

Essa classe simplifica a tarefa de gerenciar e rastrear envios de e-mails, usando o Kafka para comunicação assíncrona e salvando os registros no banco de dados.



**EmailProducer.java**
```java
package br.com.aygean.apirest.service.producer;


import br.com.aygean.apirest.dto.EmailRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class EmailProducer {

    private static final Logger logger = LoggerFactory.getLogger(EmailProducer.class);
    private static final String TOPIC = "envio_email";

    private final KafkaTemplate<String, EmailRequest> kafkaTemplate;

    public EmailProducer(KafkaTemplate<String, EmailRequest> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void enviarParaFila(EmailRequest emailRequest) {
        logger.info("Enviando mensagem para a fila {}: {}", TOPIC, emailRequest);
        kafkaTemplate.send(TOPIC, emailRequest);
    }
}
```
A classe `EmailProducer` é responsável por enviar mensagens para o tópico Kafka `"envio_email"`. Quando um email precisa ser enviado, essa classe usa o `KafkaTemplate` para colocar uma mensagem na fila, permitindo que outro serviço (ou consumer) processe a mensagem posteriormente.

### Principais pontos:

1. **Definição do Tópico**:
    - A constante `TOPIC` define o nome do tópico Kafka, `"envio_email"`, para onde as mensagens serão enviadas.

2. **Uso do `KafkaTemplate`**:
    - `KafkaTemplate` é a ferramenta principal para enviar mensagens ao Kafka. Ele é configurado para enviar objetos do tipo `EmailRequest` e é injetado no construtor da classe.
    - O método `enviarParaFila` utiliza o `kafkaTemplate.send` para enviar a mensagem ao tópico definido.

3. **Log de Envio**:
    - Antes de enviar a mensagem, um log registra a operação, incluindo o tópico e o conteúdo da mensagem (`emailRequest`). Esse log é útil para monitorar o fluxo de mensagens e depurar eventuais problemas no envio.

### Funcionamento do Método `enviarParaFila`
- Quando o método `enviarParaFila` é chamado, ele recebe um objeto `EmailRequest`, que contém as informações necessárias para o envio de um email.
- A mensagem é então enviada para o Kafka, onde outros consumidores configurados para escutar o tópico `"envio_email"` poderão processá-la e realizar ações, como registrar o envio de email.

Essa classe facilita a comunicação assíncrona entre os serviços, permitindo que o envio de emails seja tratado em etapas separadas, cada uma processada independentemente.

## Passo 6 - Testando a API 

Agora que finalizamos a implementação da nossa API, é hora de realizar os testes! Vamos garantir que todos os serviços estão em funcionamento e que a estrutura do banco de dados está devidamente carregada.

### Passo a Passo para Teste da API

1. **Iniciar os Containers Docker**:
    - O primeiro passo é iniciar os containers Docker que configuramos no arquivo `docker-compose.yml`. Isso garantirá que o PostgreSQL e o Kafka estejam rodando e prontos para interagir com a aplicação.
    - No terminal, navegue até o diretório do projeto onde está o arquivo `docker-compose.yml` e execute o comando:

      ```bash
      docker-compose up -d
      ```

    - Isso inicializará os containers em segundo plano, permitindo que a aplicação Spring Boot se conecte aos serviços conforme necessário.

2. **Iniciar a Aplicação Spring Boot**:
    - Com os containers Docker em execução, inicie o projeto Spring Boot.
    - Ao iniciar, o Spring carregará automaticamente a estrutura inicial do banco de dados utilizando o arquivo `schema.sql`, configurado para criar as tabelas necessárias (`clientes`, `pedidos`, `produtos`, entre outras).
    - Esse processo garante que o banco de dados terá as tabelas e dados iniciais prontos para uso, facilitando os testes da API.

3. **Testar a API**:
    - Agora, com a aplicação em execução e a estrutura do banco de dados criada, estamos prontos para interagir com os endpoints da API.
    - Utilize um cliente HTTP (como Postman ou cURL) para enviar requisições HTTP e verificar as funcionalidades da API, como a criação de pedidos, atualização de status e envio de mensagens para o Kafka.

Ao seguir esses passos, estaremos prontos para testar o fluxo completo da API, validando a persistência de dados, a interação com o Kafka e a comunicação assíncrona entre os serviços.

## Conclusão do Tutorial

Neste tutorial, aprendemos a construir uma API Spring Boot que simula um fluxo de e-commerce, integrando PostgreSQL para persistência de dados e Kafka para comunicação assíncrona entre serviços. Passamos por todas as etapas necessárias, desde a configuração inicial no Spring Initializr até a criação dos endpoints e repositórios, além de um sistema robusto de mensageria.

Exploramos os conceitos de **consumers** e **producers** para comunicação com Kafka, construímos e persistimos objetos de domínio como `Pedido`, `Produto`, `Cliente` e `RegistroEnvioEmail`, e configuramos um ambiente Docker Compose para inicializar o PostgreSQL e o Kafka. A estrutura arquitetural em camadas também foi implementada para garantir modularidade e organização.

A estruturação em camadas, junto com a separação da lógica de negócios nos serviços e a utilização de DTOs, torna o projeto mais limpo e escalável, facilitando a manutenção e a adição de novas funcionalidades. Ao final, testamos a API e verificamos o correto funcionamento do fluxo de pedidos, desde a criação e processamento até o envio de notificações por e-mail.

### Próximos Passos

A partir daqui, é possível explorar funcionalidades adicionais, como:
- Implementação de autenticação e autorização.
- Desenvolvimento de testes unitários e de integração para cobrir cenários específicos.
- Expansão dos consumidores e produtores Kafka para suportar novos fluxos de mensagens.
- Integração com um serviço de e-mail real para o envio de notificações para os clientes.

### Considerações Finais

Este tutorial fornece uma base sólida para o desenvolvimento de sistemas modernos com Spring Boot, PostgreSQL e Kafka. Ao utilizar tecnologias como Docker, Kafka e PostgreSQL em conjunto com Spring Boot, conseguimos criar um sistema resiliente, escalável e preparado para lidar com grandes volumes de dados em tempo real.

Agradecemos por seguir até aqui! Você agora tem em mãos uma aplicação de e-commerce funcional que demonstra a comunicação assíncrona e a persistência de dados com PostgreSQL e Kafka. Boa sorte com seus futuros projetos, e continue explorando e aprimorando seu conhecimento em desenvolvimento de APIs escaláveis e robustas.

**Atenciosamente `IratuaN Júnior`**.

## Material extra

### Requisições `CURL`

```text
curl -X POST http://localhost:8080/api/pedidos
-H "Content-Type: application/json"
-d '{
  "clienteId": 1,
  "pedidos": [
    {
      "codigoCliente": 1,
      "codigoProduto": 101,
      "quantidade": 2
    },
    {
     "codigoCliente": 1,
     "codigoProduto": 102,
     "quantidade": 1
    }
  ]
}'
{
  "clienteId": 1,
  "pedidos": [
    {
      "codigoCliente": 1,
      "codigoProduto": 1,
      "quantidade": 2
    },
    {
      "codigoCliente": 1,
      "codigoProduto": 3,
      "quantidade": 2
    }
  ]
}

```


```text

curl -X GET "http://localhost:8080/api/produtos/1" -H "Accept: application/json"

```

### Squema.sql
```sql
-- Remove as tabelas pedidos_produtos, registro_envio_email, pedidos, produtos e clientes se elas já existirem, com CASCADE para evitar conflitos
DROP TABLE IF EXISTS public.pedidos_produtos CASCADE;
DROP TABLE IF EXISTS public.registro_envio_email CASCADE;
DROP TABLE IF EXISTS public.pedidos CASCADE;
DROP TABLE IF EXISTS public.produtos CASCADE;
DROP TABLE IF EXISTS public.clientes CASCADE;

-- Criação da tabela clientes, que armazena informações dos clientes
CREATE TABLE public.clientes
(
    id       BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, -- Identificador único do cliente
    nome     VARCHAR(255)       NOT NULL,                         -- Nome do cliente, obrigatório
    cpf      VARCHAR(14) UNIQUE NOT NULL,                         -- CPF único do cliente, obrigatório
    email    VARCHAR(255) UNIQUE,                                 -- E-mail único do cliente, opcional
    telefone VARCHAR(20)                                          -- Telefone do cliente
);

-- Define o proprietário da tabela clientes como o usuário postgres
ALTER TABLE public.clientes
    OWNER TO postgres;

-- Criação da tabela produtos, que armazena os produtos disponíveis para compra
CREATE TABLE public.produtos
(
    preco NUMERIC(38, 2),                                      -- Preço do produto, com precisão de até 38 dígitos e 2 decimais
    id    BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, -- Identificador único do produto
    nome  VARCHAR(255)                                         -- Nome do produto
);

-- Define o proprietário da tabela produtos como o usuário postgres
ALTER TABLE public.produtos
    OWNER TO postgres;

-- Criação da tabela pedidos, que armazena as informações dos pedidos realizados pelos clientes
CREATE TABLE public.pedidos
(
    id               BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, -- Identificador único do pedido
    status           VARCHAR(255),                                        -- Status do pedido, como "em processamento"
    data_criacao     TIMESTAMP,                                           -- Data de criação do pedido
    data_atualizacao TIMESTAMP,                                           -- Data de atualização do pedido
    cliente_id       BIGINT NOT NULL                                      -- Chave estrangeira para o cliente que fez o pedido
);

-- Define o proprietário da tabela pedidos como o usuário postgres
ALTER TABLE public.pedidos
    OWNER TO postgres;

-- Define a chave estrangeira na tabela pedidos para referenciar a tabela clientes
ALTER TABLE public.pedidos
    ADD CONSTRAINT fk_cliente
        FOREIGN KEY (cliente_id) REFERENCES public.clientes (id);

-- Criação da tabela de associação pedidos_produtos para relacionar pedidos com produtos
CREATE TABLE public.pedidos_produtos
(
    pedido_id  BIGINT NOT NULL,                              -- ID do pedido, chave estrangeira para pedidos
    produto_id BIGINT NOT NULL,                              -- ID do produto, chave estrangeira para produtos
    PRIMARY KEY (pedido_id, produto_id),                     -- Define a chave composta para evitar duplicação de associação
    FOREIGN KEY (pedido_id) REFERENCES public.pedidos (id),  -- Chave estrangeira para pedidos
    FOREIGN KEY (produto_id) REFERENCES public.produtos (id) -- Chave estrangeira para produtos
);

-- Define o proprietário da tabela de associação pedidos_produtos como o usuário postgres
ALTER TABLE public.pedidos_produtos
    OWNER TO postgres;

-- Tabela para registrar os envios de e-mail
CREATE TABLE public.registro_envio_email
(
    id         BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, -- Identificador único do registro
    cliente_id BIGINT    NOT NULL,                                  -- Código do cliente
    pedido_id  BIGINT    NOT NULL,                                  -- Código do pedido
    data_envio TIMESTAMP NOT NULL                                   -- Data e hora do envio do e-mail
);

-- Define o proprietário da tabela registro_envio_email como o usuário postgres
ALTER TABLE public.registro_envio_email
    OWNER TO postgres;

-- Chave estrangeira para cliente
ALTER TABLE public.registro_envio_email
    ADD CONSTRAINT fk_cliente_email
        FOREIGN KEY (cliente_id) REFERENCES public.clientes (id);

-- Chave estrangeira para pedido
ALTER TABLE public.registro_envio_email
    ADD CONSTRAINT fk_pedido_email
        FOREIGN KEY (pedido_id) REFERENCES public.pedidos (id);

-- Inserção de dados de exemplo na tabela clientes
INSERT INTO public.clientes (nome, cpf, email, telefone)
VALUES ('João da Silva', '123.456.789-00', 'joao.silva@example.com', '(11) 91234-5678');

-- Inserção de dados de exemplo na tabela produtos
INSERT INTO public.produtos (preco, nome)
VALUES (19.99, 'Hamburger'); -- Produto 1: Hamburger com preço de 19.99
INSERT INTO public.produtos (preco, nome)
VALUES (7.00, 'Refrigerante de cola'); -- Produto 2: Refrigerante de cola com preço de 7.00
INSERT INTO public.produtos (preco, nome)
VALUES (9.99, 'Sobremesa de chocolate'); -- Produto 3: Sobremesa de chocolate com preço de 9.99

```