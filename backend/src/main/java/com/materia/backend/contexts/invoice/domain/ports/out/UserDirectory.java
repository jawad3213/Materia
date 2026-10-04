package com.materia.backend.contexts.invoice.domain.ports.out;

/** Looks up how a user is named on an invoice, from their account rather than from the request. */
public interface UserDirectory {

    /** The user's full name, else their e-mail, else the id itself when no account matches. */
    String displayName(String userId);
}
