package org.apereo.cas.rest.factory;

import org.apereo.cas.ticket.Ticket;

import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link TicketGrantingTicketResourceEntityResponseFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@FunctionalInterface
public interface TicketGrantingTicketResourceEntityResponseFactory {

    /**
     * Build response response entity.
     *
     * @param ticketGrantingTicket the ticket granting ticket
     * @param request              the request
     * @return the response entity
     * @throws Exception the exception
     */
    ResponseEntity<String> build(Ticket ticketGrantingTicket, HttpServletRequest request) throws Exception;
}
