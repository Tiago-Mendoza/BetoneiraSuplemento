# Betoneira Suplementos

E-commerce de suplementos desenvolvido com Spring Boot, com vitrine de produtos, carrinho, checkout, autenticação de usuários, painel administrativo e endpoints REST para catálogo e pedidos.

## Visão Geral

O projeto simula uma loja virtual de suplementos com experiência completa de compra no navegador. A aplicação permite navegar pelo catálogo, adicionar itens ao carrinho, finalizar pedidos, acompanhar compras e administrar produtos e pedidos em uma área restrita.

## Funcionalidades

- Catálogo de produtos com itens em destaque
- Carrinho de compras com atualização de quantidade
- Cadastro e login de usuários
- Checkout com finalização de pedido
- Área de "meus pedidos" para clientes autenticados
- Painel administrativo para gestão de produtos e pedidos
- API REST pública para produtos
- API REST autenticada para consulta de pedidos

## Tecnologias

- Java 17
- Spring Boot 3
- Spring MVC
- Spring Security
- Thymeleaf
- Spring Data JPA / Hibernate
- H2 Database
- Maven

## Como Executar

### Pré-requisitos

- Java 17 instalado

### Rodando localmente

No Windows:

```bat
.\mvnw.cmd spring-boot:run
```

No Linux/macOS:

```bash
./mvnw spring-boot:run
```

Depois, acesse:

- Aplicação: `http://localhost:8080`
- Console H2: `http://localhost:8080/h2-console`

## Acessos de Desenvolvimento

### Admin padrão

- E-mail: `admin@betoneira.com`
- Senha: `123456`

### Banco H2

- JDBC URL: `jdbc:h2:mem:betoneira`
- Usuário: `admin`
- Senha: `admin`

## Dados Iniciais

O projeto carrega produtos automaticamente ao iniciar a aplicação usando `src/main/resources/data.sql`.

Como o banco está configurado em memória, os dados são recriados sempre que a aplicação sobe novamente.

## Rotas Principais

- `/` ou `/home` - página inicial
- `/produtos` - catálogo completo
- `/carrinho` - carrinho de compras
- `/checkout` - finalização do pedido
- `/meus-pedidos` - histórico do usuário autenticado
- `/admin` - painel administrativo

## API REST

### Produtos

- `GET /api/produtos`
- `GET /api/produtos/destaque`
- `GET /api/produtos/{id}`

### Pedidos

- `GET /api/pedidos`
- `GET /api/pedidos/{orderNumber}`

## Estrutura do Projeto

```text
src/main/java/com/curso/boot
|- config/            # configuracoes de seguranca
|- domain/            # entidades de dominio
|- service/           # regras de negocio
|- web/controller/    # controllers MVC e REST

src/main/resources
|- templates/         # paginas Thymeleaf
|- static/css/        # estilos da aplicacao
|- application.properties
|- data.sql
```

## Testes

Para executar os testes:

```bat
.\mvnw.cmd test
```

## Observações

- O projeto usa `Thymeleaf` para renderização das páginas.
- As rotas de pedidos exigem autenticação.
- A área administrativa é protegida por perfil `ADMIN`.
