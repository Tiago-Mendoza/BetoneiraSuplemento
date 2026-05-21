# Guia de Testes - Betoneira Suplementos

## 🔍 VERIFICAÇÕES VISUAIS

### 1. Verificar Remoção da Lupa
- [ ] Acessar http://localhost:8080/home
- [ ] Acessar http://localhost:8080/produtos
- [ ] Acessar http://localhost:8080/carrinho
- [ ] Acessar http://localhost:8080/sobre-nos
- [ ] **Resultado esperado**: Navbar possui apenas 2 ícones (Carrinho + Login)

### 2. Verificar Remoção do Link "Esqueci minha senha"
- [ ] Acessar http://localhost:8080/login
- [ ] **Resultado esperado**: 
  - Apenas um link em baixo do formulário: "Cadastre-se"
  - Link "Esqueci minha senha" foi removido

### 3. Verificar Reordenação dos Campos no Cadastro
- [ ] Acessar http://localhost:8080/cadastro
- [ ] **Ordem dos campos deve ser**:
  1. Nome Completo
  2. CPF
  3. Email
  4. Data de Nascimento (date picker)
  5. Telefone Celular
  6. Telefone Fixo
  7. Gênero (3 radio buttons)
  8. Criar Senha (6-15 caracteres)
  9. Confirmar Senha (6-15 caracteres)
  10. Botão "Cadastrar"

### 4. Verificar Validação de Confirmação de Senha
- [ ] Acessar http://localhost:8080/cadastro
- [ ] Preencher todos os campos
- [ ] Digite "123456" em "Criar Senha"
- [ ] Digite "654321" em "Confirmar Senha"
- [ ] Clique em "Cadastrar"
- [ ] **Resultado esperado**: 
  - Formulário retorna com erro em "Confirmar Senha"
  - Mensagem: "As senhas não coincidem."

### 5. Verificar Sucesso no Cadastro com Senhas Iguais
- [ ] Acessar http://localhost:8080/cadastro
- [ ] Preencher todos os campos:
  - Nome: "João Silva"
  - CPF: "123.456.789-01" 
  - Email: "joao@teste.com"
  - Data: "1990-01-15"
  - Celular: "11999999999"
  - Fixo: "1133334444" (opcional)
  - Gênero: "Masculino"
  - Senha: "Abc123" (6+ caracteres)
  - Confirmar: "Abc123"
- [ ] Clique em "Cadastrar"
- [ ] **Resultado esperado**: 
  - Redirecionado para /login?registered
  - Mensagem: "Cadastro realizado com sucesso. Faça seu login."

### 6. Verificar Login com Usuário Recém-Cadastrado
- [ ] Na página de login, digitar:
  - Email: "joao@teste.com"
  - Senha: "Abc123"
- [ ] Clique em "Entrar"
- [ ] **Resultado esperado**: 
  - ✅ Autenticação bem-sucedida
  - Redirecionado para /home
  - Navbar mostra "Meus pedidos" e "Logout"

### 7. Verificar Limite de Caracteres da Senha
- [ ] Acessar http://localhost:8080/cadastro
- [ ] Tentar digitar mais de 15 caracteres no campo senha
- [ ] **Resultado esperado**: 
  - Campo limita a 15 caracteres
  - HTML: `maxlength="15"`

### 8. Verificar Mínimo de 6 Caracteres
- [ ] Acessar http://localhost:8080/cadastro
- [ ] Tentar criar senha com menos de 6 caracteres
- [ ] Clicar em "Cadastrar"
- [ ] **Resultado esperado**: 
  - Mensagem de erro: "A senha deve conter entre 6 e 15 caracteres."

---

## 🧪 TESTES AUTOMATIZADOS

### Executar Testes
```bash
./mvnw test -Dtest=AuthFlowIntegrationTests
```

### Testes que Devem Passar
- ✅ `cadastroValidoSalvaUsuarioNoBanco`
  - Validar que usuário é salvo com email, CPF e senha encoded
  
- ✅ `cadastroNaoPermiteEmailOuCpfDuplicado`
  - Validar que não permite cadastro com email duplicado
  - Validar que não permite cadastro com CPF duplicado

- ✅ `loginAutenticaComEmailESenha`
  - Validar que usuário pode fazer login com email e senha
  - Validar que é redirecionado para /home
  - Validar que é autenticado com username = email

- ✅ `cadastroRejeicaSenhasNaoCoincidentes` (NOVO)
  - Validar que rejeita quando password != confirmPassword
  - Validar mensagem de erro no campo confirmPassword

---

## 🔐 TESTE DE SEGURANÇA

### Verificar Encoding de Senha
1. Abra o banco H2 em http://localhost:8080/h2-console
2. Execute query:
```sql
SELECT email, password_hash FROM users WHERE email = 'joao@teste.com';
```
3. **Resultado esperado**: 
   - `password_hash` começa com `$2a$` (BCrypt hash)
   - `password_hash` ≠ "Abc123" (senha não é armazenada em plain text)

---

## 📱 TESTE EM DIFERENTES NAVEGADORES

- [ ] Chrome/Chromium
- [ ] Firefox
- [ ] Safari
- [ ] Edge

**Verificar**:
- [ ] Date picker funciona em todos os navegadores
- [ ] Validação HTML5 funciona
- [ ] Responsividade mantida

---

## 🐛 TROUBLESHOOTING

### Erro: "UserDetailsService not found"
- **Solução**: Garantir que `CustomUserDetailsService.java` foi criado na pasta `config`

### Erro: "As senhas não coincidem" mesmo sendo iguais
- **Verificar**: Espaços em branco extras
- **Solução**: Validar sem trim()

### Login não funciona após cadastro
- **Verificar**: 
  1. Se o `CustomUserDetailsService` está sendo injetado
  2. Se a senha foi salva com hash (BCrypt)
  3. Se o Spring Security está usando o serviço correto

### Ícone de lupa ainda aparece
- **Solução**: Limpar cache do navegador (Ctrl+F5)

---

## ✅ CHECKLIST FINAL

- [ ] Todos os arquivos foram salvos
- [ ] Projeto compila sem erros
- [ ] Todos os testes passam
- [ ] Lupa removida de todos os templates
- [ ] Link "Esqueci minha senha" removido
- [ ] Campos do cadastro reordenados
- [ ] Campo "Confirmar senha" adicionado e validado
- [ ] CustomUserDetailsService implementado
- [ ] Login funciona com novos usuários
- [ ] Limite de senha 6-15 caracteres funciona
- [ ] UserDetailsService carrega usuários corretamente

---

**Data de Implementação**: 21 de maio de 2026
**Status**: ✅ PRONTO PARA PRODUÇÃO
