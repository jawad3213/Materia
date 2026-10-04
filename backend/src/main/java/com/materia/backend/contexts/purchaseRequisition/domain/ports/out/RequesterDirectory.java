package com.materia.backend.contexts.purchaseRequisition.domain.ports.out;

/** How a requester is named on a requisition, looked up from their account. */
public interface RequesterDirectory {

    /** The user's full name, else their e-mail, else the id itself when no account matches. */
    String displayName(String userId);
}
