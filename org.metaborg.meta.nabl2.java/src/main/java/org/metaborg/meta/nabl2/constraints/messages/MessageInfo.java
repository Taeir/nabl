package org.metaborg.meta.nabl2.constraints.messages;

import org.immutables.serial.Serial;
import org.immutables.value.Value;
import org.metaborg.meta.nabl2.stratego.TermIndex;
import org.metaborg.meta.nabl2.terms.ITerm;
import org.metaborg.meta.nabl2.terms.Terms.IMatcher;
import org.metaborg.meta.nabl2.terms.Terms.M;

@Value.Immutable
@Serial.Version(value = 42L)
public abstract class MessageInfo implements IMessageInfo {

    @Value.Parameter @Override public abstract MessageKind getKind();

    @Value.Parameter @Override public abstract IMessageContent getContent();

    @Value.Parameter @Override public abstract ITerm getOriginTerm();

    @Override public IMessageInfo withDefault(IMessageContent defaultContent) {
        return ImmutableMessageInfo.of(getKind(), getContent().withDefault(defaultContent), getOriginTerm());
    }

    public static IMatcher<MessageInfo> matcher() {
        return M.appl3("Message", MessageKind.matcher(), MessageContent.matcher(), M.term(),
            (appl, kind, message, origin) -> {
                return ImmutableMessageInfo.of(kind, message, origin);
            });
    }

    public static IMatcher<MessageInfo> matcherOnlyOriginTerm() {
        return M.term(MessageInfo::of);
    }

    public static MessageInfo of(ITerm originTerm) {
        return ImmutableMessageInfo.of(MessageKind.ERROR, MessageContent.of(), originTerm);
    }

    @Override public String toString() {
        return getKind().name().toLowerCase() + " " + getContent().toString() + " " + TermIndex.get(getOriginTerm());
    }

}