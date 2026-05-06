package de.darkatra.springboot

/**
 * Represents a command that can be executed within the application.
 * Commands are used to encapsulate all information needed to perform a specific operation.
 *
 * @param R The result type that will be returned after the command is executed.
 */
interface Command<R>
