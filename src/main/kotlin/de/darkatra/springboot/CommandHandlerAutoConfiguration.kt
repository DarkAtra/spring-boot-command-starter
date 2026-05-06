package de.darkatra.springboot

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.core.GenericTypeResolver

@AutoConfiguration
class CommandAutoConfiguration(
    private val commandHandlers: List<CommandHandler<out Command<*>, *>>
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
                    "Commands can only have one CommandHandler. There was an attempt to register an additional CommandHandler for Command: ${commandType.getSimpleName()}"
                }

                put(commandType, commandHandler)
            }
        }

        return CommandDispatcher(commandToCommandHandlerMap)
    }
}
