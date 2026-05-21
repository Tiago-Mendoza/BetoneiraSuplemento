# Diagrama de Fluxo de Autenticação

## ANTES DAS ALTERAÇÕES (❌ Quebrado)

```
┌─────────────────┐
│  Tela Cadastro  │
└────────┬────────┘
         │
         ├─ Nome, CPF, Email, Data, Telefones, Gênero
         ├─ Senha (máx 6 dígitos)
         └─ Sem confirmação de senha
         │
         ▼
┌─────────────────────────────┐
│  AuthController.cadastrar   │
└────────┬────────────────────┘
         │
         ├─ Validações básicas
         ├─ Sem validação de confirmPassword
         └─ UserService.register()
         │
         ▼
┌─────────────────────────────┐
│  UserService.register()     │
└────────┬────────────────────┘
         │
         ├─ Cria User
         ├─ Encode senha com BCrypt
         └─ Salva no H2
         │
         ▼
┌─────────────────┐
│ Usuário no BD ✓ │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Tela Login     │
└────────┬────────┘
         │
         ├─ Email
         └─ Senha
         │
         ▼
┌─────────────────────────────┐
│   /login (POST)             │
│   Spring Security FormLogin │
└────────┬────────────────────┘
         │
         ├─ Procura UserDetailsService
         ├─ ❌ NÃO ENCONTRADO!
         ├─ Erro: No bean named 'userDetailsService'
         └─ Falha na autenticação
         │
         ▼
┌──────────────────────────────┐
│  ❌ Login Falha              │
│  "E-mail ou senha inválidos" │
└──────────────────────────────┘
```

---

## DEPOIS DAS ALTERAÇÕES (✅ Funcionando)

```
┌──────────────────────────────────┐
│     Tela Cadastro (Melhorada)    │
└────────┬─────────────────────────┘
         │
         ├─ 1. Nome
         ├─ 2. CPF
         ├─ 3. Email
         ├─ 4. Data de Nascimento
         ├─ 5. Telefone Celular
         ├─ 6. Telefone Fixo
         ├─ 7. Gênero
         ├─ 8. Senha (6-15 caracteres)
         ├─ 9. Confirmar Senha ✨ NOVO
         └─ Sem ícone de lupa ✨
         │
         ▼
┌─────────────────────────────────────────────┐
│  AuthController.cadastrar                   │
└────────┬────────────────────────────────────┘
         │
         ├─ Validações básicas
         ├─ Validação de CPF (11 dígitos)
         ├─ Validação de Telefone
         ├─ ✨ NOVO: Validação de confirmPassword
         │  └─ password == confirmPassword ?
         │     ├─ SIM → Continua
         │     └─ NÃO → Erro "As senhas não coincidem"
         └─ UserService.register()
         │
         ▼
┌─────────────────────────────────────────┐
│  UserService.register()                 │
└────────┬────────────────────────────────┘
         │
         ├─ Verifica duplicata de Email
         ├─ Verifica duplicata de CPF
         ├─ Cria novo User
         ├─ Encode senha com BCryptPasswordEncoder
         └─ Salva no H2 DB
         │
         ▼
┌──────────────────────────────┐
│  ✅ Usuário Salvo            │
│  Redireciona para /login     │
│  ?registered                 │
└──────────────────────────────┘
         │
         ▼
┌──────────────────────────────┐
│     Tela Login (Limpa)       │
│  - Sem lupa                  │ ✨
│  - Sem "Esqueci senha"       │ ✨
└────────┬─────────────────────┘
         │
         ├─ Email: joao@teste.com
         └─ Senha: Abc123
         │
         ▼
┌────────────────────────────────────────┐
│   /login (POST)                        │
│   Spring Security FormLogin            │
└────────┬───────────────────────────────┘
         │
         ├─ Extrai username (email) e password
         ├─ Chama AuthenticationProvider
         └─ AuthenticationProvider precisa carregar usuário
         │
         ▼
┌────────────────────────────────────────────┐
│  ✨ CustomUserDetailsService               │
│  (NOVO SERVIÇO IMPLEMENTADO)               │
└────────┬───────────────────────────────────┘
         │
         ├─ loadUserByUsername(email)
         ├─ Chama userService.findByEmail()
         ├─ Busca no H2: SELECT * FROM users WHERE email = ?
         │
         ▼
┌──────────────────────────────┐
│  ✅ Usuário Encontrado       │
│  User(                       │
│    email: joao@teste.com     │
│    password_hash: $2a$1...   │ (BCrypt)
│    roles: [ROLE_USER]        │
│  )                           │
└──────────────────────────────┘
         │
         ▼
┌────────────────────────────────────────────┐
│  Convertido para UserDetails               │
│  User.builder()                            │
│    .username(email)                        │
│    .password(password_hash)                │
│    .authorities([ROLE_USER])               │
│    .build()                                │
└────────┬───────────────────────────────────┘
         │
         ▼
┌────────────────────────────────────────────┐
│  Spring Security Valida                    │
│  BCryptPasswordEncoder.matches(             │
│    "Abc123",                               │ (senha digitada)
│    "$2a$1..."                              │ (hash no BD)
│  )                                         │
└────────┬───────────────────────────────────┘
         │
         ├─ Correspondem? ✅ SIM
         └─ Cria Authentication token
         │
         ▼
┌──────────────────────────────────────────┐
│  ✅ AUTENTICAÇÃO BEM-SUCEDIDA!           │
│                                          │
│  Authentication:                         │
│    Principal: joao@teste.com             │
│    Roles: [ROLE_USER]                    │
│    Authenticated: true                   │
└──────────────────────────────────────────┘
         │
         ▼
┌──────────────────────────────────────────┐
│  Redireciona para /home                  │
│  (defaultSuccessUrl)                     │
└──────────────────────────────────────────┘
         │
         ▼
┌──────────────────────────────────────────┐
│  ✅ Login Bem-sucedido!                  │
│                                          │
│  - Session criada                        │
│  - Cookie de sessão enviado              │
│  - Usuário autenticado                   │
│  - Pode acessar recursos protegidos      │
└──────────────────────────────────────────┘
```

---

## FLUXO DE VALIDAÇÃO DE CADASTRO

```
┌─────────────────────────────────┐
│  Envio do Formulário /cadastro  │
└────────┬────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────┐
│  AuthController.validateRegistrationData()  │
└────────┬────────────────────────────────────┘
         │
         ├─ Validar CPF
         │  ├─ Remove não-dígitos: \D
         │  ├─ Verifica se tem 11 dígitos
         │  └─ Erro se != 11 dígitos
         │
         ├─ Validar Telefone Celular
         │  ├─ Remove não-dígitos
         │  ├─ Verifica: 10 ≤ tamanho ≤ 11
         │  └─ Erro se fora do range
         │
         ├─ Validar Telefone Fixo (opcional)
         │  ├─ Se preenchido, valida como celular
         │  └─ Ignorar se vazio
         │
         └─ ✨ Validar Confirmação de Senha
            ├─ password != null && confirmPassword != null ?
            ├─ password.equals(confirmPassword) ?
            │  ├─ SIM → OK, continua
            │  └─ NÃO → Adiciona erro ao bindingResult
            │     └─ Campo: confirmPassword
            │     └─ Mensagem: "As senhas não coincidem."
            └─ Retorna para formulário com erros

┌──────────────────────────┐
│  Se tem erros:           │
│  ❌ Retorna cadastro.html │
│     com mensagens        │
└──────────────────────────┘
         │
┌──────────────────────────────┐
│  Se sem erros:               │
│  ✅ Chama UserService.register()  │
└──────────────────────────────┘
```

---

## FLUXO DE LOGIN COM NOVO UserDetailsService

```
Passo 1: POST /login com (email, senha)
         │
         ▼
Passo 2: Spring Security intercepta
         │
         ├─ Parametrização do SecurityConfig:
         │  ├─ loginProcessingUrl("/login")
         │  ├─ usernameParameter("email")  ← Mapeia para "email"
         │  ├─ passwordParameter("password")
         │  └─ defaultSuccessUrl("/home")
         │
         ▼
Passo 3: Spring procura AuthenticationProvider
         ├─ Encontra DaoAuthenticationProvider
         └─ Este procura UserDetailsService
         │
         ▼
Passo 4: ✨ CustomUserDetailsService.loadUserByUsername()
         ├─ Recebe: "joao@teste.com"
         ├─ Chama: userService.findByEmail("joao@teste.com")
         ├─ Query: SELECT u FROM User u WHERE u.email = ?
         │
         ├─ Se não encontrado:
         │  └─ Lança UsernameNotFoundException
         │
         ├─ Se encontrado:
         │  └─ Retorna UserDetails com:
         │     ├─ username: joao@teste.com
         │     ├─ password: $2a$... (BCrypt hash)
         │     └─ authorities: [ROLE_USER]
         │
         ▼
Passo 5: DaoAuthenticationProvider valida
         ├─ Pega senha digitada: "Abc123"
         ├─ Pega hash do BD: "$2a$..."
         ├─ Usa BCryptPasswordEncoder.matches():
         │
         ├─ BCrypt: Abc123 + $2a$... = $2a$... ?
         │  ├─ SIM → Senha correta ✅
         │  └─ NÃO → Senha incorreta ❌
         │
         ▼
Passo 6: Se correto:
         ├─ Cria UsernamePasswordAuthenticationToken
         ├─ Define como Authentication no SecurityContext
         ├─ Redireciona para defaultSuccessUrl("/home")
         └─ ✅ Usuário logado com sucesso!

         Se incorreto:
         ├─ Falha na autenticação
         ├─ Redireciona para failureUrl("/login?error")
         └─ ❌ Mostra mensagem de erro
```

---

## ESTRUTURA DE DADOS NO BANCO

### Tabela: USERS
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    cpf VARCHAR(11) NOT NULL UNIQUE,
    nome VARCHAR(100) NOT NULL,
    data_nascimento DATE,
    telefone_celular VARCHAR(11),
    telefone_fixo VARCHAR(11),
    genero VARCHAR(50),
    password_hash VARCHAR(255) NOT NULL
);
```

### Tabela: USER_ROLES (ElementCollection)
```sql
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### Exemplo de Dados:
```
USERS:
id  | email            | cpf        | nome         | password_hash
1   | joao@teste.com   | 12345678901| João Silva   | $2a$10$abc123...

USER_ROLES:
user_id | role
1       | ROLE_USER
```

---

## COMPARAÇÃO: ANTES vs DEPOIS

| Aspecto | Antes ❌ | Depois ✅ |
|---------|----------|----------|
| UserDetailsService | Não implementado | ✅ CustomUserDetailsService |
| Ordem de campos | Email, CPF, Nome, Data... | Nome, CPF, Email, Data... |
| Limite de senha | Máx 6 caracteres | 6-15 caracteres |
| Confirmação | Sem confirmPassword | ✅ Com confirmPassword |
| Validação | Sem validação | ✅ Valida coincidência |
| Ícone de Lupa | Presente | ✅ Removido |
| Link "Esqueci Senha" | Presente | ✅ Removido |
| Login | ❌ Não funciona | ✅ Funciona |
| Mensagens de Erro | Genéricas | Específicas por campo |

---

**Fluxo de Autenticação Implementado com Sucesso! ✅**
