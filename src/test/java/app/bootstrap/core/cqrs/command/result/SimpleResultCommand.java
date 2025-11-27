/*
 * App Bootstrap Core
 */
package app.bootstrap.core.cqrs.command.result;

import app.bootstrap.core.cqrs.IResultCommand;

public record SimpleResultCommand(String payload) implements IResultCommand<String> {}
