# Menssager

Um aplicativo de mensageria criptografada com interface gráfica Swing que usa árvores binárias de busca (BST) para transformar mensagens em estruturas de dados criptografáveis.

## Visão Gerale

O BinaryEncrypt é um aplicativo desktop Java que permite aos usuários:
- Digitar mensagens de texto
- Transformar mensagens em árvores binárias de busca (BST)
- Visualizar a árvore binária gerada
- Criptografar a árvore serializada usando o padrão de criptografia por decorador
- Salvar mensagens criptografadas em arquivos
- Descriptografar mensagens previamente salvas
- Verificar e visualizar o histórico de mensagens

## Funcionalidades

- **Interface Gráfica Moderna**: Tema escuro com cores agradáveis e layout responsivo
- **Transformação BST**: Converte texto em árvores binárias de busca para processamento
- **Visualização Árvore**: Exibe a estrutura da BST gerada a partir da mensagem
- **Criptografia Segura**: Usa criptografia XOR com chave mestra aleatória + Base64
- **Salvar/Carregar**: Permite salvar mensagens criptografadas e carregá-las posteriormente
- **Histórico de Mensagens**: Mantém registro das mensagens enviadas e recebidas
- **Verificação de Histórico**: Capacidade de carregar e visualizar histórico de sessões anteriores

## Estrutura do Projeto

```
src/main/java/com/edc/binaryencrypt/main/
├── Main.java                    # Classe principal que inicia a aplicação
├── crypto/
│   └── EncryptionDecorator.java # Implementação do padrão Decorator para criptografia
├── test/
│   ├── TestEncryption.java      # Testes de criptografia
│   ├── VerifyEncryption.java    # Verificação de criptografia
│   ├── DebugBST.java            # Debug da BST
│   ├── SessionPersistenceTest.java # Teste de persistência de sessão
│   └── HistoryBugTest.java      # Teste de correção de bug no histórico
├── trees/
│   └── BinaryTree.java          # Implementação da árvore binária de busca
└── ui/
    ├── MainWindow.java          # Janela principal da interface Swing
    └── BinaryTreeVisualizer.java # Componente para visualização da BST
```

## Pré-requisitos

- Java JDK 21 ou superior
- Maven 3.6+ (para build)

## Como Executar

### Usando Maven

```bash
# Compilar e executar
mvn clean compile exec:java -Dexec.mainClass=com.edc.binaryencrypt.main.Main

# Ou criar um JAR executável
mvn clean package
java -jar target/Main-0.1.jar
```

### Executando Diretamente

Se você já tem as classes compiladas:

```bash
java -cp target/classes com.edc.binaryencrypt.main.Main
```

## Como Usar

1. **Digitar Mensagem**: Na área de texto na parte inferior direita, digite sua mensagem
2. **Enviar**: Clique no botão "Enviar ->" para transformar a mensagem em uma BST e visualizá-la
3. **Salvar/Criptografar**: Após enviar, clique em "Salvar/Cript. ->" para salvar a mensagem criptografada
4. **Descriptografar**: Para ler uma mensagem salva, clique em "Descriptografar ->" e selecione o arquivo
5. **Verificar Histórico**: Use o botão "Verificar Histórico" para carregar e visualizar mensagens de sessões anteriores

## Detalhes Técnicos

### Criptografia

O aplicativo usa uma combinação de:
- **Criptografia XOR**: Com uma chave mestra aleatória gerada na inicialização
- **Codificação Base64**: Para representar os dados criptografados como texto seguro
- **Padrão Decorator**: Implementado em `EncryptionDecorator` para adicionar funcionalidades de criptografia à serialização da BST

### Árvore Binária de Busca (BST)

Cada mensagem é convertida em uma BST onde:
- Cada caractere da mensagem se torna um nó na árvore
- A árvore é construída inserindo caracteres em ordem (comparação lexicográfica)
- A árvore pode ser serializada em pré-ordem para criptografia
- A mensagem original pode ser recuperada percorrendo a árvore em ordem

## Licença

Este projeto está licenciado sob a licença MIT - veja o arquivo LICENSE para detalhes.

## Contribuindo

Contribuições são bem-vindas! Por favor, siga estas etapas:

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## Contato

Seu Nome - [seuemail@exemplo.com](mailto:seuemail@exemplo.com)

Link do Projeto: [https://github.com/seusuario/BinaryEncrypt](https://github.com/seusuario/BinaryEncrypt)
