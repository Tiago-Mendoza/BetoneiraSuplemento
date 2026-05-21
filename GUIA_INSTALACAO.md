# GUIA DE INSTALAÇÃO E EXECUÇÃO

## Pré-requisitos

- **Java 17+** instalado
- **Git** instalado (para clonar o repositório)
- **Navegador moderno** (Chrome, Firefox, Safari, Edge)

## Passo 1: Compilar o Projeto

### Windows (PowerShell)
```powershell
cd c:\Users\matheus.lviana\Desktop\Git\BetoneiraSuplemento
.\mvnw.cmd clean compile
```

### Linux/Mac (Terminal)
```bash
cd ~/BetoneiraSuplemento
./mvnw clean compile
```

### O que acontece:
- Maven faz download das dependências
- Compila o código Java
- Gera arquivos .class

**Tempo esperado**: 2-5 minutos (primeira vez pode levar mais)

## Passo 2: Executar os Testes

### Rodar todos os testes
```bash
./mvnw test
```

### Rodar apenas testes de autenticação
```bash
./mvnw test -Dtest=AuthFlowIntegrationTests
```

### O que valida:
✅ Cadastro válido salva usuário  
✅ Emails duplicados são rejeitados  
✅ CPFs duplicados são rejeitados  
✅ Senhas diferentes são rejeitadas  
✅ Login com usuário válido funciona  

**Resultado esperado**: 
```
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
```

## Passo 3: Iniciar a Aplicação

### Windows (PowerShell)
```powershell
.\mvnw.cmd spring-boot:run
```

### Linux/Mac
```bash
./mvnw spring-boot:run
```

### Esperado na saída (últimas linhas):
```
2026-05-21 XX:XX:XX.XXX  INFO 1234 --- [restartedMain] 
c.c.b.BetoneiraSuplementosApplication    : Started BetoneiraSuplementosApplication 
in X.XXX seconds (JVM running for X.XXX)
```

### Aplicação rodando em:
```
http://localhost:8080
```

## Passo 4: Acessar a Aplicação

### URLs Principais

| Funcionalidade | URL |
|---|---|
| Home | http://localhost:8080/home |
| Produtos | http://localhost:8080/produtos |
| Cadastro | http://localhost:8080/cadastro |
| Login | http://localhost:8080/login |
| Carrinho | http://localhost:8080/carrinho |
| Admin | http://localhost:8080/admin/pedidos |
| H2 Console | http://localhost:8080/h2-console |

## Passo 5: Testar Fluxo Completo

### 5.1 Criar uma Conta

1. Acesse http://localhost:8080/cadastro
2. Preencha os campos na ordem:
   ```
   Nome: João Silva
   CPF: 123.456.789-01
   Email: joao@teste.com
   Data: 1990-01-15
   Celular: 11999999999
   Fixo: 1133334444
   Gênero: Masculino
   Senha: Abc123456
   Confirmar: Abc123456
   ```
3. Clique em "Cadastrar"
4. Deverá ver: "Cadastro realizado com sucesso. Faça seu login."

### 5.2 Fazer Login

1. Na página de login, digite:
   ```
   Email: joao@teste.com
   Senha: Abc123456
   ```
2. Clique em "Entrar"
3. Deverá ser redirecionado para /home
4. Navbar agora mostra seu email + opção de logout

### 5.3 Testar Validações

**Testar senhas diferentes**:
1. Vá para /cadastro
2. Preencha tudo
3. Senha: "123456"
4. Confirmar: "654321"
5. Clique em "Cadastrar"
6. Resultado: Erro "As senhas não coincidem."

**Testar email duplicado**:
1. Vá para /cadastro
2. Preencha com um email já cadastrado
3. Clique em "Cadastrar"
4. Resultado: Erro "Este e-mail já está em uso."

## Passo 6: Acessar o Console H2

### URL
```
http://localhost:8080/h2-console
```

### Credenciais (pré-configuradas)
```
Driver: org.h2.Driver
URL: jdbc:h2:mem:betoneira
User: admin
Password: admin
```

### Queries Úteis

**Ver todos os usuários**:
```sql
SELECT * FROM USERS;
```

**Ver senha encoded de um usuário**:
```sql
SELECT email, password_hash FROM users WHERE email = 'joao@teste.com';
```

**Ver roles de um usuário**:
```sql
SELECT u.email, r.role FROM users u 
LEFT JOIN user_roles r ON u.id = r.user_id 
WHERE u.email = 'joao@teste.com';
```

**Ver todos os pedidos**:
```sql
SELECT * FROM ORDERS;
```

## Parar a Aplicação

### Windows
Pressione `Ctrl + C` no PowerShell onde a aplicação está rodando

### Linux/Mac
Pressione `Ctrl + C` no Terminal onde a aplicação está rodando

## Troubleshooting

### Erro: "Port 8080 already in use"
**Solução 1**: Mude a porta em `application.properties`
```properties
server.port=8081
```

**Solução 2**: Encontre e mate o processo
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -i :8080
kill -9 <PID>
```

### Erro: "Java not found"
**Solução**: Instale Java 17+ ou defina JAVA_HOME
```bash
# Windows (PowerShell como Admin)
[Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Java\jdk-17", "Machine")

# Linux/Mac
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
```

### Erro: "Maven not found"
**Solução**: Use o mvnw.cmd (Windows) ou ./mvnw (Linux/Mac)
```bash
# O projeto tem seus próprios scripts Maven
./mvnw clean install
```

### Testes Falhando
**Solução**: Limpe o build e tente novamente
```bash
./mvnw clean test
```

## Estrutura de Pastas Importante

```
BetoneiraSuplemento/
├── src/
│   ├── main/
│   │   ├── java/com/curso/boot/
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   └── CustomUserDetailsService.java ✨ NOVO
│   │   │   ├── web/controller/
│   │   │   │   └── AuthController.java ✏️ MODIFICADO
│   │   │   ├── dto/
│   │   │   │   └── RegistrationForm.java ✏️ MODIFICADO
│   │   │   └── service/
│   │   │       └── UserService.java
│   │   └── resources/
│   │       ├── templates/
│   │       │   ├── cadastro.html ✏️ MODIFICADO
│   │       │   ├── login.html ✏️ MODIFICADO
│   │       │   └── [outros].html ✏️ MODIFICADOS
│   │       └── application.properties
│   └── test/
│       └── java/com/curso/boot/
│           └── AuthFlowIntegrationTests.java ✏️ MODIFICADO
├── pom.xml
├── mvnw
├── mvnw.cmd
└── DOCUMENTACAO/
    ├── ALTERACOES_IMPLEMENTADAS.md
    ├── GUIA_TESTES.md
    ├── DETALHAMENTO_MUDANCAS.md
    ├── DIAGRAMA_FLUXO.md
    ├── README_EXECUTIVO.md
    └── GUIA_INSTALACAO.md (este arquivo)
```

## Variáveis de Ambiente (Opcional)

Você pode configurar via properties ou variáveis de ambiente:

```properties
# src/main/resources/application.properties

# Servidor
server.port=8080

# Banco de Dados H2
spring.datasource.url=jdbc:h2:mem:betoneira;MODE=MySQL
spring.datasource.username=admin
spring.datasource.password=admin

# Admin Padrão
app.admin.email=admin@betoneira.com
app.admin.password=123456
app.admin.name=Administrador
```

## Performance e Otimização

### Dicas
1. **Primeiro start**: Pode levar 5-10 segundos (Maven faz download)
2. **Próximos starts**: 2-3 segundos (Java em cache)
3. **Devtools ativado**: Permite reload automático em dev

### Monitorar Performance
```bash
# Ver logs de performance
./mvnw spring-boot:run -Dspring-boot.run.arguments="--debug"
```

## Deploy em Produção (Futuro)

### Criar JAR executável
```bash
./mvnw clean package
```

Resultado: `target/BetoneiraSuplementos-0.0.1-SNAPSHOT.jar`

### Rodar o JAR
```bash
java -jar target/BetoneiraSuplementos-0.0.1-SNAPSHOT.jar
```

## Contato e Suporte

Se encontrar problemas:

1. Verifique o GUIA_TESTES.md
2. Limpe o cache: `./mvnw clean`
3. Verifique Java: `java -version`
4. Verifique a porta 8080

---

**Pronto para começar! 🚀**

Execute `./mvnw spring-boot:run` e acesse http://localhost:8080
