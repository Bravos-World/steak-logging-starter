# Logging Starter

A Spring Boot starter library for structured logging and audit logging with built-in support for sensitive data transformation (masking, hashing, and encryption). Logs are sent to Kafka for centralized log management.

## Features

- **Event Logging**: Structured logging with metadata support (INFO, DEBUG, WARN, ERROR levels)
- **Audit Logging**: Track entity changes with old/new value comparison
- **Sensitive Data Protection**: Built-in support for masking, hashing, and encrypting sensitive data
- **Kafka Integration**: Send logs to Kafka topics for centralized log management
- **Async Processing**: Virtual thread-based async log processing
- **Reactive Support**: Support for Mono/Flux return types with `@MutateSensitiveDataAsync`

## Requirements

- Java 25+
- Spring Boot 3.5+
- Apache Kafka

## Installation

### Use jitpack.io

```Gradle (Kotlin DSL)
repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}
```

```Gradle (Groovy)
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}
```

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("com.github.Bravos-World:steak-logging-starter:v1.0.4")
}
```

### Gradle (Groovy)

```groovy
dependencies {
    implementation 'com.github.Bravos-World:steak-logging-starter:v1.0.4'
}
```

## Configuration

Add the following properties to your `application.yml` or `application.properties`:

```yaml
spring:
  application:
    name: your-service-name

# Logging levels configuration
logging:
  info: true   # Enable INFO level logging (default: true)
  debug: false # Enable DEBUG level logging (default: false)
  error: true  # Enable ERROR level logging (default: true)
  warn: true   # Enable WARN level logging (default: true)
  hash:
    key: your-hmac-hash-key # Required for sensitive data hashing
  encrypt:
    key: your-aes-secret-key # Required for sensitive data encryption (Base64 encoded AES key)
```

### Prerequisites

The library requires the following beans to be configured:

1. **KafkaTemplate**: For sending logs to Kafka
2. **Snowflake**: For generating unique IDs (from `steak-utils` library)
3. **ObjectMapper**: For JSON serialization (Jackson's `tools.jackson.databind.ObjectMapper`)

### Auto-Configuration

The library provides the following auto-configured beans:

- **LoggerFactory**: Created when `KafkaTemplate` and `Snowflake` beans are available
- **Audittor**: Created when `KafkaTemplate` and `Snowflake` beans are available  
- **TransformContext**: Created automatically using `logging.hash.key` and `logging.encrypt.key` properties
- **Transformer**: Created when `ObjectMapper` and `TransformContext` beans are available
- **MutateSensitveAspect**: Created when `Transformer` bean is available

### Bean Configuration Example

```java
@Configuration
public class LoggingConfig {

    @Bean
    public Snowflake snowflake() {
        return new Snowflake(1); // your machine ID
    }
    
    // Optional: Override default TransformContext if needed
    @Bean
    public TransformContext transformContext() {
        SecretKey secretKey = // your AES secret key
        return TransformContext.builder()
            .secretProvider(new SecretProvider(secretKey))
            .hashKey("your-hmac-hash-key")
            .build();
    }
}
```

## Usage

### Event Logging

Inject `LoggerFactory` and create a logger for your class:

```java
@Service
@RequiredArgsConstructor
public class UserService {

    private final LoggerFactory loggerFactory;
    private Logger logger;

    @PostConstruct
    public void init() {
        logger = loggerFactory.getLogger(UserService.class);
    }

    public void createUser(String username) {
        // Simple logging
        logger.info("USER_CREATED", "User created successfully");
        
        // Logging with metadata
        logger.info("USER_CREATED", "User created successfully", Map.of(
            "username", username,
            "timestamp", System.currentTimeMillis()
        ));
        
        // Error logging
        try {
            // some operation
        } catch (Exception e) {
            logger.error("USER_CREATION_FAILED", "Failed to create user", e);
        }
    }
}
```

### Logging with Sensitive Data

Create a class extending `SensitiveData` and annotate sensitive fields:

```java
public class UserRegistrationData extends SensitiveData {

    private String username;

    @Sensitive(
        maskHandler = EmailMask.class,
        hashHandler = Hmacsha512Handler.class,
        encryptHandler = AesEncryptionHandler.class,
        ignore = false
    )
    private String email;

    @Sensitive(
        maskHandler = PhonePartialMask.class,
        hashHandler = Hmacsha512Handler.class,
        encryptHandler = AesEncryptionHandler.class,
        ignore = false
    )
    private String phoneNumber;

    @Sensitive(
        maskHandler = BankCardMask.class,
        hashHandler = Hmacsha512Handler.class,
        encryptHandler = AesEncryptionHandler.class,
        ignore = false
    )
    private String creditCard;
}
```

Then use it in logging:

```java
public void registerUser(UserRegistrationData data) {
    logger.info("USER_REGISTERED", "New user registered", data);
}
```

The sensitive data will be automatically transformed to include:
- `masked`: Masked value for display
- `hashed`: Hashed value for searching
- `hash-alg`: Hash algorithm used
- `encrypted`: Encrypted value for secure storage
- `enc-alg`: Encryption algorithm used

### Audit Logging

Use `Audittor` for tracking entity changes:

```java
@Service
@RequiredArgsConstructor
public class AccountService {

    private final Audittor audittor;

    public void updateAccount(Account oldAccount, Account newAccount) {
        AuditData auditData = AuditData.builder()
            .userId(getCurrentUserId())
            .origin(Origin.builder()
                .userAgent("Mozilla/5.0...")
                .ipAddress("192.168.1.1")
                .location("US")
                .build())
            .entityName("Account")
            .entityId(newAccount.getId().toString())
            .action("UPDATE")
            .message("Account information updated")
            .success(true)
            .build();

        // With old and new values as SensitiveData
        audittor.audit(auditData, oldAccountData, newAccountData, Map.of("field", "email"));
        
        // Simple audit without sensitive data
        audittor.audit(auditData);
    }
}
```

## Sensitive Data Annotations

### `@Sensitive`

Mark a field as sensitive with transformation handlers:

```java
@Sensitive(
    ignore = false,                        // Set to false to process the field
    maskHandler = EmailMask.class,         // Mask handler class
    hashHandler = Hmacsha512Handler.class, // Hash handler class
    encryptHandler = AesEncryptionHandler.class // Encrypt handler class
)
private String email;
```

### Built-in Mask Handlers

| Handler | Description | Example |
|---------|-------------|---------|
| `EmailMask` | Masks email addresses | `j****n@example.com` |
| `PhonePartialMask` | Masks phone numbers (partial) | `123***789` |
| `PhoneEndOnlyMask` | Masks phone numbers (end only) | `*******789` |
| `BankCardMask` | Masks bank card numbers | `123456******1234` |
| `BankAccountMask` | Masks bank account numbers | - |
| `NationalIdentityMask` | Masks national ID numbers | - |
| `PassportMask` | Masks passport numbers | - |

### Built-in Hash Handlers

| Handler | Algorithm |
|---------|-----------|
| `Hmacsha512Handler` | HMAC-SHA512 |

### Built-in Encrypt Handlers

| Handler | Algorithm |
|---------|-----------|
| `AesEncryptionHandler` | AES |

## Custom Handlers

### Custom Mask Handler

```java
public class CustomMask implements MaskHandler {
    
    @Override
    public String transform(String value, TransformContext context) {
        // Your custom masking logic
        return value.substring(0, 2) + "***" + value.substring(value.length() - 2);
    }
}
```

### Custom Hash Handler

```java
public class CustomHashHandler implements HashHandler {
    
    @Override
    public String algorithm() {
        return "CUSTOM-HASH";
    }
    
    @Override
    public String transform(String value, TransformContext context) {
        // Your custom hashing logic
        return customHash(value, context.getHashKey());
    }
}
```

### Custom Encrypt Handler

```java
public class CustomEncryptHandler implements EncryptHandler {
    
    @Override
    @NonNull
    public String algorithm() {
        return "CUSTOM-ENC";
    }
    
    @Override
    public String transform(String value, TransformContext context) {
        // Your custom encryption logic
        return customEncrypt(value, context.getSecretProvider().getSecretKey());
    }
}
```

## Kafka Topics

| Topic | Description |
|-------|-------------|
| `event.log` | Event logs (configurable via `LoggerFactory`) |
| `audit.log` | Audit logs (configurable via `Audittor`) |

## Log Models

### EventLog

```json
{
  "id": 123456789,
  "level": "INFO",
  "eventName": "USER_CREATED",
  "message": "[com.example.UserService] [main] User created successfully",
  "service": "user-service",
  "exceptionTrace": null,
  "metadata": {
    "username": "john_doe"
  },
  "timestamp": 1736640000000
}
```

### AuditLog

```json
{
  "id": 123456789,
  "userId": 1,
  "origin": {
    "userAgent": "Mozilla/5.0...",
    "ipAddress": "192.168.1.1",
    "location": "US"
  },
  "service": "user-service",
  "entityName": "Account",
  "entityId": "12345",
  "action": "UPDATE",
  "oldValue": "{...}",
  "newValue": "{...}",
  "metadata": "{...}",
  "message": "Account updated",
  "success": true,
  "reason": null,
  "timestamp": 1736640000000
}
```

## Reactive Support

For reactive applications using Project Reactor, use `@MutateSensitiveDataAsync`:

```java
@MutateSensitiveDataAsync
public Mono<Void> processData(SensitiveData data) {
    // Sensitive data will be transformed asynchronously
    return Mono.just(data)
        .flatMap(this::process);
}
```

## License

See [LICENSE](LICENSE) file.

