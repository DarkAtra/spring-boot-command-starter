package de.darkatra.springboot

/**
 * Command validators are responsible for validating commands before they are
 * dispatched to their corresponding [CommandHandler].
 *
 * @param C The type of command to validate. Must implement the [Command] interface.
 * @param R The result type produced after handling the command. Must match the type defined by the command.
 */
interface CommandValidator<C : Command<R>, R> {

    /**
     * Validates a given command before it is processed by its corresponding [CommandHandler].
     *
     * This method should throw a [CommandValidationException] if validation fails.
     * Any exception thrown during validation is caught by the [CommandDispatcher]
     * and wrapped in a [CommandValidationException] if necessary.
     *
     * @param command The command to validate. The command must implement the [Command] interface.
     * @throws CommandValidationException If the command validation fails, providing details about the validation error.
     */
    @Throws(CommandValidationException::class)
    fun validate(command: C)
}
