package de.darkatra.springboot

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.beans.factory.getBean
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.assertj.AssertableApplicationContext
import org.springframework.boot.test.context.runner.ApplicationContextRunner

internal class CommandHandlerAutoConfigurationTest {

    @Test
    internal fun `should successfully create command dispatcher and handle commands`() {

        ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CommandAutoConfiguration::class.java))
            .withBean(CreateCommandHandler::class.java)
            .withBean(UpdateCommandHandler::class.java)
            .run { context: AssertableApplicationContext ->

                assertThat(context).hasNotFailed()

                val createCommandHandler = assertDoesNotThrow { context.getBean<CreateCommandHandler>() }
                val updateCommandHandler = assertDoesNotThrow { context.getBean<UpdateCommandHandler>() }

                val commandDispatcher = assertDoesNotThrow { context.getBean<CommandDispatcher>() }

                // create
                val createCommand = CreateCommand()

                val result = commandDispatcher.dispatch(createCommand)

                assertThat(result).isEqualTo("test")
                assertThat(createCommandHandler.handledCommand).isEqualTo(createCommand)

                // update
                val updateCommand = UpdateCommand()

                commandDispatcher.dispatch(updateCommand)

                assertThat(updateCommandHandler.handledCommand).isEqualTo(updateCommand)
            }
    }

    @Test
    internal fun `should fail to handle command without command handler`() {

        ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CommandAutoConfiguration::class.java))
            .run { context: AssertableApplicationContext ->
                assertThat(context).hasNotFailed()

                assertThrows<NoSuchBeanDefinitionException> {
                    context.getBean<CreateCommandHandler>()
                }

                val commandDispatcher = assertDoesNotThrow { context.getBean<CommandDispatcher>() }

                val createCommand = CreateCommand()

                assertThrows<IllegalStateException> {
                    commandDispatcher.dispatch(createCommand)
                }
            }
    }

    @Test
    internal fun `should fail to register more than one command handler per command`() {

        ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CommandAutoConfiguration::class.java))
            .withBean(CreateCommandHandler::class.java)
            .withBean(SecondCreateCommandHandler::class.java)
            .run { context: AssertableApplicationContext ->
                assertThat(context).hasFailed().failure
                    .message().contains("There was an attempt to register an additional CommandHandler for Command: CreateCommand")
            }
    }

    @Test
    internal fun `should fail to register more than one command validator per command`() {

        ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CommandAutoConfiguration::class.java))
            .withBean(CreateCommandHandler::class.java)
            .withBean(SucceedingCreateCommandValidator::class.java)
            .withBean(FailingCreateCommandValidator::class.java)
            .run { context: AssertableApplicationContext ->
                assertThat(context).hasFailed().failure
                    .message().contains("There was an attempt to register an additional CommandValidator for Command: CreateCommand")
            }
    }

    @Test
    internal fun `should successfully validate and handle command`() {

        ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CommandAutoConfiguration::class.java))
            .withBean(CreateCommandHandler::class.java)
            .withBean(SucceedingCreateCommandValidator::class.java)
            .run { context: AssertableApplicationContext ->

                assertThat(context).hasNotFailed()

                val createCommandHandler = assertDoesNotThrow { context.getBean<CreateCommandHandler>() }
                val createCommandValidator = assertDoesNotThrow { context.getBean<SucceedingCreateCommandValidator>() }

                val commandDispatcher = assertDoesNotThrow { context.getBean<CommandDispatcher>() }

                // create
                val createCommand = CreateCommand()

                val result = commandDispatcher.dispatch(createCommand)

                assertThat(result).isEqualTo("test")
                assertThat(createCommandHandler.handledCommand).isEqualTo(createCommand)
                assertThat(createCommandValidator.validatedCommand).isEqualTo(createCommand)
            }
    }

    @Test
    internal fun `should fail to validate and handle command`() {

        ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CommandAutoConfiguration::class.java))
            .withBean(CreateCommandHandler::class.java)
            .withBean(FailingCreateCommandValidator::class.java)
            .run { context: AssertableApplicationContext ->

                assertThat(context).hasNotFailed()

                val createCommandHandler = assertDoesNotThrow { context.getBean<CreateCommandHandler>() }
                assertDoesNotThrow { context.getBean<FailingCreateCommandValidator>() }

                val commandDispatcher = assertDoesNotThrow { context.getBean<CommandDispatcher>() }

                // create
                val createCommand = CreateCommand()

                val commandValidationException = assertThrows<CommandValidationException> {
                    commandDispatcher.dispatch(createCommand)
                }

                assertThat(commandValidationException.message).isEqualTo("Simulated for tests")
                assertThat(commandValidationException.command).isEqualTo(createCommand)
                assertThat(createCommandHandler.handledCommand).isNull()
            }
    }

    @Test
    internal fun `should fail validation and always return a CommandValidationException`() {

        ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CommandAutoConfiguration::class.java))
            .withBean(CreateCommandHandler::class.java)
            .withBean(NonCommandValidationExceptionFailingCreateCommandValidator::class.java)
            .run { context: AssertableApplicationContext ->

                assertThat(context).hasNotFailed()

                val createCommandHandler = assertDoesNotThrow { context.getBean<CreateCommandHandler>() }
                assertDoesNotThrow { context.getBean<NonCommandValidationExceptionFailingCreateCommandValidator>() }

                val commandDispatcher = assertDoesNotThrow { context.getBean<CommandDispatcher>() }

                // create
                val createCommand = CreateCommand()

                val commandValidationException = assertThrows<CommandValidationException> {
                    commandDispatcher.dispatch(createCommand)
                }

                assertThat(commandValidationException.message).isEqualTo("Exception validating Command: CreateCommand")
                assertThat(commandValidationException.command).isEqualTo(createCommand)
                assertThat(createCommandHandler.handledCommand).isNull()
            }
    }

    class CreateCommand : Command<String>

    class CreateCommandHandler : CommandHandler<CreateCommand, String> {

        var handledCommand: CreateCommand? = null

        override fun handle(command: CreateCommand): String {
            handledCommand = command
            return "test"
        }
    }

    class SucceedingCreateCommandValidator : CommandValidator<CreateCommand, String> {

        lateinit var validatedCommand: CreateCommand

        override fun validate(command: CreateCommand) {
            validatedCommand = command
        }
    }

    class FailingCreateCommandValidator : CommandValidator<CreateCommand, String> {

        override fun validate(command: CreateCommand) {
            throw CommandValidationException("Simulated for tests", command)
        }
    }

    class NonCommandValidationExceptionFailingCreateCommandValidator : CommandValidator<CreateCommand, String> {

        override fun validate(command: CreateCommand) {
            throw IllegalStateException("Simulated for tests")
        }
    }

    class SecondCreateCommandHandler : CommandHandler<CreateCommand, String> {

        override fun handle(command: CreateCommand): String {
            return "test2"
        }
    }

    class UpdateCommand : Command<Unit>

    class UpdateCommandHandler : CommandHandler<UpdateCommand, Unit> {

        lateinit var handledCommand: UpdateCommand

        override fun handle(command: UpdateCommand) {
            handledCommand = command
        }
    }
}
