package de.darkatra.springboot

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.core.GenericTypeResolver

@AutoConfiguration
class CommandAutoConfiguration(
    private val commandHandlers: List<CommandHandler<out Command<*>, *>>,
    private val commandValidators: List<CommandValidator<out Command<*>, *>>,
) {

    @Bean
    fun commandDispatcher(): CommandDispatcher {

        val commandToCommandHandlerMap: Map<Class<out Command<*>>, CommandHandler<out Command<*>, *>> = buildMap {
            commandHandlers.forEach { commandHandler ->

                // resolve the generics for the CommandHandler interface
                val generics = GenericTypeResolver.resolveTypeArguments(commandHandler.javaClass, CommandHandler::class.java)
                check(!(generics == null || generics.size != 2)) {
                    "Expected CommandHandler interface to have exactly two generics. Found: ${generics.contentToString()}"
                }

                @Suppress("UNCHECKED_CAST")
                val commandType = generics[0] as Class<out Command<*>?>
                check(!containsKey(commandType)) {
                    "Each Command can have at most one CommandHandler. There was an attempt to register an additional CommandHandler for Command: ${commandType.getSimpleName()}"
                }

                put(commandType, commandHandler)
            }
        }

        val commandToCommandValidatorMap: Map<Class<out Command<*>>, CommandValidator<out Command<*>, *>> = buildMap {
            commandValidators.forEach { commandValidator ->

                // resolve the generics for the CommandValidator interface
                val generics = GenericTypeResolver.resolveTypeArguments(commandValidator.javaClass, CommandValidator::class.java)
                check(!(generics == null || generics.size != 2)) {
                    "Expected CommandValidator interface to have exactly two generics. Found: ${generics.contentToString()}"
                }

                @Suppress("UNCHECKED_CAST")
                val commandType = generics[0] as Class<out Command<*>?>
                check(!containsKey(commandType)) {
                    "Each Command can have at most one CommandValidator. There was an attempt to register an additional CommandValidator for Command: ${commandType.getSimpleName()}"
                }

                put(commandType, commandValidator)
            }
        }

        return CommandDispatcher(
            commandToCommandHandlerMap = commandToCommandHandlerMap,
            commandToCommandValidatorMap = commandToCommandValidatorMap,
        )
    }
}
