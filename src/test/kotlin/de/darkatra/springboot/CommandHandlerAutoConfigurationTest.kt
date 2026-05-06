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
    internal fun `should successfully create command dispatcher`() {

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

    class CreateCommand : Command<String>

    class CreateCommandHandler : CommandHandler<CreateCommand, String> {

        lateinit var handledCommand: CreateCommand

        override fun handle(command: CreateCommand): String {
            handledCommand = command
            return "test"
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
