# RequestMNGR

Aplicação interna para registrar requisições de compra. O projeto usa Spring Boot, JDBC, PostgreSQL e migrations Flyway.

## Primeiro ADMIN de desenvolvimento

O sistema não usa a senha temporária do Spring Boot. Na primeira inicialização, quando ainda não houver usuários na tabela `app_users`, configure estas variáveis no arquivo local `.env` antes de iniciar a aplicação:

```properties
APP_INITIAL_ADMIN_USERNAME=admin
APP_INITIAL_ADMIN_PASSWORD=uma-senha-local-segura
APP_INITIAL_ADMIN_DISPLAY_NAME=Administrador local
```

O `.env` já é ignorado pelo Git e agora é carregado explicitamente na inicialização. O usuário é criado uma única vez com a senha protegida por BCrypt. Depois disso, as variáveis não são usadas para alterar a conta existente.

## Login e logout

1. Abra `http://localhost:8081/login.html` (ou a porta definida em `SERVER_PORT`).
2. Entre com o usuário inicial configurado.
3. A página principal mostra o usuário e a role autenticados.
4. Use **Sair** para encerrar a sessão.

O frontend obtém um token CSRF em `/api/csrf` e o envia no login, logout e criação de requisição. Requisições que mudam estado sem esse token são rejeitadas.
