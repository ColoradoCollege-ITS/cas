package org.apereo.cas.ticket.registry.compact;

import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import lombok.RequiredArgsConstructor;

/**
 * This is {@link TicketGrantingTicketCompactor}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
public class TicketGrantingTicketCompactor extends BaseTicketCompactor<TicketGrantingTicket> {
    private final TicketSerializationManager ticketSerializationManager;

    @Override
    public String compact(final Ticket ticket) throws Exception {
        return ticketSerializationManager.serializeTicket(ticket);
    }

    @Override
    public Class<TicketGrantingTicket> getTicketType() {
        return TicketGrantingTicket.class;
    }

    @Override
    public Ticket expand(final String ticketId) {
        return ticketSerializationManager.deserializeTicket(ticketId, getTicketType());
    }
}
