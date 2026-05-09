package de.darkatra.springboot

/**
 * Dispatches commands to their corresponding [CommandHandler] and optionally validates them using the associated [CommandValidator].
 *
 * @param commandToCommandHandlerMap A map that associates each command type with its corresponding handler.
 * @param commandToCommandValidatorMap A map that associates each command type with its corresponding validator.
 */
class CommandDispatcher internal constructor(
    private val commandToCommandHandlerMap: Map<Class<out Command<*>>, CommandHandler<out Command<*>, *>> = emptyMap(),
    private val commandToCommandValidatorMap: Map<Class<out Command<*>>, CommandValidator<out Command<*>, *>> = emptyMap(),
) {

    /**
     * Dispatches a given [Command] to its associated [CommandHandler] and optionally validates it using the corresponding [CommandValidator].
     *
     * @param command The command to dispatch.
     * @return The result of the command.
     * @throws CommandValidationException If the command validation fails or an exception occurs during validation.
     * @throws IllegalStateException If no command handler is found for the provided command.
     */
    fun <C : Command<R>, R> dispatch(command: C): R {

        @Suppress("UNCHECKED_CAST")
        val commandValidator = commandToCommandValidatorMap[command.javaClass] as CommandValidator<C, R>?
        try {
            commandValidator?.validate(command)
        } catch (e: Exception) {
            if (e is CommandValidationException) {
                throw e
            }
            throw CommandValidationException("Exception validating Command: ${command.javaClass.simpleName}", command, e)
        }

        @Suppress("UNCHECKED_CAST")
        val commandHandler = commandToCommandHandlerMap[command.javaClass] as CommandHandler<C, R>?
            ?: throw IllegalStateException("No CommandHandler found for Command: ${command.javaClass.simpleName}")

        return commandHandler.handle(command)
    }
}
