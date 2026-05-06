[![Build & Release](https://github.com/DarkAtra/spring-boot-command-starter/actions/workflows/build.yml/badge.svg)](https://github.com/DarkAtra/spring-boot-command-starter/actions/workflows/build.yml)

# spring-boot-command-starter

## About

This is a Spring Boot starter that provides an easy-to-use command bus implementation.
It lets you model application actions as typed commands, handle each command in a dedicated Spring bean, and dispatch commands through a single
`CommandDispatcher`.

## Usage

### Add the starter

Add the starter to your Spring Boot application:

[//]: # (@formatter:off)
```xml
<dependency>
    <groupId>de.darkatra.springboot</groupId>
    <artifactId>spring-boot-command-starter</artifactId>
    <version>1.0.0</version> <!-- please check if this is the current version -->
</dependency>
```
[//]: # (@formatter:on)

### Create a command

A command contains the data needed to perform an action. The generic type defines the result of the command.

[//]: # (@formatter:off)
```kotlin
import de.darkatra.springboot.Command

data class CreateUserCommand(
    val username: String,
    val email: String
) : Command<String>
```
[//]: # (@formatter:on)

### Create a command handler

Create the `CommandHandler` for the command. The handler performs the work and returns the command result.

[//]: # (@formatter:off)
```kotlin
import de.darkatra.springboot.CommandHandler
import org.springframework.stereotype.Component

@Component
class CreateUserCommandHandler : CommandHandler<CreateUserCommand, String> {

    override fun handle(command: CreateUserCommand): String {
        // create the user and return its id
        return "user-1234"
    }
}
```
[//]: # (@formatter:on)

Each command must have exactly one command handler. Application startup fails if more than one handler is registered for the same command.

### Dispatch commands

Inject `CommandDispatcher` anywhere in your application and dispatch commands through it.

[//]: # (@formatter:off)
```kotlin
import de.darkatra.springboot.CommandDispatcher
import org.springframework.stereotype.Service

@Service
class UserService(
    private val commandDispatcher: CommandDispatcher
) {

    fun createUser(username: String, email: String): String {
        return commandDispatcher.dispatch(CreateUserCommand(username, email))
    }
}
```
[//]: # (@formatter:on)

The dispatcher resolves the matching handler and returns the typed result. If no handler is registered for a command, an `IllegalStateException` is thrown.
