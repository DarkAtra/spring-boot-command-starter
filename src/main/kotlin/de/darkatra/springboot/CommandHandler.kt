package de.darkatra.springboot

/**
 * Command handlers are responsible for processing commands, which encapsulate all
 * necessary details for executing a specific operation. Each implementation of this
 * interface is tied to a specific command type and its corresponding result type.
 *
 * @param C The type of command to handle. Must implement the [Command] interface.
 * @param R The result type produced after handling the command. Must match the type defined by the command.
 */
interface CommandHandler<C : Command<R>, R> {

    fun handle(command: C): R
}
