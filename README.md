<p align="center"><img src="https://github.com/arieslab/jnose-core/blob/main/logo.png?raw=true" width="70"></p>

# JNose-Core

Biblioteca Java para detecção automática de **Test Smells** em código de teste.

## Test Smells Detectados

| Test Smell | Descrição |
|---|---|
| Assertion Roulette | Múltiplas asserções sem mensagem identificadora |
| Conditional Test Logic | Testes com lógica condicional (if/switch/loop) |
| Constructor Initialization | Inicialização via construtor em vez de @Before |
| Default Test | Teste que apenas verifica o fluxo padrão |
| Dependent Test | Testes que dependem da execução de outros |
| Duplicate Assert | Asserções duplicadas no mesmo teste |
| Eager Test | Teste que verifica múltiplos métodos |
| Empty Test | Teste sem corpo |
| Exception Catching Throwing | Captura e relançamento de exceção no teste |
| General Fixture | Fixture muito genérica compartilhada entre testes |
| Ignored Test | Teste ignorado/skipped |
| Lazy Test | Teste que não verifica o resultado |
| Magic Number Test | Uso de números mágicos em asserções |
| Mystery Guest | Teste que acessa recursos externos (arquivos, rede, banco) |
| Print Statement | Uso de System.out/System.err no teste |
| Redundant Assertion | Asserções redundantes |
| Resource Optimism | Otimismo ao acessar recursos sem verificação |
| Sensitive Equality | Comparação de igualdade usando toString() |
| Sleepy Test | Uso de Thread.sleep() no teste |
| Unknown Test | Teste que não segue padrões JUnit |
| Verbose Test | Teste com excesso de linhas / log verboso |

## Suporte a JUnit

- JUnit 3 (junit.framework)
- JUnit 4 (@Test)
- JUnit 5 (org.junit.jupiter)

## Como usar

### CLI (JAR executável)

```bash
java -jar jnose-core-<versao>-jar-with-dependencies.jar <caminho-do-projeto>
```

### Como dependência Maven

Adicione o repositório GitHub Packages no seu `pom.xml`:

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/arieslab/jnose-core</url>
    </repository>
</repositories>
```

```xml
<dependency>
    <groupId>br.ufba.jnose</groupId>
    <artifactId>jnose-core</artifactId>
    <version>0.9.0</version>
</dependency>
```

### Uso programático

```java
Config config = new Config() {
    public boolean assertionRoulette() { return true; }
    public boolean conditionalTestLogic() { return true; }
    // habilite/desabilite cada test smell
    public int maxStatements() { return 50; }
};

JNoseCore jnose = new JNoseCore(config);
List<TestClass> results = jnose.getFilesTest("/caminho/do/projeto");

for (TestClass tc : results) {
    System.out.println(tc.getName() + " - " + tc.getListTestSmell().size() + " smells");
}
```

## Build

```bash
mvn clean package
```

Gera:
- `target/jnose-core-<versao>.jar`
- `target/jnose-core-<versao>-jar-with-dependencies.jar` (com todas as dependências)

## Licença

Apache License 2.0

## Contato

- tassiovirginio@gmail.com
- tassio.virginio@ifto.edu.br
