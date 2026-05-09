package de.darkatra.springboot

/**
 * Represents an exception that occurs during the validation of a command.
 *
 * This exception is typically thrown the [CommandDispatcher] when an exception is encountered
 * during the validation of a command.
 *
 * @param message A detailed message describing the validation error.
 * @param command The command instance that failed validation.
 * @param cause The underlying exception that caused the validation failure, if any.
 */
open class CommandValidationException(
    message: String,
    val command: Command<*>,
    cause: Exception? = null,
) : RuntimeException(message, cause)
