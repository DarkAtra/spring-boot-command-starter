package de.darkatra.springboot

/**
 * Dispatches commands to their corresponding command handlers, ensuring that each command is processed
 * by the appropriate handler registered in the system. This class serves as the central mechanism for
 * executing commands and managing the relationship between commands and their handlers.
 */
class CommandDispatcher internal constructor(
    private val commandToCommandHandlerMap: Map<Class<out Command<*>>, CommandHandler<out Command<*>, *>> = emptyMap()
) {

    fun <C : Command<R>, R> dispatch(command: C): R {

        @Suppress("UNCHECKED_CAST")
        val commandHandler = commandToCommandHandlerMap[command.javaClass] as CommandHandler<C, R>?
            ?: throw IllegalStateException("No CommandHandler found for Command: ${command.javaClass}")

        return commandHandler.handle(command)
    }
}
